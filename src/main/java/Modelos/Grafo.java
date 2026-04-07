package Modelos;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Comparator;
import java.util.Set;
import java.util.HashSet;
import java.util.Queue;
import java.util.LinkedList;
import java.util.Deque;
import java.util.ArrayDeque;

public class Grafo {

    private Map<Parada, List<Ruta>> adyacencia;

    public Grafo() {
        adyacencia = new HashMap<>();
    }

    public void agregarParada(Parada p) {
        if (p == null) {
            throw new IllegalArgumentException("La parada no puede ser null");
        }

        adyacencia.putIfAbsent(p, new ArrayList<>());
    }

    public void agregarRuta(Parada origen, Ruta ruta) {

        if (origen == null || ruta == null) {
            throw new IllegalArgumentException("Origen o ruta no pueden ser null");
        }

        if (!adyacencia.containsKey(origen)) {
            throw new IllegalArgumentException("La parada origen no existe en el grafo");
        }

        adyacencia.get(origen).add(ruta);
    }

    public Map<Parada, List<Ruta>> getAdyacencia() {
        return adyacencia;
    }


    public ResultadoRuta calcularRuta(Parada origen, Parada destino, Criterio criterio) {
        List<Parada> camino = new ArrayList<>();

        // Decidir que algoritmo usar
        int opcionAlgoritmo = DecidirAlgoritmo(criterio);

        if (opcionAlgoritmo == 1) {
            camino = bfs01Transbordos(origen, destino);
        } else if (opcionAlgoritmo == 2) {
            camino = dijkstra(origen, destino, criterio);
        } else {
            System.out.println("Criterio no soportado o algoritmo no definido.");
            return null;
        }

        if (camino == null || camino.isEmpty()) {
            return null;
        }

        // totales del camino ganador
        double tiempoTotal = 0;
        double costoTotal = 0;
        double distanciaTotal = 0;
        int transbordos = 0;
        TipoVehiculo vehiculoAnterior = null;

        for (int i = 0; i < camino.size() - 1; i++) {
            Parada actual = camino.get(i);
            Parada siguiente = camino.get(i + 1);

            for (Ruta ruta : adyacencia.get(actual)) {
                if (ruta.getDestino().equals(siguiente)) {
                    tiempoTotal += ruta.getTiempo();
                    costoTotal += ruta.getCosto();
                    distanciaTotal += ruta.getDistancia();

                    // Lógica real de transbordo (cambio de vehículo)
                    if (vehiculoAnterior != null && vehiculoAnterior != ruta.getVehiculo()) {
                        transbordos++;
                    }
                    vehiculoAnterior = ruta.getVehiculo();
                    break;
                }
            }
        }

        return new ResultadoRuta(camino, tiempoTotal, costoTotal, distanciaTotal, transbordos);
    }

    private double obtenerPeso(Ruta r, Criterio criterio) {
        switch (criterio) {
            case TIEMPO:
                return r.getTiempo();
            case COSTO:
                return r.getCosto();
            case DISTANCIA:
                return r.getDistancia();
            default:
                throw new IllegalArgumentException("Criterio inválido");
        }
    }


    private List<Parada> bfs01Transbordos(Parada origen, Parada destino) {
        Map<Parada, Integer> minTransbordos = new HashMap<>();
        Map<Parada, Parada> anteriores = new HashMap<>();
        Map<Parada, Ruta> rutaLlegada = new HashMap<>(); // Memoria de vehiculo

        //doble queue
        Deque<Parada> deque = new ArrayDeque<>();

        for (Parada p : adyacencia.keySet()) {
            minTransbordos.put(p, Integer.MAX_VALUE);
        }

        minTransbordos.put(origen, 0);
        deque.addFirst(origen);

        while (!deque.isEmpty()) {
            Parada actual = deque.pollFirst();

            if (actual.equals(destino)) break;

            for (Ruta ruta : adyacencia.get(actual)) {
                Parada vecino = ruta.getDestino();
                TipoVehiculo vehiculoRuta = ruta.getVehiculo();

                int peso = 0;
                Ruta rutaPrevia = rutaLlegada.get(actual);
                if (rutaPrevia != null && rutaPrevia.getVehiculo() != vehiculoRuta) {
                    peso = 1;
                }

                int nuevosTransbordos = minTransbordos.get(actual) + peso;

                if (nuevosTransbordos < minTransbordos.get(vecino)) {
                    minTransbordos.put(vecino, nuevosTransbordos);
                    anteriores.put(vecino, actual);
                    rutaLlegada.put(vecino, ruta);

                    if (peso == 0) {
                        deque.addFirst(vecino); //si es el mismo vehiculo al frente
                    } else {
                        deque.addLast(vecino);  //sino al final
                    }
                }
            }
        }

        return reconstruirCamino(anteriores, origen, destino);
    }

