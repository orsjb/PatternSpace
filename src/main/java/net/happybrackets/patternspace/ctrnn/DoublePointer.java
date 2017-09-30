//
//  DoublePointer.java
//  JCtrnn
//
//  Created by Oliver Bown on 27/10/2005.
//  Copyright 2005 __MyCompanyName__. All rights reserved.
//

package net.happybrackets.patternspace.ctrnn;

import java.io.Serializable;



public class DoublePointer implements Serializable {
	
	protected double value;
	
	public DoublePointer() {
	}
	
	public DoublePointer(double value) {
		this.value = value;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}
	
	
}
