package com.example.myzrouter;

import android.content.Context;
import android.os.Bundle;

public class RouteManager {
    private Bundle mBundle = new Bundle();
    private String path;

    public RouteManager(String path) {
        this.path = path;
    }

    public Object navigation() {
        return navigation(null, mBundle);
    }

    public Object navigation(Context context, Bundle mBundle) {
        return ZRouter.getInstance().navigation(context, path, mBundle);
    }

    public RouteManager with(Bundle bundle) {
        if (null != bundle) {
            mBundle = bundle;
        }
        return this;
    }

    public RouteManager withString(String key, String value) {
        mBundle.putString(key, value);
        return this;
    }

    public RouteManager withBoolean(String key, boolean value) {
        mBundle.putBoolean(key, value);
        return this;
    }

    public RouteManager withInt(String key, int value) {
        mBundle.putInt(key, value);
        return this;
    }

}
