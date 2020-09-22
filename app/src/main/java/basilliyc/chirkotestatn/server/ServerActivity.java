package basilliyc.chirkotestatn.server;

import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.format.Formatter;
import android.widget.TextView;

import androidx.lifecycle.ViewModelProvider;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import basilliyc.chirkotestatn.Constants;
import basilliyc.chirkotestatn.R;
import basilliyc.chirkotestatn.base.BaseWorkActivity;
import basilliyc.chirkotestatn.utils.Utils;

public class ServerActivity extends BaseWorkActivity<ServerViewModel> {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);
        setUpPage();
        foo();
    }

    @Override
    protected ServerViewModel createViewModel() {
        return new ViewModelProvider(this).get(ServerViewModel.class);
    }

    @Override
    public void setUpPage() {
        super.setUpPage();
        setTitle(R.string.server_title);

        ((TextView) findViewById(R.id.server_label_ip)).setText(getLocalIpAddress());
    }

    private String getLocalIpAddress() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        return Formatter.formatIpAddress(wifiManager.getConnectionInfo().getIpAddress());
    }

    private void foo() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Utils.log("server 1");
                    ServerSocket serverSocket = new ServerSocket(Constants.SOCKET_PORT);
                    Utils.log("server 2");
                    Socket client = serverSocket.accept();
                    Utils.log("server 3");
                    Utils.log(client);
                } catch (IOException e) {
                    Utils.log("server error");
                    e.printStackTrace();
                }
            }
        }).start();
    }

}