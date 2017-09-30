package net.happybrackets.patternspace.core;

import java.io.Serializable;

public interface ProcessingUnit {

    public void update(double[] inputs);
    public double[] getOutputs();
    public int getDiscreteState();

}
