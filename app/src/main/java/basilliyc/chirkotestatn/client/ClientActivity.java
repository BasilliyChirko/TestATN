package basilliyc.chirkotestatn.client;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProvider;

import basilliyc.chirkotestatn.R;
import basilliyc.chirkotestatn.base.BaseWorkActivity;
import basilliyc.chirkotestatn.server.ServerViewModel;
import basilliyc.chirkotestatn.utils.Utils;

public class ClientActivity extends BaseWorkActivity<ClientViewModel> {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);
        setUpPage();
        initListeners();
    }

    @Override
    protected ClientViewModel createViewModel() {
        return new ViewModelProvider(this).get(ClientViewModel.class);
    }

    private void setUpPage() {
        setTitle(R.string.client_title);
    }

    private void initListeners() {
        findViewById(R.id.discover).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickDiscover();
            }
        });
    }

    private void onClickDiscover() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permission ACCESS_FINE_LOCATION is denied", Toast.LENGTH_SHORT).show();
            return;
        }

        wifiManager.discoverPeers(wifiChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Utils.log("discover succ");
            }

            @Override
            public void onFailure(int reasonCode) {
                switch (reasonCode) {
                    case WifiP2pManager.P2P_UNSUPPORTED:
                        onError(new Throwable("WifiP2pManager.P2P_UNSUPPORTED"));
                        break;
                    case WifiP2pManager.BUSY:
                        onError(new Throwable("WifiP2pManager.BUSY"));
                        break;
                    default:
                        onError(new Throwable("WifiP2pManager.ERROR"));
                }

            }
        });
    }
}