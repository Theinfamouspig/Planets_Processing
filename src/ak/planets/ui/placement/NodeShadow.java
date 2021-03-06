package ak.planets.ui.placement;

import ak.planets.building.Node;
import ak.planets.calculation.Point2i;
import ak.planets.util.TextureUtil;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PImage;

/**
 * Created by Aleksander on 21/11/2015.
 */
public class NodeShadow extends PlaceUtil {

    private int[] model;
    private PImage texture;
    private double scale;

    public NodeShadow(PApplet main) {
        super(main);
        model = new int[]{
                -1, 1, 0, 1,
                1, 1, 1, 1,
                1, -1, 1, 0,
                -1, -1, 0, 0
        };
        this.texture = TextureUtil.getImage("res/texture/building/outline.png");

        scale = 24;
    }


    @Override
    protected void draw(){
        main.textureMode(PConstants.NORMAL);

        main.beginShape();
            main.texture(texture);
            main.tint(255, 140);
            for (int index = 0; index < model.length; )
                main.vertex((int) (x + model[index++] * scale), (int) (y + model[index++] * scale), model[index++], model[index++]);
            main.endShape();
        main.noFill();
        main.strokeWeight(1);
        main.tint(255, 255);
        main.fill(255, 0, 0);
        main.textAlign(PConstants.CENTER, PConstants.CENTER);
        main.text(String.format("%.0f", scale), x, y - 20);
        main.text("{" + x + "," + y + "}", x, y-10);
        main.noStroke();
    }

    @Override
    //Direction is always 1 or -1
    public void scroll(int direction){
        scale += direction*8;
    }

    @Override
    public void onClick(){
        r = new Node(main, new Point2i(x, y), scale);
    }

}
