package com.codepath.blip;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;

/**
 * Created by melissahuang on 8/20/16.
 */
public class OnMap implements OnMapReadyCallback {

    private final Listener[] mListeners;

    public OnMap(Listener... listeners) {
        mListeners = listeners;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        for (Listener listener : mListeners) {
            listener.onMap(googleMap);
        }
    }

    public interface Listener {
        void onMap(GoogleMap map);
    }

}
