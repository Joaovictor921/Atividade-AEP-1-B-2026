package br.com.unicesumar.monitoramento.service;

import br.com.unicesumar.monitoramento.dao.OperadorDAO;
import br.com.unicesumar.monitoramento.model.Operador;
import br.com.unicesumar.monitoramento.util.SenhaUtil;
import java.sql.SQLException;
import java.util.Optional;

/** Autenticacao por matricula e senha e cadastro de operadores. */
public class AuthService {

    private final OperadorDAO operadorDAO = new OperadorDAO();

    public Optional<Operador> login(String matricula, String senha) throws SQLException {
        return operadorDAO.buscarPorMatricula(matricula).filter(op -> op.autenticar(senha));
    }

    public boolean existemOperadores() throws SQLException {
        return operadorDAO.contar() > 0;
    }

    public Operador cadastrarOperador(String nome, String matricula, String senha, String turno) throws SQLException {
        if (nome == null || nome.isBlank() || matricula == null || matricula.isBlank()) {
            throw new IllegalArgumentException("Nome e matricula sao obrigatorios.");
        }
        if (senha == null || senha.length() < 6) {
            throw new IllegalArgumentException("A senha deve ter pelo menos 6 caracteres.");
        }
        if (operadorDAO.buscarPorMatricula(matricula).isPresent()) {
            throw new IllegalArgumentException("Ja existe um operador com essa matricula.");
        }
        return operadorDAO.inserir(new Operador(0, nome, matricula, SenhaUtil.gerarHash(senha), turno));
    }
}
