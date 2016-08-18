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
                .backendModule(new BackendModule(getResources().getString(R.string.parse_url),
                        getResources().getString(R.string.parse_app_id), this))
                .build();
    }

    public AppComponent getAppComponent() {
        return mAppComponent;
    }
}
