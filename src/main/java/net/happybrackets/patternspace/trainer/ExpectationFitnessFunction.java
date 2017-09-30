package net.happybrackets.patternspace.trainer;

import java.util.List;

import org.jgap.FitnessFunction;
import org.jgap.IChromosome;

import net.happybrackets.patternspace.ctrnn.Chromosome;
import net.happybrackets.patternspace.ctrnn.JCtrnn;

public class ExpectationFitnessFunction extends FitnessFunction {

	private static final long serialVersionUID = 1L;
	
	List<OutputExpectation> expectations;
	JCtrnn.Params params;

	//simulation settings
	int totalTime;
	int numTrials;
	JCtrnn[] refCtrnn;

	public ExpectationFitnessFunction(List<OutputExpectation> expectations, JCtrnn.Params params) {
		this.expectations = expectations;
		this.params = params;
		numTrials = 10;
		totalTime = 100;
		JCtrnn.Params altParams = new JCtrnn.Params();
		altParams.numInputNodes = 0;
		altParams.numHiddenNodes = 100;
		altParams.numOutputNodes = params.numInputNodes;
		refCtrnn = new JCtrnn[numTrials];
		for(int i = 0; i < numTrials; i++) {
			refCtrnn[i] = new JCtrnn(altParams);
		}
	}
	
	@Override
	protected double evaluate(IChromosome chromo) {

		JCtrnn ctrnn = new JCtrnn(Chromosome.fromJGAPChromosome(chromo), params);
		
		float[] inputs = new float[params.numInputNodes];
		float[] outputs = new float[params.numOutputNodes];
		
		float averageScore = 0f;
		//do the tests
		for(int trial = 0; trial < numTrials; trial++) {
			ctrnn.resetZero();
			refCtrnn[trial].resetZero();
			for(int output = 0; output < outputs.length; output++) {
				expectations.get(output).reset();
			}
			for(int time = 0; time < totalTime; time++) {
				refCtrnn[trial].update();
				inputs = refCtrnn[trial].getOutputs();
				ctrnn.update(inputs);
				outputs = ctrnn.getOutputs();
				
			}
			float tempScore = 0f;
			for(int output = 0; output < outputs.length; output++) {
				expectations.get(output).update(inputs, outputs[output]);
				tempScore += expectations.get(output).getAverageFit();
			}
			tempScore /= outputs.length;
			averageScore += tempScore;
		}
		averageScore /= numTrials;
		return averageScore;
	}

}
