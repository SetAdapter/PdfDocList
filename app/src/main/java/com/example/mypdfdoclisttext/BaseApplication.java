package com.example.mypdfdoclisttext;

import android.app.Application;
import android.content.Context;


public class BaseApplication extends Application {
    private String apiKey = "eUoiyr0v7khkaz3jrRqCiA9O";
    private String secretKey = "r3h62RoohVfDR8CLvcYHSaPH1KNf2wHX";
    private static BaseApplication mApplication;

    public static BaseApplication getApplication() {
        return mApplication;
    }

    private static Context context;

    public static Context getContext() {
        return context;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mApplication = this;
        context = getApplicationContext();

    }

}
