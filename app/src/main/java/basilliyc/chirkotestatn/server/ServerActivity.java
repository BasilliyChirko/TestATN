package basilliyc.chirkotestatn.server;

import android.os.Bundle;

import basilliyc.chirkotestatn.R;
import basilliyc.chirkotestatn.base.BaseWorkActivity;

public class ServerActivity extends BaseWorkActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);
        setUpPage();
    }

    private void setUpPage() {
        setTitle(R.string.server_title);
    }
}