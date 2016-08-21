package com.codepath.blip;

import android.Manifest;
import android.app.Application;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.codepath.blip.clients.BackendClient;
import com.codepath.blip.models.Blip;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.parse.ParseObject;

import java.util.List;

import javax.inject.Inject;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;


public class MainActivity extends AppCompatActivity implements
        OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, LocationListener, GoogleApiClient.OnConnectionFailedListener {
    @Inject Application mApplication;
    @Inject BackendClient mBackendClient;


    private LocationRequest mLocationRequest;
    private LatLng mLatLng;
    private GoogleMap mMap;
    private GoogleApiClient mClient;
    private final int MY_LOCATION_REQUEST_CODE = 101;

    private boolean didSetMapBounds = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((BlipApplication) getApplication()).getAppComponent().inject(this);
        setContentView(R.layout.activity_main);

        // Set up map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Demo saving objects to Parse
         tempBackendMethod();

        // Demo receiving Blips via Behavior Subject
        tempListenForBlipsMethod();
        mBackendClient.updateBlips();
    }

    private GoogleApiClient getGoogleApiClient() {
        return new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMinZoomPreference(17.0f);
        mMap.setMaxZoomPreference(17.0f);
        
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            String permission = Manifest.permission.ACCESS_FINE_LOCATION;
            ActivityCompat.requestPermissions(this, new String[]{permission}, MY_LOCATION_REQUEST_CODE);
        }

        mClient = getGoogleApiClient();

        if (mClient != null) {
            mClient.connect();
        }
    }

    @SuppressWarnings("MissingPermission")
    @Override
    public void onConnected(Bundle bundle) {
        Toast.makeText(this,"onConnected",Toast.LENGTH_SHORT).show();
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mClient);

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(15000); // 15 seconds
        mLocationRequest.setFastestInterval(10000); // 10 seconds
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        //mLocationRequest.setSmallestDisplacement(0.1F); //1/10 meter

        LocationServices.FusedLocationApi.requestLocationUpdates(mClient, mLocationRequest, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        double lat = location.getLatitude();
        double lng = location.getLongitude();
        double boundsEpsilon = 0.001;
        mLatLng = new LatLng(lat, lng);

        //zoom to current position:
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(mLatLng).zoom(17).build();

        mMap.animateCamera(CameraUpdateFactory
                .newCameraPosition(cameraPosition));

        if (!didSetMapBounds) {
            LatLngBounds region = new LatLngBounds(
                    new LatLng(lat - boundsEpsilon, lng - boundsEpsilon),
                    new LatLng(lat + boundsEpsilon, lng + boundsEpsilon));
            // Constrain the camera target
            mMap.setLatLngBoundsForCameraTarget(region);
            didSetMapBounds = true;
        }

        // unregister the listener after first location for now, do moving location later
        LocationServices.FusedLocationApi.removeLocationUpdates(mClient, this);

    }

    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(this,"onConnectionSuspended",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(this,"onConnectionFailed",Toast.LENGTH_SHORT).show();
    }

    @SuppressWarnings("MissingPermission")
    private void moveToUserLocation(GoogleApiClient client, GoogleMap map) {
        Location last = LocationServices.FusedLocationApi.getLastLocation(client);
        Toast.makeText(this, "last location" + last, Toast.LENGTH_LONG).show();
        if (last != null) {
            LatLng latLng = new LatLng(last.getLatitude(), last.getLongitude());
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 18);
            map.animateCamera(cameraUpdate);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            Log.e("Error", "Location permission denied");
        }
    }

    /**
     * Temp method showing how to listen to Blips from a BehaviorSubject.
     * Note that unlike cold observables, a BehaviorSubject doesn't do anything special when something subscribes.
     * Its contents are updated independently.
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
                    System.out.println("blip loc: " + blips.get(0).getLocation());
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
