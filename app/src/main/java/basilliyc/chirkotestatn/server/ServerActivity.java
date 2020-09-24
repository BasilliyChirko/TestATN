package basilliyc.chirkotestatn.server;

import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.format.Formatter;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.lifecycle.ViewModelProvider;

import basilliyc.chirkotestatn.R;
import basilliyc.chirkotestatn.base.BaseWorkActivity;

public class ServerActivity extends BaseWorkActivity<ServerViewModel> {

    protected Button submitServerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initListeners();
        setUpPage();
    }

    @Override
    public int getLayout() {
        return R.layout.activity_server;
    }

    @Override
    protected ServerViewModel createViewModel() {
        return new ViewModelProvider(this).get(ServerViewModel.class);
    }

    private void initView() {
        submitServerButton = ((Button) findViewById(R.id.submit_server));
    }

    private void initListeners() {
        submitServerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewModel.toggleServerSocket();
            }
        });
    }

    @Override
    public void setUpPage() {
        super.setUpPage();
        setTitle(R.string.server_title);

        String address = getLocalIpAddress();
        if (address != null) {
            address = getResources().getString(R.string.current_ip, address);
        }
        ((TextView) findViewById(R.id.current_ip_address)).setText(address);
    }

    private String getLocalIpAddress() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        return Formatter.formatIpAddress(wifiManager.getConnectionInfo().getIpAddress());
    }

    @Override
    public void setUpObservers() {
        super.setUpObservers();

    }

    @Override
    public void onStatusChanged(SocketStatus status) {
        super.onStatusChanged(status);
        switch (status) {
            case WAIT_FOR_CLIENT:
                submitServerButton.setText(R.string.stop);
                break;
            case CONNECTED:
                submitServerButton.setText(R.string.stop);
                break;
            case DISCONNECTED:
                submitServerButton.setText(R.string.start);
                break;
            case DOWNLOADING:
                submitServerButton.setText(R.string.stop);
                break;
            case UPLOADING:
                submitServerButton.setText(R.string.stop);
                break;
        }
    }



}