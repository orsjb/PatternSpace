package net.happybrackets.patternspace.decider.core;

import java.io.Serializable;

public abstract class Operation implements Serializable {

	private static final long serialVersionUID = 1L;

	protected Decider decider;
	protected int usageCount;
	
	public Operation(Decider d) {
		this.decider = d;
		usageCount = 0;
	}
	
	public int getUsageCount() {
		return usageCount;
	}
	
	protected void process() {
		usageCount++;
	}
	
	protected void resetCounts() {
		usageCount = 0;
	}
	
	protected void adapt() {}
	
	protected abstract Operation copyMutate(Decider newDecider);
	protected abstract Operation copy(Decider newDecider);

	public int getRecentUsageCount() {
		return usageCount - decider.lastConsolidateTime;
	}
}
