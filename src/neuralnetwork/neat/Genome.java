/*

    OTIMIZAR FUNCAO "findCircular()"


 */
package neuralnetwork.neat;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import neuralnetwork.neat.gene.Connection;
import neuralnetwork.neat.gene.GeneFactory;
import neuralnetwork.neat.gene.Node;
import neuralnetwork.neat.neuralnetwork.Neuron;
import neuralnetwork.neat.neuralnetwork.Sinapse;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author Thales
 */
public final class Genome {
    
    //reference to the commun GeneFactory used in the population of genomes
    public  static final GeneFactory FACTORY = new GeneFactory();
    private static final Random RAND = new Random();
    
    public final int sensorsCount;
    public final int outputsCount;
    
    public final ArrayList<Node> nodes;
    public final ArrayList<Connection> connections;
    
    /**
     * Create a new Genome object and add the sensors and outputs nodes and make the connections between then
     * @param sensorsCount number of sensors neurons in the genome
     * @param outputsCount number of outputs neurons in the genome
     */
    public Genome(int sensorsCount, int outputsCount){
        this.sensorsCount = sensorsCount;
        this.outputsCount = outputsCount;
        this.nodes = new ArrayList<>(sensorsCount+outputsCount+1);
        this.connections = new ArrayList(sensorsCount*outputsCount);
        
        addNode(Node.BIAS);
        //adding sensor nodes
        for (int s = 0; s < sensorsCount; s++){
            addNode(Node.SENSOR);
        }
        
        //adding output nodes and making connections with random weights
        for (int o = sensorsCount+1; o <= outputsCount+sensorsCount; o++){
            addNode(Node.OUTPUT);
            for (int s = 1; s <= sensorsCount; s++){
                unsafeAddConnection(nodes.get(s).id, nodes.get(o).id).setWeight(RAND.nextFloat()*2-1);
            }
        }
    }
    private Genome(int sensorsCount, int outputsCount, int nodeSize, int connectionsSize){
        this.sensorsCount = sensorsCount;
        this.outputsCount = outputsCount;
        this.nodes = new ArrayList<>(nodeSize);
        this.connections = new ArrayList(connectionsSize);
    }
    
    public Network generateNetwork(){
        
        Neuron[] neurons = new Neuron[nodes.size()];
        for (int i = 0; i < nodes.size(); i++) {
            neurons[i] = new Neuron(nodes.get(i));
        }   
        
        Sinapse[] sinapses;
        int sinapsesCount = 0;
        for (int i = 0; i < connections.size(); i++) {
            if (connections.get(i).getExpression()) sinapsesCount++;
        }
        
        sinapses = new Sinapse[sinapsesCount];
        sinapsesCount = 0;
        for (int i = 0; i < connections.size(); i++) {
            Connection connection = connections.get(i);
            if (connection.getExpression()){
                sinapses[sinapsesCount] = new Sinapse(connection, neurons[connection.in], neurons[connection.out]);
                sinapsesCount++;
            }
        }
        
        return new Network(this, neurons, sinapses);
    }
    
    /**
     * Add a new node to this genome
     * @param type
     * @return
     */
    public Node addNode(byte type){
        Node node = FACTORY.getNode(nodes.size(),type);
        nodes.add(node);
        return node;
    }
    
    //may be slow
    /**
     * Check if aready exists a connection between the node id's
     * if a connection is found then this connection is returned
     * if not a new connection is created and added to genome and returned
     * @param in node id
     * @param out node id
     * @return a connection in the genome between the to nodes
     */
    public Connection addConnection(int in, int out){
        //check for connections aready present in the genome
        Connection connection = getConnection(in,out);
        if (connection != null){
            return connection;
        }
        connection = unsafeAddConnection(in, out);
        if (!connection.isRecurrent() && findCircular()) connection.recurrent();
        sortConnection();
        return connection;
    }
    //faster
    private Connection unsafeAddConnection(int in, int out){
        Connection connection = FACTORY.getConnection(in, out);
        connections.add(connection);
        return connection;
    }
    
    /**
     * return the connection between the nodes if it exists, if not the fucntion returns null
     * @param in node id
     * @param out node id
     * @return connection between the given nodes
     */
    public Connection getConnection(int in, int out){
        if (in > nodes.size() || out > nodes.size()) return null;
        for (Connection c : connections) {
            if (c.in == in && c.out == out) return c;
        }
        return null;
    }
    
    /**
     * Assing a new random weight value to a given connection
     * @param index
     * @param min min value generated
     * @param max max value generated
     */
    public void mutateRandomValue(int index, double min, double max){
        connections.get(index).setWeight(RAND.nextFloat()*(max-min)+min);
    }
    
