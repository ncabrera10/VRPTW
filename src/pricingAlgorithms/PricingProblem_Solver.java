package pricingAlgorithms;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import org.jorlib.frameworks.columnGeneration.branchAndPrice.branchingDecisions.BranchingDecision;
import org.jorlib.frameworks.columnGeneration.io.TimeLimitExceededException;
import org.jorlib.frameworks.columnGeneration.pricing.AbstractPricingProblemSolver;

import branchAndPrice.FixArc;
import branchAndPrice.RemoveArc;
import columnGeneration.Master;
import columnGeneration.VRPTW;
import dataStructures.DataHandler;
import dataStructures.GraphManager;
import metaheuristics.TabuSearch;
import parameters.CGParameters;
import columnGeneration.RoutePattern;

/**
 * This class provides a solver for the pricing problem.
 * The pricing problem is an Elementary shortest path with resource constraints
 * 
 */

/**
 * The AbstractPricingProblem serves as a container to hold the dual information coming from the MP. 
 * It defines a Pricing Problem which can be solved by some algorithm. 
 * In our case, the Pricing Problem class maintains the dual values.
 */

public final class PricingProblem_Solver extends AbstractPricingProblemSolver<VRPTW, RoutePattern, PricingProblem> {

	/**
	 * Stores the objective function of the pricing problem (with the st dual variables)
	 */
	public static double objectiveFunction;
	
	/**
	 * Was it solved to optimality or not? (we stop the pulse soon sometimes..)
	 */
	public static boolean solvedToOptimality;
	
	/**
	 * Creates a pricing problem solver
	 * @param dataModel
	 * @param pricingProblem
	 */
	public PricingProblem_Solver(VRPTW dataModel, PricingProblem pricingProblem) {
		super(dataModel, pricingProblem);
		this.name="PulseAlgorithm"; //Set a name for the solver
		this.buildModel();
	}

	/**
	 * Runs the pulse
	 */
	private void buildModel(){
		
		//Empty..
	}

	
	/**
	 * This method produces zero or more columns. 
	 */
	@Override
	protected List<RoutePattern> generateNewColumns() throws TimeLimitExceededException {
		
		// Initializes the list of patterns:
		
		List<RoutePattern> newPatterns=new ArrayList<>();
		
		// Start the clock:
		
		double start_time = System.currentTimeMillis();
		
		// Tries to solve the pricing problem: 
		
		try {
			
			
			// 1. Tries to generate columns using the tabu search:
			
				if(VRPTW.numInequalities < 1 && VRPTW.cgIteration <= CGParameters.NUM_ITERATIONS_TS && Master.use_tabu) {
				
					newPatterns = runTabuSearch();
					PricingProblem_Solver.solvedToOptimality = false;
					
				}
			
			// 2. Checks if we already found promising paths:
				
				if(newPatterns.size() == 0) {
					
					// Updates the counter for the number of times we have used the pulse:
					
					VRPTW.number_pricing_byPulse++;
					
					// Updates the binary variable: Let's not use the tabu search any more.
					
					Master.use_tabu = false;
					
					//1. Creates and runs the auxiliary problem:
					
					new PricingProblem_Handler(Master.getDuals_subset(),Master.getDuals());

					// Compute the list of paths
						
					newPatterns = createListOfElementaryPatterns();
					
					//Assume it was feasible unless we prove otherwise.
					
					this.pricingProblemInfeasible=false;
					
					//3. If solved to optimality.
					
					if(PricingProblem_Handler.isWasSolvedToOptimality()) {
						this.pricingProblemInfeasible = false;
						PricingProblem_Solver.solvedToOptimality = true;
						PricingProblem_Solver.objectiveFunction = PricingProblem_Handler.getPrimalBound();	
						this.objective = PricingProblem_Handler.getPrimalBound();	
						VRPTW.number_pricing_solvedToOptimality++;
					}else {
						PricingProblem_Solver.solvedToOptimality = false;
					}
					
					//If the number of paths was zero, the problem was indeed infeasible!
					
					if(newPatterns.size() == 0) {
						pricingProblemInfeasible = true;
						this.objective = Double.MAX_VALUE;
					}
					
				}else {
					VRPTW.number_pricing_byTabu++;
				}
			
		}
		catch(Exception e) {
			e.printStackTrace();
			System.out.println("Problem running the pulse");
			System.exit(0);
		}
		VRPTW.time_on_pricing += (System.currentTimeMillis()-start_time)/1000;
		return newPatterns;
	}
	
