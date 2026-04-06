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