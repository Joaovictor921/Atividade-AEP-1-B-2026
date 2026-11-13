package br.com.unicesumar.monitoramento.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Fabrica de conexoes com o banco remoto (Supabase / PostgreSQL).
 * As credenciais NUNCA ficam no codigo: sao lidas de variaveis de ambiente
 * ou do arquivo ".env" (ignorado pelo Git). Veja .env.example.
 */
public final class DatabaseConnection {

    private static final Map<String, String> ARQUIVO_ENV = carregarArquivoEnv();

    private DatabaseConnection() {
    }

    public static Connection getConnection() throws SQLException {
        String url = valor("SUPABASE_DB_URL");
        String usuario = valor("SUPABASE_DB_USER");
        String senha = valor("SUPABASE_DB_PASSWORD");
        if (url == null || usuario == null || senha == null) {
            throw new SQLException("Credenciais do banco ausentes. Defina SUPABASE_DB_URL, "
                    + "SUPABASE_DB_USER e SUPABASE_DB_PASSWORD (variaveis de ambiente ou arquivo .env).");
        }
        return DriverManager.getConnection(url, usuario, senha);
    }

    /** Verifica se a conexao com o Supabase esta ativa. */
    public static boolean testarConexao() {
        try (Connection c = getConnection()) {
            return c.isValid(5);
        } catch (SQLException e) {
            System.out.println("Falha ao conectar no banco: " + e.getMessage());
            return false;
        }
    }

    private static String valor(String chave) {
        String v = System.getenv(chave);
        if (v == null || v.isBlank()) {
            v = ARQUIVO_ENV.get(chave);
        }
        return (v == null || v.isBlank()) ? null : v.trim();
    }

    private static Map<String, String> carregarArquivoEnv() {
        Map<String, String> mapa = new HashMap<>();
        Path arquivo = Path.of(".env");
        if (!Files.exists(arquivo)) {
            return mapa;
        }
        try {
            List<String> linhas = Files.readAllLines(arquivo);
            for (String linha : linhas) {
                String l = linha.trim();
                int igual = l.indexOf('=');
                if (l.isEmpty() || l.startsWith("#") || igual < 1) {
                    continue;
                }
                mapa.put(l.substring(0, igual).trim(), l.substring(igual + 1).trim());
            }
        } catch (IOException e) {
            System.out.println("Nao foi possivel ler o arquivo .env: " + e.getMessage());
        }
        return mapa;
    }
}
