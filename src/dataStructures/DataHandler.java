package dataStructures;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Random;
import java.util.StringTokenizer;

import parameters.CGParameters;
import parameters.GlobalParameters;

/**
 * This class will store the main information of the current instance
 * 
 * @author nicolas.cabrera-malik
 *
 */
public class DataHandler {

	// Graph attributes
	
	/**
	 * Number of arcs
	 */
	public static int numArcs;
	
	/**
	 * Number of nodes (Customers + depot)
	 */
	public static int n;
	
	/**
	 * Last node id
	 */
	static int lastNode;
	
	/**
	 * Capacity limit for each vehicle
	 */
	public static int Q;
	
	/**
	 * All the arcs of the network stored in a vector where arcs[i][0] = tail of arc i and arcs[i][1] = head of arc i 
	 */
	public static int[][] arcs;
	
	/**
	 * Identifier of the arc ID, given a tail and a head
	 */
	public static int[][] arcs_id;
	
	/**
	 * Distance attribute for any arc i
	 */
	public static double[] distList; 
	
	/**
	 * Time attribute for any arc i
	 */
	
	public static double[] timeList;
	
	/**
	 * Cost attribute for any arc i
	 */
	public static double[] costList;
	
	/**
	 * Load attribute for any arc i
	 */
	public static double[] loadList;
	
	/**
	 * Distance matrix
	 */
	public static double[][] distance;
	
	/**
	 * Cost matrix
	 */
	public static double[][] cost;
		
	/**
	 * Demand for each node
	 */
	public static int[] demand;
	
	/**
	 * Service duration for each node
	 */
	public static int[] service;
	
	/**
	 * Lower time window for each node
	 */
	public static int[] tw_a;
	
	/**
	 * Upper time window for each node
	 */
	public static int[] tw_b;
		
	/**
	 * x coordiante for each customer
	 */
	public static double[] x;
	
	/**
	 * y coordinate for each customer
	 */
	public static double[] y;
	
	/**
	 * Random number generator
	 */
	public static Random rnd;
	
	/**
	 * Data structure that holds the graph
	 */
	private GraphManager G;
	
	/**
	 * Number of threads
	 */
	public static int numThreads;
	
	/**
	 * Threads
	 */
	public static Thread[] threads;
	
	/**
	 * Step size for the bounding procedure
	 */
	public static int boundStep;

	
	// Input file information
	
	/**
	 * Instance type: R, C, RC
	 */
	public static String  instanceType;
	
	/**
	 * Instance identifier
	 */
	public static int instanceNumber;
	
	/**
	 * Instance txt file name
	 */
	public static String CvsInput;
	
	
	/**
	 * Creates a data handler
	 * @param r path to instance files
	 * @param in instance id
	 */
	public DataHandler(String dataFile, String type, int instanceID, int number, int step) {

		// Initializes the seed for the random number generator of java:
		
		DataHandler.rnd = new Random(GlobalParameters.SEED);
		
		// Initializes info about the instance:
		
		CvsInput = dataFile;
		instanceType = type;
		instanceNumber = instanceID;
		
		// Key information for the pulse: # of threads and the size of the step for the bounding procedure:
		
		numThreads = CGParameters.PULSE_NUM_THREADS;
		boundStep = step;

		threads = new Thread[DataHandler.numThreads+1];
		for (int i = 0; i < threads.length; i++) {
			threads[i] = new Thread();
		}

	}
	
