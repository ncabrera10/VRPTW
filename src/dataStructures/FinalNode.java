package dataStructures;

import java.util.ArrayList;
import java.util.Hashtable;

import parameters.CGParameters;
import parameters.GlobalParameters;
import pricingAlgorithms.PricingProblem_Handler;



/**
 * This class represents the final node. It holds the pulse method override for the final node.
 * Whenever the final node is reached, the method pulseBound or pulseMT are used.
 * @author nicolas.cabrera-malik
 *
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
	
	/**
	 * Distance of the paths found
	 */
	public Hashtable<Integer, Double> routesPoolDist;
	
	/**
	 * Reduced cost of the paths found
	 */
	public Hashtable<Integer, Double> routesPoolRC;
	
	/**
	 * Time of the paths found
	 */
	public Hashtable<Integer, Double> routesPoolTime;
	
	/**
	 * Load of the paths found
	 */
	public Hashtable<Integer, Double> routesPoolLoad;
	
	/**
	 * List that contains the paths in string format
	 */
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
		routesPoolDist = new Hashtable<Integer, Double>();
		routesPoolRC  = new Hashtable<Integer, Double>();
		routesPoolTime = new Hashtable<Integer, Double>();
		routesPoolLoad = new Hashtable<Integer, Double>();
	}
	
	/**
	 * This method resets the current pool
	 */
	public void reset() {
		pool = new ArrayList<String>();
		routesPoolDist = new Hashtable<Integer, Double>();
		routesPoolRC  = new Hashtable<Integer, Double>();
		routesPoolTime = new Hashtable<Integer, Double>();
		routesPoolLoad = new Hashtable<Integer, Double>();
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
					int id_path = PricingProblem_Handler.getNumPaths();
					if(PCost < 0) {
						String keyS = this.Path.toString();
						
						routesPoolRC.put(id_path,PCost);
						routesPoolDist.put(id_path,PDist);
						routesPoolTime.put(id_path,PTime);
						routesPoolLoad.put(id_path,PLoad);
						pool.add(keyS);
						PricingProblem_Handler.setPruneHarder(0);
						PricingProblem_Handler.setPrimalBound(PCost);
						PricingProblem_Handler.setNumPaths(PricingProblem_Handler.getNumPaths()+1);
						if(PricingProblem_Handler.getNumPaths() > CGParameters.MAX_PATHS_PER_ITERATION) {
							PricingProblem_Handler.setStop(true);
						}
						
						
						
					}
					
					
					
				}
		

			}

		}
	
	@Override
	public void expandLabelExact(Nglabel L) {
		
		//We check if this path has a lower reduced cost than the primal bound:
		
		if(L.getReducedCost() < PricingProblem_Handler.getPrimalBound()-Math.pow(10,-GlobalParameters.PRECISION)) {
			
			PricingProblem_Handler.setPrimalBound(L.getReducedCost());
			
			//We check if the path has a negative reduced cost under the original dual variables:
			
			if(L.getReducedCost() < 0) {
				
				this.recoverPath(L);
				
			}
		}
		
	}

	@Override
	public void expandLabelHeuristic(Nglabel L) {
		
		//We check if this path has a lower reduced cost than the primal bound:
		
		if(L.getReducedCost() < PricingProblem_Handler.getPrimalBound()-Math.pow(10,-GlobalParameters.PRECISION)) {
			
			PricingProblem_Handler.setPrimalBound(L.getReducedCost());
			
			//We check if the path has a negative reduced cost under the original dual variables:
			
			if(L.getReducedCost() < 0) {
				
				this.recoverPath(L);
				
			}
		}
	}
	
	/**
	 * This method adds a path to the list of paths with negative reduced cost
	 * @param L
	 */
	public void recoverPath(Nglabel L) {
	
		int numBacks = 0;
		boolean continuar = true;
		Nglabel cambiable = L;

		ArrayList<Integer> path = new ArrayList<Integer>();
		while(continuar && numBacks <= 1000) {
			
			int predLabelID = cambiable.getPredecessor();
			boolean encontrado = false;
			for(int i=0;i<cambiable.getPredecessorNode().getNGlabelsList().size() && !encontrado;i++) {
				Nglabel Lp = cambiable.getPredecessorNode().getNGlabelsList().get(i);
				if(Lp.getId() == predLabelID) {
					path.add(Lp.getNode().id);
					encontrado = true;
					cambiable = Lp;
					if(Lp.getPredecessor() == -1) {
						continuar = false;
					}
				}
				
			}
			numBacks++;
	
		}

		// Build the path (Reverse it):
		
		this.Path.clear();
		
		this.Path.add(id);
		
		for(int i=path.size()-1;i>=0;i--) {
			this.Path.add(path.get(i));
		}
		this.Path.add(id);
		int id_path = PricingProblem_Handler.getNumPaths();
		String keyS = this.Path.toString();
		
		
		routesPoolRC.put(id_path,L.getReducedCost());
		routesPoolDist.put(id_path,L.getCost());
		routesPoolTime.put(id_path,L.getTotalTime());
		routesPoolLoad.put(id_path,L.getLoad());
		pool.add(keyS);
		PricingProblem_Handler.setPruneHarder(0);
		PricingProblem_Handler.setPrimalBound(L.getReducedCost());
		PricingProblem_Handler.setNumPaths(PricingProblem_Handler.getNumPaths()+1);
	
	
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
