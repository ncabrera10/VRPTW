package branchAndPrice;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

import org.jorlib.frameworks.columnGeneration.branchAndPrice.AbstractBranchAndPrice;
import org.jorlib.frameworks.columnGeneration.branchAndPrice.AbstractBranchCreator;
import org.jorlib.frameworks.columnGeneration.branchAndPrice.BAPNode;
import org.jorlib.frameworks.columnGeneration.branchAndPrice.EventHandling.CGListener;
import org.jorlib.frameworks.columnGeneration.colgenMain.ColGen;
import org.jorlib.frameworks.columnGeneration.io.TimeLimitExceededException;
import org.jorlib.frameworks.columnGeneration.master.OptimizationSense;
//import org.jorlib.frameworks.columnGeneration.master.cutGeneration.AbstractInequality;
import org.jorlib.frameworks.columnGeneration.pricing.AbstractPricingProblemSolver;

import columnGeneration.ColumnGenerationDirector;
import columnGeneration.Master;
import columnGeneration.VRPTW;
import parameters.CGParameters;
import parameters.GlobalParameters;
import columnGeneration.RoutePattern;
import pulseAlgorithm.PA_PricingProblem;

/**
 * This class extends the abstract branch and price class.
 * It has the methods to check if a solution is integer and the main logic 
 * that will be followed while evaluating a BAP node from the queue.
 * 
 * @author nicolas.cabrera-malik
 *
 */
public final class BranchAndPrice extends AbstractBranchAndPrice<VRPTW,RoutePattern,PA_PricingProblem> {

	/**
	 * This method creates a branch and price object and initializes the procedure
	 * @param modelData
	 * @param master
	 * @param pricingProblems
	 * @param solvers
	 * @param branchCreators
	 * @param objectiveInitialSolution
	 * @param initialSolution
	 */
	public BranchAndPrice(VRPTW modelData, Master master,List<PA_PricingProblem> pricingProblems,
			List<Class<? extends AbstractPricingProblemSolver<VRPTW,RoutePattern,PA_PricingProblem>>>solvers,
			List<? extends AbstractBranchCreator<VRPTW,RoutePattern,PA_PricingProblem>> branchCreators,
			double objectiveInitialSolution,
			List<RoutePattern> initialSolution) {
			super(modelData,master,pricingProblems,solvers,branchCreators,0,objectiveInitialSolution);
			this.warmStart(objectiveInitialSolution, initialSolution);
			this.setNodeOrder(new BapNodeComparator()); //To be able to switch between DFS and BFS
		
	}


	/**
	 * This method performs the warm start: Basically we use the initial columns to generate an upper bound
	 * @param objectiveInitialSolution
	 * @param initialSolution
	 */
	public void warmStart(double objectiveInitialSolution, List<RoutePattern> initialSolution) {
		rootNode=queue.peek();
		if(rootNode.nodeID != 0)
			throw new RuntimeException("This method can only be invoked at the start of the Branch-and-Price procedure, before runBranchAndPrice is invoked");
		rootNode.addInitialColumns(initialSolution);
		this.objectiveIncumbentSolution=objectiveInitialSolution;
		this.incumbentSolution=new ArrayList<>(initialSolution);
		if(optimizationSenseMaster==OptimizationSense.MINIMIZE)
			this.upperBoundOnObjective=objectiveInitialSolution;
		else
			this.lowerBoundOnObjective=objectiveInitialSolution;
	}

	/**
	 * This method is supposed to generate an initial solution. In this case,
	 * from the start we have one that applies to all BAP nodes.
	 * Thus, we return an empty list.
	 */
	@Override
	protected List<RoutePattern> generateInitialFeasibleSolution(BAPNode<VRPTW, RoutePattern> node) {
		// List of empty patterns
		return(new ArrayList<RoutePattern>());
	}

