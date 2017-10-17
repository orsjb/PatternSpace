package net.happybrackets.patternspace.dynamic_system.core;

import java.io.Serializable;
import java.io.Writer;

public interface DynamicSystem extends Serializable {

    public DynamicSystemProperties getProperties();
    public void update(Number[] inputs);
    public Number[] getOutputs();
    public void writeJSON(Writer out);

}
