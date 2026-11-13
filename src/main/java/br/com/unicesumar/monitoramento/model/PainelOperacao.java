package br.com.unicesumar.monitoramento.model;

import java.util.ArrayList;
import java.util.List;

/** Painel da equipe de operacao: dashboard, alertas e incidentes abertos (UML: PainelOperacao). */
public class PainelOperacao {

    private final List<Operador> operadoresLogados = new ArrayList<>();
    private List<Alerta> alertas = new ArrayList<>();
    private List<Incidente> incidentesAbertos = new ArrayList<>();
    private List<Sensor> sensores = new ArrayList<>(); // estado ativo/inativo dos equipamentos

    public void adicionarOperador(Operador operador) {
        operadoresLogados.add(operador);
    }

    public void atualizarDashboard() {
        long semEnergia = sensores.stream().filter(s -> s.isComLeitura() && !s.isStatusEnergia()).count();
        long comEnergia = sensores.stream().filter(s -> s.isComLeitura() && s.isStatusEnergia()).count();
        long semLeitura = sensores.stream().filter(s -> !s.isComLeitura()).count();

        System.out.println("\n=========== PAINEL DE OPERACAO ===========");
        for (Operador o : operadoresLogados) {
            System.out.println("Operador logado: " + o.getNome() + " (turno " + o.getTurno() + ")");
        }
        System.out.printf("Sensores: %d com energia | %d SEM ENERGIA | %d sem leitura%n", comEnergia, semEnergia, semLeitura);
        System.out.printf("Alertas recentes: %d | Incidentes abertos: %d%n", alertas.size(), incidentesAbertos.size());
        System.out.println("\n--- Status dos equipamentos ---");
        for (Sensor s : sensores) {
            String estado = !s.isComLeitura() ? "sem leitura"
                    : (s.isStatusEnergia() ? "ENERGIA OK" : "SEM ENERGIA");
            String leitura = s.isComLeitura()
                    ? String.format("%.1f C | %.1f kVA", s.getTemperatura(), s.getConsumoInstantaneo()) : "-";
            System.out.printf("%-14s | %-45s | %-12s | %s%s%n", s.getIdentificadorDispositivo(),
                    s.getEquipamento().descrever(), leitura, estado, s.isAtivo() ? "" : " (sensor inativo)");
        }
    }

    public void exibirAlertaCritico() {
        System.out.println("\n--- Alertas criticos recentes ---");
        if (alertas.isEmpty()) {
            System.out.println("Nenhum alerta.");
        }
        alertas.forEach(Alerta::emitirAlertaPainel);
        System.out.println("\n--- Incidentes em aberto ---");
        if (incidentesAbertos.isEmpty()) {
            System.out.println("Nenhum incidente aberto.");
        }
        incidentesAbertos.forEach(i -> System.out.println(i.formatar()));
    }

    public List<Operador> getOperadoresLogados() { return operadoresLogados; }
    public List<Alerta> getAlertas() { return alertas; }
    public void setAlertas(List<Alerta> alertas) { this.alertas = alertas; }
    public List<Incidente> getIncidentesAbertos() { return incidentesAbertos; }
    public void setIncidentesAbertos(List<Incidente> incidentesAbertos) { this.incidentesAbertos = incidentesAbertos; }
    public List<Sensor> getSensores() { return sensores; }
    public void setSensores(List<Sensor> sensores) { this.sensores = sensores; }
}
