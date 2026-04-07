package controladores;

import Modelos.Criterio;
import Modelos.Grafo;
import Modelos.Parada;
import Modelos.Ruta;
import Modelos.ResultadoRuta;
import Modelos.TipoVehiculo;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.event.ActionEvent;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.paint.Color;
import javafx.geometry.Point2D;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import javafx.scene.shape.Polygon;
import javafx.scene.Group;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Modality;
import java.io.IOException;
import BaseDeDatos.TransporteDB;

public class MainController {

    private Grafo redTransporte;

    private Map<Parada, Point2D> coordenadasMapa = new HashMap<>();

    @FXML
    private ComboBox<Parada> comboOrigen;
    @FXML
    private ComboBox<Parada> comboDestino;
    @FXML
    private ComboBox<Criterio> comboCriterio;
    @FXML
    private Pane mapaPane;
    @FXML
    private Label lblResultadoRuta;
    @FXML
    private Label lblTiempo;
    @FXML
    private Label lblDistancia;
    @FXML
    private Label lblCosto;
    @FXML
    private Label lblTransbordos;
    @FXML
    private ComboBox<String> comboAlgoritmo;

    @FXML
    public void initialize() {
        redTransporte = new Grafo();
        comboCriterio.getItems().setAll(Criterio.values());
        comboCriterio.setValue(Criterio.TIEMPO);
        comboAlgoritmo.getItems().addAll("Dijkstra", "BFS (Menos paradas)", "DFS (Búsqueda profunda)", "Bellman-Ford", "Floyd-Warshall");
        comboAlgoritmo.setValue("Dijkstra");
        TransporteDB db = new TransporteDB();
        db.cargarGrafo(redTransporte, coordenadasMapa);

        comboOrigen.getItems().setAll(redTransporte.getAdyacencia().keySet());
        comboDestino.getItems().setAll(redTransporte.getAdyacencia().keySet());

        //cargarDatosDePrueba();
        comboOrigen.setOnAction(e -> dibujarGrafo());
        comboDestino.setOnAction(e -> dibujarGrafo());
        dibujarGrafo();
    }

    private Group crearFlecha(double startX, double startY, double endX, double endY, Color color, double grosor) {
        Group flechaGrupo = new Group();

        double dx = endX - startX;
        double dy = endY - startY;
        double angulo = Math.atan2(dy, dx);
        double radioNodo = 15.0;

        double ajusteStartX = startX + radioNodo * Math.cos(angulo);
        double ajusteStartY = startY + radioNodo * Math.sin(angulo);
        double ajusteEndX = endX - radioNodo * Math.cos(angulo);
        double ajusteEndY = endY - radioNodo * Math.sin(angulo);

        Line linea = new Line(ajusteStartX, ajusteStartY, ajusteEndX, ajusteEndY);
        linea.setStrokeWidth(grosor);
        linea.setStroke(color);

        double tamañoPunta = 10.0 + (grosor - 2.0) * 2;
        Polygon punta = new Polygon();
        punta.getPoints().addAll(
                ajusteEndX, ajusteEndY,
                ajusteEndX - tamañoPunta * Math.cos(angulo - Math.PI / 6), ajusteEndY - tamañoPunta * Math.sin(angulo - Math.PI / 6),
                ajusteEndX - tamañoPunta * Math.cos(angulo + Math.PI / 6), ajusteEndY - tamañoPunta * Math.sin(angulo + Math.PI / 6)
        );
        punta.setFill(color);

        flechaGrupo.getChildren().addAll(linea, punta);
        return flechaGrupo;
    }

    private void dibujarLeyenda() {
        VBox leyendaBox = new VBox(8);
        leyendaBox.layoutXProperty().bind(mapaPane.widthProperty().subtract(leyendaBox.widthProperty()).subtract(20));//el total del ancho menos 20
        leyendaBox.setLayoutY(20);
        // Estilo CSS de la cajita flotante
        leyendaBox.setStyle("-fx-background-color: rgba(255, 255, 255, 0.85); -fx-padding: 15; -fx-border-color: #cccccc; -fx-border-width: 1; -fx-background-radius: 8; -fx-border-radius: 8;");

        Label titulo = new Label("Tipos de Vehículo");
        titulo.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #333333;");
        leyendaBox.getChildren().add(titulo);

        // Agregamos los items con color un poco opaco
        leyendaBox.getChildren().add(crearItemLeyenda("Bus", Color.rgb(135, 206, 250, 0.7)));
        leyendaBox.getChildren().add(crearItemLeyenda("Metro", Color.rgb(255, 182, 193, 0.7)));
        leyendaBox.getChildren().add(crearItemLeyenda("Tren", Color.rgb(255, 228, 181, 0.7)));
        leyendaBox.getChildren().add(crearItemLeyenda("Carro", Color.rgb(221, 160, 221, 0.7)));
        leyendaBox.getChildren().add(crearItemLeyenda("Moto", Color.rgb(255, 250, 205, 0.7)));

        mapaPane.getChildren().add(leyendaBox);
    }

