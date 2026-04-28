package bw.base.root;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.webkit.JavascriptInterface;

public class JsInterface {
    public static final String PREFS_NAME = "app_config";
    public static final String KEY_ACTIVE_SERVER_URL = "active_server_url";

    private final Context context;

    public JsInterface(Context context) {
        this.context = context.getApplicationContext();
    }

    @JavascriptInterface
    public void postMessage(String name, String data) {
        Log.e("TAG", "postMessage name=="+name);
        Log.e("TAG", "postMessage data=="+data);
    }

    @JavascriptInterface
    public void setActiveServerUrl(String url) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_ACTIVE_SERVER_URL, url).apply();
    }
}
