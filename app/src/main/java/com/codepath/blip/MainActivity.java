package com.codepath.blip;

import android.app.Application;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.codepath.blip.clients.BackendClient;
import com.codepath.blip.models.Blip;
import com.parse.ParseObject;

import java.util.List;

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

        // Demo saving objects to Parse
         tempBackendMethod();

        // Demo receiving Blips via Behavior Subject
        tempListenForBlipsMethod();
        mBackendClient.updateBlips();
    }

    /**
     * Temp method showing how to listen to Blips from a BehaviorSubject.
     * Note that unlike cold observables, a BehaviorSubject doesn't do anything special when something subscribes.
     * It's contents are updated independently.
     */
    private void tempListenForBlipsMethod() {
        mBackendClient.getNearbyBlipsSubject().observeOn(AndroidSchedulers.mainThread()).subscribe(new Subscriber<List<Blip>>() {
            @Override
            public void onCompleted() {
                // Nothing
            }

            @Override
            public void onError(Throwable e) {
                Log.e("Error", "Something went horribly wrong while getting nearby Blips", e);
            }

            @Override
            public void onNext(List<Blip> blips) {
                if (blips != null) {
                    Toast.makeText(MainActivity.this, "Got a list of nearby Blips!", Toast.LENGTH_LONG).show();
                }
            }
        });
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
