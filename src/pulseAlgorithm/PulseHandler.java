package pulseAlgorithm;

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
 * @author nick0
 *
 */
public class PulseHandler {

	/**
	 * Primal bound
	 */
	
	private static double primalBound;
	
	/**
	 * Number of paths found so far
	 */
	
	private static int numPaths;
	
	/**
	 * Boolean indicator: can we stop the pulse now? (Heuristically)
	 */
	
	private static boolean stop;
	
	/**
	 * Boolean indicator: can we stop the pulse now? (Heuristically)
	 */
	
	private static boolean wasSolvedToOptimality;
	
	/**
	 * Store the initial time:
	 */
	
	private static double ITime;
	
	/**
	 * Time to prune harder: 0:prunes a lot more !
	 */
	
	private static int pruneHarder;
	
	/**
	 * Pulse time limit in seconds (if already found a solution with negative reduced cost)
	 */
	
	private static double timeLimitPulse;
	
	
	//For the subset row inequalities:
	
	private static Hashtable<Integer,Integer> numVecesSubsetRowIneq;
	private static Hashtable<String,Integer> numVecesSubsetRowIneqMT;	
		
	/**
	 * This method creates an instance of a pulse handler (The auxiliary problem)
	 * @throws IloException 
	 * @throws InterruptedException 
	 */
	
	public PulseHandler(Hashtable<Integer,Double>dualsSub,double[] duals) throws IloException, InterruptedException {
		
		// Initialize key variables:
		
		setStop(false);
		setWasSolvedToOptimality(false);
		setTimeLimitPulse((double) CGParameters.TIME_LIMIT_PULSE_SEC);//30.0;//30.0;//300.0 1.0; How much should we wait after finding the first solution with a negative reduced cost
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
		
		for(int i = 0; i<DataHandler.n ; i++) {
			
			GraphManager.nodes[i].firstTime = true;
			
		}
		
		setStop(false);
		setTimeLimitPulse((double) CGParameters.TIME_LIMIT_PULSE_SEC);//30.0;//30.0;//300.0 1.0; How much should we wait after finding the first solution with a negative reduced cost
		setPruneHarder(1);
	
	}
	
	/**
	 * This method resets the graph manager key attributes
	 */
	public void resetGraphManager() {
		
		GraphManager.visited = new int[DataHandler.n+1];
		GraphManager.visitedMT = new int[DataHandler.n+1][DataHandler.numThreads+1];
		GraphManager.boundsMatrix= new double [DataHandler.n+1][(int) Math.ceil((double)DataHandler.tw_b[0]/DataHandler.boundStep)+1];
		GraphManager.bestCost= new double [DataHandler.n+1];
		for(int i=1; i<DataHandler.n+1; i++){
			GraphManager.bestCost[i]=Double.POSITIVE_INFINITY;
		}
		
		GraphManager.PrimalBound= 0;
		GraphManager.overallBestCost=0;
	}
	
	//Pulse functions:
	
	/**
	 * This method runs the bounding procedure with or without walking arcs
	 * @param type 1: with walking arcs 2:without walking arcs
	 * @throws IloException 
	 */
	public void runBoundingProcedure() throws IloException {

		GraphManager.calNaiveDualBound();									// Calculate a naive lower bound
		GraphManager.timeIncumbent=GraphManager.nodes[0].tw_b;				// Capture the depot upper time window
		int lowerTimeLimit = CGParameters.BOUND_LOWER_TIME_PULSE; 											// Lower time (resource) limit to stop the bounding procedure. For 100-series we used 50 and for 200-series we used 100;		
		int timeIndex=0;													// Index to store the bounds
		
		while(GraphManager.timeIncumbent>=lowerTimeLimit){					// Check the termination condition
			
			timeIndex=(int) Math.ceil((GraphManager.timeIncumbent/DataHandler.boundStep));		// Calculate the current index
			
			for (int i = 1; i <= DataHandler.n; i++) {					
				GraphManager.nodes[i].pulseBound(0, GraphManager.timeIncumbent, 0 , new ArrayList<Integer>(), i,0); 	// Solve an ESPPRC for all nodes given the time incumbent 
			}
			
			for(int i=1; i<=DataHandler.n; i++){
				GraphManager.boundsMatrix[i][timeIndex]=GraphManager.bestCost[i];				// Store the best cost found for each node into the bounds matrix	
			}
			//System.out.println(timeIndex+" - "+GraphManager.timeIncumbent+" - "+GraphManager.PrimalBound);
			GraphManager.overallBestCost=GraphManager.PrimalBound;					// Store the best cost found over all the nodes
			GraphManager.timeIncumbent-=DataHandler.boundStep;						// Update the time incumbent
			GraphManager.timeIndex = timeIndex;										// Update the last index done
			
		}
		
		//System.out.println(GraphManager.PrimalBound);
		//System.exit(0);
		
		GraphManager.timeIncumbent+=DataHandler.boundStep; 				// Set time incumbent to the last value solved
		GraphManager.PrimalBound=0;										// Reset the primal bound
		
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
		
		setITime((double) System.nanoTime());
		GraphManager.nodes[0].pulseMT(0, 0, 0, new ArrayList<Integer>(),0,0); 	// Run the pulse procedure on the source node
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
