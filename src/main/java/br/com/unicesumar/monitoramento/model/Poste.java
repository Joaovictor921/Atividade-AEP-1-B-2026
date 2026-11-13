package br.com.unicesumar.monitoramento.model;

/** Poste da rede de distribuicao (UML: Poste herda de Equipamento). */
public class Poste extends Equipamento {

    private String tipoPoste;
    private double altura;

    public Poste(int id, double latitude, double longitude, String bairro, boolean ativo,
                 String tipoPoste, double altura) {
        super(id, latitude, longitude, bairro, ativo);
        this.tipoPoste = tipoPoste;
        this.altura = altura;
    }

    @Override
    public String getTipo() {
        return "Poste";
    }

    @Override
    public String descrever() {
        return String.format("Poste #%d - %s, %.1f m (%s)", getId(), tipoPoste, altura, getBairro());
    }

    public String getTipoPoste() { return tipoPoste; }
    public void setTipoPoste(String tipoPoste) { this.tipoPoste = tipoPoste; }
    public double getAltura() { return altura; }
    public void setAltura(double altura) { this.altura = altura; }
}
