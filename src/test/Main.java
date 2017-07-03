package test;

import cbst.CBST;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by Maurizio Astegher on 02/02/2016
 */
public class Main {
    static int numInsert = 10;
    static int numDelete = 5;
    static int numFind = 5;
    static int maxKeyValue = 10;
    static int numThreads = 5;

    static ArrayList temp = new ArrayList();

    public static void main(String[] args) {
        CBST tree = new CBST();
        ConcurrentLinkedQueue operations = new ConcurrentLinkedQueue();
        ConcurrentLinkedQueue results = new ConcurrentLinkedQueue();

        System.out.println("Generating random operations...");
        Random randomGenerator = new Random();

        System.out.print("(" + numInsert + ") Insert: ");
        generateOp(Type.INSERT, numInsert, randomGenerator);

        System.out.print("\n(" + numDelete + ") Delete: ");
        generateOp(Type.DELETE, numDelete, randomGenerator);

        System.out.print("\n(" + numFind + ") Find: ");
        generateOp(Type.FIND, numFind, randomGenerator);

        System.out.println("\n");

        // Shuffling operations
        while (!temp.isEmpty()) {
            operations.add(temp.remove(randomGenerator.nextInt(temp.size())));
        }

        // Starting threads
        Set<Thread> threads = new HashSet<>();
        for (int i = 0; i < numThreads; i++) {
            Thread t = new SubThread(tree, operations, results);
            threads.add(t);
        }
        System.out.println(numThreads + " threads STARTED!");
        for (Thread t : threads) {
            t.start();
        }

        for (Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Printing successful operations
        System.out.println("\n" + results.size() + " operations SUCCESSFUL!");
        ArrayList ins = new ArrayList();
        ArrayList del = new ArrayList();
        ArrayList find = new ArrayList();
        while (!results.isEmpty()) {
            Operation op = (Operation) results.poll();
            Type type = op.getType();
            if (type == Type.INSERT) {
                ins.add(op.getKey());
            } else if (type == Type.DELETE) {
                del.add(op.getKey());
            } else if (type == Type.FIND) {
                find.add(op.getKey());
            }
        }

        System.out.print("(" + ins.size() + ") Insert: ");
        printOp(ins);

        System.out.print("\n(" + del.size() + ") Delete: ");
        printOp(del);

        System.out.print("\n(" + find.size() + ") Find: ");
        printOp(find);

        System.out.println("\n");

        System.out.println("Printing tree structure...");
        for (int i = 0; i < del.size(); i++) {
            for (int j = 0; j < ins.size(); j++) {
                if (ins.get(j) == del.get(i)) {
                    ins.remove(j);
                    break;
                }
            }
        }
        Collections.sort(ins);
        System.out.print("(" + ins.size() + ") Tree items: ");
        printOp(ins);

        System.out.println("\n");
        StringBuilder output = new StringBuilder();
        tree.printStructure(output);
        System.out.println(output);

        tree.generateDOT();
    }

    public static void generateOp(Type type, int max, Random randomGenerator) {
        for (int i = 0; i < max; i++) {
            if (i > 0) {
                System.out.print(", ");
            }
            int n = randomGenerator.nextInt(maxKeyValue) + 1;
            temp.add(new Operation(type, n));
            System.out.print(n);
        }
    }

    public static void printOp(ArrayList list) {
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) {
                System.out.print(", ");
            }
            System.out.print(list.get(i));
        }
    }
}