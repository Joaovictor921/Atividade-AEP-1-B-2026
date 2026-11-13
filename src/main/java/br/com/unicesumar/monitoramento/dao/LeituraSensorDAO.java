package br.com.unicesumar.monitoramento.dao;

import br.com.unicesumar.monitoramento.config.DatabaseConnection;
import br.com.unicesumar.monitoramento.model.LeituraSensor;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Armazena o historico de leituras enviadas pelos sensores (RF02). */
public class LeituraSensorDAO {

    public LeituraSensor inserir(LeituraSensor l) throws SQLException {
        String sql = "INSERT INTO leitura_sensor (sensor_id, temperatura, consumo_instantaneo, status_energia, "
                + "horario_leitura) VALUES (?, ?, ?, ?, ?) RETURNING id";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, l.getSensorId());
            ps.setDouble(2, l.getTemperatura());
            ps.setDouble(3, l.getConsumoInstantaneo());
            ps.setBoolean(4, l.isStatusEnergia());
            ps.setObject(5, l.getHorarioLeitura());
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                l.setId(rs.getLong(1));
            }
            return l;
        }
    }

    public List<LeituraSensor> listarUltimas(int sensorId, int limite) throws SQLException {
        String sql = "SELECT id, sensor_id, temperatura, consumo_instantaneo, status_energia, horario_leitura "
                + "FROM leitura_sensor WHERE sensor_id = ? ORDER BY horario_leitura DESC, id DESC LIMIT ?";
        List<LeituraSensor> lista = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, sensorId);
            ps.setInt(2, limite);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new LeituraSensor(rs.getLong("id"), rs.getInt("sensor_id"),
                            rs.getDouble("temperatura"), rs.getDouble("consumo_instantaneo"),
                            rs.getBoolean("status_energia"), rs.getObject("horario_leitura", LocalDateTime.class)));
                }
            }
        }
        return lista;
    }
}
