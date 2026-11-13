package br.com.unicesumar.monitoramento.service;

import br.com.unicesumar.monitoramento.dao.EquipamentoDAO;
import br.com.unicesumar.monitoramento.dao.SensorDAO;
import br.com.unicesumar.monitoramento.model.Equipamento;
import br.com.unicesumar.monitoramento.model.Sensor;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/** Cadastro de sensores vinculados a postes e transformadores. */
public class SensorService {

    private final SensorDAO sensorDAO = new SensorDAO();
    private final EquipamentoDAO equipamentoDAO = new EquipamentoDAO();

    public Sensor cadastrar(String identificadorDispositivo, int equipamentoId, LocalDate dataInstalacao)
            throws SQLException {
        if (identificadorDispositivo == null || identificadorDispositivo.isBlank()) {
            throw new IllegalArgumentException("O identificador do dispositivo e obrigatorio.");
        }
        Equipamento eq = equipamentoDAO.buscarPorId(equipamentoId)
                .orElseThrow(() -> new IllegalArgumentException("Equipamento " + equipamentoId + " nao encontrado."));
        try {
            return sensorDAO.inserir(new Sensor(0, identificadorDispositivo, eq, dataInstalacao, true));
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                throw new IllegalArgumentException("Ja existe sensor com o identificador " + identificadorDispositivo + ".");
            }
            throw e;
        }
    }

    public List<Sensor> listar() throws SQLException {
        return sensorDAO.listarTodos();
    }

    public List<Sensor> listarAtivos() throws SQLException {
        return sensorDAO.listarAtivos();
    }

    public Sensor buscar(int id) throws SQLException {
        return sensorDAO.buscarPorId(id).orElseThrow(() -> new IllegalArgumentException("Sensor " + id + " nao encontrado."));
    }

    public void alterarAtivo(int id, boolean ativo) throws SQLException {
        Sensor s = buscar(id);
        s.setAtivo(ativo);
        sensorDAO.atualizar(s);
    }

    public void excluir(int id) throws SQLException {
        if (!sensorDAO.excluir(id)) {
            throw new IllegalArgumentException("Sensor " + id + " nao encontrado.");
        }
    }
}
