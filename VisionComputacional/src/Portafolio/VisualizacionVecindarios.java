package Portafolio;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

public class VisualizacionVecindarios {

	public void vecindariosColoreado(int width, int height, List<List<List<Vecinos>>> vecindarios, String rutaCarpetaDestino) {

	    // Cargar la biblioteca OpenCV
	    System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

	    List<Color> colores = new ArrayList<>();

	    for (int i = 0; i < vecindarios.size(); i++) {
	        int r = (int) (Math.random() * 256);
	        int g = (int) (Math.random() * 256);
	        int b = (int) (Math.random() * 256);
	        Color color = new Color(r, g, b);
	        colores.add(color);
	    }

	    // Crear una imagen RGB con el mismo tamaño que los vecindarios
	    Mat image = new Mat(height, width, CvType.CV_8UC3);

	    // Asignar un color a cada vecindario
	    for (int i = 0; i < vecindarios.size(); i++) {
	        for (int j = 0; j < vecindarios.get(i).size(); j++) {
	            List<Vecinos> vecindario = vecindarios.get(i).get(j);
	            int colorIndex = i; // Cálculo del índice del color
	            Color color = colores.get(colorIndex);
	            for (Vecinos pixel : vecindario) {
	                // Asignar el valor del píxel correspondiente a los valores de los canales de color del color
	                image.put(pixel.getI(), pixel.getJ(), color.getRed(), color.getGreen(), color.getBlue());
	            }
	        }
	    }

	    // Guardar la imagen vecindarios a colores
	    Imgcodecs.imwrite(rutaCarpetaDestino + "./VecindariosColoreados.jpg", image);
	    
	}

}
