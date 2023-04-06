package Portafolio;

import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JOptionPane;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class AclaradoOscurecimiento1Pixel {

	public static void main(String[] args) {

		SeleccionarArchivo seleccionarArchivo = new SeleccionarArchivo();
		// Seleccionar la imagen a transformar

		String rutaImagen = seleccionarArchivo.selectFile();

		// Cargar la biblioteca OpenCV
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

		// Leer la imagen seleccionada
		Mat image = Imgcodecs.imread(rutaImagen);

		// Convertir la imagen a escala de grises
		Mat imageGray = new Mat();
		Imgproc.cvtColor(image, imageGray, Imgproc.COLOR_BGR2GRAY);
		///////////////////////////////////////////////
		// Obtener el tamaño de la imagen
		int height = imageGray.rows();
		int width = imageGray.cols();


		///////////////////////////////////////////////////////////////////////////////

		// Crear matrices de imagenes en funcion a las dimenciones originales
		Mat imagenDisminucionBrillo = new Mat(imageGray.rows(), imageGray.cols(), imageGray.type());
		Mat imagenAumentoBrillo = new Mat(imageGray.rows(), imageGray.cols(), imageGray.type());

		// Copiar imagen para las 2 resultantes
		for (int i = 0; i < imageGray.rows(); i++) {
			for (int j = 0; j < imageGray.cols(); j++) {
				// Obtener el valor del pixel
				double[] pixelValue = imageGray.get(i, j);

				// Copiar el valor del pixel
				imagenAumentoBrillo.put(i, j, pixelValue);
				imagenDisminucionBrillo.put(i, j, pixelValue);
			}
		}

		// Preguntar al usuario las coordenadas del pixel a modificar
		int row = -1, col = -1;
		while (row < 0 || row >= height || col < 0 || col >= width) {
			String input = JOptionPane.showInputDialog("Ingrese la fila y columna del pixel a modificar (x,y)");
			String[] coords = input.split(",");
			row = Integer.parseInt(coords[0].trim());
			col = Integer.parseInt(coords[1].trim());
		}

		// Obtener el valor del pixel seleccionado
		double[] pixelValue = imageGray.get(row, col);

		// Pedir valor para diferenciar de donde aclarar y desde donde disminuir
		double valorDivisorio = pixelValue[0];

		// Pedir valor a disminuir y aumentar
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
			JOptionPane.showMessageDialog(null, "No disminuira el brillo por ser un valor nulo, quedara igual");
		}

		while (valorAumentoBrillo > 255 || valorAumentoBrillo < 0) {
			valorAumentoBrillo = Integer.parseInt(JOptionPane.showInputDialog("Dame el valor a aumentar de brillo"));
			if (valorAumentoBrillo > 255 || valorAumentoBrillo < 0) {
				JOptionPane.showMessageDialog(null, "El valor debe estar entre 0 y 255");
			}
		}

		if (valorAumentoBrillo == 0) {
			JOptionPane.showMessageDialog(null, "No aumentara el brillo por ser un valor nulo, quedara igual");
		}

		// Definir variables para resguardar el valor maximo y minimo alcanzado por los
		// pixeles en sus modificaciones
		double minValue;
		double maxValue;

		// Disminución de brillo
		double[] pixelDisminucionBrillo = new double[] { pixelValue[0] - valorDisminucionBrillo };
		imagenDisminucionBrillo.put(row, col, pixelDisminucionBrillo);

		// Aumento de brillo
		double[] pixelAumentoBrillo = new double[] { pixelValue[0] + valorAumentoBrillo };
		imagenAumentoBrillo.put(row, col, pixelAumentoBrillo);

		maxValue = pixelAumentoBrillo[0];
		minValue = pixelDisminucionBrillo[0];

		// Reajustar los pixeles de imagenAumentoBrillo
		int respuestaAumentar = 100;
		int respuestaDisminuir = 100;
		if (maxValue > 255) {
			respuestaAumentar = JOptionPane.showConfirmDialog(null,"¿Quieres reajustar la imagen que aumentara el brillo?", "Reajustar",JOptionPane.YES_NO_CANCEL_OPTION);
			if (respuestaAumentar == JOptionPane.YES_OPTION) {
				for(int i = 0; i<imageGray.rows(); i++) {
					row = i;
					for(int j = 0; j<imageGray.cols(); j++) {
						col = j;
						pixelAumentoBrillo = imagenAumentoBrillo.get(row, col);
						pixelAumentoBrillo[0] = (pixelAumentoBrillo[0] / maxValue) * 255;
						imagenAumentoBrillo.put(row, col, pixelAumentoBrillo);
					}
				}

			} else if (respuestaAumentar == JOptionPane.NO_OPTION) {
				JOptionPane.showMessageDialog(null,"No se reajustara la imagen apesar que algunos pixeles tienen valores mayores a 255");
			}
		}

		// Reajustar los pixeles de imagenDisminucionBrillo
		if (minValue < 0) {
			respuestaDisminuir = JOptionPane.showConfirmDialog(null, "¿Quieres reajustar la imagen que disminuira el brillo?","Reajustar", JOptionPane.YES_NO_CANCEL_OPTION);
			if (respuestaDisminuir == JOptionPane.YES_OPTION) {
				for(int i = 0; i<imageGray.rows(); i++) {
					row = i;
					for(int j = 0; j<imageGray.cols(); j++) {
						col = j;
						pixelDisminucionBrillo = imagenDisminucionBrillo.get(row, col);
						pixelDisminucionBrillo[0] = ((pixelDisminucionBrillo[0] - minValue) / (valorDivisorio - minValue)) * 255;
						imagenDisminucionBrillo.put(row, col, pixelDisminucionBrillo);
					}
				}

			} else if (respuestaDisminuir == JOptionPane.NO_OPTION) {
				JOptionPane.showMessageDialog(null,"No se reajustara la imagen apesar que algunos pixeles tienen valores menores a 0");
			}
		}


		// Seleccionar la carpeta destino para guardar la imagen transformada
		CarpetaDestino carpeta = new CarpetaDestino();

		String carpetaDestino = carpeta.selectCarpet();

		// Guardar la imagen transformada en la carpeta seleccionada
		if (maxValue > 255 && respuestaAumentar == JOptionPane.YES_OPTION) {
			Imgcodecs.imwrite(carpetaDestino + "./Aclarado_Reajustada.jpg", imagenAumentoBrillo);
		} else {
			Imgcodecs.imwrite(carpetaDestino + "./Aclarado.jpg", imagenAumentoBrillo);
		}
		if (minValue < 0 && respuestaDisminuir == JOptionPane.YES_OPTION) {
			Imgcodecs.imwrite(carpetaDestino + "./Oscurecimiento_Reajustada.jpg", imagenDisminucionBrillo);
		} else {
			Imgcodecs.imwrite(carpetaDestino + "./Oscurecimiento.jpg", imagenDisminucionBrillo);
		}

		//Guardar matriz de imagen aumentada
		try {
			FileWriter writer = new FileWriter(carpetaDestino + "/ImagenMatrizAclarada.csv");

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
			FileWriter writer = new FileWriter(carpetaDestino + "/ImagenMatrizOscurecida.csv");

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