    /**
     * Change a connection weight with in a giver range
     * @param index
     * @param range max range of change
     */
    public void mutateUniformePertubation(int index, double range){
        Connection connection = connections.get(index);
        connection.setWeight(connection.getWeight()+RAND.nextFloat()*(range*2)-range);
    }
    
    /**
     * Create a new connection between to existente nodes (ff configuration)
     * @return the new connection
     */
    public Connection mutateConnection(){
        ArrayList<Integer[]> availables = availableConnections();
        if (availables.isEmpty()){
            System.out.println("can't create a new connection, no available possibility for connection found");
            return null;
        }
        Integer[] newConnectionNodes = availables.get(RAND.nextInt(availables.size()));
        Connection newConnection = addConnection(newConnectionNodes[0],newConnectionNodes[1]).setWeight(RAND.nextFloat()*2-1).enable();
        if (newConnectionNodes[2] == 1) newConnection.recurrent();
        return newConnection;
    }
    
    /**
     * Create a new node between a connection end to new connections to link this node the old connection is disabled
     * @return the new node in the genome
     */
    public Node mutateNode(){
        //get a random connection
        int index = RAND.nextInt(connections.size());
        Connection connection = connections.get(index);
        
        for (int i = 0; i < connections.size(); i++){
            if (index < connections.size()-1) index++;
            else index = 0;
            connection = connections.get(index);
            if (connection.getExpression() && !connection.isRecurrent())
                break;
        }
        
        if (!connection.getExpression()){
            System.out.println("unable to found a enabled connection to add a node");
            return null;
        }
        
        //disable it
        connection.disable();
        //make a new node
        Node newNode = addNode(Node.HIDDEN);
        //create a new connection between the old connection in node, and the new node with the same weight as the old connection
        unsafeAddConnection(connection.in, newNode.id).setWeight(1);
        //create a new connection between the new node and the old connection out node with the weight set to 1
        unsafeAddConnection(newNode.id, connection.out).setWeight(connection.getWeight());
        sortConnection();
        return newNode;
    }
    
    /**
     * Crossover to genomes and generate a child
     * @param best bestfit parent
     * @param other other parent
     * @return genome child
     */
    public static Genome crossover(Genome best, Genome other){
        
        final float disableProbability = 0.75f; 
        
        //copy nodes of the best fit parent
        Genome child = new Genome(best.sensorsCount, best.outputsCount, best.nodes.size(), best.connections.size());
        for (Node n: best.nodes){
            child.nodes.add(n);
        }
        
        //crossover connections
        for (Connection b : best.connections) {
            Connection o = other.getConnection(b.in, b.out);
            Connection c;
            if (o == null){
                c = b.copy();
                if (!b.getExpression() && RAND.nextFloat() < disableProbability)
                    c.disable();
                else
                    c.enable();
            }
            else{
                if (RAND.nextBoolean())
                    c = b.copy();
                else
                    c = o.copy();
                
                if (!b.getExpression() || !o.getExpression()){
                    if (RAND.nextFloat() < disableProbability)
                        c.disable();
                    else
                        c.enable();
                }
            }
            child.connections.add(c);
        }
        return child;
    }
    
    /**
     * Mesure the compatibility of two genomes for speciation
     * @param a Genome a
     * @param b Genome b
     * @param c1 constant
     * @param c2 constant
     * @param c3 constant
     * @return compatibility distance
     */
    public static double compatibilityDistance(Genome a, Genome b, double c1, double c2, double c3){
        double[] temp = getEDW(a,b);
        double D = temp[0];//disjoint
        double E = temp[1];//excess
        double W = temp[2];//Averate weight differences
        double N = Math.max(a.connections.size(), b.connections.size()); if (N < 20) N = 1;
        return (c1*E+c2*D)/N + c3*W;
    }

