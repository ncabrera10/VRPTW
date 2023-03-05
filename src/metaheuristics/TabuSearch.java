package metaheuristics;

/**
 * This class contains the main logic of a tabu search. It can be used to provide an initial pool of columns or across every iteration of the CG 
 * (before adding cuts)
 * 
 */

import java.util.ArrayList;
import java.util.Random;

import columnGeneration.Master;
import dataStructures.DataHandler;
import dataStructures.GraphManager;
import dataStructures.Node;
import parameters.CGParameters;
import utilities.Clonetor;
import utilities.Rounder;

public class TabuSearch {

	private static final int REMOVE = 0;
	private static final int INSERT = 1; 
	
	// s'-------------------------------------------
	private ArrayList<Node> routes;
	private double routesDist;
	private double routesCost;
	private int routesLoad;
	private Node[] customers;
	private static ArrayList<Node> requestBank;
	static double totalDistance;
	static double FO;
	// ---------------------------------------------


	private MetaheuristicHandler metaheuristicHandler;
	private int[]operatorStage;
	private int[]operatorPerformed;
	int forbiden = 5;
	
	private int tsIterations=0;
	private  double shift;
	
	Rounder nt;
	
	/**
	 * Creates a new tabu search object
	 * @param heu
	 */
	public TabuSearch(MetaheuristicHandler heu) {
		metaheuristicHandler = heu;
		shift = 0;
		nt = new Rounder();
	}
	
	
	public void run(ArrayList<Integer>basisRoutes, double shift){
		
		this.forbiden = 10;
		this.shift = shift;
		Random r = new Random(0);
		for (int i = 0; i < basisRoutes.size(); i++) { 
			genCopy();
			tsIterations = 0;
			resetTabuSearch();
			ArrayList<Integer> route = Master.getPaths().get(basisRoutes.get(i)).route;
			setRoute(route, basisRoutes.get(i)); //basisRoutes.get(i)
		
			while(tsIterations<CGParameters.MAX_ITERATIONS_TS){
				generateNeighbors();
				updateTabus();
				tsIterations++;
			}
		
		//Random route
		for(int k=0; k<2; k++){	
			genCopy();
			tsIterations = 0;
			resetTabuSearch();
			
			int q = r.nextInt(Master.getPaths().size());
		
			route = Master.getPaths().get(q).route; 			
			setRoute(route, q); 
		
			while(tsIterations<CGParameters.MAX_ITERATIONS_TS){
				generateNeighbors();
				updateTabus();
				tsIterations++;
			}
		}
		
		}
	}
	
	/**
	 * This method sets a route
	 * @param route
	 * @param varIndex
	 */
	private void setRoute(ArrayList<Integer> route, int varIndex) {
		routes = new ArrayList<Node>();
		routes.add(customers[0]);
		Node a = (Node) customers[0].clone();
		routes.add(a);
		for (int i = 1; i < route.size()-1; i++) {
			int nodeID = route.get(i);
			requestBank.remove(customers[nodeID]);
			routes.add(i,customers[nodeID]);
		}
		
		upDateRoutes("SetRoute", varIndex);
	}

	/**
	 * This method updates the tabus
	 */
	private void updateTabus() {
		for (int i = 0; i < operatorPerformed.length; i++) {
			if(operatorPerformed[i]!=-1){
				operatorStage[i]++;
			}
			if(operatorStage[i]>= forbiden){
				operatorPerformed[i] = -1;
				operatorStage[i] = 0; 
			}
		}
		
	}

	/**
	 * This method resets the tabu search
	 */
	private void resetTabuSearch() {
		operatorPerformed = new int[DataHandler.n+1];
		operatorStage = new int[DataHandler.n+1];
		for (int i = 0; i < operatorPerformed.length; i++) {
				operatorPerformed[i] = -1;
				operatorStage[i] = 0; 
		}
	}

	/**
	 * This method generates new neighborhoods
	 */
	private void generateNeighbors() {
		int nodeChanged = -1;
		int operator = -1;
		double minDelta = Double.POSITIVE_INFINITY;
		ArrayList<Node> r = routes;

		for (int i = 1; i < r.size() - 1; i++) {
			if (notTabu(r.get(i).id, REMOVE)) {
				double delta = -DataHandler.cost[r.get(i).id][r.get(i + 1).id]- DataHandler.cost[r.get(i - 1).id][r.get(i).id] + DataHandler.cost[r.get(i - 1).id][r.get(i + 1).id];
				if (delta < minDelta) {
					minDelta = delta;
					nodeChanged = r.get(i).id;
					operator = REMOVE;
				}
			}
		}
		int rbIndex = -1;
		int minPos = -1;
		for (int ib = 0; ib < requestBank.size(); ib++) {
			Node rc = requestBank.get(ib);
			if (notTabu(rc.id, INSERT)) {
				if (routesLoad + rc.demand <= DataHandler.Q) {
					for (int i = 1; i < r.size(); i++) {
						double foDelta = DataHandler.cost[r.get(i - 1).id][rc.id] + DataHandler.cost[rc.id][r.get(i).id] - DataHandler.cost[r.get(i - 1).id][r.get(i).id];
						if (foDelta < minDelta) {
							if (checkInsertionFeasibility(i, rc , r )) {
								// Save the best insertion so far
								rbIndex = ib;
								minPos = i;
								minDelta = foDelta;
								nodeChanged = rc.id;
								operator = INSERT;
							}
						}
					}
				}
			}
		}
		if(operator == REMOVE ){//Remove
			addTabu(nodeChanged, INSERT);//insert the contrary operator
			int id = nodeChanged;
			customers[id].arrivalTime = -1;
			customers[id].exitTime = -1;
			customers[id].cumulativeDist =-1;
			customers[id].cumulativeCost = -1;
			customers[id].route = -1;
			customers[id].visited = -1;
			routes.remove(customers[id]);
			routesLoad -= customers[id].demand;
			requestBank.add(customers[id]);
			upDateRoutes("TS Delete", 0);
			fillPool();
		}else if( operator == INSERT){
			addTabu(nodeChanged, REMOVE);
			requestBank.remove(rbIndex);
			routes.add(minPos, customers[nodeChanged]);
			routesLoad += customers[nodeChanged].demand;
			upDateRoutes("TS Insertion", 0);
			fillPool();
			
		}else{
			for (int i = 0; i < operatorPerformed.length; i++) {
				if(operatorPerformed[i]!=-1 ){
					operatorStage[i]+=CGParameters.MAX_ITERATIONS_TS;
				}
			}
		}
	}
	
