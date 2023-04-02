package Portafolio;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import java.io.FileWriter;
import java.io.IOException;

public class MatrizImagen {

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

		// Seleccionar la carpeta destino para guardar la imagen transformada
		CarpetaDestino carpetaDestino = new CarpetaDestino();

		String rutaCarpetaDestino = carpetaDestino.selectCarpet();

		FileWriter writer;
		try {
			writer = new FileWriter(rutaCarpetaDestino + "/ImagenMatriz.csv");

			for (int i = 0; i < imageGray.rows(); i++) {
				for (int j = 0; j < imageGray.cols(); j++) {
					double[] value = imageGray.get(i, j);
					writer.write(String.valueOf(value[0]) + ",");
				}
				writer.write("\n");
			}

			writer.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
