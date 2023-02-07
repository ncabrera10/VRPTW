package branchAndPrice;

import java.util.ArrayList;
import java.util.List;

import org.jorlib.frameworks.columnGeneration.branchAndPrice.EventHandling.BAPListener;
import org.jorlib.frameworks.columnGeneration.branchAndPrice.EventHandling.BranchEvent;
import org.jorlib.frameworks.columnGeneration.branchAndPrice.EventHandling.FinishEvent;
import org.jorlib.frameworks.columnGeneration.branchAndPrice.EventHandling.FinishProcessingNodeEvent;
import org.jorlib.frameworks.columnGeneration.branchAndPrice.EventHandling.NodeIsFractionalEvent;
import org.jorlib.frameworks.columnGeneration.branchAndPrice.EventHandling.NodeIsInfeasibleEvent;
import org.jorlib.frameworks.columnGeneration.branchAndPrice.EventHandling.NodeIsIntegerEvent;
import org.jorlib.frameworks.columnGeneration.branchAndPrice.EventHandling.ProcessingNextNodeEvent;
import org.jorlib.frameworks.columnGeneration.branchAndPrice.EventHandling.PruneNodeEvent;
import org.jorlib.frameworks.columnGeneration.branchAndPrice.EventHandling.StartEvent;
import org.jorlib.frameworks.columnGeneration.branchAndPrice.EventHandling.TimeLimitExceededEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import columnGeneration.Master;
import columnGeneration.RoutePattern;
import dataStructures.DataHandler;
import ilog.concert.IloColumn;
import ilog.concert.IloException;

import ilog.concert.IloNumVar;
import ilog.concert.IloObjective;
import ilog.concert.IloRange;
import ilog.cplex.IloCplex;
import parameters.CGParameters;
import parameters.GlobalParameters;

public class IntegerHeuristic implements BAPListener {

	 protected final Logger logger = LoggerFactory.getLogger(IntegerHeuristic.class);

	 private final BranchAndPrice bap;
	 private int sizePoolIntegerEvaluation = 0;
	 
	public IntegerHeuristic(BranchAndPrice bap) {
		this.bap = bap;
	}
	
	 @SuppressWarnings("deprecation")
	 private void computeHeuristicSolution(List<RoutePattern> columns) throws IloException {
	        
		 //Compute the solution
		 	
		 	try (IloCplex cplex = new IloCplex()) {
				cplex.setOut(null); //Disable cplex output
				
				//cplex.setParam(IloCplex.IntParam.Threads, config.MAXTHREADS); //Set number of threads that may be used by the cplex
				cplex.setParam(IloCplex.IntParam.Threads, GlobalParameters.THREADS); //Set number of threads that may be used by the cplex
				
				//Define the objective
				IloObjective obj= cplex.addMinimize();

				//Define constraints (visit all customers exactly once)
				IloRange[] satisfyDemandConstr=new IloRange[DataHandler.n];
				for(int i=0; i< DataHandler.n; i++)
					satisfyDemandConstr[i]= cplex.addRange(1,Double.MAX_VALUE, "satisfyDemandFinal_"+i);


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

				// Solve model
				List<RoutePattern> sol = new ArrayList<RoutePattern>();
				try {
					if (cplex.solve()) {
				    	
				    	for(int i=0;i<counter;i++) {
							if(cplex.getValue(vars[i]) > 0.5) {
								RoutePattern column = columns.get(i);
								sol.add(column);
							}
				    	}

				        if (cplex.getObjValue() < bap.getObjective()){
				        	if(CGParameters.PRINT_IN_CONSOLE) {
				        		  logger.debug("IntegerObjective;"+cplex.getObjValue());
				        	}
				          
				            bap.updateIncumbent(cplex.getObjValue(), sol);
				        }else {
				        	if(CGParameters.PRINT_IN_CONSOLE) {
				        	 logger.debug("CurrentIntegerObjective;"+cplex.getObjValue());
				        	}
				        }
				    }
				}
				catch(Exception e) {
					System.out.println("Problem with the integer heuristic");
				}
			}
	    }
	
	
	
	@Override
	public void branchCreated(BranchEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void finishBAP(FinishEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	 public void finishedColumnGenerationForNode(FinishProcessingNodeEvent finishProcessingNodeEvent) {
	       
		 	// If sufficient number of new columns generated
	        if(finishProcessingNodeEvent.node.nodeID == 0 || bap.getTotalGeneratedColumns() - Master.getPaths().size() >= 50)
	        {
	            try {
	                long s = System.currentTimeMillis();
	                computeHeuristicSolution(new ArrayList<>(Master.getPaths()));
	                if(CGParameters.PRINT_IN_CONSOLE) {
	                	logger.info(String.format("IPsolved;%.4f;", (System.currentTimeMillis()-s)/1000.0));
	                }
	                setSizePoolIntegerEvaluation(Master.getPaths().size());
	            } catch (IloException e) {
	                throw new RuntimeException("Integer model issues");
	            }
	        }
	    }

	@Override
	public void nodeIsFractional(NodeIsFractionalEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void nodeIsInfeasible(NodeIsInfeasibleEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void nodeIsInteger(NodeIsIntegerEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void processNextNode(ProcessingNextNodeEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pruneNode(PruneNodeEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void startBAP(StartEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void timeLimitExceeded(TimeLimitExceededEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public int getSizePoolIntegerEvaluation() {
		return sizePoolIntegerEvaluation;
	}

	public void setSizePoolIntegerEvaluation(int sizePoolIntegerEvaluation) {
		this.sizePoolIntegerEvaluation = sizePoolIntegerEvaluation;
	}

}
