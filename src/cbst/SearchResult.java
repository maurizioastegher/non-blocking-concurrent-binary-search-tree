package cbst;

/**
 * Created by Maurizio Astegher on 01/02/2016
 */
public class SearchResult {
    Node grandParent;
    Node parent;
    Node leaf;
    Update grandParentUpdate;
    Update parentUpdate;

    public SearchResult() {
        this.grandParent = null;
        this.parent = null;
        this.leaf = null;
        this.grandParentUpdate = null;
        this.parentUpdate = null;
    }

    public Node getGrandParent() {
        return grandParent;
    }

    public void setGrandParent(Node grandParent) {
        this.grandParent = grandParent;
    }

    public Node getParent() {
        return parent;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    public Node getLeaf() {
        return leaf;
    }

    public void setLeaf(Node leaf) {
        this.leaf = leaf;
    }

    public Update getGrandParentUpdate() {
        return grandParentUpdate;
    }

    public void setGrandParentUpdate(Update grandParentUpdate) {
        this.grandParentUpdate = grandParentUpdate;
    }

    public Update getParentUpdate() {
        return parentUpdate;
    }

    public void setParentUpdate(Update parentUpdate) {
        this.parentUpdate = parentUpdate;
    }
}
