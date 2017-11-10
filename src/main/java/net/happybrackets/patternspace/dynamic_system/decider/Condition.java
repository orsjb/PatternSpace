package net.happybrackets.patternspace.dynamic_system.decider;

public class Condition extends Operation {

	private static final long serialVersionUID = 1L;

	private Operation yesOperation, noOperation;
	private int valueIndex;
	private float valueThresh;
	private int yesDecisionCount;

	public Condition(Decider d) {
		super(d);
		yesDecisionCount = 0;
	}
	
	protected void process() {
		super.process();
		if(decider.state[valueIndex] > valueThresh) {
			yesOperation.process();
			yesDecisionCount++;
		} else {
			noOperation.process();
		}
	}
	
	public static Condition newRandom(Decider d, int index, int depth, float splitProb) {
		Condition c = new Condition(d);
		int nextIndex = index;
		if(d.rng.nextFloat() < 0.2f) nextIndex++;
		c.yesOperation = d.rng.nextFloat() < (splitProb * depth) ? ProcessArray.newRandom(d) : Condition.newRandom(d, nextIndex, depth + 1, splitProb);
		c.noOperation = d.rng.nextFloat() < (splitProb * depth) ? ProcessArray.newRandom(d) : Condition.newRandom(d, nextIndex, depth + 1, splitProb);
		c.valueIndex = (index < 2)? 0 : (index - 2) % d.state.length;
		c.valueThresh = d.numStates / 2 + d.rng.nextInt((int)(d.numStates * 0.3f)) - d.numStates * 0.15f;
		return c;
	}

	public static Condition newRandom(Decider d, int index, Operation yesOperation, Operation noOperation) {
		Condition c = new Condition(d);
		c.yesOperation = yesOperation;
		c.noOperation = noOperation;
		c.valueIndex = (index < 2)? 0 : (index - 2) % d.state.length;
		c.valueThresh = d.numStates / 2 + d.rng.nextInt((int)(d.numStates * 0.3f)) - d.numStates * 0.15f;
		return c;
	}

	public static Condition parse(Decider d, String s) {
        s = s.substring(2, s.length() - 1);     //remove the brackets and leading D
		Condition c = new Condition(d);
		String[] parts = Operation.split(s);
		//get value index
		c.valueIndex = Integer.parseInt(parts[0]);
		c.valueThresh = Float.parseFloat(parts[1]) * d.numStates;
		c.yesOperation = Operation.parse(d, parts[2]);
		c.noOperation = Operation.parse(d, parts[3]);
		return c;
	}

	@Override
	protected void adapt() {
		//Move threshold so that you get called more often
		//either randomize or drift slightly
		if(usageCount - decider.lastConsolidateTime > 0) {
			float yesRatio = (float)(yesDecisionCount - decider.lastConsolidateTime) / (float)(usageCount - decider.lastConsolidateTime);
			if(yesRatio < 0.1 || yesRatio > 0.9) {
				if(decider.verbose) System.out.println("consolidating condition " + yesDecisionCount + " " + usageCount);
				if(decider.rng.nextFloat() < 0.3) {
					valueThresh = mutateValueThresh();
				} 
			} else {
				if(decider.verbose) System.out.println("not consolidating condition " + yesDecisionCount + " " + usageCount);
			}
			yesOperation.adapt();
			noOperation.adapt();
		} else {
			if(decider.verbose) System.out.println("no usage");
		}
	}
	
	public Operation getYesOperation() {
		return yesOperation;
	}
	
	public Operation getNoOperation() {
		return noOperation;
	}
	

	protected void resetCounts() {
		super.resetCounts();
		yesOperation.resetCounts();
		noOperation.resetCounts();
		yesDecisionCount = 0;
	}
	
	protected Operation copyMutate(Decider newDecider) {
		Condition copy = new Condition(newDecider);
		copy.noOperation = noOperation.copyMutate(newDecider);
		copy.yesOperation = yesOperation.copyMutate(newDecider);
		copy.valueIndex = mutateValueIndex();
		copy.valueThresh = mutateValueThresh();
		return copy;
	}
	
	protected Operation copy(Decider newDecider) {
		Condition copy = new Condition(newDecider);
		copy.noOperation = noOperation.copy(newDecider);
		copy.yesOperation = yesOperation.copy(newDecider);
		copy.valueIndex = valueIndex;
		copy.valueThresh = valueThresh;
		return copy;
	}

	private float mutateValueThresh() {
		float width = decider.numStates / 100;
		float newValueThresh = valueThresh + (float)decider.rng.nextGaussian() * width;
		if(newValueThresh < 0) newValueThresh = 0;
		else if(newValueThresh >= decider.numStates) newValueThresh = decider.numStates - 1;
		return newValueThresh;
	}

	private int mutateValueIndex() {
		return decider.rng.nextInt(decider.state.length);
	}

	
}