    public int DecidirAlgoritmo( Criterio criterio) {
        switch (criterio) {
            case TRASBORDOS:
                return 1;
            case COSTO:
            case TIEMPO:
            case DISTANCIA:
                return 2;

        }
        return 0;
    }

    public ResultadoRuta calcularRutaConAlgoritmo(Parada origen, Parada destino, Criterio criterio, String algoritmo) {
        if (algoritmo.contains("BFS")) {
            return calcularRutaBFS(origen, destino);
        } else if (algoritmo.contains("DFS")) {
            return calcularRutaDFS(origen, destino);
        } else if (algoritmo.contains("Bellman")) {
            return calcularRutaBellmanFord(origen, destino, criterio);
        } else if (algoritmo.contains("Floyd")) {
            return calcularRutaFloydWarshall(origen, destino, criterio);
        } else {
            // Por defecto, usa tu algoritmo original (Asumido como Dijkstra)
            return calcularRuta(origen, destino, criterio);
        }
    }


    private ResultadoRuta calcularRutaBFS(Parada origen, Parada destino) {
        java.util.Queue<Parada> cola = new java.util.LinkedList<>();
        java.util.Set<Parada> visitados = new java.util.HashSet<>();
        java.util.Map<Parada, Parada> padres = new java.util.HashMap<>();

        cola.add(origen);
        visitados.add(origen);

        while (!cola.isEmpty()) {
            Parada actual = cola.poll();

            if (actual.equals(destino)) break;

            List<Ruta> rutas = adyacencia.getOrDefault(actual, new java.util.ArrayList<>());
            for (Ruta ruta : rutas) {
                Parada vecino = ruta.getDestino();
                if (!visitados.contains(vecino)) {
                    visitados.add(vecino);
                    padres.put(vecino, actual);
                    cola.add(vecino);
                }
            }
        }

        return reconstruirResultado(origen, destino, padres);
    }

    private ResultadoRuta calcularRutaDFS(Parada origen, Parada destino) {
        java.util.Stack<Parada> pila = new java.util.Stack<>();
        java.util.Set<Parada> visitados = new java.util.HashSet<>();
        java.util.Map<Parada, Parada> padres = new java.util.HashMap<>();

        pila.push(origen);

        while (!pila.isEmpty()) {
            Parada actual = pila.pop();

            if (actual.equals(destino)) break;

            if (!visitados.contains(actual)) {
                visitados.add(actual);
                List<Ruta> rutas = adyacencia.getOrDefault(actual, new java.util.ArrayList<>());
                for (Ruta ruta : rutas) {
                    Parada vecino = ruta.getDestino();
                    if (!visitados.contains(vecino)) {
                        padres.putIfAbsent(vecino, actual); // Solo guarda el primer padre encontrado
                        pila.push(vecino);
                    }
                }
            }
        }

        return reconstruirResultado(origen, destino, padres);
    }


    private ResultadoRuta calcularRutaBellmanFord(Parada origen, Parada destino, Criterio criterio) {
        java.util.Map<Parada, Double> distancias = new java.util.HashMap<>();
        java.util.Map<Parada, Parada> padres = new java.util.HashMap<>();

        for (Parada p : adyacencia.keySet()) {
            distancias.put(p, Double.MAX_VALUE);
        }
        distancias.put(origen, 0.0);

        int numNodos = adyacencia.size();


        for (int i = 0; i < numNodos - 1; i++) {
            for (Parada u : adyacencia.keySet()) {
                for (Ruta ruta : adyacencia.get(u)) {
                    Parada v = ruta.getDestino();
                    double peso = obtenerPeso(ruta, criterio);

                    if (distancias.get(u) != Double.MAX_VALUE && distancias.get(u) + peso < distancias.get(v)) {
                        distancias.put(v, distancias.get(u) + peso);
                        padres.put(v, u);
                    }
                }
            }
        }

        return reconstruirResultado(origen, destino, padres);
    }


