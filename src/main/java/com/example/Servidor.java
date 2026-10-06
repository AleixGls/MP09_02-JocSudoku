package com.example;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import java.net.InetSocketAddress;


public class Servidor extends WebSocketServer {

    private Map<WebSocket, String> jugadores = new LinkedHashMap<>();
    private Map<WebSocket, Integer> puntos = new HashMap<>();
    private Map<WebSocket, String> nombres = new LinkedHashMap<>();

    private Sudoku partidaActual;
    private WebSocket jugadorTurno;
    



    public Servidor(int puerto) {
        super(new InetSocketAddress(puerto));
    }


    
    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        System.out.println("Cliente conectado: " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {

        String jugador = jugadores.get(conn);

        boolean eraSuTurno = (conn == jugadorTurno);

        WebSocket siguienteJugador = null;

        if (eraSuTurno) {

            boolean encontrado = false;

            for (WebSocket jugadorWS : jugadores.keySet()) {

                if (encontrado) {
                    siguienteJugador = jugadorWS;
                    break;
                }

                if (jugadorWS == conn) {
                    encontrado = true;
                }
            }
        }

        jugadores.remove(conn);

        System.out.println("Cliente desconectado: " + jugador);

        if (eraSuTurno) {

            if (siguienteJugador != null && jugadores.containsKey(siguienteJugador)) {
                jugadorTurno = siguienteJugador;
            } else if (!jugadores.isEmpty()) {
                jugadorTurno = jugadores.keySet().iterator().next();
            } else {
                jugadorTurno = null;
            }

            if (jugadorTurno != null) {
                System.out.println(
                    "Turno después de desconexión → " +
                    jugadores.get(jugadorTurno)
                );

                enviarTurno();
            }
        }

        enviarListaJugadores();
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        System.out.println("Mensaje recibido: " + message);

        JSONObject obj = new JSONObject(message);

        String type = obj.getString("type");

        System.out.println("Tipo: " + type);

        if (type.equals("join")) {

            String name = obj.getString("name");

            jugadores.put(conn, name);
            nombres.put(conn, name);
            puntos.put(conn, 0);

            if (partidaActual == null) {
                partidaActual = new Sudoku();
                partidaActual.generarSolucion();

                System.out.println("Nueva partida de Sudoku creada.");
            }

            if (jugadorTurno == null) {
                jugadorTurno = conn;
                System.out.println("Primer turno para: " + name);
            }

            System.out.println("Jugador conectado: " + name);
            System.out.println("Jugadores conectados: " + jugadores.values());

            enviarListaJugadores();
            enviarPuntuaciones();
            enviarTablero();
            enviarTurno();
        }

        if (type.equals("move")) {

            String jugador = jugadores.get(conn);

            if (conn != jugadorTurno) {
                System.out.println("No es el turno de " + jugador);
                return;
            }

            int fila = obj.getInt("row");
            int columna = obj.getInt("col");
            int numero = obj.getInt("number");

            System.out.println(
                "Jugada de " + jugador +
                ": fila=" + fila +
                ", columna=" + columna +
                ", numero=" + numero
            );

            // Comprobar que el jugador no puede hacer trampas 
            // enviando el numero correcto a una casilla ocupada
            int valorActual = partidaActual.getTablero()[fila][columna];
            if (valorActual != 0) {
                System.out.println("Esa casilla ya está ocupada.");
                return;
            }

            if (partidaActual.esCorrecto(fila, columna, numero)) {
                System.out.println("Jugada CORRECTA");

                puntos.put(conn, puntos.get(conn) + 2);
                enviarPuntuaciones();

                System.out.println(
                    "Puntos de " + jugadores.get(conn) + ": " + puntos.get(conn)
                );

                partidaActual.ponerNumero(fila, columna, numero);

                enviarTablero();

                enviarCasillaCorrecta(fila, columna);

                System.out.println(
                    "Terminando turno de: " + jugadores.get(jugadorTurno)
                );

                siguienteTurno();
                enviarTurno();

            } else {
                System.out.println("Jugada INCORRECTA");

                puntos.put(conn, puntos.get(conn) - 1);
                enviarPuntuaciones();

                System.out.println(
                    "Puntos de " + jugadores.get(conn) + ": " + puntos.get(conn)
                );

                System.out.println(
                    "Terminando turno de: " + jugadores.get(jugadorTurno)
                );

                siguienteTurno();
                enviarTurno();
            }
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        System.out.println("Error del servidor: " + ex.getMessage());
    }

    @Override
    public void onStart() {
        System.out.println("Servidor iniciado en el puerto " + getPort());
    }

    // Metodos de jugadores y puntos
    private void enviarListaJugadores() {
        JSONArray lista = new JSONArray();

        for (String nombre : jugadores.values()) {
            lista.put(nombre);
        }

        JSONObject mensaje = new JSONObject();
        mensaje.put("type", "players");
        mensaje.put("players", lista);

        for (WebSocket jugador : jugadores.keySet()) {
            jugador.send(mensaje.toString());
        }
    }
    private void enviarPuntuaciones() {

        JSONObject mensaje = new JSONObject();
        mensaje.put("type", "scores");

        JSONArray jugadoresArray = new JSONArray();

        for (WebSocket jugador : jugadores.keySet()) {

            JSONObject jugadorJSON = new JSONObject();

            jugadorJSON.put("name", jugadores.get(jugador));
            jugadorJSON.put("points", puntos.get(jugador));

            jugadoresArray.put(jugadorJSON);
        }

        mensaje.put("players", jugadoresArray);

        for (WebSocket jugador : jugadores.keySet()) {
            jugador.send(mensaje.toString());
        }
    }

    // Metodos de tablero y casillas
    private void enviarTablero() {

        JSONArray tablero = new JSONArray();

        int[][] datos = partidaActual.getTablero();

        for (int fila = 0; fila < 9; fila++) {

            JSONArray filaJson = new JSONArray();

            for (int columna = 0; columna < 9; columna++) {
                filaJson.put(datos[fila][columna]);
            }

            tablero.put(filaJson);
        }

        JSONObject mensaje = new JSONObject();
        mensaje.put("type", "board");
        mensaje.put("board", tablero);

        for (WebSocket jugador : jugadores.keySet()) {
            jugador.send(mensaje.toString());
        }
    }   
    private void enviarCasillaCorrecta(int fila, int columna) {

        JSONObject mensaje = new JSONObject();

        mensaje.put("type", "correct");
        mensaje.put("row", fila);
        mensaje.put("col", columna);

        for (WebSocket jugador : jugadores.keySet()) {
            jugador.send(mensaje.toString());
        }
    }

    // Metodos de turons
    private void siguienteTurno() {

        if (jugadores.isEmpty()) {
            jugadorTurno = null;
            return;
        }

        WebSocket siguiente = null;
        boolean siguienteEncontrado = false;

        for (WebSocket jugador : jugadores.keySet()) {

            if (siguienteEncontrado) {
                siguiente = jugador;
                break;
            }

            if (jugador == jugadorTurno) {
                siguienteEncontrado = true;
            }
        }

        if (siguiente == null) {
            siguiente = jugadores.keySet().iterator().next();
        }

        jugadorTurno = siguiente;

        System.out.println(
            "CAMBIO DE TURNO → " + jugadores.get(jugadorTurno)
        );
    }
    private void enviarTurno() {

        String nombreJugador = jugadores.get(jugadorTurno);
        
        JSONObject mensaje = new JSONObject();
        
        mensaje.put("type", "turn");
        mensaje.put("player", nombreJugador);
        
        for (WebSocket jugador : jugadores.keySet()) {
            jugador.send(mensaje.toString());
        }
    }

    public static void main(String[] args) {
        Servidor servidor = new Servidor(3000);
        servidor.start();

        System.out.println("Esperando conexiones...");
    }
}