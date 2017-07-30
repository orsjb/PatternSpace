//
//  DoublePointer.java
//  JCtrnn
//
//  Created by Oliver Bown on 27/10/2005.
//  Copyright 2005 __MyCompanyName__. All rights reserved.
//

package com.olliebown.ctrnn;

import java.io.Serializable;



public class DoublePointer implements Serializable {
	
	protected float value;
	
	public DoublePointer() {
	}
	
	public DoublePointer(float value) {
		this.value = value;
	}

	public float getValue() {
		return value;
	}

	public void setValue(float value) {
		this.value = value;
	}
	
	
}
