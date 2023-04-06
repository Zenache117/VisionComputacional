package Portafolio;

import java.io.FileWriter;
import java.io.IOException;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class Negativo {
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

		Mat Negativo = new Mat(imageGray.rows(), imageGray.cols(), CvType.CV_8UC1);

		// Recorrer cada pixel de la imagen
		for (int i = 0; i < imageGray.rows(); i++) {
			for (int j = 0; j < imageGray.cols(); j++) {
				// Obtener el valor del pixel
				double[] pixelValue = imageGray.get(i, j);

				// Invertir el valor del pixel
				Negativo.put(i, j, 255 - pixelValue[0]);
			}
		}

		// Seleccionar la carpeta destino para guardar la imagen transformada
		CarpetaDestino carpetaDestino = new CarpetaDestino();

		String rutaCarpetaDestino = carpetaDestino.selectCarpet();

		// Guardar la imagen transformada en la carpeta seleccionada
		Imgcodecs.imwrite(rutaCarpetaDestino + "./Negativo.jpg", Negativo);

		// Guardar matriz de imagen
        FileWriter writer;
        try {
            writer = new FileWriter(rutaCarpetaDestino + "/ImagenMatriz.csv");

            for (int i = 0; i < Negativo.rows(); i++) {
                for (int j = 0; j < Negativo.cols(); j++) {
                    double[] value = Negativo.get(i, j);
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
