package net.happybrackets.patternspace.trainer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class DataList implements Serializable {

	/*
	 * Stores a hashmap along with an array list of names. The array list cannot be changed.
	 */
	
	private static final long serialVersionUID = 1L;
	private Hashtable<String, Float> data;
	private ArrayList<String> dataNames;
	
	public DataList() {
		data = new Hashtable<String, Float>();
		dataNames = new ArrayList<String>();
	}
	
	public void addName(String name) {
		if(!dataNames.contains(name)) dataNames.add(name);
	}
	
	public List<String> names() {
		return (List<String>)dataNames.clone();
	}
	
	//TODO options to make this more efficient: buffer array list and keep check on whether anything
	//has changed
	public float[] values() {
		//values are ordered according to names
		float[] values = new float[dataNames.size()];
		for(int i = 0; i < dataNames.size(); i++) {
			values[i] = data.get(dataNames.get(i));
		}
		return values;
	}
	
	public void set(int i, float val) {
		data.put(dataNames.get(i), val);
	}
	
	public void set(String name, float val) {
		addName(name);
		data.put(name, val);
	}
	
	public Float getVal(String name) {
		return data.get(name);
	}
	
	public String getName(int i) {
		return dataNames.get(i);
	}
	
	public Float getVal(int i) {
		return data.get(dataNames.get(i));
	}
	
	public int size() {
		return dataNames.size();
	}
}
