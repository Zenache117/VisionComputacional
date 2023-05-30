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

/*Este programa procesa una imagen seleccionada por el usuario aplicando dos m√©todos de umbralizaci√≥n diferentes: Sauvola y Niblack. A continuaci√≥n, se detallan los pasos que sigue el programa para procesar la imagen:
 * 
Se abre un di√°logo para que el usuario seleccione una imagen a procesar.

La biblioteca OpenCV se carga en el programa.

La imagen seleccionada se lee y se almacena en una matriz.

A continuaci√≥n, el programa convierte la imagen a escala de grises. 
Esto se logra creando una nueva Mat de tipo CV_8U con las mismas dimensiones que la imagen original y, a trav√©s de un bucle anidado que recorre cada p√≠xel 
de la imagen, se aplica la siguiente f√≥rmula para obtener el valor de gris de cada p√≠xel:
grayValue = 0.299 * pixel[2] + 0.587 * pixel[1] + 0.114 * pixel[0]
Donde pixel[0], pixel[1] y pixel[2] representan los valores de cada componente de color (azul, verde y rojo) del p√≠xel actual.

Luego, se aplica un m√©todo de umbralizaci√≥n llamado "Niblack" para binarizar la imagen. Este m√©todo utiliza una ventana de p√≠xeles en cada p√≠xel de la imagen para calcular el umbral local. El umbral local se calcula como la media de la 
ventana menos un factor multiplicado por la desviaci√≥n est√°ndar de la ventana. El factor utilizado es -0.2 ("k" establecido por el documento del PIA). Los p√≠xeles con un valor superior al umbral se asignan a 255 (blanco) y los p√≠xeles con un valor inferior se asignan a 0 (negro). 
El resultado se almacena en una matriz llamada "thresholdNiblack".

Adem√°s del m√©todo Niblack, el programa tambi√©n aplica el m√©todo Sauvola para binarizar la imagen. El m√©todo Sauvola es similar al m√©todo Niblack, pero utiliza un factor adaptativo basado en el contraste local y la desviaci√≥n est√°ndar de 
la ventana en lugar de un factor fijo. Este m√©todo produce resultados mejores que Niblack en im√°genes con iluminaci√≥n desigual. Para calcular el umbral de Sauvola, se utiliza la siguiente f√≥rmula:
threshold = mean * (1 + k * ((stdDev / R) - 1))

Se aplica el umbral de Sauvola a la imagen y se guarda el resultado en una nueva matriz.

Se muestran las im√°genes resultantes de aplicar los umbrales de Sauvola y Niblack al usuario.

En resumen, el programa convierte una imagen a escala de grises y aplica los m√©todos de umbralizaci√≥n de Sauvola y Niblack para obtener versiones binarias de la imagen. El usuario puede seleccionar cualquier imagen para procesarla con el programa.*/

public class Principal {

