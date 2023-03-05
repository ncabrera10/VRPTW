package dataStructures;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;

import columnGeneration.Master;

/**
 * This class represents a ng label for the labeling algorithm
 * @author nicolas.cabrera-malik
 *
 */
public class Nglabel {

	/**
	 * Reduced cost until reaching the label
	 */
	private double reduced_cost;
	
	/**
	 * Cost of the label
	 */
	private double cost;
	
	/**
	 * Total time until reaching the label
	 */
	private double time;
	
	/**
	 * Total load until reaching the label
	 */
	private double load;
	
	/**
	 * Set of unreachable customers from this label
	 */
	private int[] unreachableCustomers;
	
	/**
	 * Set of customers that violate the ng-paths restrictions from this label
	 */
	private int[] ngForbiddenCustomers;
	
	/**
	 * Id of the predecessor label
	 */
	private int predecessor;
	
	/**
	 * Pointer to the predecessor node
	 */
	private Node predecessorNode;
	
	/**
	 * Pointer to the current node
	 */
	private Node node;
	
	/**
	 * ID for this label
	 */
	private int id;
	
	/**
	 * Sorting criteria (for ordering all the labels)
	 */
	private double sortCriteria;
	
	/**
	 * Counter of number of times a given subset has been visited
	 */
	private Hashtable<Integer,Integer> numTimesSubsetRowIneq;
	
	/**
	 * Stores the indices for which the number of times is equal to 1
	 */
	private ArrayList<Integer> subsetRowThatAre1Indices;
	
	/**
	 * Number of nodes visited in this partial path
	 */
	
	private int numberOfNodes;
	
	/**
	 * Has this label been extended?
	 */
	private boolean notTreated;
	
	/**
	 * This method creates a new NG label
	 */
	public Nglabel(double redCost,double cost,double totTime,double totLoad, int pred,Node current_node,int labelID,Node predNode,int numNodes,Hashtable<Integer,Integer> numTimesIneq) {
		
		this.reduced_cost = redCost;
		this.cost = cost;
		this.time = totTime;
		this.load = totLoad;
		this.predecessor = pred;
		this.node = current_node;
		this.id = labelID;
		this.predecessorNode = predNode;
		this.notTreated = true;
		this.numberOfNodes = numNodes;
		
		//Sorting criteria: this is key!
		this.sortCriteria = -(GraphManager.finalNode.tw_b - totTime);
		
		//For handling the subset row inequalities:
		
		this.numTimesSubsetRowIneq = new Hashtable<Integer,Integer>();
		this.subsetRowThatAre1Indices  = new ArrayList<Integer>();
		Enumeration<Integer> e = numTimesIneq.keys();
		while (e.hasMoreElements()) {
	        int key = e.nextElement();
        	 if(node.getSubsetRow_ids().contains(key)) {
 	        	numTimesSubsetRowIneq.put(key,(numTimesIneq.get(key) + 1) % 2);
 	        	if(numTimesIneq.get(key) == 1 && numTimesSubsetRowIneq.get(key) == 0) {
 	        		this.reduced_cost -= Master.getDuals_subset().get(key);
 	        	}
 	        }else {
	        	numTimesSubsetRowIneq.put(key,numTimesIneq.get(key));
	        }
	        	 
	        if(numTimesSubsetRowIneq.get(key) == 1) {
	        	subsetRowThatAre1Indices.add(key);
	        }
		}
		
		// Initialize both arrays
		
		this.unreachableCustomers = new int[DataHandler.n+1];
		this.ngForbiddenCustomers = new int[DataHandler.n+1];
	
	}

	
	/**
	 * This method marks the unreachable nodes and updates the NG paths cycling restrictions
	 * @param Lj
	 */
	public void markUnreachableNodes(Nglabel Lj) {
		
		// Check if we are considering a customer node:
		
		if(node.id >= 1 && node.id <= DataHandler.n) {
			
			// Iterate over all the other customers:
			
			for(int j = 1;j <= DataHandler.n;j++) {
				
				// Recover the customer node:
				
					Node node_2 = GraphManager.nodes[j];
				
				// Adjust the unreachable customers:
					
					// Initialize the value:
				
						unreachableCustomers[node_2.id] = Lj.unreachableCustomers[node_2.id];
						
					// Adjust the time of the label:
						
						double currentTime = Math.max(time, node.tw_a);
					
					// If the arc between the two nodes does not exists, the node is already unreachable:
						
						int arc = DataHandler.arcs_id[node.id][node_2.id];
						if(arc == -1) {
							unreachableCustomers[node_2.id] = 1;
						}else { 
							
							// Otherwise we calculate a lower bound on the time to reach the end node
							
							double lowerBoundOnTime = DataHandler.distList[arc] + DataHandler.distList[DataHandler.arcs_id[node_2.id][0]] + node_2.service;
							
							if(node.id != node_2.id  && (currentTime + lowerBoundOnTime > GraphManager.finalNode.tw_b)){  // this.time + DataHandler.timeList[DataHandler.arcs_id[node.id][node_2.id]] > node_2.tw_b
								unreachableCustomers[node_2.id] = 1;
							}
						}
						
				// NG paths cycles constraints
				
				if(node_2.id == node.id || (Lj.ngForbiddenCustomers[node_2.id] == 1 && node.getNgNeighborhood().contains(node_2.id))) {
					
					ngForbiddenCustomers[node_2.id] = 1;
					
				}else {
					
					ngForbiddenCustomers[node_2.id] = 0;
					
				}
				
			}
		}else {
			for(int j = 1;j <= DataHandler.n;j++) {
				
				Node node_2 = GraphManager.nodes[j];
				
				//Unreachable customers
				
				unreachableCustomers[node_2.id] = Lj.unreachableCustomers[node_2.id];
				
				// Adjust the time of the label:
				
				double currentTime = Math.max(time, node.tw_a);
			
				// If the arc between the two nodes does not exists, the node is already unreachable:
				
				int arc = DataHandler.arcs_id[node.id][node_2.id];
				if(arc == -1) {
					unreachableCustomers[node_2.id] = 1;
				}else { 
					
					// Otherwise we calculate a lower bound on the time to reach the end node
					
					double lowerBoundOnTime = DataHandler.distList[arc] + DataHandler.distList[DataHandler.arcs_id[node_2.id][0]] + node_2.service;
					
					if(node.id != node_2.id  && (currentTime + lowerBoundOnTime > GraphManager.finalNode.tw_b)){  // this.time + DataHandler.timeList[DataHandler.arcs_id[node.id][node_2.id]] > node_2.tw_b
						unreachableCustomers[node_2.id] = 1;
					}
				}
				
				// NG paths cycles constraints
				
				if(node_2.id == node.id || (Lj.ngForbiddenCustomers[node_2.id] == 1)) {
					
					ngForbiddenCustomers[node_2.id] = 1;
					
				}else {
					
					ngForbiddenCustomers[node_2.id] = 0;
					
				}
				
			}
		}
		
	}
	
