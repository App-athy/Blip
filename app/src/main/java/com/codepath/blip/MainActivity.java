package com.codepath.blip;

import android.app.Application;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.codepath.blip.clients.BackendClient;
import com.parse.ParseObject;

import javax.inject.Inject;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;

public class MainActivity extends AppCompatActivity {
    @Inject Application mApplication;
    @Inject BackendClient mBackendClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((BlipApplication) getApplication()).getAppComponent().inject(this);
        setContentView(R.layout.activity_main);
        tempBackendMethod();
    }

    /**
     * Temp method showing how to interact with Rx and the backend client.
     */
    private void tempBackendMethod() {
        mBackendClient.postTestObjectToParse("Test", "Person", "Male").observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<ParseObject>() {
                    @Override
                    public void onCompleted() {
                        // Nothing
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("Error", "Something went horribly wrong while saving", e);
                    }

                    @Override
                    public void onNext(ParseObject parseObject) {
                        // Toast with object id as proof of save.
                        // Going forward, you'll receive a Blip object. For now, it's a generic Parse Object.
                        Toast.makeText(MainActivity.this, parseObject.getObjectId(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}
