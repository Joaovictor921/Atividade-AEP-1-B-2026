package br.com.unicesumar.monitoramento.dao;

import br.com.unicesumar.monitoramento.config.DatabaseConnection;
import br.com.unicesumar.monitoramento.model.Operador;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Persistencia de Operador (tabelas USUARIO + OPERADOR, em transacao). */
public class OperadorDAO {

    private static final String SELECT = "SELECT u.id, u.nome, u.matricula, u.senha, o.turno "
            + "FROM usuario u JOIN operador o ON o.usuario_id = u.id ";

    /** A senha do objeto ja deve estar em formato hash. */
    public Operador inserir(Operador op) throws SQLException {
        try (Connection c = DatabaseConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                int id;
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO usuario (nome, matricula, senha) VALUES (?, ?, ?) RETURNING id")) {
                    ps.setString(1, op.getNome());
                    ps.setString(2, op.getMatricula());
                    ps.setString(3, op.getSenha());
                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        id = rs.getInt(1);
                    }
                }
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO operador (usuario_id, turno) VALUES (?, ?)")) {
                    ps.setInt(1, id);
                    ps.setString(2, op.getTurno());
                    ps.executeUpdate();
                }
                c.commit();
                op.setId(id);
                return op;
            } catch (SQLException e) {
                c.rollback();
                throw e;
            }
        }
    }

    public Optional<Operador> buscarPorMatricula(String matricula) throws SQLException {
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(SELECT + "WHERE u.matricula = ?")) {
            ps.setString(1, matricula);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        }
    }

    public List<Operador> listarTodos() throws SQLException {
        List<Operador> lista = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(SELECT + "ORDER BY u.id")) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public long contar() throws SQLException {
        try (Connection c = DatabaseConnection.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM operador")) {
            rs.next();
            return rs.getLong(1);
        }
    }

    private Operador mapear(ResultSet rs) throws SQLException {
        return new Operador(rs.getInt("id"), rs.getString("nome"), rs.getString("matricula"),
                rs.getString("senha"), rs.getString("turno"));
    }
}