	/**
	 * Read a Solomon instance
	 * @throws NumberFormatException
	 * @throws IOException
	 */
	public void readSolomon(int numNodes) throws NumberFormatException, IOException {
		
		// Read the capacity of the corresponding instance:
		
		readCapacity();
		
		// Read the coordinates file:
		
		File file = new File(CvsInput);
		BufferedReader bufRdr = new BufferedReader(new FileReader(GlobalParameters.INSTANCE_FOLDER+file));
		String line = bufRdr.readLine(); //Read number of nodes
		
		n = numNodes; 
		StringTokenizer t = new StringTokenizer(line, " ");
		
		x = new double[n+1]; 
		y = new double[n+1]; 
		demand = new int[n+1]; 
		service =  new int[n+1];
		tw_a =  new int[n+1];
		tw_b =  new int[n+1];
		
		String[] stringReader = new String[7];
		int indexString = 0;
		while (t.hasMoreTokens()) {
			stringReader[indexString] = t.nextToken();
			indexString++;
		}
		
		
		x[0] =Double.parseDouble(stringReader[1]);
		y[0] =Double.parseDouble(stringReader[2]);
		service[0] = (int)(Double.parseDouble(stringReader[6]));
		demand[0]=(int)(Double.parseDouble(stringReader[3]));
		tw_a[0]= (int)(Double.parseDouble(stringReader[4]));
		tw_b[0]= (int)(Double.parseDouble(stringReader[5]));
		
		CGParameters.BOUND_LOWER_TIME_PULSE = (int) (tw_b[0]*0.2);
		
		G = new GraphManager(n+1); 
		int auxNumArcs = (n+1)*(n+1)-(n+1);
		G.addVertex(new Node(0,demand[0],service[0],-tw_b[0],tw_b[0]));
		int customerNumber = 1;
	
		while (customerNumber<=n) {
			indexString=0;
			stringReader= new String[7];
			line = bufRdr.readLine();
			t = new StringTokenizer(line, " ");
			while (t.hasMoreTokens()) {
				stringReader[indexString] = t.nextToken();
				indexString++;
			}
			x[customerNumber] =Double.parseDouble(stringReader[1]);
			y[customerNumber] =Double.parseDouble(stringReader[2]);
			service[customerNumber] = (int)(Double.parseDouble(stringReader[6]));
			demand[customerNumber]=(int)(Double.parseDouble(stringReader[3]));
			tw_a[customerNumber]= (int)(Double.parseDouble(stringReader[4]));
			tw_b[customerNumber]= (int)(Double.parseDouble(stringReader[5]));
			
			G.addVertex(new Node(customerNumber,demand[customerNumber],service[customerNumber], tw_a[customerNumber],tw_b[customerNumber]));
			customerNumber++;
		}
		
		
		distance = new double[n + 1][n + 1];
		cost = new double[n + 1][n + 1];
		arcs_id = new int[n + 1][n + 1];
		distList = new double[auxNumArcs];
		costList = new double[auxNumArcs];
		loadList = new double[auxNumArcs];
		timeList = new double[auxNumArcs];
		arcs = new int[auxNumArcs][2];
		int arc = 0;
		for (int i = 0; i <= n; i++) {
			for (int j = 0; j <= n; j++) {

				double d_ij = Math.sqrt(Math.pow((x[i] - x[j]), 2)	+ Math.pow((y[i] - y[j]), 2));
				double dINT = Math.floor(d_ij*10)/10;
				distance[i][j] = dINT;
				distance[j][i] = dINT;

				
				cost[i][j] = dINT;
				cost[j][i] = dINT;
				// Try to prune using the TW information
				if ((i==0 && (i!=j))  ||((i!=j) && tw_a[i] + service[i] + dINT <= tw_b[j]) ) {
		
					arcs_id[i][j] = arc;
					distList[arc] = dINT;
					costList[arc] = cost[i][j];
					arcs[arc][0] = i;
					arcs[arc][1] = j;
					timeList[arc] = dINT + service[i];
					loadList[arc] = demand[j];
					int a1 = arc;
					G.getNodes()[i].magicIndex.add(a1);
					
					arc++;
				}else {
					arcs_id[i][j] = -1;
				}
			}
		}
		
		numArcs =arc;
		
		for (int i = 0; i < n; i++) {
			G.getNodes()[i].autoSort();
		}
		bufRdr.close();
		
		
	}

	/**
	 * This method reads the capacities of the vehicles for each set of instances
	 * @throws IOException
	 */
	private void readCapacity() throws IOException {
			File file = new File(GlobalParameters.INSTANCE_FOLDER+"Solomon/capacities.txt");
			BufferedReader bufRdr = new BufferedReader(new FileReader(file));
			for (int i = 0; i < 6; i++) {
				String line = bufRdr.readLine(); //READ Num Nodes
				String[] spread = line.split(":");
				if(instanceType.equals(spread[0])){
					int serie = Integer.parseInt(spread[1]);
					if (instanceNumber-serie<50) {
						Q=Integer.parseInt(spread[2]);
						bufRdr.close();
						return;
					}
				}
			}
			bufRdr.close();
			
		}

	
}