	/**
	 * True if this label heuristically dominates the new label 
	 * @param redCost
	 * @param totTime
	 * @param subTTime
	 * @param totWDist
	 * @return
	 */
	public boolean heuristicallyDominatesLabel(Nglabel Lp) {
		
		// Check the reduced cost
		if(this.reduced_cost > Lp.reduced_cost) {
			return false;
		}
		
		//Check resources
		if(this.time >  Lp.time) {
			return false;
		}
		if(this.load >  Lp.load) {
			return false;
		}

		return true; //It means the current label has a lower or equal reduced cost, higher time or equal  and higher or equal walking distance
	}
	
	/**
	 * True if this label truly dominates the new label 
	 * @param redCost
	 * @param totTime
	 * @param subTTime
	 * @param totWDist
	 * @return
	 */
	public boolean exactlyDominatesLabel(Nglabel Lp) {
		
		// Calculate the additional value we need (because of the subset row inequalities)
		
		double additionalValue = 0;
		for(int i=0;i<subsetRowThatAre1Indices.size();i++) {
			int index = subsetRowThatAre1Indices.get(i);
			if(!Lp.subsetRowThatAre1Indices.contains(index)) {
				additionalValue += Master.getDuals_subset().get(index);
			}
			
		}
		if(this.reduced_cost - additionalValue > Lp.reduced_cost) {
			return false;
		}
		
		//Check resources
		
		if(this.time > Lp.time) {
			return false;
		}
		if(this.load >  Lp.load) {
			return false;
		}
		for(int j = 1; j <= DataHandler.n;j++) {
			Node node_2 = GraphManager.nodes[j];
			if(Math.max(this.unreachableCustomers[node_2.id], this.ngForbiddenCustomers[node_2.id]) > Math.max(Lp.unreachableCustomers[node_2.id], Lp.ngForbiddenCustomers[node_2.id])) {
				return false;
			}

		}
		
		return true; //It means the current label has a lower reduced cost and a lower time and visit the same list of customers
	}
	
	/**
	 * @return the reducedCost
	 */
	public double getReducedCost() {
		return reduced_cost;
	}

