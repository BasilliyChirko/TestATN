package basilliyc.chirkotestatn.base;

import android.Manifest;
import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.Observer;

import net.alhazmy13.mediapicker.FileProcessing;

import java.io.File;
import java.util.ArrayList;

import basilliyc.chirkotestatn.Preferences;
import basilliyc.chirkotestatn.R;
import basilliyc.chirkotestatn.entity.LoadingMediaInfo;
import basilliyc.chirkotestatn.server.SocketStatus;
import basilliyc.chirkotestatn.utils.Error;
import basilliyc.chirkotestatn.utils.Utils;
import basilliyc.chirkotestatn.utils.action.ActionCallLiveData;

abstract public class BaseWorkActivity<T extends BaseWorkViewModel> extends BaseActivity {

    private static final int REQUEST_DIR = 1314;
    private static final int REQUEST_MEDIA = 100;

    protected T viewModel;

    protected TextView statusLabel;

    protected TextView loadingFileNameLabel;
    protected TextView loadingProgressLabel;
    protected ProgressBar progressBar;

    protected TextView selectedDirLabel;
    protected Button selectDirButton;

    protected TextView selectedMediaLabel;
    protected Button addImageButton;
    protected Button addVideoButton;
    protected Button clearMediaButton;
    protected Button transferButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayout());
        viewModel = createViewModel();
        viewModel.preferences = new Preferences(this);
        setUpObservers();
        initView();
        initListeners();

        String lastDir = viewModel.preferences.getLastDir();
        if (lastDir != null && !lastDir.isEmpty()) {
            viewModel.onDirSelected(lastDir);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        viewModel.disconnectSocket();
    }

    public abstract int getLayout();

    public void setUpPage() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }
    }

    protected abstract T createViewModel();


    private void initView() {
        statusLabel = ((TextView) findViewById(R.id.label_status));
        selectedDirLabel = ((TextView) findViewById(R.id.selected_dir));
        selectDirButton = ((Button) findViewById(R.id.select_dir));
        loadingFileNameLabel = ((TextView) findViewById(R.id.loading_file_name));
        loadingProgressLabel = ((TextView) findViewById(R.id.loading_file_size));
        progressBar = ((ProgressBar) findViewById(R.id.loading_progress));
        selectedMediaLabel = (TextView) findViewById(R.id.selected_media);
        addImageButton = ((Button) findViewById(R.id.add_image));
        addVideoButton = ((Button) findViewById(R.id.add_video));
        clearMediaButton = ((Button) findViewById(R.id.clear_selection));
        transferButton = ((Button) findViewById(R.id.transfer));
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
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

        addImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectMedia(true);
            }
        });

        addVideoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectMedia(false);
            }
        });

        clearMediaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewModel.clearMedia();
            }
        });

        transferButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewModel.sendMedia(BaseWorkActivity.this.getApplicationContext());
            }
        });
    }

    public void setUpObservers() {
        viewModel.error.onEvent(this, new ActionCallLiveData.Callback<Throwable>() {
            @Override
            public void onEvent(Throwable data) {
                onError(data);
            }
        });


        viewModel.socketStatus.observe(this, new Observer<SocketStatus>() {
            @Override
            public void onChanged(SocketStatus socketStatus) {
                onStatusChanged(socketStatus);
            }
        });

        viewModel.loadingProgress.observe(this, new Observer<LoadingMediaInfo>() {
            @Override
            public void onChanged(LoadingMediaInfo loadingMediaInfo) {
                onLoadingStateChanged(loadingMediaInfo);
            }
        });

        viewModel.selectedDir.observe(this, new Observer<File>() {
            @Override
            public void onChanged(File file) {
                if (file != null && file.exists()) {
                    selectedDirLabel.setText(file.getPath());
                }
            }
        });

        viewModel.selectedMedia.observe(this, new Observer<ArrayList<File>>() {
            @Override
            public void onChanged(ArrayList<File> list) {
                StringBuilder builder = new StringBuilder();
                if (list != null) {
                    for (File file : list) {
                        builder.append(file.getName());
                        builder.append("\n");
                    }
                    selectedMediaLabel.setText(builder.toString());
                }
                updateTransferButton(viewModel.socketStatus.getValue(), list);
            }
        });
    }

    public void onStatusChanged(SocketStatus status) {
        int res = 0;

        switch (status) {
            case WAIT_FOR_CLIENT:
                res = R.string.server_status_wait_for_data;
                break;
            case CONNECTED:
                res = R.string.server_status_connected;
                break;
            case DISCONNECTED:
                res = R.string.server_status_disconnected;
                break;
            case DOWNLOADING:
                res = R.string.server_status_downloading;
                break;
            case UPLOADING:
                res = R.string.server_status_uploading;
                break;
        }

        statusLabel.setText(res);
        updateVisibilityButtons(viewModel.loadingProgress.getValue(), status);
        updateTransferButton(status, viewModel.selectedMedia.getValue());
    }

    public void updateTransferButton(SocketStatus status, ArrayList<File> files) {
        if (status == null) {
            transferButton.setEnabled(false);
            return;
        }
        switch (status) {
            case WAIT_FOR_CLIENT:
                transferButton.setEnabled(false);
                break;
            case CONNECTED:
                transferButton.setEnabled(files != null && !files.isEmpty());
                break;
            case DISCONNECTED:
                transferButton.setEnabled(false);
                break;
            case DOWNLOADING:
                transferButton.setEnabled(false);
                break;
            case UPLOADING:
                transferButton.setEnabled(false);
                break;
        }
    }

    public void onLoadingStateChanged(LoadingMediaInfo loadingMediaInfo) {
        boolean isLoading = loadingMediaInfo != null && (
                viewModel.socketStatus.getValue() == SocketStatus.DOWNLOADING ||
                        viewModel.socketStatus.getValue() == SocketStatus.UPLOADING
        );

        loadingFileNameLabel.setText(isLoading ? loadingMediaInfo.getFileName() : null);
        loadingProgressLabel.setText(isLoading ? Utils.humanReadableByteCountBin(loadingMediaInfo.getFileLength()) : null);
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.INVISIBLE);

        if (isLoading) {
            double percent = ((double) loadingMediaInfo.getLoadingLength()) / ((double) loadingMediaInfo.getFileLength());
            int progress = (int) (percent * 100);
            progressBar.setProgress(progress);
        }

        updateVisibilityButtons(isLoading, viewModel.socketStatus.getValue());
    }

    private void updateVisibilityButtons(LoadingMediaInfo loadingMediaInfo, SocketStatus status) {
        boolean isLoading = loadingMediaInfo != null && (
                viewModel.socketStatus.getValue() == SocketStatus.DOWNLOADING ||
                        viewModel.socketStatus.getValue() == SocketStatus.UPLOADING
        );

        updateVisibilityButtons(isLoading, status);
    }

    public void updateVisibilityButtons(boolean isLoading, SocketStatus status) {
        addImageButton.setEnabled(!isLoading);
        addVideoButton.setEnabled(!isLoading);
        clearMediaButton.setEnabled(!isLoading);

        selectDirButton.setEnabled(!isLoading);
    }

    public void onError(Throwable throwable) {
        Utils.log(throwable);
        showAlert(Error.humanizeError(throwable));
    }

    public void showAlert(String text) {
        if (text == null || text.isEmpty()) {
            return;
        }

        new AlertDialog.Builder(this)
                .setMessage(text)
                .setPositiveButton(R.string.alert_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .show();
    }


    private void selectMedia(final boolean isImage) {
        withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, new PermissionCallback() {
            @Override
            public void onGranted() {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                if (isImage) {
                    intent.setType("image/*");
                } else {
                    intent.setType("video/*");
                }
                startActivityForResult(intent, REQUEST_MEDIA);
            }

            @Override
            public void showRationale() {
                new android.app.AlertDialog.Builder(BaseWorkActivity.this)
                        .setMessage(R.string.rationale_write_external)
                        .setPositiveButton(R.string.settings, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                        .create()
                        .show();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_DIR && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                viewModel.onDirSelected(getRealPathFromURIForDir(uri));
            }
        }

        if (requestCode == REQUEST_MEDIA && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            ClipData clipData = data.getClipData();

            if (clipData != null) {
                int count = clipData.getItemCount(); //evaluate the count before the for loop --- otherwise, the count is evaluated every loop.
                for (int i = 0; i < count; i++) {
                    Uri imageUri = clipData.getItemAt(i).getUri();
                    viewModel.onMediaSelected(FileProcessing.getPath(this, imageUri));
                }
                //do something with the image (save it to some directory or whatever you need to do with it here)
            } else if (uri != null) {
                viewModel.onMediaSelected(FileProcessing.getPath(this, uri));
            }


        }

    }

    private String getRealPathFromURIForDir(Uri uri) {
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
