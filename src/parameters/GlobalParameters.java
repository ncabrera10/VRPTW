package parameters;

/**
 * This class contains the global parameters
 * @author nicolas.cabrera-malik
 *
 */
public class GlobalParameters {

	//Relevant paths:

	/**
	 * Instances folder location
	 */
	public static final String INSTANCE_FOLDER = GlobalParametersReader.<String>get("INSTANCE_FOLDER", String.class);
	
	/**
	 * Results folder location
	 */
	public static final String RESULT_FOLDER = GlobalParametersReader.<String>get("RESULT_FOLDER", String.class);
	
	/**
	 * Solutions folder location
	 */
	public static final String SOLUTIONS_FOLDER = GlobalParametersReader.<String>get("SOLUTIONS_FOLDER", String.class);

	/**
	 * Auxiliary folder location
	 */
	public static final String AUXILIAR_FOLDER = GlobalParametersReader.<String>get("AUXILIAR_FOLDER", String.class);

	/**
	 * Precision for the code
	 */
	public static final int PRECISION = GlobalParametersReader.<Integer>get("PRECISION", Integer.class);
	
	/**
	 * Decimal precision for the code
	 */
	public static final double DECIMAL_PRECISION = Math.pow(10, -PRECISION);
	
	/**
	 * Seed (to allow for replication of the initialization step)
	 */

	public static int SEED = GlobalParametersReader.<Integer>get("SEED", Integer.class);
	
	/**
	 * Number of threads when running a cplex model
	 */

	public static int THREADS = GlobalParametersReader.<Integer>get("THREADS", Integer.class);
	
}
