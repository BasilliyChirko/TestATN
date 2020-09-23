package basilliyc.chirkotestatn.client;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import net.alhazmy13.mediapicker.Image.ImagePicker;
import net.alhazmy13.mediapicker.Video.VideoPicker;

import java.io.File;
import java.util.List;

import basilliyc.chirkotestatn.R;
import basilliyc.chirkotestatn.base.BaseWorkActivity;
import basilliyc.chirkotestatn.utils.EditTextValidation;

public class ClientActivity extends BaseWorkActivity<ClientViewModel> {

    private static final int REQUEST_MEDIA = 100;

    private EditText inputIp;
    private TextView selectedMediaLabel;
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
        inputIp = (EditText) findViewById(R.id.editText);
        selectedMediaLabel = (TextView) findViewById(R.id.selected_media);
//        ((RecyclerView) findViewById(R.id.available_peers)).setAdapter(peersRecyclerAdapter);

        inputIp.setText(viewModel.getLastIp());
    }

    private void initListeners() {
        findViewById(R.id.conect).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String ip = EditTextValidation.validate(inputIp,
                        EditTextValidation.ErrorType.EMPTY,
                        EditTextValidation.ErrorType.INCORRECT_HOST_ADDRESS
                );
                viewModel.sendMedia(ip);
            }
        });

        findViewById(R.id.select_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectMedia(true);
            }
        });

        findViewById(R.id.select_video).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectMedia(false);
            }
        });

        peersRecyclerAdapter.onClickItemListener = new PeersRecyclerAdapter.OnClickItemListener() {
            @Override
            public void onClickItem(WifiP2pDevice item) {
                inputIp.setText(item.deviceAddress);
            }
        };
    }


    @Override
    public void setUpObservers() {
        super.setUpObservers();
        viewModel.selectedMedia.observe(this, new Observer<File>() {
            @Override
            public void onChanged(File file) {
                if (file != null && file.exists()) {
                    selectedMediaLabel.setText(file.getName());
                }
            }
        });
    }

    private void selectMedia(final boolean isImage) {
        withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, new PermissionCallback() {
            @Override
            public void onGranted() {
                if (isImage) {
                    new ImagePicker.Builder(ClientActivity.this)
                            .mode(ImagePicker.Mode.GALLERY)
                            .directory(ImagePicker.Directory.DEFAULT)
                            .allowMultipleImages(false)
                            .build();
                } else {
                    new VideoPicker.Builder(ClientActivity.this)
                            .mode(VideoPicker.Mode.GALLERY)
                            .directory(VideoPicker.Directory.DEFAULT)
                            .build();
                }
            }

            @Override
            public void showRationale() {
                new AlertDialog.Builder(ClientActivity.this)
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

        if (requestCode == ImagePicker.IMAGE_PICKER_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            List<String> mPaths = data.getStringArrayListExtra(ImagePicker.EXTRA_IMAGE_PATH);
            if (mPaths != null && !mPaths.isEmpty()) {
                viewModel.onMediaSelected(mPaths.get(0));
            }
        }

        if (requestCode == VideoPicker.VIDEO_PICKER_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            List<String> mPaths = data.getStringArrayListExtra(VideoPicker.EXTRA_VIDEO_PATH);
            if (mPaths != null && !mPaths.isEmpty()) {
                viewModel.onMediaSelected(mPaths.get(0));
            }
        }
    }


}