package Portafolio;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;


public class DeteccionLineas {


	public static void main(String[] args) {

		// Seleccionar la imagen a transformar
		SeleccionarArchivo archivoSeleccionado = new SeleccionarArchivo();
		String rutaArchivo = archivoSeleccionado.selectFile();

		// Cargar la biblioteca OpenCV
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

		// Lista donde se resguarda lo valores de los pixles  de la matriz de bordess}
		List<List<Integer>> matBordes = new ArrayList<>();
		
		DeteccionLineas detec = new DeteccionLineas();
		matBordes = detec.leerMatriz(rutaArchivo);
		
		
		
		
		System.out.println("");
	}
	
	public List<List<Integer>> leerMatriz(String rutaArchivo){
		List<List<Integer>> matBordes = new ArrayList<>();
		try (FileReader fileReader = new FileReader(rutaArchivo);
                CSVParser csvParser = new CSVParser(fileReader, CSVFormat.DEFAULT)) {
            for (CSVRecord csvRecord : csvParser) {
                List<Integer> fila = new ArrayList<>();
                for (String valor : csvRecord) {
                	
                	// Los valores se leen automaticamente como double por la libreria ApacheCSV y se reguarda como strinng por eso se hace una doble conversión de string a double y de double a integer
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
