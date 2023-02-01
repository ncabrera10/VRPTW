package branchAndPrice;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jorlib.frameworks.columnGeneration.branchAndPrice.AbstractBranchCreator;
import org.jorlib.frameworks.columnGeneration.master.cutGeneration.CutHandler;
//import org.jorlib.frameworks.columnGeneration.colgenMain.ColGen;
//import org.jorlib.frameworks.columnGeneration.io.SimpleBAPLogger;
//import org.jorlib.frameworks.columnGeneration.io.SimpleCGLogger;
//import org.jorlib.frameworks.columnGeneration.io.SimpleDebugger;
//import org.jorlib.frameworks.columnGeneration.io.SimpleDebugger;
//import org.jorlib.frameworks.columnGeneration.io.TimeLimitExceededException;
import org.jorlib.frameworks.columnGeneration.pricing.AbstractPricingProblemSolver;

import columnGeneration.Master;
import columnGeneration.VRPTW;
import dataStructures.DataHandler;
import columnGeneration.PLRPMasterData;
import columnGeneration.RoutePattern;
import columnGeneration.SubsetRowInequalityGenerator;
//import dataStructures.DataHandler;
import globalParameters.CGParameters;
import globalParameters.GlobalParameters;
import ilog.concert.IloColumn;
import ilog.concert.IloException;
import ilog.concert.IloNumVar;
import ilog.concert.IloObjective;
import ilog.concert.IloRange;
import ilog.cplex.IloCplex;
import pulseAlgorithm.PA_PricingProblem;
import pulseAlgorithm.PA_Solver;

/**
 * Simple solver class which solves the PLRP Problem through Column Generation.
 * 
 */
public final class VRPTWSolver {

	/**
	 * The main data storage:
	 */
	private final VRPTW dataModel;

	/**
	 * Upper bound on the objective function
	 */
	private double upperBound;
	
	/**
	 * Lower bound on the objective function
	 */
	private double lowerBound;
	
	/**
	 * Number of paths found
	 */
	private int numberOfPaths;
	
	/**
	 * Number of column generations done
	 */
	private int numberOfIterations;
	
	/**
	 * Number of nodes processed
	 */
	private int numberOfProcessedNodes;
	
	/**
	 * Number of columns created 
	 */
	private int numberOfColumnsCreated;
	
