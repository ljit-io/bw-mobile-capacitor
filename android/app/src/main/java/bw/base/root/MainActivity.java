package bw.base.root;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    private static final int REQUEST_CODE_POST_NOTIFICATIONS = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        requestNotificationPermission();
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_CODE_POST_NOTIFICATIONS);
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        WebView webView = (WebView) this.bridge.getWebView();
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith("https://accounts.google.com/")) {
                    // 開外部瀏覽器跑 OAuth
                    openOAuthInCustomTab(url);
                    return true; // 阻止 WebView 載入
                }

                Uri uri = Uri.parse(url);
                String scheme = uri.getScheme();
                String serverUrl = getString(R.string.server_url);

                // 如果有呼叫 deep link
                if (scheme != null && scheme.equals(serverUrl)) {
                    return true; // 不要讓 WebView 嘗試載入
                }

                return false; // 繼續 WebView 正常流程
            }
        });
        webView.addJavascriptInterface(new JsInterface(), "jsBridge");
    }

    private void openOAuthInCustomTab(String url) {
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        CustomTabsIntent customTabsIntent = builder.build();
        customTabsIntent.launchUrl(this, Uri.parse(url));
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        Uri data = intent.getData();
        if (data != null) {
            String scheme = data.getScheme();
            String host   = data.getHost();
            String query = data.getQuery();

            String serverUrl = getString(R.string.server_url);

            if (scheme != null && scheme.equals(serverUrl)) {
                String targetUrl = "https://" + serverUrl + "/callback?" + query;
                bridge.getWebView().loadUrl(targetUrl);
            }

            if (host != null && host.equals(serverUrl)) {
                bridge.getWebView().loadUrl(data.toString());
            }
        }
    }
}
