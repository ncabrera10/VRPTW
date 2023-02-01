package columnGeneration;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jorlib.frameworks.columnGeneration.master.cutGeneration.AbstractCutGenerator;
import org.jorlib.frameworks.columnGeneration.master.cutGeneration.AbstractInequality;

import dataStructures.DataHandler;
import dataStructures.GraphManager;
import globalParameters.CGParameters;
import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloRange;

/**
 * This class is used to generate new subset row inequalities
 * @author nicolas.cabrera-malik
 *
 */
public class SubsetRowInequalityGenerator extends AbstractCutGenerator<VRPTW,PLRPMasterData> {


	/**
	 * Creates a new subtour inequality generator
	 * @param modelData data model
	 */
	public SubsetRowInequalityGenerator(VRPTW modelData) {
		super(modelData, "subtourIneqGenerator");
	}

	/**
	 * Generate inequalities using the data originating from the master problem
	 * @return Returns true if a violated inequality has been found
	 * This method computes violated inequalities by using data originating from the MP. 
	 * In practice, this method relies on separation routines
	 * When violated inequalities are found, they are added to the MP through the addCut method
	 */
	@Override
	public List<AbstractInequality> generateInqualities() {
		
		ArrayList<SubsetRowInequality> inequalities = new ArrayList<SubsetRowInequality>();
		ArrayList<SubsetRowInequality> provisional_inequalities = new ArrayList<SubsetRowInequality>();
		double[] num_times = new double[DataHandler.n];
		
		//Check if we are adding this type of cuts or not:
		
		if(!CGParameters.USE_SUBSET_ROW_INEQ) {
			return new ArrayList<>(inequalities);
		}
		
		if(VRPTW.cutIterationAtCurrentBAPNode >= CGParameters.MAX_SUBSET_ROW_INEQ_ROOTNODE) {
			return new ArrayList<>(inequalities);
		}
	
		Double FTime = (double) System.nanoTime();
		if((FTime-VRPTW.ITime)/1000000000 > CGParameters.BAP_TIME_LIMIT_SEC/2  && VRPTW.stillAtTheRootNode) {
			return new ArrayList<>(inequalities);
		}
		
		
		//Check for violated inequalities:
		
		int numCuts = 0;
		for(int i = 1;i <= DataHandler.n;i++) {
        	
        	for(int j = i+1;j <= DataHandler.n;j++) {
	        	
        		for(int k = j+1;k <= DataHandler.n;k++) {
        			
        			//For each path we compute the coefficient:
        			
        			//String constraint = "";
        			double total_value = 0.0;
        			for(RoutePattern route:masterData.currentSolution) {
        				
        				if(route.value > 0 && route.value < 1) {
        					double coeff = 0.0;
        					
        					if(route.yieldVector[i-1] > 0) {
        						
        						coeff+=route.yieldVector[i-1];
        						
        					}
							if(route.yieldVector[j-1] > 0) {
							        						
								coeff+=route.yieldVector[j-1];
							        						
							}
							if(route.yieldVector[k-1] > 0) {
								
								coeff+=route.yieldVector[k-1];
								
							}
							
							total_value += Math.floor(coeff* 0.5)* route.value ;
							//System.out.println(route.value+" - "+coeff+" - "+(Math.ceil(coeff* 0.5)* route.value)+" - "+total_value);
        				}
        				
        			}
        			//System.out.println(i+" - "+j+" - "+k+" :"+total_value);
        			
        			//We check if the violation satisfies the threshold:
        			
        			if(total_value-CGParameters.MIN_SUBSET_ROW_INEQ_VIOL > 1) { //We can add a valid inequality
        				
        				ArrayList<RoutePattern> routes = new ArrayList<RoutePattern>();
        				ArrayList<Integer> coefficients = new ArrayList<Integer>();
        				ArrayList<Integer> nodes = new ArrayList<Integer>();
        				ArrayList<Integer> routes_ids = new ArrayList<Integer>();
        				double violation = total_value - 1;
        				
        				//Complete the arraylists:
        				
        				nodes.add(i);
        				nodes.add(j);
        				nodes.add(k);
        				
        				//The routes and the coefficients:
        				
        				for(RoutePattern route:masterData.getColumns()) { //masterData.currentSolution
            				
            				//if(route.value > 0 && route.value < 1) {
            					int coeff = 0;
            					if(route.yieldVector[i-1] > 0) {
            						
            						coeff+=route.yieldVector[i-1];
            						
            					}
    							if(route.yieldVector[j-1] > 0) {
    							        						
    								coeff+=route.yieldVector[j-1];
    							        						
    							}
    							if(route.yieldVector[k-1] > 0) {
    								
    								coeff+=route.yieldVector[k-1];
    								
    							}
    							if(coeff >= 2) {
    								routes.add(route);
        							coefficients.add(coeff);
        							routes_ids.add(route.id);
    							}
    							
            				//}
            				
            			}
        				
        				//Create the subset row inequality constraint:
        				
        				SubsetRowInequality inequality = new SubsetRowInequality(this,nodes,routes,coefficients,VRPTW.numInequalities,violation,routes_ids);
        				provisional_inequalities.add(inequality);
        				VRPTW.numInequalities++;
        			}
        			
        		}
        	}
		}
		
		// Sort the arraylist of provisional inequalities
		
		Collections.sort(provisional_inequalities);
        	
		// Iterate through the provisional inequalities and add them to the final list:
		
		for(int i = 0;i<provisional_inequalities.size() && numCuts<CGParameters.MAX_SUBSET_ROW_INEQ_PERITER;i++) {
			
			SubsetRowInequality inequality = provisional_inequalities.get(i);
			//System.out.println(inequality);
			boolean can_be_added = true;
			for(int j = 0;j<3;j++) {
				if(num_times[inequality.cutSet.get(j)-1] >= CGParameters.MAX_SUBSET_ROW_INEQ_PERCUSTOMER) {
					can_be_added = false;
				}
			}
			
			if(can_be_added && !masterData.subsetRowInequalities.containsKey(inequality)){
				this.addCut(inequality);
				//System.out.println("I added: "+inequality.id+" - "+inequality.cutSet.toString());
				inequalities.add(inequality);
				numCuts++;
				VRPTW.numInequalities++;
				
				for(int j = 0;j<3;j++) {
					num_times[inequality.cutSet.get(j)-1]++;
					//System.out.println("Node: "+GraphManager.nodes[inequality.cutSet.get(j)].id);
					GraphManager.nodes[inequality.cutSet.get(j)].getSubsetRow_ids().add(inequality.id);
				}
			}
			
		}
		
		if(numCuts == 0) {
			return(Collections.emptyList());
		}else {
			VRPTW.cutIterationAtCurrentBAPNode++;
			return new ArrayList<>(inequalities);
		}

	}

