package Portafolio;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.opencv.core.Core;
import org.opencv.core.CvType;

import java.io.FileWriter;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class DeteccionLineas {

	public static void main(String[] args) {

		// Seleccionar la imagen a transformar
		SeleccionarArchivo archivoSeleccionado = new SeleccionarArchivo();
		String rutaArchivo = archivoSeleccionado.selectFile();

		// Cargar la biblioteca OpenCV
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

		// Lista donde se resguarda lo valores de los pixles de la matriz de bordess}
		List<List<Integer>> matBordes = new ArrayList<>();

		DeteccionLineas detec = new DeteccionLineas();
		matBordes = detec.leerMatriz(rutaArchivo);

		int width = matBordes.get(0).size();
		int height = matBordes.size();
		int maxRho = (int) Math.sqrt(width * width + height * height);
		int rhoRange = maxRho * 2;
		int thetaRange = 180;
		int[][] accumulator = new int[rhoRange][thetaRange];

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if (matBordes.get(y).get(x) == 255)
					continue;
				for (int theta = 0; theta < thetaRange; theta++) {
					double rad = Math.toRadians(theta);
					int rho = (int) (x * Math.cos(rad) + y * Math.sin(rad)) + maxRho;
					accumulator[rho][theta]++;
				}
			}
		}

		int threshold = 100;
		List<int[]> peaks = new ArrayList<>();
		for (int rhoIndex = 0; rhoIndex < rhoRange; rhoIndex++) {
			for (int thetaIndex = 0; thetaIndex < thetaRange; thetaIndex++) {
				if (accumulator[rhoIndex][thetaIndex] > threshold) {
					peaks.add(new int[] { rhoIndex, thetaIndex });
				}
			}
		}

		Mat image = Mat.zeros(height, width, CvType.CV_8UC3);
		image.setTo(new Scalar(255, 255, 255));
		for (int[] peak : peaks) {
			int rhoIndex = peak[0];
			int thetaIndex = peak[1];
			int rho = rhoIndex - maxRho;
			double theta = Math.toRadians(thetaIndex);
			double cosTheta = Math.cos(theta);
			double sinTheta = Math.sin(theta);
			double x0 = cosTheta * rho;
			double y0 = sinTheta * rho;
			int[] pt1 = new int[] { (int) Math.round(x0 + 1000 * (-sinTheta)), (int) Math.round(y0 + 1000 * cosTheta) };
			int[] pt2 = new int[] { (int) Math.round(x0 - 1000 * (-sinTheta)), (int) Math.round(y0 - 1000 * cosTheta) };
			Imgproc.line(image, new Point(pt1[0], pt1[1]), new Point(pt2[0], pt2[1]), new Scalar(0, 0, 0), 1);
		}

		// Tranforma la imagen a lista
		List<List<Integer>> imageList = new ArrayList<>();
		for (int y = 0; y < image.rows(); y++) {
			List<Integer> row = new ArrayList<>();
			for (int x = 0; x < image.cols(); x++) {
				double[] pixel = image.get(y, x);
				int value = (int) pixel[0];
				row.add(value);
			}
			imageList.add(row);
		}

		// Se dibujan las lineas solo en donde el pixel tiene valor 0 en la imagen de
		// los bordes para evitar que se proyecten lineas fuera de estos
		List<List<Integer>> result = new ArrayList<>();
		for (int y = 0; y < height; y++) {
			List<Integer> row = new ArrayList<>();
			for (int x = 0; x < width; x++) {
				int value1 = matBordes.get(y).get(x);
				int value2 = imageList.get(y).get(x);
				int value;
				if (value1 == 0 && value2 == 0) {
					value = 0;
				} else {
					value = 255;
				}
				row.add(value);
			}
			result.add(row);
		}

		// Se genera una imagen resultado para Hough
		Mat resultImage = Mat.zeros(height, width, CvType.CV_8UC1);
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int value = result.get(y).get(x);
				resultImage.put(y, x, value);
			}
		}

		// Seleccionar la carpeta destino para guardar la imagen transformada
		CarpetaDestino carpetaDestino = new CarpetaDestino();
		String rutaCarpetaDestino = carpetaDestino.selectCarpet();

		Imgcodecs.imwrite(rutaCarpetaDestino + "/imagenTransformadaHough.png", image);
		Imgcodecs.imwrite(rutaCarpetaDestino + "/imagenFuncionDeUmbral.png", resultImage);

		// Guardar matriz de imagenFunción umbral
		FileWriter writer;
		try {
			writer = new FileWriter(rutaCarpetaDestino + "/matrizImagenFuncionUmbral.csv");

			for (int i = 0; i < resultImage.rows(); i++) {
				for (int j = 0; j < resultImage.cols(); j++) {
					double[] value = resultImage.get(i, j);
					writer.write(String.valueOf(value[0]) + ",");
				}
				writer.write("\n");
			}

			writer.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

		// -----------------------------------------------------------------------------------------------------------------------

		// Parámetros del algoritmo RANSAC
		int numIterations = 1000;
		double threshold2 = 2.0;
		int minNumInliers = 100;
		int numLines = 11;

		// Lista de puntos de borde
		List<Point> edgePoints = new ArrayList<Point>();
		for (int y = 0; y < resultImage.rows(); y++) {
			for (int x = 0; x < resultImage.cols(); x++) {
				double[] pixel = resultImage.get(y, x);
				if (pixel[0] == 0) {
					edgePoints.add(new Point(x, y));
				}
			}
		}

		// Crear una nueva imagen de las mismas dimensiones que resultImage
		Mat newImage = Mat.zeros(resultImage.rows(), resultImage.cols(), CvType.CV_8UC1);
		newImage.setTo(new Scalar(255, 255, 255));

		// Algoritmo RANSAC para detectar múltiples líneas
		for (int l = 0; l < numLines; l++) {
			// Mejor modelo encontrado hasta el momento
			double bestM = 0.0;
			double bestB = 0.0;
			int bestNumInliers = 0;
			List<Point> bestInliers = new ArrayList<Point>();

			for (int i = 0; i < numIterations; i++) {
				// Seleccionar dos puntos aleatorios
				Point p1 = edgePoints.get((int) (Math.random() * edgePoints.size()));
				Point p2 = edgePoints.get((int) (Math.random() * edgePoints.size()));

				// Ajustar el modelo a los puntos seleccionados
				double m = (p2.y - p1.y) / (p2.x - p1.x);
				double b = p1.y - m * p1.x;

				// Contar el número de inliers
				int numInliers = 0;
				List<Point> inliers = new ArrayList<Point>();
				for (Point p : edgePoints) {
					double distance = Math.abs(m * p.x - p.y + b) / Math.sqrt(m * m + 1);
					if (distance < threshold2) {
						numInliers++;
						inliers.add(p);
					}
				}

				// Actualizar el mejor modelo encontrado hasta el momento
				if (numInliers > bestNumInliers && numInliers > minNumInliers) {
					bestM = m;
					bestB = b;
					bestNumInliers = numInliers;
					bestInliers = inliers;
				}
			}

			// Dibujar la línea encontrada en la nueva imagen
			Point pt1 = new Point(0, bestB);
			Point pt2 = new Point(resultImage.cols(), bestM * resultImage.cols() + bestB);
			Imgproc.line(newImage, pt1, pt2, new Scalar(0, 0, 0), 3);

			// Eliminar los inliers encontrados
			edgePoints.removeAll(bestInliers);
		}

		// Tranforma la imagen a lista
		List<List<Integer>> RANSACImageList = new ArrayList<>();
		for (int y = 0; y < newImage.rows(); y++) {
			List<Integer> row = new ArrayList<>();
			for (int x = 0; x < newImage.cols(); x++) {
				double[] pixel = newImage.get(y, x);
				int value = (int) pixel[0];
				row.add(value);
			}
			RANSACImageList.add(row);
		}

		// Tranforma la imagen a lista
		List<List<Integer>> HoughImageList = new ArrayList<>();
		for (int y = 0; y < resultImage.rows(); y++) {
			List<Integer> row = new ArrayList<>();
			for (int x = 0; x < resultImage.cols(); x++) {
				double[] pixel = resultImage.get(y, x);
				int value = (int) pixel[0];
				row.add(value);
			}
			HoughImageList.add(row);
		}

		// Se dibujan las lineas solo en donde el pixel tiene valor 0 en la imagen de
		// los bordes para evitar que se proyecten lineas fuera de estos
		List<List<Integer>> resultRANSAC = new ArrayList<>();
		for (int y = 0; y < height; y++) {
			List<Integer> row = new ArrayList<>();
			for (int x = 0; x < width; x++) {
				int value1 = HoughImageList.get(y).get(x);
				int value2 = RANSACImageList.get(y).get(x);
				int value;
				if (value1 == 0 && value2 == 0) {
					value = 0;
				} else {
					value = 255;
				}
				row.add(value);
			}
			resultRANSAC.add(row);
		}

		// Se genera una imagen resultado para RANSAC
		Mat resultRANSACImage = Mat.zeros(height, width, CvType.CV_8UC1);
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int value = resultRANSAC.get(y).get(x);
				resultRANSACImage.put(y, x, value);
			}
		}

		Imgcodecs.imwrite(rutaCarpetaDestino + "/imagenRANSAC.png", newImage);
		Imgcodecs.imwrite(rutaCarpetaDestino + "/imagenRANSACajustada.png", resultRANSACImage);

		// Guardar matriz de imagenFunción umbral
		try {
			writer = new FileWriter(rutaCarpetaDestino + "/matrizImagenRANSAC.csv");

			for (int i = 0; i < resultImage.rows(); i++) {
				for (int j = 0; j < resultImage.cols(); j++) {
					double[] value = resultImage.get(i, j);
					writer.write(String.valueOf(value[0]) + ",");
				}
				writer.write("\n");
			}

			writer.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public List<List<Integer>> leerMatriz(String rutaArchivo) {
		List<List<Integer>> matBordes = new ArrayList<>();
		try (FileReader fileReader = new FileReader(rutaArchivo);
				CSVParser csvParser = new CSVParser(fileReader, CSVFormat.DEFAULT)) {
			for (CSVRecord csvRecord : csvParser) {
				List<Integer> fila = new ArrayList<>();
				for (String valor : csvRecord) {

					// Los valores se leen automaticamente como double por la libreria ApacheCSV y
					// se reguarda como strinng por eso se hace una doble conversiÃ³n de string a
					// double y de double a integer
					fila.add(Double.valueOf(Double.parseDouble(valor)).intValue());
				}
				matBordes.add(fila);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return matBordes;
	}

}
