package Portafolio;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class ProyeccionDeLineas {

	private int maxIterations;
	private double distanceThreshold;
	private int inlierThreshold;
	private int numLines;
	private int maxGap;

	public static void main(String[] args) {

		// Seleccionar la imagen a transformar
		SeleccionarArchivo archivoSeleccionado = new SeleccionarArchivo();
		String rutaImagen = archivoSeleccionado.selectFile();

		// Cargar la biblioteca OpenCV
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

		// Leer la imagen seleccionada
		Mat image = Imgcodecs.imread(rutaImagen);

		ProyeccionDeLineas detector = new ProyeccionDeLineas(1000, 3.0, 100, 100, 0);
		detector.detectLines(image);

	}

	public ProyeccionDeLineas(int maxIterations, double distanceThreshold, int inlierThreshold, int numLines,
			int maxGap) {
		this.maxIterations = maxIterations;
		this.distanceThreshold = distanceThreshold;
		this.inlierThreshold = inlierThreshold;
		this.numLines = numLines;
		this.maxGap = maxGap;
	}

	public void detectLines(Mat image) {

		// Get all black pixels
		ArrayList<Point> blackPixels = new ArrayList<>();
		for (int x = 0; x < image.width(); x++) {
			for (int y = 0; y < image.height(); y++) {
				double[] pixel = image.get(y, x);
				if (pixel[0] == 0 && pixel[1] == 0 && pixel[2] == 0) {
					blackPixels.add(new Point(x, y));
				}
			}
		}

		// RANSAC algorithm
		Random random = new Random();
		for (int l = 0; l < numLines; l++) {
			Line bestLine = null;
			int bestInliers = 0;
			ArrayList<Point> bestInlierPoints = new ArrayList<>();
			for (int i = 0; i < maxIterations; i++) {
				// Select two random points
				Point p1 = blackPixels.get(random.nextInt(blackPixels.size()));
				Point p2 = blackPixels.get(random.nextInt(blackPixels.size()));

				// Calculate line equation
				Line line = new Line(p1.x, p1.y, p2.x, p2.y);

				// Count inliers
				int inliers = 0;
				ArrayList<Point> inlierPoints = new ArrayList<>();
				for (Point p : blackPixels) {
					double distance = line.distance(p.x, p.y);
					if (distance < distanceThreshold) {
						inliers++;
						inlierPoints.add(p);
					}
				}

				// Check if line is better than current best line
				if (inliers > bestInliers && inliers > inlierThreshold) {
					bestLine = line;
					bestInliers = inliers;
					bestInlierPoints = inlierPoints;
				}
			}

			// Dibujar la mejor línea en la imagen
			if (bestLine != null) {
				// Ordenar los puntos inliers por coordenada x
				bestInlierPoints.sort(Comparator.comparingDouble(p -> p.x));

				// Encontrar grupos de puntos inliers consecutivos que están cerca uno del otro
				List<List<Point>> pointGroups = new ArrayList<>();
				List<Point> currentGroup = new ArrayList<>();
				for (int i = 0; i < bestInlierPoints.size(); i++) {
					Point p = bestInlierPoints.get(i);
					currentGroup.add(p);
					if (i == bestInlierPoints.size() - 1 || bestInlierPoints.get(i + 1).x - p.x > maxGap) {
						pointGroups.add(currentGroup);
						currentGroup = new ArrayList<>();
					}
				}

				// Dibujar líneas entre cada grupo de puntos
				for (List<Point> group : pointGroups) {
					if (group.size() >= 2) {
						Point start = group.get(0);
						Point end = group.get(group.size() - 1);
						// Calcula la pendiente de la línea
						double slope = (end.y - start.y) / (end.x - start.x);

						// Calcula el intercepto en y
						double yIntercept = start.y - slope * start.x;

						// Dibuja la línea píxel por píxel
						for (int x = (int) start.x; x <= end.x; x++) {
						    int y = (int) (slope * x + yIntercept);
						    double[] pixel = image.get(y, x);
						    if (pixel[0] == 255 && pixel[1] == 255 && pixel[2] == 255) {
						        // Si el píxel es blanco, detén el dibujo de la línea
						        break;
						    } else {
						        // Si el píxel no es blanco, dibuja un píxel verde en esa posición
						        image.put(y, x, new double[]{0, 255, 0});
						    }
						}
					}
				}

				// Eliminar inliers de píxeles negros
				Iterator<Point> iterator = blackPixels.iterator();
				while (iterator.hasNext()) {
					Point p = iterator.next();
					double distance = bestLine.distance(p.x, p.y);
					if (distance < distanceThreshold) {
						iterator.remove();
					}
				}
			}
		}

		// Seleccionar la carpeta destino para guardar la imagen lineas
		CarpetaDestino carpetaDestino = new CarpetaDestino();

		String rutaCarpetaDestino = carpetaDestino.selectCarpet();

		// Guardar la imagen lineas
		Imgcodecs.imwrite(rutaCarpetaDestino + "./Lineas.jpg", image);
	}

}

 class Line {
	private double a;
	private double b;

	public Line(double x1, double y1, double x2, double y2) {
		a = (y2 - y1) / (x2 - x1);
		b = y1 - a * x1;
	}

	public double getY(double x) {
		return a * x + b;
	}

	public double distance(double x, double y) {
		return Math.abs(a * x - y + b) / Math.sqrt(a * a + 1);
	}
}
