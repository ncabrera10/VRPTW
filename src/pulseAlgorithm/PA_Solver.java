package pulseAlgorithm;

import java.util.ArrayList;
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
import columnGeneration.RoutePattern;

/**
 * This class provides a solver for the PLRP pricing problem.
 * The pricing problem is an Elementary shortest path with resource constraints, replenishment and park-and-loop
 * 
 */

/**
 * The AbstractPricingProblem serves as a container to hold the dual information coming from the MP. 
 * It defines a Pricing Problem which can be solved by some algorithm. 
 * In our case, the Pricing Problem class maintains the dual values.
 */

public final class PA_Solver extends AbstractPricingProblemSolver<VRPTW, RoutePattern, PA_PricingProblem> {

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
	public PA_Solver(VRPTW dataModel, PA_PricingProblem pricingProblem) {
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
		
		
		// Tries to run the pulse algorithm: 
		
		try {

			//1. Creates and runs the auxiliary problem:
			
			new PulseHandler(Master.getDuals_subset(),Master.getDuals());

			// Compute the list of paths
				
			newPatterns = createListOfElementaryPatterns();
			
			//Assume it was feasible unless we prove otherwise.
			
			this.pricingProblemInfeasible=false;
			
			//3. If solved to optimality.
			
			if(PulseHandler.isWasSolvedToOptimality()) {
				this.pricingProblemInfeasible=false;
				PA_Solver.solvedToOptimality = true;
				PA_Solver.objectiveFunction = PulseHandler.getPrimalBound();	
				this.objective = PulseHandler.getPrimalBound();	
			}else {
				PA_Solver.solvedToOptimality = false;
			}
			
			//If the number of paths was zero, the problem was indeed infeasible!
			
			if(newPatterns.size() == 0) {
				pricingProblemInfeasible=true;
				this.objective=Double.MAX_VALUE;
			}
		}
		catch(Exception e) {
			e.printStackTrace();
			System.out.println("Problem running the pulse");
			System.exit(0);
		}
	
		return newPatterns;
	}
	
	public List<RoutePattern> createListOfElementaryPatterns(){
		
		List<RoutePattern> newPatterns=new ArrayList<>();
		
		//4. Adds all the paths:
		
		for(int i=0;i<GraphManager.finalNode.pool.size();i++) {
			
			//Create the actual pattern:
			
			int[] pattern=new int[DataHandler.n];
			for(int j=0;j<DataHandler.n;j++) {
				pattern[j] = 0;
			}
			
			String col = GraphManager.finalNode.pool.get(i);
			
			//Capture the cost and the reduced cost:
			
			double cost = GraphManager.finalNode.routesPoolDist.get(col);
			double rco = GraphManager.finalNode.routesPoolRC.get(col);
			
			//Put a 1 in the vector if the customer is there.
			
			
			col = col.substring(1,col.length()-1);
			String[] colSplit = col.split(", ");
			ArrayList<Integer> dummyPath = new ArrayList<>();
			dummyPath.add(0);
			for (int j = 1 ; j < colSplit.length-1; j++) {
				int node = Integer.parseInt(colSplit[j]);
				dummyPath.add(node);
				pattern[node-1] = 1;
			}
			dummyPath.add(0);
			
			//Creates the pattern
				
			RoutePattern column=new RoutePattern("NewPatternPulse", false, pattern,cost,dummyPath,pricingProblem,rco);
				
			//Adds the pattern to the list
				
			newPatterns.add(column);
			
		}
		
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
