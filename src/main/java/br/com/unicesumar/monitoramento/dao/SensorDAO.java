package br.com.unicesumar.monitoramento.dao;

import br.com.unicesumar.monitoramento.config.DatabaseConnection;
import br.com.unicesumar.monitoramento.model.Equipamento;
import br.com.unicesumar.monitoramento.model.Sensor;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** CRUD de Sensor. Ao listar, ja traz o equipamento (polimorfico) e a ultima leitura. */
public class SensorDAO implements CrudDAO<Sensor> {

    private static final String SELECT =
            "SELECT s.id AS sensor_id, s.identificador_dispositivo, s.data_instalacao, s.ativo AS sensor_ativo, "
            + EquipamentoDAO.COLUNAS + ", ul.temperatura, ul.consumo_instantaneo, ul.status_energia "
            + "FROM sensor s JOIN equipamento e ON e.id = s.equipamento_id " + EquipamentoDAO.JUNCOES
            + "LEFT JOIN LATERAL (SELECT l.temperatura, l.consumo_instantaneo, l.status_energia "
            + "FROM leitura_sensor l WHERE l.sensor_id = s.id "
            + "ORDER BY l.horario_leitura DESC, l.id DESC LIMIT 1) ul ON TRUE ";

    @Override
    public Sensor inserir(Sensor s) throws SQLException {
        String sql = "INSERT INTO sensor (identificador_dispositivo, equipamento_id, data_instalacao, ativo) "
                + "VALUES (?, ?, ?, ?) RETURNING id";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, s.getIdentificadorDispositivo());
            ps.setInt(2, s.getEquipamento().getId());
            ps.setObject(3, s.getDataInstalacao());
            ps.setBoolean(4, s.isAtivo());
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                s.setId(rs.getInt(1));
            }
            return s;
        }
    }

    @Override
    public Optional<Sensor> buscarPorId(int id) throws SQLException {
        List<Sensor> lista = consultar(SELECT + "WHERE s.id = ?", id);
        return lista.isEmpty() ? Optional.empty() : Optional.of(lista.get(0));
    }

    @Override
    public List<Sensor> listarTodos() throws SQLException {
        return consultar(SELECT + "ORDER BY s.id", null);
    }

    public List<Sensor> listarAtivos() throws SQLException {
        return consultar(SELECT + "WHERE s.ativo = TRUE AND e.ativo = TRUE ORDER BY s.id", null);
    }

    @Override
    public boolean atualizar(Sensor s) throws SQLException {
        String sql = "UPDATE sensor SET identificador_dispositivo = ?, equipamento_id = ?, "
                + "data_instalacao = ?, ativo = ? WHERE id = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, s.getIdentificadorDispositivo());
            ps.setInt(2, s.getEquipamento().getId());
            ps.setObject(3, s.getDataInstalacao());
            ps.setBoolean(4, s.isAtivo());
            ps.setInt(5, s.getId());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean excluir(int id) throws SQLException {
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM sensor WHERE id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private List<Sensor> consultar(String sql, Integer parametro) throws SQLException {
        List<Sensor> lista = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            if (parametro != null) {
                ps.setInt(1, parametro);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Equipamento eq = EquipamentoDAO.mapear(rs);
                    Sensor s = new Sensor(rs.getInt("sensor_id"), rs.getString("identificador_dispositivo"), eq,
                            rs.getObject("data_instalacao", LocalDate.class), rs.getBoolean("sensor_ativo"));
                    if (rs.getObject("temperatura") != null) {
                        s.setLeituraAtual(rs.getDouble("temperatura"), rs.getDouble("consumo_instantaneo"),
                                rs.getBoolean("status_energia"));
                    }
                    eq.adicionarSensor(s);
                    lista.add(s);
                }
            }
        }
        return lista;
    }
}
