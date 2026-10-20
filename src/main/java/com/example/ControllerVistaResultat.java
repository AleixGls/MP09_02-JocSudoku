package com.example;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ControllerVistaResultat {

    // Elementos visuales
    @FXML
    private VBox listaResultados;
    @FXML
    private Button btnVolverJugar;



    // Variables
    private UtilsWS wsClient;



    // Pasar el json de resultados a la lista del ranking en el fxml
    public void mostrarRanking(JSONArray rankingJSON) {

        // Crear un array con los objetos JSON extraidos del mensaje final del servidor
        List<JSONObject> ranking = new ArrayList<>();
        for (int i = 0; i < rankingJSON.length(); i++) {
            ranking.add(rankingJSON.getJSONObject(i));
        }

        // Ordenar la lista de ranking por los puntos
        ranking.sort((jugador1, jugador2) -> {
            return Integer.compare(
                jugador2.getInt("points"),
                jugador1.getInt("points")
            );
        });

        // Borrar la lista por si habia algo de las anteriores partidas
        listaResultados.getChildren().clear();

        // Pasar los objetos extraidos del json a variables
        // y ponerlas en la lista visible en el fxml
        for (int i = 0; i < ranking.size(); i++) {

            // Pasar el objeto que estamos iterando dentro del array 
            // a un objeto unico en la iteracion para luego extraer los datos
            JSONObject jugador = ranking.get(i);

            // Obtener los datos del objeto en variables
            String nombre = jugador.getString("name");
            int puntos = jugador.getInt("points");

            // Poner las variables en una nueva label, ponerle estilos y insertarla visualmente
            Label label = new Label(
                (i + 1) + ". " + nombre + " - " + puntos + " puntos"
            );
            label.setStyle("-fx-font-size: 20px;");
            listaResultados.getChildren().add(label);
        }
    }

    // Metodo para poder recibir el wsClient desde ControllerVistaInici y usarlo aqui
    public void setWebSocket(UtilsWS wsClient) {
        this.wsClient = wsClient;
    }

    // Metodo que se ejecuta al inicializar la clase en main
    // y añade el evento para reiniciar la partida al boton
    @FXML
    public void initialize() {

        //Añadir evento al boton
        btnVolverJugar.setOnAction(event -> solicitarReinicio());
    }

    // Metodo para solicitar el reinicio de la partida al servidor
    private void solicitarReinicio() {

        // Si no hemos obtenido el wsClient, el metodo acabara y no hara nada
        if (wsClient == null) {
            return;
        }

        // Crear un objeto JSON y enviar el mensaje de la solicitud al servidor usando el wsClient
        JSONObject mensaje = new JSONObject();
        mensaje.put("type", "restart");
        wsClient.safeSend(mensaje.toString());
        System.out.println("Solicitando reinicio de partida...");
    }
}