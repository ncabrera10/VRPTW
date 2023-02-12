package pricingAlgorithms;

import org.jorlib.frameworks.columnGeneration.pricing.AbstractPricingProblem;

import columnGeneration.VRPTW;

/**
 * Definition of the pricing problem. 
 */

public final class PA_PricingProblem extends AbstractPricingProblem<VRPTW> {

	/**
	 * This method creates a new pricing problem
	 * @param modelData
	 * @param name
	 */
	public PA_PricingProblem(VRPTW modelData, String name) {
		super(modelData, name);
	}
	
	
}
