package dataStructures;

import java.util.ArrayList;
import java.util.Hashtable;

import parameters.CGParameters;
import parameters.GlobalParameters;
import pulseAlgorithm.PulseHandler;


/** This class represents the final node. It holds the pulse method override for the final node.


*/

public class FinalNode extends Node {

	/**
	 * Node id
	 */
	public int id;

	/**
	 * Node demand
	 */
	public int demand;
	
	/**
	 * Node service time
	 */
	public int service;
	
	/**
	 * Beginning of the time window
	 */
	public int tw_a;
	
	/**
	 * End of the time window
	 */
	public int tw_b;
	
	/**
	 * Array with the indexes of all outgoing arcs from the node
	 */
	public ArrayList<Integer> magicIndex; 
	
	/**
	 * Best solution found at any time of the exploration
	 */
	public ArrayList<Integer> Path;
	
	/**
	 * Best solution time
	 */
	public double PathTime;
	
	/**
	 * Best solution load
	 */
	public double PathLoad;
	
	/**
	 * Best solution cost
	 */
	public double PathCost;
	
	/**
	 * Best solution distance
	 */
	double PathDist;	
		
	// The current pool of paths found 
	
	public Hashtable<String, Double> routesPoolDist;
	public Hashtable<String, Double> routesPoolRC;
	public ArrayList<String> pool;
	
	/** Class constructor
	 * @param i Node id
	 * @param d Node demand
	 * @param s Node service time
	 * @param a Lower time window
	 * @param b Upper time window
	 */
	public FinalNode(int i, int d, int s , int a, int b) {
		super(i, 0, 0, a, b);
		id = i;
		demand = d;
		service = s;
		tw_a = a;
		tw_b = b;	
		magicIndex = new ArrayList<Integer>();
		Path= new ArrayList<Integer>();
		pool = new ArrayList<String>();
		routesPoolDist = new Hashtable<String, Double>();
		routesPoolRC  = new Hashtable<String, Double>();
		
	}
	
	/**
	 * This method resets the current pool
	 */
	public void reset() {
		pool = new ArrayList<String>();
		routesPoolDist = new Hashtable<String, Double>();
		routesPoolRC  = new Hashtable<String, Double>();
	}
	
	
	/**
	 * Pulse bound procedure
	 */
	public void pulseBound(double PLoad, double PTime, double PCost, ArrayList<Integer> path, int Root, double PDist) {
		
		// If the path is feasible update values for the bounding matrix and primal bound
		
		if (PLoad <= DataHandler.Q && (PTime) <= tw_b) {

			if ((PCost) < GraphManager.bestCost[Root]) {
				GraphManager.bestCost[Root] = (PCost);

				if (PCost < GraphManager.PrimalBound) {
					GraphManager.PrimalBound = PCost;

				}
				
			}
		
		}

	}
	
	/**
	 * Pulse multi-thread procedure
	 */
	public synchronized void pulseMT(double PLoad, double PTime, double PCost, ArrayList<Integer> path, double PDist, int thread) {
		
		// If the path is feasible and better than the best known solution update the best known solution and primal bound	
		
		if (PLoad <= DataHandler.Q && (PTime) <= tw_b) {
				
				if (PCost <= GraphManager.PrimalBound - Math.pow(10,-GlobalParameters.PRECISION)) {
					GraphManager.PrimalBound = PCost;
					this.PathTime = PTime;
					this.PathCost = PCost;
					this.PathLoad = PLoad;
					this.PathDist = PDist;
					this.Path.clear();
					for (int i = 0; i < path.size(); i++) {
						this.Path.add(path.get(i));
					}
					
					this.Path.add(id);
					
					if(PCost < 0) {
						
						String keyS = this.Path.toString();
						if(!routesPoolRC.containsKey(keyS)) {
							routesPoolRC.put(keyS,PCost);
							routesPoolDist.put(keyS,PDist);
							pool.add(keyS);
							PulseHandler.setPruneHarder(0);
							PulseHandler.setPrimalBound(PCost);
							PulseHandler.setNumPaths(PulseHandler.getNumPaths()+1);
							if(PulseHandler.getNumPaths() > CGParameters.MAX_PATHS_PER_ITERATION) {
								PulseHandler.setStop(true);
							}
						}
						
						
					}
					
					
					
				}
		

			}

		}
	
	/**
	 * Id of the node
	 */
	public int  getID()
	{
		return id;
	}
	
	/**
	 * Name of the node
	 */
	public String toString(){
		
		return id+"";
	}
	
	/**
	 * Clone of the object
	 */
	public Object clone() {
		return super.clone();
	}
	


}
