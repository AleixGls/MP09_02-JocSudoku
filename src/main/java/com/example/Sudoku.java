package com.example;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Sudoku {

    private int[][] solucion;
    private int[][] tablero;



    public Sudoku() {
        solucion = new int[9][9];
        tablero = new int[9][9];
    }
    

    // Solucion
    public void generarSolucion() {
        rellenar(0, 0);
        crearTablero();
        quitarNumeros();
    }
    private boolean rellenar(int fila, int columna) {

        // Si hemos llegado después de la última fila,
        // significa que hemos rellenado todo el Sudoku.
        if (fila == 9) {
            return true;
        }

        // Calculamos cuál es la siguiente casilla.
        int siguienteFila = fila;
        int siguienteColumna = columna + 1;

        if (siguienteColumna == 9) {
            siguienteFila++;
            siguienteColumna = 0;
        }
 
        // Probamos los números del 1 al 9 en orden al azar.
        List<Integer> numeros = new ArrayList<>();

        for (int numero = 1; numero <= 9; numero++) {
            numeros.add(numero);
        }

        Collections.shuffle(numeros);

        for (int numero : numeros) {
        
            if (esValido(fila, columna, numero)) {
            
                solucion[fila][columna] = numero;
            
                if (rellenar(siguienteFila, siguienteColumna)) {
                    return true;
                }
            
                solucion[fila][columna] = 0;
            }
        }

        return false;
    }
    private boolean esValido(int fila, int columna, int numero) {

        // Comprobar fila
        for (int columnaActual = 0; columnaActual < 9; columnaActual++) {
            if (solucion[fila][columnaActual] == numero) {
                return false;
            }
        }

        // Comprobar columna
        for (int filaActual = 0; filaActual < 9; filaActual++) {
            if (solucion[filaActual][columna] == numero) {
                return false;
            }
        }

        // Comprobar cuadrado 3x3
        int inicioFila = (fila / 3) * 3;
        int inicioColumna = (columna / 3) * 3;

        for (int filaActual = inicioFila; filaActual < inicioFila + 3; filaActual++) {
            for (int columnaActual = inicioColumna; columnaActual < inicioColumna + 3; columnaActual++) {

                if (solucion[filaActual][columnaActual] == numero) {
                    return false;
                }
            }
        }

        return true;
    }

    // Tablero
    private void crearTablero() {
        for (int fila = 0; fila < 9; fila++) {
            for (int columna = 0; columna < 9; columna++) {
                tablero[fila][columna] = solucion[fila][columna];
            }
        }
    }
    private void quitarNumeros() {

        int casillasAEliminar = 45;

        while (casillasAEliminar > 0) {

            int fila = (int) (Math.random() * 9);
            int columna = (int) (Math.random() * 9);

            if (tablero[fila][columna] != 0) {
                tablero[fila][columna] = 0;
                casillasAEliminar--;
            }
        }
    }

    public int[][] getTablero() {
        return tablero;
    }

    public boolean esCorrecto(int fila, int columna, int numero) {
        return solucion[fila][columna] == numero;
    }

    public void ponerNumero(int fila, int columna, int numero) {
        tablero[fila][columna] = numero;
    }
}