package com.codepath.blip.modules;

import android.content.Context;

import com.codepath.blip.clients.BackendClient;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Author: soroushmehraein
 * Project: Blip
 * Date: 8/17/16
 */
@Module
public class BackendModule {
    BackendClient mBackendClient;

    public BackendModule(Context applicationContext) {
        mBackendClient = new BackendClient(applicationContext);
    }

    @Provides
    @Singleton
    BackendClient providesBackendClient() {
        return mBackendClient;
    }
}
