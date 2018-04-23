/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package neuralnetwork.neat.neuralnetwork;

import neuralnetwork.neat.gene.Connection;

/**
 *
 * @author Thales
 */
public class Sinapse {
    
    private final Connection connection;
    public final Neuron in;
    public final Neuron out;
    
    private double activation;
    private double activation_td;
    
    public Sinapse(Connection connection, Neuron in, Neuron out){
        this.connection = connection;
        this.in = in;
        this.out = out;
        this.out.parents.add(this);
    }
    
    public double getInpulse(int requestId){
        if (connection.isRecurrent()){
            activation_td = activation;
            activation = in.getActivation(requestId);
            return activation_td*connection.getWeight();
        }
        return in.getActivation(requestId)*connection.getWeight();
    }
    
}
