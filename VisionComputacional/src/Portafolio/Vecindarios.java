package Portafolio;

import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class Vecindarios {

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

		// Pedir valor de rango entre pixeles vecinos
		int rango = Integer
				.parseInt(JOptionPane.showInputDialog("Cual es el valor de rango entre prixeles para ser vecinos?"));

		// Contador de tiempo de ejecución del programa
		Long startTime = System.currentTimeMillis();
		// Pasar matriz a Lista
		// Obt�n las dimensiones de la matriz
		int filas = imageGray.rows();
		int columnas = imageGray.cols();
		int pixelesSinVecindario = 0;

		// Crea una lista de listas para guardar los valores de la matriz
		List<List<Vecinos>> imagenGris = new ArrayList<>();

		// Recorre la matriz y guarda los valores en la lista de listas
		for (int i = 0; i < filas; i++) {
			List<Vecinos> fila = new ArrayList<>();
			for (int j = 0; j < columnas; j++) {
				Vecinos vec = new Vecinos();
				double[] valoresPixel = imageGray.get(i, j);
				vec.setValor((int) valoresPixel[0]);
				vec.setI(i);
				vec.setJ(j);
				fila.add(vec);
				pixelesSinVecindario++;
			}
			imagenGris.add(fila);
		}

		// Crea una lista para guardar los pixeles vecinos potenciales del vecindario
		// que se esta formando
		List<Posicion> vecinosPotenciales = new ArrayList<>();

		// Crea una lista de listas para guardar los vecindarios
		List<List<List<Vecinos>>> vecindarios = new ArrayList<>();

		// Este objeto sera� el que ayude a desplazarse entre pixeles
		Posicion posActual = new Posicion();
		posActual.setI(0);
		posActual.setJ(0);

		// Este objeto aclara con que pixel se comparar�n los valores del vecindario
		Posicion pxlVecCntrl = new Posicion();
		int numVecindario = 0;

		// Este valor deja en claro cuantos elementos se han asignado en la lista de
		// vecindarios
		int ElmtsEnVecindarios = 0;

		// Cantidad de pixeles de la imagen
		int cantPixeles = filas * columnas;

		// La segunda condici�n nos ayuda a que una vez se han asignado todos los
		// vecindarios, la lista de vecindarios aun se llene
		while (pixelesSinVecindario > 0 || ElmtsEnVecindarios != cantPixeles) {

			// Al primer pixel del vecindario se le asigna un valor de vecindario al iniciar
			// uno de estos y se design como pixel central
			if (vecinosPotenciales.size() == 0) {
				imagenGris.get(posActual.getI()).get(posActual.getJ()).setVecindario(numVecindario);
				pxlVecCntrl.setI(posActual.getI());
				pxlVecCntrl.setJ(posActual.getJ());
				pixelesSinVecindario--;
			}

			// si la lista de vecindarios aun no se llena pero ya se han asignado todos los
			// vecindarios en la lista imagenGris, esta secci�n ya no es necesaria
			if (pixelesSinVecindario > 0) {
				// INICIO DE REVISI�N DE PIXELES A POTENCIALES
				// VECINOS-----------------------------------------------------------------------------------------------------------

				// PIXEL SUPERIOR IZQUIERDO DEL PIXEL ACTUAL

				// revisar que el pixel a potencial como vecino exista dentro de los limites de
				// la imagen
				if (posActual.getI() - 1 >= 0 && posActual.getJ() - 1 >= 0) {
					// revisar que el pixel a revisar como vecino potencial no tenga ya un
					// vecindario asignado
					if (imagenGris.get(posActual.getI() - 1).get(posActual.getJ() - 1).getVecindario() == -1) {
						// revisar que el pixel no este ya en la lista de pixeles candidatos del
						// vecindario
						boolean yaCandidato = false;
						for (Posicion revisar : vecinosPotenciales) {
							if (imagenGris.get(posActual.getI() - 1).get(posActual.getJ() - 1).getI() == revisar.getI()
									&& imagenGris.get(posActual.getI() - 1).get(posActual.getJ() - 1).getJ() == revisar
											.getJ()) {
								yaCandidato = true;
							}
						}
						// revisar si la diferencia de valores esta dentro del rango para ser
						// considerado vecino
						int difValor = Math.abs(imagenGris.get(pxlVecCntrl.getI()).get(pxlVecCntrl.getJ()).getValor()
								- imagenGris.get(posActual.getI() - 1).get(posActual.getJ() - 1).getValor());
						// Si el pixel cumple con todo y no esta registrado ya, se va para la lista de
						// pixeles candidatos a vecinos
						if (yaCandidato == false ) {
							if(difValor <= rango) {
								Posicion nuevoPotencial = new Posicion();
								nuevoPotencial.setI(posActual.getI() - 1);
								nuevoPotencial.setJ(posActual.getJ() - 1);
								vecinosPotenciales.add(nuevoPotencial);
							}else {// El pixel actual sería borde
								imagenGris.get(posActual.getI()).get(posActual.getJ()).setBorde(true);
							}
						}
					}else {
						//Revisar si el pixel actual es borde
						if(imagenGris.get(posActual.getI()).get(posActual.getJ()).getVecindario() != imagenGris.get(posActual.getI() - 1).get(posActual.getJ() - 1).getVecindario()) {
							imagenGris.get(posActual.getI()).get(posActual.getJ()).setBorde(true);
						}
					}
				}

				// PIXEL INFERIOR IZQUIERDO DEL PIXEL ACTUAL

				// revisar que el pixel a potencial como vecino exista dentro de los limites de
				// la imagen
				if (posActual.getI() + 1 < filas && posActual.getJ() - 1 >= 0) {
					// revisar que el pixel a revisar como vecino potencial no tenga ya un
					// vecindario asignado
					if (imagenGris.get(posActual.getI() + 1).get(posActual.getJ() - 1).getVecindario() == -1) {
						// revisar que el pixel no este ya en la lista de pixeles candidatos del
						// vecindario
						boolean yaCandidato = false;
						for (Posicion revisar : vecinosPotenciales) {
							if (imagenGris.get(posActual.getI() + 1).get(posActual.getJ() - 1).getI() == revisar.getI()
									&& imagenGris.get(posActual.getI() + 1).get(posActual.getJ() - 1).getJ() == revisar
											.getJ()) {
								yaCandidato = true;
							}
						}
						// revisar si la diferencia de valores esta dentro del rango para ser
						// considerado vecino
						int difValor = Math.abs(imagenGris.get(pxlVecCntrl.getI()).get(pxlVecCntrl.getJ()).getValor()
								- imagenGris.get(posActual.getI() + 1).get(posActual.getJ() - 1).getValor());
						// Si el pixel cumple con todo y no esta registrado ya, se va para la lista de
						// pixeles candidatos a vecinos
						if (yaCandidato == false ) {
							if(difValor <= rango) {
							Posicion nuevoPotencial = new Posicion();
							nuevoPotencial.setI(posActual.getI() + 1);
							nuevoPotencial.setJ(posActual.getJ() - 1);
							vecinosPotenciales.add(nuevoPotencial);
							}else {// El pixel actual sería borde
								imagenGris.get(posActual.getI()).get(posActual.getJ()).setBorde(true);
							}
						}

					}else {
						//Revisar si el pixel actual es borde
						if(imagenGris.get(posActual.getI()).get(posActual.getJ()).getVecindario() != imagenGris.get(posActual.getI() + 1).get(posActual.getJ() - 1).getVecindario()) {
							imagenGris.get(posActual.getI()).get(posActual.getJ()).setBorde(true);
						}
					}
				}

				// PIXEL ENCIMA A EL PIXEL ACTUAL

				// revisar que el pixel a potencial como vecino exista dentro de los limites de
				// la imagen
				if (posActual.getI() - 1 >= 0) {
					// revisar que el pixel a revisar como vecino potencial no tenga ya un
					// vecindario asignado
					if (imagenGris.get(posActual.getI() - 1).get(posActual.getJ()).getVecindario() == -1) {
						// revisar que el pixel no este ya en la lista de pixeles candidatos del
						// vecindario
						boolean yaCandidato = false;
						for (Posicion revisar : vecinosPotenciales) {
							if (imagenGris.get(posActual.getI() - 1).get(posActual.getJ()).getI() == revisar.getI()
									&& imagenGris.get(posActual.getI() - 1).get(posActual.getJ()).getJ() == revisar
											.getJ()) {
								yaCandidato = true;
							}
						}
						// revisar si la diferencia de valores esta dentro del rango para ser
						// considerado vecino
						int difValor = Math.abs(imagenGris.get(pxlVecCntrl.getI()).get(pxlVecCntrl.getJ()).getValor()
								- imagenGris.get(posActual.getI() - 1).get(posActual.getJ()).getValor());
						// Si el pixel cumple con todo y no esta registrado ya, se va para la lista de
						// pixeles candidatos a vecinos
						if (yaCandidato == false ) {
							if(difValor <= rango) {
							Posicion nuevoPotencial = new Posicion();
							nuevoPotencial.setI(posActual.getI() - 1);
							nuevoPotencial.setJ(posActual.getJ());
							vecinosPotenciales.add(nuevoPotencial);
							}else {// El pixel actual sería borde
								imagenGris.get(posActual.getI()).get(posActual.getJ()).setBorde(true);
							}
						}

					}else {
						//Revisar si el pixel actual es borde
						if(imagenGris.get(posActual.getI()).get(posActual.getJ()).getVecindario() != imagenGris.get(posActual.getI() - 1).get(posActual.getJ()).getVecindario()) {
							imagenGris.get(posActual.getI()).get(posActual.getJ()).setBorde(true);
						}
					}
				}

				// PIXEL POR DEBAJO DEL PIXEL ACTUAL

				// revisar que el pixel a potencial como vecino exista dentro de los limites de
				// la imagen
				if (posActual.getI() + 1 < filas) {
					// revisar que el pixel a revisar como vecino potencial no tenga ya un
					// vecindario asignado

					if (imagenGris.get(posActual.getI() + 1).get(posActual.getJ()).getVecindario() == -1) {
						// revisar que el pixel no este ya en la lista de pixeles candidatos del
						// vecindario
						boolean yaCandidato = false;
						for (Posicion revisar : vecinosPotenciales) {
							if (imagenGris.get(posActual.getI() + 1).get(posActual.getJ()).getI() == revisar.getI()
									&& imagenGris.get(posActual.getI() + 1).get(posActual.getJ()).getJ() == revisar
											.getJ()) {
								yaCandidato = true;
							}
						}
						// revisar si la diferencia de valores esta dentro del rango para ser
						// considerado vecino
						int difValor = Math.abs(imagenGris.get(pxlVecCntrl.getI()).get(pxlVecCntrl.getJ()).getValor()
								- imagenGris.get(posActual.getI() + 1).get(posActual.getJ()).getValor());
						// Si el pixel cumple con todo y no esta registrado ya, se va para la lista de
						// pixeles candidatos a vecinos
						if (yaCandidato == false ) {
							if(difValor <= rango) {
							Posicion nuevoPotencial = new Posicion();
							nuevoPotencial.setI(posActual.getI() + 1);
							nuevoPotencial.setJ(posActual.getJ());
							vecinosPotenciales.add(nuevoPotencial);
							}else {// El pixel actual sería borde
								imagenGris.get(posActual.getI()).get(posActual.getJ()).setBorde(true);
							}
						}

					}else {
						//Revisar si el pixel actual es borde
						if(imagenGris.get(posActual.getI()).get(posActual.getJ()).getVecindario() != imagenGris.get(posActual.getI() + 1).get(posActual.getJ()).getVecindario()) {
							imagenGris.get(posActual.getI()).get(posActual.getJ()).setBorde(true);
						}
					}
				}

				// PIXEL SUPERIOR DERECHO DEL PIXEL ACTUAL

				// revisar que el pixel a potencial como vecino exista dentro de los limites de
				// la imagen
				if (posActual.getI() - 1 >= 0 && posActual.getJ() + 1 < columnas) {
					// revisar que el pixel a revisar como vecino potencial no tenga ya un
					// vecindario asignado
					if (imagenGris.get(posActual.getI() - 1).get(posActual.getJ() + 1).getVecindario() == -1) {
						// revisar que el pixel no este ya en la lista de pixeles candidatos del
						// vecindario
						boolean yaCandidato = false;
						for (Posicion revisar : vecinosPotenciales) {
							if (imagenGris.get(posActual.getI() - 1).get(posActual.getJ() + 1).getI() == revisar.getI()
									&& imagenGris.get(posActual.getI() - 1).get(posActual.getJ() + 1).getJ() == revisar
											.getJ()) {
								yaCandidato = true;
							}
						}
						// revisar si la diferencia de valores esta dentro del rango para ser
						// considerado vecino
						int difValor = Math.abs(imagenGris.get(pxlVecCntrl.getI()).get(pxlVecCntrl.getJ()).getValor()
								- imagenGris.get(posActual.getI() - 1).get(posActual.getJ() + 1).getValor());
						// Si el pixel cumple con todo y no esta registrado ya, se va para la lista de
						// pixeles candidatos a vecinos
						if (yaCandidato == false ) {
							if(difValor <= rango) {
							Posicion nuevoPotencial = new Posicion();
							nuevoPotencial.setI(posActual.getI() - 1);
							nuevoPotencial.setJ(posActual.getJ() + 1);
							vecinosPotenciales.add(nuevoPotencial);
							}else {// El pixel actual sería borde
								imagenGris.get(posActual.getI()).get(posActual.getJ()).setBorde(true);
							}
						}

					}else {
						//Revisar si el pixel actual es borde
						if(imagenGris.get(posActual.getI()).get(posActual.getJ()).getVecindario() != imagenGris.get(posActual.getI() - 1).get(posActual.getJ() + 1).getVecindario()) {
							imagenGris.get(posActual.getI()).get(posActual.getJ()).setBorde(true);
						}
					}
				}

				// PIXEL LATERAL DERECHO DEL PIXEL ACTUAL

				// revisar que el pixel a potencial como vecino exista dentro de los limites de
				// la imagen
				if (posActual.getJ() + 1 < columnas) {
					// revisar que el pixel a revisar como vecino potencial no tenga ya un
					// vecindario asignado
					if (imagenGris.get(posActual.getI()).get(posActual.getJ() + 1).getVecindario() == -1) {
						// revisar que el pixel no este ya en la lista de pixeles candidatos del
						// vecindario
						boolean yaCandidato = false;
						for (Posicion revisar : vecinosPotenciales) {
							if (imagenGris.get(posActual.getI()).get(posActual.getJ() + 1).getI() == revisar.getI()
									&& imagenGris.get(posActual.getI()).get(posActual.getJ() + 1).getJ() == revisar
											.getJ()) {
								yaCandidato = true;
							}
						}
						// revisar si la diferencia de valores esta dentro del rango para ser
						// considerado vecino
						int difValor = Math.abs(imagenGris.get(pxlVecCntrl.getI()).get(pxlVecCntrl.getJ()).getValor()
								- imagenGris.get(posActual.getI()).get(posActual.getJ() + 1).getValor());
						// Si el pixel cumple con todo y no esta registrado ya, se va para la lista de
						// pixeles candidatos a vecinos
						if (yaCandidato == false ) {
							if(difValor <= rango) {
							Posicion nuevoPotencial = new Posicion();
							nuevoPotencial.setI(posActual.getI());
							nuevoPotencial.setJ(posActual.getJ() + 1);
							vecinosPotenciales.add(nuevoPotencial);
							}else {// El pixel actual sería borde
								imagenGris.get(posActual.getI()).get(posActual.getJ()).setBorde(true);
							}
						}

					}else {
						//Revisar si el pixel actual es borde
						if(imagenGris.get(posActual.getI()).get(posActual.getJ()).getVecindario() != imagenGris.get(posActual.getI()).get(posActual.getJ() + 1).getVecindario()) {
							imagenGris.get(posActual.getI()).get(posActual.getJ()).setBorde(true);
						}
					}
				}

				// PIXEL INFERIOR DERECHO DEL PIXEL ACTUAL

				// revisar que el pixel a potencial como vecino exista dentro de los limites de
				// la imagen
				if (posActual.getI() + 1 < filas && posActual.getJ() + 1 < columnas) {
					// revisar que el pixel a revisar como vecino potencial no tenga ya un
					// vecindario asignado
					if (imagenGris.get(posActual.getI() + 1).get(posActual.getJ() + 1).getVecindario() == -1) {
						// revisar que el pixel no este ya en la lista de pixeles candidatos del
						// vecindario
						boolean yaCandidato = false;
						for (Posicion revisar : vecinosPotenciales) {
							if (imagenGris.get(posActual.getI() + 1).get(posActual.getJ() + 1).getI() == revisar.getI()
									&& imagenGris.get(posActual.getI() + 1).get(posActual.getJ() + 1).getJ() == revisar
											.getJ()) {
								yaCandidato = true;
							}
						}
						// revisar si la diferencia de valores esta dentro del rango para ser
						// considerado vecino
						int difValor = Math.abs(imagenGris.get(pxlVecCntrl.getI()).get(pxlVecCntrl.getJ()).getValor()
								- imagenGris.get(posActual.getI() + 1).get(posActual.getJ() + 1).getValor());
						// Si el pixel cumple con todo y no esta registrado ya, se va para la lista de
						// pixeles candidatos a vecinos
						if (yaCandidato == false ) {
							if(difValor <= rango) {
							Posicion nuevoPotencial = new Posicion();
							nuevoPotencial.setI(posActual.getI() + 1);
							nuevoPotencial.setJ(posActual.getJ() + 1);
							vecinosPotenciales.add(nuevoPotencial);
							}else {// El pixel actual sería borde
								imagenGris.get(posActual.getI()).get(posActual.getJ()).setBorde(true);
							}
						}

					}else {
						//Revisar si el pixel actual es borde
						if(imagenGris.get(posActual.getI()).get(posActual.getJ()).getVecindario() != imagenGris.get(posActual.getI() + 1).get(posActual.getJ() + 1).getVecindario()) {
							imagenGris.get(posActual.getI()).get(posActual.getJ()).setBorde(true);
						}
					}
				}

				// FIN DE REVISI�N DE PIXELES A POTENCIALES
				// VECINOS-----------------------------------------------------------------------------------------------------------
			}
			// revisar que la lista de vecinos potenciales no este vacia
			if (vecinosPotenciales.size() > 0) {
				// revisar vecino potencial preferente a agregar dandole preferencia al que se
				// encuentre en la fila mas alta y mas cercano (de menor indice I y luego a el
				// de menor indice J)
				int menorFila = vecinosPotenciales.get(0).getI();
				int menorColumna = vecinosPotenciales.get(0).getJ();
				for (Posicion posibles : vecinosPotenciales) {
					int posPsI = posibles.getI();
					if (posPsI <= menorFila) {
						menorFila = posibles.getI();
						menorColumna = posibles.getJ();
					}
				}
				for (Posicion posibles : vecinosPotenciales) {
					int posPsJ = posibles.getJ();
					if (posibles.getI() == menorFila) {
						if (posibles.getI() == menorFila) {
							if (posPsJ <= menorColumna) {
								menorFila = posibles.getI();
								menorColumna = posibles.getJ();
							}
						}
					}
				}

				// teniendo el pixel del siguiente para el vecindario se selecciona ese como la
				// neuva posisi�n y se le coloca el valor de vecindario
				posActual.setI(menorFila);
				posActual.setJ(menorColumna);
				imagenGris.get(menorFila).get(menorColumna).setVecindario(numVecindario);
				pixelesSinVecindario--;

				// borrar elemento electo de la lista de vecinos posibles
				borrar: for (int r = 0; r < vecinosPotenciales.size(); r++) {
					if (vecinosPotenciales.get(r).getI() == menorFila
							&& vecinosPotenciales.get(r).getJ() == menorColumna) {
						vecinosPotenciales.remove(r);
						break borrar;
					}
				}

			}
			// en caso de ya no existir ningun elemento en la lista de vecinos potenciales
			// significa que el vecindario ya se realiz�
			else {

				// Se realiza una lista de los vecindarios generados
				List<List<Vecinos>> vecindarioX = new ArrayList<List<Vecinos>>();

				for (int x = 0; x < filas; x++) {
					List<Vecinos> fila = new ArrayList<>();
					for (int y = 0; y < columnas; y++) {
						Vecinos vec = new Vecinos();
						if (imagenGris.get(x).get(y).getVecindario() == numVecindario) {
							vec.setI(imagenGris.get(x).get(y).getI());
							vec.setJ(imagenGris.get(x).get(y).getJ());
							vec.setValor(imagenGris.get(x).get(y).getValor());
							vec.setVecindario(imagenGris.get(x).get(y).getVecindario());
							vec.setBorde(imagenGris.get(x).get(y).isBorde());
							fila.add(vec);
							ElmtsEnVecindarios++;
						}
					}
					if (fila.size() > 0) {
						vecindarioX.add(fila);
					}
				}

				// Se agrega el vecindario generado a la lista que contiene todos los
				// vecindarios
				vecindarios.add(vecindarioX);

				// Se incrementa el id de vecindario para asignar
				numVecindario++;

				// en caso de que un vecindario halla finalizado se revisa si es posible hacer
				// mas para continuar generandolos
				if (pixelesSinVecindario > 0) {
					// se busca el siguiente pixel a iniciar vecindario
					busqueda: for (List<Vecinos> vecLst : imagenGris) {
						for (Vecinos vec : vecLst) {
							// La condicion indica que si el pixel no tiene un vecindario definido se
							// selecciona
							if (vec.getVecindario() == -1) {
								posActual.setI(vec.getI());
								posActual.setJ(vec.getJ());
								pxlVecCntrl.setI(vec.getI());
								pxlVecCntrl.setJ(vec.getJ());

								// Al encontrar nueva posici�n no es necesario seguir en el ciclo por lo que se
								// sale y continua con el resto de la ejecucui�n
								break busqueda;
							}
						}
					}
				}

			}

		}

		// Mostrar tiempo de ejecución
		long endTime = System.currentTimeMillis();
		double tiempoDeEjecucion = (endTime - startTime) / 1000.0;
		System.out.println("Tiempo de ejecución: " + tiempoDeEjecucion + " segundos");
		// Mostrar cantidad de vecindarios
		System.out.println("Vecindarios: " + vecindarios.size());

		// Seleccionar la carpeta destino para guardar la imagen transformada
		CarpetaDestino carpetaDestino = new CarpetaDestino();

		String rutaCarpetaDestino = carpetaDestino.selectCarpet();

		// Crear el archivo CSV
		File vecindariosCSV = new File(rutaCarpetaDestino, "Vecindarios.csv");

		try {
			// Crear el FileWriter y BufferedWriter para escribir en el archivo CSV
			FileWriter fw = new FileWriter(vecindariosCSV);
			BufferedWriter bw = new BufferedWriter(fw);

			// Escribir los resultados en el archivo vecindariosCSV
			bw.write("Fila,Columna,Vecindario,Valor,Cantidad de Vecindarios," + vecindarios.size());
			bw.newLine();
			for (List<List<Vecinos>> vecindario : vecindarios) {
				for (List<Vecinos> veci : vecindario) {
					for (Vecinos vec : veci) {
						String linea = vec.getI() + "," + vec.getJ() + "," + vec.getVecindario() + "," + vec.getValor();
						bw.write(linea);
						bw.newLine();
					}
				}
				// Salta una linea entre vecindarios para visualizarlos mejor en el CSV
				bw.newLine();
			}

			// Cerrar el BufferedWriter y FileWriter
			bw.close();
			fw.close();

			System.out.println("El archivo VecindarioCSV ha sido guardado en la ubicacion seleccionada.");

		} catch (IOException e) {
			e.printStackTrace();
		}

		// Crear el archivo MatrizVecindariosCSV
		File matrizCSV = new File(rutaCarpetaDestino, "MatrizVecindarios.csv");
		try {
			// Crear el FileWriter y BufferedWriter para escribir en el archivo CSV
			FileWriter fw = new FileWriter(matrizCSV);
			BufferedWriter bw = new BufferedWriter(fw);

			// Escribir los resultados en el archivo CSV
			for (List<Vecinos> vecindario : imagenGris) {
				for (Vecinos veci : vecindario) {
					String linea = veci.getVecindario() + ",";
					bw.write(linea);
				}
				bw.newLine();
			}

			// Cerrar el BufferedWriter y FileWriter
			bw.close();
			fw.close();

			System.out.println("El archivo MatrizVecindariosCSV ha sido guardado en la ubicacion seleccionada.");

		} catch (IOException e) {
			e.printStackTrace();
		}

		// Crear imagen de vecindarios a colores
		VisualizacionVecindarios visualizar = new VisualizacionVecindarios();
		visualizar.vecindariosColoreado(imageGray.width(), imageGray.height(), vecindarios, rutaCarpetaDestino);

		// Crear imagen de bordes de vecindario
		BordesDeVecindario bordes = new BordesDeVecindario();
		bordes.binarizarBordes(imageGray.width(), imageGray.height(), vecindarios, rutaCarpetaDestino);

	}

}
