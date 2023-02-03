package main;

import director.Solver;
import parameters.CGParameters;
import parameters.CGParametersReader;
import parameters.GlobalParameters;
import parameters.GlobalParametersReader;

/**
 * This class runs the CG/BPC procedure. 
 * The user can select the instance and the implementation modifying the main method.
 * 
 * Instances:
 * 	Set: Solomon (1984)
 *  Name: C, R, or RC
 *  
 * Implementation: 
 * 	1: CG at the root node 2: BCP
 * 
 * 
 * If you want to change more parameters, you can modify the "parametersCG.xml" file.
 * For example:
 * 	-Printing useful information on console
 * 	-Maximum number of cuts per node.
 * 	Among others..
 * 
 * 
 * @author nicolas.cabrera-malik
 *
 */
public class Main {

	/**
	 * This method runs the code.
	 * @param args
	 */
	public static void main(String[] args) {

		// Select the directory to the set of instances 
		
		String dir = "Solomon/";
		
		// Select the instance type: C, R, RC
		
		String instanceType = "R";
		
		// Select the instance number: 101, 102..208
		
		int instanceNumber = 105;
		
		// Extension of the file
		
		String extension = ".txt";
		
		// Name of the file
		
		String dataFile = dir + instanceType + instanceNumber + extension;
		
		// Number of nodes in the instance
		
		int numNodes = 50; //25; 50; 100;
		
		// Select the implementation 
			//Type 1:CG
			//Type 2:BPC
		
		int type = 2; 
		
		// Seed
				
			int seed = 1;
			int threads = 4;
			
		// Runs the code:
		
		try {
			
			// Loads the global parameters: some paths, the precision..
			
			GlobalParametersReader.initialize("./config/parametersGlobal.xml");
			
			// Loads the parameters for the CG:
			
			CGParametersReader.initialize("./config/parametersCG.xml");
			setUpConfiguration(seed,threads);
			
			// Create a solver instance:

			new Solver(dataFile, instanceType, instanceNumber,type,numNodes);
			System.exit(0);
	
		}catch(Exception e) {
			e.printStackTrace();
			System.out.println("Something went wrong. Check with the administrator.");
		}
		
	}
	
	/**
	 * This method modifies the general parameters taking into account the selected configuration
	 * @param pricing
	 * @param enhacements
	 * @param branchAndPrice
	 * @param branching
	 */
	public static void setUpConfiguration(int seed,int threads) {
		
		CGParameters.CONFIGURATION = seed+"_"+threads;
		GlobalParameters.SEED = seed;
		GlobalParameters.THREADS = threads;
		
	}

}
