package Portafolio;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

public class BordesDeVecindario {
	public void binarizarBordes(int width, int height, List<List<List<Vecinos>>> vecindarios, String rutaCarpetaDestino) {

		Mat imagenBinarizada = Mat.zeros(height, width, CvType.CV_8UC1);

		for (int i = 0; i < vecindarios.size(); i++) {
			for (int j = 0; j < vecindarios.get(i).size(); j++) {
				List<Vecinos> vecindario = vecindarios.get(i).get(j);
				for (Vecinos pixel : vecindario) {
					if (pixel.isBorde()) {
						imagenBinarizada.put(pixel.getI(), pixel.getJ(), 0); // Establecer el valor del píxel en 255 si
						// es borde
					} else {
						imagenBinarizada.put(pixel.getI(), pixel.getJ(), 255); // Si no es borde se asigna un valor 255
					}
				}
			}
		}

		// Guardar la imagen de los bordes
		Imgcodecs.imwrite(rutaCarpetaDestino + "./BordesBinarizados.jpg", imagenBinarizada);
		
		//Guardar matriz de imagen Bordes
		try {
		    FileWriter writer = new FileWriter(rutaCarpetaDestino + "/ImagenMatrizBordesBinarizados.csv");

		    for (int i = 0; i < imagenBinarizada.rows(); i++) {
		        for (int j = 0; j < imagenBinarizada.cols(); j++) {
		            double[] value = imagenBinarizada.get(i, j);
		            writer.write(String.valueOf(value[0]));
		            if (j < imagenBinarizada.cols() - 1) {
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
