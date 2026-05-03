package com.voiceprompter.app;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import java.util.ArrayList;
import java.util.Locale;

public class SpeechBridge {
    private static final String TAG = "SpeechBridge";
    private final WebView wv;
    private final MainActivity act;
    private SpeechRecognizer rec;
    private boolean listening = false;

    public SpeechBridge(MainActivity a, WebView w) { act = a; wv = w; }

    @JavascriptInterface
    public void initRecognizer() {
        act.runOnUiThread(() -> {
            try {
                if (rec != null) { rec.destroy(); }
                rec = SpeechRecognizer.createSpeechRecognizer(act);
                rec.setRecognitionListener(new Listener());
            } catch (Exception e) { Log.e(TAG, "init fail", e); }
        });
    }

    @JavascriptInterface
    public void startListening() {
        if (listening) return;
        act.runOnUiThread(() -> {
            try {
                Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                i.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
                rec.startListening(i);
                listening = true;
                js("onSpeechBegin()");
            } catch (Exception e) { Log.e(TAG, "start fail", e); }
        });
    }

    @JavascriptInterface
    public void stopListening() {
        listening = false;
        act.runOnUiThread(() -> {
            if (rec != null) { try { rec.stopListening(); } catch(Exception e) {} }
        });
    }

    @JavascriptInterface
    public void startVAD() {}
    @JavascriptInterface
    public void stopVAD() {}
    @JavascriptInterface public float getEnergy() { return 0; }
    @JavascriptInterface public long getLastActive() { return 0; }

    public void stop() { listening = false; if (rec != null) rec.destroy(); }

    private void js(String s) {
        act.runOnUiThread(() -> wv.evaluateJavascript("javascript:" + s, null));
    }

    private class Listener implements RecognitionListener {
        @Override public void onReadyForSpeech(Bundle p) {}
        @Override public void onBeginningOfSpeech() { js("onSpeechBegin()"); }
        @Override public void onRmsChanged(float v) {}
        @Override public void onBufferReceived(byte[] b) {}
        @Override public void onEndOfSpeech() { js("onSpeechEnd()"); }
        @Override public void onResults(Bundle r) {
            ArrayList<String> m = r.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            if (m != null && !m.isEmpty()) js("onSpeechResult('" + esc(m.get(0)) + "')");
            if (listening) startListening();
        }
        @Override public void onPartialResults(Bundle p) {
            ArrayList<String> m = p.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            if (m != null && !m.isEmpty()) js("onSpeechPartial('" + esc(m.get(0)) + "')");
        }
        @Override public void onEvent(int t, Bundle p) {}
        @Override public void onError(int e) {
            if (e == SpeechRecognizer.ERROR_NO_MATCH || e == SpeechRecognizer.ERROR_SPEECH_TIMEOUT) {
                if (listening) startListening();
            } else {
                js("onSpeechError('" + getErr(e) + "')");
                listening = false;
            }
        }
        private String esc(String s) {
            return s.replace("\\","\\\\").replace("'","\\'").replace("\n","\\n");
        }
        private String getErr(int c) {
            switch (c) {
                case SpeechRecognizer.ERROR_AUDIO: return "音频错误";
                case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS: return "无权限";
                case SpeechRecognizer.ERROR_NETWORK: return "网络错误";
                case SpeechRecognizer.ERROR_NETWORK_TIMEOUT: return "网络超时";
                case SpeechRecognizer.ERROR_NO_MATCH: return "未识别";
                case SpeechRecognizer.ERROR_SPEECH_TIMEOUT: return "超时";
                default: return "错误(" + c + ")";
            }
        }
    }
}
