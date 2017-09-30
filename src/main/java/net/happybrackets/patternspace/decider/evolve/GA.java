package net.happybrackets.patternspace.decider.evolve;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Random;

import net.happybrackets.patternspace.decider.core.Decider;

public class GA {

	//dumbest GA you could imagine..
	public static void main(String[] args) {
		String dir = "dudes";
		File dataFolder = new File("/Users/ollie/Data/ACALCI/evolved deciders/" + dir);
		dataFolder.mkdir();
		dir = dataFolder.getAbsolutePath();
		
		int popSize = 100;
		int iterations = 1000000;
		FitnessFunction ff = new FitnessFunction() {
			@Override
			public float evaluate(Decider d) {
				return 0;
			}
		};
		Random rng = new Random();
		Decider[] pop = new Decider[popSize];
		float[] fitness = new float[popSize];
		int fittest = 0;
		//init
		for(int i = 0; i < popSize; i++) {
//			pop[i] = Decider.newRandomStub(200, 401, rng);
//			pop[i] = Decider.newRandomTree(200, 401, rng, 0.05f);
			pop[i] = Decider.newRandomTree(6, 30, rng, 0.05f);
			fitness[i] = ff.evaluate(pop[i]);
//			fitness[i] = (float)Math.random();
			if(fitness[i] > fitness[fittest]) fittest = i;
		}
		//run
		for(int time = 0; time < iterations; time++) {
			int a = rng.nextInt(popSize);
			int b = a;
			while(b == a) {
				b = rng.nextInt(popSize);
			}
			int winner = (fitness[a] > fitness[b]) ? a : b;
			int loser = (winner == a) ? b : a;
			pop[loser] = pop[winner].copyMutate();
			fitness[loser] = ff.evaluate(pop[loser]);
//			fitness[loser] = (float)Math.random();
			if(fitness[loser] > fitness[fittest]) fittest = loser;
			if(time % 100 == 0) {
				System.out.println("----------------------");
				System.out.println("fittest : fitness=" + fitness[fittest]);
//				DeciderSimulationStats dss = ads.run(pop[fittest]);
//				dss.printStats();
				write(dir, pop[fittest], time);
			}
		}
	}

	private static void write(String dir, Decider decider, int gen) {
		try {
			FileOutputStream fos = new FileOutputStream(new File(dir + "/gen" + gen));
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(decider);
			oos.close();
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
