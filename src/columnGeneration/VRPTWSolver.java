package columnGeneration;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jorlib.frameworks.columnGeneration.colgenMain.ColGen;
import org.jorlib.frameworks.columnGeneration.io.SimpleCGLogger;
import org.jorlib.frameworks.columnGeneration.io.SimpleDebugger;
//import org.jorlib.frameworks.columnGeneration.io.SimpleDebugger;
import org.jorlib.frameworks.columnGeneration.io.TimeLimitExceededException;
import org.jorlib.frameworks.columnGeneration.master.cutGeneration.CutHandler;
import org.jorlib.frameworks.columnGeneration.pricing.AbstractPricingProblemSolver;

import dataStructures.DataHandler;
import ilog.concert.IloColumn;
import ilog.concert.IloException;
import ilog.concert.IloNumVar;
import ilog.concert.IloObjective;
import ilog.concert.IloRange;
import ilog.cplex.IloCplex;
import parameters.CGParameters;
import parameters.GlobalParameters;
import pricingAlgorithms.PA_PricingProblem;
import pricingAlgorithms.PA_Solver;

/**
 * Simple solver class which solves the VRPTW Problem through Column Generation.
 * 
 * @author nicolas.cabrera-malik
 *
 */
public final class VRPTWSolver {

	/**
	 * The main data storage:
	 */
	private final VRPTW dataModel;

	/**
	 * Upper bound for the CG
	 */
	private double upperBound;
	
	/**
	 * Lower bound for the CG
	 */
	private double lowerBound;

	/**
	 * Number of CG iterations
	 */
	private int numberOfIterations;
	
	/**
	 * Creates a PLRP solver
	 * @param dataModel
	 */
	public VRPTWSolver(VRPTW dataModel){
		
		//Initializes the data model
		
		this.dataModel = dataModel;

		//Create a cutHandler, then create a Subtour AbstractInequality Generator and add it to the handler
		
		CutHandler<VRPTW,VRPTWMasterData> cutHandler = new CutHandler<>();
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
		
		double upperBound = Double.MAX_VALUE;

		//Create a set of initial columns.
		
		List<RoutePattern> initSolution=master.getInitialSolution(pricingProblem);
		
		//Lower bound on column generation solution (stronger is better): calculate least amount of finals needed to fulfil the order (ceil(\sum_j d_j*w_j /L)
		
		double lowerBound = 0;
				
		//Create a column generation instance
		
		ColGen<VRPTW, RoutePattern, PA_PricingProblem> cg = new ColumnGenerationDirector(dataModel, master, pricingProblem, solvers, initSolution, upperBound, lowerBound,pricingProblems);
		
		/**
		 * The ColGen class manages the entire procedure by invoking the MP and PP iteratively. 
		 */

		//OPTIONAL: add a debugger
		
		if(CGParameters.PRINT_IN_CONSOLE) {
			@SuppressWarnings("unused")
			SimpleDebugger debugger=new SimpleDebugger(cg);
			
			//OPTIONAL: add a logger
			@SuppressWarnings("unused")
			SimpleCGLogger logger=new SimpleCGLogger(cg, new File("./output/VRPTW.log"));

		}
		
		
		//Solve the problem through column generation
		try {
			cg.solve(System.currentTimeMillis()+10000000L);
			
			//Store some relevant info:
			
			//this.setUpperBound(cg.getBound());
			this.setLowerBound(cg.getObjective());
			this.setNumberOfIterations(cg.getNumberOfIterations());
			
		} catch (TimeLimitExceededException e) {
			e.printStackTrace();
		}
		//Print solution:
		
		if(CGParameters.PRINT_IN_CONSOLE) {
			System.out.println("================ CG metrics ================");
			List<RoutePattern> solution=cg.getSolution();
			System.out.println("CG terminated with objective: "+cg.getObjective());
			System.out.println("Number of iterations: "+cg.getNumberOfIterations());
			System.out.println("Time spent on master: "+cg.getMasterSolveTime()+" time spent on pricing: "+cg.getPricingSolveTime());
			System.out.println("================ Root node solution ================");
			for(RoutePattern column : solution)
				System.out.println(column);
			
		}
		try {
			printFinalIntegerSolution(master.getAllColumns());
		} catch (IloException e) {
			e.printStackTrace();
		}
		
		//Clean up:
		cg.close(); //This closes both the master and pricing problems
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
			
			String ruta = parameters.GlobalParameters.RESULT_FOLDER+"RMP/Solution-"+DataHandler.instanceType+"-"+DataHandler.instanceNumber+"_"+DataHandler.n+"_"+CGParameters.CONFIGURATION+".txt";
			
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
						
						RoutePattern column = columns.get(i);
						// We print all the paths for the BAP:
						ArrayList<Integer> currentRoute = column.route;
		
						// If the path is on the final solution:
						if(cplex.getValue(vars[i]) > 0.5) {
							totCost += column.cost;
							if(CGParameters.PRINT_IN_CONSOLE) {
								column.value = 1.0;
								System.out.println(column);
								column.value = 0.0;
							}

							for(int j=0;j<currentRoute.size()-1;j++) {
								pw.println((currentRoute.get(j)+1)+";"+(currentRoute.get(j+1)+1)+";1;"+i);
								
							}
						}

					}
				}
				upperBound = totCost;
				if(CGParameters.PRINT_IN_CONSOLE) {
					System.out.println("Final upper bound: "+totCost);
					
				}
				pw.close();
				
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			
			
			cplex.close();
	}
	
}
