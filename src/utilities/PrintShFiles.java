package utilities;

import java.io.File;
import java.io.PrintWriter;

public class PrintShFiles {

	public static void main(String[] args) {
	
		//Select the main parameters:
		
		int expID = 1; //1:8
		int iniInsID = 1; //1
		int finInsID = 40; //1:20/40
		int repNum = 5; //1:5
		String path = "/Users/nicolas.cabrera-malik/Documents/Work/Papers/PLRP/Experiments/";
		
		//Iterate and create each file:
		
		for(int ins = iniInsID;ins <= finInsID; ins++) { //Instance
			
			for(int rep = 1;rep <= repNum; rep++) { //Replicate
				
					String ruta = path+expID+"/experiment"+expID+"-"+ins+"-"+rep+".sh";
					try {
						PrintWriter pw = new PrintWriter(new File(ruta));
						pw.println("#!/bin/bash");
						pw.println("#PBS -N Exp"+expID+"-"+ins+"-"+rep);
						pw.println("#SBATCH --account=def-jmendoza");
						pw.println("#SBATCH --time=02:30:00"); //00:05:00
						pw.println("#SBATCH --mem=15G");
						pw.println("#SBATCH --nodes=1");
						pw.println("#SBATCH --cpus-per-task=4");//$HOME/Projects/PLRP/Outputs/"+expID+
						pw.println("#SBATCH --output=Slurm_"+expID+"-"+ins+"-"+rep+".out");
						pw.println("#SBATCH --mail-user=nicolas.cabrera-malik@hec.ca");
						pw.println("#SBATCH --mail-type=ALL");
						pw.println("module load java");
						//pw.println("module load mycplex/22.1.0");
						pw.println("cd $HOME/projects/def-jmendoza/ncabre10/PLRP/Codes/PLRP/"); ///home/ncabre10/Projects/PLRP/Codes/PLRP
						pw.println("java -server -jar PLRP.jar "+ins+" "+2+" \"I\" "+"\"c\" "+7200+" "+rep+" "+4);
						//pw.println("cd $HOME/projects/def-jmendoza/ncabre10/PLRP/Codes/PLRP_NC_BC/"); ///home/ncabre10/Projects/PLRP/Codes/PLRP
						 //pw.println("java -server -jar PLRP.jar "+ins+" "+1+" \"I\" "+"\"PA\" "+"\"L\" "+" \"bf\" "+" \"d\" "+"\"p\" "+3600+" "+rep+" "+16);
						//pw.println("java -server -jar PLRP.jar "+ins+" "+1+" \"I\" "+"\"NG-10\" "+"\"L\" "+" \"bf\" "+" \"d\" "+"\"p\" "+3600+" "+rep+" "+16);
						//pw.println("java -server -jar PLRP.jar "+ins+" "+2+" \"B\" "+"\"PA\" "+"\"L\" "+" \"bf\" "+" \"d\" "+"\"c\" "+7200+" "+rep+" "+16);
						
						//pw.println("sleep 30"); //$i 2 "I" "c" 7200 1 1
						pw.close();
						
						/**
						 * pw.println("java -Xmx14000m -jar PLRP.jar "+ins+" "+2+" \"I\" "+"\"c\" "+7200+" "+rep+" "+16); //Experimento 1
						 * pw.println("java -Xmx14000m -jar PLRP.jar "+ins+" "+2+" \"I\" "+"\"p\" "+7200+" "+rep+" "+16); //Experimento 2
						 * 
						 *pw.println("cd $HOME/Projects/PLRP/Codes/PLRP_NC_BC/"); ///home/ncabre10/Projects/PLRP/Codes/PLRP
						  pw.println("java -Xmx14000m -jar PLRP.jar "+ins+" "+1+" \"I\" "+"\"PA\" "+"\"L\" "+" \"bf\" "+" \"d\" "+"\"wt\" "+7200+" "+rep+" "+16);
						*1 "I" "PA" "L" "bf" "d" "wt" 7200 1 1
						 */
					}
					catch(Exception e) {
						System.out.println("Error imprimiendo");
					}
				
			}
			
		}
		
		
	}
	
	/**
	int expID = 1;
		int iniInsID = 1;
		int finInsID = 40;
		int repNum = 10;
		int iniTspID = 0;
		int finTspID = 4;
		int wallTime = 300;
		int setIT = 1;
		int numIT = 2500;
		int timeLimitS = 3600;
		int timeLimitC = 60;
		int factor = 6;
		
		int expID = 2;
		int iniInsID = 1;
		int finInsID = 60;
		int repNum = 10;
		int iniTspID = 0;
		int finTspID = 0;
		int wallTime = 7200;
		int setIT = 2;
		int numIT = 50;
		int timeLimitS = 3600;
		int timeLimitC = 60;
		int factor = 6;
	*/

}
