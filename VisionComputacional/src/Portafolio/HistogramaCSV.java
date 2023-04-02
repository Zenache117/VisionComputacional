package Portafolio;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class HistogramaCSV {
    public static void main(String[] args) {
        // Seleccionar el archivo CSV
    	SeleccionarArchivo archivoSeleccionado = new SeleccionarArchivo();

		String rutaImagen = archivoSeleccionado.selectFile();

        // Leer el archivo CSV
        List<List<Integer>> tabla = new ArrayList<>();
        try (Scanner filaScanner = new Scanner(new File(rutaImagen))) {
            while (filaScanner.hasNextLine()) {
                List<Integer> fila = new ArrayList<>();
                String filaStr = filaScanner.nextLine();
                String[] filaArr = filaStr.split(",");
                for (String valor : filaArr) {
                    fila.add(Integer.parseInt(valor.trim()));
                }
                tabla.add(fila);
            }
        } catch (IOException e) {
            System.err.println("Error al leer el archivo CSV: " + e.getMessage());
            return;
        }

        // Calcular el histograma
        List<Integer> histograma = new ArrayList<>();
        for (List<Integer> fila : tabla) {
            for (Integer valor : fila) {
                if (valor >= histograma.size()) {
                    int numNuevosBins = valor - histograma.size() + 1;
                    for (int i = 0; i < numNuevosBins; i++) {
                        histograma.add(0);
                    }
                }
                histograma.set(valor, histograma.get(valor) + 1);
            }
        }

        // Imprimir el histograma
        System.out.println("Histograma:");
        for (int i = 0; i < histograma.size(); i++) {
            System.out.println(i + ": " + histograma.get(i));
        }
    }
}
