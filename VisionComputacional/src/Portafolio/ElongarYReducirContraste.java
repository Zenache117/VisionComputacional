package Portafolio;

import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JOptionPane;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class ElongarYReducirContraste {
	public static void main(String[] args) {

		// Seleccionar la imagen a transformar
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
		Mat imagenDisminCont = new Mat(imageGray.rows(), imageGray.cols(), imageGray.type());
		Mat imagenAumentoCont = new Mat(imageGray.rows(), imageGray.cols(), imageGray.type());

		// Pedir factor de aumento y disminuci�n
		float factorAumento = -1;
		float factorDisminucion = -1;

		while (factorDisminucion > 1 || factorDisminucion < 0) {
			factorDisminucion = Float.parseFloat(JOptionPane.showInputDialog("Dame el factor a disminuir"));
			if (factorDisminucion > 1 || factorDisminucion < 0) {
				JOptionPane.showMessageDialog(null, "El valor debe estar entre 0 y 1");
			}
		}

		if (factorDisminucion == 0) {
			JOptionPane.showMessageDialog(null,
					"El factor de disminucion tiene un valor nulo, la imagen resultante no cambiara");
		}

		while (factorAumento > 2 || factorAumento < 1) {
			factorAumento = Float.parseFloat(JOptionPane.showInputDialog("Dame el factor de aumento"));
			if (factorAumento > 2 || factorAumento < 1) {
				JOptionPane.showMessageDialog(null, "El valor debe estar entre 1 y 2");
			}
		}

		// Pedir constante a aumentar
		int ConstAumentoBrillo = -1;

		while (ConstAumentoBrillo > 255 || ConstAumentoBrillo < 0) {
			ConstAumentoBrillo = Integer.parseInt(JOptionPane.showInputDialog("Dame la constante de aumento"));
			if (ConstAumentoBrillo > 255 || ConstAumentoBrillo < 0) {
				JOptionPane.showMessageDialog(null, "El valor debe estar entre 0 y 255");
			}
		}

		if (ConstAumentoBrillo == 0) {
			JOptionPane.showMessageDialog(null, "La constante de aumento tiene un valor nulo");
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
				double[] pixelDisminucionBrillo = new double[] { pixelOriginal[0] * factorDisminucion };
				imagenDisminCont.put(i, j, pixelDisminucionBrillo);

				// Actualiza minValue si es necesario
				minValue = Math.min(minValue, pixelDisminucionBrillo[0]);

				// Aumento de brillo
				double[] pixelAumentoBrillo = new double[] { pixelOriginal[0] * factorAumento + ConstAumentoBrillo };
				imagenAumentoCont.put(i, j, pixelAumentoBrillo);

				// Actualiza maxValue si es necesario
				maxValue = Math.max(maxValue, pixelAumentoBrillo[0]);

			}
		}

		// Reajustar los pixeles de imagenAumentoBrillo
		int respuestaAumentar = 100;
		int respuestaDisminuir = 100;
		if (maxValue > 255) {
			respuestaAumentar = JOptionPane.showConfirmDialog(null,
					"¿Quieres reajustar la imagen que aumentara el brillo?", "Reajustar",
					JOptionPane.YES_NO_CANCEL_OPTION);
			if (respuestaAumentar == JOptionPane.YES_OPTION) {
				for (int i = 0; i < imagenAumentoCont.rows(); i++) {
					for (int j = 0; j < imagenAumentoCont.cols(); j++) {
						double[] pixelAumentoBrillo = imagenAumentoCont.get(i, j);
						pixelAumentoBrillo[0] = (pixelAumentoBrillo[0] / maxValue) * 255;
						imagenAumentoCont.put(i, j, pixelAumentoBrillo);
					}
				}
			} else if (respuestaAumentar == JOptionPane.NO_OPTION) {
				JOptionPane.showMessageDialog(null,
						"No se reajustara la imagen apesar que algunos pixeles tienen valores mayores a 255");
			}
		}

		// Reajustar los pixeles de imagenDisminucionBrillo
		if (minValue < 0) {
			respuestaDisminuir = JOptionPane.showConfirmDialog(null, "¿Quieres reajustar la imagen a disminuir brillo?",
					"Reajustar", JOptionPane.YES_NO_CANCEL_OPTION);
			if (respuestaDisminuir == JOptionPane.YES_OPTION) {
				for (int i = 0; i < imagenDisminCont.rows(); i++) {
					for (int j = 0; j < imagenDisminCont.cols(); j++) {
						double[] pixelDisminucionBrillo = imagenDisminCont.get(i, j);
						pixelDisminucionBrillo[0] = pixelDisminucionBrillo[0] - minValue;
						imagenDisminCont.put(i, j, pixelDisminucionBrillo);
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
			Imgcodecs.imwrite(rutaCarpetaDestino + "./AumentoContraste_Reajustada.jpg", imagenAumentoCont);
		} else {
			Imgcodecs.imwrite(rutaCarpetaDestino + "./AumentoContraste.jpg", imagenAumentoCont);
		}
		if (minValue < 0) {
			Imgcodecs.imwrite(rutaCarpetaDestino + "./DisminucionContraste_Reajustada.jpg", imagenDisminCont);
		} else {
			Imgcodecs.imwrite(rutaCarpetaDestino + "./DisminucionContraste.jpg", imagenDisminCont);
		}

		//Guardar matriz de imagen aumentada
		try {
		    FileWriter writer = new FileWriter(rutaCarpetaDestino + "/ImagenMatrizContrasteElongado.csv");

		    for (int i = 0; i < imagenAumentoCont.rows(); i++) {
		        for (int j = 0; j < imagenAumentoCont.cols(); j++) {
		            double[] value = imagenAumentoCont.get(i, j);
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
		    FileWriter writer = new FileWriter(rutaCarpetaDestino + "/ImagenMatrizContrasteDisminuido.csv");

		    for (int i = 0; i < imagenDisminCont.rows(); i++) {
		        for (int j = 0; j < imagenDisminCont.cols(); j++) {
		            double[] value = imagenDisminCont.get(i, j);
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
