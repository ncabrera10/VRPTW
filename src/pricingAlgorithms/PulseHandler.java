package pricingAlgorithms;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

//import columnGeneration.Master;

import dataStructures.DataHandler;
import dataStructures.GraphManager;
import ilog.concert.IloException;
import parameters.CGParameters;


/**
 * This is the main class to run the auxiliary problem. It coordinates the pulse.
 * @author nicolas cabrera
 *
 */
public class PulseHandler {

	/**
	 * Primal bound
	 */
	
	private static double primalBound;
	
	/**
	 * Number of paths found so far in a given iteration of the pricing algorithm
	 */
	
	private static int numPaths;
	
	/**
	 * Boolean indicator: can we stop the pulse now? (Heuristically)
	 */
	
	private static boolean stop;
	
	/**
	 * Boolean indicator: was the pricing algorithm solved to optimality?
	 */
	
	private static boolean wasSolvedToOptimality;
	
	/**
	 * Store the initial time:
	 */
	
	private static double ITime;
	
	/**
	 * Boolean variable, that allows to start pruning harder (while solving the pulse heuristically)
	 * 0: Prunes more paths (heuristically)
	 * 1: normal behavior of the pulse
	 */
	
	private static int pruneHarder;
	
	/**
	 * Pulse time limit in seconds (while solving the pulse heuristically)
	 */
	
	private static double timeLimitPulse;
	
	
	/**
	 * Number of times a certain triplet of customers has been visited
	 */
	
	private static Hashtable<Integer,Integer> numVecesSubsetRowIneq;
	
	/**
	 * Number of times a certain triplet of customers has been visited (MT version)
	 */
	private static Hashtable<String,Integer> numVecesSubsetRowIneqMT;	
		
	/**
	 * This method creates an instance of a pulse handler
	 * @throws IloException 
	 * @throws InterruptedException 
	 */
	
	public PulseHandler(Hashtable<Integer,Double>dualsSub,double[] duals) throws IloException, InterruptedException {
		
		// Initialize key variables:
		
		setStop(false);
		setWasSolvedToOptimality(false);
		setTimeLimitPulse((double) CGParameters.TIME_LIMIT_PULSE_SEC);
		setPruneHarder(1);
		setPrimalBound(0);
		
		// Subset row inequalities:
		
		numVecesSubsetRowIneq = new Hashtable<Integer,Integer>();
		numVecesSubsetRowIneqMT = new Hashtable<String,Integer>();
					
		// For the cuts:
		
		Enumeration<Integer> e = dualsSub.keys();

        while (e.hasMoreElements()) {
 
            int key = e.nextElement();
            numVecesSubsetRowIneq.put(key, 0);
            for(int i=0;i<DataHandler.threads.length;i++) {
            	numVecesSubsetRowIneqMT.put(key+"-"+i, 0);
            }
            
        }
        
		// Reset the number of paths
		
		setNumPaths(0);
		
		// Update arc reduced costs
		
		updateReducedCosts(duals);
		
		// Resets the final node:
		
		GraphManager.finalNode.reset();
		
		// Run bounding procedure
		
		runBoundingProcedure();
		
		// Runs the pulse algorithm
		
		runPulse();
		
		// Reset the nodes
		
		resetNodes();
		
		// Reset the graph manager 
		
		resetGraphManager();
		
	}
	
	/**
	 * This method calculates the reduced cost of an arc
	 * @param duals
	 */
	public void updateReducedCosts(double[] duals) {
		
		for(int i = 0;i < DataHandler.numArcs;i++) {
			
			DataHandler.costList[i] = DataHandler.distList[i]-duals[DataHandler.arcs[i][0]]; //Calculate reduced cost with the dual variable of the tail node of each arc
			DataHandler.cost[DataHandler.arcs[i][0]][DataHandler.arcs[i][1]] = DataHandler.costList[i];
			
		}	
		
	}
	
	/**
	 * This method resets the nodes:
	 */
	
	public void resetNodes() {
		
		// Resets the indicator variable that allows for sorting the arcs based on the reduced cost
		
		for(int i = 0; i<DataHandler.n ; i++) {
			
			GraphManager.nodes[i].firstTime = true;
			
		}
		
		// Resets the key variables for heuristic pruning
		
		setStop(false);
		setPruneHarder(1);
	
	}
	
	/**
	 * This method resets the graph manager key attributes
	 */
	public void resetGraphManager() {
		
		GraphManager.visited = new int[DataHandler.n+1];
		GraphManager.visitedMT = new int[DataHandler.n+1][DataHandler.numThreads+1];
		GraphManager.boundsMatrix = new double [DataHandler.n+1][(int) Math.ceil((double)DataHandler.tw_b[0]/DataHandler.boundStep)+1];
		GraphManager.bestCost = new double [DataHandler.n+1];
		for(int i=1; i<DataHandler.n+1; i++){
			GraphManager.bestCost[i] = Double.POSITIVE_INFINITY;
		}
		
		GraphManager.PrimalBound = 0;
		GraphManager.overallBestCost = 0;
	}
	
	//Pulse functions:
	
