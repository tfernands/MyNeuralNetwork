/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package neuralnetwork.ffnn;

import functions.F;
import geometry.Edge;
import mylibray.PCamera;
import static mylibray.PCanvas.applet;
import static mylibray.PCanvas.canvas;
import mylibray.SBtn;
import mylibray.Slider;

/**
 *
 * @author Thales
 */
public class NNSampleVisualizer extends SBtn{
    
    //referencia a rede neural
    private final NNSample net;
    //botoes para interação com os neuronios
    private final SBtn[][] btnNeurons;
    
    private final Edge[] lineSinapses;
    //variavel para salvar a ativacao dos neuronios
    private final double[][] tempActivation;
    //variavel para armazernar o tamanho da maior layer de neuronios para calculo das posições
    private int largestYArray;
    //variavel para armazernar o id selecionado
    private NNSample.Sinapse selectedSinapse;
    private NNSample.Neuron selectedNeuron;
    
    Slider slValue;
    
    public NNSampleVisualizer(NNSample net, PCamera camera) {
        super(camera);
        this.net = net;
        slValue = new Slider(camera);
        btnNeurons = new SBtn[net.neurons.length][ ];
        tempActivation = new double[net.neurons.length][ ];
        lineSinapses = new Edge[net.sinapses.length];
        for (int l = 0; l < net.neurons.length; l++) {
            btnNeurons[l] = new SBtn[net.neurons[l].length];
            tempActivation[l] = new double[net.neurons[l].length];
            if (net.neurons[l].length > largestYArray){
                largestYArray = net.neurons[l].length;
            }
            for (int i = 0; i < net.neurons[l].length; i++) {
                btnNeurons[l][i] = new SBtn(camera);
                btnNeurons[l][i].id = net.neurons[l][i].id;
            }
        }
        for (int s = 0; s < net.sinapses.length; s++) {
            lineSinapses[s] = new Edge(0,0,0,0);
        }
    }
    
    @Override
    public void set(double spacingX, double spacingY, double size) {
        this.size.x = net.neurons.length*spacingX-spacingX;
        this.size.y = largestYArray*spacingY-spacingY;
        for (int l = 0; l < net.neurons.length; l++) {
            double btnX = spacingX*l;
            for (int i = 0; i < net.neurons[l].length; i++) {
                double thisArraySize = net.neurons[l].length*spacingY-spacingY;
                double btnY = (this.size.y-thisArraySize)/2+spacingY*i;
                btnNeurons[l][i].set(btnX,btnY,size);
            }
        }
        
        //set sinapses
        for (int i = 0; i < net.sinapses.length; i++) {
            SBtn parent = getBtnById(net.sinapses[i].parent.id);
            SBtn child = getBtnById(net.sinapses[i].child.id);
            //pos
            double offsetY = parent.size.y/2;
            double parentOffsetX = parent.size.x;
            double childOffsetX = 0;
            lineSinapses[i].set(parent.pos.x+parentOffsetX, parent.pos.y+offsetY, child.pos.x+childOffsetX, child.pos.y+offsetY);
        }
    }
    