	/**
	 * This method runs the tabu search to try to find some columns before using the pulse
	 * @return
	 */
	public List<RoutePattern> runTabuSearch(){
		
		// Initializes the list of patterns:
		
			List<RoutePattern> newPatterns = new ArrayList<>();
				
		// 1. Updates the weights of the arcs :
		
			for(int i = 0;i < DataHandler.numArcs;i++) {
				
				DataHandler.cost[DataHandler.arcs[i][0]][DataHandler.arcs[i][1]] = DataHandler.cost[DataHandler.arcs[i][0]][DataHandler.arcs[i][1]] - Master.getDuals()[DataHandler.arcs[i][0]];
				DataHandler.costList[i] = DataHandler.distList[i]-Master.getDuals()[DataHandler.arcs[i][0]]; //Calculate reduced cost with the dual variable of the tail node of each arc
				
			}	
		
		// 2. Resets the pool of paths:
		
			Master.heuristics.resetPool();
		
		// Calls the tabu search: 
		
			TabuSearch ts = new TabuSearch(Master.heuristics);
			ts.run(Master.basisIndexes, 0);
		
		// Sortear el pool:
			
			Master.heuristics.Sort(Master.heuristics.pool);
			
		// Recovers the solution:
			
			ArrayList<String> newCols = Master.heuristics.getPoolCols();
			Hashtable<String, Double> colsRC = Master.heuristics.getHeuPoolRC();
			Hashtable<String, Double> colsDist = Master.heuristics.getHeuPoolDist();
			for (int i = 0; i < Math.min(CGParameters.MAX_PATHS_PER_ITERATION, newCols.size()); i++) {
				String key = newCols.get(i);
				if(i==0){
				}
				String col = newCols.get(i);
				
				col = col.substring(1,col.length()-1);
				String[] colSplit = col.split(", ");
				ArrayList<Integer> dummyPath = new ArrayList<>();
				for (int j = 0; j < colSplit.length; j++) {
					int node = Integer.parseInt(colSplit[j]);
					dummyPath.add(node);
					
				}
				
				if(colsRC.get(key) < 0) {
					
					int[] pattern=new int[DataHandler.n];
					for(int j=0;j<DataHandler.n;j++) {
						pattern[j] = 0;
					}
					
					for(int j=1;j<dummyPath.size()-1;j++) {
						pattern[dummyPath.get(j)-1] = 1;
					}
					
					//Creates the pattern
					
					RoutePattern column = new RoutePattern("TabuSearch", false, pattern,colsDist.get(key),dummyPath,pricingProblem,colsRC.get(key),-1,-1);
					newPatterns.add(column);
					
				}
			
			}
		
		// Re-updates the weights of the arcs :
		
		for(int i = 0;i < DataHandler.numArcs;i++) {
			
			DataHandler.cost[DataHandler.arcs[i][0]][DataHandler.arcs[i][1]] = DataHandler.cost[DataHandler.arcs[i][0]][DataHandler.arcs[i][1]] + Master.getDuals()[DataHandler.arcs[i][0]];
			DataHandler.costList[i] = DataHandler.distList[i]+Master.getDuals()[DataHandler.arcs[i][0]]; //Calculate reduced cost with the dual variable of the tail node of each arc
			
		}	
		
		return(newPatterns);
		
	}
	
	
	/**
	 * This method iterates overall the paths found by the pulse and creates the corresponding route patterns
	 * @return list of route patterns
	 */
	public List<RoutePattern> createListOfElementaryPatterns(){
		
		// Initialize the list of patterns:
		
		List<RoutePattern> newPatterns=new ArrayList<>();
		
		// Iterates over the pool of paths found by the pulse:
		
		for(int i=0;i<GraphManager.finalNode.pool.size();i++) {
			
			//Create the actual pattern:
			
			int[] pattern=new int[DataHandler.n];
			for(int j=0;j<DataHandler.n;j++) {
				pattern[j] = 0;
			}
			
			// Recovers the list of nodes visited:
			
			String col = GraphManager.finalNode.pool.get(i);
			
			//Capture the cost and the reduced cost:
			
			double cost = GraphManager.finalNode.routesPoolDist.get(col);
			double rco = GraphManager.finalNode.routesPoolRC.get(col);
			double tim = GraphManager.finalNode.routesPoolTime.get(col);
			double loa = GraphManager.finalNode.routesPoolLoad.get(col);
			
			
			//Put a 1 in the vector if the customer is there:
			
			col = col.substring(1,col.length()-1);
			String[] colSplit = col.split(", ");
			ArrayList<Integer> dummyPath = new ArrayList<>();
			dummyPath.add(0);
			for (int j = 1 ; j < colSplit.length-1; j++) {
				int node = Integer.parseInt(colSplit[j]);
				dummyPath.add(node);
				pattern[node-1] += 1;
				
			}
			dummyPath.add(0);
			
			// Creates the pattern:
				
			RoutePattern column=new RoutePattern("NewPatternPulse", false, pattern,cost,dummyPath,pricingProblem,rco,tim,loa);
				
			// Adds the pattern to the list:
				
			newPatterns.add(column);
			
		}
		
		// Returns the list of patterns:
		
		return newPatterns;
	}

