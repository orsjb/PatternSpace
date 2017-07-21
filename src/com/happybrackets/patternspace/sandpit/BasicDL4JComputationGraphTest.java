package com.happybrackets.patternspace.sandpit;

import org.deeplearning4j.nn.conf.ComputationGraphConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.graph.GraphVertex;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.inputs.InvalidInputTypeException;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.GravesLSTM;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.conf.layers.RnnOutputLayer;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.lossfunctions.LossFunctions;

public class BasicDL4JComputationGraphTest {

    public static void main(String[] args) {


        //from the examples
        ComputationGraphConfiguration conf1 = new NeuralNetConfiguration.Builder()
                .learningRate(0.01)
                .graphBuilder()
                .addInputs("input") //can use any label for this
                .addLayer("L1", new GravesLSTM.Builder().nIn(5).nOut(5).build(), "input")
                .addLayer("L2",new RnnOutputLayer.Builder().nIn(5+5).nOut(5).build(), "input", "L1")
                .setOutputs("L2")	//We need to specify the network outputs and their order
                .build();


        ComputationGraphConfiguration conf2 = new NeuralNetConfiguration.Builder()
                .learningRate(0.01)
                .graphBuilder()
                .addInputs("input")
                .addLayer("L1", new DenseLayer.Builder().nIn(3).nOut(4).build(), "input")
                .addLayer("out1", new OutputLayer.Builder()
                        .lossFunction(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                        .nIn(4).nOut(3).build(), "L1")
                .addLayer("out2", new OutputLayer.Builder()
                        .lossFunction(LossFunctions.LossFunction.MSE)
                        .nIn(4).nOut(2).build(), "L1")
                .setOutputs("out1","out2")
                .build();


        ComputationGraph net1 = new ComputationGraph(conf1);
        net1.init();

        //here's my attempt



        ComputationGraphConfiguration ollieConf = new NeuralNetConfiguration.Builder()
//                .learningRate(0.01)
                .graphBuilder()
                .build();

    }

}
