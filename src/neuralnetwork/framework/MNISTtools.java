/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package neuralnetwork.framework;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import mylibray.PCanvas;
import processing.core.PGraphics;
import processing.core.PImage;

/**
 *
 * @author Thales
 */
public class MNISTtools {
    
    public static byte[] loadRawData(String directory){
        byte[] data = null;
        try {
            data = Files.readAllBytes(Paths.get(directory));
        } catch (IOException ex) {
            Logger.getLogger(MNISTtools.class.getName()).log(Level.SEVERE, null, ex);
        }
        return data;
    }
    
    public static int byteArrayToInt(byte[] data, int firstByteIndex){
        return  (data[firstByteIndex] << 24) | (data[firstByteIndex+1] & 0xff) << 16 | (data[firstByteIndex+2] & 0xff) <<  8 | (data[firstByteIndex+3] & 0xff);
    }
    
    public static byte[] intToByteArray(int integer){
        byte[] bytes = new byte[4];
        bytes[0] = (byte)(integer >> 24);
        bytes[1] = (byte)(integer >> 16);
        bytes[2] = (byte)(integer >> 8);
        bytes[3] = (byte)(integer);
        return bytes;
    }
    
    public static int unsignedByte(byte b){
        return b & 0xFF;
    }
    
    public static void printByteArray(byte[] data){
        for (int i = 0; i < data.length; i++){
            System.out.println("["+i+"] "+unsignedByte(data[i]));
        }
    }
    
    public static double[] toDouble(PImage img){
        double[] doubleArray = new double[img.pixels.length];
        for (int i = 0; i < doubleArray.length; i++){
            doubleArray[i] = F.map(F.getRed(img.pixels[i]), 0, 255, 0, 1);
        }
        return doubleArray;
    }

    public static double[] toDouble(byte[] array){
        double[] doubleArray = new double[array.length];
        for (int i = 0; i < doubleArray.length; i++){
            doubleArray[i] = F.map(unsignedByte(array[i]), 0, 255, 0, 1);
        }
        return doubleArray;
    }
    
    public static double[][] toDouble(byte[][] array){
        double[][] doubleArray = new double[array.length][];
        for (int i = 0; i < array.length; i++){
            doubleArray[i] = toDouble(array[i]);
        }
        return doubleArray;
    }
    
    public static double[][] generateTargets(byte[] labels){
        double[][] targets = new double[labels.length][];

        ArrayList<Byte> types = new ArrayList<>();
        for (int i = 0; i < labels.length; i++){
            if (!types.contains(labels[i])) types.add(labels[i]);
        }
        for (int i = 0; i < labels.length; i++){
            double[] target = new double[types.size()];
            for (int j = 0; j < target.length; j++){
                if (labels[i] == j) target[j] = 1;
                else target[j] = 0;
            }
            targets[i] = target;
        }
        return targets;
    }
    
    //=============== MNIST_IMAGE ================
    
    public static class MNISTImage{
        
        public int width;
        public int height;
        public byte[] pixels;
        public byte label;
        
        public MNISTImage(int width, int height, byte[] pixels, byte label){
            this.width = width;
            this.height = height;
            this.pixels = pixels;
            this.label = label;
        }
    }
    
    //=============== VISUALIZER ================
    
    public static class Visualizer{
        
        public MNISTImage[] images;

        public Visualizer(MNISTImage[] images){
            this.images = images;
        }

        public PImage getImage(int index){
            PImage img = new PImage(images[index].width,images[index].height);
            for (int i = 0; i < img.pixels.length; i++){
                int c = unsignedByte(images[index].pixels[i]);
                img.pixels[i] = F.toColor(c);
            }
            return img;
        }

    }
    
    //=============== DECODER ================
    
    public static class Decoder{
             
        public static byte[][] imageData(byte[] data){
            int sizeX = byteArrayToInt(data,8);
            int sizeY = byteArrayToInt(data,12);
            byte[][] images = new byte[byteArrayToInt(data,4)][sizeX*sizeY];
            for (int i = 0; i < images.length; i++){
                images[i] = getImageInData(data,sizeX,sizeY,16+i*sizeX*sizeY);
            }
            return images;
        }
        
        public static byte[] labelData(byte[] data){
            byte[] labels = new byte[byteArrayToInt(data,4)];
            for (int i = 0; i < labels.length; i++){
                labels[i] = data[8+i];
            }
            return labels;
        }
        
