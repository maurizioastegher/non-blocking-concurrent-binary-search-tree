package test;

import cbst.CBST;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by Maurizio Astegher on 02/02/2016
 */
public class SubThread extends Thread {
    CBST tree;
    ConcurrentLinkedQueue operations;
    ConcurrentLinkedQueue results;

    public SubThread(CBST tree, ConcurrentLinkedQueue operations, ConcurrentLinkedQueue results) {
        this.tree = tree;
        this.operations = operations;
        this.results = results;
    }

    @Override
    public void run() {
        long threadId = Thread.currentThread().getId();

        while (!operations.isEmpty()) {
            Operation op = (Operation) operations.poll();
            if (op == null) {
                break;
            }
            Type type = op.getType();
            int key = op.getKey();

            if (type == Type.INSERT) {
                System.out.println("Thread " + threadId + ": insert(" + key + ") starting...");
                if (tree.insert(key)) {
                    System.out.println("Thread " + threadId + ": insert(" + key + ") + SUCCESS!");
                    results.add(new Operation(Type.INSERT, key));
                } else {
                    System.out.println("Thread " + threadId + ": insert(" + key + ") - FAILURE! Cannot insert duplicate key");
                }
            } else if (type == Type.DELETE) {
                System.out.println("Thread " + threadId + ": delete(" + key + ") starting...");
                if (tree.delete(key)) {
                    System.out.println("Thread " + threadId + ": delete(" + key + ") + SUCCESS!");
                    results.add(new Operation(Type.DELETE, key));
                } else {
                    System.out.println("Thread " + threadId + ": delete(" + key + ") - FAILURE! Key not present");
                }
            } else if (type == Type.FIND) {
                System.out.println("Thread " + threadId + ": find(" + key + ") starting...");
                if (tree.contains(key)) {
                    System.out.println("Thread " + threadId + ": find(" + key + ") + FOUND!");
                    results.add(new Operation(Type.FIND, key));
                } else {
                    System.out.println("Thread " + threadId + ": find(" + key + ") - NOT FOUND!");
                }
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}