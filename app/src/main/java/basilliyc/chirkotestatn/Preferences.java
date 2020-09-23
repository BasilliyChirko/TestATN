package basilliyc.chirkotestatn;

import android.content.Context;
import android.content.SharedPreferences;

public class Preferences {

    private SharedPreferences pref;

    public Preferences(Context context) {
        pref = context.getSharedPreferences("pref", Context.MODE_PRIVATE);
    }

    public String getLastSendIp() {
        return pref.getString("LastSendIp", null);
    }

    public void setLastSendIp(String ip) {
        pref.edit().putString("LastSendIp", ip).apply();
    }


}
