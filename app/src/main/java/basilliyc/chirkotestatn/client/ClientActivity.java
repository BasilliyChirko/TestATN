package basilliyc.chirkotestatn.client;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.lifecycle.ViewModelProvider;

import basilliyc.chirkotestatn.R;
import basilliyc.chirkotestatn.base.BaseWorkActivity;
import basilliyc.chirkotestatn.server.SocketStatus;
import basilliyc.chirkotestatn.utils.EditTextValidation;

public class ClientActivity extends BaseWorkActivity<ClientViewModel> {

    private EditText inputIp;
    private Button submitClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setUpPage();
        initViews();
        initListeners();
    }

    @Override
    public int getLayout() {
        return R.layout.activity_client;
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
        inputIp = (EditText) findViewById(R.id.input_server_ip);
        inputIp.setText(viewModel.getLastIp());

        submitClient = (Button) findViewById(R.id.conect_to_server);
    }

    private void initListeners() {
        submitClient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String ip = EditTextValidation.validate(inputIp,
                        EditTextValidation.ErrorType.EMPTY,
                        EditTextValidation.ErrorType.INCORRECT_HOST_ADDRESS
                );
                viewModel.toggleConnection(ip);
            }
        });

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
                submitClient.setText(R.string.stop);
                break;
            case CONNECTED:
                submitClient.setText(R.string.stop);
                break;
            case DISCONNECTED:
                submitClient.setText(R.string.start);
                break;
            case DOWNLOADING:
                submitClient.setText(R.string.stop);
                break;
            case UPLOADING:
                submitClient.setText(R.string.stop);
                break;
        }
    }

}