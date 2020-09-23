package basilliyc.chirkotestatn.base;

import androidx.lifecycle.ViewModel;

import basilliyc.chirkotestatn.Preferences;
import basilliyc.chirkotestatn.utils.action.ActionCallLiveData;
import io.reactivex.disposables.CompositeDisposable;

public class BaseWorkViewModel extends ViewModel {

    protected CompositeDisposable compositeDisposable = new CompositeDisposable();
    public Preferences preferences;

    public ActionCallLiveData<Throwable> error = new ActionCallLiveData<>();

    @Override
    protected void onCleared() {
        super.onCleared();
        compositeDisposable.dispose();
    }

    public void onError(Throwable throwable) {
        error.activate(throwable);
    }
}
