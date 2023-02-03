package dataStructures;

import java.util.ArrayList;

import columnGeneration.Master;
import columnGeneration.VRPTW;
import parameters.CGParameters;
import pulseAlgorithm.PulseHandler;


/** This class represents a node. It holds the pulse method and all the logic of the algorithm.

*/

public class Node implements Cloneable{

	/**
	 * Node number
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
	 *  boolean that indicates if the node is visited for first time
	 */
	public boolean firstTime=true;
		
	/**
	 * Subsets row inequalities id's in which the node is included
	 */
	
	private ArrayList<Integer> subsetRow_ids;
	

	/** Class constructor
	 * @param i Node number
	 * @param d Node demand
	 * @param s Node service time
	 * @param a Lower time window
	 * @param b Upper time window
	 */
	public Node(int i, int d, int s , int a, int b) {
		id = i;
		demand = d;
		service = s;
		tw_a = a;
		tw_b = b;	
		magicIndex = new ArrayList<>();
		subsetRow_ids = new ArrayList<Integer>();
	}
	
	/**
	 * Pulse function for the bounding stage
	 * @param pLoad current path load
	 * @param pTime current path time
	 * @param pCost current path cost
	 * @param path current path
	 * @param root current root node
	 * @param pDist current path distance
	 */
	public void pulseBound(double pLoad, double pTime, double pCost, ArrayList<Integer> path, int root, double pDist) {
		
		// If the node is visited for the first time, sort the outgoing arcs array 
		if(this.firstTime==true){
			this.firstTime=false;
			this.Sort(this.magicIndex);
		}
		
		// If the node is reached before the lower time window wait until the beginning of the time window
		if(pTime<this.tw_a){
			pTime=this.tw_a;
		}		
		
		// Try to prune pulses with the pruning strategies: cycles, infeasibility, bounds, and rollback
		
		if(GraphManager.visited[id]==0 && pTime <= tw_b && (pCost+calcBoundPhaseI(pTime,root))<GraphManager.bestCost[root] && !rollback(path,pCost,pTime)){
			// If the pulse is not pruned add it to the path
			GraphManager.visited[id] = 1;
			path.add(id);
			// Propagate the pulse through all the outgoing arcs
			for (int i = 0; i < magicIndex.size(); i++) {

				double newPLoad = 0;
				double newPTime = 0;
				double newPCost = 0;
				double newPDist = 0;
				int arcHead = DataHandler.arcs[magicIndex.get(i)][1];
				
				// Update all path attributes
				newPTime = (pTime+DataHandler.timeList[magicIndex.get(i)]);
				newPCost = (pCost + DataHandler.costList[magicIndex.get(i)]);
				newPLoad = (pLoad + DataHandler.loadList[magicIndex.get(i)]);
				newPDist = (pDist + DataHandler.distList[magicIndex.get(i)]);
				
				// Check feasibility and propagate pulse
				if (VRPTW.isForbidden[id][arcHead] == 0 && newPTime <= GraphManager.nodes[arcHead].tw_b
					&& newPLoad <= DataHandler.Q && newPTime <= GraphManager.nodes[0].tw_b) {
				// If the head of the arc is the final node, pulse the final node	
					if(arcHead==0){
						GraphManager.finalNode.pulseBound(newPLoad, newPTime, newPCost, path, root,newPDist);	
					}
					else{
						// Update cuts info
						
						for(Integer id_cut:GraphManager.nodes[arcHead].subsetRow_ids) {
							if(PulseHandler.getNumVecesSubsetRowIneq().containsKey(id_cut)) {
								PulseHandler.getNumVecesSubsetRowIneq().put(id_cut,PulseHandler.getNumVecesSubsetRowIneq().get(id_cut)+1);
								if(PulseHandler.getNumVecesSubsetRowIneq().get(id_cut) == 2) {
									newPCost = newPCost - Master.getDuals_subset().get(id_cut);
								}
							}
						}
						
						// Pulse the node
						
						GraphManager.nodes[arcHead].pulseBound(newPLoad, newPTime, newPCost, path, root ,newPDist);
					
						// Update cuts info 
						
						for(Integer id_cut:GraphManager.nodes[arcHead].subsetRow_ids) {
							if(PulseHandler.getNumVecesSubsetRowIneq().containsKey(id_cut)) {
								PulseHandler.getNumVecesSubsetRowIneq().put(id_cut,PulseHandler.getNumVecesSubsetRowIneq().get(id_cut)-1);
							}
						}
					}
				}

			}
			// Remove the explored node from the path
			path.remove((path.size() - 1));
			GraphManager.visited[id] = 0;

		}
	}

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////7
	


/** Multithread pulse function 
 * @param PLoad current load
 * @param PTime current time
 * @param PCost current cost
 * @param path current partial path
 * @param PDist current distance
 * @param thread current thread 
 * @throws InterruptedException
 */
public void pulseMT(double PLoad, double PTime, double PCost, ArrayList<Integer> path, double PDist, int thread) throws InterruptedException {

		// Check if we can stop the pulse heuristically:
	
		if(PulseHandler.getPruneHarder() == 0 && (System.nanoTime()-PulseHandler.getITime())/1000000000 > CGParameters.TIME_LIMIT_PULSE_SEC){
			PulseHandler.setStop(true);
		}
			
		// If the node is visited for the first time, sort the outgoing arcs array 
		if(this.firstTime==true){
			this.firstTime=false;
			this.Sort(this.magicIndex);
		}
		
		// If the node is reached before the lower time window wait until the beginning of the time window
		if(PTime<this.tw_a){
			PTime=this.tw_a;
		}
		// Try to prune pulses with the pruning strategies //Aca se podria hacer un ngpaths..TODO
		if(!PulseHandler.isStop() && (GraphManager.visitedMT[id][thread]==0 && (PCost+CalcBoundPhaseII(PTime))<GraphManager.PrimalBound && !rollback(path,PCost,PTime))){
			// If the pulse is not pruned add it to the path
			GraphManager.visitedMT[id][thread]=1;
			path.add(id);	
			// Propagate the pulse through all the outgoing arcs
			for(int i=0; i<magicIndex.size(); i++){
				
				double NewPLoad = 0;
				double NewPTime = 0;
				double NewPCost = 0;
				double NewPDist = 0;
				int Head = DataHandler.arcs[magicIndex.get(i)][1];

				// Update all path attributes
				NewPTime=(PTime+DataHandler.timeList[magicIndex.get(i)]);
				NewPCost=(PCost+DataHandler.costList[magicIndex.get(i)]);
				NewPLoad=(PLoad+DataHandler.loadList[magicIndex.get(i)]);
				NewPDist=(PDist+DataHandler.distList[magicIndex.get(i)]);
				// Check feasibility and propagate pulse
				if(VRPTW.isForbidden[id][Head] == 0 &&  NewPTime<=GraphManager.nodes[Head].tw_b && NewPLoad<=DataHandler.Q && NewPTime<=GraphManager.nodes[0].tw_b){
					
					// Update cuts info
					
					for(Integer id_cut:GraphManager.nodes[Head].subsetRow_ids) {
						if(PulseHandler.getNumVecesSubsetRowIneqMT().containsKey(id_cut+"-"+thread)) {
							PulseHandler.getNumVecesSubsetRowIneqMT().put(id_cut+"-"+thread,PulseHandler.getNumVecesSubsetRowIneqMT().get(id_cut+"-"+thread)+1);

							if(PulseHandler.getNumVecesSubsetRowIneqMT().get(id_cut+"-"+thread) == 2) {
								NewPCost = NewPCost - Master.getDuals_subset().get(id_cut);
							}
						}
					}
					
					// If the head of the arc is the final node, pulse the final node
					if (Head == 0) {
						GraphManager.finalNode.pulseMT(NewPLoad,NewPTime,NewPCost, path, NewPDist,thread);
					}else{
						// If not in the start node continue the exploration on the current thread 
						

						if(id!=0){
							
							// Pulse the head node
							
							GraphManager.nodes[Head].pulseMT(NewPLoad,NewPTime,NewPCost, path, NewPDist, thread);	
							
						
						}
						// If standing in the start node, wait for the next available thread to trigger the exploration
						else {
							boolean stopLooking = false;
							for (int j = 1; j < DataHandler.threads.length; j++) {
								if(!DataHandler.threads[j].isAlive()){
									DataHandler.threads[j] = new Thread(new PulseTask(Head, NewPLoad, NewPTime, NewPCost, path, NewPDist, j));
									DataHandler.threads[j].start();
									stopLooking = true;
									j = 1000;
								}
							}
							if (!stopLooking) {
								DataHandler.threads[1].join();
								DataHandler.threads[1] = new Thread(new PulseTask(Head, NewPLoad, NewPTime, NewPCost, path, NewPDist, 1));
								DataHandler.threads[1].start();
							}
						}
						
					}
					
					// Update cuts info
					
					for(Integer id_cut:GraphManager.nodes[Head].subsetRow_ids) {
						if(PulseHandler.getNumVecesSubsetRowIneqMT().containsKey(id_cut+"-"+thread)) {
							
							PulseHandler.getNumVecesSubsetRowIneqMT().put(id_cut+"-"+thread,PulseHandler.getNumVecesSubsetRowIneqMT().get(id_cut+"-"+thread)-1);
						}
					}
				}
				
			}
		// Wait for all active threads to finish	
		if(id==0){
			for (int k = 1; k < DataHandler.threads.length; k++) {
				DataHandler.threads[k].join();
				}
			}						
	
		// Remove the explored node from the path
		path.remove((path.size()-1));
		GraphManager.visitedMT[id][thread]=0;
	}
}


/** Rollback pruning strategy
 * @param path current partial path
 * @param pCost current cost
 * @param pTime current time
 * @return
 */
private boolean rollback(ArrayList<Integer> path, double pCost, double pTime) {
	// Can't use the strategy for the start node
	if(path.size()<=1){
		return false;
	}
	else{
		// Calculate the cost for the rollback pruning strategy 
		int prevNode = (int) path.get(path.size()-1);
		int directNode = (int) path.get(path.size()-2);
		double directCost = pCost-DataHandler.cost[prevNode][id]-DataHandler.cost[directNode][prevNode]+DataHandler.cost[directNode][id];
		//double directTime = pTime-DataHandler.distance[prevNode][id]-DataHandler.cost[directNode][prevNode]+DataHandler.cost[directNode][id];
		
		
		if(directCost<=pCost ){
			return true;
		}
	}

	return false;
}


/** This method calculates a lower bound given a time consumption at a given node
* @param time current time
* @param root current root node
* @return
*/
private double calcBoundPhaseI(double time, int root) {

double Bound=0;
// If the time consumed is less than the last time incumbent solved and the node id is larger than the current root node being explored it means that there is no lower bound available and we must use the naive bound 
if(time<GraphManager.timeIncumbent+DataHandler.boundStep && this.id>=root){
	Bound=((GraphManager.timeIncumbent+DataHandler.boundStep-time)*GraphManager.naiveDualBound+GraphManager.overallBestCost);
	
}

else {
// Else use the available bound	
	int Index=((int) Math.floor((time-(GraphManager.timeIncumbent+DataHandler.boundStep))/DataHandler.boundStep))+GraphManager.timeIndex;
	Bound=GraphManager.boundsMatrix[this.id][Index];
}


return Bound;
}


/** This method calculates a lower bound given a time consumption at a given node
* @param Time current time
* @return
*/
private double CalcBoundPhaseII(double Time) {


double Bound=0;
//If the time consumed is less than the current time incumbent it means that there is no lower bound available and we must use the naive bound 
if(Time<GraphManager.timeIncumbent){
	
	Bound=(GraphManager.timeIncumbent-Time)*GraphManager.naiveDualBound+GraphManager.overallBestCost;
	
}
else {
	// Else use the available bound	
	int Index=((int) Math.floor((Time-GraphManager.timeIncumbent)/DataHandler.boundStep))+GraphManager.timeIndex;
	Bound=GraphManager.boundsMatrix[this.id][Index];
}

return Bound;
}

	
	/**
	 * Get id of the node
	 * @return
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
	 * Sort the arcs
	 */
	public void autoSort(){
		Sort(this.magicIndex);
	}
	
