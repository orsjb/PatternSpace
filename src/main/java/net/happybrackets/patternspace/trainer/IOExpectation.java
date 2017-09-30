package net.happybrackets.patternspace.trainer;

import java.io.Serializable;

public class IOExpectation implements Serializable {

	/*
	 * Defines an expectation about the relationship between this input and output pair.
	 * Assumes inputs and outputs are bounded by 0-1.
	 */
	
	private int mapSize;
	private float[] map;
	private int count;
	private float accum;
	
	IOExpectation() {
		mapSize = 100;
		reset();
	}
	
	void createMap() {
		map = new float[mapSize];
		for(int i = 0; i < mapSize; i++) {
			map[i] = (float)i / mapSize;
		}
	}
	
	void destroyMap() {
		map = null;
	}
	
	boolean isIncluded() {
		return map != null;
	}
	
	void update(float input, float output) {
		//TODO simple case -- maybe just start with one mapping -- accumulate distance
		accum += Math.pow(1f - Math.abs(output - map[(int)(input * 99)]), 2f); //greatest this can be is 1
		count++;
	}
	
	float getAverageFit() {
		return accum / count;
	}
	
	public float mapValue(float x) {
		return map[(int)(x * mapSize)];
	}

	public void markMap(float x, float y) {
		if(map == null) {
			createMap();
		} 
		map[(int)(x * mapSize)] = y;
	}

	public void reset() {
		count = 0;
		accum = 0;
	}
	
	
}
