===========================================================================================================================================================================
 Readme for the Branch-price-and-cut Java code for the VRPTW
 Version: 1.0
===========================================================================================================================================================================

 Author:       Nicolas Cabrera (nicolas.cabrera-malik@hec.ca)
      
 Author:       Daniel Yamin (d.yamin@uniandes.edu.co)
      

===========================================================================================================================================================================

This code contains all the source code for executing a branch-price-and-cut algorithm for the Vehicle Routing Problem with Time Windows.

The pricing problem is solved by the Pulse Algorithm. To speed-up the algorithm, the pulse is solved heuristically in the first CG iterations by 
setting a limit on the number of paths that can be generated at a given iteration.

We also show how the subset row inequalities can be handled by the pulse algorithm. 

===========================================================================================================================================================================

The main class is called "Main".

The user can run the code in the well known Solomon's testbed for the VRPTW.
