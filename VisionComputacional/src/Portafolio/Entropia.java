package Portafolio;

import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;

public class Entropia {
	public static double calcularEntropia(BufferedImage imagen) {
        int ancho = imagen.getWidth();
        int alto = imagen.getHeight();
        double entropia = 0.0;

        // Obtener el histograma de intensidad de los píxeles de la imagen
        ColorConvertOp operador = new java.awt.image.ColorConvertOp(
        imagen.getColorModel().getColorSpace(), ColorSpace.getInstance(ColorSpace.CS_GRAY), null);

        BufferedImage imagenGris = operador.filter(imagen, null);
        Raster raster = imagenGris.getData();
        int[] histograma = new int[256];
        for (int y = 0; y < alto; y++) {
            for (int x = 0; x < ancho; x++) {
                histograma[raster.getSample(x, y, 0)]++;
            }
        }

        // Calcular la distribución de probabilidades
        double[] distribucion = new double[256];
        for (int i = 0; i < 256; i++) {
            distribucion[i] = (double) histograma[i] / (ancho * alto);
        }

        // Calcular la entropía
        for (int i = 0; i < 256; i++) {
            if (distribucion[i] > 0) {
                entropia -= distribucion[i] * (Math.log(distribucion[i]) / Math.log(2));
            }
        }

        return entropia;
    }

    public static void main(String[] args) throws IOException {
    	JFileChooser fileChooser = new JFileChooser(); // crea un file chooser
    	fileChooser.setCurrentDirectory(new File(System.getProperty("user.home"))); // establece el directorio inicial
    	int result = fileChooser.showOpenDialog(null); // muestra el dialogo para abrir archivo

    	if (result == JFileChooser.APPROVE_OPTION) { // si el usuario selecciona un archivo
    	    File selectedFile = fileChooser.getSelectedFile(); // obtiene el archivo seleccionado
    	    BufferedImage imagen = ImageIO.read(selectedFile); // lee la imagen seleccionada
    	    double entropia = calcularEntropia(imagen);
    	    System.out.println("Entropía de la imagen: " + entropia);
    	}
    }
}