	/**
	 * @param reducedCost the reducedCost to set
	 */
	public void setReducedCost(double reducedCost) {
		this.reduced_cost = reducedCost;
	}

	/**
	 * @return the totalTime
	 */
	public double getTotalTime() {
		return time;
	}

	/**
	 * @param totalTime the totalTime to set
	 */
	public void setTotalTime(double totalTime) {
		this.time = totalTime;
	}

	/**
	 * @return the predecessor
	 */
	public int getPredecessor() {
		return predecessor;
	}

	/**
	 * @param predecessor the predecessor to set
	 */
	public void setPredecessor(int predecessor) {
		this.predecessor = predecessor;
	}

	/**
	 * @return the node
	 */
	public Node getNode() {
		return node;
	}

	/**
	 * @param node the node to set
	 */
	public void setNode(Node node) {
		this.node = node;
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * @return the cost
	 */
	public double getCost() {
		return cost;
	}

	/**
	 * @param cost the cost to set
	 */
	public void setCost(double cost) {
		this.cost = cost;
	}

	/**
	 * @return the predecessorNode
	 */
	public Node getPredecessorNode() {
		return predecessorNode;
	}

	/**
	 * @param predecessorNode the predecessorNode to set
	 */
	public void setPredecessorNode(Node predecessorNode) {
		this.predecessorNode = predecessorNode;
	}
	
	

	/**
	 * @return the sortCriteria
	 */
	public double getSortCriteria() {
		return sortCriteria;
	}

	/**
	 * @param sortCriteria the sortCriteria to set
	 */
	public void setSortCriteria(double sortCriteria) {
		this.sortCriteria = sortCriteria;
	}

	@Override
	public String toString() {
		return("Label "+id+" - RC:"+reduced_cost+" - C:"+cost+" - T:"+time+" - L:"+load+" - "+" - Node:"+node.id+" - Pred:"+predecessorNode.id+" - "+"LPred:"+predecessor+" - "+Arrays.toString(unreachableCustomers)+" - "+Arrays.toString(ngForbiddenCustomers));
	}

	/**
	 * @return the unreachableCustomers
	 */
	public int[] getUnreachableCustomers() {
		return unreachableCustomers;
	}

	/**
	 * @param unreachableCustomers the unreachableCustomers to set
	 */
	public void setUnreachableCustomers(int[] unreachableCustomers) {
		this.unreachableCustomers = unreachableCustomers;
	}

	/**
	 * @return the ngForbiddenCustomers
	 */
	public int[] getNgForbiddenCustomers() {
		return ngForbiddenCustomers;
	}

	/**
	 * @param ngForbiddenCustomers the ngForbiddenCustomers to set
	 */
	public void setNgForbiddenCustomers(int[] ngForbiddenCustomers) {
		this.ngForbiddenCustomers = ngForbiddenCustomers;
	}

	/**
	 * @return the numTimesSubsetRowIneq
	 */
	public Hashtable<Integer, Integer> getNumTimesSubsetRowIneq() {
		return numTimesSubsetRowIneq;
	}

	/**
	 * @param numTimesSubsetRowIneq the numTimesSubsetRowIneq to set
	 */
	public void setNumTimesSubsetRowIneq(Hashtable<Integer, Integer> numTimesSubsetRowIneq) {
		this.numTimesSubsetRowIneq = numTimesSubsetRowIneq;
	}

	/**
	 * @return the subsetRowThatAre1Indices
	 */
	public ArrayList<Integer> getSubsetRowThatAre1Indices() {
		return subsetRowThatAre1Indices;
	}

	/**
	 * @param subsetRowThatAre1Indices the subsetRowThatAre1Indices to set
	 */
	public void setSubsetRowThatAre1Indices(ArrayList<Integer> subsetRowThatAre1Indices) {
		this.subsetRowThatAre1Indices = subsetRowThatAre1Indices;
	}

	/**
	 * @return the numberOfNodes
	 */
	public int getNumberOfNodes() {
		return numberOfNodes;
	}

	/**
	 * @param numberOfNodes the numberOfNodes to set
	 */
	public void setNumberOfNodes(int numberOfNodes) {
		this.numberOfNodes = numberOfNodes;
	}




	/**
	 * @return the notTreated
	 */
	public boolean isNotTreated() {
		return notTreated;
	}


	/**
	 * @param notTreated the notTreated to set
	 */
	public void setNotTreated(boolean notTreated) {
		this.notTreated = notTreated;
	}


	public double getLoad() {
		return load;
	}


	public void setLoad(double load) {
		this.load = load;
	}
}