    private HBox crearItemLeyenda(String nombre, Color color) {
        HBox item = new HBox(10);
        item.setAlignment(Pos.CENTER_LEFT);

        // La línea de muestra de color
        Line linea = new Line(0, 0, 30, 0);
        linea.setStrokeWidth(4);
        linea.setStroke(color);

        // El texto del vehículo
        Label lbl = new Label(nombre);
        lbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #555555;");

        item.getChildren().addAll(linea, lbl);
        return item;
    }

    private void dibujarGrafo() {
        dibujarGrafo(null, false); // Por defecto no es alternativa
    }

    private void dibujarGrafo(List<Parada> rutaOptima, boolean esAlternativa) {
        mapaPane.getChildren().clear();
        Map<Parada, List<Ruta>> adyacencia = redTransporte.getAdyacencia();

        for (Parada origen : adyacencia.keySet()) {
            for (Ruta ruta : adyacencia.get(origen)) {
                Parada destino = ruta.getDestino();

                Point2D posOrigen = coordenadasMapa.get(origen);
                Point2D posDestino = coordenadasMapa.get(destino);

                if (posOrigen != null && posDestino != null) {
                    boolean esParteDeRuta = false;

                    if (rutaOptima != null && rutaOptima.size() > 1) {
                        for (int i = 0; i < rutaOptima.size() - 1; i++) {
                            if (rutaOptima.get(i).equals(origen) && rutaOptima.get(i + 1).equals(destino)) {
                                esParteDeRuta = true;
                                break;
                            }
                        }
                    }

                    TipoVehiculo tipo = ruta.getVehiculo();
                    Color colorBasePastel;

                    switch (tipo) {
                        case BUS: colorBasePastel = Color.rgb(135, 206, 250, 1.0); break;
                        case METRO: colorBasePastel = Color.rgb(255, 182, 193, 1.0); break;
                        case TREN: colorBasePastel = Color.rgb(255, 228, 181, 1.0); break;
                        case CARRO: colorBasePastel = Color.rgb(221, 160, 221, 1.0); break;
                        case MOTO: colorBasePastel = Color.rgb(255, 250, 205, 1.0); break;
                        default: colorBasePastel = Color.rgb(200, 200, 200, 1.0); break;
                    }

                    Color colorFlecha;
                    if (esParteDeRuta) {
                        // NUEVO: Decidimos si interpolamos hacia Verde Lima o hacia Gris Oscuro
                        Color colorDestino = esAlternativa ? Color.DIMGRAY : Color.LIMEGREEN;
                        Color mezclado = colorBasePastel.interpolate(colorDestino, 0.65);
                        colorFlecha = Color.color(mezclado.getRed(), mezclado.getGreen(), mezclado.getBlue(), 1.0);
                    } else {
                        colorFlecha = colorBasePastel;
                    }

                    double grosor = esParteDeRuta ? 4.0 : 2.5;

                    Group flecha = crearFlecha(posOrigen.getX(), posOrigen.getY(), posDestino.getX(), posDestino.getY(), colorFlecha, grosor);
                    mapaPane.getChildren().add(flecha);
                }
            }
        }

        for (Parada p : adyacencia.keySet()) {
            Point2D pos = coordenadasMapa.get(p);

            if (pos != null) {
                boolean esNodoDeRuta = rutaOptima != null && rutaOptima.contains(p);
                boolean esSeleccionada = p.equals(comboOrigen.getValue()) || p.equals(comboDestino.getValue());

                Color colorBase;
                if (esNodoDeRuta) {
                    // NUEVO: Los nodos también cambian a gris si es ruta alternativa
                    colorBase = esAlternativa ? Color.DIMGRAY : Color.LIMEGREEN;
                } else if (esSeleccionada) {
                    colorBase = Color.LIGHTBLUE;
                } else {
                    colorBase = Color.DODGERBLUE;
                }

                Circle nodo = new Circle(pos.getX(), pos.getY(), 15, colorBase);
                nodo.setStroke(Color.DARKBLUE);
                nodo.setStrokeWidth(2);

                nodo.setOnMouseClicked(event -> {
                    if (comboOrigen.getValue() == null || (comboOrigen.getValue() != null && comboDestino.getValue() != null)) {
                        comboOrigen.setValue(p);
                        comboDestino.setValue(null);
                    } else {
                        comboDestino.setValue(p);
                    }
                    dibujarGrafo();
                });

                nodo.setOnMouseEntered(e -> nodo.setFill(Color.CYAN));
                nodo.setOnMouseExited(e -> nodo.setFill(colorBase));

                Label etiqueta = new Label(p.getNombre());
                etiqueta.setLayoutX(pos.getX() - 20);
                etiqueta.setLayoutY(pos.getY() - 35);
                etiqueta.setStyle("-fx-font-weight: bold; -fx-text-fill: #333333;");

                mapaPane.getChildren().addAll(nodo, etiqueta);
            }
        }

        dibujarLeyenda();
    }

