//
// JLi.java
// JCtrnn
//
// Created by Oliver Bown on 27/10/2005.
// Copyright 2005 __MyCompanyName__. All rights reserved.
//

package net.happybrackets.patternspace.ctrnn;



import java.io.Serializable;

public class JLi implements Serializable {

	public static final long serialVersionUID = 1;
	
	public static enum TransferFunction implements Serializable {
		LOGSIG, TANH, SINTANH, NONE
	};
	
	protected float y; 
	protected DoublePointer output;			//network variables
	private float outputTemp;			//temporary storage of output
	protected float gain, bias, t;		//network parameters
	protected int numInputs;				//number of inputs
	protected DoublePointer[] input;			//array of pointers to floats
	protected float[] weight;				//array of weights
	protected float timeStep;
	protected float transferFlatness;	//variable equivalent to 'a' in the transfer func: output = a*tanh(input) + (1-a)*sin(2*input)
	protected TransferFunction transferFunction;
	
	public JLi() {
		output = new DoublePointer(0.0f);
		transferFunction = TransferFunction.SINTANH;
	}
	
	public JLi(int numInputs) {
		this();
		this.numInputs = numInputs;
		input = new DoublePointer[this.numInputs];
		for(int i = 0; i < this.numInputs; i++) {
			input[i] = new DoublePointer();
		}
	}
	
	public void setTransferFunction(TransferFunction t) {
		this.transferFunction = t;
	}
	
	public float calculateOutput()
	{
		float y_dot;
		int i;
		
		y_dot = -1.0f * y;
		for(i = 0; i < numInputs; i++) {
			y_dot += weight[i] * input[i].value;
		}
		y_dot /= t;
		y += y_dot * timeStep;
		outputTemp = transfer(gain * (y - bias));
		return outputTemp;
	}
	
	private float transfer(float x) {
		switch(transferFunction) {
		case LOGSIG:
			return logsig(x);
		case TANH:
			return (float)Math.tanh(x);
		case SINTANH:
			return transferFlatness * (float)Math.tanh(x) + (1.0f - transferFlatness) * (float)Math.sin(2 * x);
		case NONE:
			break;
		}
		return x;
	}
	
	public static float logsig(float x) {
		return 1f / (1f + (float)Math.exp(-x));
	}

	protected void update()
	{
		output.value = outputTemp;
	}
	
	
	protected void reset()
	{
		y = 0.0f;
		output.value = 0.0f;
		outputTemp = 0.0f;
	}
	

	//getters and setters

	public float getY() {
		return y;
	}

	public void setY(float y) {
		this.y = y;
	}

	public DoublePointer getOutput() {
		return output;
	}

	public void setOutput(DoublePointer output) {
		this.output = output;
	}

	public float getOutputTemp() {
		return outputTemp;
	}

	public void setOutputTemp(float outputTemp) {
		this.outputTemp = outputTemp;
	}

	public float getGain() {
		return gain;
	}

	public void setGain(float gain) {
		this.gain = gain;
	}

	public float getBias() {
		return bias;
	}

	public void setBias(float bias) {
		this.bias = bias;
	}

	public float getT() {
		return t;
	}

	public void setT(float t) {
		this.t = t;
	}

	public int getNumInputs() {
		return numInputs;
	}

	public void setNumInputs(int numInputs) {
		this.numInputs = numInputs;
	}

	public DoublePointer[] getInput() {
		return input;
	}

	public void setInput(DoublePointer[] input) {
		this.input = input;
	}

	public float[] getWeight() {
		return weight;
	}

	public void setWeight(float[] weight) {
		this.weight = weight;
	}

	public float getTimeStep() {
		return timeStep;
	}

	public void setTimeStep(float timeStep) {
		this.timeStep = timeStep;
	}

	public float getTransferFlatness() {
		return transferFlatness;
	}

	public void setTransferFlatness(float transferFlatness) {
		this.transferFlatness = transferFlatness;
	}

	public TransferFunction getTransferFunction() {
		return transferFunction;
	}

	

}
