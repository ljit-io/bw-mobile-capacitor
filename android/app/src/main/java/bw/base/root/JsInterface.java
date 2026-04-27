package bw.base.root;

import android.util.Log;
import android.webkit.JavascriptInterface;

public class JsInterface {
    @JavascriptInterface
    public void postMessage(String name, String data) {
        Log.e("TAG", "postMessage name=="+name);
        Log.e("TAG", "postMessage data=="+data);
    }
}
