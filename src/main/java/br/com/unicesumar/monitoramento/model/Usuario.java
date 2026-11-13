package br.com.unicesumar.monitoramento.model;

import br.com.unicesumar.monitoramento.util.SenhaUtil;

/** Superclasse abstrata dos usuarios do sistema (UML: Usuario). */
public abstract class Usuario {

    private int id;
    private String nome;
    private String matricula;
    private String senha; // hash PBKDF2

    protected Usuario(int id, String nome, String matricula, String senha) {
        this.id = id;
        this.nome = nome;
        this.matricula = matricula;
        this.senha = senha;
    }

    /** Confere a senha informada com o hash armazenado. */
    public boolean autenticar(String senhaInformada) {
        return SenhaUtil.verificar(senhaInformada, senha);
    }

    /** Polimorfismo: cada tipo de usuario informa seu perfil. */
    public abstract String getPerfil();

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getMatricula() { return matricula; }
    public void setMatricula(String matricula) { this.matricula = matricula; }
    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }
}
