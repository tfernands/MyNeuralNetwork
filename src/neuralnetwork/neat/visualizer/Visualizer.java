/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package neuralnetwork.neat.visualizer;

import static mylibray.PCanvas.canvas;
import mylibray.PCamera;
import neuralnetwork.neat.Genome;
import java.util.ArrayList;
import mylibray.SBtn;
import functions.F;
import mylibray.Vector2D;

/**
 *
 * @author Thales
 */
public class Visualizer {

    PCamera camera;

    public Genome genome;

    private ArrayList<DNode> nodes;

    float x;
    float y;
    float sizeY;
    float sizeX;
    float size;
    float spacingY;

    public Visualizer(PCamera camera, Genome genome) {
        this.genome = genome;
        this.camera = camera;
        nodes = new ArrayList<>(genome.nodes.size());
        for (int i = 0; i < genome.nodes.size(); i++) {
            nodes.add(new DNode(genome.nodes.get(i).id));
        }
    }

    public void set(float x, float y, float sizeX, float sizeY, float size) {
        this.x = x;
        this.y = y;
        this.sizeY = sizeY;
        this.sizeX = sizeX;
        this.size = size;
        
        nodes.get(0).btn.set(x+sizeX/10, y, size);

        int maxLayerY = Math.max(genome.sensorsCount, genome.outputsCount);
        if (maxLayerY < 2) {
            maxLayerY = 2;
        }

        spacingY = (sizeY - size * maxLayerY) / (maxLayerY - 1);

        float posY;
        float posX;

        //Sensors
        posX = x;
        posY = y + (sizeY - size * genome.sensorsCount - spacingY * (genome.sensorsCount - 1)) / 2;
        for (int s = 1; s <= genome.sensorsCount; s++) {
            nodes.get(s).btn.set(posX, posY, size);
            posY += spacingY + size;
        }

        //Outputs
        posX = x + sizeX - size;
        posY = y + (sizeY - size * genome.outputsCount - spacingY * (genome.outputsCount - 1)) / 2;
        for (int o = genome.sensorsCount+1; o <= genome.sensorsCount + genome.outputsCount; o++) {
            nodes.get(o).btn.set(posX, posY, size);
            posY += spacingY + size;
        }

        //Hiddens
        for (int h = genome.sensorsCount + genome.outputsCount+1; h < genome.nodes.size(); h++) {
            nodes.get(h).btn.set(x + size + Math.random() * (sizeX - size * 2), y + Math.random() * sizeY, size);
            posY += spacingY + size;
        }
    }

    public void update() {

        //update topology
        if (genome.nodes.size() != nodes.size()) {
            for (int i = nodes.size(); i < genome.nodes.size(); i++) {
                DNode node = new DNode(genome.nodes.get(i).id);

                for (int j = 0; j < genome.connections.size(); j++) {
                    if (genome.connections.get(j).out == node.btn.id) {
                        DNode nodeIn = nodes.get(genome.connections.get(j).in);
                        node.btn.set(nodeIn.btn.pos.x, nodeIn.btn.pos.y, size);
                        break;
                    }
                }
                nodes.add(node);
            }
        }

        for (DNode node : nodes) {
            node.btn.update();
            if (node.btn.pressed) {
                node.applyForce(mouseDrag(node, 0.5));
                if (node.btn.id == 0){
                    node.btn.pos.set(camera.mouseX()-size/2, camera.mouseY()-size/2);
                }
            }
        }
    }

    public void relax(float intensity) {

        //Spring force
        applySpringForce(size * 4, 0.1);

        //distance force
        applyColisionForce(size * 3, 0.1);

        //apply forces
        for (DNode node : nodes) {
            node.updatePos();
        }

    }

