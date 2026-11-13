package br.com.unicesumar.monitoramento.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Superclasse abstrata dos equipamentos da rede (UML: Equipamento).
 * Composicao 1:N com Sensor: os sensores pertencem ao equipamento.
 * As operacoes cadastrar/atualizar/excluir/buscar do UML sao realizadas na camada
 * dao/service, mantendo a regra de negocio isolada do acesso a dados.
 */
public abstract class Equipamento {

    private int id;
    private double latitude;
    private double longitude;
    private String bairro;
    private boolean ativo;
    private final List<Sensor> sensores = new ArrayList<>();

    protected Equipamento(int id, double latitude, double longitude, String bairro, boolean ativo) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.bairro = bairro;
        this.ativo = ativo;
    }

    public void adicionarSensor(Sensor sensor) {
        sensores.add(sensor);
    }

    public List<Sensor> getSensores() {
        return Collections.unmodifiableList(sensores);
    }

    /** Polimorfismo: nome do tipo concreto do equipamento. */
    public abstract String getTipo();

    /** Polimorfismo: descricao resumida do equipamento. */
    public abstract String descrever();

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public String getBairro() { return bairro; }
    public void setBairro(String bairro) { this.bairro = bairro; }
    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
}
