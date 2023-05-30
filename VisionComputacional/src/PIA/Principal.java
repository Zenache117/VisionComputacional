package PIA;

import java.io.FileWriter;
import java.io.IOException;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.TermCriteria;
import org.opencv.imgcodecs.Imgcodecs;
import Portafolio.CarpetaDestino;
import Portafolio.SeleccionarArchivo;

/*Este programa procesa una imagen seleccionada por el usuario aplicando dos métodos de umbralización diferentes: Sauvola y Niblack. A continuación, se detallan los pasos que sigue el programa para procesar la imagen:
 * 
Se abre un diálogo para que el usuario seleccione una imagen a procesar.

La biblioteca OpenCV se carga en el programa.

La imagen seleccionada se lee y se almacena en una matriz.

A continuación, el programa convierte la imagen a escala de grises. 
Esto se logra creando una nueva Mat de tipo CV_8U con las mismas dimensiones que la imagen original y, a través de un bucle anidado que recorre cada píxel 
de la imagen, se aplica la siguiente fórmula para obtener el valor de gris de cada píxel:
grayValue = 0.299 * pixel[2] + 0.587 * pixel[1] + 0.114 * pixel[0]
Donde pixel[0], pixel[1] y pixel[2] representan los valores de cada componente de color (azul, verde y rojo) del píxel actual.

El siguiente paso es aplicar un algoritmo de segmentación previa para separar los objetos de la imagen en dos grupos. El método utilizado para esta tarea es K-means, el cual se aplica a una versión "aplanada" de la imagen en escala 
de grises. Primero, se recorre la imagen y se almacena cada valor de píxel en una nueva Mat llamada "reshaped". Luego se aplica k-means. 
(en este caso, 2), una matriz para almacenar las etiquetas de cada píxel y otra matriz para almacenar los centros de cada grupo. La función kmeans devuelve los centros de los grupos, que se usan para crear una nueva imagen segmentada, 
donde cada píxel se asigna al centro de su respectivo grupo.

El programa calcula un histograma de la imagen segmentada para su posterior análisis. El histograma se almacena en una matriz de tipo CV_64F.

Luego, se aplica un método de umbralización llamado "Niblack" para binarizar la imagen. Este método utiliza una ventana de píxeles en cada píxel de la imagen para calcular el umbral local. El umbral local se calcula como la media de la 
ventana menos un factor multiplicado por la desviación estándar de la ventana. El factor utilizado es -0.2. Los píxeles con un valor superior al umbral se asignan a 255 (blanco) y los píxeles con un valor inferior se asignan a 0 (negro). 
El resultado se almacena en una matriz llamada "thresholdNiblack".

Además del método Niblack, el programa también aplica el método Sauvola para binarizar la imagen. El método Sauvola es similar al método Niblack, pero utiliza un factor adaptativo basado en el contraste local y la desviación estándar de 
la ventana en lugar de un factor fijo. Este método produce resultados mejores que Niblack en imágenes con iluminación desigual. Para calcular el umbral de Sauvola, se utiliza la siguiente fórmula:
threshold = mean * (1 + k * ((stdDev / R) - 1))

Se aplica el umbral de Sauvola a la imagen y se guarda el resultado en una nueva matriz.

Se muestran las imágenes resultantes de aplicar los umbrales de Sauvola y Niblack al usuario.

En resumen, el programa convierte una imagen a escala de grises y aplica los métodos de umbralización de Sauvola y Niblack para obtener versiones binarias de la imagen. El usuario puede seleccionar cualquier imagen para procesarla con el programa.*/

public class Principal {

