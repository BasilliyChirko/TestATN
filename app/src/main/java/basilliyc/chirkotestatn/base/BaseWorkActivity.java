package basilliyc.chirkotestatn.base;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.Observer;

import basilliyc.chirkotestatn.R;
import basilliyc.chirkotestatn.utils.Utils;

abstract public class BaseWorkActivity<T extends BaseWorkViewModel> extends BaseActivity implements WifiP2pManager.ChannelListener {

    protected WifiP2pManager wifiManager;
    protected WifiP2pManager.Channel wifiChannel;
    private BroadcastReceiver wifiStateReceiver;
    private IntentFilter wifiStateIntentFilter;

    protected T viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = createViewModel();
        setUpWifi();
    }

    public void setUpPage() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }
        setUpPartConnectedDevice();
    }

    private void setUpPartConnectedDevice() {
        findViewById(R.id.part_connected_disconnect).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                wifiManager.removeGroup(wifiChannel, new WifiActionListener());
            }
        });

        viewModel.connectedDeviceAddress.observe(this, new Observer<String>() {
            @Override
            public void onChanged(final String address) {
                boolean connected = address != null && !address.isEmpty();
                findViewById(R.id.part_connected_layout).setVisibility(connected ? View.VISIBLE : View.GONE);
                String text = getResources().getString(R.string.connected_s, address);
                ((TextView) findViewById(R.id.part_connected_ip)).setText(text);
            }
        });
    }

    protected abstract T createViewModel();

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.discover_peers:
                discoverPeers();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setUpWifi() {
        wifiManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        wifiChannel = wifiManager.initialize(this, Looper.getMainLooper(), this);
        wifiStateReceiver = new WiFiDirectBroadcastReceiver(wifiManager, wifiChannel, this);

        viewModel.wifiManager = wifiManager;
        viewModel.wifiChannel = wifiChannel;

        wifiStateIntentFilter = new IntentFilter();
        wifiStateIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        wifiStateIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        wifiStateIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        wifiStateIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
    }

    @Override
    public void onChannelDisconnected() {
        Utils.log("onChannelDisconnected");
        //TODO
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        Utils.log("onPointerCaptureChanged hasCapture=" + hasCapture);
        //TODO
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(wifiStateReceiver, wifiStateIntentFilter);
        discoverPeers();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(wifiStateReceiver);
    }

    public void onError(Throwable throwable) {
        Utils.log(throwable);
    }

    public void onWifiStateChanged() {
        viewModel.onWifiStateChanged();
    }

    public void onWifiPeersChanged() {
        viewModel.onWifiPeersChanged();
    }

    public void onWifiConnectionChanged() {
        viewModel.onWifiConnectionChanged();
    }

    public void onWifiThisDeviceChanged() {
        viewModel.onWifiThisDeviceChanged();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopSearchPeer();
        wifiManager.removeGroup(wifiChannel, new WifiActionListener());
    }


    protected void discoverPeers() {
        //TODO handle permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permission ACCESS_FINE_LOCATION is denied", Toast.LENGTH_SHORT).show();
            return;
        }
        wifiManager.discoverPeers(wifiChannel, new WifiActionListener());
    }

    protected void stopSearchPeer() {
        wifiManager.stopPeerDiscovery(wifiChannel, new WifiActionListener());
    }

    public class WifiActionListener implements WifiP2pManager.ActionListener {

        @Override
        public void onSuccess() {

        }

        @Override
        public void onFailure(int reasonCode) {
            switch (reasonCode) {
                case WifiP2pManager.P2P_UNSUPPORTED:
                    if (!onUnsupported())
                        BaseWorkActivity.this.onError(new Throwable("WifiP2pManager.P2P_UNSUPPORTED"));
                    break;
                case WifiP2pManager.BUSY:
                    if (!onBusy())
                        BaseWorkActivity.this.onError(new Throwable("WifiP2pManager.BUSY"));
                    break;
                default:
                    if (!onError())
                        BaseWorkActivity.this.onError(new Throwable("WifiP2pManager.ERROR"));
            }
        }

        public boolean onBusy() {
            return false;
        }

        public boolean onUnsupported() {
            return false;
        }

        public boolean onError() {
            return false;
        }
    }
}
