package br.com.unicesumar.monitoramento.dao;

import br.com.unicesumar.monitoramento.config.DatabaseConnection;
import br.com.unicesumar.monitoramento.model.Poste;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** CRUD de Poste (tabelas EQUIPAMENTO + POSTE, em transacao). */
public class PosteDAO implements CrudDAO<Poste> {

    private static final String SELECT = "SELECT " + EquipamentoDAO.COLUNAS + " FROM equipamento e "
            + "JOIN poste p ON p.equipamento_id = e.id "
            + "LEFT JOIN transformador t ON t.equipamento_id = e.id ";

    @Override
    public Poste inserir(Poste p) throws SQLException {
        try (Connection c = DatabaseConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                int id = EquipamentoDAO.inserirBase(c, p);
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO poste (equipamento_id, tipo_poste, altura) VALUES (?, ?, ?)")) {
                    ps.setInt(1, id);
                    ps.setString(2, p.getTipoPoste());
                    ps.setDouble(3, p.getAltura());
                    ps.executeUpdate();
                }
                c.commit();
                p.setId(id);
                return p;
            } catch (SQLException e) {
                c.rollback();
                throw e;
            }
        }
    }

    @Override
    public Optional<Poste> buscarPorId(int id) throws SQLException {
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(SELECT + "WHERE e.id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of((Poste) EquipamentoDAO.mapear(rs)) : Optional.empty();
            }
        }
    }

    @Override
    public List<Poste> listarTodos() throws SQLException {
        List<Poste> lista = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(SELECT + "ORDER BY e.id");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add((Poste) EquipamentoDAO.mapear(rs));
            }
        }
        return lista;
    }

    @Override
    public boolean atualizar(Poste p) throws SQLException {
        try (Connection c = DatabaseConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                int base = EquipamentoDAO.atualizarBase(c, p);
                int especifico;
                try (PreparedStatement ps = c.prepareStatement(
                        "UPDATE poste SET tipo_poste = ?, altura = ? WHERE equipamento_id = ?")) {
                    ps.setString(1, p.getTipoPoste());
                    ps.setDouble(2, p.getAltura());
                    ps.setInt(3, p.getId());
                    especifico = ps.executeUpdate();
                }
                c.commit();
                return base > 0 && especifico > 0;
            } catch (SQLException e) {
                c.rollback();
                throw e;
            }
        }
    }

    @Override
    public boolean excluir(int id) throws SQLException {
        String sql = "DELETE FROM equipamento e USING poste p WHERE p.equipamento_id = e.id AND e.id = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }
}
