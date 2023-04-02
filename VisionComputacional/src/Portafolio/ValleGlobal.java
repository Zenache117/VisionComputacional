package Portafolio;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class ValleGlobal {

	public static void main(String[] args) {

		// Leer CSV
		SeleccionarArchivo archivoSeleccionado = new SeleccionarArchivo();

		String rutaImagen = archivoSeleccionado.selectFile();

		// Cargar la biblioteca OpenCV
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

		// Leer la imagen seleccionada
		Mat image = Imgcodecs.imread(rutaImagen);

		// Convertir la imagen a escala de grises
		Mat imageGray = new Mat();
		Imgproc.cvtColor(image, imageGray, Imgproc.COLOR_BGR2GRAY);

		// Generar el histograma manualmente con ciclos
		int[] histogram = new int[256];
		for (int row = 0; row < imageGray.rows(); row++) {
			for (int col = 0; col < imageGray.cols(); col++) {
				double[] pixel = imageGray.get(row, col);
				int intensity = (int) pixel[0];
				histogram[intensity]++;
			}
		}

		int[] histograma = new int[256];
		double media = 0;
		for (int fila = 0; fila < imageGray.rows(); fila++) {
			for (int col = 0; col < imageGray.cols(); col++) {
				double[] pixel = imageGray.get(fila, col);
				int intensidad = (int) pixel[0];
				histograma[intensidad]++;
				media += intensidad;
			}
		}
		media /= imageGray.rows() * imageGray.cols();

		double varianza = 0;
		for (int i = 0; i < histograma.length; i++) {
			double probabilidad = (double) histograma[i] / (imageGray.rows() * imageGray.cols());
			varianza += probabilidad * Math.pow((i - media), 2);
		}

		double desviacionEstandar = Math.sqrt(varianza);

		List<Double> resultado = new ArrayList<Double>();
		for (int i = 1; i < histogram.length - 1; i++) {
			double F = ((desviacionEstandar * (histogram[i - 1] - histogram[i])
					+ (desviacionEstandar * (histogram[i] - histogram[i + 1])))) / 2;
			resultado.add(F);
		}

		double maximo = Double.MIN_VALUE; // Inicializar la variable con el valor m�nimo posible

		// Recorrer todos los elementos de la lista
		for (double valor : resultado) {
			if (valor > maximo) {
				maximo = valor; // Si el valor actual es mayor al m�ximo encontrado hasta ahora, se actualiza la
				// variable
			}
		}

		System.out.println(maximo);

		// Encontrar el valor maximo del histograma
		int maxVal = Arrays.stream(histogram).max().getAsInt();

	}

}
