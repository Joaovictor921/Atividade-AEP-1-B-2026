package br.com.unicesumar.monitoramento.dao;

import br.com.unicesumar.monitoramento.config.DatabaseConnection;
import br.com.unicesumar.monitoramento.model.Alerta;
import br.com.unicesumar.monitoramento.model.Operador;
import br.com.unicesumar.monitoramento.model.TipoAlerta;
import br.com.unicesumar.monitoramento.model.Transformador;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Persistencia de Alerta (RF03). */
public class AlertaDAO {

    private static final String SELECT =
            "SELECT a.id AS alerta_id, a.mensagem, a.horario_geracao, a.tipo_alerta, a.limite_ultrapassado, "
            + "a.operador_destinatario_id, u.nome AS op_nome, u.matricula AS op_matricula, "
            + EquipamentoDAO.COLUNAS + " FROM alerta a JOIN equipamento e ON e.id = a.transformador_origem_id "
            + EquipamentoDAO.JUNCOES + "LEFT JOIN usuario u ON u.id = a.operador_destinatario_id ";

    public Alerta inserir(Alerta a) throws SQLException {
        String sql = "INSERT INTO alerta (transformador_origem_id, operador_destinatario_id, mensagem, "
                + "horario_geracao, tipo_alerta, limite_ultrapassado) VALUES (?, ?, ?, ?, ?, ?) RETURNING id";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, a.getTransformadorOrigem().getId());
            if (a.getOperadorDestinatario() == null) {
                ps.setNull(2, java.sql.Types.INTEGER);
            } else {
                ps.setInt(2, a.getOperadorDestinatario().getId());
            }
            ps.setString(3, a.getMensagem());
            ps.setObject(4, a.getHorarioGeracao());
            ps.setString(5, a.getTipoAlerta().name());
            ps.setDouble(6, a.getLimiteUltrapassado());
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                a.setId(rs.getInt(1));
            }
            return a;
        }
    }

    public Optional<Alerta> buscarPorId(int id) throws SQLException {
        List<Alerta> lista = consultar(SELECT + "WHERE a.id = ?", id);
        return lista.isEmpty() ? Optional.empty() : Optional.of(lista.get(0));
    }

    public List<Alerta> listarRecentes(int limite) throws SQLException {
        return consultar(SELECT + "ORDER BY a.horario_geracao DESC, a.id DESC LIMIT ?", limite);
    }

    public boolean atualizarDestinatario(int alertaId, int operadorId) throws SQLException {
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "UPDATE alerta SET operador_destinatario_id = ? WHERE id = ?")) {
            ps.setInt(1, operadorId);
            ps.setInt(2, alertaId);
            return ps.executeUpdate() > 0;
        }
    }

    private List<Alerta> consultar(String sql, int parametro) throws SQLException {
        List<Alerta> lista = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, parametro);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Transformador t = (Transformador) EquipamentoDAO.mapear(rs);
                    Alerta a = new Alerta(rs.getInt("alerta_id"), rs.getString("mensagem"),
                            rs.getObject("horario_geracao", LocalDateTime.class),
                            TipoAlerta.valueOf(rs.getString("tipo_alerta")),
                            rs.getDouble("limite_ultrapassado"), t);
                    int opId = rs.getInt("operador_destinatario_id");
                    if (!rs.wasNull()) {
                        a.setOperadorDestinatario(new Operador(opId, rs.getString("op_nome"),
                                rs.getString("op_matricula"), null, null));
                    }
                    lista.add(a);
                }
            }
        }
        return lista;
    }
}
