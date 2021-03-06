package net.happybrackets.patternspace.dynamic_system.decider;

import java.io.Serializable;


public class Process extends Operation {

	private static final long serialVersionUID = 1L;

	static float scale = 0.01f;

	static enum BinaryOp implements Serializable {
		ADD, SUBTRACT, MULTIPLY, SET_LOW, SET_HIGH
	}
	
	BinaryOp op;
	int sourceIndex, targetIndex;
	
	public Process(Decider d) {
		super(d);
	}
	
	private float op(int deciderState) {
		return scale * deciderState;
	}
	
	@Override
	protected void process() {
		super.process();
		switch(op) {
		case ADD:
			decider.state[targetIndex] += op(decider.state[sourceIndex]);
			break;
		case SUBTRACT:
			decider.state[targetIndex] -= op(decider.state[sourceIndex]);
			break;
		case MULTIPLY:
			decider.state[targetIndex] *= 0.5f + (float)decider.state[sourceIndex] / (decider.numStates);
			break;
		case SET_LOW:
			decider.state[targetIndex] = 0;
			break;
		case SET_HIGH:
			decider.state[targetIndex] = decider.numStates - 1;
			break;
		default:
		}
		decider.state[targetIndex] = constrain(decider.state[targetIndex]);
		decider.stateChangeCount[targetIndex]++;
		decider.stateInfluenceCount[sourceIndex]++;
	}
	
	private int constrain(int x) {
		while(x < 0) x += decider.numStates;
		return x % decider.numStates;
	}

	public static Process newRandom(Decider d) {
		Process p = new Process(d);
		p.op = BinaryOp.values()[d.rng.nextInt(3)];
		p.sourceIndex = d.nextRandomSourceIndex();
		p.targetIndex  = d.nextRandomTargetIndex();
		return p;
	}

	@Override
	protected Operation copyMutate(Decider newDecider) {
		Process copy = new Process(newDecider);
		copy.op = op;
		copy.sourceIndex = sourceIndex;
		copy.targetIndex = targetIndex;
		if(decider.prob(0.05f)) copy.op = BinaryOp.values()[decider.rng.nextInt(BinaryOp.values().length)];
		if(decider.prob(0.05f)) copy.sourceIndex = decider.nextRandomSourceIndex();
		if(decider.prob(0.05f)) copy.targetIndex = decider.nextRandomTargetIndex();
		return copy;
	}
	
	protected Operation copy(Decider newDecider) {
		Process copy = new Process(newDecider);
		copy.op = op;
		copy.sourceIndex = sourceIndex;
		copy.targetIndex = targetIndex;
		return copy;
	}

}
