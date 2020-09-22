package basilliyc.chirkotestatn.client;

import android.os.Bundle;

import basilliyc.chirkotestatn.R;
import basilliyc.chirkotestatn.base.BaseWorkActivity;

public class ClientActivity extends BaseWorkActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);
        setUpPage();
    }

    private void setUpPage() {
        setTitle(R.string.client_title);
    }
}