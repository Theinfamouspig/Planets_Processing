package ak.planets.main;

import ak.planets.background.BackgroundOld;
import ak.planets.calculation.Point2i;
import ak.planets.logger.Logger;
import ak.planets.render.RenderQueue;
import ak.planets.render.Renderable;
import ak.planets.building.Connection;
import ak.planets.building.Connector;
import ak.planets.building.Node;
import ak.planets.camera.Camera;
import ak.planets.ui.TileOverlay;
import ak.planets.ui.clickable.ButtonAction;
import ak.planets.ui.clickable.UIButton;
import ak.planets.ui.clickable.UIContainer;
import ak.planets.ui.placement.NodeShadow;
import ak.planets.ui.placement.PlaceUtil;
import processing.core.PApplet;
import processing.event.KeyEvent;
import processing.event.MouseEvent;
import sun.rmi.runtime.Log;

import java.awt.*;
import java.io.IOException;
import java.util.stream.Stream;

import static ak.planets.main.Display.GameState.*;
import static ak.planets.logger.Logger.LogLevel.*;
/**
 * Created by Aleksander on 18/10/2015.
 */
public class Display extends PApplet {


    public static void main(String[] args) {
        PApplet.main(new String[]{"ak.planets.main.Display"});
    }

    enum GameState {
        MAIN_MENU,
        PLAYING,
        PAUSED,
    }

    private PlaceUtil placeUtil;

    private GameState gameState;

    private Map map;
    private RenderQueue queue;

    private Camera camera;
    private BackgroundOld background;

    private UIContainer container;


    private boolean shiftPressed;

    private TileOverlay tileOverlay;

    public void settings() {
        size(800, 600, P2D);
        smooth();
    }

    public void setup() {
        surface.setTitle(Reference.gameName);

        //This needs to work, anytime now would be good
        frame.setIconImage(Toolkit.getDefaultToolkit().getImage("res/texture/icon/planet.ico"));

        gameState = PLAYING;

        camera = new Camera(this);
        map = new Map(this);
        queue = new RenderQueue();

        //TODO: Make new layered background work
        background = new BackgroundOld(this, camera);


        noStroke();
        Logger.log(ALL, "Game Started");

        add(background);

        container = new UIContainer(this, 1, 1, "res/texture/ui/sideUI/button.png");

        PApplet main = this;

        try {
            container.addComponent(new UIButton("New     \n     Node", new ButtonAction() {
                @Override
                public void exectute() {
                    placeUtil = new NodeShadow(main);
                    updatePlaceUtil();
                }

                @Override
                public void onHover() {
                //TODO: POP UP BUBBLE
                }
            }, 90, 40));
            container.addComponent(new UIButton("Toggle     \n    Overlay", new ButtonAction() {
                @Override
                public void exectute() {
                    tileOverlay.toggleVisible();
                }

                @Override
                public void onHover() {

                }
            }, 90, 40));
        }catch (IOException e){
            e.printStackTrace();
        }

        container.setLayout(UIContainer.VERTICAL);

        tileOverlay = new TileOverlay(map,main,camera);

        textureMode(NORMAL);
    }

    public void draw() {

        if(gameState == MAIN_MENU){
            mainMenu();
        }

        if(gameState == PLAYING || gameState == PAUSED) {
            surface.setTitle(Reference.gameName + " : " + frameRate);
            background(0);
            camera.update();
            while (queue.hasNext())
                queue.next().render();
            queue.reset();

            //Render Static Components.
            if (placeUtil != null){
                placeUtil.render();
            }

            popMatrix();    //restore coord system

            if (tileOverlay.isVisible())
                tileOverlay.render();
            container.render();
        }
    }

    public void mainMenu(){
        background(0);
    }

    public void add(Renderable n) {
        n.setup();
        if (n instanceof Node){
           if (!map.add((Node) n)){
               Logger.log(ALL, "Node not added successfully!");
               return;
           }
        }
        queue.addAndSort(n);
    }
    public void delete(Node n){
        map.remove(n);
        queue.remove(n);
    }


