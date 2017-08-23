package com.olliebown.decider.evolve;

import com.olliebown.decider.core.Decider;

public interface FitnessFunction {
	
	public float evaluate(Decider d);
	
}
