package com.codepath.blip.modules;

import com.codepath.blip.BackendClient;

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

    public BackendModule(String fakeConfig) {
        mBackendClient = new BackendClient(fakeConfig);
    }

    @Provides
    @Singleton
    BackendClient providesBackendClient() {
        return mBackendClient;
    }
}
