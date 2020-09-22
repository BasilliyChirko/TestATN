package basilliyc.chirkotestatn.server;

import android.os.Bundle;

import androidx.lifecycle.ViewModelProvider;

import basilliyc.chirkotestatn.R;
import basilliyc.chirkotestatn.base.BaseWorkActivity;
import basilliyc.chirkotestatn.base.BaseWorkViewModel;

public class ServerActivity extends BaseWorkActivity<ServerViewModel> {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);
        setUpPage();
    }

    @Override
    protected ServerViewModel createViewModel() {
        return new ViewModelProvider(this).get(ServerViewModel.class);
    }

    private void setUpPage() {
        setTitle(R.string.server_title);
    }


}