//
//  JCtrnn.java
//  JCtrnn
//
//  Created by Oliver Bown on 27/10/2005.
//  Copyright (c) 2005 _MyCompanyName__. All rights reserved.
//

package net.happybrackets.patternspace.ctrnn;

import java.beans.XMLEncoder;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Random;


public class JCtrnn implements Serializable {
	
	public static final long serialVersionUID = 1;
	
	//common node parameters
	public Params params;
	private DoublePointer[] input;	//array of inputs
	private DoublePointer[] output;	//array of pointers to outputs
	private JLi[] hiddenNode;		//pointer to array of hidden nodes
	private JLi[] inNode;			//pointer to array of input nodes
	private float[] startState;		//start state which can be recalled

	//new fully-connected JCtrnn with no external inputs, output trace is connected to first input
	//required genotype size is (I*4 + N^2 + N*3) where I = number of input nodes and N = number of hidden nodes
	public JCtrnn(Chromosome genotype, Params params) {
		this.params = params;
		int i, j;
		int numNodes = params.numInputNodes + params.numHiddenNodes;
		
		input = new DoublePointer[params.numInputNodes];
		hiddenNode = new JLi[params.numHiddenNodes];
		inNode = new JLi[params.numInputNodes];
		output = new DoublePointer[params.numOutputNodes];
		startState = new float[numNodes * 2];
		for(i = 0; i < params.numInputNodes; i++) {
			input[i] = new DoublePointer();
		}
		for(i = 0; i < params.numHiddenNodes; i++) {
			hiddenNode[i] = new JLi(numNodes);
			hiddenNode[i].transferFunction = params.hTransferFunc;
			hiddenNode[i].timeStep = params.timeStep;
		}
		for(i = 0; i < params.numInputNodes; i++) {
			inNode[i] = new JLi(1);
			inNode[i].transferFunction = params.inTransferFunc;
			inNode[i].timeStep = params.timeStep;
			inNode[i].input[0] = input[i];
			inNode[i].weight = new float[inNode[i].numInputs];
			inNode[i].weight[0] = linMap(genotype.getNextGene(), params.inWeightMin, params.inWeightMax); 
			
			inNode[i].gain = linMap(genotype.getNextGene(), params.inGainMin, params.inGainMax);	
			inNode[i].bias = linMap(genotype.getNextGene(), params.inBiasMin, params.inBiasMax);	
			inNode[i].t = (float)Math.exp(linMap(genotype.getNextGene(), params.inTcMin, params.inTcMax));	
			float f = genotype.getNextGene();
			inNode[i].transferFlatness = (float)Math.pow(f, 0.99f);	
//			inNode[i].transferFunction = JLi.TransferFunction.NONE;
		}
		for(i = 0; i < params.numHiddenNodes; i++) {
			for(j = 0; j <params.numInputNodes; j++) {
				hiddenNode[i].input[j] = inNode[j].output;
			}
			for(j = 0; j < params.numHiddenNodes; j++) {
				hiddenNode[i].input[j+params.numInputNodes] = hiddenNode[j].output;
			}
			hiddenNode[i].weight = new float[numNodes];
			for(j = 0; j < numNodes; j++) {
				hiddenNode[i].weight[j] = linMap(genotype.getNextGene(), params.hWeightMin, params.hWeightMax);
			}		
			hiddenNode[i].gain = linMap(genotype.getNextGene(), params.hGainMin, params.hGainMax);	
			hiddenNode[i].bias = linMap(genotype.getNextGene(), params.hBiasMin, params.hBiasMax);	
			hiddenNode[i].t = (float)Math.exp(linMap(genotype.getNextGene(), params.hTcMin, params.hTcMax));	
			float f = genotype.getNextGene();
			hiddenNode[i].transferFlatness = (float)Math.pow(f, 0.99f);			
		}
		for(i = 0; i < params.numOutputNodes; i++) {
			output[i] = hiddenNode[i].output;
		}
		for(i = 0; i < numNodes * 2; i++) {
			startState[i] = 0.0f;
		}
		resetZero();
	}
	
	//new randomly-connected JCtrnn
	public JCtrnn(Params params) {
		this(Chromosome.newRandom(new Random(), params.getGenotypeLength()), params);
	}
	
	public JCtrnn() {
	}
	
	public Params getParams() {
		return params;
	}
	
	private static float linMap(float x, float min, float max) {
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
	
	public void setState(float[] state)
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
	
	public void setStartState(float[] state)
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
		float[] state = this.getState();
		this.setStartState(state);
	}
	
