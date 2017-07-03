package cbst;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

/**
 * Created by Maurizio Astegher on 02/02/2016
 */
public class CBST {
    final Node root;

    public CBST() {
        root = new Node(Integer.MAX_VALUE, new Node(Integer.MAX_VALUE - 1), new Node(Integer.MAX_VALUE));
    }

    public boolean insert(Integer k) {
        Node newNode = new Node(k); // Pointer to a new leaf node whose key field is k
        Node newSibling; // Pointer to a new leaf whose key is leaf -> key
        Node newInternal; // Pointer to a new internal node whose key field is max(k, leaf -> key)

        while (true) {
            SearchResult result = search(k);
            Node parent = result.getParent();
            Node leaf = result.getLeaf();
            Update parentUpdate = result.getParentUpdate();
            Integer leafKey = leaf.getKey();

            if (k.compareTo(leafKey) == 0) { // Cannot insert duplicate key; return false
                return false;
            } else if (parentUpdate != null && parentUpdate.getState() != State.CLEAN) {
                help(parentUpdate); // Help the other operation
            } else {
                newSibling = new Node(leafKey);
                if (k.compareTo(leafKey) < 0) {
                    newInternal = new Node(leafKey, newNode, newSibling);
                } else {
                    newInternal = new Node(k, newSibling, newNode);
                }

                IInfo newInfo = new IInfo(parent, leaf, newInternal);
                final Update newUpdate = new Update(State.IFLAG, newInfo);
                if (parent.compareAndSetUpdate(parentUpdate, newUpdate)) { // Try to flag parent using CAS (iflag)
                    helpInsert(newUpdate); // The CAS was successful; finish the insertion
                    return true;
                } else {
                    help(parent.getUpdate()); // The CAS failed; help the operation that caused failure
                }
            }
        }
    }

    public boolean delete(Integer k) {
        while (true) {
            SearchResult result = search(k);
            Node grandParent = result.getGrandParent();
            Node parent = result.getParent();
            Node leaf = result.getLeaf();
            Update grandParentUpdate = result.getGrandParentUpdate();
            Update parentUpdate = result.getParentUpdate();
            Integer leafKey = leaf.getKey();

            if (k.compareTo(leafKey) != 0) { // key is not in the tree; return false
                return false;
            } else if (grandParentUpdate != null && grandParentUpdate.getState() != State.CLEAN) {
                help(grandParentUpdate); // Help the other operation
            } else if (parentUpdate != null && parentUpdate.getState() != State.CLEAN) {
                help(parentUpdate); // Help the other operation
            } else {

                DInfo newInfo = new DInfo(grandParent, parent, leaf, parentUpdate);
                final Update newUpdate = new Update(State.DFLAG, newInfo);

                // Try to flag grandParent using CAS (dflag)
                if (grandParent.compareAndSetUpdate(grandParentUpdate, newUpdate)) {
                    if (helpDelete(newUpdate)) { // CAS successful; either finish deletion or unflag grandParent
                        return true;
                    }
                } else {
                    help(grandParent.getUpdate()); // The CAS failed; help the operation that caused the failure
                }
            }
        }
    }

    public Node find(Integer k) {
        SearchResult result = search(k);
        Node leaf = result.getLeaf();
        Integer leafKey = leaf.getKey();

        if (k.compareTo(leafKey) != 0) {
            return null;
        }
        return leaf;
    }

    public boolean contains(Integer k) {
        if (find(k) == null) {
            return false;
        } else {
            return true;
        }
    }

    // General-purpose helping routine
    public void help(Update update) {
        if (update.getState() == State.IFLAG) {
            helpInsert(update);
        } else if (update.getState() == State.DFLAG) {
            helpDelete(update);
        } else if (update.getState() == State.MARK) {
            helpMarked(update);
        }
    }

    public void helpInsert(Update update) {
        IInfo info = (IInfo) update.getInfo();
        Node parent = info.getParent();

        // Look for correct position (left or right) and swing pointer using CAS (ichild)
        casChild(parent, info.getLeaf(), info.getNewInternal());

        parent.compareAndSetUpdate(update, new Update(State.CLEAN, info)); // Unflag parent using CAS (iunflag)
    }

    public boolean helpDelete(Update update) {
        DInfo info = (DInfo) update.getInfo();
        Node grandParent = info.getGrandParent();
        Node parent = info.getParent();

        final Update newUpdate = new Update(State.MARK, info);
        if (parent.compareAndSetUpdate(info.getParentUpdate(), newUpdate)) { // Try to mark parent using CAS (mark)
            helpMarked(update); // Complete the deletion
            return true; // Tell delete routine it is done
        } else {
            help(parent.getUpdate()); // The CAS failed; help the operation that caused the failure
            grandParent.compareAndSetUpdate(update, new Update(State.CLEAN, info)); // Backtrack CAS
            return false;
        }
    }

