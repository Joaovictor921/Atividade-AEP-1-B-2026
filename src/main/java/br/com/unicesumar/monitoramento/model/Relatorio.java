package br.com.unicesumar.monitoramento.model;

import br.com.unicesumar.monitoramento.util.Entrada;
import br.com.unicesumar.monitoramento.util.PdfSimples;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/** Relatorio de consumo excessivo por bairro e periodo (RF05). */
public class Relatorio {

    public static final String TIPO_CONSUMO_EXCESSIVO = "CONSUMO_EXCESSIVO";

    private int id;
    private String bairroFiltro; // null = todos os bairros
    private LocalDate periodoInicio;
    private LocalDate periodoFim;
    private String tipoRelatorio = TIPO_CONSUMO_EXCESSIVO;
    private final List<String> linhas = new ArrayList<>();

    public Relatorio(String bairroFiltro, LocalDate periodoInicio, LocalDate periodoFim) {
        this.bairroFiltro = bairroFiltro;
        this.periodoInicio = periodoInicio;
        this.periodoFim = periodoFim;
    }

    /** Monta o texto do relatorio com os equipamentos que operaram acima da capacidade ideal. */
    public void gerarRelatorioConsumoExcessivo(List<ConsumoExcessivo> dados) {
        linhas.clear();
        linhas.add("RELATORIO DE CONSUMO EXCESSIVO");
        linhas.add("Bairro: " + (bairroFiltro == null ? "Todos" : bairroFiltro));
        linhas.add("Periodo: " + Entrada.formatarData(periodoInicio) + " a " + Entrada.formatarData(periodoFim));
        linhas.add("Gerado em: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        linhas.add("");
        linhas.add(String.format("%-12s %-20s %12s %14s %8s", "Equipamento", "Bairro", "Capac.(kVA)", "Maior cons.", "Ocorr."));
        linhas.add("-".repeat(70));
        for (ConsumoExcessivo c : dados) {
            linhas.add(String.format("%-12s %-20s %12.1f %14.1f %8d", c.identificador(),
                    abreviar(c.bairro(), 20), c.capacidadeNominal(), c.maiorConsumo(), c.ocorrencias()));
        }
        linhas.add("-".repeat(70));
        linhas.add("Total de equipamentos acima da capacidade: " + dados.size());
    }

    /** Exporta o relatorio em arquivo PDF. */
    public void exportarPDF(Path destino) throws IOException {
        if (linhas.isEmpty()) {
            throw new IllegalStateException("Gere o relatorio antes de exportar.");
        }
        if (destino.getParent() != null) {
            Files.createDirectories(destino.getParent());
        }
        Files.write(destino, PdfSimples.gerar(linhas));
    }

    public String getConteudoTexto() {
        return String.join("\n", linhas);
    }

    public byte[] getConteudoBytes() {
        return getConteudoTexto().getBytes(StandardCharsets.UTF_8);
    }

    private static String abreviar(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getBairroFiltro() { return bairroFiltro; }
    public LocalDate getPeriodoInicio() { return periodoInicio; }
    public LocalDate getPeriodoFim() { return periodoFim; }
    public String getTipoRelatorio() { return tipoRelatorio; }
}
