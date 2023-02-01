package branchAndPrice;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.jorlib.frameworks.columnGeneration.branchAndPrice.AbstractBranchCreator;
import org.jorlib.frameworks.columnGeneration.branchAndPrice.BAPNode;

import columnGeneration.VRPTW;
import columnGeneration.RoutePattern;
import globalParameters.CGParameters;
import globalParameters.GlobalParameters;
import pulseAlgorithm.PA_PricingProblem;

/**
 * This class implements the branching on an arc decisions
 * Basically:
 * 1. It searches the arc that we should use for branching
 * 2. It generates the new BAP nodes based on the branching decisions
 * 3. It checks if it is possible to branch on an arc
 * @author nicolas.cabrera-malik
 *
 */
public final class BranchOnArc extends  AbstractBranchCreator<VRPTW,RoutePattern,PA_PricingProblem>{

	/**
	 * The pricing problem
	 */
	private PA_PricingProblem pricingProblem=null;
	
	/**
	 * Selected arc for branching
	 */
	private String arcForBranching=null;
	
	/**
	 * This method creates an instance of the branch on arc object
	 * @param modelData
	 * @param pricingProblems
	 */
	public BranchOnArc(VRPTW modelData, List<PA_PricingProblem> pricingProblems) {
		
		super(modelData,pricingProblems);
	}

	/**
	 * This method determines on which arc we are going to branch
	 * @param solution Fractional column generation solution
	 * @return true if a fractional arc exists
	 */
	@Override
	protected boolean canPerformBranching(List<RoutePattern> solution) {
		
		//Reset values
		
		pricingProblem = null;
		
		//Check the solution
		
			// Initialize a table to store the total value of an arc in the current solution
			
			Hashtable<String,Double> arc_values = new Hashtable<String,Double>(); 
			
			// Calculate the value of each arc:
			
				for(int i = 0;i < solution.size();i++) {
					
					// Retrieve the current path:
					
					RoutePattern currentPath = solution.get(i);
					
					// Recover the route associated to the path:
					
					String base = currentPath.route;
					
					// Recover the arcs in the route:
					
					String[] arcs = base.split(";");
					
					// Iterate over all arcs:
					
					for(int j = 0;j<arcs.length;j++) {
						String arc_act = arcs[j].replaceAll("[()]","");
						if(!arc_values.containsKey(arc_act)) {
							arc_values.put(arc_act,currentPath.value);
						}else {
							arc_values.put(arc_act,currentPath.value + arc_values.get(arc_act));
						}
						
					}
				}
		
			// Get the final list of candidates:
			
				int numFracArcs = 0;
				Set<String> setOfKeys = arc_values.keySet();
		        Iterator<String> itr = setOfKeys.iterator();
        
	        // Initialize the hashtables to store the arc values:
			
		        Hashtable<String,Double> candidates_d = new Hashtable<String,Double>();
		        
		        while (itr.hasNext()) {
		        	 
		            String key = itr.next(); //!key.contains(""+(PLRP.numCustomers+1)) && 
		            if(arc_values.get(key) < 1-Math.pow(10,-GlobalParameters.PRECISION)) {
		            	candidates_d.put(key, arc_values.get(key));
		            	numFracArcs++;
		            }
		            
		        }

        //Print:
        
        String selected_arc = "";
        Double value_selected_arc = 0.0;
        
        // Iterate overall all arcs: we prioritize driving arcS:
        
        	setOfKeys = candidates_d.keySet();
            itr = setOfKeys.iterator();
           
            while (itr.hasNext()) {
            	 
                String key = itr.next();
                if(!VRPTW.forbiddenArcs.containsKey(key)) {
                    if(Math.abs(0.5-value_selected_arc) > Math.abs(0.5-candidates_d.get(key))) {
                    	value_selected_arc = candidates_d.get(key);
                    	selected_arc = key;
                    }
                }
                
            }
            
           
            
        
	        if(selected_arc == "") {
				return false;
	        }else {
	        	String arc_key = selected_arc;

	        	if(CGParameters.PRINT_IN_CONSOLE) {
	        		System.out.println("Selected arc: "+selected_arc+" - "+value_selected_arc+" - Options: "+numFracArcs);
	        	}
	        	
				//Take the selected arc:
				
				arcForBranching = arc_key;
				
				//Select the pricing problem:
				
				pricingProblem = pricingProblems.get(0);
				
				return(true);
	        }
	}

	/**
	 * Create the branches:
	 * 
	 * Branch 1: arc must be used 
	 * Branch 2: arc must not used
	 * 
	 * @param parentNode Fractional node on which we branch
	 * @return List of child nodes
	 */
	@Override
	protected List<BAPNode<VRPTW, RoutePattern>> getBranches(BAPNode<VRPTW, RoutePattern> parentNode) {
		
		// We are not longer at the root node:
		
		VRPTW.stillAtTheRootNode = false;
		
		RemoveArc branchingDecision1 = new RemoveArc(pricingProblem,arcForBranching,parentNode.getInequalities());
		BAPNode<VRPTW,RoutePattern> node1 = this.createBranch(parentNode, branchingDecision1, parentNode.getInitialColumns(), parentNode.getInequalities());
		
		// Branch 2: fix the arc:
		
		FixArc branchingDecision2 = new FixArc(pricingProblem,arcForBranching,parentNode.getInequalities());
		BAPNode<VRPTW,RoutePattern> node2 = this.createBranch(parentNode, branchingDecision2, parentNode.getInitialColumns(), parentNode.getInequalities());

		return(Arrays.asList(node1,node2));

	}
	
}
