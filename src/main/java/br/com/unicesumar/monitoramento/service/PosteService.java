package br.com.unicesumar.monitoramento.service;

import br.com.unicesumar.monitoramento.dao.PosteDAO;
import br.com.unicesumar.monitoramento.model.Poste;
import java.sql.SQLException;
import java.util.List;

/** Cadastro de postes (user story da Sprint 1) - CRUD completo. */
public class PosteService {

    private final PosteDAO dao = new PosteDAO();

    public Poste cadastrar(double latitude, double longitude, String bairro, String tipoPoste, double altura)
            throws SQLException {
        Poste p = new Poste(0, latitude, longitude, bairro, true, tipoPoste, altura);
        validar(p);
        return dao.inserir(p);
    }

    public List<Poste> listar() throws SQLException {
        return dao.listarTodos();
    }

    public Poste buscar(int id) throws SQLException {
        return dao.buscarPorId(id).orElseThrow(() -> new IllegalArgumentException("Poste " + id + " nao encontrado."));
    }

    public void atualizar(Poste p) throws SQLException {
        validar(p);
        if (!dao.atualizar(p)) {
            throw new IllegalArgumentException("Poste " + p.getId() + " nao encontrado.");
        }
    }

    public void excluir(int id) throws SQLException {
        if (!dao.excluir(id)) {
            throw new IllegalArgumentException("Poste " + id + " nao encontrado.");
        }
    }

    private void validar(Poste p) {
        if (p.getBairro() == null || p.getBairro().isBlank() || p.getTipoPoste() == null || p.getTipoPoste().isBlank()) {
            throw new IllegalArgumentException("Bairro e tipo do poste sao obrigatorios.");
        }
        if (p.getLatitude() < -90 || p.getLatitude() > 90 || p.getLongitude() < -180 || p.getLongitude() > 180) {
            throw new IllegalArgumentException("Coordenadas invalidas.");
        }
        if (p.getAltura() <= 0) {
            throw new IllegalArgumentException("A altura deve ser maior que zero.");
        }
    }
}
