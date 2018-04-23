/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package neuralnetwork.neat;

import neuralnetwork.NNInterface;
import neuralnetwork.neat.gene.Connection;
import neuralnetwork.neat.neuralnetwork.Neuron;
import neuralnetwork.neat.neuralnetwork.Sinapse;

/**
 *
 * @author Thales
 */
public class Network implements NNInterface{

    public final Genome genome;

    public final Neuron[] neurons;
    public final Sinapse[] sinapses;
    
    private int requestIdCounter;
    
    public Network(Genome genome, Neuron[] neurons, Sinapse[] sinapses){
        this.genome = genome;
        this.neurons = neurons;
        this.sinapses = sinapses;
        requestIdCounter = 0;
    }

    @Override
    public double[] feedForward(double... inputs){
        for (int i = 0; i < inputs.length; i++){
            neurons[i+1].setInput(inputs[i]);
        }
        double[] output = new double[genome.outputsCount];
        for (int i = 0; i < output.length; i++){
            output[i] = neurons[genome.sensorsCount+1+i].getActivation(requestIdCounter++);
        }
        return output;
    }

}