	/**
	 * This method checks if a solution is integer.
	 * If the solution is integer it returns TRUE.
	 * If the solution is not integer:
	 * 	it first adds all the columns generated at the BAP node to the pool of paths
	 * 	it returns FALSE.
	 */
	@Override
	protected boolean isIntegerNode(BAPNode<VRPTW, RoutePattern> node) {
		
		// We first assume that the current node is integer:
		
		boolean isInteger = true;
		
		// We check every route in the solution (to see if we can prove our assumption wrong:
		
		for(RoutePattern column : node.getSolution()) {
			
			// We check if the column has a value lower than 1:
			
			if(column.value < 1 -Math.pow(10,-GlobalParameters.PRECISION)) {
				
				//Add the columns we generated to the initial solution columns (For the children nodes)
				
				List<RoutePattern> routesToAdd = new ArrayList<RoutePattern>();
				for(RoutePattern route : master.getColumns(pricingProblems.get(0))) {
					if(!node.getInitialColumns().contains(route)) {
						routesToAdd.add(route);
					}
				}
				node.addInitialColumns(routesToAdd);
				return(false);
			}
		}
		return isInteger;
	}

	/**
	 * This method solves the CG procedure at a BAP node.
	 */
	@Override
	protected void solveBAPNode(BAPNode<VRPTW, RoutePattern> bapNode, long timeLimit) throws TimeLimitExceededException {
		ColGen<VRPTW, RoutePattern, PA_PricingProblem> cg = null;
		try {
			// Initializes the stabilized column generation object:
			
			cg = new ColumnGenerationDirector(dataModel, (Master) master, pricingProblems.get(0), solvers, bapNode.getInitialColumns(), objectiveIncumbentSolution, bapNode.getBound(),pricingProblems); //Solve the node
			for(CGListener listener : columnGenerationEventListeners) cg.addCGEventListener(listener);
			
			// Adds the cuts created/included by the parent node:
			
			master.addCuts(bapNode.getInitialInequalities());
			
			// Re-starts the cut iteration identifier
			
			VRPTW.cutIterationAtCurrentBAPNode = 0;
			VRPTW.bapNodeID = bapNode.nodeID;
			VRPTW.bapParentNodeID = bapNode.getParentID();
			VRPTW.bapDepthNode = bapNode.getNodeDepth();
			
			// Solves the CG: we can impose a time limit..
			
			cg.solve(timeLimit);
			
			if(CGParameters.PRINT_IN_CONSOLE) {
				for(RoutePattern column:cg.getSolution()) {
					System.out.println(column);
				}
			}
			
		}finally{
			//Update statistics
			if(cg != null) {
				timeSolvingMaster += cg.getMasterSolveTime();
				timeSolvingPricing += cg.getPricingSolveTime();
				totalNrIterations += cg.getNumberOfIterations();
				totalGeneratedColumns += cg.getNrGeneratedColumns();
				notifier.fireFinishCGEvent(bapNode, cg.getBound(), cg.getObjective(), cg.getNumberOfIterations(), cg.getMasterSolveTime(), cg.getPricingSolveTime(), cg.getNrGeneratedColumns());
			}
		}
		

		//Store the solution found:
		
		bapNode.storeSolution(cg.getObjective(), cg.getBound(), cg.getSolution(), cg.getCuts());
	}

	/**
	 * This method sets the exploration order
	 * @param comparator
	 */
	public void setNodeOrder(Comparator<BAPNode<VRPTW,RoutePattern>> comparator) {
		Queue<BAPNode<VRPTW,RoutePattern>> newQueue=new PriorityQueue<>(comparator);
		newQueue.addAll(queue);
		this.queue=newQueue;
	}

	/**
	 * Returns the lower bound (taking into account all the nodes we have explored so far).
	 * @return
	 */
	public double getLowerBoundOnObjective() {
		return(this.lowerBoundOnObjective);
	}
	
	/**
	 * Returns the upper bound (taking into account all the nodes we have explored so far).
	 * @return
	 */
	public double getUpperBoundOnObjective() {
		return(this.upperBoundOnObjective);
	}

	/**
	 * This method updates the incumbent solution
	 * @param objectiveValue
	 * @param solution
	 */
	public void updateIncumbent(double objectiveValue, List<RoutePattern> solution){
        this.objectiveIncumbentSolution = objectiveValue;
        this.upperBoundOnObjective = objectiveValue;
        this.incumbentSolution = solution;
    }
	
	
}
