/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package neuralnetwork.framework;

import functions.F;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import neuralnetwork.NNInterface;

/**
 *
 * @author Thales
 */
public class DataSet {
    public double[][] inputs;
    public double[][] targets;
    
    public int batchIndex = 0;
    public int batchSize = 0;
    public int dataLoops = 0;
    
    private double[][] inputsBatch;
    private double[][] targetsBatch;
    
    public DataSet(double[][] inputs, double[][] targets){
        this.inputs = inputs;
        this.targets = targets;
    }

    public void nextBatch(int batchSize, boolean randomizeEveryLoop){
        batchIndex += this.batchSize;
        if (batchSize > inputs.length) batchSize = inputs.length;
        if (inputs.length-batchIndex-batchSize <= 0){
            this.batchSize = inputs.length-batchIndex;
            if (this.batchSize <= 0){
                batchIndex = 0;
                dataLoops++;
                this.batchSize = batchSize;
                if (randomizeEveryLoop) randomizeOrder();
            }
        }else{
            this.batchSize = batchSize;
        }
        inputsBatch = new double[this.batchSize][];
        targetsBatch = new double[this.batchSize][];
        System.arraycopy(inputs, batchIndex, inputsBatch, 0, this.batchSize);
        System.arraycopy(targets, batchIndex, targetsBatch, 0, this.batchSize);
    }
    public double[][] getInputsBatch(){
        return inputsBatch;
    }
    public double[][] getTargetsBatch(){
        return targetsBatch;
    }
    
    public double evaluate(NNInterface nn){
        nextBatch(inputs.length,true);
        double maxFitness = batchSize*targets[0].length;
        double fitness = maxFitness;
        for (int i = 0; i < inputs.length; i++){
            double[] output = nn.feedForward(inputs[i]);
            for (int j = 0; j < output.length; j++){
                fitness -= Math.abs(targets[i][j]-output[j])*2;
            }
        }
        return F.map(fitness, 0, maxFitness, 0, 1);
    }
    
    @Override
    public String toString(){
        String string = "DataSet size: "+inputs.length;
        for (int i = 0; i < inputs.length; i++){
            string += "\n   Input["+i+"]: "+Arrays.toString(inputs[i])+" target:"+Arrays.toString(targets[i]);
        }
        return string;
    }
    
    private void randomizeOrder(){
        ArrayList<Integer> rIndex = new ArrayList<>(inputs.length);
        ArrayList<double[]> rInputs = new ArrayList<>(inputs.length);
        ArrayList<double[]> rTargets = new ArrayList<>(inputs.length);
        
        for (int i = 0; i < inputs.length; i++){
            rIndex.add(i);
        }
        
        Random r = new Random();
        
        while (!rIndex.isEmpty()){
            int index = rIndex.remove(r.nextInt(rIndex.size()));
            rInputs.add(inputs[index]);
            rTargets.add(targets[index]);
        }
        
        rInputs.toArray(inputs);
        rTargets.toArray(targets);
    }
}
