package parameters;

/**
 * This class contains the main parameters of the BCP procedure
 * @author nicolas.cabrera-malik
 *
 */
public class CGParameters {
	
	// Actual configuration
	
		public static String CONFIGURATION;
	
	// General parameters:
	
		/**
		 * Should we print in console?
		 */
		
		public static boolean PRINT_IN_CONSOLE = CGParametersReader.<String>get("PRINT_IN_CONSOLE", String.class).equals("false") ? false:true;
		
	// Column generation iterations:
	
		/**
		 * Maximum number of paths to add at every iteration
		 */
		
		public static int MAX_PATHS_PER_ITERATION = CGParametersReader.<Integer>get("MAX_PATHS_PER_ITERATION", Integer.class);
		
		
	//Pulse algorithm parameters:
	
		/**
		 * Number of threads for the MT pulse
		 */
		public static int PULSE_NUM_THREADS = CGParametersReader.<Integer>get("PULSE_NUM_THREADS", Integer.class);
	
		/**
		 * Time limit to stop the exact pulse..(heuristically)
		 */
		
		public static double TIME_LIMIT_PULSE_SEC = CGParametersReader.<Double>get("TIME_LIMIT_PULSE_SEC", Double.class);
		
		/**
		 * Bound step for the bounding procedure of the pulse
		 */
		public static int BOUND_STEP_PULSE = CGParametersReader.<Integer>get("BOUND_STEP_PULSE", Integer.class);
		
		/**
		 * Lower time for the bounding procedure of the pulse
		 */
		public static int BOUND_LOWER_TIME_PULSE = CGParametersReader.<Integer>get("BOUND_LOWER_TIME_PULSE", Integer.class);


	// Branch and price:
	
		/**
		 * Time limit for the Branch and price procedure
		 */
		
		public static int BAP_TIME_LIMIT_SEC = CGParametersReader.<Integer>get("BAP_TIME_LIMIT_SEC", Integer.class);


	// Subset row inequalities parameters:

		/**
		 * Should we use the subset row inequalities cuts?
		 */
		public static boolean USE_SUBSET_ROW_INEQ = CGParametersReader.<String>get("USE_SUBSET_ROW_INEQ", String.class).equals("false") ? false:true;
		
		/**
		 * Maximum number of subset row inequalities per iteration
		 */
		
		public static int MAX_SUBSET_ROW_INEQ_PERITER = CGParametersReader.<Integer>get("MAX_SUBSET_ROW_INEQ_PERITER", Integer.class);
		
		/**
		 * Maximum number of subset row inequalities per customer to be added per iteration
		 */
		
		public static int MAX_SUBSET_ROW_INEQ_PERCUSTOMER = CGParametersReader.<Integer>get("MAX_SUBSET_ROW_INEQ_PERCUSTOMER", Integer.class);
		
		/**
		 * Minimum violation of the subset row inequalities
		 */
		
		public static double MIN_SUBSET_ROW_INEQ_VIOL = Double.parseDouble(CGParametersReader.<String>get("MIN_SUBSET_ROW_INEQ_VIOL", String.class));
		
		/**
		 * Maximum number of cuts on the root node
		 */
		
		public static int MAX_SUBSET_ROW_INEQ_ROOTNODE = CGParametersReader.<Integer>get("MAX_SUBSET_ROW_INEQ_ROOTNODE", Integer.class);
		
		
		// Parameters for the tabu search
		
		/**
		 * Maximum number of iteration for each call of the tabu search
		 */
		
		public static int MAX_ITERATIONS_TS = CGParametersReader.<Integer>get("MAX_ITERATIONS_TS", Integer.class);
		
		
		/**
		 * Maximum number of iterations in which we can use the tabu search
		 */
		
		public static int NUM_ITERATIONS_TS = CGParametersReader.<Integer>get("NUM_ITERATIONS_TS", Integer.class);
		
		/**
		 * Maximum number of paths to add at every iteration
		 */
		
		public static int MAX_PATHS_PER_ITERATION_TABU = CGParametersReader.<Integer>get("MAX_PATHS_PER_ITERATION_TABU", Integer.class);
		
		// Labeling algorithm 
		
		/**
		 * Boolean variable that indicates if the labeling algorithm should be used instead
		*/
				
		public static boolean USE_LABELING_ALG = CGParametersReader.<String>get("USE_LABELING_ALG", String.class).equals("false") ? false:true;
				
		/**
		* Maximum number of cuts on the root node
		*/
				
		public static int LABELING_NUM_NG_NEIGHBORS = CGParametersReader.<Integer>get("LABELING_NUM_NG_NEIGHBORS", Integer.class);
		
		
}
