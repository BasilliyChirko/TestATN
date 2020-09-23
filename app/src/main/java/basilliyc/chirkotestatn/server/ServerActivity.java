package basilliyc.chirkotestatn.server;

import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.text.format.Formatter;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import java.io.File;

import basilliyc.chirkotestatn.R;
import basilliyc.chirkotestatn.base.BaseWorkActivity;
import basilliyc.chirkotestatn.entity.LoadingMediaInfo;
import basilliyc.chirkotestatn.utils.Utils;

public class ServerActivity extends BaseWorkActivity<ServerViewModel> {

    private static final int REQUEST_DIR = 1314;

    private TextView selectedDirLabel;
    private TextView serverStatusLabel;
    private Button submitServerButton;
    private Button selectDirButton;
    private TextView loadingFileNameLabel;
    private TextView loadingProgressLabel;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);
        initView();
        initListeners();
        setUpPage();

        String lastDir = viewModel.preferences.getLastDir();
        if (lastDir != null && !lastDir.isEmpty()) {
            viewModel.onDirSelected(lastDir, false);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        viewModel.stopServerSocket();
    }

    @Override
    protected ServerViewModel createViewModel() {
        return new ViewModelProvider(this).get(ServerViewModel.class);
    }

    private void initView() {
        selectedDirLabel = ((TextView) findViewById(R.id.selected_dir));
        serverStatusLabel = ((TextView) findViewById(R.id.server_status));
        submitServerButton = ((Button) findViewById(R.id.submit_server));
        selectDirButton = ((Button) findViewById(R.id.select_dir));
        loadingFileNameLabel = ((TextView) findViewById(R.id.loading_file_name));
        loadingProgressLabel = ((TextView) findViewById(R.id.loading_progress));
        progressBar = ((ProgressBar) findViewById(R.id.progressBar));
    }

    private void initListeners() {
        selectDirButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                i.addCategory(Intent.CATEGORY_DEFAULT);
                startActivityForResult(Intent.createChooser(i, "Choose directory"), REQUEST_DIR);
            }
        });

        submitServerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewModel.toggleServerWork();
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
        ((TextView) findViewById(R.id.server_label_ip)).setText(address);
    }

    private String getLocalIpAddress() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        return Formatter.formatIpAddress(wifiManager.getConnectionInfo().getIpAddress());
    }

    @Override
    public void setUpObservers() {
        super.setUpObservers();
        viewModel.selectedDir.observe(this, new Observer<File>() {
            @Override
            public void onChanged(File file) {
                if (file != null && file.exists()) {
                    selectedDirLabel.setText(file.getPath());
                }
            }
        });

        viewModel.serverStatus.observe(this, new Observer<ServerStatus>() {
            @Override
            public void onChanged(ServerStatus serverStatus) {
                int res = 0;

                switch (serverStatus) {
                    case DIR_NOT_SELECTED:
                        submitServerButton.setText(R.string.start);
                        res = R.string.server_status_dir_not_selected;
                        break;
                    case WAIT_FOR_DATA:
                        submitServerButton.setText(R.string.stop);
                        res = R.string.server_status_wait_for_data;
                        break;
                    case STOPPED:
                        submitServerButton.setText(R.string.start);
                        res = R.string.server_status_stopped;
                        break;
                    case LOADING:
                        submitServerButton.setText(R.string.stop);
                        res = R.string.server_status_loading;
                        break;
                }

                serverStatusLabel.setText(res);
            }
        });

        viewModel.loadingProgress.observe(this, new Observer<LoadingMediaInfo>() {
            @Override
            public void onChanged(LoadingMediaInfo loadingMediaInfo) {
                boolean showLoading = loadingMediaInfo != null && viewModel.serverStatus.getValue() == ServerStatus.LOADING;

                selectDirButton.setEnabled(!showLoading);
                loadingFileNameLabel.setText(showLoading ? loadingMediaInfo.getFileName() : null);
                loadingProgressLabel.setText(showLoading ? Utils.humanReadableByteCountBin(loadingMediaInfo.getFileLength()) : null);
                progressBar.setVisibility(showLoading ? View.VISIBLE : View.INVISIBLE);

                if (showLoading) {
                    double percent = ((double) loadingMediaInfo.getLoadingLength()) / ((double) loadingMediaInfo.getFileLength());
                    int progress = (int) (percent * 100);
                    progressBar.setProgress(progress);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_DIR && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                viewModel.onDirSelected(getRealPathFromURI(uri), true);
            }
        }
    }

    private String getRealPathFromURI(Uri uri) {
        String lastPathSegment = uri.getLastPathSegment(); //primary:Download/ttt
        if (lastPathSegment == null) return null;

        String[] split = lastPathSegment.split(":");
        if (split.length < 2) return null;

        String partPath = split[1]; //Download/ttt

        String authority = uri.getAuthority();
        if (authority == null) return null;

        boolean isExternal = authority.contains("com.android.externalstorage.documents");

        if (isExternal) {
            return Environment.getExternalStorageDirectory() + "/" + partPath;
        } else {
            return getFilesDir().getPath() + "/" + partPath;
        }
    }
}