	/**
	 * This method runs the bounding procedure
	 * @throws IloException 
	 */
	public void runBoundingProcedure() throws IloException {

		// Calculates a naive dual bound based on the current network:
		
		GraphManager.calNaiveDualBound();									
		
		// Captures the depot upper time window:
		
		GraphManager.timeIncumbent = GraphManager.nodes[0].tw_b;				
		
		// Sets the lower time limit:
		
		int lowerTimeLimit = CGParameters.BOUND_LOWER_TIME_PULSE; 
		
		// Index to store the bounds:
		
		int timeIndex=0;												
		
		// While we have not reached the lower time limit:
		
		while(GraphManager.timeIncumbent >= lowerTimeLimit){
			
			// Calculate the current index:
			
			timeIndex=(int) Math.ceil((GraphManager.timeIncumbent/DataHandler.boundStep));		
			
			// Propagate a pulse from every node:
			
			for (int i = 1; i <= DataHandler.n; i++) {					
				GraphManager.nodes[i].pulseBound(0, GraphManager.timeIncumbent, 0 , new ArrayList<Integer>(), i,0); 	// Solve an ESPPRC for all nodes given the time incumbent 
			}
			
			// Store the best cost found for each node:
			
			for(int i=1; i<=DataHandler.n; i++){
				GraphManager.boundsMatrix[i][timeIndex] = GraphManager.bestCost[i];
			}
			
			// Store the best overall cost (considering all nodes): 
			
			GraphManager.overallBestCost = GraphManager.PrimalBound;	
			
			// Update the time incumbent:
			
			GraphManager.timeIncumbent -= DataHandler.boundStep;
			 
			// Update the last time index:
			
			GraphManager.timeIndex = timeIndex;
			
		}
		
		// Set the time incumbent to the last value:
		
		GraphManager.timeIncumbent += DataHandler.boundStep;
		
		// Resets the primal bound:
		
		GraphManager.PrimalBound = 0;
		
	}
	
	
	/**
	 * This method runs the pulse
	 * @param pG
	 * @param N
	 * @param MP
	 * @throws IloException
	 * @throws InterruptedException 
	 */
	public void runPulse() throws IloException, InterruptedException {
		
		// Starts the clock for the pulse:
		
		setITime((double) System.nanoTime());
		
		// Runs the pulse from the source node:
		
		GraphManager.nodes[0].pulseMT(0, 0, 0, new ArrayList<Integer>(),0,0); 
		
		// Checks if we solved the pulse to optimality or not:
		
		if(!stop) {
			wasSolvedToOptimality = true;
		}else {
			wasSolvedToOptimality = false;
		}
		
	}
	
	
	// ----------------------------------Auxiliary methods----------------------------------------

	
	/**
	 * @return the numVecesSubsetRowIneq
	 */
	public static Hashtable<Integer, Integer> getNumVecesSubsetRowIneq() {
		return numVecesSubsetRowIneq;
	}

	/**
	 * @param numVecesSubsetRowIneq the numVecesSubsetRowIneq to set
	 */
	public static void setNumVecesSubsetRowIneq(Hashtable<Integer, Integer> numVecesSubsetRowIneq) {
		PulseHandler.numVecesSubsetRowIneq = numVecesSubsetRowIneq;
	}

	/**
	 * @return the numVecesSubsetRowIneqMT
	 */
	public static Hashtable<String, Integer> getNumVecesSubsetRowIneqMT() {
		return numVecesSubsetRowIneqMT;
	}

	/**
	 * @param numVecesSubsetRowIneqMT the numVecesSubsetRowIneqMT to set
	 */
	public static void setNumVecesSubsetRowIneqMT(Hashtable<String, Integer> numVecesSubsetRowIneqMT) {
		PulseHandler.numVecesSubsetRowIneqMT = numVecesSubsetRowIneqMT;
	}

	public static int getNumPaths() {
		return numPaths;
	}

	public static void setNumPaths(int numPaths) {
		PulseHandler.numPaths = numPaths;
	}

	public static boolean isStop() {
		return stop;
	}

	public static void setStop(boolean stop) {
		PulseHandler.stop = stop;
	}

	public static boolean isWasSolvedToOptimality() {
		return wasSolvedToOptimality;
	}

	public static void setWasSolvedToOptimality(boolean wasSolvedToOptimality) {
		PulseHandler.wasSolvedToOptimality = wasSolvedToOptimality;
	}

	public static double getITime() {
		return ITime;
	}

	public static void setITime(double iTime) {
		ITime = iTime;
	}

	public static int getPruneHarder() {
		return pruneHarder;
	}

	public static void setPruneHarder(int pruneHarder) {
		PulseHandler.pruneHarder = pruneHarder;
	}

	public static double getTimeLimitPulse() {
		return timeLimitPulse;
	}

	public static void setTimeLimitPulse(double timeLimitPulse) {
		PulseHandler.timeLimitPulse = timeLimitPulse;
	}

	public static double getPrimalBound() {
		return primalBound;
	}

	public static void setPrimalBound(double primalBound) {
		PulseHandler.primalBound = primalBound;
	}
}
