package com.codepath.blip;

import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by melissahuang on 8/20/16.
 */

public class MoveToLocationFirstTime implements
        OnMap.Listener,
        OnClient.Listener {

    private final Bundle mSavedInstanceState;

    private GoogleApiClient mClient;
    private GoogleMap mGoogleMap;

    public MoveToLocationFirstTime(@Nullable Bundle savedInstanceState) {
        mSavedInstanceState = savedInstanceState;
    }

    // TODO Move map to current location
    // Use LocationServices' FusedLocationApi.
    // Get last location.
    // Move map with camera.
    // Use getCameraPosition helper method.
    @SuppressWarnings("MissingPermission")
    private void moveToUserLocation(GoogleApiClient client, GoogleMap map) {
        Log.e("Error", "Listener move called");

        Location last = LocationServices.FusedLocationApi.getLastLocation(client);
        LatLng latLng = new LatLng(last.getLatitude(), last.getLongitude());
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 18);
        map.animateCamera(cameraUpdate);
    }

    // TODO Build CameraPosition
    // Use CameraPosition.Builder.
    // Set target, zoom, and tilt (for 3d effect).
    private CameraPosition getCameraPosition(LatLng latLng) {
        return new CameraPosition.Builder().build();
    }

    private void check() {
        if (mSavedInstanceState == null &&
                mClient != null && mClient.isConnected() &&
                mGoogleMap != null) {
            moveToUserLocation(mClient, mGoogleMap);
        }
    }

    @Override
    public void onClient(@Nullable GoogleApiClient client) {
        mClient = client;
        check();
    }

    @Override
    public void onMap(GoogleMap map) {
        mGoogleMap = map;
        check();
    }

}