	public static void main(String[] args) {

		SeleccionarArchivo archivoSeleccionado = new SeleccionarArchivo();

		String rutaImagen = archivoSeleccionado.selectFile();

		// Cargar la biblioteca OpenCV
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

		// Leer la imagen seleccionada
		Mat image = Imgcodecs.imread(rutaImagen);

		// Convertir a escala de grises
		Mat gray = new Mat(image.rows(), image.cols(), CvType.CV_8U);
		for (int row = 0; row < image.rows(); row++) {
			for (int col = 0; col < image.cols(); col++) {
				double[] pixel = image.get(row, col);
				double grayValue = 0.299 * pixel[2] + 0.587 * pixel[1] + 0.114 * pixel[0];
				gray.put(row, col, grayValue);
			}
		}

		// ---------------------------------------------------------------------------------------------------------

		// Calcular umbral de Niblack manualmente
		Mat thresholdNiblack = new Mat(gray.size(), CvType.CV_8UC1);
		for (int row = 0; row < gray.rows(); row++) {
			for (int col = 0; col < gray.cols(); col++) {
				// Calcular la media y la desviacion estandar de la ventana
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
				/////
				/////
				/////
				/// FORMULA NIBLACK
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

		// Calcular el MSE (error cuadr·tico medio)
		/*
		 * El MSE se calcula como el promedio de los cuadrados de las diferencias entre
		 * los valores de pÌxeles correspondientes de las dos im·genes.
		 */
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

		/*
		 * PSNR (RelaciÛn SeÒal-Ruido de Pico, por sus siglas en inglÈs) es una medida
		 * de la calidad de la reconstrucciÛn de im·genes que han sido comprimidas. Se
		 * calcula comparando la imagen original con la imagen reconstruida y se mide la
		 * cantidad de ruido introducido durante el proceso de compresiÛn. Un valor m·s
		 * alto de PSNR indica una mejor calidad de la imagen reconstruida.
		 * 
		 * PSNR se calcula utilizando el error cuadr·tico medio (MSE, por sus siglas en
		 * inglÈs) entre la imagen original y la imagen reconstruida. Una vez que se ha
		 * calculado el MSE, se puede calcular el PSNR (en dB) utilizando la fÛrmula:
		 * PSNR = 20 * log10(MAX / sqrt(MSE)), donde MAX es el valor m·ximo posible del
		 * pÌxel en la imagen (255) y sqrt(MSE) es la raÌz cuadrada del MSE 1.
		 */

		// Calcular el PSNR - (Entre mas alto es este valor mejor fue la reconstrucciÛn
		// de la imagen)
		double psnr_Niblack = 20 * Math.log10(255.0 / Math.sqrt(mse));

		System.out.println("PSNR_Niblack: " + psnr_Niblack);

		// ---------------------------------------------------------------------------------------------------------

		// Aplicar tecnica Sauvola manualmente
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

				//// FORMULA SAUVOLA
				// double thresholdValue = mean * (1 + k * (Math.sqrt(variance / 128) - 1));
				double desv = Math.sqrt(variance);
				double thresholdValue = mean * (1 - k * (1 - (desv / 128)));
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

		// ---------------------------------------------------------------------------------------------------------

		// Seleccionar la carpeta destino para guardar la imagen transformada
		CarpetaDestino carpetaDestino = new CarpetaDestino();

		String rutaCarpetaDestino = carpetaDestino.selectCarpet();

		Imgcodecs.imwrite(rutaCarpetaDestino + "/ResultadoSauvola.jpg", sauvola);
		Imgcodecs.imwrite(rutaCarpetaDestino + "/ResultadoNiblack.jpg", thresholdNiblack);

		// Calcular el Coeficiente de Jaccard para comparar las im·genes resultantes
		double jaccardSauvola = jaccard(gray, sauvola);
		double jaccardNiblack = jaccard(gray, thresholdNiblack);

		int[] histogramSauvola = new int[256];
		for (int i = 0; i < sauvola.rows(); i++) {
			for (int j = 0; j < sauvola.cols(); j++) {
				int value = (int) sauvola.get(i, j)[0];
				histogramSauvola[value]++;
			}
		}

		int[] histogramNiblack = new int[256];
		for (int i = 0; i < thresholdNiblack.rows(); i++) {
			for (int j = 0; j < thresholdNiblack.cols(); j++) {
				int value = (int) thresholdNiblack.get(i, j)[0];
				histogramNiblack[value]++;
			}
		}

		try {
			FileWriter writer = new FileWriter(rutaCarpetaDestino + "/Histograma_Jaccard_PSNR.csv");
			writer.write("0,255,Jaccard,PSNR");
			writer.write("\n");
			writer.write(String.valueOf(histogramSauvola[0] + "," + String.valueOf(histogramSauvola[255]) + ","
					+ jaccardSauvola + "," + psnr_Sauvola + ",Sauvola"));
			writer.write("\n");
			writer.write(String.valueOf(histogramNiblack[0] + "," + String.valueOf(histogramNiblack[255]) + ","
					+ jaccardNiblack + "," + psnr_Niblack + ",Niblack"));
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Guardar matriz de imagen Sauvola
		try {
			FileWriter writer = new FileWriter(rutaCarpetaDestino + "/matrizImagenSauola.csv");

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

		// Guardar matriz de imagen Niblack
		try {
			FileWriter writer = new FileWriter(rutaCarpetaDestino + "/matrizImagenNiblack.csv");

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

	// Calculo de similitud mediante jaccard (el maximo valor de similitud es 1 y el
	// minimo es 0) la relaciÛn de jaccard sse basa en revisar la cantidad total de
	// pixeles de un tipo y ver cuales de esos pixeles se intersectan entre si, una
	// vez se obtienen esos valores, se dividen las intersecciones entre los pixeles
	// encontrados y ese ess su nivel de relaciÛn
	public static double jaccard(Mat img1, Mat img2) {
		int intersection = 0;
		int union = 0;

		for (int row = 0; row < img1.rows(); row++) {
			for (int col = 0; col < img1.cols(); col++) {
				double[] pixel1 = img1.get(row, col);
				double[] pixel2 = img2.get(row, col);

				if (pixel1[0] == 0 && pixel2[0] == 0) {
					intersection++;
					union++;
				} else if (pixel1[0] == 0 || pixel2[0] == 0) {
					union++;
				}
			}
		}

		double div = (double) intersection / (double) union;

		return div;
	}
}