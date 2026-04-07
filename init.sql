CREATE TABLE paradas (
                         id VARCHAR(50) PRIMARY KEY,
                         nombre VARCHAR(100) NOT NULL,
                         sector VARCHAR(100) NOT NULL,
                         coordenada_x DOUBLE PRECISION NOT NULL,
                         coordenada_y DOUBLE PRECISION NOT NULL
);

CREATE TABLE rutas (
                       id_ruta SERIAL PRIMARY KEY,
                       origen_id VARCHAR(50) NOT NULL,
                       destino_id VARCHAR(50) NOT NULL,
                       tiempo DOUBLE PRECISION NOT NULL,
                       costo DOUBLE PRECISION NOT NULL,
                       distancia DOUBLE PRECISION NOT NULL,
                       vehiculo VARCHAR(50) NOT NULL,
                       CONSTRAINT fk_origen FOREIGN KEY (origen_id) REFERENCES paradas(id),
                       CONSTRAINT fk_destino FOREIGN KEY (destino_id) REFERENCES paradas(id)
);

-- 2. SEEDING: INSERTAR PARADAS (Nodos)
INSERT INTO paradas (id, nombre, sector, coordenada_x, coordenada_y) VALUES
                                                                         ('P1', 'Estación Central', 'Centro', 400.0, 300.0),
                                                                         ('P2', 'Terminal Norte', 'Norte', 400.0, 100.0),
                                                                         ('P3', 'Plaza Sur', 'Sur', 400.0, 500.0),
                                                                         ('P4', 'Campus Univ.', 'Este', 700.0, 300.0),
                                                                         ('P5', 'Parque Ind.', 'Oeste', 100.0, 300.0),
                                                                         ('P6', 'Centro Médico', 'Noreste', 650.0, 120.0),
                                                                         ('P7', 'Zona Comercial', 'Sureste', 650.0, 480.0),
                                                                         ('P8', 'Barrio Res.', 'Noroeste', 150.0, 120.0);

-- 3. SEEDING: INSERTAR RUTAS (Aristas)
INSERT INTO rutas (origen_id, destino_id, tiempo, costo, distancia, vehiculo) VALUES
                                                                                  ('P1', 'P2', 10.0, 20.0, 4.0, 'BUS'),
                                                                                  ('P1', 'P3', 12.0, 20.0, 5.0, 'METRO'),
                                                                                  ('P3', 'P1', 18.0, 20.0, 5.0, 'METRO'),
                                                                                  ('P2', 'P6', 12.0, 15.0, 5.0, 'BUS'),
                                                                                  ('P6', 'P4', 8.0, 15.0, 3.0, 'METRO'),
                                                                                  ('P4', 'P7', 11.0, 20.0, 4.5, 'METRO'),
                                                                                  ('P7', 'P3', 10.0, 15.0, 4.0, 'BUS'),
                                                                                  ('P1', 'P5', 15.0, 25.0, 6.0, 'TREN'),
                                                                                  ('P5', 'P8', 8.0, 10.0, 3.0, 'TREN'),
                                                                                  ('P8', 'P2', 14.0, 20.0, 5.5, 'BUS'),
                                                                                  ('P1', 'P6', 5.0, 100.0, 2.0, 'CARRO'),
                                                                                  ('P4', 'P1', 25.0, 30.0, 8.0, 'MOTO');