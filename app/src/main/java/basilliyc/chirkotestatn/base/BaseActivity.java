package basilliyc.chirkotestatn.base;

import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

abstract public class BaseActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST = 122;

    public interface PermissionCallback {
        void onGranted();

        void showRationale();
    }

    private PermissionCallback permissionCallback;

    protected void withPermission(String permission, PermissionCallback callback) {

        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                callback.showRationale();
            } else {
                permissionCallback = callback;
                ActivityCompat.requestPermissions(this, new String[]{permission}, PERMISSION_REQUEST);
            }
        } else {
            callback.onGranted();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && permissionCallback != null) {
            permissionCallback.onGranted();
            permissionCallback = null;
        }

    }
}
