package com.example;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        UtilsViews.addView(Main.class, "VistaInici", "/VistaInici.fxml");
        UtilsViews.addView(Main.class, "VistaJoc", "/VistaJoc.fxml");
        UtilsViews.addView(Main.class, "VistaResultat", "/VistaResultat.fxml");

        Scene scene = new Scene(UtilsViews.parentContainer);

        stage.setTitle("Joc Sudoku");
        stage.setScene(scene);

        // Cerrar la conexion al servidor cuando se cierre
        ControllerVistaInici controller = (ControllerVistaInici) UtilsViews.getController("VistaInici");
        stage.setOnCloseRequest(event -> {
            controller.cerrarConexion();
        });

        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}