package net.happybrackets.patternspace.ctrnn;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Random;

import org.jgap.Gene;
import org.jgap.IChromosome;
import org.jgap.impl.DoubleGene;

public class Chromosome implements Serializable {
	
	private static final long serialVersionUID = 1;
	
	private double[] genes;
	private int index;
	
	public Chromosome(int length) {
		genes = new double[length];
		reset();
	}
	
	public void reset() {
		index = 0;
	}
	
	public double getNextGene() {
		return genes[index++];
	}
	
	public static Chromosome fromJGAPChromosome(IChromosome source) {
		Gene[] sourceGenes = source.getGenes();
		Chromosome c = new Chromosome(sourceGenes.length);
		for(int i = 0; i < sourceGenes.length; i++) {
			c.genes[i] = ((DoubleGene)sourceGenes[i]).doubleValue();
		}
		return c;
	}
	
	public static Chromosome fromJGAPChromosomeFile(String filename) throws Exception {
		FileInputStream fis = new FileInputStream(new File(filename));
		ObjectInputStream ois = new ObjectInputStream(fis);
		IChromosome c = (IChromosome)ois.readObject();
		return fromJGAPChromosome(c);
	}
	
	public static Chromosome newRandom(Random rng, int length) {
		Chromosome newRandom = new Chromosome(length);
		for(int i = 0; i < length; i++) {
			newRandom.genes[i] = rng.nextDouble();
		}
		return newRandom;
	}
	
	public double[] getGenes() {
		return genes;
	}
	
	public Chromosome mutate(Random rng, double stdDev) {
		Chromosome child = new Chromosome(genes.length);
		for(int i = 0; i < genes.length; i++) {
			child.genes[i] = genes[i] + rng.nextGaussian() * stdDev;
			if(child.genes[i] < 0.0f) child.genes[i] = -0.5f * child.genes[i];
			else if(child.genes[i] > 1.0f) child.genes[i] = 1.0f - 0.5f * (child.genes[i] - 1.0f);
		}
		return child;
	}

	public double getGeneticDistance(Chromosome c) {
		double distance = 0.0f;
		for(int i = 0; i < genes.length; i++) {
			distance += (genes[i] - c.genes[i]) * (genes[i] - c.genes[i]);
		}
		distance = Math.sqrt(distance / genes.length); //RMS
		return distance;
	}

}
