package basilliyc.chirkotestatn.client;

import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import basilliyc.chirkotestatn.Constants;
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
                sendTest(ip);
            }
        });

        peersRecyclerAdapter.onClickItemListener = new PeersRecyclerAdapter.OnClickItemListener() {
            @Override
            public void onClickItem(WifiP2pDevice item) {
                inputIp.setText(item.deviceAddress);
            }
        };
    }

    ///---------

    private void sendTest(final String host) {
        new Thread(new Runnable() {
            @Override
            public void run() {
//                Context context = this.getApplicationContext();
                int port = Constants.SOCKET_PORT;
                int len;
                byte buf[] = new byte[1024];


                try {

                    Socket socket = new Socket(host, port);

                    OutputStream outputStream = socket.getOutputStream();
//            ContentResolver cr = context.getContentResolver();
//            InputStream inputStream = null;
//            inputStream = cr.openInputStream(Uri.parse("path/to/picture.jpg"));
//            while ((len = inputStream.read(buf)) != -1) {
//                outputStream.write(buf, 0, len);
//            }

                    String s = "Hellow";
                    outputStream.write(s.getBytes(StandardCharsets.UTF_8));

                    outputStream.close();
//            inputStream.close();
                } catch (Throwable t) {
                    Utils.log(t.toString());
                    t.printStackTrace();
                    //catch logic
                }
            }
        }).start();
    }

}