	/**
	 * Creates a PLRP solver
	 * @param dataModel
	 */
	public VRPTWSolver(VRPTW dataModel){
		
		//Initializes the data model
		
		this.dataModel = dataModel;
		
		//Create a cutHandler, then create a Subtour AbstractInequality Generator and add it to the handler
		
		CutHandler<VRPTW,PLRPMasterData> cutHandler = new CutHandler<>();
		SubsetRowInequalityGenerator cutGen = new SubsetRowInequalityGenerator(dataModel);
		cutHandler.addCutGenerator(cutGen);
							
		//Create the pricing problem
	
		PA_PricingProblem pricingProblem = new PA_PricingProblem(dataModel, "PA");
		List<PA_PricingProblem> pricingProblems = new ArrayList<>();
		pricingProblems.add(pricingProblem);
		
		//Create the master problem
		
		Master master=new Master(dataModel, pricingProblem,cutHandler);
		master.initializeMaster(DataHandler.n);
		
		//Define which solvers to use
		
		List<Class<? extends AbstractPricingProblemSolver<VRPTW, RoutePattern, PA_PricingProblem>>> solvers= Collections.singletonList(PA_Solver.class);

		//Define an upper bound (stronger is better). In this case we simply sum the demands, i.e. cut each final from its own raw (Rather poor initial solution).
		
		double upperBound=Double.MAX_VALUE;

		//Create a set of initial columns.
		
		List<RoutePattern> initSolution=master.getInitialSolution(pricingProblem);
		
		//Lower bound on column generation solution (stronger is better): calculate least amount of finals needed to fulfil the order (ceil(\sum_j d_j*w_j /L)
		
		//double lowerBound=DataHandler.getFixCost()*OriginalGraph.getMinRoutes();
			
		//Define Branch creators
		List<? extends AbstractBranchCreator<VRPTW,RoutePattern,PA_PricingProblem>> branchCreators= Collections.singletonList(new BranchOnArc(dataModel, pricingProblems));

		
		//Create a branch and price instance 
		BranchAndPrice bap = new BranchAndPrice(dataModel,master,pricingProblems,solvers,branchCreators,upperBound,initSolution);
		
		
		/**
		 * The ColGen class manages the entire procedure by invoking the MP and PP iteratively. 
		 */

		//OPTIONAL: add a debugger
		
		if(CGParameters.PRINT_IN_CONSOLE) {
			@SuppressWarnings("unused")
			SimpleDebugger debugger=new SimpleDebugger(bap,true);
			//OPTIONAL: add a logger
			@SuppressWarnings("unused")
			SimpleBAPLogger logger=new SimpleBAPLogger(bap, new File("./output/Logfile-"+DataHandler.instanceType+DataHandler.instanceNumber+".log"));

		}
		
		
		bap.runBranchAndPrice(System.currentTimeMillis()+CGParameters.BAP_TIME_LIMIT_SEC*1000);

		//Store some relevant info:
		
		//this.setUpperBound(cg.getBound());
		this.setLowerBound(bap.getLowerBoundOnObjective());
		this.setNumberOfIterations(bap.getTotalNrIterations());
		this.setNumberOfPaths(initSolution.size());
		this.setNumberOfColumnsCreated(bap.getTotalGeneratedColumns());
		this.setNumberOfProcessedNodes(bap.getNumberOfProcessedNodes());
		
		//Print solution:
		if(CGParameters.PRINT_IN_CONSOLE) {
			System.out.println("================ Solution ================");
			//List<RoutePattern> solution=bap.getSolution();
			System.out.println("BAP Upper bound: "+bap.getUpperBoundOnObjective());
			System.out.println("BAP Lower bound: "+bap.getLowerBoundOnObjective());
			System.out.println("Number of iterations: "+bap.getTotalNrIterations());
			System.out.println("Time spent on master: "+bap.getMasterSolveTime()+" time spent on pricing: "+bap.getPricingSolveTime());
			if(bap.hasSolution()) {
				System.out.println("Solution is optimal: "+bap.isOptimal());
				System.out.println("Columns (only non-zero columns are returned):");
				//for(RoutePattern column : solution) {
					//if(column.value > 0) {
						//System.out.println(column);
					//}
				//}
			}	
		}
		try {
			printFinalIntegerSolution(bap.getSolution());
		} catch (IloException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//Clean up:
		bap.close(); //This closes both the master and pricing problems
	}

	/**
	 * The main method (in case we want to run it from here..)
	 * @param args
	 */

	public static void main(String[] args){
		VRPTW cs=new VRPTW();
		new VRPTWSolver(cs);
	}


	/**
	 * Returns the data model
	 * @return
	 */
	public VRPTW getDataModel() {
		return dataModel;
	}

	/**
	 * @return the upperBound
	 */
	public double getUpperBound() {
		return upperBound;
	}

	/**
	 * @param upperBound the upperBound to set
	 */
	public void setUpperBound(double upperBound) {
		this.upperBound = upperBound;
	}

	/**
	 * @return the lowerBound
	 */
	public double getLowerBound() {
		return lowerBound;
	}

	/**
	 * @param lowerBound the lowerBound to set
	 */
	public void setLowerBound(double lowerBound) {
		this.lowerBound = lowerBound;
	}

	/**
	 * @return the numberOfPaths
	 */
	public int getNumberOfPaths() {
		return numberOfPaths;
	}

	/**
	 * @param numberOfPaths the numberOfPaths to set
	 */
	public void setNumberOfPaths(int numberOfPaths) {
		this.numberOfPaths = numberOfPaths;
	}

	/**
	 * @return the numberOfIterations
	 */
	public int getNumberOfIterations() {
		return numberOfIterations;
	}

	/**
	 * @param numberOfIterations the numberOfIterations to set
	 */
	public void setNumberOfIterations(int numberOfIterations) {
		this.numberOfIterations = numberOfIterations;
	}
	
	
	
	/**
	 * @return the numberOfProcessedNodes
	 */
	public int getNumberOfProcessedNodes() {
		return numberOfProcessedNodes;
	}

	/**
	 * @param numberOfProcessedNodes the numberOfProcessedNodes to set
	 */
	public void setNumberOfProcessedNodes(int numberOfProcessedNodes) {
		this.numberOfProcessedNodes = numberOfProcessedNodes;
	}

	/**
	 * @return the numberOfColumnsCreated
	 */
	public int getNumberOfColumnsCreated() {
		return numberOfColumnsCreated;
	}

	/**
	 * @param numberOfColumnsCreated the numberOfColumnsCreated to set
	 */
	public void setNumberOfColumnsCreated(int numberOfColumnsCreated) {
		this.numberOfColumnsCreated = numberOfColumnsCreated;
	}

	/**
	 * This method prints a txt file with the final solution that can be used on the shiny website.
	 * @param columns
	 * @throws IloException
	 */
	@SuppressWarnings("deprecation")
	public void printFinalIntegerSolution(List<RoutePattern> columns) throws IloException {
		
		//1. Solve the master problem with the final pool of columns, imposing the integrality conditions.
		
		//1.1 Create an empty model:
		
			IloCplex cplex =new IloCplex(); //Create cplex instance
			cplex.setOut(null); //Disable cplex output
			//cplex.setParam(IloCplex.IntParam.Threads, config.MAXTHREADS); //Set number of threads that may be used by the cplex
			cplex.setParam(IloCplex.IntParam.Threads, GlobalParameters.THREADS); //Set number of threads that may be used by the cplex
			
			//Define the objective
			IloObjective obj= cplex.addMinimize();
	
			//Define constraints (visit all customers exactly once)
			IloRange[] satisfyDemandConstr=new IloRange[DataHandler.n];
			for(int i=0; i< DataHandler.n; i++)
				satisfyDemandConstr[i]= cplex.addRange(1,Double.MAX_VALUE, "satisfyDemandFinal_"+i);
		
		//1.2 Add the variables:
			
			IloNumVar[] vars = new IloNumVar[columns.size()];
			int counter = 0;
			for(RoutePattern column : columns) {
				
				
				IloColumn iloColumn= cplex.column(obj,column.cost);

				//Register column with demand constraint
				for(int i=0; i< DataHandler.n; i++)
					iloColumn=iloColumn.and(cplex.column(satisfyDemandConstr[i], column.yieldVector[i]));

				//Create the variable and store it
				IloNumVar var= cplex.boolVar(iloColumn, "z_"+","+counter);
				cplex.add(var);
				vars[counter] = var;
				counter++;
				
			}

		//1.3 Solve the model and print the solution:
			
			String ruta = globalParameters.GlobalParameters.RESULT_FOLDER+"BPC/Solution-"+DataHandler.instanceType+"-"+DataHandler.instanceNumber+"_"+DataHandler.n+"_"+CGParameters.CONFIGURATION+".txt";
			PrintWriter pw;
			try {
				pw = new PrintWriter(new File(ruta));
				if(CGParameters.PRINT_IN_CONSOLE) {
					System.out.println("================ Integer Solution ================");	
				}
				double totCost = 0.0;
				cplex.setOut(null);
				if(cplex.solve()) {
					for(int i=0;i<counter;i++) {
						if(cplex.getValue(vars[i]) > 0.5) {
							RoutePattern column = columns.get(i);
							totCost += column.cost;
							if(CGParameters.PRINT_IN_CONSOLE) {
								column.value = 1.0;
								System.out.println(column);
								column.value = 0.0;
							}
							
							String currentRoute = column.route;
							String[] parts = currentRoute.split(";");

							for(int j=0;j<parts.length;j++) {
								
								String currentArc = parts[j];
								currentArc = currentArc.replaceAll("[()]", "");
								String[] currentNodes = currentArc.split("-");
								pw.println((Integer.parseInt(currentNodes[0])+1)+";"+(Integer.parseInt(currentNodes[1])+1)+";1;"+i);
								
							}
						}

					}
				}
				if(CGParameters.PRINT_IN_CONSOLE) {
					System.out.println("Final upper bound: "+totCost);
					
				}
				this.setUpperBound(totCost);
				pw.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			cplex.close();
	}
}