    public void display(float spacing) {
        canvas.fill(0);
        canvas.stroke(0);
        canvas.strokeWeight((float) (0.5f / camera.getZoom()));

        spacing = (float) (spacing / camera.getZoom());
        float half_size = size / 2;

        for (int i = 0; i < genome.connections.size(); i++) {
            
            if (!genome.connections.get(i).getExpression()) {
                continue;
            }
            DNode s1 = nodes.get(genome.connections.get(i).in);
            DNode s2 = nodes.get(genome.connections.get(i).out);
            Vector2D vec = Vector2D.sub(s2.btn.pos, s1.btn.pos);
            Vector2D offset = vec.copy().setMag(spacing+size/2);
            Vector2D pos1 = s1.btn.pos.copy().add(half_size, half_size);
            Vector2D pos2 = s2.btn.pos.copy().add(half_size, half_size);
            if (genome.connections.get(i).isRecurrent()){
                canvas.stroke(0,150);
                //if circular recurency
                offset.rotate(0.3);
                
                Vector2D offset2;
                if (s1 == s2) {
                    offset.set(0,spacing+size/2).rotate(-1);
                    offset2 = offset.copy().rotate(0.8);

                    pos1.add(offset);
                    pos2.add(offset2);
                }else{
                    offset2 = offset.copy().mult(-1);
                    pos1.add(offset);
                    pos2.add(offset2);
                    offset.mult(vec.mag());
                    offset2.mult(vec.mag());
                }

                vec.set(offset2);
                offset.add(pos1);
                offset2.add(pos2);
                
                canvas.noFill();
                canvas.bezier((float)pos1.x, (float)pos1.y, (float)(offset.x), (float) (offset.y), (float) (offset2.x), (float) (offset2.y), (float) pos2.x, (float) pos2.y);
                canvas.fill(255);
                F.drawArrow((float)(vec.angleOrigin()), pos2, spacing);
            }else{
                canvas.stroke(0);
                pos1.add(offset);
                F.drawArrow((float)(pos1.x), (float)(pos1.y), (float)vec.mag()-spacing*2-size, (float)(-vec.angleOrigin()+Math.PI/2), (float)spacing);
            }
            canvas.ellipse((float) pos1.x, (float) pos1.y, spacing / 2, spacing / 2);
        }

        canvas.textAlign(canvas.CENTER, canvas.CENTER);
        for (DNode node : nodes) {

            if (node.btn.pressed) {
                canvas.fill(180);
            } else if (node.btn.mouseOver()) {
                canvas.fill(200);
            } else {
                canvas.fill(255);
            }
            node.btn.display();
            
            //ID
            String id = ""+node.btn.id;
            if (node.btn.id == 0) id = "B";
            canvas.fill(0);
            canvas.pushMatrix();
            canvas.resetMatrix();
            canvas.textSize((float) (size / 2 * camera.getZoom()));
            canvas.text(id, (float) camera.worldXToScreenX(node.btn.pos.x + size / 2), (float) camera.worldYToScreenY(node.btn.pos.y + size / 2));
            canvas.popMatrix();
        }
    }

    private void applySpringForce(double length, double k) {
        for (int i = 0; i < genome.connections.size(); i++) {
            if (!genome.connections.get(i).getExpression() || genome.connections.get(i).isRecurrent()) {
                continue;
            }
            DNode s1 = nodes.get(genome.connections.get(i).in);
            DNode s2 = nodes.get(genome.connections.get(i).out);

            if (s1 == s2) {
                continue;
            }

            Vector2D force = Vector2D.sub(s2.btn.pos, s1.btn.pos);
            double mag = force.mag();
            force.normalize();
            force.mult(length - mag);
            force.mult(k);
            s2.applyForce(force);
            s1.applyForce(force.mult(-1));
        }
    }

    private void applyColisionForce(double distance, double strength) {
        for (int a = 0; a < nodes.size(); a++) {
            for (int b = 0; b < nodes.size(); b++) {
                if (a != b) {
                    Vector2D force = Vector2D.sub(nodes.get(b).btn.pos, nodes.get(a).btn.pos);
                    double mag = force.mag();
                    force.normalize();
                    double value = distance - mag;
                    if (value < 0) {
                        continue;
                    }
                    force.mult(value * value);
                    force.mult(strength);
                    nodes.get(b).applyForce(force);
                }
            }
        }
    }

    private Vector2D mouseDrag(DNode n, double strength) {
        Vector2D mouse = new Vector2D(camera.mouseX() - size / 2, camera.mouseY() - size / 2);
        Vector2D force = Vector2D.sub(mouse, n.btn.pos);
        force.mult(strength);
        return force;
    }

    class DNode {

        public final SBtn btn;
        private final Vector2D acc;
        private final Vector2D vel;

        public DNode(int id) {
            btn = new SBtn(camera, id);
            vel = new Vector2D();
            acc = new Vector2D();
        }

        public void applyForce(Vector2D f) {
            acc.add(f);
        }

        public void updatePos() {
            if (btn.id <= genome.sensorsCount + genome.outputsCount) {
                return;
            }
            vel.add(acc);
            btn.pos.add(vel);
            acc.set(0, 0);
            vel.mult(0.7);
        }
    }
}
