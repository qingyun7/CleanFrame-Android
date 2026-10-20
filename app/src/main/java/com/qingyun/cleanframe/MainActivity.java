package com.qingyun.cleanframe;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Base64;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/** Offline Android host for the independently packaged CleanFrame interface. */
public final class MainActivity extends Activity {
    private static final String ASSET_ROOT = "file:///android_asset/cleanframe/";
    private static final String START_URL = ASSET_ROOT + "index.html";
    private WebView webView;
    private volatile boolean visible;
    private volatile String lastError = "";
    private boolean pageReady;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // Public release builds do not expose WebView inspection.
        WebView.setWebContentsDebuggingEnabled(BuildConfig.DEBUG);
        webView = new WebView(this);
        webView.setBackgroundColor(Color.rgb(17, 19, 24));
        webView.setFocusable(true);
        webView.setFocusableInTouchMode(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setAllowContentAccess(false);
        webView.getSettings().setAllowFileAccessFromFileURLs(false);
        webView.getSettings().setAllowUniversalAccessFromFileURLs(false);
        webView.getSettings().setMediaPlaybackRequiresUserGesture(true);
        webView.getSettings().setSupportMultipleWindows(false);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(false);
        webView.getSettings().setBuiltInZoomControls(false);
        webView.getSettings().setDisplayZoomControls(false);
        webView.addJavascriptInterface(new TvBridge(), "AndroidTV");
        webView.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return !localAsset(request.getUrl().toString());
            }
            @Override public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return !localAsset(url);
            }
            @Override public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                if (localAsset(url) || url.startsWith("data:")) return null;
                return new WebResourceResponse("text/plain", "UTF-8", 403, "Blocked",
                        java.util.Collections.emptyMap(), new ByteArrayInputStream(new byte[0]));
            }
            @Override public void onPageFinished(WebView view, String url) {
                if (START_URL.equals(url)) {
                    pageReady = true;
                    dispatchVisibility();
                    view.requestFocus();
                }
            }
        });
        setContentView(webView);
        immersive();
        webView.loadUrl(START_URL);
    }

    private static boolean localAsset(String url) {
        if (url == null || !url.startsWith(ASSET_ROOT)) return false;
        String path = Uri.parse(url).getPath();
        return path != null && !path.contains("/../") && !path.endsWith("/..")
                && !path.contains("\\");
    }

    private void immersive() {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
    }

    @Override public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) immersive();
    }

    @Override protected void onResume() {
        super.onResume();
        visible = true;
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (webView != null) {
            webView.onResume();
            dispatchVisibility();
            webView.requestFocus();
        }
        immersive();
    }

    @Override protected void onPause() {
        visible = false;
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (webView != null) {
            dispatchVisibility();
            webView.onPause();
        }
        super.onPause();
    }

    private void dispatchVisibility() {
        if (webView == null || !pageReady) return;
        // WebView on some TV firmware does not update Page Visibility on Activity changes.
        // Mirror Activity visibility into this local document so existing wallpaper timers pause.
        String script = "(function(){window.__cleanframeVisible=" + visible + ";"
                + "if(!window.__cleanframeVisibilityInstalled){try{"
                + "Object.defineProperty(document,'hidden',{configurable:true,get:function(){return !window.__cleanframeVisible;}});"
                + "Object.defineProperty(document,'visibilityState',{configurable:true,get:function(){return window.__cleanframeVisible?'visible':'hidden';}});"
                + "window.__cleanframeVisibilityInstalled=true;}catch(e){}}"
                + "document.dispatchEvent(new Event('visibilitychange'));"
                + "window.dispatchEvent(new Event(window.__cleanframeVisible?'focus':'blur'));"
                + "if(window.__cleanframeVisible&&typeof window.onActivityResume==='function'){window.onActivityResume();}"
                + "})();";
        webView.evaluateJavascript(script, null);
    }

    @Override protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        if (webView != null) {
            webView.requestFocus();
            if (pageReady && Intent.ACTION_MAIN.equals(intent.getAction())
                    && intent.hasCategory(Intent.CATEGORY_HOME)) {
                webView.evaluateJavascript("if(typeof window.onHomeIntent==='function'){window.onHomeIntent();}", null);
            }
        }
        immersive();
    }

    @Override public boolean dispatchKeyEvent(KeyEvent event) {
        int code;
        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_DPAD_LEFT: code = 37; break;
            case KeyEvent.KEYCODE_DPAD_UP: code = 38; break;
            case KeyEvent.KEYCODE_DPAD_RIGHT: code = 39; break;
            case KeyEvent.KEYCODE_DPAD_DOWN: code = 40; break;
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
            case KeyEvent.KEYCODE_NUMPAD_ENTER: code = 13; break;
            case KeyEvent.KEYCODE_BACK: code = 10009; break;
            default: return super.dispatchKeyEvent(event);
        }
        if (event.getAction() != KeyEvent.ACTION_DOWN && event.getAction() != KeyEvent.ACTION_UP) return true;
        if (webView != null && pageReady) {
            String type = event.getAction() == KeyEvent.ACTION_DOWN ? "keydown" : "keyup";
            String script = "(function(){var e=new Event('" + type + "',{bubbles:true,cancelable:true});"
                    + "e.keyCode=" + code + ";e.which=" + code + ";e.repeat=" + (event.getRepeatCount() > 0) + ";"
                    + "document.dispatchEvent(e);})();";
            webView.evaluateJavascript(script, null);
        }
        return true;
    }

    @Override public void onBackPressed() {
        // Back is a launcher navigation action. It must never reveal the disabled Xiaomi Home.
        if (webView != null && pageReady) {
            webView.evaluateJavascript("(function(){var e=new Event('keydown',{bubbles:true,cancelable:true});e.keyCode=10009;e.which=10009;document.dispatchEvent(e);var u=new Event('keyup',{bubbles:true});u.keyCode=10009;u.which=10009;document.dispatchEvent(u);})();", null);
        }
    }

    @Override protected void onDestroy() {
        if (webView != null) {
            webView.removeJavascriptInterface("AndroidTV");
            webView.stopLoading();
            webView.destroy();
            webView = null;
        }
        super.onDestroy();
    }

    private interface UiOperation { boolean run() throws Exception; }

    private boolean onUi(UiOperation operation) {
        CountDownLatch finished = new CountDownLatch(1);
        AtomicBoolean result = new AtomicBoolean(false);
        lastError = "";
        runOnUiThread(() -> {
            try {
                if (isFinishing() || isDestroyed()) {
                    lastError = "桌面暂时不可用，请按主页键重试。";
                } else result.set(operation.run());
            } catch (Exception e) {
                lastError = errorMessage(e);
            } finally { finished.countDown(); }
        });
        try {
            if (!finished.await(3, TimeUnit.SECONDS)) {
                lastError = "电视没有及时响应，请重试。";
                return false;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            lastError = "操作已中断，请重试。";
            return false;
        }
        return result.get();
    }

    private static String errorMessage(Exception e) {
        String message = e.getMessage();
        return message == null || message.isEmpty() ? e.getClass().getSimpleName() : message;
    }

    private boolean startResolved(Intent intent) {
        if (intent.resolveActivity(getPackageManager()) == null) return false;
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        return true;
    }

    private String appIcon(Drawable drawable) {
        if (drawable == null) return "";
        Bitmap bitmap = Bitmap.createBitmap(96, 96, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        int width = Math.max(1, drawable.getIntrinsicWidth());
        int height = Math.max(1, drawable.getIntrinsicHeight());
        float scale = Math.min(96f / width, 96f / height);
        int drawWidth = Math.max(1, Math.round(width * scale));
        int drawHeight = Math.max(1, Math.round(height * scale));
        int x = (96 - drawWidth) / 2;
        int y = (96 - drawHeight) / 2;
        drawable.setBounds(x, y, x + drawWidth, y + drawHeight);
        drawable.draw(canvas);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bytes);
        bitmap.recycle();
        return "data:image/png;base64," + Base64.encodeToString(bytes.toByteArray(), Base64.NO_WRAP);
    }

    private int appRank(String packageName) {
        switch (packageName) {
            case "com.ktcp.video": return 0;
            case "com.xiaodianshi.tv.yst": return 1;
            case "com.zentraedi.zspace.tv": return 2;
            case "org.xbmc.kodi": return 3;
            default: return 30;
        }
    }

    public final class TvBridge {
        @JavascriptInterface public String getApps() {
            lastError = "";
            JSONArray result = new JSONArray();
            PackageManager pm = getPackageManager();
            List<ResolveInfo> candidates = new ArrayList<>();
            Set<String> seen = new HashSet<>();
            try {
                candidates.addAll(pm.queryIntentActivities(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER), 0));
                candidates.addAll(pm.queryIntentActivities(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER), 0));
                candidates.sort(Comparator.comparingInt((ResolveInfo r) -> appRank(r.activityInfo.packageName))
                        .thenComparing(r -> String.valueOf(r.loadLabel(pm)), String.CASE_INSENSITIVE_ORDER));
                for (ResolveInfo info : candidates) {
                    String id = info.activityInfo.packageName;
                    ApplicationInfo app = info.activityInfo.applicationInfo;
                    if (id.equals(getPackageName()) || id.equals("com.gathin.tvhome") || !app.enabled || !info.activityInfo.enabled || seen.contains(id)) continue;
                    seen.add(id);
                    JSONObject item = new JSONObject();
                    item.put("appId", id);
                    item.put("name", String.valueOf(info.loadLabel(pm)));
                    item.put("rank", appRank(id));
                    try { item.put("icon", appIcon(info.loadIcon(pm))); }
                    catch (RuntimeException e) { item.put("icon", ""); }
                    result.put(item);
                }
            } catch (Exception e) { lastError = errorMessage(e); }
            return result.toString();
        }

        @JavascriptInterface public boolean launchApp(String packageName) {
            if (packageName == null || packageName.equals(getPackageName()) || !packageName.matches("[A-Za-z0-9_]+(\\.[A-Za-z0-9_]+)+")) {
                lastError = "无法打开这个应用。";
                return false;
            }
            return onUi(() -> {
                PackageManager pm = getPackageManager();
                ApplicationInfo app = pm.getApplicationInfo(packageName, 0);
                if (!app.enabled) { lastError = "这个应用已停用。"; return false; }
                Intent intent = pm.getLeanbackLaunchIntentForPackage(packageName);
                if (intent == null) intent = pm.getLaunchIntentForPackage(packageName);
                if (intent == null) { lastError = "这个应用没有可打开的电视页面。"; return false; }
                startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                return true;
            });
        }

        @JavascriptInterface public boolean openSettings() {
            return onUi(() -> {
                if (startResolved(new Intent("com.xiaomi.mitv.settings.SETTINGS").setPackage("com.xiaomi.mitv.settings"))) return true;
                if (startResolved(new Intent(Settings.ACTION_SETTINGS))) return true;
                lastError = "没有找到电视设置。";
                return false;
            });
        }

        @JavascriptInterface public boolean openWifi() {
            return onUi(() -> {
                if (startResolved(new Intent("com.xiaomi.mitv.settings.WIFI_SETTINGS").setPackage("com.xiaomi.mitv.settings"))) return true;
                if (startResolved(new Intent(Settings.ACTION_WIFI_SETTINGS))) return true;
                lastError = "没有找到网络设置。";
                return false;
            });
        }

        @JavascriptInterface public boolean openInputs() {
            return onUi(() -> {
                Intent intent = new Intent("com.xiaomi.mitv.settings.INPUTSOURCE_POPUP").setPackage("com.xiaomi.mitv.settings");
                if (getPackageManager().queryBroadcastReceivers(intent, 0).isEmpty()) {
                    lastError = "这台电视没有提供信号源菜单。";
                    return false;
                }
                sendBroadcast(intent);
                // Android cannot report whether the settings app displayed its menu.
                return true;
            });
        }

        @JavascriptInterface public String getLastError() { return lastError; }
        @JavascriptInterface public boolean isVisible() { return visible; }
    }
}
