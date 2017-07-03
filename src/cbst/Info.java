package cbst;

/**
 * Created by Maurizio Astegher on 01/02/2016
 */
public class Info {
    final Node parent;
    final Node leaf;

    public Info(Node parent, Node leaf) {
        this.parent = parent;
        this.leaf = leaf;
    }

    public Node getParent() {
        return parent;
    }

    public Node getLeaf() {
        return leaf;
    }
}
