package com.tsysinfo.billing;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

/**
 * Created by tsysinfo on 12/27/2016.
 */
public class MyApplication extends Application {
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}
