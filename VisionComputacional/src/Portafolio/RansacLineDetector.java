package Portafolio;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class RansacLineDetector {
	private int maxIterations;
	private double distanceThreshold;
	private int inlierThreshold;
	private int numLines;

	public RansacLineDetector(int maxIterations, double distanceThreshold, int inlierThreshold, int numLines) {
		this.maxIterations = maxIterations;
		this.distanceThreshold = distanceThreshold;
		this.inlierThreshold = inlierThreshold;
		this.numLines = numLines;
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
			for (int i = 0; i < maxIterations; i++) {
				// Select two random points
				Point p1 = blackPixels.get(random.nextInt(blackPixels.size()));
				Point p2 = blackPixels.get(random.nextInt(blackPixels.size()));

				// Calculate line equation
				Line line = new Line(p1.x, p1.y, p2.x, p2.y);

				// Count inliers
				int inliers = 0;
				for (Point p : blackPixels) {
					double distance = line.distance(p.x, p.y);
					if (distance < distanceThreshold) {
						inliers++;
					}
				}

				// Check if line is better than current best line
				if (inliers > bestInliers && inliers > inlierThreshold) {
					bestLine = line;
					bestInliers = inliers;
				}
			}

			// Draw best line on image
			if (bestLine != null) {
				Point start = new Point(0, bestLine.getY(0));
				Point end = new Point(image.width(), bestLine.getY(image.width()));
				Imgproc.line(image, start, end, new Scalar(0, 255, 0), 2);

				// Remove inliers from black pixels
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

	private class Line {
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
}