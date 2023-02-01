package dataStructures;

import java.util.ArrayList;

import globalParameters.CGParameters;
import globalParameters.GlobalParameters;
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
	}
	
	/* Override for the bounding procedure
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
	
	/* Override for the pulse procedure
	 */
	public synchronized void pulseMT(double PLoad, double PTime, double PCost, ArrayList<Integer> path, double PDist, int thread) {
		// If the path is feasible and better than the best known solution update the best known solution and primal bound	
		//if(CGParameters.PRINT_IN_CONSOLE) {
		//	System.out.println(PLoad+" - "+PTime+" - "+PCost+" - "+path.toString()+" - "+PDist+" - "+thread+" - "+GraphManager.PrimalBound+" - "+PulseHandler.getNumPaths());
		//}
		
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
					//if(CGParameters.PRINT_IN_CONSOLE) {
					//	System.out.println(PLoad+" - "+PTime+" - "+PCost+" - "+path.toString()+" - "+PDist+" - "+thread+" - "+GraphManager.PrimalBound+" - "+PulseHandler.getNumPaths());
					//}
					PulseHandler.addPath(path, PCost, PDist);
					PulseHandler.setPruneHarder(0);
					PulseHandler.setPrimalBound(PCost);
					if(PulseHandler.getNumPaths() > CGParameters.MAX_PATHS_PER_ITERATION) {
						PulseHandler.setStop(true);
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
	@SuppressWarnings("unused")
	private void SortF(ArrayList<Double> set) {
		QSF(set, 0, set.size() - 1);
	}

	/**
	 * Put method
	 * @param e
	 * @param b
	 * @param t
	 * @return
	 */
	public int putEndNode(ArrayList<Double> e, int b, int t) {
		int i;
		int pivot;
		double pivotVal;
		double temp;

		pivot = b;
		pivotVal = e.get(pivot) ;
		for (i = b + 1; i <= t; i++) {
			if (  e.get(i) < pivotVal) {
				pivot++;
				temp = e.get(i);
				e.set(i, e.get(pivot));
				e.set(pivot,temp);
			}
		}
		temp =  e.get(b);
		e.set(b, e.get(pivot));
		e.set(pivot,temp);
		return pivot;
	}

	/**
	 * QSF
	 * @param e
	 * @param b
	 * @param t
	 */
	public void QSF(ArrayList<Double> e, int b, int t) {
		int pivot;
		if (b < t) {
			pivot = putEndNode(e, b, t);
			QSF(e, b, pivot - 1);
			QSF(e, pivot + 1, t);
		}
	}

}
