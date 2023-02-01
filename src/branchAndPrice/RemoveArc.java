package branchAndPrice;

import java.util.ArrayList;
import java.util.List;

import org.jorlib.frameworks.columnGeneration.branchAndPrice.branchingDecisions.BranchingDecision;
import org.jorlib.frameworks.columnGeneration.master.cutGeneration.AbstractInequality;

import columnGeneration.VRPTW;
import columnGeneration.RoutePattern;
import pulseAlgorithm.PA_PricingProblem;

/**
 * This class implements the remove arc branching decision.
 * It contains methods that:
 * 1. Check if a column is compatible with the decision
 * 2. Store the forbidden walking and driving arcs
 * @author nicolas.cabrera-malik
 *
 */
public final class RemoveArc implements BranchingDecision<VRPTW,RoutePattern>{
	
	
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
	 * Creates a remove arc branch
	 * @param pricingProblem
	 * @param arc
	 */
	public RemoveArc(PA_PricingProblem pricingProblem,String arc,List<AbstractInequality> list) {
		
		//Initializes the hashtables:
		
		tails_drive = new ArrayList<Integer>();
		heads_drive = new ArrayList<Integer>();
		
		//Initializes the main variables for this branch:
		
		this.pricingProblem = pricingProblem;
		
		String[] nodes = arc.split("-");
		
		this.tail_id = Integer.parseInt(nodes[0]);
		this.head_id = Integer.parseInt(nodes[1]);
		this.subsetRowCuts = list;
		
		//Stores the arc key
			
		this.keyArc = arc;
			
		//Forbid the arc:
			
		tails_drive.add(tail_id);
		heads_drive.add(head_id);
		
	}

	/**
	 * Determine whether the given column remains feasible for the child node
	 * @param column column
	 * @return true if the column is compliant with the branching decision
	 */
	@Override
	public boolean columnIsCompatibleWithBranchingDecision(RoutePattern column) {
		boolean isCompatible = true;
	
		//If the column is artificial, we always return true:
		
		if(column.isArtif) {
			return(true);
		}
		
		//We check if the column contains the key arc:
		
		for(int j=0;j<column.route.size()-1;j++) {
			if(column.route.get(j) == tail_id && column.route.get(j+1) == head_id) {
				return(false);
			}
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
	        return "Remove: "+keyArc+" for pricingProblem: "+pricingProblem;
	    }

}
