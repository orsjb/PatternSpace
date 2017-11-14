package net.happybrackets.patternspace.dynamic_system.decider;

import java.util.ArrayList;


public class ProcessArray extends Leaf {

	private static final long serialVersionUID = 1L;

	public ArrayList<Operation> getOperations() {
		return operations;
	}

	private ArrayList<Operation> operations;
	
	public ProcessArray(Decider d) {
		super(d);
		operations = new ArrayList<Operation>();
	}
	
	@Override
	protected void process() {
		super.process();
		for(Operation p : operations) {
			p.process();
		}
	}
	
	static ProcessArray newRandom(Decider d) {
		ProcessArray pa = new ProcessArray(d);
		int numProcesses = d.rng.nextInt(50);
		for(int i = 0; i < numProcesses; i++) {
			pa.operations.add(Process.newRandom(d));
		}
		return pa;
	}
	
	protected void resetCounts() {
		super.resetCounts();
		for(Operation p : operations) {
			p.resetCounts();
		}
	}
	
	public void addOperation(Operation o) {
		operations.add(o);
	}

	@Override
	protected Operation copyMutate(Decider newDecider) {
		if(decider.prob(0.8f)) {
			//just return a copy, no mutations
			return copy(newDecider);
		} else {
			if(decider.prob(0.05f)) {
				//split and return a new random condition with two
				//mutated copies of this
				return Condition.newRandom(newDecider, decider.randomIndex(), 
						copyMutateNoSplit(newDecider), copyMutateNoSplit(newDecider));
			} else {
				return copyMutateNoSplit(newDecider);
			}
		}
	}
	
	protected Operation copyMutateNoSplit(Decider newDecider) {
		//return a copy with mutations
		ProcessArray copy = new ProcessArray(newDecider);
		for(Operation o : operations) {
			if(decider.prob(0.5f)) {
				copy.addOperation(o.copyMutate(newDecider));
			} else {
				copy.addOperation(o.copy(newDecider));
			}
		}
		return copy;
	}
	
	protected Operation copy(Decider newDecider) {
		ProcessArray copy = new ProcessArray(newDecider);
		for(Operation o : operations) {
			copy.addOperation(o.copy(newDecider));
		}
		return copy;
	}

	public static Operation parse(Decider d, String s) {
		s = s.substring(1, s.length() - 1);     //remove the brackets
		ProcessArray pa = new ProcessArray(d);
		String[] bits = s.split("[,]");
		int i = 0;
		while(i < bits.length) {
			Process p = new Process(d);
			p.sourceIndex = Integer.parseInt(bits[i++]);
			String op = bits[i++];
			switch (op) {
				case "+":
					p.op = Process.BinaryOp.ADD;
					break;
				case "*":
					p.op = Process.BinaryOp.MULTIPLY;
					break;
				case "-":
					p.op = Process.BinaryOp.SUBTRACT;
					break;
				case "LOW":
					p.op = Process.BinaryOp.SET_LOW;
					break;
				case "HIGH":
					p.op = Process.BinaryOp.SET_HIGH;
					break;
			}
			p.targetIndex = Integer.parseInt(bits[i++]);
            pa.addOperation(p);
		}

		return pa;
	}

}
