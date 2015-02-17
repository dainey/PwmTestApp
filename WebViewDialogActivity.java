package com.verifone.vmf.testapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;

import com.verifone.vmf.api.VMF;
import com.verifone.vmf.api.VMF.UIReqListener;

@SuppressLint("SetJavaScriptEnabled")
public class WebViewDialogActivity extends Activity
{
  private WebView mWebView;

  @Override
  protected void onStart()
  {
    super.onStart();
    VMF.setUIReqListener(new WebViewUIReqReceiver());
  }

  @Override
  protected void onDestroy()
  {
    super.onDestroy();
    ((LinearLayout)mWebView.getParent()).removeAllViews();
    mWebView.destroy();
  }

  @Override
  public void onBackPressed()
  {
    //no cancelable dialog
  }

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    requestWindowFeature(Window.FEATURE_NO_TITLE);

    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_web_view_dialog);

    String data = null;
    Intent intent = getIntent();

    if (intent != null)
    {
      data = intent.getStringExtra("data");
    }

    mWebView = (WebView)findViewById(R.id.webView);

    mWebView.getSettings().setJavaScriptEnabled(true);
    mWebView.setWebViewClient(new WebViewClient()
    {

      public boolean shouldOverrideUrlLoading(WebView view, String url)
      {
        int paramPos = url.indexOf("?");

        if (paramPos != -1)
        {
          String params = url.substring(paramPos);
          Log.d("WebViewDialogActivity", "params: " + params);
          byte[] data = params.getBytes();
          int keepWebView = params.indexOf("KEEP_DISP");
          VMF.sendUIResponseData(data);

          if (keepWebView == -1)
          {
            finish();
          }

        }

        return true;
      }

    });

    mWebView.loadData(data, "text/html", "UTF-8");
  }

  private class WebViewUIReqReceiver implements UIReqListener
  {

    @Override
    public void onReceive(final byte[] uiReqData)
    {
      runOnUiThread(new Runnable()
      {

        @Override
        public void run()
        {
          mWebView.loadData(new String(uiReqData), "text/html", "UTF-8");
        }
      });
    }

  }
}
