/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package neuralnetwork.neat.neuralnetwork;

import java.util.ArrayList;
import neuralnetwork.framework.Af;
import neuralnetwork.neat.gene.Node;

/**
 *
 * @author Thales
 */
public class Neuron {

    public final Node node;
    public final ArrayList<Sinapse> parents;

    public double input;
    
    private int requestId;    //id of the request
    
    private double activationCache;    
    
    public Neuron(Node node) {
        this.node = node;
        if (node.type == Node.SENSOR || node.type == Node.BIAS)
            this.parents = null;
        else
            this.parents = new ArrayList<>();
        requestId = -1;
    }

    public void setInput(double input){
        if (node.type == Node.SENSOR){
            this.input = input;
        }else{
            try{
                throw new Exception("neuron node isn't a SENSOR");
            }catch (Exception ex){
            }
        }
    }
    
    public double getActivation(int requestId){
        if (node.type == Node.SENSOR) return input;
        if (node.type == Node.BIAS) return 1;
        if (this.requestId == requestId) return activationCache;
        else{
            this.requestId = requestId;
            double tempActivation = 0;
            
            for (Sinapse s: parents){
                tempActivation += s.getInpulse(requestId);
            }
            
            activationCache = Af.sigmoid(tempActivation);
            return activationCache;
        }
    }
    
    @Override
    public String toString(){
        String string = node.toString();
        string += " | last request id: "+requestId;
        if (node.type != Node.SENSOR){
            string += " | parents sinapses "+parents.size()+": ";
            for (int i = 0; i < parents.size(); i++){
                if (i > 0) string += ",";
                string += parents.get(i).in.node.id;
            }
        }
        return string;
    }
}
