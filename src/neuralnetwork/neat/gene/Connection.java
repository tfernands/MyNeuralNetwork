/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package neuralnetwork.neat.gene;

/**
 *
 * @author Thales
 */
public class Connection {

    public final int innovation;
    public final int in;
    public final int out;

    private double weight; 
    private boolean expressed;
    private boolean recurrent;

    protected Connection(int inovationNumber, int in, int out){
        this.innovation = inovationNumber;
        this.in = in;
        this.out = out;
        this.weight = 1;
        this.expressed = true;
        this.recurrent = false;
    }

    public double getWeight(){
        return weight;
    }
    public boolean getExpression(){
        return expressed;
    }
    public Connection setWeight(double weight){
        this.weight = weight;
        return this;
    }
    public Connection disable(){
        this.expressed = false;
        return this;
    }
    public Connection enable(){
        this.expressed = true;
        return this;
    }
    public boolean isRecurrent(){
        return recurrent;
    }
    public void recurrent(){
        recurrent = true;
    }
    public void normal(){
        recurrent = false;
    }

    public Connection copy(){
        Connection connectionCopy = new Connection(this.innovation, this.in, this.out);
        connectionCopy.weight = this.weight;
        connectionCopy.expressed = this.expressed;
        connectionCopy.recurrent = this.recurrent;
        return connectionCopy;
    }

    @Override
    public String toString(){
        String expressionString;
        if (expressed == true){
            expressionString =  " Enabled";
        }else{
            expressionString = "\033[0;31m" + "Disabled" + "\033[0m";
        }
        if (recurrent) expressionString += " RECURRENT";
        return "Connection: "+in+" -> "+out+", Weight: "+String.format("%2.2f", weight)+", Exp.: "+expressionString+", Innov: "+innovation;
    }
        
}
