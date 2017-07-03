package test;

/**
 * Created by Maurizio Astegher on 02/02/2016
 */
public class Operation {
    Type type;
    int key;

    public Operation(Type type, int key) {
        this.type = type;
        this.key = key;
    }

    public Type getType() {
        return type;
    }

    public int getKey() {
        return key;
    }
}