	/**
	 * Sort method
	 * @param set
	 */
	private synchronized void Sort(ArrayList<Integer> set) {
		QS(set, 0, set.size() - 1);
	}
	
	/**
	 * Put method (for the sorting)
	 * @param e
	 * @param b
	 * @param t
	 * @return
	 */
	public int put(ArrayList<Integer> e, int b, int t) {
		int i;
		int pivot;
		double pivotVal;
		int temp;
	
		pivot = b;
		pivotVal = DataHandler.costList[e.get(pivot)] ;
		for (i = b + 1; i <= t; i++) {
			if (   DataHandler.costList[e.get(i)]< pivotVal) {
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
	 * QS
	 * @param e
	 * @param b
	 * @param t
	 */
	public void QS(ArrayList<Integer> e, int b, int t) {
		int pivot;
		if (b < t) {
			pivot = put(e, b, t);
			QS(e, b, pivot - 1);
			QS(e, pivot + 1, t);
		}
	}

	// Subset row inequalities:
	
		/**
		 * @return the subsetRow_ids
		 */
		public ArrayList<Integer> getSubsetRow_ids() {
			return subsetRow_ids;
		}

		/**
		 * @param subsetRow_ids the subsetRow_ids to set
		 */
		public void setSubsetRow_ids(ArrayList<Integer> subsetRow_ids) {
			this.subsetRow_ids = subsetRow_ids;
		}

		//Some methods to keep track of how many times we have visited a certain subset:
		
		public void addAVisitSubSetRow() {
			for(Integer id:this.subsetRow_ids) {
				if(PulseHandler.getNumVecesSubsetRowIneq().containsKey(id)) {
					PulseHandler.getNumVecesSubsetRowIneq().put(id,PulseHandler.getNumVecesSubsetRowIneq().get(id)+1);
				}
			}
		}
		public void minusAVisitSubSetRow() {
			for(Integer id:this.subsetRow_ids) {
				if(PulseHandler.getNumVecesSubsetRowIneq().containsKey(id)) {
					PulseHandler.getNumVecesSubsetRowIneq().put(id,PulseHandler.getNumVecesSubsetRowIneq().get(id)-1);
				}
			}
		}
		
		public void addAVisitSubSetRow(int Thread) {
			for(Integer id:this.subsetRow_ids) {
				if(PulseHandler.getNumVecesSubsetRowIneqMT().containsKey(id+"-"+Thread)) {
					PulseHandler.getNumVecesSubsetRowIneqMT().put(id+"-"+Thread,PulseHandler.getNumVecesSubsetRowIneqMT().get(id+"-"+Thread)+1);
				}
			}
		}
		public void minusAVisitSubSetRow(int Thread) {
			for(Integer id:this.subsetRow_ids) {
				if(PulseHandler.getNumVecesSubsetRowIneqMT().containsKey(id+"-"+Thread)) {
					PulseHandler.getNumVecesSubsetRowIneqMT().put(id+"-"+Thread,PulseHandler.getNumVecesSubsetRowIneqMT().get(id+"-"+Thread)-1);
				}
			}
		}
		
		public Object clone() {
			try {
				return super.clone();
			} catch (CloneNotSupportedException e) {
				return null;
			}
		}
}
