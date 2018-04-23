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
public class Node {
    public static final byte BIAS = 0;
    public static final byte SENSOR = 1;
    public static final byte HIDDEN = 2;
    public static final byte OUTPUT = 3;
    
    public final int id;
    public final byte type;

    //allowed recurrent calls
    protected Node(int id, byte type){
        this.id = id;
        this.type = type;
    }

    @Override
    public String toString(){
        String string = "Node "+id+" ";
        string += getType();
        return string;
    }
    public String getType(){
        switch (type) {
            case BIAS:
                return " Bias ";
            case SENSOR:
                return "Sensor";
            case HIDDEN:
                return "Hidden";
            case OUTPUT:
                return "Output";
        }
        return null;
    }
}
