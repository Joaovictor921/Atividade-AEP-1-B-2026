package br.com.unicesumar.monitoramento.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Incidente de "local sem energia" (RF04). */
public class Incidente {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private int id;
    private double latitudeOcorrencia;
    private double longitudeOcorrencia;
    private String descricao;
    private LocalDateTime horarioInterrupcao;
    private StatusIncidente status = StatusIncidente.ABERTO;
    private Transformador transformadorMaisProximo;
    private Operador operadorAtribuido;

    public Incidente(int id, double latitudeOcorrencia, double longitudeOcorrencia, String descricao) {
        this.id = id;
        this.latitudeOcorrencia = latitudeOcorrencia;
        this.longitudeOcorrencia = longitudeOcorrencia;
        this.descricao = descricao;
    }

    /** Registra a queda: associa ao transformador mais proximo e marca o horario da interrupcao. */
    public void registrarQuedaEnergia(Transformador maisProximo) {
        this.transformadorMaisProximo = maisProximo;
        this.horarioInterrupcao = LocalDateTime.now();
        this.status = StatusIncidente.ABERTO;
    }

    public void atualizarStatus(StatusIncidente novoStatus) {
        if (status == StatusIncidente.RESOLVIDO) {
            throw new IllegalStateException("Incidente ja resolvido nao pode mudar de status.");
        }
        this.status = novoStatus;
    }

    public String formatar() {
        String trafo = transformadorMaisProximo == null ? "?" : transformadorMaisProximo.getIdentificadorUnico();
        String equipe = operadorAtribuido == null ? "sem equipe" : "operador " + operadorAtribuido.getNome();
        return String.format("#%d | %s | %s | trafo %s | (%.5f, %.5f) | %s | %s", id,
                horarioInterrupcao == null ? "-" : horarioInterrupcao.format(FORMATO),
                status, trafo, latitudeOcorrencia, longitudeOcorrencia, descricao, equipe);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public double getLatitudeOcorrencia() { return latitudeOcorrencia; }
    public double getLongitudeOcorrencia() { return longitudeOcorrencia; }
    public String getDescricao() { return descricao; }
    public LocalDateTime getHorarioInterrupcao() { return horarioInterrupcao; }
    public void setHorarioInterrupcao(LocalDateTime h) { this.horarioInterrupcao = h; }
    public StatusIncidente getStatus() { return status; }
    public void setStatus(StatusIncidente status) { this.status = status; }
    public Transformador getTransformadorMaisProximo() { return transformadorMaisProximo; }
    public void setTransformadorMaisProximo(Transformador t) { this.transformadorMaisProximo = t; }
    public Operador getOperadorAtribuido() { return operadorAtribuido; }
    public void setOperadorAtribuido(Operador operadorAtribuido) { this.operadorAtribuido = operadorAtribuido; }
}
