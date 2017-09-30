package net.happybrackets.patternspace.trainer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class OutputExpectation implements Serializable {

	/*
	 * Contains an array of IO expectations 
	 */
	
	private IOExpectation[] ioExpectations;
	private List<float[]> outputHistory;

	private float hierarchicalMotion;
	private float jumpiness;
	private float curviness;
	private float longTermTrend;
	
	public OutputExpectation(int numInputs) {
		ioExpectations = new IOExpectation[numInputs];
		for(int i = 0; i < numInputs; i++) {
			ioExpectations[i] = new IOExpectation();
		}
	}
	
	public void reset() {
		outputHistory = new ArrayList<float[]>();
		for(IOExpectation ioExp : ioExpectations) {
			ioExp.reset();
		}
	}
	
	public float getNumInputs() {
		return ioExpectations.length;
	}
	
	public void update(float[] inputs, float output) {
		for(int i = 0; i < inputs.length; i++) {
			ioExpectations[i].update(inputs[i], output);
		}
	}
	
	IOExpectation getIOExpectation(int i) {
		return ioExpectations[i];
	}
	
	public float getAverageFit() {
		float averageFit = 0f;
		int count = 0;
		for(int i = 0; i < ioExpectations.length; i++) {
			if(ioExpectations[i].isIncluded()) {
				averageFit += ioExpectations[i].getAverageFit();
				count++;
			}
		}
		if(count != 0) averageFit /= count;
		return averageFit;
	}
}
