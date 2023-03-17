package pricingAlgorithms;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;

import columnGeneration.Master;

//import columnGeneration.Master;

import dataStructures.DataHandler;
import dataStructures.GraphManager;
import dataStructures.Nglabel;
import ilog.concert.IloException;
import parameters.CGParameters;


/**
 * This is the main class to run the auxiliary problem. It coordinates the pulse.
 * @author nicolas cabrera
 *
 */
public class PricingProblem_Handler {

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
		
	//Labeling algorithm:
	
	/**
	 * Keeps track of labels id
	*/
	private static int labelCounter;
			

	/**
	* The ng labels queue
	*/
	private static ArrayList<Nglabel> labelsQueue_NG;
		
	/**
	 * This method creates an instance of a pulse handler
	 * @throws IloException 
	 * @throws InterruptedException 
	 */
	
	public PricingProblem_Handler(Hashtable<Integer,Double>dualsSub,double[] duals) throws IloException, InterruptedException {
		
		// Initialize key variables:
		
		setStop(false);
		setWasSolvedToOptimality(false);
		setTimeLimitPulse((double) CGParameters.TIME_LIMIT_PULSE_SEC);
		setPruneHarder(1);
		setPrimalBound(0);
		labelsQueue_NG = new ArrayList<Nglabel>();
		labelCounter = 0;
		
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
		
		// Solve the pricing problem:
		
		if(CGParameters.USE_LABELING_ALG) {
			
			runLabeling();
			
		}else {
			
			// Run bounding procedure
			
			runBoundingProcedure();
			
			// Runs the pulse algorithm
			
			runPulse();
			
			// Reset the nodes
			
			resetNodes();
			
			// Reset the graph manager 
			
			resetGraphManager();
		}
		
		
		
	}
	
	/**
	 * This method calculates the reduced cost of an arc
	 * @param duals
	 */
	public void updateReducedCosts(double[] duals) {
		
		for(int i = 0;i < DataHandler.numArcs;i++) {
			
			DataHandler.costList[i] = DataHandler.distList[i]-duals[DataHandler.arcs[i][0]]; //Calculate reduced cost with the dual variable of the tail node of each arc
			DataHandler.cost[DataHandler.arcs[i][0]][DataHandler.arcs[i][1]] = DataHandler.costList[i];
			DataHandler.sortList[i] = DataHandler.distList[i]-duals[DataHandler.arcs[i][0]]; //Use the dual variable of the tail node for sorting.
			
		}	
		
	}
	
	/**
	 * This method resets the nodes:
	 */
	
