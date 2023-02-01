package utilities;


import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * The Class <code>Maths</code> contains useful maths method.
 * 
 */
public class Maths {

	/**
	 * Default constructor.
	 * 
	 * @throw {@link UnsupportedOperationException} if the constructor is
	 *        called.
	 */
	protected Maths() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Returns the minimal value in the given array.
	 * 
	 * @param array
	 *            a {@code int[]} array
	 * @return the minimal value in the given array.
	 * @throw {@link IllegalArgumentException} if the array is null.
	 */
	public static int minValueInArray(int[] array) {
		if (array == null) {
			throw new IllegalArgumentException("Array can't be null");
		}
		int min = Integer.MAX_VALUE;
		for (int i = 0; i < array.length; i++) {
			if (array[i] < min) {
				min = array[i];
			}
		}
		return min;
	}

	/**
	 * Returns the maximal value in the given array.
	 * 
	 * @param array
	 *            a {@code int[]} array
	 * @return the maximal value in the given array.
	 * @throw {@link IllegalArgumentException} if the array is null.
	 */
	public static int maxValueInArray(int[] array) {
		if (array == null) {
			throw new IllegalArgumentException("Array can't be null");
		}
		int max = -Integer.MAX_VALUE;
		for (int i = 0; i < array.length; i++) {
			if (array[i] > max) {
				max = array[i];
			}
		}
		return max;
	}

	/**
	 * Returns the maximal value in the given 2D array.
	 * 
	 * @param array
	 *            a {@code int[][]} array
	 * @return the maximal value in the given array.
	 * @throw {@link IllegalArgumentException} if the array is null.
	 */
	public static int maxValueIn2DArray(int[][] array) {
		if (array == null) {
			throw new IllegalArgumentException("Array can't be null");
		}
		int max = Integer.MIN_VALUE;
		for (int i = 0; i < array.length; i++) {
			for (int j = 0; j < array[i].length; j++) {
				if (array[i][j] > max) {
					max = array[i][j];
				}
			}
		}
		return max;
	}

	/**
	 * Rounds the number <code>a</code> with precision <code>n</code>.
	 * 
	 * @param a
	 *            the number to round.
	 * @param n
	 *            the precision.
	 * @return the number <code>a</code> rounded with precision <code>n</code>.
	 */
	public static double floor(final double a, final int n) {
		int p = (int) Math.pow(10.0, n);
		return Math.floor((a * p) + 0.5) / p;
	}

	public static double truncate(double x, int numberofDecimals) {
		if (x > 0) {
			return new BigDecimal(String.valueOf(x)).setScale(numberofDecimals, BigDecimal.ROUND_FLOOR).doubleValue();
		} else {
			return new BigDecimal(String.valueOf(x)).setScale(numberofDecimals, BigDecimal.ROUND_CEILING).doubleValue();
		}
	}

	/**
	 * Sorts the given <code>array</code> according to the value given in
	 * <code>valueToCompare</code>.
	 * 
	 * @param array
	 *            the array to sort.
	 * @param valueToCompare
	 *            the value to compare.
	 */
	public static void sortArray(int[] array, final int[] valueToCompare) {
		if (array.length != valueToCompare.length) {
			throw new IllegalArgumentException(array.length + " not equal to " + valueToCompare.length);
		}
		final Integer[] objectArray = Utilities.toObject(array);
		Arrays.sort(objectArray, new Comparator<Integer>() {
			@Override
			public int compare(Integer i, Integer j) {
				return ((Integer) valueToCompare[i]).compareTo(valueToCompare[j]);
			}
		});
		System.arraycopy(Utilities.toPrimitive(objectArray), 0, array, 0, array.length);
	}

	/**
	 * Sorts the given <code>array</code>.
	 * 
	 * @param array
	 *            the array to sort.
	 */
	public static void sortArray(double[] array) {
		final Double[] objectArray = Utilities.toObject(array);
		Arrays.sort(objectArray);
		System.arraycopy(Utilities.toPrimitive(objectArray), 0, array, 0, array.length);
	}

