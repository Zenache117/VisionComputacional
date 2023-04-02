package Portafolio;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;

public class ReconocimientoFacial {
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

		// Cargar el classificador Haar para detectar rostros
		CascadeClassifier faceDetector = new CascadeClassifier();
		faceDetector.load("C:/opencv/sources/data/lbpcascades/lbpcascade_frontalface.xml");

		// Detectar rostros en la imagen
		MatOfRect faceDetections = new MatOfRect();
		faceDetector.detectMultiScale(imageGray, faceDetections);
		for (Rect rect : faceDetections.toArray()) {
			Imgproc.rectangle(image, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height),
					new Scalar(0, 0, 255), 2);
		}

		// Seleccionar la carpeta destino para guardar la imagen transformada
		CarpetaDestino carpetaDestino = new CarpetaDestino();

		String rutaCarpetaDestino = carpetaDestino.selectCarpet();

		// Guardar la imagen transformada en la carpeta seleccionada
		Imgcodecs.imwrite(rutaCarpetaDestino + "./ImagenTransformada.jpg", imageGray);
		Imgcodecs.imwrite(rutaCarpetaDestino + "./RostroResaltado.jpg", image);

	}
}
