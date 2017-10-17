//
//  Ctrnn.java
//  Ctrnn
//
//  Created by Oliver Bown on 27/10/2005.
//  Copyright (c) 2005 _MyCompanyName__. All rights reserved.
//

package net.happybrackets.patternspace.dynamic_system.ctrnn;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import net.happybrackets.patternspace.dynamic_system.core.DynamicSystem;
import net.happybrackets.patternspace.dynamic_system.core.DynamicSystemProperties;
import net.happybrackets.patternspace.dynamic_system.decider.Decider;

import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;
import java.util.Random;


public class Ctrnn implements Serializable, DynamicSystem {
	
	public static final long serialVersionUID = 1;
	
	//common node parameters
	public Params params;
	private DoublePointer[] input;	//array of inputs
	private DoublePointer[] output;	//array of pointers to outputs
    Number[] outputCache;
	private CtrnnNode[] hiddenNode;		//pointer to array of hidden nodes
	private CtrnnNode[] inNode;			//pointer to array of input nodes
	private double[] startState;		//start state which can be recalled

	//new fully-connected Ctrnn with no external inputs, output trace is connected to first input
	//required genotype size is (I*4 + N^2 + N*3) where I = number of input nodes and N = number of hidden nodes
	public Ctrnn(Chromosome genotype, Params params) {
		this.params = params;
		int i, j;
		int numNodes = params.numInputNodes + params.numHiddenNodes;
		input = new DoublePointer[params.numInputNodes];
		hiddenNode = new CtrnnNode[params.numHiddenNodes];
		inNode = new CtrnnNode[params.numInputNodes];
		output = new DoublePointer[params.numOutputNodes];
        outputCache = new Number[params.numOutputNodes];
		startState = new double[numNodes * 2];
		for(i = 0; i < params.numInputNodes; i++) {
			input[i] = new DoublePointer();
		}
		for(i = 0; i < params.numHiddenNodes; i++) {
			hiddenNode[i] = new CtrnnNode(numNodes);
			hiddenNode[i].transferFunction = params.hTransferFunc;
			hiddenNode[i].timeStep = params.timeStep;
		}
		for(i = 0; i < params.numInputNodes; i++) {
			inNode[i] = new CtrnnNode(1);
			inNode[i].transferFunction = params.inTransferFunc;
			inNode[i].timeStep = params.timeStep;
			inNode[i].input[0] = input[i];
			inNode[i].weight = new double[inNode[i].numInputs];
			inNode[i].weight[0] = linMap(genotype.getNextGene(), params.inWeightMin, params.inWeightMax); 
			
			inNode[i].gain = linMap(genotype.getNextGene(), params.inGainMin, params.inGainMax);	
			inNode[i].bias = linMap(genotype.getNextGene(), params.inBiasMin, params.inBiasMax);	
			inNode[i].t = Math.exp(linMap(genotype.getNextGene(), params.inTcMin, params.inTcMax));
			double f = genotype.getNextGene();
			inNode[i].transferFlatness = Math.pow(f, 0.99f);
//			inNode[i].transferFunction = CtrnnNode.TransferFunction.NONE;
		}
		for(i = 0; i < params.numHiddenNodes; i++) {
			for(j = 0; j <params.numInputNodes; j++) {
				hiddenNode[i].input[j] = inNode[j].output;
			}
			for(j = 0; j < params.numHiddenNodes; j++) {
				hiddenNode[i].input[j+params.numInputNodes] = hiddenNode[j].output;
			}
			hiddenNode[i].weight = new double[numNodes];
			for(j = 0; j < numNodes; j++) {
				hiddenNode[i].weight[j] = linMap(genotype.getNextGene(), params.hWeightMin, params.hWeightMax);
			}		
			hiddenNode[i].gain = linMap(genotype.getNextGene(), params.hGainMin, params.hGainMax);	
			hiddenNode[i].bias = linMap(genotype.getNextGene(), params.hBiasMin, params.hBiasMax);	
			hiddenNode[i].t = Math.exp(linMap(genotype.getNextGene(), params.hTcMin, params.hTcMax));
			double f = genotype.getNextGene();
			hiddenNode[i].transferFlatness = Math.pow(f, 0.99f);
		}
		for(i = 0; i < params.numOutputNodes; i++) {
			output[i] = hiddenNode[i].output;
		}
		for(i = 0; i < numNodes * 2; i++) {
			startState[i] = 0.0f;
		}
		resetZero();
	}
	
