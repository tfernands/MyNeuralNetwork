/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package neuralnetwork.neat.gene;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author Thales
 */
public class GeneFactory {
    
    //store last innovation number used 
    private int globalInnovation;

    private final ArrayList<Node> nodes; //store nodes
    private final Map<Integer,Integer> innovations; //store a map of <"inovation number hash map", "innovation number"> to compare
    
    /**
     * Make a new instance of GeneFactory
     */
    public GeneFactory(){
        globalInnovation = 1;
        nodes = new ArrayList<>();
        innovations = new LinkedHashMap<>();
    }
    
    /**
     * return a new connection between nodes with the respective innovation number
     * @param in node id
     * @param out node id
     * @return
     */
    public Connection getConnection(int in, int out){
        int inovationHashMap = inovationHashMap(in,out);
        Integer connectionInnovation = innovations.get(inovationHashMap);
        
        if (connectionInnovation == null){
            connectionInnovation = globalInnovation++;
            innovations.put(inovationHashMap, connectionInnovation);
        }
        return new Connection(connectionInnovation, in, out);
    }
    
    public Node getNode(int id, byte type){
        Node node;
        if (id < nodes.size()){
            node = nodes.get(id);
            if (node.type != type)
                System.out.println("NODE TYPE REQUESTED DIFFERE FROM THE TYPE EXISTENT FOR THE ID: "+id);
        }else{
            node = new Node(id, type);
            nodes.add(node);
        }
        return node;
    }
    
    @Override
    public String toString(){
        return "Higher innovation number: "+globalInnovation;
    }
    
    /**
     * <p>Implementation of Cantor algorym</p>
     * Return a hashmap to a innovation number given to connections nodes ids
     * @param a
     * @param b
     * @return
     */
    public static int inovationHashMap(int a, int b){
        if (a > -1 || b > -1) {
            return (int)(0.5 * (a + b) * (a + b + 1) + b);
        }
        return -1;
    }
}


