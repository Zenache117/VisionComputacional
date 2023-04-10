package Portafolio;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;


public class UmbralConVarianza {
	public static void main(String[] args) throws IOException {
		// Crea un JFileChooser con filtro de extensiones para imágenes
		JFileChooser fileChooser = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Imágenes", "jpg", "jpeg", "png", "gif");
		fileChooser.setFileFilter(filter);

		// Muestra el diálogo para que el usuario seleccione un archivo
		int result = fileChooser.showOpenDialog(null);

		// Si el usuario seleccionó un archivo, lee la imagen y aplica el umbral
		if (result == JFileChooser.APPROVE_OPTION) {
		    File selectedFile = fileChooser.getSelectedFile();
		    BufferedImage image = ImageIO.read(selectedFile);
		    BufferedImage thresholdedImage = varianzaThreshold(image);
		    // Guarda el resultado en un archivo
		    File outputFile = new File("umbral.png");
		    ImageIO.write(thresholdedImage, "png", outputFile);
		}
	}
		
		public static BufferedImage varianzaThreshold(BufferedImage image) {
		    int width = image.getWidth();
		    int height = image.getHeight();

		    BufferedImage thresholdedImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY);

		    int[][] grayMatrix = new int[height][width];
		    int[] histogram = new int[256];

		    // Pasa la imagen a escala de grises y calcula el histograma
		    for (int y = 0; y < height; y++) {
		        for (int x = 0; x < width; x++) {
		            Color color = new Color(image.getRGB(x, y));
		            int gray = (int) (0.2989 * color.getRed() + 0.5870 * color.getGreen() + 0.1140 * color.getBlue());
		            grayMatrix[y][x] = gray;
		            histogram[gray]++;
		        }
		    }

		    // Calcula el umbral basado en las varianzas de los dos grupos
		    int threshold = 0;
		    double maxBetweenClassVar = 0.0;
		    double w1 = 0.0;
		    double w2 = 0.0;
		    double mean1 = 0.0;
		    double mean2 = 0.0;
		    double variance1 = 0.0;
		    double variance2 = 0.0;
		    for (int i = 0; i < 256; i++) {
		        w1 += histogram[i];
		        if (w1 == 0) {
		            continue;
		        }

		        w2 = width * height - w1;
		        if (w2 == 0) {
		            break;
		        }

		        mean1 = (mean1 * (w1 - histogram[i]) + i * histogram[i]) / w1;
		        mean2 = (mean2 * (w2 + histogram[i]) - i * histogram[i]) / w2;
		        variance1 = variance1 * (w1 - histogram[i]) + (i - mean1) * (i - mean1) * histogram[i];
		        variance1 /= w1;
		        variance2 = variance2 * (w2 + histogram[i]) + (i - mean2) * (i - mean2) * histogram[i];
		        variance2 /= w2;

		        double betweenClassVar = w1 * w2 * Math.pow((mean1 - mean2), 2) / Math.pow(width * height, 2);
		        if (betweenClassVar > maxBetweenClassVar) {
		            maxBetweenClassVar = betweenClassVar;
		            threshold = i;
		        }
		    }

		    // Aplica el umbral a la imagen
		    for (int y = 0; y < height; y++) {
		        for (int x = 0; x < width; x++) {
		            if (grayMatrix[y][x] > threshold) {
		                thresholdedImage.setRGB(x, y, Color.WHITE.getRGB());
		            } else {
		                thresholdedImage.setRGB(x, y, Color.BLACK.getRGB());
		            }
		        }
		    }

		    return thresholdedImage;
		}
		
	}

