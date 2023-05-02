package Portafolio;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;



public class ProyeccionDeLineas {

	public static void main(String[] args) {

		// Seleccionar la imagen a transformar
		SeleccionarArchivo archivoSeleccionado = new SeleccionarArchivo();
		String rutaImagen = archivoSeleccionado.selectFile();

		// Cargar la biblioteca OpenCV
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

		// Leer la imagen seleccionada
		Mat image = Imgcodecs.imread(rutaImagen);

		RansacLineDetector detector = new RansacLineDetector(1000, 1.0, 100, 50);
		detector.detectLines(image);

	}

}
