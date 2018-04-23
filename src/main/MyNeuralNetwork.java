/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import functions.F;
import genetic_code.GeneticEngine;
import genetic_code.DNA;
import neuralnetwork.framework.DataSet;
import neuralnetwork.framework.MNISTtools;
import neuralnetwork.ffnn.NNFFInterface;

/**
 *
 * @author Thales
 */
public class MyNeuralNetwork {

    public static DataSet data;

    public static void main(String[] args) {
//        data = Data.MNISTData(
//            "D:/Projetos/Programação/JAVA/NeuralNetwork/train-images.idx3-ubyte",
//            "D:/Projetos/Programação/JAVA/NeuralNetwork/train-labels.idx1-ubyte"
//            //"D:\\Projetos\\Programação\\JAVA\\NeuralNetwork\\sqc_image",
//            //"D:\\Projetos\\Programação\\JAVA\\NeuralNetwork\\sqc_label"    
//        );
        Count cnt = new Count();
        Count cnt2 = null;
        while(true){
            if (Number.number == 3 && cnt2 == null) cnt2 = new Count();
            System.out.println(Number.number);
        }
        
    }
    
    public static class Number{
        public static int number = 0;
    }

    static class Data {

        public static DataSet MNISTData(String directoryImages, String directorylabels) {
            byte[] imagesRawData = MNISTtools.loadRawData(directoryImages);
            byte[] labelsRawData = MNISTtools.loadRawData(directorylabels);
            return new DataSet(
                    MNISTtools.toDouble(MNISTtools.Decoder.imageData(imagesRawData)),
                    MNISTtools.generateTargets(MNISTtools.Decoder.labelData(labelsRawData))
            );
        }

        public static DataSet xor() {
            double[][] inputs = new double[][]{
                {0, 0},
                {0, 1},
                {1, 0},
                {1, 1},};
            double[][] targets = new double[][]{
                {0},
                {1},
                {1},
                {0},};
            return new DataSet(inputs, targets);
        }

        public static int getCorrectGuesses(NNFFInterface nn, double[][] inputs, double[][] targets) {
            int acertos = 0;
            for (int i = 0; i < inputs.length; i++) {
                double[] output = nn.feedForward(inputs[i]);
                int outputIndex = -1;
                int targetIndex = -2;
                double confidence = Double.NEGATIVE_INFINITY;
                for (int j = 0; j < output.length; j++) {
                    if (output[j] > confidence) {
                        confidence = output[j];
                        outputIndex = j;
                    }
                    if (targets[i][j] == 1) {
                        targetIndex = j;
                    }
                }
                if (outputIndex == targetIndex) {
                    acertos++;
                }
            }
            return acertos;
        }

        public double[] getGuess(NNFFInterface nn, double[] input) {
            double[] guess = new double[2];
            double[] output = nn.feedForward(input);
            int outputIndex = -1;
            double confidence = Double.NEGATIVE_INFINITY;
            for (int j = 0; j < output.length; j++) {
                if (output[j] > confidence) {
                    confidence = output[j];
                    outputIndex = j;
                }
            }
            guess[0] = outputIndex;
            guess[1] = F.porcentage(output)[outputIndex];
            return guess;
        }
    }

    static class Evolve {

        public static NNFFInterface nn;

        static DataSet data;
        static GeneticEngine e;
        static double randomValues;

        public static void initalize(NNFFInterface net, DataSet dataset, int popCount, double randomValues_) {
            randomValues = randomValues_;
            nn = net;
            e = new GeneticEngine(nn.getDNA().length, popCount);
            e.randomize(-randomValues, randomValues);
            data = dataset;
        }

        public static void resume(NNFFInterface net) {
            nn = net;
            DNA[] pop = new DNA[e.pop.length];
            DNA dna = new DNA(nn.getDNA(), fitness(nn, data.inputs, data.targets));
            System.out.println(dna.fitness);
            for (int i = 0; i < pop.length; i++) {
                pop[i] = dna.copy();
            }
            e.setPopulation(pop);
        }

        public static void evolve(String saveDirectory, int maxGenerations) {
            while (true) {
                if (e.generation == maxGenerations) {
                    break;
                }

                int index = 0;
                data.nextBatch(2000, true);
                for (DNA pop : e.pop) {
                    //System.out.println(index);
                    pop.fitness = fitness(pop, data.getInputsBatch(), data.getTargetsBatch());
                    index++;
                }

                //e.elitism(2);
                e.selection(30);
                //e.addRandons(2, -5, 5);
                e.crossOver();
                e.mutate(0.01, -randomValues, randomValues);
                //e.evolveValue(0.005, 0.2);
                e.updatePop();

                nn.fromDNA(e.best.dna);
                nn.save(saveDirectory);

                System.out.println(e);

                if (e.generation % 1 == 0) {
                    System.out.print("checking guesses");
                    int corrects = Data.getCorrectGuesses(nn, data.inputs, data.targets);
                    System.out.println(" | correct guesses: " + corrects + " => " + F.map(corrects, 0, data.inputs.length, 0, 100) + "% | fitness: " + e.best.fitness);
                }
                System.out.println("");
            }
        }

        private static double fitness(DNA dna, double[][] inputs, double[][] targets) {
            nn.fromDNA(dna.dna);
            return fitness(nn, inputs, targets);
        }

        public static double fitness(NNFFInterface nn, double[][] inputs, double[][] targets) {
            double maxFitness = inputs.length * targets[0].length;
            double fitness = maxFitness;
            for (int i = 0; i < inputs.length; i++) {
                double[] output = nn.feedForward(inputs[i]);
                for (int j = 0; j < output.length; j++) {
                    fitness -= Math.abs(targets[i][j] - output[j]) * 2;
                }
            }
            return F.map(fitness, 0, maxFitness, 0, 100);
        }
    }
}