    public void keyPressed(KeyEvent event) {
        Logger.log(DEBUG, "keyPressed " + event.getKeyCode());
        if(gameState == PLAYING){
            switch (event.getKeyCode()) {
                case 16:
                    shiftPressed = true;
                    break;
                case 88:
                    background.toggleHidden();
                    break;
                case 147:
                    Node del_node = map.search(camera.getRelativePosition(mouseX, mouseY), -1);
                    if (del_node != null)
                        delete(del_node);
                    else
                        Logger.log(ERROR, "delNode == null");
                    break;
            }
        } else if(gameState == MAIN_MENU){
            switch (event.getKeyCode()){
                case 10:
                    gameState = PLAYING;
            }
        }
    }

    public void keyReleased(KeyEvent event) {
        Logger.log(DEBUG, "keyReleased ", event.getKeyCode());
        switch (event.getKeyCode()) {
            case 16:
                shiftPressed = false;
                break;
        }
    }

    public void mouseWheel(MouseEvent event) {
        /*
        Node addSize = map.search(camera.getRelativePosition(mouseX, mouseY), 100);
        if(addSize != null)
            addSize.add();
        */

        //Scroll
        if (placeUtil != null) {
            placeUtil.scroll(event.getCount() * -1);
        }

        //Update camera zoom
        if(mouseButton == 37){
            camera.updateZoom(event.getCount() * 0.05);
        }
    }

    public void mousePressed(MouseEvent event) {
        Logger.log(DEBUG, "mousePressed" + event);
        if (gameState == PLAYING) {
            if (event.getButton() == 37){
                //Check if UI clicked
                if (container.checkClick(mouseX, mouseY))
                    return;

                //Check if 'shadow' placement clicked
                if (placeUtil != null){
                    placeUtil.onClick();
                    if (placeUtil.ready()){
                        Renderable r = placeUtil.fetchObject();
                        if (r instanceof Node){
                            add(r);
                        }else if (r instanceof Connection){
                            add(r);
                        }
                        if(!shiftPressed)
                            placeUtil = null;
                    }
                }
                //Left Click
                /*

                Point2i mouse = camera.getRelativePosition(mouseX, mouseY);
                Node closestNode = map.search(mouse, -1);
                Logger.log(DEBUG, closestNode + " = closestNode");

                if (closestNode != null) {
                    Connector c = closestNode.getClosestConnection(mouse);
                    Logger.log(ALL, "Getting connection at %s from %s. It is %s", mouse, closestNode, c);

                    if (c == null){
                        System.out.println(mouse);
                        return;
                    }

                    if ( connector_C == null) {
                        connector_C = c;
                        node_C = closestNode;

                    } else if ( connector_C != c) {
                        Connection connection = new Connection(this, connector_C, c);
                        if (!map.isConnectionIntersecting(connection.asLine())) {
                            node_C.connect(closestNode, connection);
                            add(connection);
                        }
                        connector_C = null;
                    }
                }
                */
            }else if (event.getButton() == 39){
                if (placeUtil != null)
                    placeUtil = null;

            }else if (event.getButton() == 3){
                //Middle Mouse
                camera.mousePressed(event);
            }


        } else if (gameState == MAIN_MENU) {

        }
    }

    public void mouseReleased(MouseEvent event) {
        if(gameState == PLAYING){
            camera.mouseReleased(event);
        }
    }


    public void mouseMoved(){
        //Needs to be called on every update to pass mouse coords into camera
        if(gameState == PLAYING){

            //Update camera?
            camera.mouseMoved(mouseX, mouseY);

            //Update 'shadow' for placement
            if (placeUtil != null) {
                updatePlaceUtil();
            }

        }
    }

    public void mouseDragged(){
        this.mouseMoved();
    }

    private void updatePlaceUtil(){
        if (placeUtil.shouldUpdate()) {
            Point2i point = camera.getRelativePosition(mouseX, mouseY);
            point = point.add(new Point2i(4 , 4));
            point = point.divide(8);
            point = point.multiply(8);
            placeUtil.updatePosition(point.getX(), point.getY());
        }
    }
}