	public static void main(String[] args) {

		SeleccionarArchivo archivoSeleccionado = new SeleccionarArchivo();

		String rutaImagen = archivoSeleccionado.selectFile();

		// Cargar la biblioteca OpenCV
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

		// Leer la imagen seleccionada
		Mat image = Imgcodecs.imread(rutaImagen);

		// Convertir a escala de grises manualmente
		Mat gray = new Mat(image.rows(), image.cols(), CvType.CV_8U);
		for (int row = 0; row < image.rows(); row++) {
			for (int col = 0; col < image.cols(); col++) {
				double[] pixel = image.get(row, col);
				double grayValue = 0.299 * pixel[2] + 0.587 * pixel[1] + 0.114 * pixel[0];
				gray.put(row, col, grayValue);
			}
		}

		// Calcular umbral de Niblack
		Mat thresholdNiblack = new Mat(gray.size(), CvType.CV_8UC1);
		for (int row = 0; row < gray.rows(); row++) {
			for (int col = 0; col < gray.cols(); col++) {
				// Calcular la media y la desviaci�n est�ndar de la ventana
				int windowSize = 15;
				int windowOffset = windowSize / 2;
				double sum = 0;
				double sumSquared = 0;
				for (int i = row - windowOffset; i <= row + windowOffset; i++) {
					for (int j = col - windowOffset; j <= col + windowOffset; j++) {
						if (i >= 0 && i < gray.rows() && j >= 0 && j < gray.cols()) {
							double pixelValue = gray.get(i, j)[0];
							sum += pixelValue;
							sumSquared += pixelValue * pixelValue;
						}
					}
				}
				double windowSizeDouble = windowSize * windowSize;
				double mean = sum / windowSizeDouble;
				double stdDev = Math.sqrt((sumSquared / windowSizeDouble) - (mean * mean));

				// Calcular el umbral de Niblack
				double k = -0.2;
				double thresholdValue = mean + (k * stdDev);
				double pixelValue = gray.get(row, col)[0];
				if (pixelValue > thresholdValue) {
					thresholdNiblack.put(row, col, 255);
				} else {
					thresholdNiblack.put(row, col, 0);
				}
			}
		}
		System.out.println("Umbral de Niblack: " + Core.mean(thresholdNiblack).val[0]);

		// Calcular el MSE
		double mse = 0;
		for (int i = 0; i < gray.rows(); i++) {
			for (int j = 0; j < gray.cols(); j++) {
				double[] pixelOriginal = gray.get(i, j);
				double[] pixelProcesada = thresholdNiblack.get(i, j);
				double diff = pixelOriginal[0] - pixelProcesada[0];
				mse += diff * diff;
			}
		}
		mse /= gray.rows() * gray.cols();

		// Calcular el PSNR
		double psnr_Niblack = 20 * Math.log10(255.0 / Math.sqrt(mse));

		System.out.println("PSNR_Niblack: " + psnr_Niblack);

		// Aplicar tecnica Sauvola
		Mat sauvola = new Mat(gray.rows(), gray.cols(), CvType.CV_8UC1);
		int windowSize = 25;
		// Mismo valor de K utilizado en el articulo
		double k = 0.5;
		for (int row = 0; row < gray.rows(); row++) {
			for (int col = 0; col < gray.cols(); col++) {
				double[] pixel = gray.get(row, col);
				double sum = 0;
				double sumSquared = 0;
				int count = 0;
				for (int i = -windowSize / 2; i <= windowSize / 2; i++) {
					for (int j = -windowSize / 2; j <= windowSize / 2; j++) {
						if (row + i < 0 || row + i >= gray.rows() || col + j < 0 || col + j >= gray.cols()) {
							continue;
						}
						double[] neighbor = gray.get(row + i, col + j);
						sum += neighbor[0];
						sumSquared += Math.pow(neighbor[0], 2);
						count++;
					}
				}
				double mean = sum / count;
				double variance = (sumSquared - Math.pow(sum, 2) / count) / count;
				double thresholdValue = mean * (1 + k * (Math.sqrt(variance / 128) - 1));
				if (pixel[0] > thresholdValue) {
					sauvola.put(row, col, 255);
				} else {
					sauvola.put(row, col, 0);
				}
			}
		}
		System.out.println("Umbral de Sauvola: " + Core.mean(sauvola).val[0]);

		// Calcular el MSE
		mse = 0;
		for (int i = 0; i < gray.rows(); i++) {
			for (int j = 0; j < gray.cols(); j++) {
				double[] pixelOriginal = gray.get(i, j);
				double[] pixelProcesada = sauvola.get(i, j);
				double diff = pixelOriginal[0] - pixelProcesada[0];
				mse += diff * diff;
			}
		}
		mse /= gray.rows() * gray.cols();

		// Calcular el PSNR
		double psnr_Sauvola = 20 * Math.log10(255.0 / Math.sqrt(mse));

		System.out.println("PSNR_Sauvola: " + psnr_Sauvola);

		// Seleccionar la carpeta destino para guardar la imagen transformada
		CarpetaDestino carpetaDestino = new CarpetaDestino();

		String rutaCarpetaDestino = carpetaDestino.selectCarpet();

		Imgcodecs.imwrite(rutaCarpetaDestino + "/ResultadoSauvola.jpg", sauvola);
		Imgcodecs.imwrite(rutaCarpetaDestino + "/ResultadoNiblack.jpg", thresholdNiblack);

		// Guardar matriz de imagen Sauvola
		try {
			FileWriter writer = new FileWriter(rutaCarpetaDestino + "/ImagenMatrizBordesBinarizados.csv");

			for (int i = 0; i < sauvola.rows(); i++) {
				for (int j = 0; j < sauvola.cols(); j++) {
					double[] value = sauvola.get(i, j);
					writer.write(String.valueOf(value[0]));
					if (j < sauvola.cols() - 1) {
						writer.write(",");
					}
				}
				writer.write("\n");
			}

			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Guardar matriz de imagen Bordes
		try {
			FileWriter writer = new FileWriter(rutaCarpetaDestino + "/ImagenMatrizBordesBinarizados.csv");

			for (int i = 0; i < thresholdNiblack.rows(); i++) {
				for (int j = 0; j < thresholdNiblack.cols(); j++) {
					double[] value = thresholdNiblack.get(i, j);
					writer.write(String.valueOf(value[0]));
					if (j < thresholdNiblack.cols() - 1) {
						writer.write(",");
					}
				}
				writer.write("\n");
			}

			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}