	//new randomly-connected Ctrnn
	public Ctrnn(Params params) {
		this(Chromosome.newRandom(new Random(), params.getGenotypeLength()), params);
	}
	
	public Ctrnn() {
	}
	
	public Params getParams() {
		return params;
	}
	
	private static double linMap(double x, double min, double max) {
		return ( x * (max - min) ) + min;
	}
	
	public void resetZero()
	{
		int i;
		
		for(i = 0; i < this.params.numInputNodes; i++) {
			this.inNode[i].reset();
		}
		for(i = 0; i < this.params.numHiddenNodes; i++) {
			this.hiddenNode[i].reset();
		}
	}
	
	public void setState(double[] state)
	{
		int i, j = 0;
		for(i = 0; i < (this.params.numInputNodes); i++) {
			this.inNode[i].y = state[j++];
			this.inNode[i].output.value = state[j++];
		}
		for(i = 0; i < (this.params.numHiddenNodes); i++) {
			this.hiddenNode[i].y = state[j++];
			this.hiddenNode[i].output.value = state[j++];
		}
	}
	
	public void setStartState(double[] state)
	{
		int i;
		int twiceNumNodes = 2 * (this.params.numInputNodes + this.params.numHiddenNodes);
		for(i = 0; i < twiceNumNodes; i++) {
			this.startState[i] = state[i];
		}
	}
	
	public void resetToStartState() 
	{
		int i, j = 0;
		for(i = 0; i < this.params.numInputNodes; i++) {
			this.inNode[i].y = this.startState[j++];
			this.inNode[i].output.value = this.startState[j++];
		}
		for(i = 0; i < this.params.numHiddenNodes; i++) {
			this.hiddenNode[i].y = this.startState[j++];
			this.hiddenNode[i].output.value = this.startState[j++];
		}
	}
	
	public void setStartStateToCurrentState()
	{
		double[] state = this.getState();
		this.setStartState(state);
	}
	
	public double[] getState()
	{
		int twiceNumNodes = 2 * (this.params.numInputNodes + this.params.numHiddenNodes);
		double[] state = new double[twiceNumNodes];
		int i, j = 0;
		for(i = 0; i < (this.params.numInputNodes); i++) {
			state[j++] = this.inNode[i].y;
			state[j++] = this.inNode[i].output.value;
		}
		for(i = 0; i < (this.params.numHiddenNodes); i++) {
			state[j++] = this.hiddenNode[i].y;
			state[j++] = this.hiddenNode[i].output.value;
		}
		return state;
	}
	
	public double[] getStartState()
	{
		int twiceNumNodes = 2 * (this.params.numInputNodes + this.params.numHiddenNodes);
		double[] state = new double[twiceNumNodes];
		for(int i = 0; i < twiceNumNodes; i++) {
			state[i] = this.startState[i];
		}
		return state;
	}

	@Override
	public void update(Number[] input) {
		feedInputs(input);
		update();
	}
	
	//could make a more efficient access, since in general when you're
	//gathering the input for a ctrnn you're copying data into a new buffer
	private void feedInputs(Number[] input) {
		int i;
		//update Ctrnn
		//set inputs
		for(i = 0; i < this.params.numInputNodes; i++) {
			this.input[i].value = input[i].doubleValue();
		}
	}
	
	private void update() {
		int i;
		//update input nodes (witholding output value)
		for(i = 0; i < this.params.numInputNodes; i++) {
			this.inNode[i].calculateOutput();
//			System.out.print(this.inNode[i].y + " ");
		}
//		System.out.println();
		//update hidden nodes (witholding output value)
		for(i = 0; i < this.params.numHiddenNodes; i++) {
			this.hiddenNode[i].calculateOutput();
		}
		//update node outputs synchronously
		for(i = 0; i < this.params.numInputNodes; i++) {
			this.inNode[i].update();
		}
		for(i = 0; i < this.params.numHiddenNodes; i++) {
			this.hiddenNode[i].update();
		}
        for(i = 0; i < outputCache.length; i++) {
            outputCache[i] = output[i].value;
        }
	}

