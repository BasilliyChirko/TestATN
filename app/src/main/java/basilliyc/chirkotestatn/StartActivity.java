package basilliyc.chirkotestatn;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import basilliyc.chirkotestatn.client.ClientActivity;
import basilliyc.chirkotestatn.server.ServerActivity;

public class StartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        initListeners();
    }

    private void initListeners() {
        findViewById(R.id.start_mode_client).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startAsClient();
            }
        });

        findViewById(R.id.start_mode_server).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startAsServer();
            }
        });
    }


    private void startAsClient() {
        startActivity(new Intent(this, ClientActivity.class));
    }

    private void startAsServer() {
        startActivity(new Intent(this, ServerActivity.class));
    }
}