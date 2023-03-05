package columnGeneration;

import java.util.Hashtable;

import org.jorlib.frameworks.columnGeneration.model.ModelInterface;

import dataStructures.DataHandler;

/**
 * Define a CG model
 * @author nicolas.cabrera-malik
 *
 */

public final class VRPTW implements ModelInterface{

	/** Small class defining the project (dataModel) **/

	/**
	 * Current forbidden arcs
	 */
	public static Hashtable<String,Integer> forbiddenArcs;
	
	/**
	 * Current fixed arcs
	 */
	public static Hashtable<String,Integer> fixedArcs = new Hashtable<String,Integer>();
	
	/**
	 * Current removed arcs
	 */
	public static Hashtable<String,Integer> removedArcs = new Hashtable<String,Integer>();
	
	/**
	 * Number of inequalities added so far
	 */
	public static int numInequalities;
	
	/**
	 * Number of cuts added
	 */
	public static int numCutsAdded;
	
	/**
	 * Number of BAP nodes explored
	 */
	
	public static int numBAPnodes;
	
	/**
	 * Number of columns added so far
	 */
	public static int numColumns;
	
	/**
	 * Number of columns on the initialization step
	 */
	
	public static int numColumns_iniStep;
	
	/**
	 * Initial time for the BAP
	 */
	public static Double ITime = (double) System.nanoTime();
	
	/**
	 * Final time for the BAP
	 */
	public static Double FTime;
	
	/**
	 * Number of column generation iterations
	 */
	public static int cgIteration;
	
	/**
	 * Number of cuts added at a certain BAP node
	 */
	public static int cutIterationAtCurrentBAPNode; //How many times have we added cuts on this BAP node
	
	/**
	 * Current BAP node ID
	 */
	public static int bapNodeID;
	
	/**
	 * Father BAP node ID
	 */
	public static int bapParentNodeID;
	
	/**
	 * Current depth of the BAP node
	 */
	public static int bapDepthNode;

	/**
	 * Are we still working at the root node?
	 */
	public static boolean stillAtTheRootNode;
	
	/**
	 * Array that stores the number of times an arc has been forbidden in the current branch
	 */
	public static int[][] isForbidden = new int[DataHandler.n+1][DataHandler.n+1]; //Takes a value of 0 for allowed arcs 
	
	// Other important metrics:
	
	/**
	 * Number of times the pricing problem was solved by the pulse:
	 */
	
	public static int number_pricing_byPulse;
	
	/**
	 * Number of times the pricing problem was solved by the tabu search:
	 */
	
	public static int number_pricing_byTabu;
	
	/**
	 * Number of times the pricing problem was solved to optimality:
	 */
	public static int number_pricing_solvedToOptimality;
	
	/**
	 * Computational time spend on the master problem
	 */
	
	public static double time_on_master;
	
	/**
	 * Computational time spend on the pricing problem
	 */
	
	public static double time_on_pricing;
	
	/**
	 * Computational time used for solving the root node
	 */
	
	public static double time_on_root_node;
	
	public static double lb_on_root_node;
	
	@Override
	public String getName() {
		return "VRPTW";
	}
}
