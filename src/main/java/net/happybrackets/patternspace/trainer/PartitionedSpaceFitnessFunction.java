package net.happybrackets.patternspace.trainer;

import java.util.ArrayList;
import java.util.Random;

import org.jgap.FitnessFunction;
import org.jgap.IChromosome;

import net.happybrackets.patternspace.ctrnn.Chromosome;
import net.happybrackets.patternspace.ctrnn.JCtrnn;
import net.happybrackets.patternspace.ctrnn.JLi;

public class PartitionedSpaceFitnessFunction extends FitnessFunction {

	private static final long serialVersionUID = 1L;
	private JCtrnn.Params params;
	private Random rng;
	private int numPartitions;
	private Partition[] partitions;
	private float[][] inputs;
	float[] zeroInput;
	private int leadTime;
	private int inputTime;
	private int endTime;
	
	private class Partition {
		
		float[] mins;
		float[] maxs;
		
		boolean contains(float[] pos) {
			for(int i = 0; i < mins.length; i++) {
				if(pos[i] >= maxs[i] || pos[i] < mins[i]) {
					return false;
				}
			}
			return true;
		}
		
		void print() {
			for(int i = 0; i < mins.length; i++) {
				System.out.print(mins[i] + " ");
			}
			System.out.println();
			for(int i = 0; i < maxs.length; i++) {
				System.out.print(maxs[i] + " ");
			}
			System.out.println();
		}
		
	}
	
	Partition randomPartition() {
		Partition p = new Partition();
		int n = params.numOutputNodes;
		p.mins = new float[n];
		p.maxs = new float[n];
		for(int i = 0; i < n; i++) {
			if(rng.nextFloat() < 0.5f) {
				float a = rng.nextFloat();
				float b = rng.nextFloat();
				p.mins[i] = Math.min(a, b);
				p.maxs[i] = Math.max(a, b);
			} else {
				p.mins[i] = 0;
				p.maxs[i] = 1;
			}
		}
		return p;
	}
	
	public PartitionedSpaceFitnessFunction(JCtrnn.Params params, Random rng) {
		this.params = params;
		this.rng = rng;
		//settings
		numPartitions = 20;
		leadTime = 500;
		inputTime = 500;
		endTime = 500;
		//make a random partitioningx
		partitions = new Partition[numPartitions];
		for(int i = 0; i < numPartitions; i++) {
			partitions[i] = randomPartition();
			partitions[i].print();
		}
		inputs = new float[numPartitions][params.getNumInputNodes()];
		for(int i = 0; i < inputs.length; i++) {
			for(int j = 0; j < inputs[i].length; j++) {
				inputs[i][j] = rng.nextFloat();
			}
		}
		zeroInput = new float[params.numInputNodes];
	}
	
	@Override
	protected double evaluate(IChromosome chromo) {
		//the space is partitioned
		//the network has to keep its outputs in a given part of the space
		//for a given input.
		//the network must cycle (additional)
		JCtrnn ctrnn = new JCtrnn(Chromosome.fromJGAPChromosome(chromo), params);
		float partitionScore = 0f;
		float changeScore = 0f;
		ctrnn.resetZero();
		
		//rearrange the sequence
		ArrayList<Float> order = new ArrayList<Float>();
		int[] shuffledOrder = new int[numPartitions];
		int index = 0;
		for(int i = 0; i < numPartitions; i++) {
			order.add((float)i);
		}
		while(order.size() > 0) {
			int choice = rng.nextInt(order.size());
			shuffledOrder[index++] = order.get(choice).intValue();
			order.remove(choice);
		}
		
		for(int trial = 0; trial < numPartitions; trial++) {
//			for(int time = 0; time < leadTime; time++) {
//				ctrnn.update(zeroInput);
//			}
			for(int time = 0; time < inputTime; time++) {
				ctrnn.update(inputs[shuffledOrder[trial]]);
				float[] outputs = ctrnn.getOutputs().clone();
				for(int i = 0; i < outputs.length; i++) {
					outputs[i] = outputs[i] * 0.5f + 0.5f;
				}
				if(partitions[shuffledOrder[trial]].contains(outputs)) {
					partitionScore += 1;
				}
			}
			float[] last = new float[ctrnn.params.numOutputNodes];
			float[] change = new float[ctrnn.params.numOutputNodes];
			ctrnn.update(zeroInput);
			for(int i = 0; i < change.length; i++) {
				last[i] = ctrnn.getOutput(i);
			}
//			for(int time = 0; time < endTime; time++) {
//				ctrnn.update(zeroInput);
//				float thisScore = 0f;
//				for(int i = 0; i < change.length; i++) {
//					change[i] = ctrnn.getOutput(i) - last[i];
//					last[i] = ctrnn.getOutput(i);
//					thisScore += Math.abs(change[i]);
//				}
//				thisScore /= change.length;
//				thisScore = Math.min(1, thisScore);
//				changeScore += 1f - thisScore;
//			}
		}
		partitionScore /= (numPartitions * inputTime);
		changeScore /= (numPartitions * endTime);
		
//		System.out.println("partition score: " + partitionScore + " change score: " + changeScore);
		
		//return weighted value
		float weightChange = 0.f; //??
		return weightChange * changeScore + (1f - weightChange) * partitionScore;
	}

	public static void main(String[] args) throws Exception {
		JCtrnn.Params params = new JCtrnn.Params();
		params.resetToDefault();
		params.numInputNodes = 11;
		params.numHiddenNodes = 20;
		params.numOutputNodes = 20;
		params.inWeightMax = 20f;
		params.inWeightMin = -20f;
		params.hWeightMax = 20f;
		params.hWeightMin = -20f;
		params.inTcMax = params.hTcMax = 5f;
		params.inTcMin = params.hTcMin = -1f;
		params.hTransferFunc = JLi.TransferFunction.SINTANH;
		
		PartitionedSpaceFitnessFunction bdff = new PartitionedSpaceFitnessFunction(params, new Random());
		CTRNNTrainer trainer = new CTRNNTrainer(params, bdff, "/Users/ollie/Desktop/evolve");
		trainer.evolve();
	}
}
