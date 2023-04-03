package Portafolio;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
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

		List<List<List<Vecinos>>> vecindariosLeidos = new ArrayList<>();
		try {
			Reader reader = Files.newBufferedReader(Paths.get(rutaArchivo));
			CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader());

			for (CSVRecord record : csvParser) {
				int i = Integer.parseInt(record.get("Fila"));
				int j = Integer.parseInt(record.get("Columna"));
				int vecindario = Integer.parseInt(record.get("Vecindario"));
				int valor = Integer.parseInt(record.get("Valor"));

				// Crear un nuevo objeto Vecinos y agregarlo a la lista correspondiente
				Vecinos vecino = new Vecinos();
				vecino.setI(i);
				vecino.setJ(j);
				vecino.setVecindario(vecindario);
				vecino.setValor(valor);

				if (vecindariosLeidos.size() <= vecindario) {
					// Agregar una nueva lista de vecinos para este vecindario si no existe aún
					vecindariosLeidos.add(new ArrayList<>());
				}
				if (vecindariosLeidos.get(vecindario).size() <= i) {
					// Agregar una nueva lista de vecinos para esta fila si no existe aún
					vecindariosLeidos.get(vecindario).add(new ArrayList<>());
				}
				
				vecindariosLeidos.get(vecindario).get(i).add(vecino);
				
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		// Iterar a través de la lista de vecindarios
		for (List<List<Vecinos>> vecindario : vecindariosLeidos) {
			// Iterar a través de cada vecindario en la lista
			for (List<Vecinos> veci : vecindario) {
				// Crear una lista para almacenar los valores de los vecinos en el vecindario
				List<Integer> valores = new ArrayList<>();
				// Iterar a través de cada vecino en el vecindario y agregar su valor a la lista
				// de valores
				for (Vecinos vec : veci) {
					valores.add(vec.getValor());
				}
				// Calcular la media de los valores en la lista
				double media = valores.stream().mapToInt(val -> val).average().orElse(0.0);
				// Asignar el valor de la media a cada vecino en el vecindario
				for (Vecinos vec : veci) {
					vec.setValor((int) media);
				}
			}
		}

		// Obtener los valores máximos de i y j de los Vecinos en la lista (Para definir el tamaño de la imagen)
		int maxI = -1;
		int maxJ = -1;
		for (List<List<Vecinos>> vecindario : vecindariosLeidos) {
		    for (List<Vecinos> veci : vecindario) {
		        for (Vecinos vec : veci) {
		            maxI = Math.max(maxI, vec.getI());
		            maxJ = Math.max(maxJ, vec.getJ());
		        }
		    }
		}

		// Crear la imagen en escala de grises con el tamaño correspondiente
		Mat img = new Mat(maxI+1, maxJ+1, CvType.CV_8UC1);
		for (int k = 0; k < vecindariosLeidos.size(); k++) {
		    List<List<Vecinos>> vecindario = vecindariosLeidos.get(k);
		    for (List<Vecinos> fila : vecindario) {
		        for (Vecinos vecino : fila) {
		            int i = vecino.getI();
		            int j = vecino.getJ();
		            int valor = vecino.getValor();
		            img.put(i, j, valor);
		        }
		    }
		}

		// Seleccionar la carpeta destino para guardar la imagen transformada
		CarpetaDestino carpetaDestino = new CarpetaDestino();

		String rutaCarpetaDestino = carpetaDestino.selectCarpet();

		// Guardar la imagen en la carpeta destino
		String rutaImagenDestino = rutaCarpetaDestino + File.separator + "FiltroMedia.png";
		Imgcodecs.imwrite(rutaImagenDestino, img);

		FileWriter writer;
		try {
			writer = new FileWriter(rutaCarpetaDestino + "/FiltroMedianaMatriz.csv");

			for (int i = 0; i < img.rows(); i++) {
				for (int j = 0; j < img.cols(); j++) {
					double[] value = img.get(i, j);
					writer.write(String.valueOf(value[0]) + ",");
				}
				writer.write("\n");
			}

			writer.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
