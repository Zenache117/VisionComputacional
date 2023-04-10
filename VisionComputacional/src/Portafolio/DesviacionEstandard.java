package Portafolio;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.CvType;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class DesviacionEstandard {

    public static void main(String[] args) {
        try {
            // Crear un objeto JFileChooser
            JFileChooser fileChooser = new JFileChooser();

            // Mostrar el diálogo de selección de archivo
            int result = fileChooser.showOpenDialog(null);

            // Si el usuario selecciona un archivo
            if (result == JFileChooser.APPROVE_OPTION) {
                // Obtener la ruta del archivo seleccionado
                File selectedFile = fileChooser.getSelectedFile();
                String filePath = selectedFile.getAbsolutePath();

                // Cargar la imagen desde el archivo seleccionado
                BufferedImage image = ImageIO.read(new File(filePath));

                // Obtener el ancho y alto de la imagen
                int width = image.getWidth();
                int height = image.getHeight();

                // Convertir la imagen en una matriz bidimensional de valores de píxeles
                int[][] pixels = new int[width][height];
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        pixels[x][y] = image.getRGB(x, y) & 0xFF;
                    }
                }

                // Calcular la media de los valores de los píxeles
                double sum = 0;
                for (int[] row : pixels) {
                    for (int value : row) {
                        sum += value;
                    }
                }
                double mean = sum / (width * height);

                // Calcular la suma de los cuadrados de las diferencias entre cada valor de píxel y la media
                double sqDiffSum = 0;
                for (int[] row : pixels) {
                    for (int value : row) {
                        double diff = value - mean;
                        sqDiffSum += diff * diff;
                    }
                }

                // Dividir la suma de los cuadrados de las diferencias entre cada valor de píxel y la media por el número total de píxeles menos 1
                double variance = sqDiffSum / (width * height - 1);

                // Calcular la raíz cuadrada del resultado obtenido en el paso anterior para obtener la desviación estándar
                double stdDeviation = Math.sqrt(variance);

                System.out.println("La desviación estándar de la imagen es: " + stdDeviation);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
