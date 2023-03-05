package columnGeneration;

import java.util.ArrayList;
import java.util.Arrays;

import org.jorlib.frameworks.columnGeneration.colgenMain.AbstractColumn;

import pricingAlgorithms.PricingProblem;

/**
 * Implementation of a column in the VRPTW.
 * A column is a route visiting some customers respecting the time windows and the vehicle capacity.
 * 
 */

public final class RoutePattern extends AbstractColumn<VRPTW,PricingProblem>{

	/** Abstract Column - This class defines a column in the CG process. **/
	/** Defines a column. Each column has information regarding if a client is visited or not **/
	/** This information is stores in yield vector **/
	
	/**
	 * Vector that states if a customer is visited or not in the route
	 */
	public final int[] yieldVector;
	
	/**
	 * Total cost associated to this route
	 */
	public double cost;
	
	/**
	 * Total time associated to this route
	 */
	public double time;
	
	/**
	 * Total load associated to this route
	 */
	public double load;
	
	/**
	 * Reduced cost (when the path was created)
	 */
	public double reducedCost;
	
	/**
	 * ArrayList<Integer> associated to this route
	 */
	public ArrayList<Integer> route;
	
	/**
	 * Binary indicator: true if the column is artificial
	 */
	public boolean isArtif;
	
	/**
	 * Id of the route
	 */
	
	public int id;
	
	/**
	 * Binary indicator: true if the column is elementary
	 */
	
	public boolean isElementary;
	/**
	 * This method creates a route pattern
	 * @param creator
	 * @param isArtificial
	 * @param pattern
	 * @param totalCost
	 * @param r
	 * @param pricingProblem
	 * @param redco
	 */
	public RoutePattern(String creator, boolean isArtificial, int[] pattern, double totalCost,ArrayList<Integer> r,PricingProblem pricingProblem,double redco,double tim,double loa) {
		super(pricingProblem, isArtificial, creator);
		this.yieldVector=pattern;
		this.cost = totalCost;
		this.route = r;
		this.reducedCost = redco;
		this.time = tim;
		this.load = loa;
		if(creator.equals("Artificial")) {
			this.isArtif = true;
		}else {
			this.isArtif = false;
		}
		this.id = VRPTW.numColumns;
		VRPTW.numColumns++;
		
		/** String creator: textual description denoting who created the column (some algorithm,
		 * an initial solution, etc.). For debugging purposes.
		 * boolean isArtificial: artificial columns may be added to create an initial feasible solution to the MP.
		 * PricingProblem: the pricing problem to which this column belongs. **/

	}

	/** The equals and hashCode methods are important **/
	@Override
	public boolean equals(Object o) {
		if(this==o)
			return true;
		if(!(o instanceof RoutePattern))
			return false;
		RoutePattern other=(RoutePattern) o;
		return (other.cost == this.cost && Arrays.equals(this.yieldVector, other.yieldVector) && other.route.toString().equals(this.route.toString()));
	}

	@Override
	public int hashCode() {
		return id;
	}

	@Override
	public String toString() {
		return id+" - "+"with value: "+ this.value+" Route pattern: "+Arrays.toString(yieldVector)+" actual route: "+route.toString()+" cost: "+this.cost+" reduced cost (When created): "+this.reducedCost+" creator: "+ this.creator+" - time: "+time+" - load: "+load;
	}

	
}