	public double getOutput(int i) {
		return outputCache[i].doubleValue();
	}

	@Override
	public DynamicSystemProperties getProperties() {
		return null;
	}

	public Number[] getOutputs() {
		return outputCache;
	}

	@Override
	public void writeJSON(Writer out) {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		gson.toJson(this, out);
	}

	public static Ctrnn readJSON(Reader in) {
		Gson gson = new Gson();
		JsonReader reader = new JsonReader(in);
		Ctrnn.Params params = new Ctrnn.Params();
        JsonElement rootElement = new JsonParser().parse(reader);
        JsonObject jobject = rootElement.getAsJsonObject();
        jobject = jobject.getAsJsonObject("params");
        JsonPrimitive primitive = jobject.getAsJsonPrimitive("timeStep");
        double timeStep = primitive.getAsDouble();
        System.out.println("Time Step is " + timeStep);
//		Ctrnn result = gson.fromJson(reader, Ctrnn.class);
		return null;
	}


	public static class Params implements Serializable {
		
		private static final long serialVersionUID = 1;
		
		public int numInputNodes;
		public int numHiddenNodes;
		public int numOutputNodes;
		public double timeStep;
		public double inGainMin;
		public double inGainMax;
		public double inBiasMin;
		public double inBiasMax;
		public double inWeightMin;
		public double inWeightMax;
		public double inTcMin;
		public double inTcMax;
		public double hDensity;
		public double hGainMin;
		public double hGainMax;
		public double hBiasMin;
		public double hBiasMax;
		public double hWeightMin;
		public double hWeightMax;
		public double hTcMin;
		public double hTcMax;
		
		public CtrnnNode.TransferFunction inTransferFunc;
		public CtrnnNode.TransferFunction hTransferFunc;
		
		
		public Params() {
			resetToDefault();
		}
		
		public void resetToDefault() {
			this.numInputNodes = 2;
			this.numHiddenNodes = 20;
			this.numOutputNodes = 3;
			
			this.timeStep = 0.01f;
			
			this.inGainMin = 0.0f;
			this.inGainMax = 3.0f;
			this.inBiasMin = -4.0f;
			this.inBiasMax = 4.0f;
			this.inWeightMin = -10.0f;
			this.inWeightMax = 10.0f;
			this.inTcMin = -0.3f;
			this.inTcMax = 3.0f;
			
			this.hDensity = 0.7f;
			
			this.hGainMin = 0.0f;
			this.hGainMax = 3.0f;
			this.hBiasMin = -4.0f;
			this.hBiasMax = 4.0f;
			this.hWeightMin = -10.0f;
			this.hWeightMax = 10.0f;
			this.hTcMin = -0.3f;
			this.hTcMax = 3.0f;
			
			this.inTransferFunc = CtrnnNode.TransferFunction.LOGSIG;
			this.hTransferFunc = CtrnnNode.TransferFunction.LOGSIG;
		}
		
		public int getGenotypeLength() {
			return (this.numInputNodes * 5 + this.numHiddenNodes * (this.numInputNodes + this.numHiddenNodes) + this.numHiddenNodes * 4);
		}

		public int getNumInputNodes() {
			return numInputNodes;
		}

		public void setNumInputNodes(int numInputNodes) {
			this.numInputNodes = numInputNodes;
		}

		public int getNumHiddenNodes() {
			return numHiddenNodes;
		}

		public void setNumHiddenNodes(int numHiddenNodes) {
			this.numHiddenNodes = numHiddenNodes;
		}

		public int getNumOutputNodes() {
			return numOutputNodes;
		}

		public void setNumOutputNodes(int numOutputNodes) {
			this.numOutputNodes = numOutputNodes;
		}

		public double getTimeStep() {
			return timeStep;
		}

