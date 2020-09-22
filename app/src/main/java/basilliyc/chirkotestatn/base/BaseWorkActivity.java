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
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import basilliyc.chirkotestatn.R;
import basilliyc.chirkotestatn.utils.Utils;

abstract public class BaseWorkActivity<T extends BaseWorkViewModel> extends AppCompatActivity implements WifiP2pManager.ChannelListener {

    protected WifiP2pManager wifiManager;
    protected WifiP2pManager.Channel wifiChannel;
    private BroadcastReceiver wifiStateReceiver;
    private IntentFilter wifiStateIntentFilter;

    protected T viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = createViewModel();
        setUpPage();
        setUpWifi();
    }

    private void setUpPage() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }
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

    /* register the broadcast receiver with the intent values to be matched */
    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(wifiStateReceiver, wifiStateIntentFilter);
        discoverPeers();
    }

    /* unregister the broadcast receiver */
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(wifiStateReceiver);
    }

    public void onError(Throwable throwable) {
        Utils.log(throwable);
    }

    public void onWifiStateChanged() {

    }

    public void onWifiPeersChanged() {

    }

    public void onWifiConnectionChanged() {

    }

    public void onWifiThisDeviceChanged() {

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopSearchPeer();
    }


    protected void discoverPeers() {
        //TODO handle permission
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

    protected void stopSearchPeer() {
        wifiManager.stopPeerDiscovery(wifiChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                //Nothing
            }

            @Override
            public void onFailure(int i) {
                //Nothing
            }
        });
    }
}
