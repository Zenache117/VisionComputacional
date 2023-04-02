package Portafolio;

import java.util.Arrays;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.core.Point;

public class ImpresionHistograma {

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

		// Generar el histograma manualmente con ciclos
		int[] histogram = new int[256];
		for (int row = 0; row < imageGray.rows(); row++) {
			for (int col = 0; col < imageGray.cols(); col++) {
				double[] pixel = imageGray.get(row, col);
				int intensity = (int) pixel[0];
				histogram[intensity]++;
			}
		}

		// Seleccionar la carpeta destino para guardar la imagen transformada
		CarpetaDestino carpetaDestino = new CarpetaDestino();

		String rutaCarpetaDestino = carpetaDestino.selectCarpet();

		// Encontrar el valor m�ximo del histograma
		int maxVal = Arrays.stream(histogram).max().getAsInt();

		// Crear una imagen en blanco para el histograma
		Mat histImage = new Mat(maxVal + 50, imageGray.cols() * 2, CvType.CV_8UC3, new Scalar(0, 0, 0));

		// Escalar el histograma para que encaje en la imagen
		Core.normalize(new MatOfInt(histogram), new MatOfInt(), 0, histImage.rows(), Core.NORM_MINMAX, CvType.CV_32F);

		// Dibujar el histograma
		for (int i = 0; i < 256; i++) {
			Imgproc.line(histImage, new Point(i * 2, histImage.rows()),
					new Point(i * 2, histImage.rows() - histogram[i] * 2), new Scalar(0, 255, 0), 1, Imgproc.LINE_AA,
					0);
		}

		// Guardar la imagen en la carpeta destino
		String nombreArchivo = "histograma.png";
		String rutaDestino = rutaCarpetaDestino + "\\" + nombreArchivo;
		Imgcodecs.imwrite(rutaDestino, histImage);

	}

}