	/**
	 * Check feasibility for a node insertion
	 * @param i Position in the route where the node is going to be inserted
	 * @param rc Request customer, node to be inserted
	 * @param r Route in which the node is going to be inserted
	 * @return true if the insertion is feasible, false otherwise
	 */
	private boolean checkInsertionFeasibility(int i, Node rc, ArrayList<Node> r) {
		
		boolean feasible = true;
		double arrival_i = Rounder.round6Dec( r.get(i - 1).exitTime + DataHandler.distance[r.get(i - 1).id][rc.id]);
		double exit_i = Rounder.round6Dec(Math.max(arrival_i, rc.tw_a) + rc.service);
		if (arrival_i<= rc.tw_b  ) {// feasible TW for rc
			rc.arrivalTime =  arrival_i;
			rc.exitTime = exit_i;
			r.add(i, rc);// rc is temporally added, if thetour is feasible, then is temporal
			for (int j = i + 1; j < r.size() && feasible; j++) {
				double arrival_j = Rounder.round6Dec(exit_i + DataHandler.distance[r.get(j - 1).id][r.get(j).id]);
				if (arrival_j > r.get(j).tw_b) {
					feasible = false;
					r.remove(i);
					rc.arrivalTime = -1;
					rc.exitTime = -1;
				} else {
					exit_i = Math.max(arrival_j,r.get(j).tw_a) + r.get(j).service;
				}
			}
			if(feasible){
				r.remove(i);
				rc.arrivalTime=-1;
				rc.exitTime=-1;
			}
		}else{
			feasible = false;
		}
		return feasible;
	}

	/**
	 * Add a tabu for a node (This method set the contrary operator)
	 * @param nodeChange Node inserted or removed
	 * @param Contrary operator Operator performed
	 */
	private void addTabu(int nodeChange, int operator) {
		operatorPerformed[nodeChange] = operator;
		operatorStage[nodeChange] = 1;
	}

	private boolean notTabu(int id, int operator) {
		if(operatorPerformed[id]==operator){
			return false;
		}else{
			return true;
		}
	}	
	
	private void fillPool() {
		String keyS = routes.toString();
		if (routesCost < -0.1 && routes.size()>2 && !metaheuristicHandler.routesPoolRC.containsKey(keyS)) {
			metaheuristicHandler.pool.add(keyS);
			metaheuristicHandler.routesPoolRC.put(keyS, routesCost);
			metaheuristicHandler.routesPoolDist.put(keyS, routesDist);
			metaheuristicHandler.generator.put(keyS, 2);
		}
	}

	private void genCopy() {
		customers = Clonetor.cloneArrayList(GraphManager.nodes);
		requestBank = new ArrayList<Node>();
		for (int i = 1; i < customers.length; i++) {
			customers[i].arrivalTime = 0;
			customers[i].exitTime = 0;
			customers[i].route = -1;
			requestBank.add(customers[i]);
		}
	}
	
	/**
	 * Procedure to update a single route given a parameter.
	 * Main information is updated for each node 
	 * Data structure routes and global variables are updated 
	 * @param varIndex 
	 * @param k route to be updated
	 */
	private void upDateRoutes(String origen, int varIndex) {
		
		int load = 0;
		
		Node customer = null;
		for (int i = 1; i < routes.size(); i++) {
			customer = routes.get(i);
			customer.arrivalTime  = Rounder.round6Dec(routes.get(i - 1).exitTime+ DataHandler.distance[routes.get(i - 1).id][customer.id]);
			if(customer.arrivalTime >customer.tw_b){
				System.out.println("Time: " +  customer.arrivalTime);
				System.out.print("Fatal error Tabu Search: columa infeasible due to TW_b: In " + origen + " Column " + varIndex  + "  generator: " + Master.generator.get(varIndex));
				System.out.println("\nRuta TS " + routes);
				System.out.println("Client: " +customer.id + " has some problems" );
			}
			customer.cumulativeDist = (routes.get(i - 1).cumulativeDist+ DataHandler.distance[routes.get(i - 1).id][customer.id]);
			customer.cumulativeCost = (routes.get(i - 1).cumulativeCost+ DataHandler.cost[routes.get(i - 1).id][customer.id]);
			customer.exitTime  = Rounder.round6Dec(Math.max(customer.arrivalTime , customer.tw_a) + customer.service);
			customer.route = 0;
			customer.visited = i;
			load += customer.demand;
		}
		routesLoad = load;
		routesDist = customer.cumulativeDist;
		routesCost = customer.cumulativeCost-shift;
		totalDistance = routesDist;
		FO = routesCost;
	}
}
