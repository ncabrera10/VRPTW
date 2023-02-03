package dataStructures;

import columnGeneration.VRPTW;

public class GraphManager {

	/**
	 * Array of nodes
	 */
	public static Node[] nodes;
	
	/**
	 * Binary indicator for detecting cycles in the bounding stage
	 */
	public static int[] visited;
	
	/**
	 * Binary indicator for detecting cycles one for each thread
	 */
	public static int[][] visitedMT;
	
	/**
	 *  Primal bound updated through the execution of the algorithm
	 */
	public static double PrimalBound;
	
	/**
	 * Naive dual bound 
	 */
	public static double naiveDualBound;
	
	/**
	 * Bounds matrix
	 */
	public static double[][] boundsMatrix;
	public static int timeIndex;			// Last index for the bounds matrix
	
	/**
	 * Best cost found for each node at each iteration of the bounding stage
	 */
	public static double[] bestCost;
	
	/**
	 * Overall best cost found at each iteration of the bounding stage
	 */
	public static double overallBestCost;
	
	/**
	 * Time incumbent for the bounding stage
	 */
	public static double timeIncumbent;
	
	/**
	 * The final node overrides the class node and is different because it stops the recursion
	 */
	
	public static FinalNode finalNode;
		
	// Class constructor
	public GraphManager( int numNodes) {
		
		nodes = new Node[numNodes];
		visited = new int[numNodes];
		visitedMT = new int[numNodes][DataHandler.numThreads+1];
		boundsMatrix= new double [numNodes][(int) Math.ceil((double)DataHandler.tw_b[0]/DataHandler.boundStep)+1];

		bestCost= new double [numNodes];
		for(int i=1; i<numNodes; i++){
			bestCost[i]=Double.POSITIVE_INFINITY;
		}
		
		PrimalBound= 0;
		overallBestCost=0;
		finalNode = new FinalNode(numNodes, 0, 0, 0, DataHandler.tw_b[0]);
		
	}

	// This method adds a vertex to the graph
	public boolean addVertex(Node v) {
		nodes[v.getID()] = v;
		return true;
	}
	
	// This method returns the array of nodes
	public Node[] getNodes(){
		return nodes;
	}
	
	//This method finds the best arc regarding the cost/time ratio
	public static void  calNaiveDualBound() {
		if(VRPTW.numInequalities == 0) {

			GraphManager.naiveDualBound=Double.POSITIVE_INFINITY;
			for (int i = 0; i < DataHandler.numArcs; i++) {
				if(DataHandler.timeList[i]!=0 && DataHandler.costList[i]/DataHandler.timeList[i]<=GraphManager.naiveDualBound ){
					GraphManager.naiveDualBound=DataHandler.costList[i]/DataHandler.timeList[i];
					}
			}
		}
		else {
			GraphManager.naiveDualBound=-Double.POSITIVE_INFINITY;
		}
	}
}