    public void helpMarked(Update update) {
        DInfo info = (DInfo) update.getInfo();
        Node grandParent = info.getGrandParent();
        Node parent = info.getParent();
        Node other;

        // Set other to point to the sibling of the node to which info -> leaf points
        if (parent.getRight() == info.getLeaf()) {
            other = parent.getLeft();
        } else {
            other = parent.getRight();
        }

        // Splice the node to which info -> parent points out of the tree, replacing it by other using CAS (dchild)
        casChild(grandParent, parent, other);

        grandParent.compareAndSetUpdate(update, new Update(State.CLEAN, info)); // Unflag grandParent using CAS (dunflag)
    }

    public void casChild(Node parent, Node leaf, Node newInternal) {
        if (parent.getLeft() == leaf) {
            parent.compareAndSetLeft(leaf, newInternal);
        } else {
            parent.compareAndSetRight(leaf, newInternal);
        }
    }

    public SearchResult search(Integer k) {
        SearchResult result = new SearchResult();

        Node grandParent = null;
        Node parent = root;
        Node leaf = parent.getLeft();
        Integer leafKey = leaf.getKey();

        while (leaf.getLeft() != null) {
            grandParent = parent; // Remember grandparent of leaf
            parent = leaf; // Remember parent of leaf
            if (k.compareTo(leafKey) < 0) { // Move down to appropriate child
                leaf = leaf.getLeft();
            } else {
                leaf = leaf.getRight();
            }
            leafKey = leaf.getKey();
        }

        Update grandParentUpdate = null;
        Update parentUpdate = parent.getUpdate();
        if (grandParent != null) { // grandParent is null when the tree is empty
            grandParentUpdate = grandParent.getUpdate();
        }

        result.setGrandParent(grandParent);
        result.setParent(parent);
        result.setLeaf(leaf);
        result.setGrandParentUpdate(grandParentUpdate);
        result.setParentUpdate(parentUpdate);

        return result;
    }

    public StringBuilder printStructure(StringBuilder output) {
        print(root, output);
        return output;
    }

    public void generateDOT() {
        PrintWriter writer = null;
        try {
            writer = new PrintWriter("tree.dot");
            writer.println("digraph {");
            writer.println("graph [ordering = \"out\"];");

            // Print root and dummy nodes
            String color = ", style = filled, fillcolor = grey80";
            writer.println("{" + root.getKey() + " [label = \"INF2\"" + color + "]} -> " +
                    "{" + root.getLeft().getKey() + " [label = \"INF1\"" + color + "]}");
            writer.println("{" + root.getKey() + " [label = \"INF2\"" + color + "]} -> " +
                    "{L" + root.getRight().getKey() + " [label = \"INF2\"" + color + "]}");

            // If the tree is not empty print the 4th dummy node and start iterating
            Node trueRoot = root.getLeft().getLeft();
            if (trueRoot != null) {
                writer.println("{" + root.getLeft().getKey() + " [label = \"INF1\"" + color + "]} -> " + trueRoot.getKey());
                writer.println("{" + root.getLeft().getKey() + " [label = \"INF1\"" + color + "]} -> " +
                        "{L" + root.getLeft().getRight().getKey() + " [label = \"INF1\"" + color + "]}");
                print(trueRoot, writer);
            }
            writer.println("}");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                writer.close();
                System.out.println("\nDOT file generated!");
            }
        }
    }

    public void print(Node root, StringBuilder output) {
        Node left = root.getLeft();
        Node right = root.getRight();

        if (root == this.root) {
            output.append("Node(" + root.getKey() + "");
        } else {
            output.append(", Node(" + root.getKey() + "");
        }

        if (left != null) {
            if (left.getLeft() == null && left.getRight() == null) {
                output.append(", Leaf " + left.getKey());
            } else {
                print(left, output);
            }
        } else {
            output.append(", Empty");
        }

        if (right != null) {
            if (right.getLeft() == null && right.getRight() == null) {
                output.append(", Leaf " + right.getKey() + ")");
            } else {
                print(right, output);
                output.append(")");
            }
        } else {
            output.append(", Empty)");
        }
    }

    public void print(Node root, PrintWriter writer) {
        Node left = root.getLeft();
        Node right = root.getRight();

        if (left != null) {
            if (left.getLeft() == null && left.getRight() == null) {
                String color = ", style = filled, fillcolor = palegreen";
                writer.println(root.getKey() + " -> {L" + left.getKey() +
                        " [label = \"" + left.getKey() + "\"" + color + "]}");
            } else {
                writer.println(root.getKey() + " -> " + left.getKey());
                print(left, writer);
            }
        }

        if (right != null) {
            if (right.getLeft() == null && right.getRight() == null) {
                String color = ", style = filled, fillcolor = palegreen";
                writer.println(root.getKey() + " -> {L" + right.getKey() +
                        " [label = \"" + right.getKey() + "\"" + color + "]}");
            } else {
                writer.println(root.getKey() + " -> " + right.getKey());
                print(right, writer);
            }
        }
    }
}