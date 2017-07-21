

package com.olliebown.ctrnn;

import java.io.Serializable;



public class RealVector implements Serializable {
	
	private static final long serialVersionUID = 1;
	
	private int dimensions;
	private double[] element;
	
	public RealVector(int dimensions) {
		this.dimensions = dimensions;
		element = new double[dimensions];
		for(int i = 0; i < element.length; i++) {
			element[i] = 0.0;
		}
	}
	
	public RealVector copy() {
		RealVector copy = new RealVector(this.dimensions);
		for(int i = 0; i < dimensions; i++) {
			copy.setComponent(i, this.getComponent(i));
		}
		return copy;
	}
	
	public double getComponent(int index) {
		return element[index];
	}


	public void setComponent(int index, double value) {
		element[index] = value;
	}
	
	
	public RealVector add(RealVector other) {
		int minDim = Math.min(this.dimensions, other.dimensions);
		RealVector newVector = new RealVector(minDim);
		for(int i = 0; i < minDim; i++) {
			newVector.setComponent(i, this.getComponent(i) + other.getComponent(i));
		}	
		return newVector;
	}
	
	
	public RealVector subtract(RealVector other) {
		int minDim = Math.min(this.dimensions, other.dimensions);
		RealVector newVector = new RealVector(minDim);
		for(int i = 0; i < minDim; i++) {
			newVector.setComponent(i, this.getComponent(i) - other.getComponent(i));
		}	
		return newVector;
	}
	
	
	public double dotProduct(RealVector other) {
		int minDim = Math.min(this.dimensions, other.dimensions);
		double dotProd = 0.0;
		for(int i = 0; i < minDim; i++) {
			dotProd += this.getComponent(i) * other.getComponent(i);
		}	
		return dotProd;
	}
	
	
	public double distance(RealVector other) {
		int minDim = Math.min(this.dimensions, other.dimensions);
		double distance = 0.0;
		for(int i = 0; i < minDim; i++) {
			distance += (this.getComponent(i) - other.getComponent(i)) * (this.getComponent(i) - other.getComponent(i));
		}	
		distance = Math.pow(distance, 0.5);
		return distance;
	}
	

	public double norm() {
		double norm = 0.0;
		for(int i = 0; i < this.dimensions; i++) {
			norm += this.getComponent(i) * this.getComponent(i);
		}	
		norm = Math.pow(norm, 0.5);
		return norm;
	}
	
	
	public double norm(double power) {
		double norm = 0.0;
		for(int i = 0; i < this.dimensions; i++) {
			norm += Math.pow(this.getComponent(i), power);
		}	
		norm = Math.pow(norm, (1.0/power));
		return norm;
	}
	
	
	public RealVector scalarMultiply(double factor) {
		RealVector newVector = new RealVector(this.dimensions);
		for(int i = 0; i < this.dimensions; i++) {
			newVector.setComponent(i, this.getComponent(i) * factor);
		}
		return newVector;
	}
	
	
	public RealVector scalarDivide(double divisor) {
		RealVector newVector = new RealVector(this.dimensions);
		for(int i = 0; i < this.dimensions; i++) {
			newVector.setComponent(i, this.getComponent(i) / divisor);
		}
		return newVector;
	}
	
	//accessor methods
	public int getDimensions() {
		return dimensions;
	}
	
}
