package br.com.unicesumar.monitoramento.model;

/** Linha do relatorio de consumo excessivo (RF05). */
public record ConsumoExcessivo(String identificador, String bairro, double capacidadeNominal,
                               double maiorConsumo, long ocorrencias) {
}
