package Portafolio;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.jfree.data.category.DefaultCategoryDataset;
import org.opencv.core.Core;

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

		// --------------------------------------------------------------------------------------------------------------------

		// Numero de filas y columnas en la matriz de bordes
		int rows = matBordes.size();
		int cols = matBordes.get(0).size();

		// Rango de valores para rho y theta
		double rhoMin = 0;
		double rhoMax = Math.hypot(rows, cols);
		double thetaMin = 0;
		double thetaMax = Math.PI;

		// Numero de contenedores para rho y theta
		int rhoBins = 200;
		int thetaBins = 200;

		// Tama�o de los contenedores para rho y theta
		double rhoStep = (rhoMax - rhoMin) / rhoBins;
		double thetaStep = (thetaMax - thetaMin) / thetaBins;

		// Crear el acumulador y llenarlo con ceros
		int[][] accumulator = new int[rhoBins][thetaBins];
		for (int i = 0; i < rhoBins; i++) {
			for (int j = 0; j < thetaBins; j++) {
				accumulator[i][j] = 0;
			}
		}

		// Para cada pixel de borde en la matriz de bordes
		for (int y = 0; y < rows; y++) {
			for (int x = 0; x < cols; x++) {
				if (matBordes.get(y).get(x) == 1) {
					// Calcular los valores de rho para cada valor de theta
					for (int i = 0; i < thetaBins; i++) {
						double theta = thetaMin + i * thetaStep;
						double rho = x * Math.cos(theta) + y * Math.sin(theta);
						int j = (int) Math.round((rho - rhoMin) / rhoStep);
						accumulator[j][i]++;
					}
				}
			}
		}

		// Seleccionar la carpeta destino para guardar la imagen transformada
		CarpetaDestino carpetaDestino = new CarpetaDestino();
		String rutaCarpetaDestino = carpetaDestino.selectCarpet();

		// -----------------------------------------------------------------------------------------------------------------------

		System.out.println("");
	}

	public List<List<Integer>> leerMatriz(String rutaArchivo) {
		List<List<Integer>> matBordes = new ArrayList<>();
		try (FileReader fileReader = new FileReader(rutaArchivo);
				CSVParser csvParser = new CSVParser(fileReader, CSVFormat.DEFAULT)) {
			for (CSVRecord csvRecord : csvParser) {
				List<Integer> fila = new ArrayList<>();
				for (String valor : csvRecord) {

					// Los valores se leen automaticamente como double por la libreria ApacheCSV y
					// se reguarda como strinng por eso se hace una doble conversión de string a
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
