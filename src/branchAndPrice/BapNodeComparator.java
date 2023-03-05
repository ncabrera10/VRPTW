package branchAndPrice;

import java.util.Comparator;
import org.jorlib.frameworks.columnGeneration.branchAndPrice.BAPNode;

import columnGeneration.VRPTW;
import columnGeneration.RoutePattern;

/**
 * This class enables the comparison between two BAP nodes.  This will help us select the next BAP node to explore.
 * @author nicolas.cabrera-malik
 *
 */
public class BapNodeComparator implements Comparator<BAPNode<VRPTW, RoutePattern>>{
	public int compare(BAPNode<VRPTW, RoutePattern> o1, BAPNode<VRPTW, RoutePattern> o2) {
		return Integer.compare(o1.nodeID, o2.nodeID); //BFS
    }
}