    private void cargarDatosDePrueba() {
        // 1. Crear las Paradas
        Parada p1 = new Parada("P1", "Estación Central", "Centro");
        Parada p2 = new Parada("P2", "Terminal Norte", "Norte");
        Parada p3 = new Parada("P3", "Plaza Sur", "Sur");
        Parada p4 = new Parada("P4", "Campus Univ.", "Este");
        Parada p5 = new Parada("P5", "Parque Ind.", "Oeste");
        Parada p6 = new Parada("P6", "Centro Médico", "Noreste");
        Parada p7 = new Parada("P7", "Zona Comercial", "Sureste");
        Parada p8 = new Parada("P8", "Barrio Res.", "Noroeste");

        Parada[] todasLasParadas = {p1, p2, p3, p4, p5, p6, p7, p8};
        for (Parada p : todasLasParadas) {
            redTransporte.agregarParada(p);
        }

        // 2. Asignar las coordenadas visuales
        coordenadasMapa.put(p1, new Point2D(400, 300));
        coordenadasMapa.put(p2, new Point2D(400, 100));
        coordenadasMapa.put(p3, new Point2D(400, 500));
        coordenadasMapa.put(p4, new Point2D(700, 300));
        coordenadasMapa.put(p5, new Point2D(100, 300));
        coordenadasMapa.put(p6, new Point2D(650, 120));
        coordenadasMapa.put(p7, new Point2D(650, 480));
        coordenadasMapa.put(p8, new Point2D(150, 120));

        // 3. Agregar Rutas (ESTRICTAMENTE DIRIGIDAS Y CON VEHÍCULOS)
        // Parámetros: Destino, Tiempo (min), Costo ($), Distancia (km), TipoVehiculo

        // Salidas desde la Central (P1)
        redTransporte.agregarRuta(p1, new Ruta(p2, 10, 20.0, 4.0, TipoVehiculo.BUS));
        redTransporte.agregarRuta(p1, new Ruta(p3, 12, 20.0, 5.0, TipoVehiculo.METRO));

        // Retorno hacia la Central
        redTransporte.agregarRuta(p3, new Ruta(p1, 18, 20.0, 5.0, TipoVehiculo.METRO));

        // Ruta circular periférica con transbordos forzados
        redTransporte.agregarRuta(p2, new Ruta(p6, 12, 15.0, 5.0, TipoVehiculo.BUS));
        redTransporte.agregarRuta(p6, new Ruta(p4, 8, 15.0, 3.0, TipoVehiculo.METRO)); // Transbordo (Bus a Metro)
        redTransporte.agregarRuta(p4, new Ruta(p7, 11, 20.0, 4.5, TipoVehiculo.METRO));
        redTransporte.agregarRuta(p7, new Ruta(p3, 10, 15.0, 4.0, TipoVehiculo.BUS)); // Transbordo (Metro a Bus)

        // Conexiones hacia el Oeste (P5 y P8)
        redTransporte.agregarRuta(p1, new Ruta(p5, 15, 25.0, 6.0, TipoVehiculo.TREN));
        redTransporte.agregarRuta(p5, new Ruta(p8, 8, 10.0, 3.0, TipoVehiculo.TREN));
        redTransporte.agregarRuta(p8, new Ruta(p2, 14, 20.0, 5.5, TipoVehiculo.BUS)); // Transbordo (Tren a Bus)

        // LA RUTA TRAMPA (Directo de P1 a P6, pero carísima en tiempo y dinero)
        redTransporte.agregarRuta(p1, new Ruta(p6, 5, 100.0, 2.0, TipoVehiculo.CARRO));

        // Retorno expreso desde la Universidad
        redTransporte.agregarRuta(p4, new Ruta(p1, 25, 30.0, 8.0, TipoVehiculo.MOTO));

        // 4. Poblar los selectores
        comboOrigen.getItems().addAll(todasLasParadas);
        comboDestino.getItems().addAll(todasLasParadas);
    }

