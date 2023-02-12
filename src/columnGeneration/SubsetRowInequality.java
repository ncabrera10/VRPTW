package columnGeneration;

import java.util.ArrayList;

import org.jorlib.frameworks.columnGeneration.master.cutGeneration.AbstractCutGenerator;
import org.jorlib.frameworks.columnGeneration.master.cutGeneration.AbstractInequality;

/**
 * Class represent a subset row inequality as proposed by Jepsen.
 * @author nicolas.cabrera-malik
 *
 */
public class SubsetRowInequality extends AbstractInequality implements Comparable<SubsetRowInequality>{
	
	/** Vertices in the cut set **/
	public final ArrayList<Integer> cutSet;
	
	/**
	 * List of routes that visit 2 or more customers of the triplet:
	 */
	public ArrayList<RoutePattern> routes;
	
	/**
	 * Coefficient for each route in the inequality:
	 */
	public ArrayList<Integer> coefficients;
	
	/**
	 * Routes id's for the routes that visit 2 or more customers of the triplet
	 */
	public ArrayList<Integer> routes_ids;
	
	/**
	 * Id of this inequality
	 */
	public final int id;
	
	/**
	 * Violation when added to the MP
	 */
	public final double violation;
	
	/**
	 * This method creates a new subset row inequality object
	 * @param maintainingGenerator
	 * @param cutSet
	 * @param routes
	 * @param coefficients
	 * @param id
	 * @param viol
	 * @param routes_id
	 */
	public SubsetRowInequality(AbstractCutGenerator<VRPTW,VRPTWMasterData> maintainingGenerator, ArrayList<Integer> cutSet,ArrayList<RoutePattern> routes,ArrayList<Integer>coefficients,int id,double viol,ArrayList<Integer>routes_id) {
		super(maintainingGenerator);
		this.cutSet=cutSet;
		this.routes=routes;
		this.routes_ids=routes_id;
		this.coefficients=coefficients;
		this.id=id;
		this.violation=viol;
	}

	@Override
	public boolean equals(Object o) {
		if(this==o)
			return true;
		else if(!(o instanceof SubsetRowInequality))
			return false;
		SubsetRowInequality other=(SubsetRowInequality)o;
		return this.cutSet.equals(other.cutSet);
	}

	@Override
	public int hashCode() {
		return cutSet.hashCode();
	}

	@Override
	public String toString() {
	
		String cut = "";
		for(int i=0;i<routes.size();i++) {
			cut += Math.floor(coefficients.get(i)/2)+"*X_"+routes.get(i).id+" +";
		}
		cut += "<= 1 | S={";
		for(int i=0;i<cutSet.size();i++) {
			cut += cutSet.get(i)+" , ";
		}
		cut += " }" + " with a violation of: "+violation;
		return "The cut: "+id+" - "+cut;
	}

	@Override
	public int compareTo(SubsetRowInequality o) {
		if(violation > o.violation) {
			return(-1);
		}
		return violation == o.violation?0:1;
	}

	public boolean containsRoute(int route_id) {
		for (final int i : routes_ids) {
	        if (i == route_id) {
	            return true;
	        }
	    }
	    return false;
	}
}
