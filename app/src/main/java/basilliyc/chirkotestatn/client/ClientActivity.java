package basilliyc.chirkotestatn.client;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import basilliyc.chirkotestatn.R;
import basilliyc.chirkotestatn.base.BaseWorkActivity;
import basilliyc.chirkotestatn.utils.Utils;

public class ClientActivity extends BaseWorkActivity<ClientViewModel> {

    private EditText inputIp;
    private PeersRecyclerAdapter peersRecyclerAdapter = new PeersRecyclerAdapter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);
        setUpPage();
        initViews();
        initListeners();

        onWifiPeersChanged();
    }

    @Override
    protected ClientViewModel createViewModel() {
        return new ViewModelProvider(this).get(ClientViewModel.class);
    }

    @Override
    public void setUpPage() {
        super.setUpPage();
        setTitle(R.string.client_title);
    }

    private void initViews() {
        inputIp = ((EditText) findViewById(R.id.editText));
        ((RecyclerView) findViewById(R.id.available_peers)).setAdapter(peersRecyclerAdapter);
    }

    private void initListeners() {
        findViewById(R.id.conect).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String ip = inputIp.getText().toString();

                WifiP2pConfig config = new WifiP2pConfig();
                config.deviceAddress = ip;
                connect(config);
            }
        });

        peersRecyclerAdapter.onClickItemListener = new PeersRecyclerAdapter.OnClickItemListener() {
            @Override
            public void onClickItem(WifiP2pDevice item) {
                inputIp.setText(item.deviceAddress);
            }
        };
    }

    private boolean isNeedRetryConnection = false;

    private void connect(final WifiP2pConfig config) {
        isNeedRetryConnection = !isNeedRetryConnection;

        if (ActivityCompat.checkSelfPermission(ClientActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(ClientActivity.this, "Permission ACCESS_FINE_LOCATION is denied", Toast.LENGTH_SHORT).show();
            return;
        }

        wifiManager.connect(wifiChannel, config, new WifiActionListener() {
            @Override
            public boolean onBusy() {
                if (isNeedRetryConnection) {
                    wifiManager.cancelConnect(wifiChannel, new WifiActionListener());
                    connect(config);
                }
                return isNeedRetryConnection;
            }
        });
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
                onNewPeersList(wifiP2pDeviceList);
            }
        });
    }

    private void onNewPeersList(WifiP2pDeviceList wifiP2pDeviceList) {
        Utils.log("onPeersAvailable " + wifiP2pDeviceList.getDeviceList().size());
        peersRecyclerAdapter.data.clear();
        peersRecyclerAdapter.data.addAll(wifiP2pDeviceList.getDeviceList());
        peersRecyclerAdapter.notifyDataSetChanged();
    }


}