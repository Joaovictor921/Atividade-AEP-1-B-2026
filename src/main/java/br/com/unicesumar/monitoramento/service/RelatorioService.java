package br.com.unicesumar.monitoramento.service;

import br.com.unicesumar.monitoramento.dao.RelatorioDAO;
import br.com.unicesumar.monitoramento.model.ConsumoExcessivo;
import br.com.unicesumar.monitoramento.model.Relatorio;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/** Geracao e exportacao de relatorios de consumo excessivo (RF05). */
public class RelatorioService {

    private final RelatorioDAO relatorioDAO = new RelatorioDAO();

    public Relatorio gerarConsumoExcessivo(String bairro, LocalDate inicio, LocalDate fim) throws SQLException {
        if (inicio.isAfter(fim)) {
            throw new IllegalArgumentException("A data inicial nao pode ser posterior a data final.");
        }
        List<ConsumoExcessivo> dados = relatorioDAO.consultarConsumoExcessivo(bairro, inicio, fim);
        Relatorio relatorio = new Relatorio(bairro, inicio, fim);
        relatorio.gerarRelatorioConsumoExcessivo(dados);
        relatorioDAO.inserir(relatorio);
        return relatorio;
    }

    public void exportarPDF(Relatorio relatorio, Path destino) throws IOException {
        relatorio.exportarPDF(destino);
    }

    public List<String> listarBairros() throws SQLException {
        return relatorioDAO.listarBairros();
    }
}