	public float[] getState()
	{
		int twiceNumNodes = 2 * (this.params.numInputNodes + this.params.numHiddenNodes);
		float[] state = new float[twiceNumNodes];
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
	
	public float[] getStartState()
	{
		int twiceNumNodes = 2 * (this.params.numInputNodes + this.params.numHiddenNodes);
		float[] state = new float[twiceNumNodes];
		for(int i = 0; i < twiceNumNodes; i++) {
			state[i] = this.startState[i];
		}
		return state;
	}

	public void update(float[] input) {
		feedInputs(input);
		update();
	}
	
	//could make a more efficient access, since in general when you're
	//gathering the input for a ctrnn you're copying data into a new buffer
	public void feedInputs(float[] input) {
		int i;
		//update JCtrnn
		//set inputs
		for(i = 0; i < this.params.numInputNodes; i++) {
			this.input[i].value = input[i];
		}
	}
	
	public void update() {
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
	}

	public float getOutput(int i) {
		return output[i].value;
	}
	
	public float[] getOutputs() {
		float[] outputs = new float[output.length];
		for(int i = 0; i < outputs.length; i++) {
			outputs[i] = output[i].value;
		}
		return outputs;
	}
	
	public static void main(String[] args) throws IOException {
		Params p = new Params();
		p.resetToDefault();
		p.inTransferFunc = JLi.TransferFunction.SINTANH;
		FileOutputStream fos = new FileOutputStream(new File("/Users/ollie/Desktop/p_test.txt"));
		XMLEncoder oos = new XMLEncoder(fos);
		oos.writeObject(p);
		oos.close();
		fos.close();
	}

	public static class Params implements Serializable {
		
		private static final long serialVersionUID = 1;
		
		public int numInputNodes;
		public int numHiddenNodes;
		public int numOutputNodes;
		public float timeStep;
		public float inGainMin;
		public float inGainMax;
		public float inBiasMin;
		public float inBiasMax;
		public float inWeightMin;
		public float inWeightMax;
		public float inTcMin;
		public float inTcMax;
		public float hDensity;
		public float hGainMin;
		public float hGainMax;
		public float hBiasMin;
		public float hBiasMax;
		public float hWeightMin;
		public float hWeightMax;
		public float hTcMin;
		public float hTcMax;	
		
		public JLi.TransferFunction inTransferFunc;
		public JLi.TransferFunction hTransferFunc;
		
		
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
			
			this.inTransferFunc = JLi.TransferFunction.LOGSIG;
			this.hTransferFunc = JLi.TransferFunction.LOGSIG;
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

		public float getTimeStep() {
			return timeStep;
		}

		public void setTimeStep(float timeStep) {
			this.timeStep = timeStep;
		}

		public float getInGainMin() {
			return inGainMin;
		}

		public void setInGainMin(float inGainMin) {
			this.inGainMin = inGainMin;
		}

		public float getInGainMax() {
			return inGainMax;
		}

		public void setInGainMax(float inGainMax) {
			this.inGainMax = inGainMax;
		}

		public float getInBiasMin() {
			return inBiasMin;
		}

		public void setInBiasMin(float inBiasMin) {
			this.inBiasMin = inBiasMin;
		}

		public float getInBiasMax() {
			return inBiasMax;
		}

		public void setInBiasMax(float inBiasMax) {
			this.inBiasMax = inBiasMax;
		}

		public float getInWeightMin() {
			return inWeightMin;
		}

		public void setInWeightMin(float inWeightMin) {
			this.inWeightMin = inWeightMin;
		}

		public float getInWeightMax() {
			return inWeightMax;
		}

		public void setInWeightMax(float inWeightMax) {
			this.inWeightMax = inWeightMax;
		}

		public float getInTcMin() {
			return inTcMin;
		}

		public void setInTcMin(float inTcMin) {
			this.inTcMin = inTcMin;
		}

		public float getInTcMax() {
			return inTcMax;
		}

		public void setInTcMax(float inTcMax) {
			this.inTcMax = inTcMax;
		}

		public float getHDensity() {
			return hDensity;
		}

		public void setHDensity(float density) {
			hDensity = density;
		}

		public float getHGainMin() {
			return hGainMin;
		}

		public void setHGainMin(float gainMin) {
			hGainMin = gainMin;
		}

		public float getHGainMax() {
			return hGainMax;
		}

		public void setHGainMax(float gainMax) {
			hGainMax = gainMax;
		}

		public float getHBiasMin() {
			return hBiasMin;
		}

		public void setHBiasMin(float biasMin) {
			hBiasMin = biasMin;
		}

		public float getHBiasMax() {
			return hBiasMax;
		}

		public void setHBiasMax(float biasMax) {
			hBiasMax = biasMax;
		}

		public float getHWeightMin() {
			return hWeightMin;
		}

		public void setHWeightMin(float weightMin) {
			hWeightMin = weightMin;
		}

		public float getHWeightMax() {
			return hWeightMax;
		}

		public void setHWeightMax(float weightMax) {
			hWeightMax = weightMax;
		}

		public float getHTcMin() {
			return hTcMin;
		}

		public void setHTcMin(float tcMin) {
			hTcMin = tcMin;
		}

		public float getHTcMax() {
			return hTcMax;
		}

		public void setHTcMax(float tcMax) {
			hTcMax = tcMax;
		}

		public JLi.TransferFunction getInTransferFunc() {
			return inTransferFunc;
		}

		public void setInTransferFunc(JLi.TransferFunction inTransferFunc) {
			this.inTransferFunc = inTransferFunc;
		}

		public JLi.TransferFunction getHTransferFunc() {
			return hTransferFunc;
		}

		public void setHTransferFunc(JLi.TransferFunction transferFunc) {
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

	public JLi[] getHiddenNode() {
		return hiddenNode;
	}

	public void setHiddenNode(JLi[] hiddenNode) {
		this.hiddenNode = hiddenNode;
	}

	public JLi[] getInNode() {
		return inNode;
	}

	public void setInNode(JLi[] inNode) {
		this.inNode = inNode;
	}

	public void setParams(Params params) {
		this.params = params;
	}

	
}
