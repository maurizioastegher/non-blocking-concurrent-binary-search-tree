package cbst;

/**
 * Created by Maurizio Astegher on 01/02/2016
 */
public class IInfo extends Info {
    final Node newInternal;

    public IInfo(Node parent, Node leaf, Node newInternal) {
        super(parent, leaf);
        this.newInternal = newInternal;
    }

    public Node getNewInternal() {
        return newInternal;
    }
}
