package net.happybrackets.patternspace.dynamic_system.core;

import java.io.Serializable;

public class DynamicSystemProperties implements Serializable {

    Class<? extends DynamicSystem> theClass;
    Class<? extends Number>[] inputTypes;
    Class<? extends Number>[] outputTypes;

    public DynamicSystemProperties(Class<? extends DynamicSystem> theClass, Class<? extends Number>[] inputTypes, Class<? extends Number>[] outputTypes) {
        this.inputTypes = inputTypes;
        this.outputTypes = outputTypes;
        this.theClass = theClass;
    }

    public Class<? extends Number>[] getInputTypes() {
        return inputTypes;
    }

    public Class<? extends Number>[] getOutputTypes() {
        return outputTypes;
    }

    public Class<? extends DynamicSystem> getTheClass() {
        return theClass;
    }
}
