package basilliyc.chirkotestatn.utils.action;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

public class ActionCallLiveData<T> extends MutableLiveData<Action<T>> {

    public interface Callback<T> {
        void onEvent(T data);
    }

    public ActionCallLiveData() {
        super(new Action<T>());
    }

    public void activate(T data) {
        Action<T> value = getValue();
        if (value == null) {
            value = new Action<>();
        }

        value.data = data;
        postValue(value.activate());
    }

    public void onEvent(LifecycleOwner owner, final Callback<T> callback) {
        observe(owner, new Observer<Action<T>>() {
            @Override
            public void onChanged(Action<T> tAction) {
                if (tAction.isActive()) {
                    callback.onEvent(tAction.data);
                }
            }
        });
    }

}
