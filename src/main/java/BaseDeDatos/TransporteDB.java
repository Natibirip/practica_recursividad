package BaseDeDatos;

import Modelos.Grafo;
import Modelos.Parada;
import Modelos.Ruta;
import Modelos.TipoVehiculo;
import javafx.geometry.Point2D;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class TransporteDB {

    public void insertarParada(Parada p, Point2D pos) {
        String sql = "INSERT INTO paradas (id, nombre, ubicacion, coordenada_x, coordenada_y) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = ConexionDB.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, p.getId());
            pstmt.setString(2, p.getNombre());
            pstmt.setString(3, p.getUbicacion());
            pstmt.setDouble(4, pos.getX());
            pstmt.setDouble(5, pos.getY());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void actualizarParada(Parada p, Point2D pos) {
        String sql = "UPDATE paradas SET nombre = ?, sector = ?, coordenada_x = ?, coordenada_y = ? WHERE id = ?";
        try (Connection conn = ConexionDB.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, p.getNombre());
            pstmt.setString(2, p.getUbicacion());
            pstmt.setDouble(3, pos.getX());
            pstmt.setDouble(4, pos.getY());
            pstmt.setString(5, p.getId());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void eliminarParada(String idParada) {
        String sql = "DELETE FROM paradas WHERE id = ?";
        try (Connection conn = ConexionDB.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, idParada);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public void insertarRuta(Parada origen, Ruta ruta) {
        String sql = "INSERT INTO rutas (origen_id, destino_id, tiempo, costo, distancia, vehiculo) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConexionDB.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, origen.getId());
            pstmt.setString(2, ruta.getDestino().getId());
            pstmt.setDouble(3, ruta.getTiempo());
            pstmt.setDouble(4, ruta.getCosto());
            pstmt.setDouble(5, ruta.getDistancia());
            pstmt.setString(6, ruta.getVehiculo().name());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void eliminarRuta(String origenId, String destinoId) {
        String sql = "DELETE FROM rutas WHERE origen_id = ? AND destino_id = ?";
        try (Connection conn = ConexionDB.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, origenId);
            pstmt.setString(2, destinoId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void cargarGrafo(Grafo redTransporte, Map<Parada, Point2D> coordenadasMapa) {
        Map<String, Parada> mapaParadas = new HashMap<>();

        try (Connection conn = ConexionDB.getConexion()) {
            System.out.println("1. Conexión a Postgres exitosa.");

            String sqlParadas = "SELECT * FROM paradas";
            try (PreparedStatement stmt = conn.prepareStatement(sqlParadas);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String id = rs.getString("id");
                    double x = rs.getDouble("coordenada_x");
                    double y = rs.getDouble("coordenada_y");
                    Parada p = new Parada(id, rs.getString("nombre"), rs.getString("sector"));

                    redTransporte.agregarParada(p);
                    coordenadasMapa.put(p, new Point2D(x, y));
                    mapaParadas.put(id, p);
                }
                System.out.println("2. Paradas cargadas: " + mapaParadas.size());
            }

            String sqlRutas = "SELECT * FROM rutas";
            int rutasCargadas = 0;
            try (PreparedStatement stmt = conn.prepareStatement(sqlRutas);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String origenId = rs.getString("origen_id");
                    String destinoId = rs.getString("destino_id");
                    String vehiculoStr = rs.getString("vehiculo");

                    Parada origen = mapaParadas.get(origenId);
                    Parada destino = mapaParadas.get(destinoId);

                    if (origen != null && destino != null) {
                        TipoVehiculo vehiculo = TipoVehiculo.valueOf(vehiculoStr);
                        redTransporte.agregarRuta(origen, new Ruta(destino, rs.getDouble("tiempo"), rs.getDouble("costo"), rs.getDouble("distancia"), vehiculo));
                        rutasCargadas++;
                    } else {
                        System.out.println("⚠️ Error conectando ruta: " + origenId + " -> " + destinoId + " (Una parada no existe)");
                    }
                }
                System.out.println("3. Rutas cargadas: " + rutasCargadas);
            }

        } catch (Exception e) {
            System.err.println("❌ ERROR FATAL: " + e.getMessage());
            e.printStackTrace();
        }
    }
}