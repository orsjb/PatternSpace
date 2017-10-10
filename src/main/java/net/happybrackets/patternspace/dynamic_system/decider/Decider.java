package net.happybrackets.patternspace.dynamic_system.decider;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.happybrackets.patternspace.dynamic_system.core.DynamicSystem;

import java.io.*;
import java.util.ArrayList;
import java.util.Random;


public class Decider implements Serializable, DynamicSystem {

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
	int usageCount;
	int lastConsolidateTime;
	int[] state;
	double[] output;
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
		state = new int[numElements];
		output = new double[numElements - numInputs];
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
	}

	@Override
	public void update(double[] inputs) {
		for(int i = 0; i < inputs.length; i++) {
			setInputFract(i, inputs[i]);
		}
		process();
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

	public double getStateFract(int i) {
		if(i + numInputs < numElements) {
			return (double)state[i + numInputs] / numStates;
		} else {
			throw new ArrayIndexOutOfBoundsException(i);
		}
	}

	@Override
	public double[] getOutputs() {
		for(int i = 0; i < output.length; i++) {
			output[i] = state[i + numInputs];
		}
		return  output;
	}
	
	public double getInputFract(int i) {
		if(i < numInputs) {
			return (double)state[i] / numStates;
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
	
	public void setInputFract(int i, double val) {
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

	public static Decider readJSON(Reader in) {
		Gson gson = new Gson();
		JsonReader reader = new JsonReader(in);
		Decider result = gson.fromJson(reader, Decider.class);
		return result;
	}

    @Override
	public void writeJSON(Writer out) {
        Gson gson = new Gson();
        gson.toJson(this, out);
	}

	@Override
	public int getDiscreteState() {
		return lastLeaf;
	}
}