	/**
	 * When the Pricing Problem is solved, the set objective function gets invoked first. 
	 */
	@Override
	protected void setObjective() {
		//In this case is empty
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}

	/**
	 * This method updates the graph, to account for the branching decisions.
	 * Forbidden arcs are marked using a matrix.
	 */
	@Override
	public void branchingDecisionPerformed(@SuppressWarnings("rawtypes") BranchingDecision bd) {
		
		if(bd instanceof FixArc) {
			FixArc fixArcDecision = (FixArc) bd;
			if(fixArcDecision.pricingProblem == this.pricingProblem) {
				VRPTW.forbiddenArcs.put(fixArcDecision.keyArc, 1);
				VRPTW.fixedArcs.put(fixArcDecision.keyArc, 1);
				int numD = fixArcDecision.heads_drive.size();
				for(int i = 0;i<numD;i++) {
					VRPTW.isForbidden[fixArcDecision.tails_drive.get(i)][fixArcDecision.heads_drive.get(i)]++;
				}
			}
		}else if(bd instanceof RemoveArc) {
			RemoveArc removeArcDecision = (RemoveArc) bd;
			if(removeArcDecision.pricingProblem == this.pricingProblem) {
				VRPTW.removedArcs.put(removeArcDecision.keyArc, 1);
				int numD = removeArcDecision.heads_drive.size();
				for(int i = 0;i<numD;i++) {
					VRPTW.isForbidden[removeArcDecision.tails_drive.get(i)][removeArcDecision.heads_drive.get(i)]++;
				}
			}
		}
	}

	/**
	 * This method unmarks arcs, when branching decisions are reversed:
	 */
	@Override
	public void branchingDecisionReversed(@SuppressWarnings("rawtypes") BranchingDecision bd) {
		if(bd instanceof FixArc) {
			FixArc fixArcDecision = (FixArc) bd;
			if(fixArcDecision.pricingProblem == this.pricingProblem) {
				VRPTW.forbiddenArcs.remove(fixArcDecision.keyArc);
				VRPTW.fixedArcs.remove(fixArcDecision.keyArc);
				int numD = fixArcDecision.heads_drive.size();
				for(int i = 0;i<numD;i++) {
					VRPTW.isForbidden[fixArcDecision.tails_drive.get(i)][fixArcDecision.heads_drive.get(i)]--;
				}
			}
		}else if(bd instanceof RemoveArc) {
			RemoveArc removeArcDecision = (RemoveArc) bd;
			if(removeArcDecision.pricingProblem == this.pricingProblem) {
				VRPTW.removedArcs.remove(removeArcDecision.keyArc);
				int numD = removeArcDecision.heads_drive.size();
				for(int i = 0;i<numD;i++) {
					VRPTW.isForbidden[removeArcDecision.tails_drive.get(i)][removeArcDecision.heads_drive.get(i)]--;
				}

			}
		}
		
	}
	
	
	
}
