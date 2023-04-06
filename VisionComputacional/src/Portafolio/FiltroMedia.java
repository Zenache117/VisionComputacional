package Portafolio;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

public class FiltroMedia {

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
		
		FiltroMedia calulo = new FiltroMedia();
		calulo.CalcularMedia(pixels, vecindarios);
		
		// Seleccionar la carpeta destino para guardar la imagen transformada
		CarpetaDestino carpetaDestino = new CarpetaDestino();
		String rutaCarpetaDestino = carpetaDestino.selectCarpet();

		// Guardar la imagen transformada en la carpeta seleccionada
		Imgcodecs.imwrite(rutaCarpetaDestino + "./FiltroMedia.jpg", pixels);

		// Guardar matriz de imagen
		FileWriter writer;
		try {
			writer = new FileWriter(rutaCarpetaDestino + "/ImagenMatrizFiltroMedia.csv");

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
	
	public void CalcularMedia(Mat pixels, List<List<Vecinos>> vecindarios) {
		// Iterar sobre los píxeles de la imagen y aplicar el filtro de media manualmente
        for (List<Vecinos> vecindario : vecindarios) {
            for (Vecinos vecino : vecindario) {
                int i = vecino.getI();
                int j = vecino.getJ();
                int valor = vecino.getValor();

                // Calcular la media de los valores de los píxeles vecinos
                int suma = valor;
                int numVecinos = 1;
                for (int ii = i-1; ii <= i+1; ii++) {
                    for (int jj = j-1; jj <= j+1; jj++) {
                        if (ii >= 0 && jj >= 0 && ii < pixels.rows() && jj < pixels.cols() && !(ii == i && jj == j)) {
                            suma += pixels.get(ii, jj)[0];
                            numVecinos++;
                        }
                    }
                }
                int media = suma / numVecinos;

                // Asignar el valor de la media al píxel de la imagen
                pixels.put(i, j, media);
            }
        }
	}
	
}
