package utilities;
import java.util.ArrayList;

import dataStructures.Node;

/**
 * Class to quickly clone an arraylist.
 * @author Daniel Duque
 *
 */
public class Clonetor {
	
	
	
	public static void main(String[] args) {
		@SuppressWarnings("unchecked")
		ArrayList<Node>[] numbers = new ArrayList[3];
		//ArrayList<Node> numbers = new ArrayList<Node>();
		for (int i = 0; i < numbers.length; i++) {
			numbers[i] = new ArrayList<Node>();
			for (int j = 0; j < 5; j++) {
				numbers[i].add(new Node(j, 2, 3, 4, 5));
			}
		}	
		ArrayList<Node>[] numbersCopy = cloneArrayList(numbers);
		
				//((Node[])numbers).clone();
		
		System.out.println(numbers[1]);

		System.out.println(numbersCopy[1]);
		numbersCopy[1].get(2).id=-100;

		System.out.println(numbers[1]);

		System.out.println(numbersCopy[1]);
		
		double[] a1 = new double[]{1.2,1.3,1.4};
		double[] a2 = a1.clone();
		a2[1]=0.1;
		System.out.println(a1[1]);
		System.out.println(a2[1]);
	}
	
	/**
	 * This method clones an arraylist of nodes
	 * @param nodeArrayList
	 * @return
	 */
	
	public static ArrayList<Node> cloneArrayList(ArrayList<Node> nodeArrayList) {
	    ArrayList<Node> clone = new ArrayList<Node>(nodeArrayList.size());
	    for(Node item: nodeArrayList) clone.add((Node) item.clone());
	    return clone;
	}
	
	/**
	 * This method clones a node permutation
	 * @param nodePermutation
	 * @return
	 */
	public static Node[] cloneArrayList(Node[] nodePermutation) {
		//System.out.println("Clon");
		Node[] clone = new Node[(nodePermutation.length)];
	    for(int i = 0; i<nodePermutation.length;i++){
	    	clone[i]=(Node) nodePermutation[i].clone();
	    }
	    return clone;
	}
	
	/**
	 * Clones an arraylist
	 * @param ArrayList
	 * @return
	 */
	public static ArrayList<Node>[] cloneArrayList(ArrayList<Node>[] ArrayList) {
		@SuppressWarnings("unchecked")
		ArrayList<Node>[] clone = new ArrayList[(ArrayList.length)];
	    for(int i = 0; i<ArrayList.length;i++){
	    	clone[i]=new ArrayList<Node>();
	    	for (int j = 0; j < ArrayList[i].size(); j++) {
	    		clone[i].add((Node)ArrayList[i].get(j).clone());
			}
	    	
	    }
	    return clone;
	}

	
	

}
