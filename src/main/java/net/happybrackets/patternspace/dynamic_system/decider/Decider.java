package net.happybrackets.patternspace.dynamic_system.decider;
import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.happybrackets.patternspace.dynamic_system.core.DynamicSystem;
import net.happybrackets.patternspace.dynamic_system.core.DynamicSystemProperties;

import java.io.*;
import java.util.ArrayList;
import java.util.Random;


public class Decider implements Serializable, DynamicSystem {

	/**
	 * Grammar that describes the Decider for evolution of contextfreegrammer in MOEA framework.
	 */
	public static final String GRAMMAR = "<expr> ::= (<decision>)\n"
			+ "<node> ::= (<decision>)|(<leaf>)\n"
			+ "<decision> ::= D <index>,<thresh>,<node>,<node>\n"
			+ "<index> ::= '0'|'1'|'2'|'3'|'4'|'5'|'6'|'7'|'8'|'9'|'10'|'11'|'12'|'13'|'14'|'15'|'16'|'17'|'18'|'19'|'20'|'21'|'22'|'23'|'24'|'25'\n"
			+ "<thresh> ::= '0'|'0.1'|'0.2'|'0.3'|'0.4'|'0.5'|'0.6'|'0.7'|'0.8'|'0.9'|'1.0'\n"
            + "<leaf> ::= <index>,<op>,<index>|<leaf>,<leaf>\n"
			+ "<op> ::= '*'|'+'|'-'|'LOW'|'HIGH'\n";

	private static final long serialVersionUID = 1L;
	public static final int DEFAULT_NUM_ELEMENTS = 20;
	public static final int DEFAULT_NUM_STATES = 1000000;
	public static final int DEFAULT_CONSOLIDATE_INTERVAL = 100;

	DynamicSystemProperties properties;
	
	Random rng;
	
	int numElements;
	int numStates;
	int consolidateInterval;
	
	boolean doConsolidate;
	boolean verbose;
	int usageCount;
	int lastConsolidateTime;
	int[] state;                //the state array is an array of states first populated with the input variables then with internal states
	double[] output;
	Number[] outputCache;
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
		doConsolidate = false;
		state = new int[numElements];
		output = new double[numElements - numInputs];
		outputCache = new Number[output.length + 1]; //stores the state plus the discrete output
        for(int i = 0; i < output.length; i++) {
            outputCache[i] = new Double(0);
        }
        outputCache[output.length] = new Integer(0);    //the integer
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

	public static Decider parseFromString(int numInputs, int numElements, String grammarString, Random rng) {
		Decider d = new Decider(rng, numElements, DEFAULT_NUM_STATES, DEFAULT_CONSOLIDATE_INTERVAL, numInputs);
        d.root = Condition.parse(d, grammarString);
		return d;
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
	public void update(Number[] inputs) {
		for(int i = 0; i < inputs.length; i++) {
			setInputFract(i, inputs[i].doubleValue());
		}
		process();
	}

	@Override
	public void reset() {
		for(int i = 0; i < state.length; i++) {
			state[i] = 0;
		}
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
	public DynamicSystemProperties getProperties() {
		if(properties == null) {
		    Class<? extends Number>[] inputTypes = new Class[numInputs];
		    for(int i = 0; i < numInputs; i++) {
		        inputTypes[i] = Double.class;
            }
            Class<? extends Number>[] outputTypes = new Class[output.length + 1];
            for(int i = 0; i < output.length; i++) {
                outputTypes[i] = Double.class;
            }
            outputTypes[output.length] = Integer.class;
            properties = new DynamicSystemProperties(getClass(), inputTypes, outputTypes);
        }
		return properties;
	}

	@Override
	public Number[] getOutputs() {
		for(int i = 0; i < output.length; i++) {
			output[i] = getStateFract(i);
			outputCache[i] = output[i];
		}
		outputCache[output.length] = lastLeaf;
		return  outputCache;
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
		d.reset();
		return d;
	}

	public static Decider readJSON(JsonElement obj) {
		Gson gson = new Gson();
		Decider result = gson.fromJson(obj, Decider.class);
		return result;
	}

    @Override
	public JsonElement writeJSON() {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		return gson.toJsonTree(this);
	}

}

