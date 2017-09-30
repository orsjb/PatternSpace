package net.happybrackets.patternspace.decider;

public abstract class Leaf extends Operation {

	private static final long serialVersionUID = 1L;

	int index;
	
	public Leaf(Decider d) {
		super(d);
		index = d.nextLeafIndex++;
	}
	
	
	protected void process() {
		super.process();
		decider.lastLeaf = index;
	}

}
