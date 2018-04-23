/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package neuralnetwork.neat;

import functions.F;
import java.util.ArrayList;
import java.util.Random;
import neuralnetwork.neat.Genome;
import neuralnetwork.neat.Organism;
import neuralnetwork.neat.Network;

/**
 *
 * @author Thales
 */
public abstract class Neat {
    public static final double C1 = 1.0f; //excees distance multiplier
    public static final double C2 = 1.0f; //disjoint distance multiplier
    public static final double C3 = 0.4f; //weights distance multiplier
    public static final double DISTANCE_THRESHOLD = 3.0f; //distance threshold
    public static final double WEIGHTS_MUTATION = 0.8f; //chande of weights mutation ocurrer
    public static final double RANDOM_MUTATION = 0.1f; //chance of a totaly new number being generated
    public static final double NEW_NODE_MUTATION = 0.03f; //chance of a new node appers
    public static final double NEW_LINK_MUTATION = 0.05f; //chance of a new link appers
    public static final double CROSSOVER = 0.75f; //crossover rate
    //public static final float INTERSPECIES_MATING = 0.001f; //crossover interspecies rate
    
    public static final double WEIGHTS_MUTATE_RANGE = 5;
    public static final double WEIGHTS_UNIFORM_POWER = 0.3f;
    public static final double WEIGHTS_UNIFORM_POWER = 0.3f;
  
    public abstract double evaluate(Network net);
    
}


    
//    public void sortMembers(){
//        members.sort((Organism a, Organism b) -> {
//            if (a.fitness < b.fitness) return 1;
//            if (a.fitness > b.fitness) return -1;
//            return 0;
//        });
//    }
