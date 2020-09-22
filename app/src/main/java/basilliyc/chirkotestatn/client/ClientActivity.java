package basilliyc.chirkotestatn.client;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
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

        onWifiPeersChanged();
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
        discoverPeers();
    }

    @Override
    public void onWifiPeersChanged() {
        super.onWifiPeersChanged();
        //TODO handle permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permission ACCESS_FINE_LOCATION is denied", Toast.LENGTH_SHORT).show();
            return;
        }

        Utils.log("onWifiPeersChanged");
        wifiManager.requestPeers(wifiChannel, new WifiP2pManager.PeerListListener() {
            @Override
            public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {
                Utils.log("onPeersAvailable " + wifiP2pDeviceList.getDeviceList().size());
                StringBuilder builder = new StringBuilder();
                for (WifiP2pDevice device : wifiP2pDeviceList.getDeviceList()) {
                    builder.append(device.deviceAddress);
                    builder.append("\n");
                    builder.append(device.deviceName);
                    builder.append("\n");
                    builder.append("\n");
                }
                ((TextView) findViewById(R.id.device_list)).setText(builder.toString());
            }
        });
    }
}