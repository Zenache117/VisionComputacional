package Portafolio;

import java.io.FileWriter;
import java.io.IOException;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class CopiarImagen {
	public static void main(String[] args) {

		// Seleccionar la imagen a transformar
		SeleccionarArchivo archivoSeleccionado = new SeleccionarArchivo();

		String rutaImagen = archivoSeleccionado.selectFile();

		// Cargar la biblioteca OpenCV
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

		// Leer la imagen seleccionada
		Mat image = Imgcodecs.imread(rutaImagen);

		// Convertir la imagen a escala de grises
		Mat imageGray = new Mat();
		Imgproc.cvtColor(image, imageGray, Imgproc.COLOR_BGR2GRAY);

		// Generar matriz de imagen copiada en grises
		Mat imageCopia = new Mat(imageGray.rows(), imageGray.cols(), imageGray.type());

		// Recorrer cada pixel de la imagen
		for (int i = 0; i < imageGray.rows(); i++) {
			for (int j = 0; j < imageGray.cols(); j++) {
				// Obtener el valor del pixel
				double[] pixelValue = imageGray.get(i, j);

				// Copiar el valor del pixel
				imageCopia.put(i, j, pixelValue);
			}
		}

		// Seleccionar la carpeta destino para guardar la imagen transformada
		CarpetaDestino carpetaDestino = new CarpetaDestino();

		String rutaCarpetaDestino = carpetaDestino.selectCarpet();

		// Guardar la imagen transformada en la carpeta seleccionada
		Imgcodecs.imwrite(rutaCarpetaDestino + "./Original.jpg", imageGray);
		Imgcodecs.imwrite(rutaCarpetaDestino + "./ImagenCopia.jpg", imageCopia);

		//Guardar matriz de imagen copiada
				try {
				    FileWriter writer = new FileWriter(rutaCarpetaDestino + "/ImagenMatrizCopia.csv");

				    for (int i = 0; i < imageCopia.rows(); i++) {
				        for (int j = 0; j < imageCopia.cols(); j++) {
				            double[] value = imageCopia.get(i, j);
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