	/**
	 * Get the quartile of the given <code>array</code>.
	 * 
	 * @param array
	 *            an array already sorted.
	 * @param quartile
	 *            <code>true</code> if first quartile, <code>false</code> if top
	 *            quartile.
	 */
	public static double quartile(double[] array, boolean first) {
		if (first) {
			return array[(int) Math.min(array.length - 1, Math.ceil(array.length / 4.0))];
		} else {
			return array[(int) Math.floor(3 * array.length / 4.0)];
		}
	}

	/**
	 * Get the decile of the given <code>array</code>.
	 * 
	 * @param array
	 *            an array already sorted.
	 * @param decile
	 *            <code>true</code> if first decile, <code>false</code> if top
	 *            decile.
	 */
	public static double decile(double[] array, boolean first) {
		if (first) {
			return array[(int) Math.min(array.length - 1, Math.ceil(array.length / 10.0))];
		} else {
			return array[(int) Math.floor(9 * array.length / 10.0)];
		}
	}

	/**
	 * Get the mediane of the given <code>array</code>.
	 * 
	 * @param array
	 *            an array already sorted.
	 */
	public static double mediane(double[] array) {
		return array[(int) Math.min(array.length - 1, Math.ceil(array.length / 2.0))];
	}

	/**
	 * Multiply all array member of <code>children</code> by the given
	 * <code>number</code>.
	 * 
	 * @param children
	 *            an array.
	 * @param number
	 *            a number.
	 * @return an array where <code>array[i]=children[i]*number</code>.
	 * @throw {@link IllegalArgumentException} if <code>array=null</code>.
	 */
	public static int[] multiply(int[] children, int number) {
		if (children == null) {
			throw new IllegalArgumentException("the given array cannot be null");
		}
		int array[] = new int[children.length];
		for (int i = 0; i < children.length; i++) {
			array[i] = children[i] * number;
		}
		return array;
	}

	/**
	 * Sum the arrays <code>array1</code> and <code>array2</code>.
	 * 
	 * @param array1
	 *            an array.
	 * @param array2
	 *            another array.
	 * @return an array where <code>array[i]=array1[i]+array2[i]</code>.
	 * @throw {@link IllegalArgumentException} if <code>array1=null</code> or
	 *        <code>array2=null</code> or if the given arrays have not the same
	 *        length.
	 */
	public static int[] sum(int[] array1, int[] array2) {
		if (array1 == null || array2 == null) {
			throw new IllegalArgumentException("the given array cannot be null");
		}
		if (array1.length != array2.length) {
			throw new IllegalArgumentException(
					"the given array have not the same length " + array1.length + "/" + array2.length);

		}
		int array[] = new int[array1.length];
		for (int i = 0; i < array.length; i++) {
			array[i] = array1[i] + array2[i];
		}
		return array;
	}

	/**
	 * Checks if the array <code>array1</code> is superior or equal to
	 * <code>array2</code>.
	 * 
	 * @param array1
	 *            an array.
	 * @param array2
	 *            another array.
	 * @return <code>true</code> if <code>array1[i]</code> is superior or equal
	 *         to <code>array2[i]</code> for all i, otherwise <code>false</code>
	 *         .
	 * @throw {@link IllegalArgumentException} if <code>array1=null</code> or
	 *        <code>array2=null</code> or if the given arrays have not the same
	 *        length.
	 */
	public static boolean superiorOrEqual(int[] array1, int[] array2) {
		if (array1 == null || array2 == null) {
			throw new IllegalArgumentException("the given array cannot be null");
		}
		if (array1.length != array2.length) {
			throw new IllegalArgumentException("the given array have not the same length");
		}
		int array[] = new int[array1.length];
		for (int i = 0; i < array.length; i++) {
			if (array1[i] < array2[i]) {
				return false;
			}
		}
		return true;
	}

