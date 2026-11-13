package br.com.unicesumar.monitoramento.service;

import br.com.unicesumar.monitoramento.dao.TransformadorDAO;
import br.com.unicesumar.monitoramento.model.Transformador;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/** Regras de negocio do cadastro de transformadores (RF01) - CRUD completo. */
public class TransformadorService {

    private static final String UNIQUE_VIOLATION = "23505";
    private static final String FK_VIOLATION = "23503";

    private final TransformadorDAO dao = new TransformadorDAO();

    public Transformador cadastrar(double latitude, double longitude, String bairro, String identificador,
                                   double capacidadeNominal, LocalDate dataUltimaManutencao) throws SQLException {
        Transformador t = new Transformador(0, latitude, longitude, bairro, true, identificador,
                capacidadeNominal, dataUltimaManutencao);
        validar(t);
        try {
            return dao.inserir(t);
        } catch (SQLException e) {
            if (UNIQUE_VIOLATION.equals(e.getSQLState())) {
                throw new IllegalArgumentException("Ja existe transformador com o identificador " + identificador + ".");
            }
            throw e;
        }
    }

    public List<Transformador> listar() throws SQLException {
        return dao.listarTodos();
    }

    public Transformador buscar(int id) throws SQLException {
        return dao.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Transformador " + id + " nao encontrado."));
    }

    public void atualizar(Transformador t) throws SQLException {
        validar(t);
        try {
            if (!dao.atualizar(t)) {
                throw new IllegalArgumentException("Transformador " + t.getId() + " nao encontrado.");
            }
        } catch (SQLException e) {
            if (UNIQUE_VIOLATION.equals(e.getSQLState())) {
                throw new IllegalArgumentException("Ja existe outro transformador com esse identificador.");
            }
            throw e;
        }
    }

    public void excluir(int id) throws SQLException {
        try {
            if (!dao.excluir(id)) {
                throw new IllegalArgumentException("Transformador " + id + " nao encontrado.");
            }
        } catch (SQLException e) {
            if (FK_VIOLATION.equals(e.getSQLState())) {
                throw new IllegalStateException("Este transformador possui incidentes registrados e nao pode ser "
                        + "excluido. Desative-o pela opcao de atualizacao.");
            }
            throw e;
        }
    }

    private void validar(Transformador t) {
        if (t.getIdentificadorUnico() == null || t.getIdentificadorUnico().isBlank()) {
            throw new IllegalArgumentException("O identificador unico e obrigatorio.");
        }
        if (t.getBairro() == null || t.getBairro().isBlank()) {
            throw new IllegalArgumentException("O bairro e obrigatorio.");
        }
        if (t.getLatitude() < -90 || t.getLatitude() > 90) {
            throw new IllegalArgumentException("Latitude deve estar entre -90 e 90.");
        }
        if (t.getLongitude() < -180 || t.getLongitude() > 180) {
            throw new IllegalArgumentException("Longitude deve estar entre -180 e 180.");
        }
        if (t.getCapacidadeNominal() <= 0) {
            throw new IllegalArgumentException("A capacidade nominal deve ser maior que zero.");
        }
        if (t.getDataUltimaManutencao() != null && t.getDataUltimaManutencao().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("A data da ultima manutencao nao pode ser futura.");
        }
    }
}
