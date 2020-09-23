package basilliyc.chirkotestatn;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;

import basilliyc.chirkotestatn.base.BaseActivity;
import basilliyc.chirkotestatn.client.ClientActivity;
import basilliyc.chirkotestatn.server.ServerActivity;

public class StartActivity extends BaseActivity {


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
                start(false);
            }
        });

        findViewById(R.id.start_mode_server).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                start(true);
            }
        });
    }

    private void start(final boolean asServer) {
        withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, new PermissionCallback() {
            @Override
            public void onGranted() {
                if (asServer) {
                    startAsServer();
                } else {
                    startAsClient();
                }
            }

            @Override
            public void showRationale() {
                new AlertDialog.Builder(StartActivity.this)
                        .setMessage(R.string.rationale_write_external)
                        .setPositiveButton(R.string.settings, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent();
                                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package", getPackageName(), null);
                                intent.setData(uri);
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                        .create()
                        .show();
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