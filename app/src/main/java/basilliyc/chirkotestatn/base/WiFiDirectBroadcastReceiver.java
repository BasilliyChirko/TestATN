package basilliyc.chirkotestatn.base;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pManager;

import basilliyc.chirkotestatn.utils.Utils;

public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {

    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private BaseWorkActivity activity;

    public WiFiDirectBroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel, BaseWorkActivity activity) {
        super();
        this.manager = manager;
        this.channel = channel;
        this.activity = activity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (action == null) {
            return;
        }

        switch (action) {
            case WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION:
                Utils.log("WIFI_P2P_STATE_CHANGED_ACTION");
                // Check to see if Wi-Fi is enabled and notify appropriate activity
                break;
            case WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION:
                Utils.log("WIFI_P2P_PEERS_CHANGED_ACTION");
                // Call WifiP2pManager.requestPeers() to get a list of current peers
                break;
            case WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION:
                Utils.log("WIFI_P2P_CONNECTION_CHANGED_ACTION");
                // Respond to new connection or disconnections
                break;
            case WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION:
                Utils.log("WIFI_P2P_THIS_DEVICE_CHANGED_ACTION");
                // Respond to this device's wifi state changing
                break;
        }

    }
}
