package com.monnfamily.enlibraryapp.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.monnfamily.enlibraryapp.R;
import com.monnfamily.enlibraryapp.Utils.AppManager;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PrivacyActivity extends AppCompatActivity {


    @BindView(R.id.toolBar_activity)
    Toolbar toolbar;
    @BindView(R.id.showPrivacy)
    WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Privacy");
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl("http://thelovelands.com/privacy/");
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