    @FXML
    private void calcularRuta(ActionEvent event) {
        Parada origen = comboOrigen.getValue();
        Parada destino = comboDestino.getValue();
        Criterio criterio = comboCriterio.getValue();

        if (origen == null || destino == null || criterio == null) {
            lblResultadoRuta.setText("Por favor, seleccione Origen, Destino y Criterio.");
            return;
        }

        if (origen.equals(destino)) {
            lblResultadoRuta.setText("El origen y destino son iguales.");
            dibujarGrafo();
            return;
        }

        // validacion de conexo
        boolean redConectada = redTransporte.esFuertementeConexo();
        String alerta = "";
        if (!redConectada) {
            alerta = "ALERTA: La red tiene paradas desconectadas o rutas sin retorno. \n";
        }


        ResultadoRuta resultado = redTransporte.calcularRuta(origen, destino, criterio);

        if (resultado != null && !resultado.getCamino().isEmpty()) {
            lblResultadoRuta.setText(alerta + "Ruta: " + resultado.getCamino().toString());
            lblTiempo.setText("Tiempo: " + resultado.getTiempoTotal() + " min");
            lblDistancia.setText("Distancia: " + resultado.getDistanciaTotal() + " km");
            lblCosto.setText("Costo: $" + resultado.getCostoTotal());
            lblTransbordos.setText("Transbordos: " + resultado.getTrasbordos());

            dibujarGrafo(resultado.getCamino(), false);
        } else {
            lblResultadoRuta.setText(alerta + "No hay ruta disponible entre estas paradas.");
            lblTiempo.setText("Tiempo: 0 min");
            lblDistancia.setText("Distancia: 0 km");
            lblCosto.setText("Costo: $0.00");
            lblTransbordos.setText("Transbordos: 0");

            dibujarGrafo();
        }
    }


    @FXML
    private void calcularRutaAlternativa(ActionEvent event) {
        Parada origen = comboOrigen.getValue();
        Parada destino = comboDestino.getValue();
        Criterio criterio = comboCriterio.getValue();
        String algoritmoSeleccionado = comboAlgoritmo.getValue();

        if (origen == null || destino == null || criterio == null || algoritmoSeleccionado == null) {
            lblResultadoRuta.setText("Por favor, seleccione Origen, Destino, Criterio y Algoritmo.");
            return;
        }

        if (origen.equals(destino)) {
            lblResultadoRuta.setText("El origen y destino son iguales.");
            dibujarGrafo();
            return;
        }

        boolean redConectada = redTransporte.esFuertementeConexo();
        String alerta = !redConectada ? "ALERTA: La red tiene paradas desconectadas. \n" : "";


        ResultadoRuta resultado = redTransporte.calcularRutaConAlgoritmo(origen, destino, criterio, algoritmoSeleccionado);

        if (resultado != null && resultado.getCamino() != null && !resultado.getCamino().isEmpty()) {
            lblResultadoRuta.setText(alerta + "Ruta (" + algoritmoSeleccionado + "): " + resultado.getCamino().toString());
            lblTiempo.setText("Tiempo: " + resultado.getTiempoTotal() + " min");
            lblDistancia.setText("Distancia: " + resultado.getDistanciaTotal() + " km");
            lblCosto.setText("Costo: $" + resultado.getCostoTotal());
            lblTransbordos.setText("Transbordos: " + resultado.getTrasbordos());

            dibujarGrafo(resultado.getCamino(), true);
        } else {
            lblResultadoRuta.setText(alerta + "No hay ruta disponible con " + algoritmoSeleccionado + ".");
            lblTiempo.setText("Tiempo: 0 min");
            lblDistancia.setText("Distancia: 0 km");
            lblCosto.setText("Costo: $0.00");
            lblTransbordos.setText("Transbordos: 0");

            dibujarGrafo();
        }
    }


    //abror ventana
    @FXML
    private void abrirGestor(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/vistas/GestorView.fxml")); //injectar dependency
            Parent root = loader.load();

            GestorController gestorController = loader.getController();

            gestorController.inicializarDatos(redTransporte, coordenadasMapa, this);

            Stage stage = new Stage();
            stage.setTitle("Gestor de Paradas y Rutas");
            stage.setScene(new Scene(root));

            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void actualizarVistaCompleta() {
        dibujarGrafo();
        comboOrigen.getItems().setAll(redTransporte.getAdyacencia().keySet());
        comboDestino.getItems().setAll(redTransporte.getAdyacencia().keySet());
    }
}