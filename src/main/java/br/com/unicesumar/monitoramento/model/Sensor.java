package br.com.unicesumar.monitoramento.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Sensor IoT instalado em poste ou transformador (UML: Sensor). Possui N leituras (composicao). */
public class Sensor {

    private int id;
    private String identificadorDispositivo;
    private Equipamento equipamento;
    private LocalDate dataInstalacao;
    private boolean ativo;

    // Ultimo estado conhecido (UML: temperatura, consumoInstantaneo, statusEnergia)
    private double temperatura;
    private double consumoInstantaneo;
    private boolean statusEnergia = true;
    private boolean comLeitura;

    private final List<LeituraSensor> leituras = new ArrayList<>();

    public Sensor(int id, String identificadorDispositivo, Equipamento equipamento,
                  LocalDate dataInstalacao, boolean ativo) {
        this.id = id;
        this.identificadorDispositivo = identificadorDispositivo;
        this.equipamento = equipamento;
        this.dataInstalacao = dataInstalacao;
        this.ativo = ativo;
    }

    /** Registra uma nova leitura, atualiza o estado atual e devolve o registro criado. */
    public LeituraSensor registrarLeitura(double temperatura, double consumo, boolean statusEnergia) {
        setLeituraAtual(temperatura, consumo, statusEnergia);
        LeituraSensor leitura = new LeituraSensor(0, id, temperatura, consumo, statusEnergia, LocalDateTime.now());
        leituras.add(leitura);
        return leitura;
    }

    /** Devolve a leitura mais recente, pronta para ser enviada ao backend (null se nao houver). */
    public LeituraSensor enviarDados() {
        return leituras.isEmpty() ? null : leituras.get(leituras.size() - 1);
    }

    /** Usado ao carregar do banco o ultimo estado conhecido. */
    public void setLeituraAtual(double temperatura, double consumo, boolean statusEnergia) {
        this.temperatura = temperatura;
        this.consumoInstantaneo = consumo;
        this.statusEnergia = statusEnergia;
        this.comLeitura = true;
    }

    public List<LeituraSensor> getLeituras() { return Collections.unmodifiableList(leituras); }
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getIdentificadorDispositivo() { return identificadorDispositivo; }
    public void setIdentificadorDispositivo(String v) { this.identificadorDispositivo = v; }
    public Equipamento getEquipamento() { return equipamento; }
    public void setEquipamento(Equipamento equipamento) { this.equipamento = equipamento; }
    public LocalDate getDataInstalacao() { return dataInstalacao; }
    public void setDataInstalacao(LocalDate dataInstalacao) { this.dataInstalacao = dataInstalacao; }
    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
    public double getTemperatura() { return temperatura; }
    public double getConsumoInstantaneo() { return consumoInstantaneo; }
    public boolean isStatusEnergia() { return statusEnergia; }
    public boolean isComLeitura() { return comLeitura; }
}
