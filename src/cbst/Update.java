package cbst;

/**
 * Created by Maurizio Astegher on 01/02/2016
 */
public class Update {
    final State state;
    final Info info;

    public Update(State state, Info info) {
        this.state = state;
        this.info = info;
    }

    public State getState() {
        return state;
    }

    public Info getInfo() {
        return info;
    }
}
