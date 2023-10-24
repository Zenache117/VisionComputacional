package PIA;

import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JOptionPane;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import java.util.List;
import java.util.ArrayList;

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

Luego, se aplica un método de umbralización llamado "Niblack" para binarizar la imagen. Este método utiliza una ventana de píxeles en cada píxel de la imagen para calcular el umbral local. El umbral local se calcula como la media de la 
ventana menos un factor multiplicado por la desviación estándar de la ventana. El factor utilizado es -0.2 ("k" establecido por el documento del PIA). Los píxeles con un valor superior al umbral se asignan a 255 (blanco) y los píxeles con un valor inferior se asignan a 0 (negro). 
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

		// Convertir a escala de grises
		Mat gray = new Mat(image.rows(), image.cols(), CvType.CV_8U);
		for (int row = 0; row < image.rows(); row++) {
			for (int col = 0; col < image.cols(); col++) {
				double[] pixel = image.get(row, col);
				int grayValue = (int) (0.299 * pixel[2] + 0.587 * pixel[1] + 0.114 * pixel[0]);
				gray.put(row, col, grayValue);
			}
		}

		List<Double> PSNRNiblack = new ArrayList<>();
		List<Double> PSNRdSauvola = new ArrayList<>();
		List<Double> jaccardNiblack = new ArrayList<>();
		List<Double> jaccardSauvola = new ArrayList<>();
		List<Integer> UmbralSauvola = new ArrayList<>();
		List<Integer> UmbralNiblack = new ArrayList<>();

		// ---------------------------------------------------------------------------------------------------------

		// Calcular el umbral de Niblack
		/*
		 * double kniblack = 2.0; while (kniblack < 0.0 || kniblack > 1.0) { kniblack =
		 * Double.parseDouble(JOptionPane.
		 * showInputDialog("Valor de k para calculo de umbral de niblack")); if
		 * (kniblack < 0.0 || kniblack > 1.0) { JOptionPane.showMessageDialog(null,
		 * "El valor de k debe estar entre 0.0 y 1.0"); } }
		 */
		// Calcular umbral de Niblack manualmente
		double kniblack = 0.0;

		List<Mat> imageNiblack = new ArrayList<>();

		for (int w = 0; w < 10; w++) {
			kniblack += 0.10;
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

					/////
					/////
					/////
					/// FORMULA NIBLACK
					int thresholdValue = (int) (mean + (kniblack * stdDev));
					double pixelValue = gray.get(row, col)[0];
					if (pixelValue > thresholdValue) {
						thresholdNiblack.put(row, col, 255);
					} else {
						thresholdNiblack.put(row, col, 0);
					}
				}
			}
			System.out.println("K=" + kniblack + "\nUmbral de Niblack: " + (int) Core.mean(thresholdNiblack).val[0]);

			UmbralNiblack.add((int) Core.mean(thresholdNiblack).val[0]);

			// Calcular el MSE (error cuadr�tico medio)
			/*
			 * El MSE se calcula como el promedio de los cuadrados de las diferencias entre
			 * los valores de p�xeles correspondientes de las dos im�genes.
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
			 * PSNR (Relaci�n Se�al-Ruido de Pico, por sus siglas en ingl�s) es una medida
			 * de la calidad de la reconstrucci�n de im�genes que han sido comprimidas. Se
			 * calcula comparando la imagen original con la imagen reconstruida y se mide la
			 * cantidad de ruido introducido durante el proceso de compresi�n. Un valor m�s
			 * alto de PSNR indica una mejor calidad de la imagen reconstruida.
			 * 
			 * PSNR se calcula utilizando el error cuadr�tico medio (MSE, por sus siglas en
			 * ingl�s) entre la imagen original y la imagen reconstruida. Una vez que se ha
			 * calculado el MSE, se puede calcular el PSNR (en dB) utilizando la f�rmula:
			 * PSNR = 20 * log10(MAX / sqrt(MSE)), donde MAX es el valor m�ximo posible del
			 * p�xel en la imagen (255) y sqrt(MSE) es la ra�z cuadrada del MSE 1.
			 */

			// Calcular el PSNR - (Entre mas alto es este valor mejor fue la reconstrucci�n
			// de la imagen)
			double psnr_Niblack = 20 * Math.log10(255.0 / Math.sqrt(mse));

			PSNRNiblack.add(psnr_Niblack);

			System.out.println("PSNR_Niblack: " + psnr_Niblack);
			imageNiblack.add(thresholdNiblack);
		}

		// ---------------------------------------------------------------------------------------------------------
		double ksauvola = 0.0;
		List<Mat> imageSauvola = new ArrayList<>();

		for (int w = 0; w < 10; w++) {
			ksauvola += 0.10;
			// Aplicar tecnica Sauvola manualmente
			Mat sauvola = new Mat(gray.rows(), gray.cols(), CvType.CV_8UC1);
			int windowSize = 25;
			// Mismo valor de K utilizado en el articulo

			while (ksauvola < 0.0 || ksauvola > 1.0) {
				ksauvola = Double
						.parseDouble(JOptionPane.showInputDialog("Valor de k para calculo de umbral de sauvola"));
				if (kniblack < 0.0 || kniblack > 1.0) {
					JOptionPane.showMessageDialog(null, "El valor de k debe estar entre 0.0 y 1.0");
				}
			}
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
					int thresholdValue = (int) (mean * (1 - ksauvola * (1 - (desv / 128))));
					if (pixel[0] > thresholdValue) {
						sauvola.put(row, col, 255);
					} else {
						sauvola.put(row, col, 0);
					}
				}
			}
			System.out.println("K=" + ksauvola + "\nUmbral de Sauvola: " + (int) Core.mean(sauvola).val[0]);

			UmbralSauvola.add((int) Core.mean(sauvola).val[0]);

			double mse = 0;
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

			PSNRdSauvola.add(psnr_Sauvola);

			System.out.println("PSNR_Sauvola: " + psnr_Sauvola);

			imageSauvola.add(sauvola);
		}

		// ---------------------------------------------------------------------------------------------------------

		// Seleccionar la carpeta destino para guardar la imagen transformada
		CarpetaDestino carpetaDestino = new CarpetaDestino();

		String rutaCarpetaDestino = carpetaDestino.selectCarpet();

		Imgcodecs.imwrite(rutaCarpetaDestino + "/ImagenEnGrises.jpg", gray);

		int posicion = 0;
		for (Mat imagen : imageNiblack) {
			posicion++;
			Imgcodecs.imwrite(rutaCarpetaDestino + "/ResultadoNiblack" + posicion + ".jpg", imagen);
		}
		posicion = 0;
		for (Mat imagen : imageSauvola) {
			posicion++;
			Imgcodecs.imwrite(rutaCarpetaDestino + "/ResultadoSauvola" + posicion + ".jpg", imagen);
		}

		for (int w = 0; w < 10; w++) {
			double jaccardS = jaccard(gray, imageSauvola.get(w));
			double jaccardN = jaccard(gray, imageNiblack.get(w));
			jaccardSauvola.add(jaccardS);
			jaccardNiblack.add(jaccardN);
		}

		List<String> HISTSA = new ArrayList<>();
		List<String> HISTNI = new ArrayList<>();

		for (int w = 0; w < 10; w++) {
			int[] histogramSauvola = new int[256];
			for (int i = 0; i < imageSauvola.get(w).rows(); i++) {
				for (int j = 0; j < imageSauvola.get(w).cols(); j++) {
					int value = (int) imageSauvola.get(w).get(i, j)[0];
					histogramSauvola[value]++;
				}
			}

			int[] histogramNiblack = new int[256];
			for (int i = 0; i < imageNiblack.get(w).rows(); i++) {
				for (int j = 0; j < imageNiblack.get(w).cols(); j++) {
					int value = (int) imageNiblack.get(w).get(i, j)[0];
					histogramNiblack[value]++;
				}
			}

			String valores = histogramSauvola[0] + "," + histogramSauvola[255];

			HISTNI.add(valores);
			valores = histogramNiblack[0] + "," + histogramNiblack[255];
			HISTSA.add(valores);
		}

		// Histograma imagen en grises
		int[] histogramGray = new int[256];
		for (int i = 0; i < gray.rows(); i++) {
			for (int j = 0; j < gray.cols(); j++) {
				int value = (int) gray.get(i, j)[0];
				histogramGray[value]++;
			}
		}

		// Conjunto de resultados
		try {
			FileWriter writer = new FileWriter(rutaCarpetaDestino + "/Histograma_Jaccard_PSNR.csv");
			writer.write("Pixeles valor 0,Pixeles valor 255,Jaccard,PSNR,Umbral,K");

			double contadorK = 0.0;
			for (int w = 0; w < 10; w++) {
				contadorK += 0.1;

				writer.write("\n");
				writer.write(HISTNI.get(w) + "," + jaccardSauvola.get(w) + "," + PSNRdSauvola.get(w) + ","
						+ UmbralSauvola.get(w) + "," + contadorK + ",Sauvola");
			}

			writer.write("\n");

			contadorK = 0.0;
			for (int w = 0; w < 10; w++) {
				contadorK += 0.1;

				writer.write("\n");
				writer.write(HISTSA.get(w) + "," + jaccardNiblack.get(w) + "," + PSNRNiblack.get(w) + ","
						+ UmbralNiblack.get(w) + "," + contadorK + ",Niblack");
			}
			writer.write("\n");
			writer.write("\n");
			for (int w = 0; w < 256; w++) {
				writer.write(w + ",");

			}
			writer.write("\n");
			for (int w = 0; w < 256; w++) {
				writer.write(String.valueOf(histogramGray[w]) + ",");
			}

			writer.write("ImagenEnGrises");
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Matriz Escala de grises
		try {
			FileWriter writer = new FileWriter(rutaCarpetaDestino + "/matrizGrises.csv");

			for (int i = 0; i < gray.rows(); i++) {
				for (int j = 0; j < gray.cols(); j++) {
					double[] value = gray.get(i, j);
					writer.write(String.valueOf(value[0]));
					if (j < gray.cols() - 1) {
						writer.write(",");
					}
				}
				writer.write("\n");
			}

			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		for (int w = 0; w < 10; w++) {
			// Guardar matriz de imagen Sauvola
			try {
				FileWriter writer = new FileWriter(rutaCarpetaDestino + "/matrizImagenSauvola" + w + ".csv");

				for (int i = 0; i < imageSauvola.get(w).rows(); i++) {
					for (int j = 0; j < imageSauvola.get(w).cols(); j++) {
						double[] value = imageSauvola.get(w).get(i, j);
						writer.write(String.valueOf(value[0]));
						if (j < imageSauvola.get(w).cols() - 1) {
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
				FileWriter writer = new FileWriter(rutaCarpetaDestino + "/matrizImagenNiblack" + w + ".csv");

				for (int i = 0; i < imageNiblack.get(w).rows(); i++) {
					for (int j = 0; j < imageNiblack.get(w).cols(); j++) {
						double[] value = imageNiblack.get(w).get(i, j);
						writer.write(String.valueOf(value[0]));
						if (j < imageNiblack.get(w).cols() - 1) {
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

	// Calculo de similitud mediante jaccard (el maximo valor de similitud es 1 y el
	// minimo es 0) la relaci�n de jaccard sse basa en revisar la cantidad total de
	// pixeles de un tipo y ver cuales de esos pixeles se intersectan entre si, una
	// vez se obtienen esos valores, se dividen las intersecciones entre los pixeles
	// encontrados y ese ess su nivel de relaci�n
	public static double jaccard(Mat img1, Mat img2) {
		int intersection = 0;
		int union = 0;

		for (int row = 0; row < img1.rows(); row++) {
			for (int col = 0; col < img1.cols(); col++) {
				double[] pixel1 = img1.get(row, col);
				double[] pixel2 = img2.get(row, col);

				if (pixel1[0] == 255 && pixel2[0] == 255) {
					intersection++;
					union++;
				} else if (pixel1[0] == 255 || pixel2[0] == 255) {
					union++;
				}
			}
		}

		double div = (double) intersection / (double) union;

		return div;
	}
}