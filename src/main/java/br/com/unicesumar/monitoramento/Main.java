package br.com.unicesumar.monitoramento;

import br.com.unicesumar.monitoramento.config.DatabaseConnection;
import br.com.unicesumar.monitoramento.model.Operador;
import br.com.unicesumar.monitoramento.service.AuthService;
import br.com.unicesumar.monitoramento.util.Entrada;
import java.sql.SQLException;
import java.util.Optional;

/** Ponto de entrada: valida a conexao com o Supabase, autentica o operador e abre o menu. */
public class Main {

    public static void main(String[] args) {
        System.out.println("=== Monitoramento da Infraestrutura Eletrica (ODS 9) ===");
        System.out.println("Conectando ao banco de dados remoto (Supabase)...");
        if (!DatabaseConnection.testarConexao()) {
            System.out.println("Nao foi possivel conectar. Revise o arquivo .env e o README.md.");
            return;
        }
        System.out.println("Conexao ativa.");

        try {
            AuthService auth = new AuthService();
            if (!auth.existemOperadores()) {
                System.out.println("\nNenhum operador cadastrado. Crie o primeiro acesso:");
                String nome = Entrada.texto("Nome completo: ");
                String matricula = Entrada.texto("Matricula: ");
                String senha = Entrada.texto("Senha (min. 6 caracteres): ");
                String turno = Entrada.texto("Turno (Manha/Tarde/Noite): ");
                auth.cadastrarOperador(nome, matricula, senha, turno);
                System.out.println("Operador criado com sucesso.");
            }

            Operador logado = null;
            for (int tentativa = 1; tentativa <= 3 && logado == null; tentativa++) {
                System.out.println("\n--- Login (tentativa " + tentativa + "/3) ---");
                String matricula = Entrada.texto("Matricula: ");
                String senha = Entrada.texto("Senha: ");
                Optional<Operador> op = auth.login(matricula, senha);
                if (op.isPresent()) {
                    logado = op.get();
                } else {
                    System.out.println("Matricula ou senha invalida.");
                }
            }
            if (logado == null) {
                System.out.println("Numero maximo de tentativas excedido.");
                return;
            }
            new br.com.unicesumar.monitoramento.view.MenuPrincipal(logado).executar();
        } catch (SQLException e) {
            System.out.println("Erro de banco de dados: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("Aviso: " + e.getMessage());
        }
    }
}
