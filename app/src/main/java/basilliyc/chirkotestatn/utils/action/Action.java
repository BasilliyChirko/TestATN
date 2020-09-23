package basilliyc.chirkotestatn.utils.action;

public class Action<T> {
    public T data;
    private boolean active = false;

    public boolean isActive() {
        if (active) {
            active = false;
            return true;
        } else {
            return false;
        }
    }

    public Action<T> activate() {
        active = true;
        return this;
    }
}
