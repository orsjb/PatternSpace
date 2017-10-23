package net.happybrackets.patternspace.dynamic_system.core;

import com.google.gson.JsonElement;

import java.io.Serializable;
import java.io.Writer;

public interface DynamicSystem extends Serializable {

    public DynamicSystemProperties getProperties();
    public void update(Number[] inputs);
    public Number[] getOutputs();
    public JsonElement writeJSON();

}
