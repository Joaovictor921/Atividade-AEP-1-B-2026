package br.com.unicesumar.monitoramento.dao;

import br.com.unicesumar.monitoramento.config.DatabaseConnection;
import br.com.unicesumar.monitoramento.model.ConsumoExcessivo;
import br.com.unicesumar.monitoramento.model.Relatorio;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Consulta de consumo excessivo e persistencia dos relatorios gerados (RF05). */
public class RelatorioDAO {

    /**
     * Transformadores com leituras acima da capacidade nominal no periodo (fim inclusive),
     * com filtro opcional por bairro.
     */
    public List<ConsumoExcessivo> consultarConsumoExcessivo(String bairro, LocalDate inicio, LocalDate fim)
            throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT t.identificador_unico, e.bairro, t.capacidade_nominal, "
                + "MAX(l.consumo_instantaneo) AS maior_consumo, COUNT(*) AS ocorrencias "
                + "FROM leitura_sensor l JOIN sensor s ON s.id = l.sensor_id "
                + "JOIN equipamento e ON e.id = s.equipamento_id "
                + "JOIN transformador t ON t.equipamento_id = e.id "
                + "WHERE l.consumo_instantaneo > t.capacidade_nominal "
                + "AND l.horario_leitura >= ? AND l.horario_leitura < ? ");
        if (bairro != null) {
            sql.append("AND LOWER(e.bairro) = LOWER(?) ");
        }
        sql.append("GROUP BY t.identificador_unico, e.bairro, t.capacidade_nominal ORDER BY maior_consumo DESC");

        List<ConsumoExcessivo> lista = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql.toString())) {
            ps.setTimestamp(1, Timestamp.valueOf(inicio.atStartOfDay()));
            ps.setTimestamp(2, Timestamp.valueOf(fim.plusDays(1).atStartOfDay()));
            if (bairro != null) {
                ps.setString(3, bairro);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new ConsumoExcessivo(rs.getString("identificador_unico"), rs.getString("bairro"),
                            rs.getDouble("capacidade_nominal"), rs.getDouble("maior_consumo"),
                            rs.getLong("ocorrencias")));
                }
            }
        }
        return lista;
    }

    public Relatorio inserir(Relatorio r) throws SQLException {
        String sql = "INSERT INTO relatorio (bairro_filtro, periodo_inicio, periodo_fim, tipo_relatorio, "
                + "conteudo_relatorio) VALUES (?, ?, ?, ?, ?) RETURNING id";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, r.getBairroFiltro());
            ps.setObject(2, r.getPeriodoInicio());
            ps.setObject(3, r.getPeriodoFim());
            ps.setString(4, r.getTipoRelatorio());
            ps.setBytes(5, r.getConteudoBytes());
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                r.setId(rs.getInt(1));
            }
            return r;
        }
    }

    public List<String> listarBairros() throws SQLException {
        List<String> bairros = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT DISTINCT bairro FROM equipamento ORDER BY bairro");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                bairros.add(rs.getString(1));
            }
        }
        return bairros;
    }
}
