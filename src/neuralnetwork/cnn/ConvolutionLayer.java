/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package neuralnetwork.cnn;

import mylibray.Matrix;

/**
 *
 * @author Thales
 */
public class ConvolutionLayer {
    
    //working
    public static Image feed(double[] input, int width, int height, Matrix kernel){
        int kernelSize = kernel.cols*kernel.rows;
        int outputWidth = width-kernel.cols+1;
        int outputHeight = width-kernel.rows+1;
        
        double[] output = new double[outputWidth*outputHeight];
        for (int y = 0; y < outputWidth; y++){
            for (int x = 0; x < outputHeight; x++){
                int index = y*width+x;
                int outputIndex = y*outputWidth+x;
                for (int kx = 0; kx < kernel.cols; kx++){
                    for (int ky = 0; ky < kernel.rows; ky++){
                        int kindex = ky*width+kx;
                        output[outputIndex] += input[index+kindex]*kernel.matrix[ky][kx];
                    }
                }

            }
        }
        return new Image(output,outputWidth,outputHeight);
    }
    
    public static class Image{
        public int width;
        public int height;
        public double[] pixels;
        public Image(double[] pixels, int width, int height){
            this.pixels = pixels; this.width = width; this.height = height;
        }
    }
    
}
