package br.com.unicesumar.monitoramento.dao;

import br.com.unicesumar.monitoramento.config.DatabaseConnection;
import br.com.unicesumar.monitoramento.model.Equipamento;
import br.com.unicesumar.monitoramento.model.Poste;
import br.com.unicesumar.monitoramento.model.Transformador;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Acesso a superclasse EQUIPAMENTO. Carrega polimorficamente Transformador ou Poste
 * (heranca "tabela por subclasse"). Tambem concentra o SQL comum reutilizado pelos demais DAOs.
 */
public class EquipamentoDAO {

    /** Colunas necessarias para montar qualquer Equipamento (alias "e" = equipamento). */
    public static final String COLUNAS = "e.id, e.latitude, e.longitude, e.bairro, e.ativo, "
            + "t.identificador_unico, t.capacidade_nominal, t.data_ultima_manutencao, p.tipo_poste, p.altura";

    public static final String JUNCOES = "LEFT JOIN transformador t ON t.equipamento_id = e.id "
            + "LEFT JOIN poste p ON p.equipamento_id = e.id ";

    private static final String SELECT = "SELECT " + COLUNAS + " FROM equipamento e " + JUNCOES;

    /** Converte a linha atual do ResultSet em Transformador ou Poste. */
    public static Equipamento mapear(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        double latitude = rs.getDouble("latitude");
        double longitude = rs.getDouble("longitude");
        String bairro = rs.getString("bairro");
        boolean ativo = rs.getBoolean("ativo");
        String identificador = rs.getString("identificador_unico");
        if (identificador != null) {
            return new Transformador(id, latitude, longitude, bairro, ativo, identificador,
                    rs.getDouble("capacidade_nominal"),
                    rs.getObject("data_ultima_manutencao", LocalDate.class));
        }
        return new Poste(id, latitude, longitude, bairro, ativo, rs.getString("tipo_poste"), rs.getDouble("altura"));
    }

    /** Insere a linha-base em EQUIPAMENTO dentro de uma transacao e devolve o id gerado. */
    static int inserirBase(Connection c, Equipamento e) throws SQLException {
        String sql = "INSERT INTO equipamento (latitude, longitude, bairro, ativo) VALUES (?, ?, ?, ?) RETURNING id";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDouble(1, e.getLatitude());
            ps.setDouble(2, e.getLongitude());
            ps.setString(3, e.getBairro());
            ps.setBoolean(4, e.isAtivo());
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    static int atualizarBase(Connection c, Equipamento e) throws SQLException {
        String sql = "UPDATE equipamento SET latitude = ?, longitude = ?, bairro = ?, ativo = ? WHERE id = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDouble(1, e.getLatitude());
            ps.setDouble(2, e.getLongitude());
            ps.setString(3, e.getBairro());
            ps.setBoolean(4, e.isAtivo());
            ps.setInt(5, e.getId());
            return ps.executeUpdate();
        }
    }

    public Optional<Equipamento> buscarPorId(int id) throws SQLException {
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(SELECT + "WHERE e.id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        }
    }

    public List<Equipamento> listarTodos() throws SQLException {
        List<Equipamento> lista = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(SELECT + "ORDER BY e.id")) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }
}
