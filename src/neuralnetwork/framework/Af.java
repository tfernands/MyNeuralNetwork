/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package neuralnetwork.framework;

/**
 *
 * @author Thales
 */
public class Af {
    /**
     * @param x
     * @return sigmoid of 'x'
     */
    public static double sigmoid(double x){
        return 1./(1+Math.exp(-x));
    }
    /**
     * @param x
     * @return derivate of sigmoid 'x'
     */
    public static double dsigmoid(double x){
        double y = sigmoid(x);
        return y * (1-y);
    }
    /**
     * @param x
     * @return derivate of a number aread sigmoided 'x'
     */
    public static double dsigmoided(double x){
        return x * (1-x);
    }
    
    public static double revertSigmoid(double x){
        return Math.log(x/(1-x));
    }
}
