package net.happybrackets.patternspace.trainer;

import java.util.ArrayList;

import org.jgap.FitnessFunction;
import org.jgap.IChromosome;

import net.happybrackets.patternspace.ctrnn.Chromosome;
import net.happybrackets.patternspace.ctrnn.JCtrnn;
import net.happybrackets.patternspace.ctrnn.JLi;

public class BeDifferentFitnessFunction extends FitnessFunction {

	private static final long serialVersionUID = 1L;

	JCtrnn.Params params;
	int leadTime, inputTime, measureTime;
	int numTrials;
	float[] zeroInput;
	float[][] inputs;
	
	public BeDifferentFitnessFunction(JCtrnn.Params params) {
		this.params = params;
		numTrials = 5;
		leadTime = 500;
		inputTime = 100;
		measureTime = 100;
		zeroInput = new float[params.numInputNodes];
		inputs = new float[numTrials][params.numInputNodes];
		for(int i = 0; i < numTrials; i++) {
			for(int j = 0; j < params.numInputNodes; j++) {
				inputs[i][j] = (float)Math.random();
			}
		}
	}
	
	@Override
	protected double evaluate(IChromosome chromo) {
		JCtrnn ctrnn = new JCtrnn(Chromosome.fromJGAPChromosome(chromo), params);
		ArrayList<ArrayList<float[]>> output = new ArrayList<ArrayList<float[]>>();
		float finalScore = 0f;
		for(int trial = 0; trial < numTrials; trial++) {
//			ctrnn.resetZero();
			for(int time = 0; time < leadTime; time++) {
				ctrnn.update(zeroInput);
			}
			
			for(int time = 0; time < inputTime; time++) {
				ctrnn.update(inputs[trial]);
			}
			ArrayList<float[]> thisOutput = new ArrayList<float[]>();
			for(int time = 0; time < measureTime; time++) {
				ctrnn.update(inputs[trial]);
				thisOutput.add(ctrnn.getOutputs());
			}
			output.add(thisOutput);
			float[] last = new float[ctrnn.params.numOutputNodes];
			float[] change = new float[ctrnn.params.numOutputNodes];
			float score = 0f;
			for(int time = 0; time < leadTime; time++) {
				ctrnn.update(zeroInput);
				float thisScore = 0f;
				for(int i = 0; i < change.length; i++) {
					change[i] = ctrnn.getOutput(i) - last[i];
					last[i] = ctrnn.getOutput(i);
					thisScore += Math.abs(change[i]);
				}
//				thisScore /= change.length;
				thisScore = Math.min(1, thisScore);
				score += 1f - thisScore;
			}
			score /= leadTime;
			finalScore += score;
		}
		finalScore /= numTrials;
		//how much to weight the score towards this stillness value?
		finalScore *= 10f;
		float diff = DifferenceMeasure.measureAllDifferences(output);
//		System.out.println(diff);
		finalScore += diff;
		finalScore /= 11f;
		return finalScore;
	}
	
	public static void main(String[] args) throws Exception {
		JCtrnn.Params params = new JCtrnn.Params();
		params.resetToDefault();
		params.numInputNodes = 200;
		params.numHiddenNodes = 200;
		params.numOutputNodes = 200;
	
		params.hGainMax = 0.01f;
		params.hGainMin = 0f;
		
		params.hWeightMax = 2f;
		params.hWeightMin = -1f;
		
		params.inGainMax = 10f;
		params.inWeightMax = 20f;
		params.inWeightMin = -20f;
		
		params.timeStep = 0.1f;
		
		params.inTcMin = -3;
		params.inTcMax = 10;

		params.hTcMin = -3;
		params.hTcMax = 10;
		
		params.hTransferFunc = JLi.TransferFunction.TANH;
		params.inTransferFunc = JLi.TransferFunction.TANH;
		
		
		BeDifferentFitnessFunction bdff = new BeDifferentFitnessFunction(params);
		CTRNNTrainer trainer = new CTRNNTrainer(params, bdff, "/Users/ollie/Desktop/evolve");
		trainer.evolve();
	}

}
