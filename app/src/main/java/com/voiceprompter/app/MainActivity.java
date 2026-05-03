package com.voiceprompter.app;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {
    private static final int PERM_REQ = 100;
    private WebView wv;
    private SpeechBridge sb;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        setContentView(R.layout.activity_main);
        wv = findViewById(R.id.webview);
        setupWebView();
        sb = new SpeechBridge(this, wv);
        wv.addJavascriptInterface(sb, "NativeSpeech");
        checkMicPerm();
    }

    private void setupWebView() {
        WebSettings s = wv.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setAllowFileAccess(true);
        s.setAllowContentAccess(true);
        s.setCacheMode(WebSettings.LOAD_DEFAULT);
        wv.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            WebView.setWebContentsDebuggingEnabled(true);
        wv.setWebChromeClient(new WebChromeClient());
        wv.setWebViewClient(new WebViewClient());
        wv.loadUrl("file:///android_asset/teleprompter.html");
    }

    private void checkMicPerm() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.RECORD_AUDIO}, PERM_REQ);
        else onPermGranted();
    }

    @Override
    public void onRequestPermissionsResult(int code, @NonNull String[] perms,
                                           @NonNull int[] results) {
        super.onRequestPermissionsResult(code, perms, results);
        if (code == PERM_REQ && results.length > 0)
            if (results[0] == PackageManager.PERMISSION_GRANTED) onPermGranted();
            else wv.evaluateJavascript(
                "javascript:window.onNativePermissionResult(false)", null);
    }

    private void onPermGranted() {
        wv.evaluateJavascript(
            "javascript:window.onNativePermissionResult(true)", null);
    }

    @Override
    protected void onResume() { super.onResume(); wv.onResume(); }
    @Override
    protected void onPause() { super.onPause(); wv.onPause(); }
    @Override
    protected void onDestroy() {
        if (sb != null) sb.stop();
        if (wv != null) wv.destroy();
        super.onDestroy();
    }
    @Override
    public void onBackPressed() { moveTaskToBack(true); }
}
