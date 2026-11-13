package br.com.unicesumar.monitoramento.dao;

import br.com.unicesumar.monitoramento.config.DatabaseConnection;
import br.com.unicesumar.monitoramento.model.Equipamento;
import br.com.unicesumar.monitoramento.model.Transformador;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** CRUD da entidade principal: Transformador (tabelas EQUIPAMENTO + TRANSFORMADOR, em transacao). */
public class TransformadorDAO implements CrudDAO<Transformador> {

    private static final String SELECT = "SELECT " + EquipamentoDAO.COLUNAS + " FROM equipamento e "
            + "JOIN transformador t ON t.equipamento_id = e.id "
            + "LEFT JOIN poste p ON p.equipamento_id = e.id ";

    @Override
    public Transformador inserir(Transformador t) throws SQLException {
        try (Connection c = DatabaseConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                int id = EquipamentoDAO.inserirBase(c, t);
                String sql = "INSERT INTO transformador (equipamento_id, identificador_unico, "
                        + "capacidade_nominal, data_ultima_manutencao) VALUES (?, ?, ?, ?)";
                try (PreparedStatement ps = c.prepareStatement(sql)) {
                    ps.setInt(1, id);
                    ps.setString(2, t.getIdentificadorUnico());
                    ps.setDouble(3, t.getCapacidadeNominal());
                    ps.setObject(4, t.getDataUltimaManutencao());
                    ps.executeUpdate();
                }
                c.commit();
                t.setId(id);
                return t;
            } catch (SQLException e) {
                c.rollback();
                throw e;
            }
        }
    }

    @Override
    public Optional<Transformador> buscarPorId(int id) throws SQLException {
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(SELECT + "WHERE e.id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of((Transformador) EquipamentoDAO.mapear(rs)) : Optional.empty();
            }
        }
    }

    @Override
    public List<Transformador> listarTodos() throws SQLException {
        return consultar(SELECT + "ORDER BY e.id");
    }

    public List<Transformador> listarAtivos() throws SQLException {
        return consultar(SELECT + "WHERE e.ativo = TRUE ORDER BY e.id");
    }

    @Override
    public boolean atualizar(Transformador t) throws SQLException {
        try (Connection c = DatabaseConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                int base = EquipamentoDAO.atualizarBase(c, t);
                String sql = "UPDATE transformador SET identificador_unico = ?, capacidade_nominal = ?, "
                        + "data_ultima_manutencao = ? WHERE equipamento_id = ?";
                int especifico;
                try (PreparedStatement ps = c.prepareStatement(sql)) {
                    ps.setString(1, t.getIdentificadorUnico());
                    ps.setDouble(2, t.getCapacidadeNominal());
                    ps.setObject(3, t.getDataUltimaManutencao());
                    ps.setInt(4, t.getId());
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
        String sql = "DELETE FROM equipamento e USING transformador t WHERE t.equipamento_id = e.id AND e.id = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private List<Transformador> consultar(String sql) throws SQLException {
        List<Transformador> lista = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Equipamento e = EquipamentoDAO.mapear(rs);
                lista.add((Transformador) e);
            }
        }
        return lista;
    }
}
