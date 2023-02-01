package utilities;

/**
 * Implements a simple euclidean distance calculator
 * 
 * @author Jorge E. Mendoza (dev@jorge-mendoza.com)
 * @version %I%, %G%
 * @since Jan 8, 2015
 *
 */
public class EuclideanCalculator {

	/**
	 * Computes the Euclidean distance between two points
	 * 
	 * @param cx1
	 *            the coordinates on the x axis of point 1
	 * @param cy1
	 *            the coordinates on the y axis of point 1
	 * @param cx2
	 *            the coordinates on the x axis of point 2
	 * @param cy2
	 *            the coordinates on the y axis of point 2
	 * @return the Euclidean distance between the two points
	 */
	public static double calc(double cx1, double cy1, double cx2, double cy2) {
		return Math.sqrt(Math.pow((cx1 - cx2), 2) + Math.pow((cy1 - cy2), 2));
	}

	/**
	 * Computes a matrix of Euclidean distances from a coordinates matrix
	 * 
	 * @param coordinates
	 *            the coordinates
	 * @return the distance matrix
	 */
	public static double[][] calc(double[][] coordinates) {
		if (coordinates[0].length != 2)
			throw new IllegalArgumentException(
					"argument coordinates must be a matrix with 2 columns and an open number of files");

		double[][] matrix = new double[coordinates.length][coordinates.length];
		for (int i = 0; i < coordinates.length; i++) {
			for (int j = i + 1; j < coordinates.length; j++) {
				matrix[i][j] = calc(coordinates[i][0], coordinates[i][1], coordinates[j][0], coordinates[j][1]);
				matrix[j][i] = matrix[i][j];
			}
		}
		return matrix;
	}
	
}