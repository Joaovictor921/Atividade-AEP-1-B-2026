package br.com.unicesumar.monitoramento.service;

import br.com.unicesumar.monitoramento.dao.IncidenteDAO;
import br.com.unicesumar.monitoramento.dao.TransformadorDAO;
import br.com.unicesumar.monitoramento.model.Incidente;
import br.com.unicesumar.monitoramento.model.Operador;
import br.com.unicesumar.monitoramento.model.StatusIncidente;
import br.com.unicesumar.monitoramento.model.Transformador;
import br.com.unicesumar.monitoramento.util.GeoUtil;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/** Registro automatico de quedas de energia e atendimento dos incidentes (RF04). */
public class IncidenteService {

    private final IncidenteDAO incidenteDAO = new IncidenteDAO();
    private final TransformadorDAO transformadorDAO = new TransformadorDAO();

    /**
     * Associa a queda ao transformador ativo mais proximo e grava o incidente.
     * Retorna vazio se ja houver incidente aberto para esse transformador (evita duplicidade).
     */
    public Optional<Incidente> registrarQuedaEnergia(double latitude, double longitude, String descricao)
            throws SQLException {
        List<Transformador> candidatos = transformadorDAO.listarAtivos();
        if (candidatos.isEmpty()) {
            throw new IllegalStateException("Nenhum transformador ativo cadastrado para associar a queda.");
        }
        Transformador maisProximo = candidatos.stream()
                .min(Comparator.comparingDouble(t ->
                        GeoUtil.distanciaKm(latitude, longitude, t.getLatitude(), t.getLongitude())))
                .orElseThrow();
        if (incidenteDAO.existeAbertoParaTransformador(maisProximo.getId())) {
            return Optional.empty();
        }
        Incidente incidente = new Incidente(0, latitude, longitude, descricao);
        incidente.registrarQuedaEnergia(maisProximo);
        return Optional.of(incidenteDAO.inserir(incidente));
    }

    public List<Incidente> listarAbertos() throws SQLException {
        return incidenteDAO.listarAbertos();
    }

    public List<Incidente> listarTodos() throws SQLException {
        return incidenteDAO.listarTodos();
    }

    /** Despacha a equipe tecnica vinculada ao incidente (Sprint 3). */
    public Incidente despacharEquipe(Operador operador, int incidenteId) throws SQLException {
        Incidente i = buscar(incidenteId);
        if (i.getStatus() != StatusIncidente.ABERTO) {
            throw new IllegalStateException("Somente incidentes ABERTOS podem receber uma equipe.");
        }
        operador.despacharEquipe(i);
        incidenteDAO.atualizarStatus(i);
        return i;
    }

    public Incidente resolver(int incidenteId) throws SQLException {
        Incidente i = buscar(incidenteId);
        i.atualizarStatus(StatusIncidente.RESOLVIDO);
        incidenteDAO.atualizarStatus(i);
        return i;
    }

    private Incidente buscar(int id) throws SQLException {
        return incidenteDAO.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Incidente " + id + " nao encontrado."));
    }
}
