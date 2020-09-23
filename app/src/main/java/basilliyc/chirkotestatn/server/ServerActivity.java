package basilliyc.chirkotestatn.server;

import android.os.Bundle;
import android.widget.TextView;

import androidx.lifecycle.ViewModelProvider;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;

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


    public String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        String hostAddress = inetAddress.getHostAddress();
                        String substring1 = hostAddress.substring(6, 25).replaceAll(":", "");
                        String s = substring1.substring(0, 6) + substring1.substring(10);
                        StringBuilder s2 = new StringBuilder();
                        int a = 0;

                        for (char c : s.toCharArray()) {
                            if (a == 2) {
                                a = 0;
                                s2.append(":");
                            }

                            s2.append(c);
                            a++;
                        }

                        return s2.toString();
                    }
                }
            }
        } catch (SocketException ex) {
            ex.printStackTrace();
            Utils.log(ex.toString());
        }
        return null;
    }


}