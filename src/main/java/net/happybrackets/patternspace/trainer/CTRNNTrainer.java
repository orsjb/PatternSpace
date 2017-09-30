package net.happybrackets.patternspace.trainer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;

import org.jgap.Chromosome;
import org.jgap.Configuration;
import org.jgap.FitnessFunction;
import org.jgap.Gene;
import org.jgap.Genotype;
import org.jgap.IChromosome;
import org.jgap.impl.DefaultConfiguration;
import org.jgap.impl.DoubleGene;
import net.happybrackets.patternspace.ctrnn.JCtrnn;


public class CTRNNTrainer {
	
	/*
	 * Trains a CTRNN using a GA. Requires a CTRNN Params object and a bunch of data 
	 * about the expected kinds of mappings that will emerge. This data takes the form of a
	 * matrix of expectations. For each input/output pair, define an expectation and then
	 * use the expectqtion to calculate contribution to fitness.
	 */
	
	JCtrnn.Params params;
	FitnessFunction myFunc;
	String destDir;
	
	public CTRNNTrainer(JCtrnn.Params params, FitnessFunction myFunc, String destDir) {
		this.params = params;
		this.myFunc = myFunc;
		this.destDir = destDir;
	}
	
	public void evolve() throws Exception {
		//use JGAP to evolve
		Configuration conf = new DefaultConfiguration();
	    conf.setPreservFittestIndividual(true);
	    conf.setFitnessFunction(myFunc);
	    Gene[] sampleGenes = new Gene[params.getGenotypeLength()];
	    for(int i = 0; i < sampleGenes.length; i++) {
	    	sampleGenes[i] = new DoubleGene(conf, 0, 1);
	    }
	    Chromosome sampleChromosome = new Chromosome(conf, sampleGenes);
	    conf.setSampleChromosome(sampleChromosome);
	    conf.setPopulationSize(30);
	    Genotype pop = Genotype.randomInitialGenotype(conf);
	    for (int i = 0; i < 1000; i++) {
		    System.out.print("gen " + i + ", ");
	          FileOutputStream fos = new FileOutputStream(new File(destDir + "/fittest_gen" + i));
	          ObjectOutputStream oos = new ObjectOutputStream(fos);
	          IChromosome fittest = pop.getFittestChromosome();
	          JCtrnn ctrnn = new JCtrnn(net.happybrackets.patternspace.ctrnn.Chromosome.fromJGAPChromosome(fittest), params);
	          oos.writeObject(ctrnn);
	          pop.evolve();
	          // add current best fitness to chart
	          double fitness = fittest.getFitnessValue();
	          System.out.println("fitness: " + fitness);
	    }
	}

}
