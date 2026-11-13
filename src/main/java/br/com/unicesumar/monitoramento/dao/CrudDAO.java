package br.com.unicesumar.monitoramento.dao;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/** Contrato generico de CRUD (Create, Read, Update, Delete) da camada de persistencia. */
public interface CrudDAO<T> {

    T inserir(T entidade) throws SQLException;

    Optional<T> buscarPorId(int id) throws SQLException;

    List<T> listarTodos() throws SQLException;

    /** @return true se algum registro foi alterado. */
    boolean atualizar(T entidade) throws SQLException;

    /** @return true se algum registro foi removido. */
    boolean excluir(int id) throws SQLException;
}