    /**
     * Calc the number of excess, disjoint, and match genes weights diference between two genomes
     * @param a Genome
     * @param b Genome
     * @return
     * <ul>
     *  <li>[0] number of excess genes</li>
     *  <li>[1] number of disjoint genes</li>
     *  <li>[2] match genes weights diference sum</li>
     * </ul>
     */
    private static double[] getEDW(Genome a, Genome b){
        double[] EDW = new double[3];
            
        int aLastInnovation = a.connections.get(a.connections.size()-1).innovation;
        int bLastInnovation = b.connections.get(b.connections.size()-1).innovation;
        
        int largerInnovationNumber = Math.max(aLastInnovation, bLastInnovation);
        
        int i = 0; //innovation
        int ai = 0; //index a
        int bi = 0; //index b

        while (i <= largerInnovationNumber){
            i++;
            //update index
            while(a.connections.get(ai).innovation < i && ai < a.connections.size()-1){
                ai++;
            }
            while(b.connections.get(bi).innovation < i && bi < b.connections.size()-1){
                bi++;
            }
            
            //check
            if (a.connections.get(ai).innovation == i && b.connections.get(bi).innovation == i){
                EDW[2] += Math.abs(a.connections.get(ai).getWeight() - b.connections.get(bi).getWeight());
            }else
            if (a.connections.get(ai).innovation == i || b.connections.get(bi).innovation == i){
                
                boolean c1 = (bLastInnovation > i && b.connections.get(bi).innovation != i);
                boolean c2 = (aLastInnovation > i && a.connections.get(ai).innovation != i);

                if ( c1 || c2 ){
                    EDW[1]++;
                }else if (aLastInnovation <= i){
                    EDW[0] = b.connections.size() - bi;
                    break;
                }else if (bLastInnovation <= i){
                    EDW[0] = a.connections.size() - ai;
                    break;
                }
            }
        } 
        return EDW;
    }
    
    /**
     * Return a copy of this genome object
     * @return
     */
    public Genome copy(){
        Genome genomeCopy = new Genome(sensorsCount, outputsCount, nodes.size(), connections.size());
        for (Node n: nodes){
            genomeCopy.nodes.add(n);
        }
        for (Connection n: connections){
            genomeCopy.connections.add(n.copy());
        }
        return genomeCopy;
    }
    
    public void save(String directory){
        try {
            //new document
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.newDocument();
            
            // root elements
            Element rootElement = doc.createElement("Genome");
            rootElement.setAttribute("nodes", Integer.toString(nodes.size()));
            rootElement.setAttribute("connections", Integer.toString(connections.size()));
            doc.appendChild(rootElement);
            
            for (Node n : nodes){
                Element node = doc.createElement("node");
                node.setAttribute("id",Integer.toString(n.id));
                node.setAttribute("type",Byte.toString(n.type));
                rootElement.appendChild(node);
            }
            
            for (Connection c : connections){
                Element connection = doc.createElement("connection");
                connection.setAttribute("in",Integer.toString(c.in));
                connection.setAttribute("out",Integer.toString(c.out));
                connection.setAttribute("weight",Double.toString(c.getWeight()));
                connection.setAttribute("expression",Boolean.toString(c.getExpression()));
                connection.setAttribute("innovation",Integer.toString(c.innovation));
                rootElement.appendChild(connection);
            }
            
            // write the content into xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(directory));

            transformer.transform(source, result);
        
        } catch (ParserConfigurationException | TransformerException pce) {
	}
    }

    @Override
    public String toString(){
        String string = "    Nodes:\n";
        if (nodes.size() > 0){
            String l1 = "        | ";
            String l2 = "        | ";
            for (Node n: nodes){
                l1 += "Id: "+String.format("%2.0f",(double)n.id)+" | ";
                l2 += n.getType()+" | ";
            }
            string += l1+"\n";
            string += l2+"\n";
        }
        string += "\n    Connections:\n";
        if (connections.size() > 0){
            String l1 = "        | ";
            String l2 = "        | ";
            String l3 = "        | ";
            String l4 = "        | ";
            String l5 = "        | ";
            for (Connection c: connections){
                
                if (c.isRecurrent()){
                    l1 += "In: "+String.format("%6.0f",(float)c.in)+"(r)"+" | ";
                    l2 += "Out:"+String.format("%6.0f",(float)c.out)+"(r)"+" | ";
                }else{
                    l1 += "In:"+String.format("%10.0f",(float)c.in)+" | ";
                    l2 += "Out:"+String.format("%9.0f",(float)c.out)+" | ";
                }
                l3 += "Weight:"+String.format("%6.2f", c.getWeight())+" | ";
                l4 += "Exp.:"+expressionToString(c)+" | ";
                l5 += "Innov: "+String.format("%6.0f",(float)c.innovation)+" | ";
            }
            string += l1+"\n";
            string += l2+"\n";
            string += l3+"\n";
            string += l4+"\n";
            string += l5+"\n";
        }
        return string;
    }
    private String expressionToString(Connection c){
        if (c.getExpression() == true){
            return " Enabled";
        }else{
            return "\033[0;31m" + "Disabled" + "\033[0m";
        }
    }
    
