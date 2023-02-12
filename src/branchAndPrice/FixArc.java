package branchAndPrice;

import java.util.ArrayList;
import java.util.List;

import org.jorlib.frameworks.columnGeneration.branchAndPrice.branchingDecisions.BranchingDecision;
import org.jorlib.frameworks.columnGeneration.master.cutGeneration.AbstractInequality;

import columnGeneration.VRPTW;
import dataStructures.DataHandler;
import pricingAlgorithms.PA_PricingProblem;
import columnGeneration.RoutePattern;


/**
 * This class represents a fix arc branching decision
 * @author nicolas.cabrera-malik
 *
 */
public final class FixArc implements BranchingDecision<VRPTW,RoutePattern>{
	
	
	/**
	 * Pricing problem
	 */
	public final PA_PricingProblem pricingProblem;
	
	/**
	 * List of inequalities we currently have
	 */
	
	public final List<AbstractInequality> subsetRowCuts;
	
	/**
	 * Key of the arc
	 */
	
	public final String keyArc;
	
	/**
	 * Tails of the forbidden arcs of this branch (Driving)
	 */
	public ArrayList<Integer> tails_drive;
	
	/**
	 * Heads of the forbidden arcs of this branch (Driving)
	 */
	public ArrayList<Integer> heads_drive;
	
	/**
	 * Id of the head node
	 */
	public int head_id;
	
	/**
	 * Id of the tail node
	 */
	public int tail_id;
	
	/**
	 * 
	 * @param pricingProblem
	 * @param arc
	 */
	public FixArc(PA_PricingProblem pricingProblem,String arc,List<AbstractInequality> list) {
		
		//Initialize the lists of arcs and the main parameters:
		
		tails_drive = new ArrayList<Integer>();
		heads_drive = new ArrayList<Integer>();
		this.pricingProblem = pricingProblem;
		String[] nodes = arc.split("-");
		
		this.tail_id = Integer.parseInt(nodes[0]);
		this.head_id = Integer.parseInt(nodes[1]);
		this.subsetRowCuts = list;
		
		//1. an arc that starts at the depot:
		
		if(tail_id == 0) { //If the arc starts at the depot
			
			// Store the key of the arc:
			
			this.keyArc = tail_id+"-"+head_id;

			//Forbid arcs driving from others to j
			
			for(int k = 1;k <= DataHandler.n;k++) {
				tails_drive.add(k);
				heads_drive.add(head_id);
			}
			
		}else if(head_id == 0) { //If the arc ends at the depot (it can only be driving)
			
			// Store the key of the arc:
			
			this.keyArc = tail_id+"-"+head_id;

			//Forbid arcs driving from i to others
			
			for(int k = 1;k <= DataHandler.n;k++) {
				tails_drive.add(tail_id);
				heads_drive.add(k);
			}
			
		}else { //2. If the arc is a driving arc i to j:
			
			// Store the key of the arc:
			
				this.keyArc = tail_id+"-"+head_id;
			
			//Forbid arcs driving from i to others and from others to j
				
				for(int k = 1;k <= DataHandler.n;k++) {
					if(k != head_id && tail_id != k) {
						tails_drive.add(tail_id);
						heads_drive.add(k);
						tails_drive.add(k);
						heads_drive.add(head_id);
					}
				}
				
			// The arc driving on the opposite direction: We cannot use it ! it will create a loop..
				
				tails_drive.add(head_id);
				heads_drive.add(tail_id);
		}
		
	}

	/**
	 * Determine whether the given column remains feasible for the child node
	 * @param column column
	 * @return true if the column is compliant with the branching decision
	 */
	@Override
	public boolean columnIsCompatibleWithBranchingDecision(RoutePattern column) {
		
		// Initialize a boolean indicator:
		
		boolean isCompatible = true;

		//If the column is artificial we accept it no matter what:
		
		if(column.isArtif) {
			return(true);
		}
		
		// Iterate through the lists of forbidden arcs:
		
		int numD = tails_drive.size();
		for(int i=0;i<numD;i++) {
			for(int j=0;j<column.route.size()-1;j++) {
				if(column.route.get(j) == tails_drive.get(i) && column.route.get(j+1) == heads_drive.get(i)) {
					return(false);
				}
			}
			
		}

		return isCompatible;
	}

	@Override
	public boolean inEqualityIsCompatibleWithBranchingDecision(AbstractInequality arg0) {
		return true;
	}
	
	@Override
    public String toString(){
        return "Fix: "+keyArc+" for pricingProblem: "+pricingProblem;
    }
	

}
