package br.com.unicesumar.monitoramento.service;

import br.com.unicesumar.monitoramento.dao.AlertaDAO;
import br.com.unicesumar.monitoramento.model.Alerta;
import br.com.unicesumar.monitoramento.model.Operador;
import java.sql.SQLException;
import java.util.List;

public class AlertaService {

    private final AlertaDAO alertaDAO = new AlertaDAO();

    public List<Alerta> listarRecentes(int limite) throws SQLException {
        return alertaDAO.listarRecentes(limite);
    }

    /** O operador assume a analise do alerta. */
    public Alerta analisar(Operador operador, int alertaId) throws SQLException {
        Alerta alerta = alertaDAO.buscarPorId(alertaId)
                .orElseThrow(() -> new IllegalArgumentException("Alerta " + alertaId + " nao encontrado."));
        operador.analisarAlerta(alerta);
        alertaDAO.atualizarDestinatario(alerta.getId(), operador.getId());
        return alerta;
    }
}
