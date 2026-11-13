package br.com.unicesumar.monitoramento.model;

/** Operador do Painel de Operacao (UML: Operador herda de Usuario). */
public class Operador extends Usuario {

    private String turno;

    public Operador(int id, String nome, String matricula, String senha, String turno) {
        super(id, nome, matricula, senha);
        this.turno = turno;
    }

    /** Assume a analise do alerta (torna-se o destinatario). */
    public void analisarAlerta(Alerta alerta) {
        alerta.setOperadorDestinatario(this);
    }

    /** Despacha a equipe tecnica: atribui o incidente ao operador e inicia o atendimento. */
    public void despacharEquipe(Incidente incidente) {
        incidente.setOperadorAtribuido(this);
        incidente.atualizarStatus(StatusIncidente.EM_ATENDIMENTO);
    }

    @Override
    public String getPerfil() {
        return "OPERADOR";
    }

    public String getTurno() { return turno; }
    public void setTurno(String turno) { this.turno = turno; }
}
