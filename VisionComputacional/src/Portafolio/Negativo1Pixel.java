package Portafolio;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JOptionPane;

public class Negativo1Pixel {
	    public static void main(String[] args) {

			SeleccionarArchivo archivoSeleccionado = new SeleccionarArchivo();

			String rutaImagen = archivoSeleccionado.selectFile();

			// Cargar la biblioteca OpenCV
			System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

			// Leer la imagen seleccionada
			Mat image = Imgcodecs.imread(rutaImagen);

			// Convertir la imagen a escala de grises
			Mat imageGray = new Mat();
			Imgproc.cvtColor(image, imageGray, Imgproc.COLOR_BGR2GRAY);

	        // Obtener el tamaño de la imagen
	        int height = imageGray.rows();
	        int width = imageGray.cols();

	        // Preguntar al usuario las coordenadas del pixel a modificar
	        int row = -1, col = -1;
	        String input="";
	        while (row < 0 || row >= height || col < 0 || col >= width) {
	            input = JOptionPane.showInputDialog("Ingrese la fila y columna del pixel a modificar (x,y)");
	            String[] coords = input.split(",");
	            row = Integer.parseInt(coords[0].trim());
	            col = Integer.parseInt(coords[1].trim());
	        }

	        // Obtener el valor del pixel seleccionado
	        double[] pixelValue = imageGray.get(row, col);

	        // Invertir el valor del pixel
	        imageGray.put(row, col, 255 - pixelValue[0]);

	     // Seleccionar la carpeta destino para guardar la imagen transformada
			CarpetaDestino carpetaDestino = new CarpetaDestino();

			String rutaCarpetaDestino = carpetaDestino.selectCarpet();

			// Guardar la imagen transformada en la carpeta seleccionada
			Imgcodecs.imwrite(rutaCarpetaDestino + "./Negativo1Pixel1pxl.jpg", imageGray);
			
			// Guardar matriz de imagen
	        FileWriter writer;
	        try {
	            writer = new FileWriter(rutaCarpetaDestino + "/ImagenMatrizNegativo1Pixel.csv");

	            for (int i = 0; i < imageGray.rows(); i++) {
	                for (int j = 0; j < imageGray.cols(); j++) {
	                    double[] value = imageGray.get(i, j);
	                    writer.write(String.valueOf(value[0]) + ",");
	                }
	                writer.write("\n");
	            }

	            writer.close();

	        } catch (IOException e) {
	            e.printStackTrace();
	        }
		}
	    
	}
