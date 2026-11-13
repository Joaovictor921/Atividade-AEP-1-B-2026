package br.com.unicesumar.monitoramento.model;

import java.time.LocalDate;

/** Transformador (UML: Transformador herda de Equipamento). Entidade principal do CRUD. */
public class Transformador extends Equipamento {

    /** Limite de seguranca de temperatura (graus Celsius) - RF03. */
    public static final double LIMITE_TEMPERATURA_CELSIUS = 85.0;

    private String identificadorUnico;
    private double capacidadeNominal; // kVA
    private LocalDate dataUltimaManutencao;

    public Transformador(int id, double latitude, double longitude, String bairro, boolean ativo,
                         String identificadorUnico, double capacidadeNominal, LocalDate dataUltimaManutencao) {
        super(id, latitude, longitude, bairro, ativo);
        this.identificadorUnico = identificadorUnico;
        this.capacidadeNominal = capacidadeNominal;
        this.dataUltimaManutencao = dataUltimaManutencao;
    }

    public boolean temperaturaExcedida(double temperatura) {
        return temperatura > LIMITE_TEMPERATURA_CELSIUS;
    }

    /** Consumo acima da capacidade nominal (capacidade ideal) - RF03/RF05. */
    public boolean consumoExcedido(double consumo) {
        return consumo > capacidadeNominal;
    }

    /** Retorna true quando temperatura e consumo estao dentro dos limites de seguranca. */
    public boolean verificarLimitesSeguranca(double temperatura, double consumo) {
        return !temperaturaExcedida(temperatura) && !consumoExcedido(consumo);
    }

    @Override
    public String getTipo() {
        return "Transformador";
    }

    @Override
    public String descrever() {
        return String.format("Transformador %s - %.1f kVA (%s)", identificadorUnico, capacidadeNominal, getBairro());
    }

    public String getIdentificadorUnico() { return identificadorUnico; }
    public void setIdentificadorUnico(String identificadorUnico) { this.identificadorUnico = identificadorUnico; }
    public double getCapacidadeNominal() { return capacidadeNominal; }
    public void setCapacidadeNominal(double capacidadeNominal) { this.capacidadeNominal = capacidadeNominal; }
    public LocalDate getDataUltimaManutencao() { return dataUltimaManutencao; }
    public void setDataUltimaManutencao(LocalDate dataUltimaManutencao) { this.dataUltimaManutencao = dataUltimaManutencao; }
}
