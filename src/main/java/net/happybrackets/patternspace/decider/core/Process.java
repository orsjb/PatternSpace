package net.happybrackets.patternspace.decider.core;

import java.io.Serializable;


public class Process extends Operation {

	private static final long serialVersionUID = 1L;

	static float scale = 0.01f;
//	static float[] sine;
//	
//	static {
//		sine = new float[1000000];
//		for(int i = 0; i < sine.length; i++) {
//			sine[i] = ((float)Math.sin((float)i / sine.length * 2f * Math.PI) + 1f) * 0.5f * scale;
//		}
//	}

	static enum BinaryOp implements Serializable {
		ADD, SUBTRACT, MULTIPLY
	}
	
	BinaryOp op;
	int sourceIndex, targetIndex;
	
	public Process(Decider d) {
		super(d);
	}
	
	private float op(int deciderState) {
//		return decider.numStates * sine[(int)(deciderState / (float)decider.numStates * (sine.length - 1))];
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
//			decider.state[targetIndex] *= 2f * (float)decider.state[sourceIndex] / (decider.numStates);
			decider.state[targetIndex] *= 0.5f + (float)decider.state[sourceIndex] / (decider.numStates);
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
	
	protected void resetCounts() {
		super.resetCounts();
	}

	@Override
	protected Operation copyMutate(Decider newDecider) {
		Process copy = new Process(newDecider);
		copy.op = op;
		copy.sourceIndex = sourceIndex;
		copy.targetIndex = targetIndex;
		if(decider.prob(0.05f)) copy.op = BinaryOp.values()[decider.rng.nextInt(3)];
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