	public static int[] roundArray(double[] array) {
		if (array == null) {
			throw new IllegalArgumentException("array can't be null");
		}
		int[] intArray = new int[array.length];
		for (int i = 0; i < array.length; i++) {
			intArray[i] = (int) Math.round(array[i]);
		}
		return intArray;
	}

	public static int[][] roundArray(double[][] array) {
		int[][] roundArray = new int[array.length][];
		for (int i = 0; i < array.length; i++) {
			roundArray[i] = new int[array[i].length];
			for (int j = 0; j < array[i].length; j++) {
				roundArray[i][j] = (int) Math.round(array[i][j]);
			}
		}
		return roundArray;
	}

	public static double[] roundArray(double[] array, int precision) {
		double[] roundArray = new double[array.length];
		for (int i = 0; i < array.length; i++) {
			roundArray[i] = Maths.floor(array[i], precision);
		}
		return roundArray;
	}

	public static double norme(double[] vecteur) {
		if (vecteur == null) {
			throw new IllegalArgumentException("the given vector cannot be null");
		}
		double norme = 0;
		for (int i = 0; i < vecteur.length; i++) {
			norme += vecteur[i] * vecteur[i];
		}
		return Math.sqrt(norme);
	}

	public static double getAverage(double[] array) {
		if (array == null) {
			throw new IllegalArgumentException("the given array cannot be null");
		}
		double sum = 0.0;
		for (double a : array) {
			sum += a;
		}
		int length = array.length;
		return sum / length;
	}

	public static double getVariance(double[] array) {
		if (array == null) {
			throw new IllegalArgumentException("the given array cannot be null");
		}
		double mean = getAverage(array);
		// System.out.println("average = " + mean);
		double temp = 0.0;
		int size = array.length;
		for (int j = 0; j < size; j++) {
			temp += (mean - array[j]) * (mean - array[j]);
		}
		return temp / size;
	}

	public static double getStdDev(double[] array) {
		if (array == null) {
			throw new IllegalArgumentException("the given array cannot be null");
		}
		// for (double a : array) {
		// System.out.print(a + ";");
		// }
		// System.out.println("");
		double var = getVariance(array);
		// System.out.println("variance = " + var);
		double stdDev = Math.sqrt(var);
		// System.out.println("std = " + stdDev);
		return stdDev;
	}

	public static int minValueIn2DArray(int[][] array) {
		if (array == null) {
			throw new IllegalArgumentException("Array can't be null");
		}
		int min = Integer.MAX_VALUE;
		for (int i = 0; i < array.length; i++) {
			for (int j = 0; j < array[i].length; j++) {
				if (array[i][j] < min) {
					min = array[i][j];
				}
			}
		}
		return min;
	}

	public static double max(double... n) {
		int i = 0;
		double max = n[i];
		while (++i < n.length)
			if (n[i] > max)
				max = n[i];

		return max;
	}

	public static int max(int... n) {
		int i = 0;
		int max = n[i];
		while (++i < n.length)
			if (n[i] > max)
				max = n[i];

		return max;
	}

	public static double min(double... n) {
		int i = 0;
		double min = n[i];
		while (++i < n.length)
			if (n[i] < min)
				min = n[i];

		return min;
	}

	public static int min(int... n) {
		int i = 0;
		int min = n[i];
		while (++i < n.length)
			if (n[i] < min)
				min = n[i];

		return min;
	}

	public static double sum(double[] array) {
		if (array == null) {
			throw new IllegalArgumentException("the given array cannot be null");
		}
		double sum = 0;
		for (double a : array) {
			sum += a;
		}
		return sum;
	}

	public static int sum(List<Integer> list) {
		int sum = 0;
		for (Integer e : list) {
			sum += e;
		}
		return sum;
	}

	public static int factorial(int n) {
		int fact = 1;
		for (int i = 1; i <= n; i++) {
			fact *= i;
		}
		return fact;
	}

	public static int combination(int k, int n) {
		return factorial(n) / (factorial(k) * factorial(n - k));
	}

	public static int permutation(int k, int n) {
		return factorial(n) / factorial(k);
	}

}


