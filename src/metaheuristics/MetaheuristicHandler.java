package metaheuristics;

import java.util.ArrayList;
import java.util.Hashtable;

/**
 * This Class will carry out the data structures for keep
 * track of the pool is been encountered. Adapted from the code of Daniel Duque. 
 *
 */
public class MetaheuristicHandler {

	/**
	 * Reduced cost associated to each route in the pool
	 */
	public Hashtable<String, Double> routesPoolRC;
	
	/**
	 * Distance associated to each route in the pool
	 */
	public Hashtable<String, Double> routesPoolDist;
	
	/**
	 * Returns the pool
	 */
	public ArrayList<String> pool;
	
	/**
	 * Stores a value indicating 
	 */
	public Hashtable<String, Integer> generator;
	
	/**
	 * Creates a new metaheuristic handler
	 */
	public MetaheuristicHandler() {
		pool = new ArrayList<>();
		routesPoolRC = new Hashtable<>(1000); //We set the initial capacity to 1000
		routesPoolDist = new Hashtable<>(1000); //We set the initial capacity to 1000
		generator = new Hashtable<>(1000); //We set the initial capacity to 1000
	}
	
	/**
	 * Hash table of routes and reduced cost
	 * @return routesPoolRC
	 */
	public Hashtable<String, Double> getHeuPoolRC(){
		return routesPoolRC;
	}

	/**
	 * Hash table of routes and distance
	 * @return routesPoolDist
	 */
	public Hashtable<String, Double> getHeuPoolDist(){
		return routesPoolDist;
	}
	
	/**
	 * Returns the columns found
	 * @return
	 */
	public ArrayList<String> getPoolCols(){
		return pool;
	}
	
	/**
	 * Stores the generator hashtable
	 * @return
	 */
	public Hashtable<String, Integer> getColsGenerator(){
		return generator;
	}
	
	/**
	 * This method resets the current pool
	 */
	public void resetPool() {
		
		pool = new ArrayList<>();
		generator = new Hashtable<>();
		routesPoolRC = new Hashtable<>(1000);
		routesPoolDist = new Hashtable<>(1000);
		
	}
	
	/**
	 * This method sorts the list
	 * @param set
	 */
	public void Sort(ArrayList<String> set) {
		QS(set, 0, set.size() - 1);
	}

	/**
	 * Method to exchange two positions
	 * @param e
	 * @param b
	 * @param t
	 * @return
	 */
	public int colocar(ArrayList<String> e, int b, int t) {
		int i;
		int pivote;
		double valor_pivote;
		String temp;

		pivote = b;
		valor_pivote = routesPoolRC.get(e.get(pivote)) ;
		for (i = b + 1; i <= t; i++) {
			if (routesPoolRC.get( e.get(i)) < valor_pivote) {
				pivote++;
				temp = e.get(i);
				e.set(i, e.get(pivote));
				e.set(pivote,temp);
			}
		}
		temp =  e.get(b);
		e.set(b, e.get(pivote));
		e.set(pivote,temp);
		return pivote;
	}

	/**
	 * QS
	 * @param e
	 * @param b
	 * @param t
	 */
	public void QS(ArrayList<String> e, int b, int t) {
		int pivote;
		if (b < t) {
			pivote = colocar(e, b, t);
			QS(e, b, pivote - 1);
			QS(e, pivote + 1, t);
		}
	}
	
	
}
