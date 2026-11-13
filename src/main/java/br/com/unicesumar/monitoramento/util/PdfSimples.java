package br.com.unicesumar.monitoramento.util;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Gerador minimo de PDF (texto, fonte Courier, A4) sem dependencias externas,
 * assim o projeto roda igual em Windows, Linux e macOS.
 */
public final class PdfSimples {

    private static final int LINHAS_POR_PAGINA = 52;
    private static final int COLUNAS_MAXIMAS = 85;

    private PdfSimples() {
    }

    public static byte[] gerar(List<String> linhas) {
        List<String> preparadas = new ArrayList<>();
        for (String linha : linhas) {
            String l = linha == null ? "" : linha;
            do {
                int corte = Math.min(COLUNAS_MAXIMAS, l.length());
                preparadas.add(l.substring(0, corte));
                l = l.substring(corte);
            } while (!l.isEmpty());
        }
        int totalPaginas = Math.max(1, (int) Math.ceil(preparadas.size() / (double) LINHAS_POR_PAGINA));

        // Objetos: 1=catalogo, 2=paginas, 3=fonte, depois (pagina, conteudo) por pagina.
        List<String> objetos = new ArrayList<>();
        StringBuilder kids = new StringBuilder();
        for (int p = 0; p < totalPaginas; p++) {
            kids.append(4 + 2 * p).append(" 0 R ");
        }
        objetos.add("<< /Type /Catalog /Pages 2 0 R >>");
        objetos.add("<< /Type /Pages /Kids [" + kids + "] /Count " + totalPaginas + " >>");
        objetos.add("<< /Type /Font /Subtype /Type1 /BaseFont /Courier /Encoding /WinAnsiEncoding >>");
        for (int p = 0; p < totalPaginas; p++) {
            StringBuilder fluxo = new StringBuilder("BT /F1 10 Tf 40 800 Td 14 TL\n");
            int inicio = p * LINHAS_POR_PAGINA;
            int fim = Math.min(preparadas.size(), inicio + LINHAS_POR_PAGINA);
            for (int i = inicio; i < fim; i++) {
                fluxo.append('(').append(escapar(preparadas.get(i))).append(") Tj T*\n");
            }
            fluxo.append("ET");
            int tamanho = fluxo.toString().getBytes(StandardCharsets.ISO_8859_1).length;
            objetos.add("<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] "
                    + "/Resources << /Font << /F1 3 0 R >> >> /Contents " + (5 + 2 * p) + " 0 R >>");
            objetos.add("<< /Length " + tamanho + " >>\nstream\n" + fluxo + "\nendstream");
        }

        ByteArrayOutputStream saida = new ByteArrayOutputStream();
        escrever(saida, "%PDF-1.4\n");
        int[] posicoes = new int[objetos.size()];
        for (int i = 0; i < objetos.size(); i++) {
            posicoes[i] = saida.size();
            escrever(saida, (i + 1) + " 0 obj\n" + objetos.get(i) + "\nendobj\n");
        }
        int xref = saida.size();
        StringBuilder tabela = new StringBuilder("xref\n0 " + (objetos.size() + 1) + "\n0000000000 65535 f \n");
        for (int pos : posicoes) {
            tabela.append(String.format("%010d 00000 n \n", pos));
        }
        tabela.append("trailer\n<< /Size ").append(objetos.size() + 1).append(" /Root 1 0 R >>\nstartxref\n")
                .append(xref).append("\n%%EOF\n");
        escrever(saida, tabela.toString());
        return saida.toByteArray();
    }

    private static String escapar(String texto) {
        StringBuilder sb = new StringBuilder();
        for (char c : texto.toCharArray()) {
            if (c == '\\' || c == '(' || c == ')') {
                sb.append('\\').append(c);
            } else if (c > 255) {
                sb.append('?');
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private static void escrever(ByteArrayOutputStream out, String texto) {
        byte[] bytes = texto.getBytes(StandardCharsets.ISO_8859_1);
        out.write(bytes, 0, bytes.length);
    }
}
