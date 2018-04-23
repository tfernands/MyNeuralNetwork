/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package neuralnetwork.neat;

import java.util.ArrayList;

/**
 *
 * @author Thales
 */
public class Specie {
    
    int id;
    int age;
    boolean novel;
    int expectedOffspring;
    double averageFitness;
    double maxFitness;
    double maxFitnessEver;
    int ageOfLastImprovement;
    ArrayList<Organism> organisms;
    
    public Specie(int id){
        this.id = id;
        age = 1;
        novel = false;
        averageFitness = 0;
        maxFitness = 0;
        maxFitnessEver = 0;
        ageOfLastImprovement = 0;
        organisms = new ArrayList<>();
    }
    
    public void add(Organism organism){
        organisms.add(organism);
    }
    
    public void adjustFitness(){
        
    }
    
    
}
