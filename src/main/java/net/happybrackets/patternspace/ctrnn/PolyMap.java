/**
 * 
 */
package net.happybrackets.patternspace.ctrnn;

import java.io.Serializable;

public class PolyMap implements Serializable {
	
	private static final long serialVersionUID = 1;
	
	private Params p;
	private float[][][] coeff;
	private float[][][] mod;
	
	public PolyMap(Params p) {
		this.p = p;
		coeff = new float[p.outs][p.ins][p.depth];
		mod = new float[p.outs][p.ins][p.depth];
		generateRandom();
	}
	
	public PolyMap(Chromosome c, Params p) {
		this.p = p;
		coeff = new float[p.outs][p.ins][p.depth];
		mod = new float[p.outs][p.ins][p.depth];
		generateFromChromosome(c);
	}
	
	public void generateFromChromosome(Chromosome c) {
		for(int i = 0; i < p.outs; i++) {
			for(int j = 0; j < p.ins; j++) {
				for(int k = 0; k < p.depth; k++) {
					coeff[i][j][k] = (float)(linMap(c.getNextGene(), p.coeffMin, p.coeffMax));
					mod[i][j][k] = (float)(linMap(c.getNextGene(), p.modMin, p.modMax));
					//System.out.println(coeff[i][j][k] + " " + mod[i][j][k]);
				}
			}
		}
	}
	
	public float[] compute(float[] ds) {
		float[] result = new float[p.outs];
		for(int i = 0; i < p.outs; i++) {
			result[i] = 0.0f;
			for(int k = 0; k < p.depth; k++) {
				for(int j = 0; j < p.ins; j++) {
					result[i] += coeff[i][j][k] * (float)Math.pow(ds[j], k) % mod[i][j][k];
				}
				//result[i] = result[i]  % mod[i][0][k]; // if we do this then mod only needs 2D
			}
			//result[i] = (float)Math.tanh(result[i]) * 0.5f + 0.5f;
		}
		return result;
	}
	
	public void generateRandom() {
		for(int i = 0; i < p.outs; i++) {
			for(int j = 0; j < p.ins; j++) {
				for(int k = 0; k < p.depth; k++) {
					coeff[i][j][k] = (float)(linMap((float)Math.random(), p.coeffMin, p.coeffMax));
					mod[i][j][k] = (float)(linMap((float)Math.random(), p.modMin, p.modMax));
				}
			}
		}
	}
	
	public static class Params implements Serializable {
		
		private static final long serialVersionUID = 1;
		
		public int ins;
		public int outs;
		public int depth;
		public float coeffMin;
		public float coeffMax;
		public float modMin;
		public float modMax;
		
		public Params() {
			this.resetToDefault();
		}

		private void resetToDefault() {
			ins = 1;
			outs = 1;
			depth = 10;
			coeffMin = -1.0f;
			coeffMax = 1.0f;
			modMin = 0;
			modMax = 10;
		}
		
		public int getGenotypeLength() {
			return ins * outs * depth * 2;
		}
		
	}

	
	public static void main(String[] args) {
		JCtrnn.Params params = new JCtrnn.Params();
		params.numInputNodes = 1;
		params.numHiddenNodes = 50;
		params.numOutputNodes = 1;
		JCtrnn ctrnn = new JCtrnn(params);
		ctrnn.resetZero();
		Params pmp = new Params();
		PolyMap pm = new PolyMap(pmp);
		for(int i = 0; i < 1000; i++) {
			ctrnn.update(new float[] {0.0f});
			//System.out.println(pm.compute(ctrnn.getOutputs())[0]);
		}
	}
	
	private static float linMap(float x, float min, float max) {
		return ( x * (max - min) ) + min;
	}
	
}