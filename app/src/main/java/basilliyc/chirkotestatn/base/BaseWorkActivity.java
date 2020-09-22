package basilliyc.chirkotestatn.base;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.Looper;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

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
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
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
        //TODO
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        //TODO
    }

    /* register the broadcast receiver with the intent values to be matched */
    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(wifiStateReceiver, wifiStateIntentFilter);
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
}
