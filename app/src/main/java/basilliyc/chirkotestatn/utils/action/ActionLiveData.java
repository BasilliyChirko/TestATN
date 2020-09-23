package basilliyc.chirkotestatn.utils.action;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

public class ActionLiveData extends MutableLiveData<Action<Object>> {

    public interface Callback {
        void onEvent();
    }


    public ActionLiveData() {
        super(new Action<>());
    }

    public void activate() {
        Action<Object> value = getValue();
        if (value == null) {
            value = new Action<>();
        }

        postValue(value.activate());
    }


    public void onEvent(LifecycleOwner owner, final Callback callback) {
        observe(owner, new Observer<Action<Object>>() {
            @Override
            public void onChanged(Action<Object> tAction) {
                if (tAction.isActive()) {
                    callback.onEvent();
                }
            }
        });
    }


}