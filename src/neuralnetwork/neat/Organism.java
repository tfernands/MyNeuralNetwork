/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package neuralnetwork.neat;

/**
 *
 * @author Thales
 */
public class Organism {
    
    double fitness;
    
    //A fitness measure that won't change during adjustments
    double orig_fitness;
    
    //The Organism's genotype
    final Genome genome;
    
    //The Organism's phenotype
    final Network net;
    
    //Number of children this Organism may have
    double expected_offspring;
    
    //Tells which generation this Organism is from
    int generation;
    
    //Marker for destruction of inferior Organisms
    boolean eliminate;
    
    public Organism(Genome genome){
        this.genome = genome;
        net = genome.generateNetwork();
    }
    
    public Organism copy(){
        Organism newOrganism = new Organism(genome.copy());
        newOrganism.fitness = fitness;
        return newOrganism;
    }
    
}
