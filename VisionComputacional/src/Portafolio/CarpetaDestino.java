package Portafolio;

import java.awt.FileDialog;
import java.awt.Frame;

public class CarpetaDestino {

	public String selectCarpet() {
		// Seleccionar la carpeta destino para guardar la imagen transformada
				String rutaCarpetaDestino;
				// Crear un nuevo objeto FileDialog
				FileDialog carpetaDestino = new FileDialog((Frame) null, "Seleccionar carpeta destino", FileDialog.LOAD);
				carpetaDestino.setMode(FileDialog.SAVE);
				carpetaDestino.setVisible(true);
				rutaCarpetaDestino = carpetaDestino.getDirectory();
				carpetaDestino.dispose();
				return rutaCarpetaDestino;
	}
}
