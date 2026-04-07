package BaseDeDatos;

import Modelos.Grafo;
import Modelos.Parada;
import Modelos.Ruta;
import Modelos.TipoVehiculo;
import javafx.geometry.Point2D;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

public class TransporteDB {

    public void cargarGrafo(Grafo redTransporte, Map<Parada, Point2D> coordenadasMapa) {
        Map<String, Parada> mapaParadas = new HashMap<>();

        try (Connection conn = ConexionDB.getConexion()) {

            // CARGA PARADAS
            String sqlParadas = "SELECT * FROM paradas";
            try (PreparedStatement stmt = conn.prepareStatement(sqlParadas);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    String id = rs.getString("id");
                    String nombre = rs.getString("nombre");
                    String sector = rs.getString("sector");
                    double x = rs.getDouble("coordenada_x");
                    double y = rs.getDouble("coordenada_y");

                    Parada p = new Parada(id, nombre, sector);

                    redTransporte.agregarParada(p);
                    coordenadasMapa.put(p, new Point2D(x, y));
                    mapaParadas.put(id, p);
                }
            }

            // CARGA RUTAS
            String sqlRutas = "SELECT * FROM rutas";
            try (PreparedStatement stmt = conn.prepareStatement(sqlRutas);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    String origenId = rs.getString("origen_id");
                    String destinoId = rs.getString("destino_id");
                    double tiempo = rs.getDouble("tiempo");
                    double costo = rs.getDouble("costo");
                    double distancia = rs.getDouble("distancia");
                    String vehiculoStr = rs.getString("vehiculo");

                    Parada origen = mapaParadas.get(origenId);
                    Parada destino = mapaParadas.get(destinoId);
                    TipoVehiculo vehiculo = TipoVehiculo.valueOf(vehiculoStr);

                    if (origen != null && destino != null) {
                        redTransporte.agregarRuta(origen, new Ruta(destino, tiempo, costo, distancia, vehiculo));
                    }
                }
            }

            System.out.println("Base de datos cargada con éxito.");

        } catch (Exception e) {
            System.err.println("Error al cargar la base de datos: " + e.getMessage());
            e.printStackTrace();
        }
    }
}