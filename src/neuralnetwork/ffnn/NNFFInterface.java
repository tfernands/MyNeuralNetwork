/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package neuralnetwork.ffnn;

/**
 *
 * @author Thales
 */
public interface NNFFInterface {
    public void randomize(double min, double max);
    public double[] feedForward(double... inputs);
    public void train(double[][] inputs, double[][] targets, double lr);
    public int[] getLayersSize();
    public double[] getDNA();
    public void fromDNA(double[] dna);
    public void save(String directory);
    public abstract NNFFInterface load(String directory);
    public NNFFInterface copy();
}
