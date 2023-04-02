package Portafolio;

import java.awt.FileDialog;
import java.awt.Frame;

public class CarpetaDestino {

	public String selectFile() {
		String rutaImagen = "";
		// Leer CSV
		// Crear un nuevo objeto FileDialog
		FileDialog fileChooser = new FileDialog((Frame) null, "Seleccionar archivo CSV", FileDialog.LOAD);

		// Mostrar el diálogo para que el usuario seleccione un archivo
		fileChooser.setVisible(true);

		// Verificar si el usuario seleccionó un archivo
		if (fileChooser.getFile() != null) {

			// Obtener el archivo seleccionado
			rutaImagen = fileChooser.getDirectory() + fileChooser.getFile();

		}
		fileChooser.dispose();
		return rutaImagen;
	}
}
