/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package neuralnetwork.ffnn;

import neuralnetwork.framework.Af;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import mylibray.Matrix;
import mylibray.XML;
import org.xml.sax.SAXException;

/**
 *
 * @author Thales
 */
public class NNMatrix implements NNFFInterface{

    public Matrix[] weights;
    public Matrix[] bias;
    
    private final int[] layers;
    private int dnaSize = 0;
    
    //creating gradients holders
    public NNMatrix(int... layers){
        this.layers = layers;
        weights = new Matrix[layers.length-1];
        bias = new Matrix[layers.length-1];
        for (int i = 0; i < weights.length; i++){
            weights[i] = new Matrix(layers[i+1],layers[i]);
            weights[i].randomize(-10,10);
            bias[i] = new Matrix(layers[i+1],1);
            bias[i].randomize(-10, 10);
            dnaSize += layers[i+1]*layers[i]+layers[i+1];
        }
        
    }
    
    @Override
    public void randomize(double min, double max){
        for (int i = 0; i < weights.length; i++){
            weights[i].randomize(min, max);
            bias[i].randomize(min, max);
        }
    }
    
    @Override
    public double[] feedForward(double... inputs){
        Matrix temp = new Matrix(inputs);
        for (int i = 0; i < weights.length; i++){
            temp = Matrix.product(weights[i], temp).add(bias[i]).map(Af::sigmoid);
        }
        return temp.colToArray(0);
    }
    
    @Override
    public void train(double[][] input_, double[][] target_, double lr){
        
        Matrix[] weightsGradientSum = new Matrix[weights.length];
        Matrix[] biasGradient = new Matrix[bias.length];
        Matrix[] activations = new Matrix[weights.length+1];
        
        for (int i = 0; i < weightsGradientSum.length; i++){
            weightsGradientSum[i] = new Matrix(weights[i].rows, weights[i].cols);
            biasGradient[i] = new Matrix(bias[i].rows, bias[i].cols);
        }
        
        //calc gradients
        for (int d = 0; d < input_.length; d++){

            //put target in a matrix "vectors"
            Matrix target = new Matrix(target_[0].length,1); target.arrayToCol(target_[d], 0);

            //feedforward and save activations
            activations[0] = new Matrix(input_[d]);
            for (int i = 0; i < weights.length; i++){
                activations[i+1] = Matrix.product(weights[i], activations[i]).add(bias[i]).map(Af::sigmoid);
            }
            
            //dA1
            Matrix error = Matrix.sub(activations[activations.length-1], target).mult(2);

            //time dZ1
            error.mult( Matrix.map(activations[activations.length-1], Af::dsigmoided));

            //dW1
            weightsGradientSum[weights.length-1].add(Matrix.product(error, Matrix.transpose(activations[activations.length-2])));
            biasGradient[biasGradient.length-1] = error;
            
            for (int i = weights.length-1; i > 0; i--){
                //dA
                error = Matrix.product(Matrix.transpose(weights[i]),error);
                //time dZ
                error.mult( Matrix.map(activations[i], Af::dsigmoided));
                //dW
                weightsGradientSum[i-1].add(Matrix.product(error, Matrix.transpose(activations[i-1])));
                biasGradient[i-1].add(error);
            }
        }
        
        //apply
        for (int i = 0; i < weightsGradientSum.length; i++){
            weights[i].sub(weightsGradientSum[i].mult(lr));
            bias[i].sub(biasGradient[i].mult(lr));
        }
        
    }

    @Override
    public int[] getLayersSize(){
        return layers;
    }
    
    @Override
    public double[] getDNA(){
        double[] dna = new double[dnaSize];
        int index = 0;
        for (Matrix b : bias) {
            double[] matrixDna = b.toArray();
            for (int i = 0; i < matrixDna.length; i++){
                dna[index] = matrixDna[i];
                index++;
            }
        }
        for (Matrix w : weights) {
            double[] matrixDna = w.toArray();
            for (int i = 0; i < matrixDna.length; i++){
                dna[index] = matrixDna[i];
                index++;
            }
        }
        return dna;
    }
    
    @Override
    public void fromDNA(double[] dna){
        int index = 0;
        for (Matrix b : bias) {
            double[] matrix = new double[b.cols * b.rows];
            for (int j = 0; j < matrix.length; j++){
                matrix[j] = dna[index];
                index++;
            }
            b.fromArray(matrix);
        }
        for (Matrix w : weights) {
            double[] matrix = new double[w.cols * w.rows];
            for (int j = 0; j < matrix.length; j++){
                matrix[j] = dna[index];
                index++;
            }
            w.fromArray(matrix);
        }
    }
    
    @Override
    public void save(String directory){
        XML xml = null;
        try {
            xml = new XML("NNMatrix");
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(NNMatrix.class.getName()).log(Level.SEVERE, null, ex);
        }
        xml.addAttribute("number_of_layers", Integer.toString(layers.length));
        for (int i = 0; i < layers.length; i++){
            xml.addAttribute("layer_"+i, Integer.toString(layers[i]));
        }
        xml.setData(getDNA());
        try {
            xml.save(directory);
        } catch (TransformerException ex) {
            Logger.getLogger(NNMatrix.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public NNMatrix load(String directory){
        XML xml = null;
        try {
            xml = XML.loadXML(directory);
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            Logger.getLogger(NNMatrix.class.getName()).log(Level.SEVERE, null, ex);
        }
        int number_of_layers = Integer.parseInt(xml.getAttribute("number_of_layers"));
        int[] layers = new int[number_of_layers];
        for (int i = 0; i < layers.length; i++){
            layers[i] = Integer.parseInt(xml.getAttribute("layer_"+i));
        }
        NNMatrix nn = new NNMatrix(layers);
        nn.fromDNA(xml.getData());
        return nn;
    }
    
    @Override
    public NNMatrix copy(){
        NNMatrix nn = new NNMatrix(layers);
        nn.fromDNA(getDNA());
        return nn;
    }

}
