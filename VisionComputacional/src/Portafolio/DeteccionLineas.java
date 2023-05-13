package Portafolio;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.opencv.core.Core;
import java.io.FileWriter;





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

		//Guardar matriz de imagen copiada
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
		
	
		

		// -----------------------------------------------------------------------------------------------------------------------

		System.out.println("");
	}

	public int[][] houghTransform(List<List<Integer>> matBordes) {
		int rows = matBordes.size();
		int cols = matBordes.get(0).size();
		int[][] votingSpace = new int[rows][cols];

		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				if (matBordes.get(i).get(j) == 0) { // pixel borde
					votingSpace[i][j]++;
					// verificar píxeles adyacentes
					for (int x = -1; x <= 1; x++) {
						for (int y = -1; y <= 1; y++) {
							if (x == 0 && y == 0)
								continue;
							int newX = i + x;
							int newY = j + y;
							if (newX >= 0 && newX < rows && newY >= 0 && newY < cols
									&& matBordes.get(newX).get(newY) == 0) {
								votingSpace[newX][newY]++;
								// verificar píxeles consecutivos en la misma dirección
								int nextX = newX + x;
								int nextY = newY + y;
								while (nextX >= 0 && nextX < rows && nextY >= 0 && nextY < cols
										&& matBordes.get(nextX).get(nextY) == 0) {
									votingSpace[nextX][nextY]++;
									nextX += x;
									nextY += y;
								}
							}
						}
					}
				}
			}
		}

		return votingSpace;
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
