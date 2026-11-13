-- Dados de exemplo (opcional). Execute UMA vez, depois do schema.sql.
-- O primeiro operador e criado pela propria aplicacao (tela de login), pois a senha e salva com hash.

-- Transformadores
WITH e AS (INSERT INTO equipamento (latitude, longitude, bairro, ativo) VALUES (-23.4273, -51.9375, 'Zona 7', TRUE) RETURNING id)
INSERT INTO transformador (equipamento_id, identificador_unico, capacidade_nominal, data_ultima_manutencao)
SELECT id, 'TR-0001', 75.0, DATE '2026-03-10' FROM e;

WITH e AS (INSERT INTO equipamento (latitude, longitude, bairro, ativo) VALUES (-23.4155, -51.9250, 'Centro', TRUE) RETURNING id)
INSERT INTO transformador (equipamento_id, identificador_unico, capacidade_nominal, data_ultima_manutencao)
SELECT id, 'TR-0002', 112.5, DATE '2026-01-22' FROM e;

WITH e AS (INSERT INTO equipamento (latitude, longitude, bairro, ativo) VALUES (-23.4420, -51.9100, 'Jardim Alvorada', TRUE) RETURNING id)
INSERT INTO transformador (equipamento_id, identificador_unico, capacidade_nominal, data_ultima_manutencao)
SELECT id, 'TR-0003', 45.0, DATE '2025-11-05' FROM e;

-- Sensores dos transformadores
INSERT INTO sensor (identificador_dispositivo, equipamento_id, data_instalacao, ativo)
SELECT 'SN-' || identificador_unico, equipamento_id, DATE '2026-03-15', TRUE FROM transformador;

-- Postes com sensor
WITH e AS (INSERT INTO equipamento (latitude, longitude, bairro, ativo) VALUES (-23.4268, -51.9362, 'Zona 7', TRUE) RETURNING id),
     p AS (INSERT INTO poste (equipamento_id, tipo_poste, altura) SELECT id, 'Concreto Duplo T', 11.0 FROM e RETURNING equipamento_id)
INSERT INTO sensor (identificador_dispositivo, equipamento_id, data_instalacao, ativo)
SELECT 'SN-PT-0001', equipamento_id, DATE '2026-03-16', TRUE FROM p;

WITH e AS (INSERT INTO equipamento (latitude, longitude, bairro, ativo) VALUES (-23.4410, -51.9112, 'Jardim Alvorada', TRUE) RETURNING id),
     p AS (INSERT INTO poste (equipamento_id, tipo_poste, altura) SELECT id, 'Concreto Circular', 9.0 FROM e RETURNING equipamento_id)
INSERT INTO sensor (identificador_dispositivo, equipamento_id, data_instalacao, ativo)
SELECT 'SN-PT-0002', equipamento_id, DATE '2026-03-16', TRUE FROM p;
