package br.com.unicesumar.monitoramento.model;

import java.time.LocalDateTime;

/** Registro historico enviado por um sensor (tabela LEITURA_SENSOR do DER). */
public class LeituraSensor {

    private long id;
    private final int sensorId;
    private final double temperatura;
    private final double consumoInstantaneo;
    private final boolean statusEnergia;
    private final LocalDateTime horarioLeitura;

    public LeituraSensor(long id, int sensorId, double temperatura, double consumoInstantaneo,
                         boolean statusEnergia, LocalDateTime horarioLeitura) {
        this.id = id;
        this.sensorId = sensorId;
        this.temperatura = temperatura;
        this.consumoInstantaneo = consumoInstantaneo;
        this.statusEnergia = statusEnergia;
        this.horarioLeitura = horarioLeitura;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public int getSensorId() { return sensorId; }
    public double getTemperatura() { return temperatura; }
    public double getConsumoInstantaneo() { return consumoInstantaneo; }
    public boolean isStatusEnergia() { return statusEnergia; }
    public LocalDateTime getHorarioLeitura() { return horarioLeitura; }
}
