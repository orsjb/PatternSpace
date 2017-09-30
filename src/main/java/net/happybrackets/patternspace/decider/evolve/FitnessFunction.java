package net.happybrackets.patternspace.decider.evolve;

import net.happybrackets.patternspace.decider.core.Decider;

public interface FitnessFunction {
	
	public float evaluate(Decider d);
	
}
