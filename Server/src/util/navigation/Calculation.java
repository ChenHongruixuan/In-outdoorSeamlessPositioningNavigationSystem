package util.navigation;

import data.Node;

public class Calculation {
    public static double calDistance(Node fNode, Node sNode) {
        return Math.sqrt(Math.pow(fNode.getLatitude() - sNode.getLatitude(), 2) +
                Math.pow(fNode.getLongitude() - sNode.getLongitude(), 2));
    }
}
