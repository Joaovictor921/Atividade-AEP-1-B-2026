package br.com.unicesumar.monitoramento.service;

import br.com.unicesumar.monitoramento.dao.AlertaDAO;
import br.com.unicesumar.monitoramento.dao.IncidenteDAO;
import br.com.unicesumar.monitoramento.dao.SensorDAO;
import br.com.unicesumar.monitoramento.model.Operador;
import br.com.unicesumar.monitoramento.model.PainelOperacao;
import java.sql.SQLException;

/** Monta o PainelOperacao com os dados atuais do banco. */
public class PainelService {

    private final AlertaDAO alertaDAO = new AlertaDAO();
    private final IncidenteDAO incidenteDAO = new IncidenteDAO();
    private final SensorDAO sensorDAO = new SensorDAO();

    public PainelOperacao montar(Operador operadorLogado) throws SQLException {
        PainelOperacao painel = new PainelOperacao();
        painel.adicionarOperador(operadorLogado);
        painel.setAlertas(alertaDAO.listarRecentes(10));
        painel.setIncidentesAbertos(incidenteDAO.listarAbertos());
        painel.setSensores(sensorDAO.listarTodos());
        return painel;
    }
}
