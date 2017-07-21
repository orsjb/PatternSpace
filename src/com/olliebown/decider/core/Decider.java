package com.olliebown.decider.core;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;


public class Decider implements Serializable {

	private static final long serialVersionUID = 1L;
	public static final int DEFAULT_NUM_ELEMENTS = 20;
	public static final int DEFAULT_NUM_STATES = 1000000;
	public static final int DEFAULT_CONSOLIDATE_INTERVAL = 100;
	
	Random rng;
	
	int numElements;
	int numStates;
	int consolidateInterval;
	
	boolean doConsolidate;
	boolean verbose;
	boolean doAnalysis;
	int usageCount;
	int lastConsolidateTime;
	int[] state;
	int[] stateChangeCount;
	int[] stateInfluenceCount;
	int numInputs;
	Operation root;
	ArrayList<Integer> sourceIndices, targetIndices;
	int nextLeafIndex;
	int lastLeaf;
	
	public Decider() {
		this(new Random());
	}
	
	private Decider(Random rng) {
		this(rng, DEFAULT_NUM_ELEMENTS, DEFAULT_NUM_STATES, DEFAULT_CONSOLIDATE_INTERVAL, 0);
	}
	
	private Decider(Random rng, int numElements, int numStates, int consolidateInterval, int numInputs) {
		this.rng = rng;
		this.numElements = numElements;
		this.numStates = numStates;
		this.consolidateInterval = consolidateInterval;
		this.numInputs = numInputs;
		nextLeafIndex = 0;
		verbose = false;
		doConsolidate = true;
		doAnalysis = true;
		state = new int[numElements];
		stateChangeCount = new int[numElements];
		stateInfluenceCount = new int[numElements];
		usageCount = 0;
		sourceIndices = new ArrayList<Integer>();
		targetIndices = new ArrayList<Integer>();
	}
	
	public Random getRng() {
		return rng;
	}
	
	public void setNumInputs(int numInputs) {
		this.numInputs = numInputs;
	}
	
	public void setConsolidate(boolean cons) {
		doConsolidate = cons;
	}
	
	public boolean isConsolidate() {
		return doConsolidate;
	}
	
	public Operation getRoot() {
		return root;
	}
	
	public boolean prob(float prob) {
		return rng.nextFloat() < prob;
	}
	
	public void printState() {
		for(int i = 0; i < state.length; i++) {
			System.out.print(state[i] + " ");
		}
		System.out.println();
	}
	
	public static Decider newRandomTree(int numInputs, int numElements, Random rng, float splitProb) {
		Decider d = new Decider(rng, numElements, DEFAULT_NUM_STATES, DEFAULT_CONSOLIDATE_INTERVAL, numInputs);
		d.root = Condition.newRandom(d, 0, 1, splitProb);
		return d;
	}
	
	public static Decider newRandomStub(int numInputs, int numElements, Random rng) {
		Decider d = new Decider(rng, numElements, DEFAULT_NUM_STATES, DEFAULT_CONSOLIDATE_INTERVAL, numInputs);
		d.setNumInputs(numInputs);
		d.root = ProcessArray.newRandom(d);
		return d;
	}
	
	
	public void randomiseState() {
		for(int i = 0; i < state.length; i++) {
			state[i] = rng.nextInt(numStates);
		}
	}

	public void centerState() {
		for(int i = 0; i < state.length; i++) {
			state[i] = numStates / 2;
		}
	}
	
	/**
	 * This is where the update gets called.
	 */
	public void process() {
		root.process();
		usageCount++;
		if(usageCount % consolidateInterval == 0) {
			if(doConsolidate) adapt();

			resetCounts();
			lastConsolidateTime = usageCount;
			
		}
		
		//any analysis?
		if(doAnalysis) analysis();
	}
	
	private void analysis() {

		//look at long term entropy and transfer entropy.
		
		int[] inputs = new int[numInputs];
		for(int i = 0; i < numInputs; i++) {
			inputs[i] = getInputInt(i);
		}
		int result = getCurrentLeaf();
		
		//divide result into 10x10 states (note this should reflect the output to audio)
		int r1 = result % 10;
		int r2 = (result / 10) % 10;
		
		//analyse entropy history - store numbers in buffer
		//given last N elements, make prediction 
		
		
		//also look at transfer entropy - causality
		
		
		
		
	}
	
	private void adapt() {
		root.adapt();
	}

	private void resetCounts() {
		for(int i = 0; i < numElements; i++) {
			stateChangeCount[i] = 0;
			stateInfluenceCount[i] = 0;
		}
		usageCount = 0;
		root.resetCounts();
	}
	
	int nextRandomSourceIndex() {
		if(sourceIndices.size() == 0) {
			for(int i = 0; i < numElements; i++) {
				sourceIndices.add(i);
			}
		}
		int result = sourceIndices.get(rng.nextInt(sourceIndices.size()));
		sourceIndices.remove(new Integer(result));
		return result;
	}

	int nextRandomTargetIndex() {
		if(targetIndices.size() == 0) {
			for(int i = numInputs; i < numElements; i++) {
				targetIndices.add(i);
			}
		}
		int result = targetIndices.get(rng.nextInt(targetIndices.size()));
		targetIndices.remove(new Integer(result));
		return result;
	}

	public float getStateFract(int i) {
		if(i + numInputs < numElements) {
			return (float)state[i + numInputs] / numStates;
		} else {
			throw new ArrayIndexOutOfBoundsException(i);
		}
	}
	
	public float getInputFract(int i) {
		if(i < numInputs) {
			return (float)state[i] / numStates;
		} else {
			throw new ArrayIndexOutOfBoundsException(i);
		}
	}
	
	public int getInputInt(int i) {
		if(i < numInputs) {
			return state[i];
		} else {
			throw new ArrayIndexOutOfBoundsException(i);
		}
	}
	
	public void setInputFract(int i, float val) {
		if(i < numInputs) {
			val = Math.min(Math.max(val, 0), 1);
			state[i] = (int)(val * numStates);
		}
	}
	
	public boolean consolidateModeOn() {
		return doConsolidate;
	}

	public int getNumElements() {
		return numElements;
	}
	
	public int getNumInputs() {
		return numInputs;
	}
	
	public int getNumHiddenElements() {
		return numElements - numInputs;
	}

	public int getNumStates() {
		return numStates;
	}

	public int getConsolidateInterval() {
		return consolidateInterval;
	}

	public boolean isVerbose() {
		return verbose;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}
	
	public int getUsageCount() {
		return usageCount;
	}

	public int randomIndex() {
		return rng.nextInt(numElements);
	}

	public int getCurrentLeaf() {
		return lastLeaf;
	}

	public Decider copyMutate() {
		Decider newD = new Decider(rng, numElements, numStates, consolidateInterval, numInputs);
		newD.root = root.copyMutate(newD);
		newD.randomiseState();
		return newD;
	}

	public int getNumLeaves() {
		return nextLeafIndex;
	}

	public static Decider read(String filename) {
		Decider d = null;
		try {
			FileInputStream fis = new FileInputStream(filename);
			ObjectInputStream ois = new ObjectInputStream(fis);
			d = (Decider)ois.readObject();
			fis.close();
			ois.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
		return d;
	}
	
	
	
	
}






