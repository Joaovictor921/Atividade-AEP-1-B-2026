package br.com.unicesumar.monitoramento.dao;

import br.com.unicesumar.monitoramento.config.DatabaseConnection;
import br.com.unicesumar.monitoramento.model.Incidente;
import br.com.unicesumar.monitoramento.model.Operador;
import br.com.unicesumar.monitoramento.model.StatusIncidente;
import br.com.unicesumar.monitoramento.model.Transformador;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Persistencia de Incidente (RF04). */
public class IncidenteDAO {

    private static final String SELECT =
            "SELECT i.id AS incidente_id, i.latitude_ocorrencia, i.longitude_ocorrencia, i.descricao, "
            + "i.horario_interrupcao, i.status_incidente, i.operador_atribuido_id, "
            + "u.nome AS op_nome, u.matricula AS op_matricula, " + EquipamentoDAO.COLUNAS
            + " FROM incidente i JOIN equipamento e ON e.id = i.transformador_associado_id "
            + EquipamentoDAO.JUNCOES + "LEFT JOIN usuario u ON u.id = i.operador_atribuido_id ";

    public Incidente inserir(Incidente i) throws SQLException {
        String sql = "INSERT INTO incidente (transformador_associado_id, operador_atribuido_id, "
                + "latitude_ocorrencia, longitude_ocorrencia, descricao, horario_interrupcao, status_incidente) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING id";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, i.getTransformadorMaisProximo().getId());
            if (i.getOperadorAtribuido() == null) {
                ps.setNull(2, Types.INTEGER);
            } else {
                ps.setInt(2, i.getOperadorAtribuido().getId());
            }
            ps.setDouble(3, i.getLatitudeOcorrencia());
            ps.setDouble(4, i.getLongitudeOcorrencia());
            ps.setString(5, i.getDescricao());
            ps.setObject(6, i.getHorarioInterrupcao());
            ps.setString(7, i.getStatus().name());
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                i.setId(rs.getInt(1));
            }
            return i;
        }
    }

    public Optional<Incidente> buscarPorId(int id) throws SQLException {
        List<Incidente> lista = consultar(SELECT + "WHERE i.id = ?", id);
        return lista.isEmpty() ? Optional.empty() : Optional.of(lista.get(0));
    }

    public List<Incidente> listarAbertos() throws SQLException {
        return consultar(SELECT + "WHERE i.status_incidente <> 'RESOLVIDO' ORDER BY i.horario_interrupcao DESC", null);
    }

    public List<Incidente> listarTodos() throws SQLException {
        return consultar(SELECT + "ORDER BY i.horario_interrupcao DESC", null);
    }

    /** Evita registrar varios incidentes para a mesma queda ainda nao resolvida. */
    public boolean existeAbertoParaTransformador(int transformadorId) throws SQLException {
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT 1 FROM incidente "
                     + "WHERE transformador_associado_id = ? AND status_incidente <> 'RESOLVIDO' LIMIT 1")) {
            ps.setInt(1, transformadorId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /** Persiste o novo status e o operador responsavel. */
    public boolean atualizarStatus(Incidente i) throws SQLException {
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "UPDATE incidente SET status_incidente = ?, operador_atribuido_id = ? WHERE id = ?")) {
            ps.setString(1, i.getStatus().name());
            if (i.getOperadorAtribuido() == null) {
                ps.setNull(2, Types.INTEGER);
            } else {
                ps.setInt(2, i.getOperadorAtribuido().getId());
            }
            ps.setInt(3, i.getId());
            return ps.executeUpdate() > 0;
        }
    }

    private List<Incidente> consultar(String sql, Integer parametro) throws SQLException {
        List<Incidente> lista = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            if (parametro != null) {
                ps.setInt(1, parametro);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Incidente i = new Incidente(rs.getInt("incidente_id"), rs.getDouble("latitude_ocorrencia"),
                            rs.getDouble("longitude_ocorrencia"), rs.getString("descricao"));
                    i.setHorarioInterrupcao(rs.getObject("horario_interrupcao", LocalDateTime.class));
                    i.setStatus(StatusIncidente.valueOf(rs.getString("status_incidente")));
                    i.setTransformadorMaisProximo((Transformador) EquipamentoDAO.mapear(rs));
                    int opId = rs.getInt("operador_atribuido_id");
                    if (!rs.wasNull()) {
                        i.setOperadorAtribuido(new Operador(opId, rs.getString("op_nome"),
                                rs.getString("op_matricula"), null, null));
                    }
                    lista.add(i);
                }
            }
        }
        return lista;
    }
}
