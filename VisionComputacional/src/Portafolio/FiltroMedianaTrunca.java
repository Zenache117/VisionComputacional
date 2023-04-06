package Portafolio;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JOptionPane;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

public class FiltroMedianaTrunca {
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

		FiltroMedianaTrunca calulo = new FiltroMedianaTrunca();
		calulo.CalcularMedianaTrunca(pixels, vecindarios);

		// Seleccionar la carpeta destino para guardar la imagen transformada
		CarpetaDestino carpetaDestino = new CarpetaDestino();
		String rutaCarpetaDestino = carpetaDestino.selectCarpet();

		// Guardar la imagen transformada en la carpeta seleccionada
		Imgcodecs.imwrite(rutaCarpetaDestino + "./FiltroMedianaTrunca.jpg", pixels);

		// Guardar matriz de imagen
		FileWriter writer;
		try {
			writer = new FileWriter(rutaCarpetaDestino + "/ImagenMatrizFiltroMedianaTrunca.csv");

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
	
	public void CalcularMedianaTrunca(Mat pixels, List<List<Vecinos>> vecindarios) {
		int t = Integer.parseInt(JOptionPane.showInputDialog("Dame un valor t para eliminar valores mas pequeños o grandes"));
	    // Iterar sobre los píxeles de la imagen y aplicar el filtro de mediana trunca manualmente
	    for (List<Vecinos> vecindario : vecindarios) {
	        int size = vecindario.size();
	        int[] valores = new int[size];
	        int idx = 0;
	        for (Vecinos vecino : vecindario) {
	            valores[idx++] = vecino.getValor();
	        }
	        Arrays.sort(valores);

	        int n = size - (2 * t);
	        if (n <= 0) {
	            // Si no quedan suficientes valores después de truncar, se calcula la mediana simple
	            int mediana = size % 2 == 0 ? (valores[size/2-1] + valores[size/2]) / 2 : valores[size/2];
	            int i = vecindario.get(0).getI();
	            int j = vecindario.get(0).getJ();
	            pixels.put(i, j, mediana);
	        } else {
	            // Si quedan suficientes valores, se calcula la mediana con los valores restantes
	            int nuevaLongitud = size - (2 * t);
	            int mediana;
	            if (nuevaLongitud % 2 == 0) {
	                mediana = (valores[t + (nuevaLongitud/2)-1] + valores[t + (nuevaLongitud/2)]) / 2;
	            } else {
	                mediana = valores[t + (nuevaLongitud/2)];
	            }
	            int i = vecindario.get(0).getI();
	            int j = vecindario.get(0).getJ();
	            pixels.put(i, j, mediana);
	        }
	    }
	}

	
}