        public static MNISTImage[] getMNISTImages(byte[] RawImageData, byte[] RawLabelData){
            int sizeX = byteArrayToInt(RawImageData,8);
            int sizeY = byteArrayToInt(RawImageData,12);
            MNISTImage[] images = new MNISTImage[byteArrayToInt(RawImageData,4)];
            for (int i = 0; i < images.length; i++){
                images[i] = new MNISTImage(sizeX,sizeY,getImageInData(RawImageData,sizeX,sizeY,16+i*sizeX*sizeY),RawLabelData[8+i]);
            }
            return images;
        }

        private static byte[] getImageInData(byte[] data, int sizeX, int sizeY, int offset){
            byte[] image = new byte[sizeX*sizeY];
            for (int i = 0; i < image.length; i++){
                image[i] = data[offset+i];
            }
            return image;
        }
    }
    
    //=============== IMAGE GENERATO ================
    
    public static abstract class ImageGenerator{

        private final byte[] rawImagesData;
        private final byte[] rawLabelsData;
        private final int width;
        private final int height;

        public ImageGenerator(int width, int height, int count){
           this.width = width;
           this.height = height;
           rawImagesData = new byte[count*width*height];
           rawLabelsData = new byte[count];
        }

        public abstract byte generateImageAndReturnLabel(PGraphics canvas);

        public void save(String directory){
            PGraphics canvas = PCanvas.applet.createGraphics(width, height);
            canvas.smooth(16);
            int imageIndex = 0;
            for (int i = 0; i < rawLabelsData.length; i++){
                rawLabelsData[i] = generateImageAndReturnLabel(canvas);
                for (int j = 0; j < canvas.pixels.length; j++){
                    rawImagesData[imageIndex] = (byte)F.getRed(canvas.pixels[j]);
                    imageIndex++;
                }
            }
            PCanvas.applet.saveBytes(directory+"_image", toMNISTImageData(rawImagesData, width, height));
            PCanvas.applet.saveBytes(directory+"_label", toMNISTLabelData(rawLabelsData));
            System.out.println("saved");
        }

        public static byte[] toMNISTLabelData(byte[] rawLabelsData){
            byte[] data = new byte[rawLabelsData.length+8];
            byte[] magicNumber = MNISTtools.intToByteArray(2049);
            byte[] numberOfItens = MNISTtools.intToByteArray(rawLabelsData.length);
            int index = 0;
            for (int i = 0; i < 4; i++){
              data[index] = magicNumber[i];
              index++;
            }
            for (int i = 0; i < 4; i++){
              data[index] = numberOfItens[i];
              index++;
            }
            for (int i = 0; i < rawLabelsData.length; i++){
              data[index] = rawLabelsData[i];
              index++;
            }
            return data;
        }

        public static byte[] toMNISTImageData(byte[] rawImagesData, int width, int height){
            byte[] data = new byte[rawImagesData.length+16];
            byte[] magicNumber = MNISTtools.intToByteArray(2051);
            byte[] numberOfItens = MNISTtools.intToByteArray(rawImagesData.length/width/height);
            byte[] numberOfRows = MNISTtools.intToByteArray(width);
            byte[] numberOfCols = MNISTtools.intToByteArray(height);
            int index = 0;
            for (int i = 0; i < 4; i++){
              data[index] = magicNumber[i];
              index++;
            }
            for (int i = 0; i < 4; i++){
              data[index] = numberOfItens[i];
              index++;
            }
            for (int i = 0; i < 4; i++){
              data[index] = numberOfRows[i];
              index++;
            }
            for (int i = 0; i < 4; i++){
              data[index] = numberOfCols[i];
              index++;
            }

            for (int i = 0; i < rawImagesData.length; i++){
              data[index] = rawImagesData[i];
              index++;
            }
            return data;
        }
    }
    
    //parcial F class from mylibrary
    public static class F{
        public static double map(double value, double lr1, double hr1, double lr2, double hr2){
            return (value*(hr2-lr2)-lr1*hr2+hr1*lr2)/(hr1-lr1);
        }
        public static int getRed(int c){
            return (c >> 16) & 0xFF;
        }
        public static int toColor(int bw){
            if(bw>255)bw = 255;
            if(bw<0)bw = 0;
            return (255<<24|bw<<16|bw<<8|bw);
        }
    }
    
}
