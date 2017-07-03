package cbst;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/**
 * Created by Maurizio Astegher on 01/02/2016
 */
public class Node {
    final Integer key;
    volatile Node left;
    volatile Node right;
    volatile Update update; // The state and info fields are stored together in a CAS object

    // AtomicReferenceFieldUpdater enables atomic updates to designated volatile reference fields of designated classes
    final AtomicReferenceFieldUpdater<Node, Node> leftUpdater =
            AtomicReferenceFieldUpdater.newUpdater(Node.class, Node.class, "left");
    final AtomicReferenceFieldUpdater<Node, Node> rightUpdater =
            AtomicReferenceFieldUpdater.newUpdater(Node.class, Node.class, "right");
    final AtomicReferenceFieldUpdater<Node, Update> updateUpdater =
            AtomicReferenceFieldUpdater.newUpdater(Node.class, Update.class, "update");

    // Internal node
    public Node(Integer key, Node left, Node right) {
        this.key = key;
        this.left = left;
        this.right = right;
        this.update = new Update(State.CLEAN, null);
    }

    // Leaf
    public Node(Integer key) {
        this.key = key;
        this.left = null;
        this.right = null;
        this.update = new Update(State.CLEAN, null);
    }

    boolean compareAndSetLeft(Node expect, Node update) {
        return leftUpdater.compareAndSet(this, expect, update);
    }

    boolean compareAndSetRight(Node expect, Node update) {
        return rightUpdater.compareAndSet(this, expect, update);
    }

    boolean compareAndSetUpdate(Update expect, Update update) {
        return updateUpdater.compareAndSet(this, expect, update);
    }

    public Integer getKey() {
        return key;
    }

    public Node getLeft() {
        return left;
    }

    public Node getRight() {
        return right;
    }

    public Update getUpdate() {
        return update;
    }
}