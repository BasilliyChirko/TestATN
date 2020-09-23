package basilliyc.chirkotestatn.base;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;

import basilliyc.chirkotestatn.Preferences;
import basilliyc.chirkotestatn.R;
import basilliyc.chirkotestatn.utils.Error;
import basilliyc.chirkotestatn.utils.Utils;
import basilliyc.chirkotestatn.utils.action.ActionCallLiveData;

abstract public class BaseWorkActivity<T extends BaseWorkViewModel> extends BaseActivity {

    protected T viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = createViewModel();
        viewModel.preferences = new Preferences(this);
        setUpObservers();
    }

    public void setUpPage() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }
    }

    protected abstract T createViewModel();

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void setUpObservers() {
        viewModel.error.onEvent(this, new ActionCallLiveData.Callback<Throwable>() {
            @Override
            public void onEvent(Throwable data) {
                onError(data);
            }
        });
    }

    public void onError(Throwable throwable) {
        Utils.log(throwable);
        showAlert(Error.humanizeError(throwable));
    }


    public void showAlert(String text) {
        if (text == null || text.isEmpty()) {
            return;
        }

        new AlertDialog.Builder(this)
                .setMessage(text)
                .setPositiveButton(R.string.alert_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .show();
    }

}
