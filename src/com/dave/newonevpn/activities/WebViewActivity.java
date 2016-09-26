package com.dave.newonevpn.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.dave.newonevpn.model.Global;
import com.dave.newonevpn.netutil.CommonAsyncTask;
import com.dave.onevpnfresh.R;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by gustaf on 11/2/15.
 */
public class WebViewActivity extends Activity {


    private CommonAsyncTask mAsyncTask = null;
    private JSONArray objResArray;
    private JSONObject objRes;
    private String strRes;

    WebView webView;
    ProgressBar loading;
    ImageView imgback;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);


        webView = (WebView)findViewById(R.id.webView);
        loading = (ProgressBar)findViewById(R.id.loading);
        imgback = (ImageView)findViewById(R.id.imgBack);
        imgback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new AppWebViewClients());
        webView.loadUrl("http://signup.onevpn.com/?uniqueid=" + Global.deviceId);
    }
    public class AppWebViewClients extends WebViewClient {

        public AppWebViewClients() {
            loading.setVisibility(View.VISIBLE);
        }
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            // TODO Auto-generated method stub
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            // TODO Auto-generated method stub
            super.onPageFinished(view, url);
            loading.setVisibility(View.GONE);
        }
    }


}
