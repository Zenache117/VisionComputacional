package Portafolio;

import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JOptionPane;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class Aumento_Disminucion {

	public static void main(String[] args) {

		SeleccionarArchivo archivoSeleccionado = new SeleccionarArchivo();

		String rutaImagen = archivoSeleccionado.selectFile();

		// Cargar la biblioteca OpenCV
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

		// Leer la imagen seleccionada
		Mat image = Imgcodecs.imread(rutaImagen);

		// Convertir la imagen a escala de grises
		Mat imageGray = new Mat();
		Imgproc.cvtColor(image, imageGray, Imgproc.COLOR_BGR2GRAY);

		// Crear matrices de imagenes en funci�n a las dimenciones originales
		Mat imagenDisminucionBrillo = new Mat(imageGray.rows(), imageGray.cols(), imageGray.type());
		Mat imagenAumentoBrillo = new Mat(imageGray.rows(), imageGray.cols(), imageGray.type());

		// Pedir valor a disminu�r y aumentar
		int valorDisminucionBrillo = -1;
		int valorAumentoBrillo = -1;

		while (valorDisminucionBrillo > 255 || valorDisminucionBrillo < 0) {
			valorDisminucionBrillo = Integer
					.parseInt(JOptionPane.showInputDialog("Dame el valor a disminuir de brillo"));
			if (valorDisminucionBrillo > 255 || valorDisminucionBrillo < 0) {
				JOptionPane.showMessageDialog(null, "El valor debe estar entre 0 y 255");
			}
		}

		if (valorDisminucionBrillo == 0) {
			JOptionPane.showMessageDialog(null, "No disminuira el brillo por ser un valor nulo, quedar� igual");
		}

		while (valorAumentoBrillo > 255 || valorAumentoBrillo < 0) {
			valorAumentoBrillo = Integer.parseInt(JOptionPane.showInputDialog("Dame el valor a aumentar de brillo"));
			if (valorAumentoBrillo > 255 || valorAumentoBrillo < 0) {
				JOptionPane.showMessageDialog(null, "El valor debe estar entre 0 y 255");
			}
		}

		if (valorAumentoBrillo == 0) {
			JOptionPane.showMessageDialog(null, "No aumentara el brillo por ser un valor nulo, quedar� igual");
		}

		// Definir variables para resguardar el valor maximo y minimo alcanzado por los
		// pixeles en sus modificaciones
		double minValue = Double.MAX_VALUE;
		double maxValue = Double.MIN_VALUE;

		// Comenzar tratamiento de pixeles
		for (int i = 0; i < imageGray.rows(); i++) {
			for (int j = 0; j < imageGray.cols(); j++) {
				double[] pixelOriginal = imageGray.get(i, j);

				// Disminuci�n de brillo
				double[] pixelDisminucionBrillo = new double[] { pixelOriginal[0] - valorDisminucionBrillo };
				imagenDisminucionBrillo.put(i, j, pixelDisminucionBrillo);

				// Actualiza minValue si es necesario
				minValue = Math.min(minValue, pixelDisminucionBrillo[0]);

				// Aumento de brillo
				double[] pixelAumentoBrillo = new double[] { pixelOriginal[0] + valorAumentoBrillo };
				imagenAumentoBrillo.put(i, j, pixelAumentoBrillo);

				// Actualiza maxValue si es necesario
				maxValue = Math.max(maxValue, pixelAumentoBrillo[0]);

			}
		}

		// Reajustar los pixeles de imagenAumentoBrillo
		int respuestaAumentar = 100;
		int respuestaDisminuir = 100;
		if (maxValue > 255) {
			respuestaAumentar = JOptionPane.showConfirmDialog(null,
					"�Quieres reajustar la imagen que aumentara el brillo?", "Reajustar",
					JOptionPane.YES_NO_CANCEL_OPTION);
			if (respuestaAumentar == JOptionPane.YES_OPTION) {
				for (int i = 0; i < imagenAumentoBrillo.rows(); i++) {
					for (int j = 0; j < imagenAumentoBrillo.cols(); j++) {
						double[] pixelAumentoBrillo = imagenAumentoBrillo.get(i, j);
						pixelAumentoBrillo[0] = (pixelAumentoBrillo[0] / maxValue) * 255;
						imagenAumentoBrillo.put(i, j, pixelAumentoBrillo);
					}
				}
			} else if (respuestaAumentar == JOptionPane.NO_OPTION) {
				JOptionPane.showMessageDialog(null,
						"No se reajustara la imagen apesar que algunos pixeles tienen valores mayores a 255");
			}
		}

		// Reajustar los pixeles de imagenDisminucionBrillo
		if (minValue < 0) {
			respuestaDisminuir = JOptionPane.showConfirmDialog(null, "�Quieres reajustar la imagen a disminuir brillo?",
					"Reajustar", JOptionPane.YES_NO_CANCEL_OPTION);
			if (respuestaDisminuir == JOptionPane.YES_OPTION) {
				for (int i = 0; i < imagenDisminucionBrillo.rows(); i++) {
					for (int j = 0; j < imagenDisminucionBrillo.cols(); j++) {
						double[] pixelDisminucionBrillo = imagenDisminucionBrillo.get(i, j);
						pixelDisminucionBrillo[0] = pixelDisminucionBrillo[0] - minValue;
						imagenDisminucionBrillo.put(i, j, pixelDisminucionBrillo);
					}
				}
			} else if (respuestaDisminuir == JOptionPane.NO_OPTION) {
				JOptionPane.showMessageDialog(null,
						"No se reajustara la imagen apesar que algunos pixeles tienen valores menores a 0");
			}
		}

		// Seleccionar la carpeta destino para guardar la imagen transformada
		CarpetaDestino carpetaDestino = new CarpetaDestino();

		String rutaCarpetaDestino = carpetaDestino.selectCarpet();

		// Guardar la imagen transformada en la carpeta seleccionada
		if (maxValue > 255) {
			Imgcodecs.imwrite(rutaCarpetaDestino + "./Aumento_Reajustada.jpg", imagenAumentoBrillo);
		} else {
			Imgcodecs.imwrite(rutaCarpetaDestino + "./Aumento.jpg", imagenAumentoBrillo);
		}
		if (minValue < 0) {
			Imgcodecs.imwrite(rutaCarpetaDestino + "./Disminuci�n_Reajustada.jpg", imagenDisminucionBrillo);
		} else {
			Imgcodecs.imwrite(rutaCarpetaDestino + "./Disminuci�n.jpg", imagenDisminucionBrillo);
		}
		//Guardar matriz de imagen aumentada
		try {
		    FileWriter writer = new FileWriter(rutaCarpetaDestino + "/ImagenMatrizAumentada.csv");

		    for (int i = 0; i < imagenAumentoBrillo.rows(); i++) {
		        for (int j = 0; j < imagenAumentoBrillo.cols(); j++) {
		            double[] value = imagenAumentoBrillo.get(i, j);
		            writer.write(String.valueOf(value[0]) + ",");
		        }
		        writer.write("\n");
		    }

		    writer.close();
		} catch (IOException e) {
		    e.printStackTrace();
		}

		//Guardar matriz de imagen disminuida
		try {
		    FileWriter writer = new FileWriter(rutaCarpetaDestino + "/ImagenMatrizDisminuida.csv");

		    for (int i = 0; i < imagenDisminucionBrillo.rows(); i++) {
		        for (int j = 0; j < imagenDisminucionBrillo.cols(); j++) {
		            double[] value = imagenDisminucionBrillo.get(i, j);
		            writer.write(String.valueOf(value[0]) + ",");
		        }
		        writer.write("\n");
		    }

		    writer.close();
		} catch (IOException e) {
		    e.printStackTrace();
		}
	}

}
