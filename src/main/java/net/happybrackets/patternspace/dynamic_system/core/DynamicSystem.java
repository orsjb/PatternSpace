package net.happybrackets.patternspace.dynamic_system.core;

import java.io.Serializable;

public interface DynamicSystem {

    public void update(double[] inputs);
    public double[] getOutputs();
    public int getDiscreteState();

}