		public void setTimeStep(double timeStep) {
			this.timeStep = timeStep;
		}

		public double getInGainMin() {
			return inGainMin;
		}

		public void setInGainMin(double inGainMin) {
			this.inGainMin = inGainMin;
		}

		public double getInGainMax() {
			return inGainMax;
		}

		public void setInGainMax(double inGainMax) {
			this.inGainMax = inGainMax;
		}

		public double getInBiasMin() {
			return inBiasMin;
		}

		public void setInBiasMin(double inBiasMin) {
			this.inBiasMin = inBiasMin;
		}

		public double getInBiasMax() {
			return inBiasMax;
		}

		public void setInBiasMax(double inBiasMax) {
			this.inBiasMax = inBiasMax;
		}

		public double getInWeightMin() {
			return inWeightMin;
		}

		public void setInWeightMin(double inWeightMin) {
			this.inWeightMin = inWeightMin;
		}

		public double getInWeightMax() {
			return inWeightMax;
		}

		public void setInWeightMax(double inWeightMax) {
			this.inWeightMax = inWeightMax;
		}

		public double getInTcMin() {
			return inTcMin;
		}

		public void setInTcMin(double inTcMin) {
			this.inTcMin = inTcMin;
		}

		public double getInTcMax() {
			return inTcMax;
		}

		public void setInTcMax(double inTcMax) {
			this.inTcMax = inTcMax;
		}

		public double getHDensity() {
			return hDensity;
		}

		public void setHDensity(double density) {
			hDensity = density;
		}

		public double getHGainMin() {
			return hGainMin;
		}

		public void setHGainMin(double gainMin) {
			hGainMin = gainMin;
		}

		public double getHGainMax() {
			return hGainMax;
		}

		public void setHGainMax(double gainMax) {
			hGainMax = gainMax;
		}

		public double getHBiasMin() {
			return hBiasMin;
		}

		public void setHBiasMin(double biasMin) {
			hBiasMin = biasMin;
		}

		public double getHBiasMax() {
			return hBiasMax;
		}

		public void setHBiasMax(double biasMax) {
			hBiasMax = biasMax;
		}

		public double getHWeightMin() {
			return hWeightMin;
		}

		public void setHWeightMin(double weightMin) {
			hWeightMin = weightMin;
		}

		public double getHWeightMax() {
			return hWeightMax;
		}

		public void setHWeightMax(double weightMax) {
			hWeightMax = weightMax;
		}

		public double getHTcMin() {
			return hTcMin;
		}

		public void setHTcMin(double tcMin) {
			hTcMin = tcMin;
		}

		public double getHTcMax() {
			return hTcMax;
		}

		public void setHTcMax(double tcMax) {
			hTcMax = tcMax;
		}

		public CtrnnNode.TransferFunction getInTransferFunc() {
			return inTransferFunc;
		}

		public void setInTransferFunc(CtrnnNode.TransferFunction inTransferFunc) {
			this.inTransferFunc = inTransferFunc;
		}

		public CtrnnNode.TransferFunction getHTransferFunc() {
			return hTransferFunc;
		}

		public void setHTransferFunc(CtrnnNode.TransferFunction transferFunc) {
			hTransferFunc = transferFunc;
		}
		
		public static Params getDefault() {
			Params p = new Params();
			return p;
		}
		
	}
	
	//getters and setters

	public DoublePointer[] getInput() {
		return input;
	}

	public void setInput(DoublePointer[] input) {
		this.input = input;
	}

	public DoublePointer[] getOutput() {
		return output;
	}

	public void setOutput(DoublePointer[] output) {
		this.output = output;
	}

	public CtrnnNode[] getHiddenNode() {
		return hiddenNode;
	}

	public void setHiddenNode(CtrnnNode[] hiddenNode) {
		this.hiddenNode = hiddenNode;
	}

	public CtrnnNode[] getInNode() {
		return inNode;
	}

	public void setInNode(CtrnnNode[] inNode) {
		this.inNode = inNode;
	}

	public void setParams(Params params) {
		this.params = params;
	}

	
}
