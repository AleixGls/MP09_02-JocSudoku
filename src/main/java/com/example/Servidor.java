package com.example;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

import java.net.InetSocketAddress;


public class Servidor extends WebSocketServer {

    private Map<WebSocket, String> jugadores = new HashMap<>();
    private Sudoku partidaActual;



    public Servidor(int puerto) {
        super(new InetSocketAddress(puerto));
    }


    
    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        System.out.println("Cliente conectado: " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        String jugador = jugadores.remove(conn);
        System.out.println("Cliente desconectado: " + jugador);
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

            if (partidaActual == null) {
                partidaActual = new Sudoku();
                partidaActual.generarSolucion();

                System.out.println("Nueva partida de Sudoku creada.");
            }

            System.out.println("Jugador conectado: " + name);
            System.out.println("Jugadores conectados: " + jugadores.values());

            enviarListaJugadores();
            enviarTablero();
        }

        if (type.equals("move")) {

            String jugador = jugadores.get(conn);

            int fila = obj.getInt("row");
            int columna = obj.getInt("col");
            int numero = obj.getInt("number");

            System.out.println(
                "Jugada de " + jugador +
                ": fila=" + fila +
                ", columna=" + columna +
                ", numero=" + numero
            );

            if (partidaActual.esCorrecto(fila, columna, numero)) {
                System.out.println("Jugada CORRECTA");

                partidaActual.ponerNumero(fila, columna, numero);

                enviarTablero();

                enviarCasillaCorrecta(fila, columna);

            } else {
                System.out.println("Jugada INCORRECTA");
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
    
    public static void main(String[] args) {
        Servidor servidor = new Servidor(3000);
        servidor.start();

        System.out.println("Esperando conexiones...");
    }
}