    /**
     * Sort the connections list based on innovation number
     */
    private void sortConnection(){
        connections.sort((Connection a, Connection b) -> {
            if (a.innovation > b.innovation) return 1;
            if (a.innovation < b.innovation) return -1;
            return 0;
        });
    }
    
    /**
     * A list of Integer arrays with the index of new possibles nodes connetions and the type of connection
     * 
     * @return
     * <ul>
     *  <li>[0] in node index</li>
     *  <li>[1] out node index</li>
     *  <li>[2] 0 -> normal -> 1 recurrent</li>
     * </ul>
     */
    public ArrayList<Integer[]> availableConnections(){
        ArrayList<Integer[]> availables = new ArrayList<>(possibleFFCombinationsCount());

        //get sensors/bias -> all-sensors connections
        for (int s = 0; s <= sensorsCount; s++){
            for (int t = sensorsCount+1; t < nodes.size(); t++){
                Node ns = nodes.get(s);
                Node nt = nodes.get(t);
                Connection c = getConnection(ns.id,nt.id);
                if (c == null || !c.getExpression()) availables.add(new Integer[]{ns.id,nt.id,0});
            }
        }

//        //get hidden -> hidden
        int hiddenStartIndex = sensorsCount+outputsCount+1;        
//        
//        for (int h0 = hiddenStartIndex; h0 < nodes.size(); h0++){
//            for (int h1 = hiddenStartIndex; h1 < nodes.size(); h1++){
//                int recurrent = 0;
//                Node nh0 = nodes.get(h0);
//                Node nh1 = nodes.get(h1);
//                Connection c = getConnection(nh0.id,nh1.id);
//                Connection cb = getConnection(nh1.id,nh0.id);
//                if (cb != null && !cb.isRecurrent()|| h0 == h1) recurrent = 1;
//                if (c == null  || !c.getExpression()){
//                    availables.add(new Integer[]{nh0.id,nh1.id,recurrent});
//                }
//            }
//        }

        //get hidden -> output
        for (int h = hiddenStartIndex; h < nodes.size(); h++){
            for (int o = sensorsCount+1; o < hiddenStartIndex; o++){
                Node no = nodes.get(o);
                Node nh = nodes.get(h);
                Connection c = getConnection(nh.id,no.id);
                if (c == null  || !c.getExpression()) availables.add(new Integer[]{nh.id,no.id,0});
            }
        }
        
//        //get output -> hidden
//        for (int o = sensorsCount+1; o < hiddenStartIndex; o++){
//            for (int h = hiddenStartIndex; h < nodes.size(); h++){
//                Node no = nodes.get(o);
//                Node nh = nodes.get(h);
//                Connection c = getConnection(no.id,nh.id);
//                if (c == null  || !c.getExpression()) availables.add(new Integer[]{no.id,nh.id,1});
//            }
//        }
//        
//        //get output -> output
//        for (int o = sensorsCount+1; o < hiddenStartIndex; o++){
//            Node no = nodes.get(o);
//            Connection c = getConnection(no.id,no.id);
//            if (c == null  || !c.getExpression()) availables.add(new Integer[]{no.id,no.id,1});
//        }
        
        return availables;
    }
    
    private int possibleFFCombinationsCount(){
        int hiddens = nodes.size()-sensorsCount-outputsCount-1;
        int sensorToOthers = (sensorsCount+1)*(hiddens+outputsCount);
        int hiddenInnerConnections = (hiddens*hiddens);
        int outputToHidden = (outputsCount*hiddens);
        return sensorToOthers+hiddenInnerConnections+outputToHidden-connections.size()+1;
    }
    
    
    public boolean findCircular(){
        for (int i = 0; i < sensorsCount; i++){
            if (loop(nodes.get(i).id,new int[]{nodes.get(i).id})[0] == -1) return true;
        }
        return false;
    }
    private int[] loop(int index, int[] path){
        for (int i = 0; i < connections.size(); i++){
            Connection c = connections.get(i);
            if (c.in == path[path.length-1] && c.getExpression() && !c.isRecurrent()){
                
                int[] newPath = new int[path.length+1];
                
                System.arraycopy(path, 0, newPath, 0, path.length);
                newPath[path.length] = c.out;

                boolean stuck = false;
                for (int j = 0; j < path.length; j++){
                    if (c.out == path[j]){
                        stuck = true;
                        break;
                    }
                }

                if (stuck){
                    return new int[] {-1};
                }
                newPath = loop(index, newPath);
                if (newPath[0] == -1) return newPath;
                if (index == newPath[newPath.length-1]) return newPath;      
                
            }
        }
        return path;
    }
}