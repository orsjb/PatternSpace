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
	
	protected double y;
	protected DoublePointer output;			//network variables
	private double outputTemp;			//temporary storage of output
	protected double gain, bias, t;		//network parameters
	protected int numInputs;				//number of inputs
	protected DoublePointer[] input;			//array of pointers to floats
	protected double[] weight;				//array of weights
	protected double timeStep;
	protected double transferFlatness;	//variable equivalent to 'a' in the transfer func: output = a*tanh(input) + (1-a)*sin(2*input)
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
	
	public double calculateOutput()
	{
		double y_dot;
		int i;
		
		y_dot = -1.0 * y;
		for(i = 0; i < numInputs; i++) {
			y_dot += weight[i] * input[i].value;
		}
		y_dot /= t;
		y += y_dot * timeStep;
		outputTemp = transfer(gain * (y - bias));
		return outputTemp;
	}
	
	private double transfer(double x) {
		switch(transferFunction) {
		case LOGSIG:
			return logsig(x);
		case TANH:
			return Math.tanh(x);
		case SINTANH:
			return transferFlatness * Math.tanh(x) + (1.0f - transferFlatness) * Math.sin(2 * x);
		case NONE:
			break;
		}
		return x;
	}
	
	public static double logsig(double x) {
		return 1 / (1 + Math.exp(-x));
	}

	protected void update()
	{
		output.value = outputTemp;
	}
	
	
	protected void reset()
	{
		y = 0.0;
		output.value = 0.0;
		outputTemp = 0.0;
	}
	

	//getters and setters

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

	public DoublePointer getOutput() {
		return output;
	}

	public void setOutput(DoublePointer output) {
		this.output = output;
	}

	public double getOutputTemp() {
		return outputTemp;
	}

	public void setOutputTemp(double outputTemp) {
		this.outputTemp = outputTemp;
	}

	public double getGain() {
		return gain;
	}

	public void setGain(double gain) {
		this.gain = gain;
	}

	public double getBias() {
		return bias;
	}

	public void setBias(double bias) {
		this.bias = bias;
	}

	public double getT() {
		return t;
	}

	public void setT(double t) {
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

	public double[] getWeight() {
		return weight;
	}

	public void setWeight(double[] weight) {
		this.weight = weight;
	}

	public double getTimeStep() {
		return timeStep;
	}

	public void setTimeStep(double timeStep) {
		this.timeStep = timeStep;
	}

	public double getTransferFlatness() {
		return transferFlatness;
	}

	public void setTransferFlatness(double transferFlatness) {
		this.transferFlatness = transferFlatness;
	}

	public TransferFunction getTransferFunction() {
		return transferFunction;
	}

	

}
