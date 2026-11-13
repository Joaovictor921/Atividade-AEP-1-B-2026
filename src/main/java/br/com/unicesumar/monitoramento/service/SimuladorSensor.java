package br.com.unicesumar.monitoramento.service;

import br.com.unicesumar.monitoramento.model.Sensor;
import br.com.unicesumar.monitoramento.model.Transformador;
import java.sql.SQLException;
import java.util.List;
import java.util.Random;

/** Mock (simulador) de envio de dados dos sensores IoT para o backend (Sprint 2). */
public class SimuladorSensor {

    private static final double PROBABILIDADE_QUEDA = 0.05;

    private final MonitoramentoService monitoramentoService;
    private final SensorService sensorService = new SensorService();
    private final Random random = new Random();

    public SimuladorSensor(MonitoramentoService monitoramentoService) {
        this.monitoramentoService = monitoramentoService;
    }

    /** Gera uma leitura aleatoria para cada sensor ativo. @return total de eventos gerados. */
    public int executarRodada() throws SQLException {
        List<Sensor> sensores = sensorService.listarAtivos();
        if (sensores.isEmpty()) {
            throw new IllegalStateException("Nao ha sensores ativos cadastrados.");
        }
        int eventos = 0;
        for (Sensor s : sensores) {
            double temperatura = 45 + random.nextDouble() * 55;       // 45 a 100 C
            double consumo;
            if (s.getEquipamento() instanceof Transformador t) {
                consumo = t.getCapacidadeNominal() * (0.4 + random.nextDouble() * 0.9); // 40% a 130%
            } else {
                consumo = 0.5 + random.nextDouble() * 4;
            }
            boolean comEnergia = random.nextDouble() >= PROBABILIDADE_QUEDA;
            System.out.printf("[SIMULADOR] %s -> %.1f C | %.1f kVA | %s%n", s.getIdentificadorDispositivo(),
                    temperatura, consumo, comEnergia ? "com energia" : "SEM ENERGIA");
            eventos += monitoramentoService.processarLeitura(s, temperatura, consumo, comEnergia).size();
        }
        return eventos;
    }
}
