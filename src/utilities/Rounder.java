package utilities;

/**
 * This class is used to do fast roundings
 * @author nicolas.cabrera-malik
 *
 */
public class Rounder {
	
	/**
	 * Deviation 
	 */
	public static final double deviation = 0.0000000000;
	
	/**
	 * Method that rounds a value
	 * @param rounded
	 * @return
	 */
	public static double  round6Dec(double rounded) {
		return (Math.round(rounded*100)/100.0);
	}
}
