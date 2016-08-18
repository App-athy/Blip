package com.codepath.blip;

import android.app.Application;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import javax.inject.Inject;

public class MainActivity extends AppCompatActivity {
    @Inject Application mApplication;
    @Inject BackendClient mBackendClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((BlipApplication) getApplication()).getAppComponent().inject(this);
        setContentView(R.layout.activity_main);
    }
}
