package utilities;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class Utilities {

	/**
	 * Convert an array into a string with a delimiter between each array
	 * values.
	 * 
	 * @param array
	 *            an array
	 * @param delimiter
	 *            the delimiter between each array values.
	 * @return the formatted string.
	 * @throw {@link IllegalArgumentException} if the array is null.
	 */
	public static <T> String arrayToString(T[] array, String delimiter) {
		if (array == null) {
			throw new IllegalArgumentException("Array can't be null");
		}
		StringBuilder sBuilder = new StringBuilder();
		for (int i = 0; i < array.length; i++) {
			sBuilder.append(array[i]);
			if (i < array.length - 1) {
				sBuilder.append(delimiter);
			}
		}
		return sBuilder.toString();
	}

	/**
	 * Convert an array into a string with a delimiter between each array
	 * values.
	 * 
	 * @param array
	 *            an array
	 * @param delimiter
	 *            the delimiter between each array values.
	 * @return the formatted string.
	 * @throw {@link IllegalArgumentException} if the array is null.
	 */
	public static String intArrayToString(int[] array, String delimiter) {
		if (array == null) {
			throw new IllegalArgumentException("Array can't be null");
		}
		StringBuilder sBuilder = new StringBuilder();
		for (int i = 0; i < array.length; i++) {
			sBuilder.append(array[i]);
			if (i < array.length - 1) {
				sBuilder.append(delimiter);
			}
		}
		return sBuilder.toString();
	}

	/**
	 * Convert an array into a string with a delimiter between each array
	 * values.
	 * 
	 * @param array
	 *            an array
	 * @param delimiter
	 *            the delimiter between each array values.
	 * @return the formatted string.
	 * @throw {@link IllegalArgumentException} if the array is null.
	 */
	public static String booleanArrayToString(boolean[] array, String delimiter) {
		if (array == null) {
			throw new IllegalArgumentException("Array can't be null");
		}
		StringBuilder sBuilder = new StringBuilder();
		for (int i = 0; i < array.length; i++) {
			sBuilder.append(array[i]);
			if (i < array.length - 1) {
				sBuilder.append(delimiter);
			}
		}
		return sBuilder.toString();
	}

	/**
	 * Convert an array into a string with a delimiter between each array
	 * values.
	 * 
	 * @param array
	 *            an array
	 * @param delimiter
	 *            the delimiter between each array values.
	 * @return the formatted string.
	 * @throw {@link IllegalArgumentException} if the array is null.
	 */
	public static String doubleArrayToString(double[] array, String delimiter) {
		if (array == null) {
			throw new IllegalArgumentException("Array can't be null");
		}
		StringBuilder sBuilder = new StringBuilder();
		for (int i = 0; i < array.length; i++) {
			sBuilder.append(array[i]);
			if (i < array.length - 1) {
				sBuilder.append(delimiter);
			}
		}
		return sBuilder.toString();
	}

	/**
	 * Convert an array into a string with a delimiter between each array
	 * values.
	 * 
	 * @param array
	 *            an array
	 * @param delimiter
	 *            the delimiter between each array values.
	 * @return the formatted string.
	 * @throw {@link IllegalArgumentException} if the array is null.
	 */
	public static String doubleArrayToString(double[] array, String delimiter, String unit) {
		if (array == null) {
			throw new IllegalArgumentException("Array can't be null");
		}
		StringBuilder sBuilder = new StringBuilder();
		for (int i = 0; i < array.length; i++) {
			sBuilder.append(array[i] + " " + unit);
			if (i < array.length - 1) {
				sBuilder.append(delimiter);
			}
		}
		return sBuilder.toString();
	}

	/**
	 * Computes all the subset of a set.
	 * 
	 * @param originalSet
	 *            the original set.
	 * @return all the computed subset of <code>originalSet</code>.
	 */
	public static <T> Set<Set<T>> powerSet(Set<T> originalSet) {
		Set<Set<T>> sets = new HashSet<>();
		if (originalSet.isEmpty()) {
			sets.add(new HashSet<T>());
			return sets;
		}
		List<T> list = new ArrayList<>(originalSet);
		T head = list.get(0);
		Set<T> rest = new HashSet<>(list.subList(1, list.size()));
		for (Set<T> set : powerSet(rest)) {
			Set<T> newSet = new HashSet<>();
			newSet.add(head);
			newSet.addAll(set);
			sets.add(newSet);
			sets.add(set);
		}
		return sets;
	}

	/**
	 * Computes all the subset of a set.
	 * 
	 * @param originalSet
	 *            the original set.
	 * @return all the computed subset of <code>originalSet</code>.
	 */
	public static <T> List<Set<T>> powerSetList(Set<T> originalSet, boolean minFirst, boolean empty) {
		List<Set<T>> setsList = new ArrayList<>();
		Set<Set<T>> sets = powerSet(originalSet);
		Iterator<Set<T>> it = sets.iterator();
		while (it.hasNext()) {
			Set<T> set = it.next();
			if (empty || !set.isEmpty()) {
				setsList.add(set);
			}
		}
		if (minFirst) {
			Collections.sort(setsList, new Comparator<Set<T>>() {
				@Override
				public int compare(Set<T> o1, Set<T> o2) {
					return o1.size() - o2.size();
				}

			});
		}
		return setsList;
	}

	public static int[] parseArrayInt(final String[] array) {
		int[] result = new int[array.length];
		for (int i = 0; i < array.length; i++) {
			result[i] = Integer.parseInt(array[i]);
		}
		return result;
	}

	public static Integer[] parseArrayIntObj(final String[] array) {
		Integer[] result = new Integer[array.length];
		for (int i = 0; i < array.length; i++) {
			result[i] = Integer.parseInt(array[i]);
		}
		return result;
	}

	public static boolean[] parseArrayBoolean(final String[] array) {
		boolean[] result = new boolean[array.length];
		for (int i = 0; i < array.length; i++) {
			result[i] = Boolean.parseBoolean(array[i]);
		}
		return result;
	}

	public static double[] parseArrayDouble(final String[] array) {
		double[] result = new double[array.length];
		for (int i = 0; i < array.length; i++)
			result[i] = Double.parseDouble(array[i]);
		return result;
	}

	public static Double[] parseArrayDoubleObj(final String[] array) {
		Double[] result = new Double[array.length];
		for (int i = 0; i < array.length; i++)
			result[i] = Double.parseDouble(array[i]);
		return result;
	}

	/**
	 * Convert a set into a string with a delimiter between each array values.
	 * 
	 * @param set
	 *            a set
	 * @param delimiter
	 *            the delimiter between each values.
	 * @return the formatted string.
	 * @throw {@link IllegalArgumentException} if the set is null.
	 */
	public static <T> String setToString(HashSet<T> set, String delimiter) {
		if (set == null) {
			throw new IllegalArgumentException("set can't be null");
		}
		StringBuilder sBuilder = new StringBuilder();
		Iterator<T> it = set.iterator();
		if (it.hasNext()) {
			sBuilder.append(it.next());
		}
		while (it.hasNext()) {
			sBuilder.append(delimiter);
			sBuilder.append(it.next());
		}
		return sBuilder.toString();
	}

	/**
	 * Gets a list of integer values between <code>a</code> and <code>b</code>.
	 * 
	 * @param a
	 *            an integer.
	 * @param b
	 *            another integer.
	 * @return a list of integer values between <code>a</code> and
	 *         <code>b</code>.
	 * @throw {@link IllegalArgumentException} if <code>b</code> strictly
	 *        inferior to <code>a</code>.
	 */
	public static List<Integer> listInteger(int a, int b) {
		if (b < a) {
			throw new IllegalArgumentException(a + " > " + b);
		}
		List<Integer> list = new ArrayList<>(b - a + 1);
		for (int n = a; n <= b; n++) {
			list.add(n);
		}
		return list;
	}

	/**
	 * Gets an array of integer values between <code>a</code> and <code>b</code>
	 * .
	 * 
	 * @param a
	 *            an integer.
	 * @param b
	 *            another integer.
	 * @return an array of integer values between <code>a</code> and
	 *         <code>b</code>.
	 * @throw {@link IllegalArgumentException} if <code>b</code> strictly
	 *        inferior to <code>a</code>.
	 */
	public static int[] arrayInteger(int a, int b) {
		if (b < a) {
			throw new IllegalArgumentException(a + " > " + b);
		}
		int[] array = new int[b - a + 1];
		int index = 0;
		for (int n = a; n <= b; n++) {
			array[index] = n;
			index++;
		}
		return array;
	}

	public static List<int[]> generatePerm(int[] array) {
		if (array.length == 0) {
			List<int[]> result = new ArrayList<>();
			return result;
		}
		int firstElement = array[0];
		List<int[]> returnValue = new ArrayList<>();
		List<int[]> permutations = generatePerm(array);
		for (int[] smallerPermutated : permutations) {
			for (int index = 0; index <= smallerPermutated.length; index++) {
				int[] temp = new int[array.length];
				System.arraycopy(smallerPermutated, 0, temp, 0, array.length);
				temp[index] = firstElement;
				returnValue.add(temp);
			}
		}
		return returnValue;
	}

	public static List<int[]> permuteWithRepetitions(int[] a, int k) {
		int n = a.length;
		int[] indexes = new int[k];
		int total = (int) Math.pow(n, k);
		List<int[]> pWR = new ArrayList<>();
		while (total-- > 0) {
			int[] snapshot = new int[k];
			for (int i = 0; i < k; i++) {
				snapshot[i] = a[indexes[i]];
			}
			pWR.add(snapshot);

			for (int i = 0; i < k; i++) {
				if (indexes[i] >= n - 1) {
					indexes[i] = 0;
				} else {
					indexes[i]++;
					break;
				}
			}
		}
		return pWR;
	}

	public static <T> Set<T> intersection(Set<T> set1, Set<T> set2) {
		Set<T> intersection = new HashSet<>(set1);
		Iterator<T> it = intersection.iterator();
		while (it.hasNext()) {
			T element = it.next();
			if (!set2.contains(element)) {
				it.remove();
			}
		}
		return intersection;
	}

	public static <T> List<List<T>> generatePerm(List<T> original) {
		if (original.size() == 0) {
			List<List<T>> result = new ArrayList<>();
			result.add(new ArrayList<T>());
			return result;
		}
		T firstElement = original.remove(0);
		List<List<T>> returnValue = new ArrayList<>();
		List<List<T>> permutations = generatePerm(original);
		for (List<T> smallerPermutated : permutations) {
			for (int index = 0; index <= smallerPermutated.size(); index++) {
				List<T> temp = new ArrayList<>(smallerPermutated);
				temp.add(index, firstElement);
				returnValue.add(temp);
			}
		}
		return returnValue;
	}

	public static <T> List<List<T>> generatePerm(Set<T> original) {
		if (original.size() == 0) {
			List<List<T>> result = new ArrayList<>();
			result.add(new ArrayList<T>());
			return result;
		}
		Iterator<T> it = original.iterator();
		T firstElement = it.next();
		it.remove();
		List<List<T>> returnValue = new ArrayList<>();
		List<List<T>> permutations = generatePerm(original);
		for (List<T> smallerPermutated : permutations) {
			for (int index = 0; index <= smallerPermutated.size(); index++) {
				List<T> temp = new ArrayList<>(smallerPermutated);
				temp.add(index, firstElement);
				returnValue.add(temp);
			}
		}
		return returnValue;
	}

	/**
	 * Converts an array of primitive <code>int</code> to an array of
	 * <code>Integer</code>.
	 * 
	 * @param array
	 *            an <code>int</code> array
	 * @return an <code>Integer</code> array, <code>null</code> if null array
	 *         input
	 */
	public static Integer[] toObject(int[] array) {
		if (array == null) {
			return null;
		}
		final Integer[] result = new Integer[array.length];
		for (int i = 0; i < array.length; i++) {
			result[i] = new Integer(array[i]);
		}
		return result;
	}

	/**
	 * Converts an array of primitive <code>int</code> to an array of
	 * <code>Integer</code>.
	 * 
	 * @param array
	 *            an <code>int</code> array
	 * @return an <code>Integer</code> array, <code>null</code> if null array
	 *         input
	 */
	public static Double[] toObject(double[] array) {
		if (array == null) {
			return null;
		}
		final Double[] result = new Double[array.length];
		for (int i = 0; i < array.length; i++) {
			result[i] = new Double(array[i]);
		}
		return result;
	}

	/**
	 * Converts an array of object Integers to primitives
	 * 
	 * @param array
	 *            a <code>Integer</code> array, may be <code>null</code>
	 * @return an <code>int</code> array, <code>null</code> if null array input
	 */
	public static int[] toPrimitive(Integer[] array) {
		if (array == null) {
			return null;
		}
		final int[] result = new int[array.length];
		for (int i = 0; i < array.length; i++) {
			result[i] = array[i].intValue();
		}
		return result;
	}

	/**
	 * Converts an array of object Double to primitives
	 * 
	 * @param array
	 *            a <code>Double</code> array, may be <code>null</code>
	 * @return an <code>double</code> array, <code>null</code> if null array
	 *         input
	 */
	public static double[] toPrimitive(Double[] array) {
		if (array == null) {
			return null;
		}
		final double[] result = new double[array.length];
		for (int i = 0; i < array.length; i++) {
			result[i] = array[i].doubleValue();
		}
		return result;
	}

	/**
	 * Converts an array of object Booleans to primitives
	 * 
	 * @param array
	 *            a <code>Boolean</code> array, may be <code>null</code>
	 * @return an <code>boolean</code> array, <code>null</code> if null array
	 *         input
	 */
	public static boolean[] toPrimitive(Boolean[] array) {
		if (array == null) {
			return null;
		}
		final boolean[] result = new boolean[array.length];
		for (int i = 0; i < array.length; i++) {
			result[i] = array[i].booleanValue();
		}
		return result;
	}

	public static boolean verifyTriangularInequality(double[][] array, int precision) {
		int length = array.length;
		double[][] newArray = new double[length][length];
		// initialize distances to infinity
		for (int v = 0; v < length; v++) {
			for (int w = 0; w < length; w++) {
				newArray[v][w] = Double.POSITIVE_INFINITY;
			}
		}

		// initialize distances using edge-weighted digraph's
		for (int v = 0; v < length; v++) {
			for (int v1 = 0; v1 < length; v1++) {
				newArray[v][v1] = array[v][v1];
			}
			// in case of self-loops
			if (newArray[v][v] >= 0.0) {
				newArray[v][v] = 0.0;
			}
		}

		// Floyd-Warshall updates
		for (int i = 0; i < length; i++) {
			// compute shortest paths using only 0, 1, ..., i as intermediate
			// vertices
			for (int v = 0; v < length; v++) {
				for (int w = 0; w < length; w++) {
					if (newArray[v][w] > newArray[v][i] + newArray[i][w]) {
						newArray[v][w] = newArray[v][i] + newArray[i][w];
					}
				}
				// check for negative cycle
				if (newArray[v][v] < 0.0) {
					throw new IllegalStateException("Negative cycle");
				}
			}
		}
		double precisionD = Math.pow(10, -precision);
		for (int i = 0; i < length; i++) {
			// compute shortest paths using only 0, 1, ..., i as intermediate
			// vertices
			for (int v = 0; v < length; v++) {
				if (Math.abs(newArray[i][v] - array[i][v]) > precisionD) {
					System.out.println("i=" + i + " j=" + v + " -> " + newArray[i][v] + " vs " + array[i][v]);
					return false;
				}
			}
		}
		return true;
	}

	public static boolean isArraySymmetric(double[][] array) {
		if (array == null) {
			return true;
		}
		for (int i = 0; i < array.length; i++) {
			for (int j = i + 1; i < array.length; i++) {
				if (array[i][j] != array[j][i]) {
					return false;
				}
			}
		}
		return true;

	}

	public static boolean isArraySymmetric(boolean[][] array) {
		if (array == null) {
			return true;
		}
		for (int i = 0; i < array.length; i++) {
			for (int j = i + 1; i < array.length; i++) {
				if (array[i][j] != array[j][i]) {
					return false;
				}
			}
		}
		return true;

	}

	public static double[][] clone(double[][] matrix) {
		double[][] matrixNew = matrix.clone();
		for (int i = 0; i < matrixNew.length; i++) {
			matrixNew[i] = matrix[i].clone();
		}
		return matrixNew;
	}

	public static <T> T[][] clone(T[][] matrix) {
		T[][] matrixNew = matrix.clone();
		for (int i = 0; i < matrixNew.length; i++) {
			matrixNew[i] = matrix[i].clone();
		}
		return matrixNew;
	}

}

