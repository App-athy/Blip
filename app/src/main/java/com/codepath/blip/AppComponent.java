package com.codepath.blip;

import com.codepath.blip.modules.BackendModule;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Author: soroushmehraein
 * Project: Blip
 * Date: 8/17/16
 */
@Singleton
@Component(modules = {BackendModule.class})
public interface AppComponent {
    void inject(MainActivity activity);
}
