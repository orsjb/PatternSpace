package com.happybrackets.patternspace.sandpit;


import org.deeplearning4j.nn.conf.graph.GraphVertex;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.inputs.InvalidInputTypeException;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.nd4j.linalg.api.ndarray.INDArray;

public class LeakyIntegratorGraphVertex extends GraphVertex {


    @Override
    public GraphVertex clone() {
        return null;
    }

    @Override
    public boolean equals(Object o) {
        return false;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public int numParams(boolean b) {
        return 0;
    }

    @Override
    public org.deeplearning4j.nn.graph.vertex.GraphVertex instantiate(ComputationGraph computationGraph, String s, int i, INDArray indArray, boolean b) {
        return null;
    }

    @Override
    public InputType getOutputType(int i, InputType... inputTypes) throws InvalidInputTypeException {
        return null;
    }
}
