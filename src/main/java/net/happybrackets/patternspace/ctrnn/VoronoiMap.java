package net.happybrackets.patternspace.ctrnn;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.Random;


public class VoronoiMap implements Serializable {

	private static final long serialVersionUID = 1;
	private static float width = 1f;
	
	private class Point implements Serializable {
		
		private static final long serialVersionUID = 1L;

		float[][][] coeff; //mapping from relative position
		float[] pos;  
		
		public float[] calculateOutput(float[] input) {
			int minDim = Math.min(input.length, p.ins);
			float[] result = new float[p.outs];
			for(int i = 0; i < p.outs; i++) {
				result[i] = 0.0f;
				for(int k = 0; k < p.depth; k++) {
					for(int j = 0; j < minDim; j++) {
						float axisDistance = input[j] - pos[j];
						while(axisDistance > width / 2) axisDistance -= width;
						while(axisDistance < -(width / 2)) axisDistance += width;
						result[i] += coeff[i][j][k] * (float)Math.pow(2 * axisDistance, k);
					}
				}
				result[i] = (float)Math.tanh(result[i]) * 0.5f + 0.5f;
			}
			return result;
		}
	}
	
	private Params p;
	private Point[] points; //a list of points, each of which stores a map from xy to z.
	private int lastId;
	
	public VoronoiMap(Chromosome c, Params vmp) {
		this.p = vmp;
		points = new Point[vmp.numPoints];
		for(int i = 0; i < vmp.numPoints; i++) {
			points[i] = new Point();
			points[i].coeff = new float[p.outs][p.ins][p.depth];
			points[i].pos = new float[p.ins];
		}
		generateFromChromosome(c);
	}
	
	public VoronoiMap(Params vmp) {
		this(Chromosome.newRandom(new Random(), vmp.getGenotypeLength()), vmp);
	}
	
	public int getLastPointIndex() {
		return lastId;
	}
	
	public void generateFromChromosome(Chromosome c) {
		for(int pIndex = 0; pIndex < p.numPoints; pIndex++) {
			for(int j = 0; j < p.ins; j++) {
				points[pIndex].pos[j] = (float)c.getNextGene() * 2f - 1f;
				for(int i = 0; i < p.outs; i++) {
					for(int k = 0; k < p.depth; k++) {
						points[pIndex].coeff[i][j][k] = (float)(linMap(c.getNextGene(), p.coeffMin, p.coeffMax));
					}
				}
			}
		}
	}
	
	public Params getParams() {
		return p;
	}
	
	private static double linMap(double x, double min, double max) {
		return ( x * (max - min) ) + min;
	}
	
	public float[] calculateOutput(float[] input) {
		
		//find nearest point
		float bestDistance = Float.MAX_VALUE;
		int bestPointIndex = -1;
		for(int i = 0; i < points.length; i++) {
//			System.out.println(points[i].pos.length);
			float distance = distance(points[i].pos, input);
			if(distance < bestDistance) {
				bestDistance = distance;
				bestPointIndex = i;
			}
		}
		lastId = bestPointIndex;
		return points[bestPointIndex].calculateOutput(input);
	}
	
	public float distance(float[] a, float[] b) {
		int minDim = Math.min(a.length, b.length);
		float distance = 0.0f;
		for(int i = 0; i < minDim; i++) {
			float axisDistance = Math.abs(a[i] - b[i]);
			if(p.toroidal) {
				if(axisDistance > width / 2) axisDistance = (width / 2) - axisDistance;
			}
			distance += axisDistance * axisDistance;
		}
		distance = (float)Math.sqrt(distance);
		return distance;
	}
	

	public static class Params implements Serializable {
		
		private static final long serialVersionUID = 1;
		
		public int numPoints;
		public int ins;
		public int outs;
		public int depth;
		public double coeffMin;
		public double coeffMax;
		public boolean toroidal;
		
		public Params() {
			resetToDefault();
		}

		private void resetToDefault() {
			numPoints = 30;
			ins = 2;
			outs = 1;
			depth = 15;
			coeffMin = -1.0;
			coeffMax = 1.0;
			toroidal = true;
		}
		
		public int getGenotypeLength() {
			return numPoints * (ins * outs * depth + ins);
		}
		
	}
	
	public static void main(String[] args) throws IOException {
		File outFile = new File("/Users/ollie/Desktop/voroini");
		FileOutputStream fos = new FileOutputStream(outFile);
		PrintStream ps = new PrintStream(fos);
		Params vmp = new Params();
		Chromosome c = Chromosome.newRandom(new Random(), vmp.getGenotypeLength());
		VoronoiMap vm = new VoronoiMap(c, vmp);
		for(int i = 0; i < vmp.numPoints; i++) {
			System.out.println(vm.points[i].pos[0] + " " + vm.points[i].pos[1]);
		}
		int pixelWidth = 500;
		for(int i = 0; i < pixelWidth; i++) {
			float x = (float)i / (float)pixelWidth;
			for(int j = 0; j < pixelWidth; j++) {
				float y = (float)j / (float)pixelWidth;
				ps.println(x + " " + y + " " + vm.calculateOutput(new float[] {x, y})[0]);
			}
			ps.println();
		}
		ps.close();
		fos.close();
		System.out.println(c.getGenes().length);
	}
	
}
