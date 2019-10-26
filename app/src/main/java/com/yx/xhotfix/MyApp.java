package com.yx.xhotfix;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;


/**
 * Author by YX, Date on 2019/10/25.
 */
public class MyApp extends Application{

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected void attachBaseContext(Context base) {
        MultiDex.install(base);
        FixManager.loadDex(base);
        super.attachBaseContext(base);
    }
}
