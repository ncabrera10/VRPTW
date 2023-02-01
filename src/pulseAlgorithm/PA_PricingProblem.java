package pulseAlgorithm;

import org.jorlib.frameworks.columnGeneration.pricing.AbstractPricingProblem;

import columnGeneration.VRPTW;

/**
 * Definition of the pricing problem. Since there's only 1 pricing problem in the PLRP
 * we can simply extend the pricing problem included in the framework with no further modifications.
 * 
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