	/**
	 * If a violated inequality has been found add it to the master problem.
	 * @param subtourInequality subtour inequality
	 * This method adds a violated inequality to the MP (it can only access the shared resource in the MasterData object).
	 * Handle the corresponding cplex object in the MasterData so that the Cut Generator Directly add a constraint. 
	 */
	private void addCut(SubsetRowInequality subsetRowInequality){
		if(masterData.subsetRowInequalities.containsKey(subsetRowInequality))
			throw new RuntimeException("Error, duplicate subset row cut is being generated! This cut should already exist in the master problem: "+subsetRowInequality);
		//Create the inequality in cplex
		try {
			IloLinearNumExpr expr=masterData.cplex.linearNumExpr();
			for(int i = 0;i < subsetRowInequality.routes.size(); i++) { 
				RoutePattern route = subsetRowInequality.routes.get(i);
				IloNumVar var=masterData.getVar(route);
				if(!(var == null)) {
					expr.addTerm(Math.floor(subsetRowInequality.coefficients.get(i)/2), var);
				}
			}
			String adicional = "";
			for(int j:subsetRowInequality.cutSet) {
				adicional += j + "_";
			}
			if(CGParameters.PRINT_IN_CONSOLE) {
				System.out.println("Adding inequality: "+VRPTW.numInequalities+" with violation "+subsetRowInequality.violation+" with subset "+subsetRowInequality.cutSet.toString()+ " - "+expr);
			}
			IloRange subsetRowConstraint = masterData.cplex.addLe(expr, 1, "subsetRowIneq_"+subsetRowInequality.id+"_"+adicional);
			masterData.subsetRowInequalities.put(subsetRowInequality, subsetRowConstraint);
		} catch (IloException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Add a subtour inequality to the master problem
	 * @param cut AbstractInequality
	 */
	@Override
	public void addCut(AbstractInequality cut) {
		if(!(cut instanceof SubsetRowInequality))
			throw new IllegalArgumentException("This AbstractCutGenerator can ONLY add SubsetRowInequalities");
		SubsetRowInequality subsetRowInequality=(SubsetRowInequality) cut;
		this.addCut(subsetRowInequality);
	}

	/**
	 * Retuns a list of inequalities that have been generated.
	 * @return Retuns a list of inequalities that have been generated.
	 */
	@Override
	public List<AbstractInequality> getCuts() {
		return new ArrayList<>(masterData.subsetRowInequalities.keySet());
	}

	/**
	 * Close the generator
	 */
	@Override
	public void close() {} //Nothing to do here

}
