package bw.base.root;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.RenderProcessGoneDetail;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;

import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.getcapacitor.BridgeActivity;
import com.getcapacitor.BridgeWebViewClient;

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

        configureWebViewForCloudflare(webView);

        webView.setWebViewClient(new BridgeWebViewClient(this.bridge) {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                String url = uri.toString();

                if (url.startsWith("https://accounts.google.com/")) {
                    // 開外部瀏覽器跑 OAuth
                    openOAuthInCustomTab(url);
                    return true; // 阻止 WebView 載入
                }

                String scheme = uri.getScheme();
                String serverUrl = getActiveServerHost();

                // 如果有呼叫 deep link
                if (scheme != null && scheme.equals(serverUrl)) {
                    return true; // 不要讓 WebView 嘗試載入
                }

                return super.shouldOverrideUrlLoading(view, request);
            }

            @Override
            public boolean onRenderProcessGone(WebView view, RenderProcessGoneDetail detail) {
                boolean didCrash = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && detail.didCrash();
                Log.e("MainActivity", "WebView render process gone, didCrash=" + didCrash);
                // 不回傳 true 的話 App 會跟著被 OS 殺掉。重建 Activity 讓 launcher 重跑一次。
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (!isFinishing() && !isDestroyed()) {
                        recreate();
                    }
                });
                return true;
            }
        });
        webView.addJavascriptInterface(new JsInterface(this), "jsBridge");
    }

    private void configureWebViewForCloudflare(WebView webView) {
        WebSettings settings = webView.getSettings();
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        // 拿掉 UA 裡的 "; wv)" 標記，避免被 Cloudflare 認定為 WebView 流量而觸發更嚴格的 challenge
        String ua = settings.getUserAgentString();
        if (ua != null && ua.contains("; wv)")) {
            settings.setUserAgentString(ua.replace("; wv)", ")"));
        }

        // Cloudflare challenge 過程會寫第三方 cookie，沒打開的話 cookie 寫不進去 → 永遠通不過 challenge → render 端容易掛
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.setAcceptThirdPartyCookies(webView, true);
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

            String serverUrl = getActiveServerHost();

            if (scheme != null && scheme.equals(serverUrl)) {
                String targetUrl = "https://" + serverUrl + "/callback?" + query;
                bridge.getWebView().loadUrl(targetUrl);
            }

            if (host != null && host.equals(serverUrl)) {
                bridge.getWebView().loadUrl(data.toString());
            }
        }
    }

    private String getActiveServerHost() {
        SharedPreferences prefs = getSharedPreferences(JsInterface.PREFS_NAME, MODE_PRIVATE);
        String stored = prefs.getString(JsInterface.KEY_ACTIVE_SERVER_URL, null);
        if (!TextUtils.isEmpty(stored)) {
            try {
                String host = Uri.parse(stored).getHost();
                if (!TextUtils.isEmpty(host)) {
                    return host;
                }
            } catch (Exception ignored) {}
        }
        return getString(R.string.server_url);
    }
}
