package br.com.unicesumar.monitoramento.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Alerta automatico de limite de seguranca (RF03). */
public class Alerta {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private int id;
    private String mensagem;
    private LocalDateTime horarioGeracao;
    private TipoAlerta tipoAlerta;
    private double limiteUltrapassado;
    private Transformador transformadorOrigem;
    private Operador operadorDestinatario;

    public Alerta(int id, String mensagem, LocalDateTime horarioGeracao, TipoAlerta tipoAlerta,
                  double limiteUltrapassado, Transformador transformadorOrigem) {
        this.id = id;
        this.mensagem = mensagem;
        this.horarioGeracao = horarioGeracao;
        this.tipoAlerta = tipoAlerta;
        this.limiteUltrapassado = limiteUltrapassado;
        this.transformadorOrigem = transformadorOrigem;
    }

    /** Emite o alerta no painel da equipe de operacao. */
    public void emitirAlertaPainel() {
        System.out.println(">>> [ALERTA " + tipoAlerta + "] " + formatar());
    }

    public String formatar() {
        String origem = transformadorOrigem == null ? "?" : transformadorOrigem.getIdentificadorUnico();
        String analista = operadorDestinatario == null ? "sem analista" : "analisando: " + operadorDestinatario.getNome();
        return String.format("#%d | %s | %s | %s | %s", id, horarioGeracao.format(FORMATO), origem, mensagem, analista);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getMensagem() { return mensagem; }
    public LocalDateTime getHorarioGeracao() { return horarioGeracao; }
    public TipoAlerta getTipoAlerta() { return tipoAlerta; }
    public double getLimiteUltrapassado() { return limiteUltrapassado; }
    public Transformador getTransformadorOrigem() { return transformadorOrigem; }
    public Operador getOperadorDestinatario() { return operadorDestinatario; }
    public void setOperadorDestinatario(Operador operadorDestinatario) { this.operadorDestinatario = operadorDestinatario; }
}
