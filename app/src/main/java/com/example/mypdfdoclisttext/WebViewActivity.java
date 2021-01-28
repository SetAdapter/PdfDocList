package com.example.mypdfdoclisttext;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.joanzapata.pdfview.PDFView;

import java.io.File;

public class WebViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        Intent intent = getIntent();
        if (intent != null) {
            String path = intent.getStringExtra("path");
            if (!TextUtils.isEmpty(path)) {
                PDFView pdfView = findViewById(R.id.pdfView);
                File file = FileUtils.getFile(path);
                pdfView.fromFile(file)
                        //.fromFile("")指定加载某个文件
                        //指定加载某一页
                        /*.pages(0, 1,2, 3, 4, 5)*/
                        .defaultPage(1)
                        .showMinimap(false)
                        .enableSwipe(true)
                        /* .onDraw(onDraw)
                           .onLoad(onLoadCompleteListener)
                           .onPageChange(onPageChangeListener)*/
                        .load();
            } else {
                Toast.makeText(this, "地址异常", Toast.LENGTH_SHORT).show();
            }
        }


    }


    public class AppWebViewClients extends WebViewClient {

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

        }
    }
}
