package com.dthfish.dbflowdemo;

import android.app.Application;

import com.raizlabs.android.dbflow.config.FlowConfig;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.config.ShipGeneratedDatabaseHolder;

/**
 * Description ${Desc}
 * Author DthFish
 * Date 2017/12/6.
 */

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FlowConfig flowConfig = new FlowConfig.Builder(this)
                .addDatabaseHolder(ShipGeneratedDatabaseHolder.class)
                .build();
        FlowManager.init(flowConfig);
    }
}
