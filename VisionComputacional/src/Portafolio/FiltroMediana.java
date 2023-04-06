package Portafolio;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

public class FiltroMediana {

	public static void main(String[] args) {

		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

		// Seleccionar la imagen a transformar
		SeleccionarArchivo archivoSeleccionado = new SeleccionarArchivo();

		String rutaArchivo = archivoSeleccionado.selectFile();

		List<List<Vecinos>> vecindarios = new ArrayList<>();
		List<Vecinos> vecindarioActual = null;
		int vecindarioAnterior = -1;

		try (CSVParser parser = new CSVParser(new FileReader(rutaArchivo), CSVFormat.DEFAULT.withHeader())) {
			for (CSVRecord record : parser) {
				int fila = Integer.parseInt(record.get("Fila"));
				int columna = Integer.parseInt(record.get("Columna"));
				int vecindario = Integer.parseInt(record.get("Vecindario"));
				int valor = Integer.parseInt(record.get("Valor"));

				Vecinos vecinoActual = new Vecinos();
				vecinoActual.setI(fila);
				vecinoActual.setJ(columna);
				vecinoActual.setValor(valor);

				if (vecindario != vecindarioAnterior) {
					vecindarioActual = new ArrayList<>();
					vecindarios.add(vecindarioActual);
					vecindarioAnterior = vecindario;
				}

				vecinoActual.setVecindario(vecindario);
				vecindarioActual.add(vecinoActual);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Encontrar los valores máximos de i y j en la lista
		int maxI = 0;
		int maxJ = 0;
		for (List<Vecinos> vecindario : vecindarios) {
			for (Vecinos vecino : vecindario) {
				if (vecino.getI() > maxI) {
					maxI = vecino.getI();
				}
				if (vecino.getJ() > maxJ) {
					maxJ = vecino.getJ();
				}
			}
		}

		// Crear una matriz de píxeles de tamaño correspondiente
		Mat pixels = new Mat(maxI + 1, maxJ + 1, CvType.CV_8UC1);

		FiltroMediana calulo = new FiltroMediana();
		calulo.CalcularMediana(pixels, vecindarios);

		// Seleccionar la carpeta destino para guardar la imagen transformada
		CarpetaDestino carpetaDestino = new CarpetaDestino();
		String rutaCarpetaDestino = carpetaDestino.selectCarpet();

		// Guardar la imagen transformada en la carpeta seleccionada
		Imgcodecs.imwrite(rutaCarpetaDestino + "./FiltroMediana.jpg", pixels);

		// Guardar matriz de imagen
		FileWriter writer;
		try {
			writer = new FileWriter(rutaCarpetaDestino + "/ImagenMatrizFiltroMediana.csv");

			for (int i = 0; i < pixels.rows(); i++) {
				for (int j = 0; j < pixels.cols(); j++) {
					double[] value = pixels.get(i, j);
					writer.write(String.valueOf(value[0]) + ",");
				}
				writer.write("\n");
			}

			writer.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void CalcularMediana(Mat pixels, List<List<Vecinos>> vecindarios) {
		// Iterar sobre los vecindarios
		for (List<Vecinos> vecindario : vecindarios) {
			// Obtener los valores de los vecinos en el vecindario
			List<Integer> valoresVecinos = new ArrayList<>();
			for (Vecinos vecino : vecindario) {
				valoresVecinos.add(vecino.getValor());
			}

			// Ordenar los valores de los vecinos
			Collections.sort(valoresVecinos);

			// Obtener la mediana de los valores
			int mediana;
			int tam = valoresVecinos.size();
			if (tam % 2 == 0) {
				// Si el tamaño de la lista es par, se toma el promedio de los dos valores del
				// medio
				mediana = (valoresVecinos.get(tam / 2 - 1) + valoresVecinos.get(tam / 2)) / 2;
			} else {
				// Si el tamaño de la lista es impar, se toma el valor del medio
				mediana = valoresVecinos.get(tam / 2);
			}

			// Asignar el valor de la mediana al píxel correspondiente en la matriz
			int i = vecindario.get(0).getI();
			int j = vecindario.get(0).getJ();
			pixels.put(i, j, mediana);
		}
	}
}
