package cbst;

/**
 * Created by Maurizio Astegher on 01/02/2016
 */
public class DInfo extends Info {
    final Node grandParent;
    final Update parentUpdate;

    public DInfo(Node grandParent, Node parent, Node leaf, Update parentUpdate) {
        super(parent, leaf);
        this.grandParent = grandParent;
        this.parentUpdate = parentUpdate;
    }

    public Node getGrandParent() {
        return grandParent;
    }

    public Update getParentUpdate() {
        return parentUpdate;
    }
}
