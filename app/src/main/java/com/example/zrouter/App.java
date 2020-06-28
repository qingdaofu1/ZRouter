package com.example.zrouter;

import android.app.Application;

import com.example.myzrouter.ZRouter;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ZRouter.init(this);
    }
}
