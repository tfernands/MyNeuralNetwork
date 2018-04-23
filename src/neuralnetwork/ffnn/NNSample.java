package neuralnetwork.ffnn;

import neuralnetwork.framework.Af;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import neuralnetwork.NNInterface;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public final class NNSample implements NNFFInterface, NNInterface{
    
    public final int total_neurons;
    private final int[] layers;
    public final int[][] neurons_index;
    
    public Neuron[][] neurons;
    public Sinapse[] sinapses;
    
    private int call = 0;
    private int backpropagationCallId = 0;
    public double pcost = 0;
    
    public NNSample(int... neurons_per_layer){
        this.layers = neurons_per_layer;

        //create neurons layers
        neurons = new Neuron[neurons_per_layer.length][ ];
        for (int i = 0; i < neurons_per_layer.length; i++){
            neurons[i] = new Neuron[neurons_per_layer[i]];
        }

        //create neurons
        int tempNeuronCount = 0;
        for (int l = 0; l < neurons.length; l++) {
            for (int i = 0; i < neurons[l].length; i++) {
                if (l == 0){
                    neurons[l][i] = new Neuron(tempNeuronCount, Neuron.SENSOR);
                }else
                if (l == neurons.length-1){
                    neurons[l][i] = new Neuron(tempNeuronCount, Neuron.OUTPUT);
                }else{
                    neurons[l][i] = new Neuron(tempNeuronCount, Neuron.HIDDEN);
                }
                tempNeuronCount++;
            }
        }
        total_neurons = tempNeuronCount;
        
        //create index
        neurons_index = new int[total_neurons][ ];
        for (int l = 0; l < neurons.length; l++) {
            for (int i = 0; i < neurons[l].length; i++) {
                neurons_index[neurons[l][i].id] = new int[]{l,i};
            }
        }
        
        //GENERATE SINAPSES
        generateSinapses();
    }
    
    @Override
    public void randomize(double min, double max){
        for (Neuron[] neuronLayer : neurons) {
            for (Neuron neuron : neuronLayer) {
                neuron.bias = Math.random()*(max-min)+min;
            }
        }
        for (Sinapse sinapse : sinapses) {
            sinapse.weight = Math.random()*(max-min)+min;
        }
    }

    @Override
    public double[] feedForward(double... inputs){
        //inputs_neurons bias is the input value
        call++;
        for (int i = 0; i < neurons[0].length; i++){
            neurons[0][i].input(inputs[i]);
        }
        
        //create a array of outputs
        double[] output = new double[neurons[neurons.length-1].length];
        for (int i = 0; i < output.length; i++){
            output[i] = neurons[neurons.length-1][i].activation(call);
        }
        return output;
    }
    
    @Override
    public int[] getLayersSize(){
        return layers;
    }
    
    @Override
    public void train(double[][] inputs, double[][] targets, double lr){
        applyGradient(getGradient(inputs,targets),lr);
    }
      
    public double[] getGradient(double[][] inputs, double[][] targets){
        //initialize gradient array
        double[] gradient;
        int count = 0;
        for (int i = 0; i < neurons.length; i++){
            count += neurons[i].length;
            if (i < neurons.length-1)
                count += neurons[i].length*neurons[i+1].length;
        }
        gradient = new double[count];
        
        pcost = 0;
        //create gradiente descente
        for (int d = 0; d < inputs.length; d++){
            //set targets
            feedForward(inputs[d]);
            for (int i = 0; i < neurons[neurons.length-1].length; i++){
                neurons[neurons.length-1][i].setTarget(targets[d][i]);
                pcost+= Math.pow(neurons[neurons.length-1][i].activationCache-targets[d][i], 2);
            }
            
            //calc gradient
            count = 0;
            backpropagationCallId++;
            for (Neuron[] neuron1 : neurons) {
                for (Neuron neuron : neuron1) {
                    gradient[count] += neuron.getBiasCost(backpropagationCallId);
                    count++;
                    if (neuron.type == Neuron.OUTPUT) continue;
                    double[] w = neuron.getSinapsesCost(backpropagationCallId);
                    for (int j = 0; j < neuron.childsSinapses.length; j++){
                        gradient[count] += w[j];
                        count++;
                    }
                }
            }
        }
        pcost /= inputs.length;
        return gradient;
    }
    
    public void applyGradient(double[] gradient, double lr){
        int count = 0;
        for (Neuron[] neuron1 : neurons) {
            for (Neuron neuron : neuron1) {
                neuron.bias -= gradient[count]*lr;
                count++;
                if (neuron.childsSinapses == null) continue;
                for (Sinapse cSinapse : neuron.childsSinapses) {
                    cSinapse.weight -= gradient[count]*lr;
                    count++;
                }
            }
        }
    }
    
    public Neuron getNeuronById(int id){
        return neurons[neurons_index[id][0]][neurons_index[id][1]];
    }
    
    @Override
    public double[] getDNA(){
        double[] dna = new double[total_neurons+sinapses.length];
        int dnaIndex = 0;
        for (int l = 1; l < neurons.length; l++) {
            for (Neuron neuron : neurons[l]) {
                dna[dnaIndex] = neuron.bias;
                dnaIndex++;
            }
        }
        for (Sinapse sinapse : sinapses) {
            dna[dnaIndex] = sinapse.weight;
            dnaIndex++;
        }
        return dna;
    }
    
    @Override
    public void fromDNA(double[] dna){
        int dnaIndex = 0;
        for (int l = 1; l < neurons.length; l++) {
            for (Neuron neuron : neurons[l]) {
                neuron.bias = dna[dnaIndex];
                dnaIndex++;
            }
        }
        for (Sinapse sinapse : sinapses) {
            sinapse.weight = dna[dnaIndex];
            dnaIndex++;
        }
    }
    
    @Override
    public void save(String directory){
        try {
            //new document
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.newDocument();
            
            // root elements
            Element rootElement = doc.createElement("NeuralNetwork");
            rootElement.setAttribute("layers", Integer.toString(layers.length));
            for (int i = 0; i < layers.length; i++){
               rootElement.setAttribute("layer"+i, Integer.toString(layers[i]));
            }
            doc.appendChild(rootElement);
            
            double[] dna = getDNA();
            for (Double d : dna){
                Element codon = doc.createElement("codon");
                codon.setTextContent(Double.toHexString(d));
                rootElement.appendChild(codon);
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
    public NNSample load(String directory){
        File fXmlFile = new File(directory);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = null;
        try {
            dBuilder = dbFactory.newDocumentBuilder();
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(NNSample.class.getName()).log(Level.SEVERE, null, ex);
        }
        Document doc = null;
        try {
            doc = dBuilder.parse(fXmlFile);
        } catch (SAXException | IOException ex) {
            Logger.getLogger(NNSample.class.getName()).log(Level.SEVERE, null, ex);
        }
        doc.getDocumentElement().normalize();

        // root elements
        int[] layers;
        Element rootElement = doc.getDocumentElement();
        layers = new int[Integer.parseInt(rootElement.getAttribute("layers"))];
        for (int i = 0; i < layers.length; i++){
           layers[i] = Integer.parseInt(rootElement.getAttribute("layer"+i));
        }
        NNSample net = new NNSample(layers);
        NodeList codons = doc.getElementsByTagName("codon");
        double[] dna =  new double[codons.getLength()];
        for (int i = 0; i < dna.length; i++){
            dna[i] = Double.valueOf(codons.item(i).getTextContent());
        }
        net.fromDNA(dna);
        return net;
    }
    
    @Override
    public NNSample copy() {
        NNSample c = new NNSample(layers);
        c. neurons = neurons.clone();
        c. sinapses = sinapses.clone();
        return c;
    }
    
    @Override
    public String toString(){
        String string = "NNSample: layers: "+neurons.length+", neurons: "+total_neurons+", sinapses: "+sinapses.length;
        for (int i = 0; i < neurons.length; i++){
            string += "\n   Neuron layer["+i+"]";
            for (Neuron n: neurons[i]){
                string += "\n       ";
                string += n.toString();
            }
        }
        string += "\n   Sinapses";
        for (Sinapse s: sinapses){
            string += "\n       ";
            string += s.toString();
        }
        return string;
    }
    
    private void generateSinapses(){
        //initialize sinapses array
        int tempSinapsesCount = 0;
        for (int l = 0; l < neurons.length-1; l++){
            tempSinapsesCount += neurons[l].length*neurons[l+1].length;
        } 
        sinapses = new Sinapse[tempSinapsesCount];
        
        //reset variable for counting sinapses created
        tempSinapsesCount = 0;
        
        //create sisnapses and assing to neurons
        for (int l = 0; l < neurons.length-1; l++){
            for (int i = 0; i < neurons[l].length; i++){
                Neuron parent = neurons[l][i];
                parent.childsSinapses = new Sinapse[neurons[l+1].length];
                for (int c = 0; c < neurons[l+1].length; c++){
                    Neuron child = neurons[l+1][c];
                    if (child.parentsSinapses == null){
                        child.parentsSinapses = new Sinapse[neurons[l].length];
                    }
                    sinapses[tempSinapsesCount] = new Sinapse(parent,child);
                    parent.childsSinapses[c] = sinapses[tempSinapsesCount];
                    child.parentsSinapses[i] = sinapses[tempSinapsesCount];
                    tempSinapsesCount++;
                }
            }
        } 
    }
    
    public static class Neuron {
        //CONSTANTS
        public static final byte SENSOR = 0;
        public static final byte HIDDEN = 1;
        public static final byte OUTPUT = 2;

        public final int id;
        public final byte type;
        public double bias;
        public Sinapse[] parentsSinapses;
        public Sinapse[] childsSinapses;

        private int call;
        protected double activationCache;
        protected double zCache;

        protected int backpropCall;
        protected double activationCostCache;

        public Neuron(final int id, final byte type){
            this.id = id;
            this.type = type;
            this.bias = 0;
            this.call = 0;
            this.zCache = 0;
            this.activationCache = 0;
            this.backpropCall = 0;
            this.activationCostCache = 0;
        }

        public void input(double in){
            if (type == SENSOR){
                bias = in;
            }else{
                System.out.println("this neuron is not an input");
            }
        }
        public double activation(int callNumber){
            if (type == SENSOR) return bias;
            if (callNumber == call) return activationCache;
            call = callNumber;
            zCache = 0;
            //sum activations;
            if (parentsSinapses == null) return activationCache = 1;
            for (Sinapse parentsSinapse : parentsSinapses) {
                zCache += parentsSinapse.inpulse(callNumber);
            }
            //sum bias
            zCache += bias;
            activationCache = Af.sigmoid(zCache);
            return activationCache;
        }
        
        //backpropagation
        public void setTarget(double target){
            activationCostCache = 2*(activationCache-target);
        }

        public double getActivationCost(int callId){
            if (type == SENSOR) return 0;
            if (type == OUTPUT) return activationCostCache;
            if (backpropCall == callId) return activationCostCache;
            backpropCall = callId;
            activationCostCache = 0;
            for (Sinapse cSinapse : childsSinapses) {
                activationCostCache += cSinapse.weight * Af.dsigmoid(cSinapse.child.zCache) * cSinapse.child.getActivationCost(callId);
            }
            return activationCostCache;
        }

        public double[] getSinapsesCost(int callId){
            double[] Wcosts = new double[childsSinapses.length];
            for (int i = 0; i < Wcosts.length; i++) {
                Wcosts[i] = activationCache * Af.dsigmoid(childsSinapses[i].child.zCache) * childsSinapses[i].child.getActivationCost(callId);
            }
            return Wcosts;
        }

        public double getBiasCost(int callId){
            if (type == SENSOR) return 0;
            return Af.dsigmoid(zCache) * getActivationCost(callId);
        }

        @Override
        public String toString(){
            String t = null;
            switch (type) {
                case SENSOR:
                    t = "SENSOR";
                    break;
                case HIDDEN:
                    t = "HIDDEN";
                    break;
                case OUTPUT:
                    t = "OUTPUT";
                    break;
                default:
                    break;
            }

            return "Neuron "+id+" "+t+" - bias: "+bias;
        }
    }
    public static class Sinapse{

        public Neuron parent;
        public Neuron child;
        public double weight;

        public Sinapse(Neuron parent, Neuron child){
            this.parent = parent;
            this.child = child;
        }

        public double inpulse(int callNumber){
            return parent.activation(callNumber)*weight;
        }

        @Override
        public String toString(){
            return "Sinapse "+parent.id+" -> "+child.id+" weight: "+weight;
        }
    }
}