	public void resetNodes() {
		
		// Resets the indicator variable that allows for sorting the arcs based on the reduced cost
		
		for(int i = 0; i<=DataHandler.n ; i++) {
			
			GraphManager.nodes[i].firstTime = true;
			GraphManager.nodes[i].setNGlabelsList(new ArrayList<Nglabel>());
			
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
	
	// ----------------------------------Labeling methods----------------------------------------

	/**
	 * This method runs the pulse
	 * @param pG
	 * @param N
	 * @param MP
	 * @throws IloException
	 * @throws InterruptedException 
	 */
	public void runLabeling() throws IloException, InterruptedException {
		
		// Fill neighborhoods:
		
			fillNeighborhoods();
			
			// 1. Heuristic driving:
			
				// Run bounding procedure using the pulse algorithm to get some good bounds!
			
					runBoundingProcedure();
						
				// Run labeling algorithm with heuristic set of dominance rules
			
					this.runLabelingHeuristic();
					
					//2. Check if we should continue:
					
					boolean continu = false;
					int numberOfUnprocessedLabels = 0;
			
					//3. If we haven't found any paths, we try to solve it exactly:
					
					if(numPaths == 0) {
						
						// Run bounding procedure using the pulse algorithm to get some good bounds!
						
							runBoundingProcedure();
						
						// 3. Run the labeling algorithm with the exact set of dominance rules:
						
							numberOfUnprocessedLabels = this.runLabelingExact();
							
							continu = true;
							
							if(numberOfUnprocessedLabels == 0 && continu) {
								wasSolvedToOptimality = true;
							}
							
						}

		// Empty the ng neighborhoods:
					
		emptyNeighborhoods();
		
	}
	
	/**
	 * This method runs the labeling algorithm with the ng-paths relaxation
	 * @throws IloException
	 * @throws InterruptedException 
	 */
	public void runLabelingHeuristic() throws IloException, InterruptedException {
		
		// Run the labeling algorithm
		
			setITime((double) System.nanoTime());

		//1.  Initialize a pool of labels
		
			labelsQueue_NG = new ArrayList<Nglabel>();
			labelCounter = 1;
			numPaths = 0;
		
		//2. Create the first label at the source node:
		
			// Counter of the number of times a subset has been visited:
			
				Hashtable<Integer,Integer> numTimesIneq = new Hashtable<Integer,Integer>();
				Enumeration<Integer> e = Master.getDuals_subset().keys();
				while (e.hasMoreElements()) {
					int key = e.nextElement();
					numTimesIneq.put(key, 0);
				}
			
			// Creates the label
				
				Nglabel label = new Nglabel(0.0,0.0,0.0,0.0,-1,GraphManager.nodes[0],labelCounter,null,0,numTimesIneq);
				PricingProblem_Handler.setLabelCounter(PricingProblem_Handler.getLabelCounter()+1);
				
		//3. Try to expand the initial label
				
				GraphManager.nodes[0].expandLabelHeuristic(label);
	
				
		//4. Explore the unprocessed labels:
			
			int numberOfUnprocessedLabels = labelsQueue_NG.size();

			int numIter = 0;
			while(numberOfUnprocessedLabels > 0 && getNumPaths() < CGParameters.MAX_PATHS_PER_ITERATION && numIter < Integer.MAX_VALUE) {
				numIter++;
				// Pop the first label:
			
				Nglabel actLabel = labelsQueue_NG.remove(numberOfUnprocessedLabels - 1);
				actLabel.setNotTreated(false);
				
				
				// Try to generate new labels from that node (using the info of the current label):
			
					actLabel.getNode().expandLabelHeuristic(actLabel);
					
				// Update the number of labels that remain:
				
				numberOfUnprocessedLabels = labelsQueue_NG.size();
			}
			
			// Reset the nodes
			
			resetNodes();
							
			// Reset the graph manager 
							
			resetGraphManager();
				
	}
	
	/**
	 * This method runs the labeling algorithm with the ng-paths relaxation
	 * @throws IloException
	 * @throws InterruptedException 
	 */
	public int runLabelingExact() throws IloException, InterruptedException {
		
		// Run the labeling algorithm
		
			setITime((double) System.nanoTime());

		//1.  Initialize a pool of labels
		
			labelsQueue_NG = new ArrayList<Nglabel>();
			labelCounter = 1;
			numPaths = 0;
		
		//2. Create the first label at the source node:
		
			// Counter of the number of times a subset has been visited:
			
				Hashtable<Integer,Integer> numTimesIneq = new Hashtable<Integer,Integer>();
				Enumeration<Integer> e = Master.getDuals_subset().keys();
				while (e.hasMoreElements()) {
					int key = e.nextElement();
					numTimesIneq.put(key, 0);
				}
			
			// Creates the label
				
				Nglabel label = new Nglabel(0.0,0.0,0.0,0.0,-1,GraphManager.nodes[0],labelCounter,null,0,numTimesIneq);
				PricingProblem_Handler.setLabelCounter(PricingProblem_Handler.getLabelCounter()+1);
				
		//3. Try to expand the initial label
				
				GraphManager.nodes[0].expandLabelExact(label);
			
		//4. Explore the unprocessed labels:
			
			int numberOfUnprocessedLabels = labelsQueue_NG.size();
			int numIter = 0;
			
			while(numberOfUnprocessedLabels > 0 && getNumPaths() < CGParameters.MAX_PATHS_PER_ITERATION && numIter < Integer.MAX_VALUE) {
				numIter++;
				
				// Pop the first label:
			
				Nglabel actLabel = labelsQueue_NG.remove(numberOfUnprocessedLabels - 1);
				actLabel.setNotTreated(false);
				
				// Try to generate new labels from that node (using the info of the current label):
			
					actLabel.getNode().expandLabelExact(actLabel);
					
				// Update the number of labels that remain:
				
				numberOfUnprocessedLabels = labelsQueue_NG.size();
			}
			
			// Reset the nodes
			
			resetNodes();
							
			// Reset the graph manager 
							
			resetGraphManager();
			
		// Return the number of unprocessed labels:
		
		return(numberOfUnprocessedLabels);
	}
	
	/**
	 * This method searches for an specific label
	 * @param p
	 * @param labels
	 * @return
	 */
	public static int binarySearch_NG(Nglabel p, ArrayList<Nglabel> labels) {
		double cScore = p.getSortCriteria();
		boolean cond = true;
		int l = 0; //izq
		int r = labels.size()-1; //der
		int m = (int) ((l + r) / 2); //medio
		double mVal = 0;
		if(labels.size() == 1){
			return 0;
		}else{
			mVal = labels.get(m).getSortCriteria();
		}
		while (cond) {
			if (r - l > 1) {
				if (cScore > mVal) {
					r = m;
					m = (int) ((l + r) / 2);
				} else if (cScore < mVal) {
					l = m;
					m = (int) ((l + r) / 2);
				} else if (p.getNode().id>labels.get(m).getNode().id){
					r = m;
					m = (int) ((l + r) / 2);
				} else if (p.getNode().id<labels.get(m).getNode().id){
					l = m;
					m = (int) ((l + r) / 2);
				}  else if (p.getTotalTime()>labels.get(m).getTotalTime()){
					r = m;
					m = (int) ((l + r) / 2);
				} else if (p.getTotalTime()<labels.get(m).getTotalTime()){
					l = m;
					m = (int) ((l + r) / 2);
				}else if (p.getCost()>labels.get(m).getCost()){
					r = m;
					m = (int) ((l + r) / 2);
				} else if (p.getCost()<labels.get(m).getCost()){
					l = m;
					m = (int) ((l + r) / 2);
				}else if (p.getLoad()>labels.get(m).getLoad()){
					r = m;
					m = (int) ((l + r) / 2);
				} else if (p.getLoad()<labels.get(m).getLoad()){
					l = m;
					m = (int) ((l + r) / 2);
				}else {
					return m;
				}
				mVal = labels.get(m).getSortCriteria();
			} else {
				cond = false;
				if (p.equals(labels.get(r))){
					return r;
				}else if (p.equals(labels.get(l))){
					return l;
				}

			}
		}
		return -1;

	}
	
	/**
	 * This method adds a pending pulse to the queue in order
	 * @param p
	 * @param labels
	 */
	public static void addLabel_DOrder_NG(Nglabel p,ArrayList<Nglabel> pendingQueue){

		double cScore = p.getSortCriteria();
		boolean cond = true;
		int l = 0; //Por izquierda
		int r = pendingQueue.size(); //Por derecha
		int m = (int) ((l + r) / 2); //La mitad
		double mVal = 0;
		if(pendingQueue.size() == 0) {
			pendingQueue.add(p);
			return;
		}
		else if(pendingQueue.size()  == 1) {
			mVal = pendingQueue.get(m).getSortCriteria();
			if(cScore == mVal) {
				if(p.getNode() == pendingQueue.get(m).getNode()) {
					pendingQueue.add(p.getTotalTime()>pendingQueue.get(m).getTotalTime()?0:1,p);
				}
				else {
					pendingQueue.add(p.getNode().id>pendingQueue.get(m).getNode().id?0:1,p);
				}
				return;
			}else {
				pendingQueue.add(cScore>mVal?0:1,p);
				return;
			}
		}
		else {
			mVal = pendingQueue.get(m).getSortCriteria();
		}
		while(cond) {
			if (r - l > 1) {
				if (cScore > mVal) {
					r = m;
					m = (int) ((l + r) / 2);
				} else if (cScore < mVal) {
					l = m;
					m = (int) ((l + r) / 2);
				} else if (p.getNode().id>pendingQueue.get(m).getNode().id){
					r = m;
					m = (int) ((l + r) / 2);
				} else if (p.getNode().id<pendingQueue.get(m).getNode().id){
					l = m;
					m = (int) ((l + r) / 2);
				}  else if (p.getTotalTime()>pendingQueue.get(m).getTotalTime()){
					r = m;
					m = (int) ((l + r) / 2);
				} else if (p.getTotalTime()<pendingQueue.get(m).getTotalTime()){
					l = m;
					m = (int) ((l + r) / 2);
				}   else if (p.getCost()>pendingQueue.get(m).getCost()){
					r = m;
					m = (int) ((l + r) / 2);
				} else if (p.getCost()<pendingQueue.get(m).getCost()){
					l = m;
					m = (int) ((l + r) / 2);
				}  else if (p.getLoad()>pendingQueue.get(m).getLoad()){
					r = m;
					m = (int) ((l + r) / 2);
				} else if (p.getLoad()<pendingQueue.get(m).getLoad()){
					l = m;
					m = (int) ((l + r) / 2);
				}else {
					pendingQueue.add(m, p);
					return;
				}
				mVal = pendingQueue.get(m).getSortCriteria();
			} else {
				cond = false;
				if(l == m ){
					if (cScore == mVal){
						if(p.getNode()==pendingQueue.get(m).getNode()){
							pendingQueue.add(p.getTotalTime()>pendingQueue.get(m).getTotalTime()?l:l+1,p);
						}else{
							pendingQueue.add(p.getNode().id>pendingQueue.get(m).getNode().id?l:l+1,p);
						}						
					}else{
						pendingQueue.add(cScore>mVal?l:l+1,p);
					}
				}else if (r == m){
					if (cScore == mVal){
						if(p.getNode()==pendingQueue.get(m).getNode()){
							pendingQueue.add(p.getTotalTime()>pendingQueue.get(m).getTotalTime()?r:Math.min(r+1, pendingQueue.size()),p);
						}else{
							pendingQueue.add(p.getNode().id>pendingQueue.get(m).getNode().id?r:Math.min(r+1, pendingQueue.size()),p);
						}
					}else{
						pendingQueue.add(cScore>mVal?r:Math.min(r+1, pendingQueue.size()),p);
					}
				}else
				{
					System.err.println("LABEL, addLabel ");
				}
				return;
			}
			
			
		}
		
	}
	
	/**
	 * This method fills the N_i for each node: either the costumer or the source depot
	 */
	public void fillNeighborhoods() {
		
		for(int j = 0;j < DataHandler.n;j++) {
			int count = 0;
			GraphManager.nodes[j].autoSort();
			for(int i=0; i<GraphManager.nodes[j].magicIndex.size(); i++) {
				if(count < CGParameters.LABELING_NUM_NG_NEIGHBORS) {
					GraphManager.nodes[j].addNeighbor(DataHandler.arcs[GraphManager.nodes[j].magicIndex.get(i)][1]);
					count++;
				}
			}
		}
		
		
	}
	
	/**
	 * This method that clears the N_i for each node: either the costumer or the source depot
	 */
	public void emptyNeighborhoods() {
		
		for(int j=0;j<DataHandler.n;j++) {
			GraphManager.nodes[j].setNgNeighborhood(new HashSet<Integer>());
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
		PricingProblem_Handler.numVecesSubsetRowIneq = numVecesSubsetRowIneq;
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
		PricingProblem_Handler.numVecesSubsetRowIneqMT = numVecesSubsetRowIneqMT;
	}

	public static int getNumPaths() {
		return numPaths;
	}

	public static void setNumPaths(int numPaths) {
		PricingProblem_Handler.numPaths = numPaths;
	}

	public static boolean isStop() {
		return stop;
	}

	public static void setStop(boolean stop) {
		PricingProblem_Handler.stop = stop;
	}

	public static boolean isWasSolvedToOptimality() {
		return wasSolvedToOptimality;
	}

	public static void setWasSolvedToOptimality(boolean wasSolvedToOptimality) {
		PricingProblem_Handler.wasSolvedToOptimality = wasSolvedToOptimality;
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
		PricingProblem_Handler.pruneHarder = pruneHarder;
	}

	public static double getTimeLimitPulse() {
		return timeLimitPulse;
	}

	public static void setTimeLimitPulse(double timeLimitPulse) {
		PricingProblem_Handler.timeLimitPulse = timeLimitPulse;
	}

	public static double getPrimalBound() {
		return primalBound;
	}

	public static void setPrimalBound(double primalBound) {
		PricingProblem_Handler.primalBound = primalBound;
	}
	/**
	 * @return the labelCounter
	 */
	public static int getLabelCounter() {
		return labelCounter;
	}

	/**
	 * @param labelCounter the labelCounter to set
	 */
	public static void setLabelCounter(int labelCounter) {
		PricingProblem_Handler.labelCounter = labelCounter;
	}
	/**
	 * @return the labelsQueue_NG
	 */
	public static ArrayList<Nglabel> getLabelsQueue_NG() {
		return labelsQueue_NG;
	}

	/**
	 * @param labelsQueue_NG the labelsQueue_NG to set
	 */
	public static void setLabelsQueue_NG(ArrayList<Nglabel> labelsQueue_NG) {
		PricingProblem_Handler.labelsQueue_NG = labelsQueue_NG;
	}
}
