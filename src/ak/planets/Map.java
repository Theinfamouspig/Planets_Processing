package ak.planets;

import ak.planets.building.Node;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Aleksander on 20/10/2015.
 */

public class Map {

    HashMap<Point, Node> nodeMap;
    ArrayList<Point> points;

    public Map() {
        this.nodeMap = new HashMap<>();
        this.points = new ArrayList<>();
    }

    public boolean add(Node n) {
        Point p = n.getPoint();
        if (!points.contains(p)) {
            nodeMap.put(p, n);
            points.add(p);
            System.out.println("Added " + p + " to nodeMap, with hash " + p.hashCode() + " \t nodeMap contains " + nodeMap.size() + " values");
            return true;
        }
        return false;
    }

    public void remove(Node n) {
        Point p = n.getPoint();
        nodeMap.remove(p);
        while (points.contains(p))
            points.remove(p);
    }

    public Node get(Point p) {
        System.out.println("Getting " + p + " from nodeMap with hashcode " + p.hashCode() + " \t nodeMap contains " + nodeMap.size() + " values");
        return nodeMap.get(p);
    }

    //TODO: FIX TO USE {@CODE final Point}
    public Node search(final Point p, int max) {
        if (nodeMap.size() == 0)
            return null;
        ArrayList<Point> sorted = new ArrayList<>(points);
        sorted.sort((p1, p2) -> Double.compare(p1.computeDistanceSquared(p), p2.computeDistanceSquared(p)));
        if (max < 0)
            return nodeMap.get(sorted.get(0));
        if (sorted.get(0).computeDistanceSquared(p) > max * max)
            return null;
        return nodeMap.get(sorted.get(0));
    }

    public ArrayList<Node> sortByDistance(final Point p){
        if (nodeMap.size() == 0)
            return null;
        ArrayList<Point> sorted = new ArrayList<>(points);
        sorted.sort((p1, p2) -> Double.compare(p1.computeDistanceSquared(p), p2.computeDistanceSquared(p)));
        ArrayList<Node> nodeList = new ArrayList<>(sorted.size());
        sorted.stream().forEachOrdered(point -> nodeList.add(nodeMap.get(point)));
        return nodeList;
    }
}