    @Override
    public void update(){
        updateBtn();
        for (int l = 0; l < net.neurons.length; l++) {
            for (int i = 0; i < net.neurons[l].length; i++) {
                tempActivation[l][i] = net.neurons[l][i].activationCache;
                btnNeurons[l][i].update();
            }
        }
        
        //SELECTION
        slValue.update();
        if (!slValue.mouseOver() && !slValue.pressed){
            if (onPress || !pressed && applet.mousePressed){
                selectedNeuron = null;
                selectedSinapse = null;
                slValue.values(-10, 10);
            }
            for (int l = 0; l < net.neurons.length; l++) {
                for (int i = 0; i < net.neurons[l].length; i++) {
                   if (btnNeurons[l][i].pressed){
                        selectedNeuron = net.neurons[l][i];
                        if (l == 0){
                            slValue.values(0, 1);
                        }
                        setSlider(selectedNeuron.bias,btnNeurons[l][i].pos.x+btnNeurons[l][i].size.x/2,btnNeurons[l][i].pos.y+btnNeurons[l][i].size.y/2);
                    } 
                }
            }
//            //find closes sinapse/ select sinapse
//            double dist = Double.POSITIVE_INFINITY;
//            if (onPress && selectedNeuron == null){
//                for (int i = 0; i < net.sinapses.length; i++) {
//                    Vector2D mouse = new Vector2D(camera.mouseX(),camera.mouseY());
//                    Vector2D d = lineSinapses[i].dist(mouse);
//                    double tempDist = Vector2D.dist(mouse, d);
//                    if (tempDist < dist){
//                        dist = tempDist;
//                        selectedSinapse = net.sinapses[i];
//                        setSlider(selectedSinapse.weight,(lineSinapses[i].p1.x+lineSinapses[i].p2.x)/2,(lineSinapses[i].p1.y+lineSinapses[i].p2.y)/2);
//                        slValue.values(-10, 10);
//                    }
//                }
//                if (dist > 5/camera.getZoom() && camera.mouseX() > 0){
//                    selectedSinapse = null;
//                    slValue.setValue(0);
//                }
//            }
        }
        if (selectedNeuron != null){
            selectedNeuron.bias = slValue.getValue();
        }else
        if (selectedSinapse != null){
            selectedSinapse.weight = slValue.getValue();
        }
    }
    private void setSlider(double value, double px, double py){
        double sizeX = 200/camera.getZoom();
        double sizeY = 15/camera.getZoom();
        slValue.setValue(value);
        slValue.set(px-sizeX/2, py-sizeY/2, sizeX,sizeY);
    }
    
    @Override
    public void display() {
        canvas.strokeWeight((float) (1/camera.getZoom()));
        for (int l = 0; l < net.neurons.length; l++) {
            for (int i = 0; i < net.neurons[l].length; i++) {
                canvas.fill((int)F.map(tempActivation[l][i],0,1,0,255));
                canvas.strokeWeight((float) (1/camera.getZoom()));
                if (btnNeurons[l][i].pressed){
                    btnNeurons[l][i].display();
                }else{
                    btnNeurons[l][i].feedBack();
                    btnNeurons[l][i].display();
                }
                if (net.neurons[l][i] == selectedNeuron){
                    canvas.strokeWeight((float) (2/camera.getZoom()));
                    btnNeurons[l][i].display();
                }
                
            }
        }
        if (selectedNeuron != null || selectedSinapse != null){
            canvas.strokeWeight((float) (1/camera.getZoom()));
            slValue.display();
            slValue.displayValues(255);
        }
    }
    
    public void displaySinapses(double weight){
        for (int i = 0; i < net.sinapses.length; i++) {
            double strokeWeight = Math.abs(net.sinapses[i].weight)*weight*getActivationById(net.sinapses[i].parent.id);
            if (selectedSinapse == net.sinapses[i]){
                canvas.stroke(255);
                strokeWeight = Math.abs(net.sinapses[i].weight)*weight;
                if (net.sinapses[i].weight > 0){
                    canvas.stroke(100,255,100);
                }else{
                    canvas.stroke(255,100,100);
                }
            }else
            if (net.sinapses[i].weight > 0){
                canvas.stroke(0,255,0);
            }else{
                canvas.stroke(255,0,0);
            }
            canvas.strokeWeight((float)(strokeWeight/camera.getZoom()));
            lineSinapses[i].display();
        }
    }
    
    /**
     *
     * @return
     */
    @Override
    public String toString(){
        String in = "";
        for (int i = 0; i < tempActivation[0].length; i++){
            in += "| "+String.format("%1.2f", tempActivation[0][i])+" ";
        }
        
        String out = "";
        for (int i = 0; i < tempActivation[tempActivation.length-1].length; i++){
            out += "| "+String.format("%1.2f", tempActivation[tempActivation.length-1][i])+" ";
        }
        return "Inputs: "+in.substring(1)+" output: "+out.substring(1);
    }
    
    private int[] getIndexById(int id){
        return net.neurons_index[id];
    }
    private SBtn getBtnById(int id){
        return btnNeurons[net.neurons_index[id][0]][net.neurons_index[id][1]];
    }
    private double getActivationById(int id){
        return tempActivation[net.neurons_index[id][0]][net.neurons_index[id][1]];
    }
}