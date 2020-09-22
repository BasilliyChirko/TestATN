package basilliyc.chirkotestatn.utils;

import android.util.Log;

public class Utils {

    private static String TAG = "AndroidRuntime";

    public static void log(Object x) {
        if (x == null) Log.i(TAG, "null");
        else Log.i(TAG, x.toString());
    }
}
