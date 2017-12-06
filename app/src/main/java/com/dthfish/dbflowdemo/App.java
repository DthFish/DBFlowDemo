package com.dthfish.dbflowdemo;

import android.app.Application;

import com.raizlabs.android.dbflow.config.FlowManager;

/**
 * Description ${Desc}
 * Author DthFish
 * Date 2017/12/6.
 */

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FlowManager.init(this);
    }
}
