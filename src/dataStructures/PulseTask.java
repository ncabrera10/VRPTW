package dataStructures;

import java.util.ArrayList;
/** This class is used for the multithread tasking.

 * Ref.: Lozano, L., Duque, D., and Medaglia, A. L. (2013). 

 * An exact algorithm for the elementary shortest path problem with resource constraints. Technical report COPA 2013-2  

 * @author L. Lozano & D. Duque

 * @affiliation Universidad de los Andes - Centro para la Optimizaci�n y Probabilidad Aplicada (COPA)

 * @url http://copa.uniandes.edu.co/
 *
 */

public class PulseTask implements Runnable{

	double PLoad;
	double PTime;
	double PCost;
	ArrayList<Integer> Path;
	double PDist;
	int Thread;
	int Head;
	
	public PulseTask(int head, double pLoad, double pTime, double pCost, ArrayList<Integer> path, double pDist, int thread) {
		
		PLoad = pLoad;
		PTime = pTime;
		PCost = pCost;
		Path = new ArrayList<Integer>();
		PDist = pDist;
		Thread = thread;
		Head = head;
		
		for (int i = 0; i < path.size(); i++) {
			Path.add(path.get(i));
		}
	}
	@Override
	public void run() {
		try {
			GraphManager.nodes[Head].addAVisitSubSetRow(Thread);
			GraphManager.nodes[Head].pulseMT(PLoad, PTime, PCost, Path, PDist, Thread);
			GraphManager.nodes[Head].minusAVisitSubSetRow(Thread);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	

}
