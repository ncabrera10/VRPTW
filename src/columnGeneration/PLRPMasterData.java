package columnGeneration;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import org.jorlib.frameworks.columnGeneration.master.MasterData;
import org.jorlib.frameworks.columnGeneration.util.OrderedBiMap;

import ilog.concert.IloNumVar;
import ilog.concert.IloRange;
import ilog.cplex.IloCplex;
import pulseAlgorithm.PA_PricingProblem;

/**
 * Data container for the master problem.
 */

/**
 * Note: data originating from the MP (e.g., its variables) may be required by other classes. A shared 
 * object which extends AbstractMasterData is created for this purpose in the buildModel method of the 
 * MP. For the CuttingStock we only store the variables in this object.  
 */


public final class PLRPMasterData extends MasterData<VRPTW, RoutePattern, PA_PricingProblem, IloNumVar>{

	/** Mapping of the Subtour inequalities to constraints in the cplex model **/
	public final Map<SubsetRowInequality, IloRange> subsetRowInequalities;
	/** Cplex instance **/
	public final IloCplex cplex;
	
	public ArrayList<RoutePattern> currentSolution;
	
	public PLRPMasterData(IloCplex cplex,Map<PA_PricingProblem, OrderedBiMap<RoutePattern, IloNumVar>> varMap) {
		super(varMap);
		subsetRowInequalities=new LinkedHashMap<>();
		this.cplex=cplex;
	}

	@Override
	public void addColumn(RoutePattern column, IloNumVar variable) {
		if(varMap.get(column.associatedPricingProblem).containsKey(column))
			throw new RuntimeException("Duplicate column has been generated for pricing problem: "+column.associatedPricingProblem.toString()+"! This column already exists and by definition should not have negative reduced cost: "+column+" - "+varMap.get(column.associatedPricingProblem).get(column));
		else
			varMap.get(column.associatedPricingProblem).put(column, variable);
	}
	
	
}
