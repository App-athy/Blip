package com.codepath.blip;

/**
 * Author: soroushmehraein
 * Project: Blip
 * Date: 8/17/16
 */
public class BackendClient {
    private String mFakeConfig;

    public BackendClient(String fakeConfig) {
        this.mFakeConfig = fakeConfig;
    }

    public String getFakeConfig() {
        return mFakeConfig;
    }
}
