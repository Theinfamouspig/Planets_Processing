package ak.planets.building;

import ak.planets.calculation.Point2d;
import ak.planets.logger.Logger;
import ak.planets.render.Renderable;
import ak.planets.calculation.Point2i;
import com.sun.org.apache.xerces.internal.util.SynchronizedSymbolTable;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PImage;

import java.awt.geom.Line2D;
import java.util.Arrays;

/**
 * Created by Aleksander on 19/10/2015.
 */
public class Connection extends Renderable{

    //Constants


    public static int CONNECTION_NORMAL = 1;
    private Point2i point1, point2;
    private int type;
    private double length;
    private PImage texture;
    private PApplet main;
    private double[] model;
    private int width;

    public Connection(PApplet main, Point2i point1, Point2i point2) {

        this.point1 = point1;
        this.point2 = point2;

        this.renderPriority = 10;

        this.length = node1.getPoint().computeDistanceSquared(node2.getPoint());
        this.length = Math.sqrt(length);
        this.type = CONNECTION_NORMAL;
        this.main = main;
        this.width = 10;
    }

    /**
     * @return {@code Array} containing 2 {@code Connector}
     */

    public Node[] getNodes(){
        return new Node[]{node1, node2};
    }

    public boolean isIntersection(Line2D intersect){
        Line2D line1 = new Line2D.Double(connector1.getPoint().getX(), connector1.getPoint().getY(), connector2.getPoint().getX(), connector2.getPoint().getY());
        return line1.intersectsLine(intersect);
    }

    @Override
    public void setup() {
        //Replace with call to the not yet built util class.
        //This should be generated procedurally, based on the textures of the parents nodes.
        this.texture = main.loadImage("res/texture/connection/connection.png");
        Logger.log(Logger.LogLevel.DEBUG, "Texture size for connection is " + texture.width + " : " + texture.height);

        Point2i con1 = node1.getPoint();
        Point2i con2 = node2.getPoint();

        Point2d con1_V = new Point2d(con1);
        Point2d con2_V = new Point2d(con2);


        Point2d calcVector = con2_V.sub(con1_V);
        calcVector = calcVector.getPerpendicular();
        calcVector = calcVector.normalise();

        Point2d linearVector = calcVector.getPerpendicular();
        con1_V = con1_V.sub(linearVector.multiply(1));
        con2_V = con2_V.add(linearVector.multiply(1));

        calcVector = calcVector.multiply(width);

        Logger.log(Logger.LogLevel.DEBUG, "NormalisedVector to P1 and P2 is " + calcVector.toString());

        Point2d r1 = con1_V.add(calcVector);
        Point2d r2 = con2_V.add(calcVector);
        Point2d r3 = con2_V.sub(calcVector);
        Point2d r4 = con1_V.sub(calcVector);

        double xLength = Math.sqrt(con1.computeDistanceSquared(con2))/texture.width;

        model = new double[]{
                r1.getX(), r1.getY(), 0, 0,
                r2.getX(), r2.getY(), xLength, 0,
                r3.getX(), r3.getY(), xLength, 1,
                r4.getX(), r4.getY(), 0, 1,
        };
    }

    @Override
    public void render() {
        main.beginShape();
        main.textureWrap(PConstants.REPEAT);
        main.texture(texture);
        for (int index = 0; index < model.length; index+=4) {
            main.vertex((float) model[index],(float)  model[index+1],(float)  model[index+2],(float)  model[index+3]);
        }
        main.endShape(PConstants.CLOSE);
        main.textureWrap(PConstants.NORMAL);

        /*main.fill(main.color(0, 0, 255));
        main.beginShape();
            r1.getPoint().vertex(main);
            r2.getPoint().vertex(main);
            r3.getPoint().vertex(main);
            r4.getPoint().vertex(main);
        main.endShape();
        main.noFill();
        */
    }


    @Override
    public void update() {

    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ak.planets.building.Connection{");
        sb.append("connector1=").append(node1);
        sb.append(", connector2=").append(node2);
        sb.append(", length=").append(length);
        sb.append(", main=").append(main);
        sb.append(", model=").append(Arrays.toString(model));
        sb.append(", texture=").append(texture);
        sb.append(", type=").append(type);
        sb.append(", width=").append(width);
        sb.append('}');
        return sb.toString();
    }
}