    private ResultadoRuta calcularRutaFloydWarshall(Parada origen, Parada destino, Criterio criterio) {
        List<Parada> nodos = new java.util.ArrayList<>(adyacencia.keySet());
        int n = nodos.size();
        double[][] dist = new double[n][n];
        int[][] next = new int[n][n];


        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                dist[i][j] = Double.MAX_VALUE;
                next[i][j] = -1;
            }
            dist[i][i] = 0;
        }


        for (int i = 0; i < n; i++) {
            Parada u = nodos.get(i);
            for (Ruta ruta : adyacencia.getOrDefault(u, new java.util.ArrayList<>())) {
                Parada v = ruta.getDestino();
                int j = nodos.indexOf(v);
                double peso = obtenerPeso(ruta, criterio);
                if (peso < dist[i][j]) {
                    dist[i][j] = peso;
                    next[i][j] = j;
                }
            }
        }

        for (int k = 0; k < n; k++) {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    if (dist[i][k] != Double.MAX_VALUE && dist[k][j] != Double.MAX_VALUE &&
                            dist[i][k] + dist[k][j] < dist[i][j]) {
                        dist[i][j] = dist[i][k] + dist[k][j];
                        next[i][j] = next[i][k];
                    }
                }
            }
        }

        // Reconstruir el camino
        int indiceOrigen = nodos.indexOf(origen);
        int indiceDestino = nodos.indexOf(destino);

        if (next[indiceOrigen][indiceDestino] == -1) {
            return new ResultadoRuta(new java.util.ArrayList<>(), 0, 0, 0, 0); // No hay camino
        }

        List<Parada> camino = new java.util.ArrayList<>();
        int actual = indiceOrigen;
        camino.add(nodos.get(actual));

        while (actual != indiceDestino) {
            actual = next[actual][indiceDestino];
            camino.add(nodos.get(actual));
        }

        return ensamblarResultadoFinal(camino, criterio);
    }


    private ResultadoRuta reconstruirResultado(Parada origen, Parada destino, java.util.Map<Parada, Parada> padres) {
        List<Parada> camino = new java.util.ArrayList<>();
        Parada paso = destino;

        // Si no se llegó al destino, retornamos vacío
        if (!padres.containsKey(destino) && !origen.equals(destino)) {
            return new ResultadoRuta(camino, 0, 0, 0, 0);
        }

        camino.add(paso);
        while (padres.containsKey(paso)) {
            paso = padres.get(paso);
            camino.add(paso);
        }
        java.util.Collections.reverse(camino);

        if (!camino.get(0).equals(origen)) {
            return new ResultadoRuta(new java.util.ArrayList<>(), 0, 0, 0, 0);
        }

        return ensamblarResultadoFinal(camino, Criterio.TIEMPO); // Usamos Tiempo por defecto para BFS/DFS
    }

    private ResultadoRuta ensamblarResultadoFinal(List<Parada> camino, Criterio criterio) {
        double tiempoTotal = 0;
        double costoTotal = 0;
        double distanciaTotal = 0;
        int transbordos = 0;
        TipoVehiculo vehiculoAnterior = null;

        for (int i = 0; i < camino.size() - 1; i++) {
            Parada u = camino.get(i);
            Parada v = camino.get(i + 1);


            Ruta mejorRuta = null;
            double mejorPeso = Double.MAX_VALUE;

            for (Ruta r : adyacencia.get(u)) {
                if (r.getDestino().equals(v)) {
                    double pesoReal = obtenerPeso(r, criterio);
                    if (pesoReal < mejorPeso) {
                        mejorPeso = pesoReal;
                        mejorRuta = r;
                    }
                }
            }

            if (mejorRuta != null) {
                tiempoTotal += mejorRuta.getTiempo();
                costoTotal += mejorRuta.getCosto();
                distanciaTotal += mejorRuta.getDistancia();

                if (vehiculoAnterior != null && mejorRuta.getVehiculo() != vehiculoAnterior) {
                    transbordos++;
                }
                vehiculoAnterior = mejorRuta.getVehiculo();
            }
        }

        return new ResultadoRuta(camino, tiempoTotal, costoTotal, distanciaTotal, transbordos);
    }

    public List<Parada> dijkstra (Parada origen, Parada destino, Criterio criterio) {

        Map<Parada, Double> peso = new HashMap<>();
        Map<Parada, Parada> anteriores = new HashMap<>();
        Set<Parada> visitados = new HashSet<>();

        PriorityQueue<Parada> cola =
                new PriorityQueue<>(Comparator.comparingDouble(peso::get));

        for (Parada p : adyacencia.keySet()) {
            peso.put(p, Double.POSITIVE_INFINITY);
        }

        peso.put(origen, 0.0);
        cola.add(origen);

        while (!cola.isEmpty()) {

            Parada actual = cola.poll();

            if (visitados.contains(actual)) continue;
            visitados.add(actual);


            if (actual.equals(destino)) break;

            for (Ruta ruta : adyacencia.get(actual)) {

                Parada vecino = ruta.getDestino();

                if (visitados.contains(vecino)) continue;

                double nuevaDistancia = //nuevo peso
                        peso.get(actual)
                                + obtenerPeso(ruta, criterio);

                if (nuevaDistancia < peso.get(vecino)) {

                    peso.put(vecino, nuevaDistancia);
                    anteriores.put(vecino, actual);
                    cola.add(vecino);
                }
            }
        }

        return reconstruirCamino(anteriores, origen, destino);
    }

    private List<Parada> reconstruirCamino(
            Map<Parada, Parada> anteriores,
            Parada origen,
            Parada destino) {

        List<Parada> camino = new ArrayList<>();

        Parada actual = destino;

        while (actual != null) {
            camino.add(0, actual);
            actual = anteriores.get(actual);
        }

        if (!camino.isEmpty() && camino.get(0).equals(origen)) {
            return camino;
        }

        return new ArrayList<>(); // no hay camino
    }


    public boolean esFuertementeConexo() {
        if (adyacencia.isEmpty()) return true;

        // una parada como punto de inicio
        Parada inicio = adyacencia.keySet().iterator().next();

        // chequea si se llega a todas las paradas desde 'inicio'
        if (!alcanzaTodas(inicio, adyacencia)) {
            return false;
        }

        // Grafo Transpuesto
        Map<Parada, List<Parada>> grafoInvertido = new HashMap<>();
        for (Parada p : adyacencia.keySet()) {
            grafoInvertido.put(p, new ArrayList<>());
        }
        for (Parada origen : adyacencia.keySet()) {
            for (Ruta ruta : adyacencia.get(origen)) {
                // se invierte
                grafoInvertido.get(ruta.getDestino()).add(origen);
            }
        }

        // chequea si inicio se llaga desde las demas paradas
        return alcanzaTodasInvertido(inicio, grafoInvertido);
    }

    // BFS normal
    private boolean alcanzaTodas(Parada inicio, Map<Parada, List<Ruta>> grafo) {
        Set<Parada> visitados = new HashSet<>();
        Queue<Parada> cola = new LinkedList<>();

        cola.add(inicio);
        visitados.add(inicio);

        while (!cola.isEmpty()) {
            Parada actual = cola.poll();
            for (Ruta ruta : grafo.get(actual)) {
                if (!visitados.contains(ruta.getDestino())) {
                    visitados.add(ruta.getDestino());
                    cola.add(ruta.getDestino());
                }
            }
        }
        return visitados.size() == grafo.keySet().size();
    }

    //BFS en el grafo invertido
    private boolean alcanzaTodasInvertido(Parada inicio, Map<Parada, List<Parada>> grafoInvertido) {
        Set<Parada> visitados = new HashSet<>();
        Queue<Parada> cola = new LinkedList<>();

        cola.add(inicio);
        visitados.add(inicio);

        while (!cola.isEmpty()) {
            Parada actual = cola.poll();
            for (Parada vecino : grafoInvertido.get(actual)) {
                if (!visitados.contains(vecino)) {
                    visitados.add(vecino);
                    cola.add(vecino);
                }
            }
        }
        return visitados.size() == grafoInvertido.keySet().size();
    }

    public void eliminarParada(Parada p) {
        if (!adyacencia.containsKey(p)) return;

        //Elimina ruta
        for (List<Ruta> rutas : adyacencia.values()) {
            rutas.removeIf(ruta -> ruta.getDestino().equals(p));
        }

        // Eliminar la parada
        adyacencia.remove(p);
    }

    public void eliminarRuta(Parada origen, Parada destino) {
        if (adyacencia.containsKey(origen)) {
            adyacencia.get(origen).removeIf(ruta -> ruta.getDestino().equals(destino));
        }
    }

}