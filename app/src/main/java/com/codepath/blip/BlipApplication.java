package com.codepath.blip;

import android.app.Application;

import com.codepath.blip.modules.AppModule;
import com.codepath.blip.modules.BackendModule;

/**
 * Author: soroushmehraein
 * Project: Blip
 * Date: 8/17/16
 */
public class BlipApplication extends Application {

    private AppComponent mAppComponent;

    @Override
    public void onCreate() {
        super.onCreate();
        mAppComponent = DaggerAppComponent.builder()
                .appModule(new AppModule(this))
                .backendModule(new BackendModule("Hello!"))
                .build();
    }

    public AppComponent getAppComponent() {
        return mAppComponent;
    }
}
