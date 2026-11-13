package br.com.unicesumar.monitoramento.service;

import br.com.unicesumar.monitoramento.dao.AlertaDAO;
import br.com.unicesumar.monitoramento.dao.LeituraSensorDAO;
import br.com.unicesumar.monitoramento.model.Alerta;
import br.com.unicesumar.monitoramento.model.Equipamento;
import br.com.unicesumar.monitoramento.model.Sensor;
import br.com.unicesumar.monitoramento.model.TipoAlerta;
import br.com.unicesumar.monitoramento.model.Transformador;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Nucleo do monitoramento: recebe e armazena leituras (RF02), emite alertas de limite (RF03)
 * e dispara o registro de quedas de energia (RF04).
 */
public class MonitoramentoService {

    private final LeituraSensorDAO leituraDAO = new LeituraSensorDAO();
    private final AlertaDAO alertaDAO = new AlertaDAO();
    private final IncidenteService incidenteService = new IncidenteService();

    /** @return descricao dos eventos gerados (alertas/incidentes) por esta leitura. */
    public List<String> processarLeitura(Sensor sensor, double temperatura, double consumo, boolean statusEnergia)
            throws SQLException {
        if (!sensor.isAtivo()) {
            throw new IllegalArgumentException("O sensor " + sensor.getIdentificadorDispositivo() + " esta inativo.");
        }
        sensor.registrarLeitura(temperatura, consumo, statusEnergia);
        leituraDAO.inserir(sensor.enviarDados()); // enviarDados() = leitura recem registrada

        List<String> eventos = new ArrayList<>();
        Equipamento equipamento = sensor.getEquipamento();

        if (equipamento instanceof Transformador t) {
            if (t.temperaturaExcedida(temperatura)) {
                eventos.add(emitirAlerta(t, TipoAlerta.TEMPERATURA, Transformador.LIMITE_TEMPERATURA_CELSIUS,
                        String.format("Temperatura %.1f C acima do limite de %.1f C",
                                temperatura, Transformador.LIMITE_TEMPERATURA_CELSIUS)));
            }
            if (t.consumoExcedido(consumo)) {
                eventos.add(emitirAlerta(t, TipoAlerta.CONSUMO, t.getCapacidadeNominal(),
                        String.format("Consumo %.1f kVA acima da capacidade de %.1f kVA",
                                consumo, t.getCapacidadeNominal())));
            }
        }

        if (!statusEnergia) {
            String descricao = "Queda de energia detectada pelo sensor " + sensor.getIdentificadorDispositivo();
            incidenteService.registrarQuedaEnergia(equipamento.getLatitude(), equipamento.getLongitude(), descricao)
                    .ifPresent(i -> eventos.add("INCIDENTE registrado: " + i.formatar()));
        }
        return eventos;
    }

    private String emitirAlerta(Transformador t, TipoAlerta tipo, double limite, String mensagem) throws SQLException {
        Alerta alerta = new Alerta(0, mensagem, LocalDateTime.now(), tipo, limite, t);
        alertaDAO.inserir(alerta);
        alerta.emitirAlertaPainel();
        return "ALERTA " + tipo + " gerado para " + t.getIdentificadorUnico();
    }
}
