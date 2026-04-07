package BaseDeDatos;

import Modelos.Parada;
import Modelos.Ruta;
import javafx.geometry.Point2D;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

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
            pstmt.setString(2, p.getUbicacion()); // Usamos el getUbicacion() que tienes en tu clase
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
}