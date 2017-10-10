package net.happybrackets.patternspace.dynamic_system.core;

import java.io.Serializable;
import java.io.Writer;

public interface DynamicSystem extends Serializable {

    public void update(double[] inputs);
    public double[] getOutputs();
    public int getDiscreteState();
    public void writeJSON(Writer out);

}
