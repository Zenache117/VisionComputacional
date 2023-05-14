package Portafolio;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.opencv.core.Core;
import org.opencv.core.CvType;

import java.io.FileWriter;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

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

		int[][] accumulator = detec.houghTransform(matBordes);

		// Seleccionar la carpeta destino para guardar la imagen transformada
		CarpetaDestino carpetaDestino = new CarpetaDestino();
		String rutaCarpetaDestino = carpetaDestino.selectCarpet();

		// Guardar matriz de imagen copiada
		try {
			FileWriter writer = new FileWriter(rutaCarpetaDestino + "/TransformadaHough.csv");

			for (int i = 0; i < matBordes.size(); i++) {
				for (int j = 0; j < matBordes.get(0).size(); j++) {
					int value = accumulator[i][j];
					writer.write(String.valueOf(value) + ",");
				}
				writer.write("\n");
			}

			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		detec.mostrarImagenUmbral(80, accumulator, rutaCarpetaDestino);

		// -----------------------------------------------------------------------------------------------------------------------

		System.out.println("");
	}

	public void mostrarImagenUmbral(int Umbral, int[][] accumulator, String rutaCarpetaDestino) {
		// Crear una matriz en blanco y negro con las mismas dimensiones que el
		// acumulador
		Mat imagen = new Mat(accumulator.length, accumulator[0].length, CvType.CV_8UC1);

		// Recorrer cada píxel de la imagen
		for (int x = 0; x < accumulator.length; x++) {
			for (int y = 0; y < accumulator[0].length; y++) {
				// Si el valor del acumulador en la posición (x,y) es mayor que el umbral
				if (accumulator[x][y] > Umbral) {
					// Dibujar el píxel en negro
					imagen.put(x, y, 0);
				} else {
					imagen.put(x, y, 255);
				}
			}
		}

		// Guardar la imagen en un archivo
		Imgcodecs.imwrite(rutaCarpetaDestino + "/imagenFuncionDeUmbral.png", imagen);
	}

	public int[][] houghTransform(List<List<Integer>> matBordes) {
		int rows = matBordes.size();
		int cols = matBordes.get(0).size();
		int[][] votes = new int[rows][cols];
		double angleTolerance = 80.0; // Angle tolerance in degrees

		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				if (matBordes.get(i).get(j) == 0) {
					// Check horizontal line
					for (int k = j + 1; k < cols && matBordes.get(i).get(k) == 0; k++) {
						votes[i][j]++;
						votes[i][k]++;
					}
					// Check vertical line
					for (int k = i + 1; k < rows && matBordes.get(k).get(j) == 0; k++) {
						votes[i][j]++;
						votes[k][j]++;
					}
					// Check diagonal line (left to right)u
					double initialAngle = Math.atan2(1, 1) * 180 / Math.PI;
					for (int k = 1; i + k < rows && j + k < cols && matBordes.get(i + k).get(j + k) == 0; k++) {
						double angle = Math.atan2(k, k) * 180 / Math.PI;
						if (Math.abs(angle - initialAngle) <= angleTolerance) {
							votes[i][j]++;
							votes[i + k][j + k]++;
						}
					}
					// Check diagonal line (right to left)
					initialAngle = Math.atan2(1, -1) * 180 / Math.PI;
					for (int k = 1; i + k < rows && j - k >= 0 && matBordes.get(i + k).get(j - k) == 0; k++) {
						double angle = Math.atan2(k, -k) * 180 / Math.PI;
						if (Math.abs(angle - initialAngle) <= angleTolerance) {
							votes[i][j]++;
							votes[i + k][j - k]++;
						}
					}
				}
			}
		}

		return votes;
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
