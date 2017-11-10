package net.happybrackets.patternspace.dynamic_system.decider;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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

	public static Operation parse(Decider d, String s) {
		Operation result = null;
		//check for D
		if(s.startsWith("(D")) {
			//it's a condition
			result = Condition.parse(d, s);
		} else {
			//it's a leaf
			result = ProcessArray.parse(d, s);
		}
		return result;
	}

	public static String[] split(String s) {
        //split around ',' ignoring bracketed elements
        List<Integer> splitPoints = new ArrayList<>();
        int depth = 0;
        for(int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if(ch == '(') {
                depth++;
            } else if(ch == ')') {
                depth--;
            } else if(ch == ',' && depth == 0) {
                splitPoints.add(i);
            }
        }
        String[] results = new String[splitPoints.size() + 1];
        int previousSplitPoint = -1;
        for(int i = 0; i < splitPoints.size(); i++) {
            results[i] = s.substring(previousSplitPoint+1, splitPoints.get(i));
            previousSplitPoint = splitPoints.get(i);
        }
        results[splitPoints.size()] = s.substring(splitPoints.get(splitPoints.size() - 1) + 1);
        return results;
    }
}
