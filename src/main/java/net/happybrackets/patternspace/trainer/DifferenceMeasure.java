package net.happybrackets.patternspace.trainer;

import java.util.ArrayList;

public class DifferenceMeasure {

	public static float measureAllDifferences(ArrayList<ArrayList<float[]>> all) {
		float overallDifference = 0f;
		int elements = all.size();
		int count = 0;
		for(int i = 0; i < elements; i++) {
			ArrayList<float[]> a = all.get(i);
			for(int j = 0; j < i; j++) {
				ArrayList<float[]> b = all.get(j);
				float difference = getSmallestDifference(a, b);
				overallDifference += difference;
				count++;
			}
		}
		overallDifference /= count;
//		System.out.println("overall diff = " + overallDifference);
		return overallDifference;
	}

	private static float getSmallestDifference(ArrayList<float[]> list1, ArrayList<float[]> list2) {
		float smallestDifference = Float.MAX_VALUE;
		int halfSize = list1.size() / 2;
		for(int i = -halfSize; i < halfSize; i++) {
			float difference = measureDifference(list1, list2, i);
			if(difference < smallestDifference) smallestDifference = difference;
		}
		return smallestDifference;
	}
	
	private static float measureDifference(ArrayList<float[]> list1, ArrayList<float[]> list2, int offset) {
		float averageDistance = 0f;
		int count = 0;
		for(int i = 0; i < list1.size(); i++) {
			if(i + offset < list2.size() && i + offset >= 0) { 
				float distance = rms(list1.get(i), list2.get(i + offset));
				averageDistance += distance;
				count++;
			}
		}
		if(count == 0) return Float.MAX_VALUE;
		averageDistance /= count;
		return averageDistance;
	}
	
	private static float rms(float[] a, float[] b) {
		float distance = 0f;
		for(int i = 0; i < a.length; i++) {
			distance += (a[i] - b[i]) * (a[i] - b[i]);
		}
		distance = (float)Math.sqrt(distance / a.length);
		return distance;
	}
}
