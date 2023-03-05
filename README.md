===========================================================================================================================================================================
 Readme for the Branch-price-and-cut Java code for the VRPTW
 Version: 1.0
===========================================================================================================================================================================

 Author:       Nicolas Cabrera (nicolas.cabrera-malik@hec.ca)
      
 Author:       Daniel Yamin (d.yamin@uniandes.edu.co)
      

===========================================================================================================================================================================

This code contains all the source code for executing a branch-price-and-cut algorithm for the Vehicle Routing Problem with Time Windows.

The pricing problem is solved by either the pulse algorithm, a labeling algorithm or a tabu search. To speed-up the algorithm, the pricing algorithm is solved heuristically in the first CG iterations by setting a limit on the number of paths that can be generated at a given iteration.

We also show how the subset row inequalities can be handled by the pulse algorithm. 

===========================================================================================================================================================================

The main class is called "Main". The user can run the code in the well known Solomon's testbed for the VRPTW.

===========================================================================================================================================================================

The parameters used by the BPC algorithm can be modified on the file "parametersCG.xml" inside the folder "config". For example, the user can modify the maximum number of paths to be added at each iteration of any column generation iteration.

The authors would really enjoy to know the (good) use of the pulse algorithm in different fields, so please send a line to nicolas.cabrera-malik@hec.ca or copa@uniandes.edu.co describing us your application (as brief as you want).
