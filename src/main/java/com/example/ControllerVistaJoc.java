package com.example;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.json.JSONArray;
import org.json.JSONObject;

public class ControllerVistaJoc {

    @FXML
    private GridPane gridSudoku;

    @FXML
    private VBox panelJugadores;

    @FXML
    private VBox listaJugadores;

    @FXML
    private Label lblJugadorActual;

    @FXML
    private Button btnNumero1;

    @FXML
    private Button btnNumero2;

    @FXML
    private Button btnNumero3;

    @FXML
    private Button btnNumero4;

    @FXML
    private Button btnNumero5;

    @FXML
    private Button btnNumero6;

    @FXML
    private Button btnNumero7;

    @FXML
    private Button btnNumero8;

    @FXML
    private Button btnNumero9;

    private UtilsWS wsClient;

    private Button[][] celdas = new Button[9][9];
    private boolean[][] casillasCorrectas = new boolean[9][9];

    private int filaSeleccionada = -1;
    private int columnaSeleccionada = -1;
    private Button celdaSeleccionada = null;

    private String nombreJugador;
    private boolean esMiTurno = false;


    
    @FXML
    public void initialize() {
        System.out.println("ControllerVistaJoc inicializado");

        crearTablero();
        configurarBotonesNumeros();
    }

    // Metodos de soporte para la conexion al servidor
    public void setWebSocket(UtilsWS wsClient) {
        this.wsClient = wsClient;
        wsClient.onMessage(this::handleMessage);
    }
    private void handleMessage(String message) {
        System.out.println("JUEGO RECIBE: " + message);

        JSONObject obj = new JSONObject(message);

        String type = obj.getString("type");

        // Poner la lista de jugadores en la derecha
        if (type.equals("players")) {
            JSONArray players = obj.getJSONArray("players");
            
            Platform.runLater(() -> {
                listaJugadores.getChildren().clear();
                
                for (int i = 0; i < players.length(); i++) {
                    String nombre = players.getString(i);
                
                    Label jugador = new Label(nombre);
                    jugador.setStyle("-fx-font-size: 18px;");
                
                    listaJugadores.getChildren().add(jugador);
                }
            });
        }
        if (type.equals("board")) {

            JSONArray board = obj.getJSONArray("board");

            Platform.runLater(() -> {
            
                for (int fila = 0; fila < 9; fila++) {
                
                    JSONArray filaJson = board.getJSONArray(fila);
                
                    for (int columna = 0; columna < 9; columna++) {
                    
                        int numero = filaJson.getInt(columna);
                    
                        if (numero != 0) {
                            celdas[fila][columna].setText(String.valueOf(numero));
                            celdas[fila][columna].setDisable(true);
                        }
                    }
                }
            });
        }
        if (type.equals("correct")) {
        
            int fila = obj.getInt("row");
            int columna = obj.getInt("col");
        
            Platform.runLater(() -> {
            
                casillasCorrectas[fila][columna] = true;
            
                celdas[fila][columna].setStyle(
                    "-fx-background-color: lightgreen;"
                );
            
                celdas[fila][columna].setDisable(true);
            });
        }
        if (type.equals("turn")) {

            String jugador = obj.getString("player");

            Platform.runLater(() -> {
                lblJugadorActual.setText(jugador);

                boolean esMiTurno = jugador.equals(nombreJugador);

                if (this.esMiTurno && !esMiTurno) {
                    deseleccionarCasilla();
                }

                this.esMiTurno = esMiTurno;

                actualizarControlesTurno(esMiTurno);
            });
        }
    }

    // Metodo para crear el tablero de sudoku
    private void crearTablero() {
        gridSudoku.getChildren().clear();

        for (int fila = 0; fila < 9; fila++) {
            for (int columna = 0; columna < 9; columna++) {

                Button celda = new Button();
                celda.setPrefSize(50, 50);

                celdas[fila][columna] = celda;

                final int filaCelda = fila;
                final int columnaCelda = columna;

                celda.setOnAction(event -> {

                    if (!esMiTurno) {
                        return;
                    }

                
                    if (celdaSeleccionada != null) {
                    
                        if (casillasCorrectas[filaSeleccionada][columnaSeleccionada]) {
                            celdaSeleccionada.setStyle(
                                "-fx-background-color: lightgreen;"
                            );
                        } else {
                            celdaSeleccionada.setStyle("");
                        }
                    }
                
                    celdaSeleccionada = celda;
                    filaSeleccionada = filaCelda;
                    columnaSeleccionada = columnaCelda;
                
                    celdaSeleccionada.setStyle(
                        "-fx-background-color: lightblue;"
                    );
                
                    System.out.println(
                        "Casilla seleccionada: fila " +
                        filaSeleccionada +
                        ", columna " +
                        columnaSeleccionada
                    );
                });

                gridSudoku.add(celda, columna, fila);
            }
        }
    }

    // Metodos para poner los numeros en las casillas del tablero
    private void configurarBotonesNumeros() {
        btnNumero1.setOnAction(event -> ponerNumero(1));
        btnNumero2.setOnAction(event -> ponerNumero(2));
        btnNumero3.setOnAction(event -> ponerNumero(3));
        btnNumero4.setOnAction(event -> ponerNumero(4));
        btnNumero5.setOnAction(event -> ponerNumero(5));
        btnNumero6.setOnAction(event -> ponerNumero(6));
        btnNumero7.setOnAction(event -> ponerNumero(7));
        btnNumero8.setOnAction(event -> ponerNumero(8));
        btnNumero9.setOnAction(event -> ponerNumero(9));
    }
    private void ponerNumero(int numero) {

        if (celdaSeleccionada == null) {
            return;
        }

        JSONObject mensaje = new JSONObject();

        mensaje.put("type", "move");
        mensaje.put("row", filaSeleccionada);
        mensaje.put("col", columnaSeleccionada);
        mensaje.put("number", numero);

        wsClient.safeSend(mensaje.toString());

        System.out.println("Jugada enviada: " + mensaje);
    }

    public void setNombreJugador(String nombreJugador) {
        this.nombreJugador = nombreJugador;
    }
    private void actualizarControlesTurno(boolean esMiTurno) {

        btnNumero1.setDisable(!esMiTurno);
        btnNumero2.setDisable(!esMiTurno);
        btnNumero3.setDisable(!esMiTurno);
        btnNumero4.setDisable(!esMiTurno);
        btnNumero5.setDisable(!esMiTurno);
        btnNumero6.setDisable(!esMiTurno);
        btnNumero7.setDisable(!esMiTurno);
        btnNumero8.setDisable(!esMiTurno);
        btnNumero9.setDisable(!esMiTurno);
    }
    private void deseleccionarCasilla() {

        if (celdaSeleccionada != null) {

            if (casillasCorrectas[filaSeleccionada][columnaSeleccionada]) {
                celdaSeleccionada.setStyle(
                    "-fx-background-color: lightgreen;"
                );
            } else {
                celdaSeleccionada.setStyle("");
            }

            celdaSeleccionada = null;
            filaSeleccionada = -1;
            columnaSeleccionada = -1;
        }
    }
}
