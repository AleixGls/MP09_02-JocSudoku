package com.example;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class ControllerVistaInici {

    // Elementos y variables
    @FXML
    private TextField txtServidor;

    @FXML
    private TextField txtJugador;

    @FXML 
    private Label lblError;
    
    @FXML 
    private Button btnConectar;

    private UtilsWS wsClient;



    // Metodos
    //Metodo que se ejecuta cuando se inicia la vista
    @FXML
    public void initialize() {

        // Limpiar label errores al iniciar
        lblError.setText("");

        // Bloquear boton si no hay nada escrito en los textField
        actualizarEstadoBoton();
        txtServidor.textProperty().addListener((observable, oldValue, newValue) -> {
            actualizarEstadoBoton();
        });
        txtJugador.textProperty().addListener((observable, oldValue, newValue) -> {
            actualizarEstadoBoton();
        });
    }

    // Metodo para conectar al servidor
    @FXML
    private void handleConectar() {

        lblError.setText("");

        String servidor = txtServidor.getText().trim();
        String jugador = txtJugador.getText().trim();

        if (servidor.isEmpty()) {
            lblError.setText("Debes introducir el servidor.");
            return;
        }

        if (jugador.isEmpty()) {
            lblError.setText("Debes introducir el nombre de jugador.");
            return;
        }

        String serverUri = "ws://" + servidor + ":3000";

        wsClient = UtilsWS.getSharedInstance(serverUri);

        wsClient.onOpen(this::handleOpen);
        wsClient.onError(this::handleError);
    }

    // Metodos de soporte para la conexion al servidor
    private void handleOpen(String message) {
        System.out.println("CONEXION CORRECTA");
        System.out.println(message);

        ControllerVistaJoc controllerJoc = (ControllerVistaJoc) UtilsViews.getController("VistaJoc");
        controllerJoc.setWebSocket(wsClient);

        String jugador = txtJugador.getText().trim();
        wsClient.safeSend(
            "{\"type\":\"join\",\"name\":\"" + jugador + "\"}"
        );
        Platform.runLater(() -> {
            UtilsViews.setViewAnimating("VistaJoc");
        });
        
    }
    private void handleError(String message) {
        System.out.println("ERROR DE CONEXION");
        System.out.println(message);
    }
    public void cerrarConexion() {
        if (wsClient != null) {
            wsClient.forceExit();
        }
    }

    // Metodo para bloquear boton si no hay nada escrito en los textField
    private void actualizarEstadoBoton() {
        boolean servidorVacio = txtServidor.getText().trim().isEmpty();
        boolean jugadorVacio = txtJugador.getText().trim().isEmpty();

        btnConectar.setDisable(servidorVacio || jugadorVacio);
    }
}