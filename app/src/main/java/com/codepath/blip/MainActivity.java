package com.codepath.blip;

import android.Manifest;
import android.app.Application;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.codepath.blip.clients.BackendClient;
import com.codepath.blip.models.Blip;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.clustering.ClusterManager;

import java.util.List;

import javax.inject.Inject;

import rx.Subscriber;


public class MainActivity extends AppCompatActivity implements
        OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, LocationListener, GoogleApiClient.OnConnectionFailedListener {
    @Inject Application mApplication;
    @Inject BackendClient mBackendClient;


    private ClusterManager<Blip> mClusterManager;
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

        // Demo receiving Blips via Behavior Subject
        // REMOVE THIS ONCE YOU'RE READY TO ACTUALLY USE BLIPS
        tempListenForBlipsMethod();
        mBackendClient.updateBlips().subscribe(new Subscriber<Boolean>() {
            @Override
            public void onCompleted() {
                // Nothing
            }

            @Override
            public void onError(Throwable e) {
                // Nothing
            }

            @Override
            public void onNext(Boolean aBoolean) {
                if (aBoolean) {
                    Toast.makeText(MainActivity.this, "Fetched new Blips!", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(MainActivity.this, "Failed to fetch new Blips!", Toast.LENGTH_LONG).show();
                }
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, ComposeBlipActivity.class);
                Bundle b = new Bundle();
                b.putParcelable("location", mLatLng);
                i.putExtra("bundle", b);
                startActivityForResult(i, 2);
            }
        });
    }

    private GoogleApiClient getGoogleApiClient() {
        return new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        // Set up default map view preferences
        mMap = googleMap;
        mMap.setMinZoomPreference(17.0f);

        // Make sure location is enabled
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            String permission = Manifest.permission.ACCESS_FINE_LOCATION;
            ActivityCompat.requestPermissions(this, new String[]{permission}, MY_LOCATION_REQUEST_CODE);
        }

        // Set up client for my location
        mClient = getGoogleApiClient();

        if (mClient != null) {
            mClient.connect();
        }

        // Initialize the manager with the context and the map.
        // (Activity extends context, so we can pass 'this' in the constructor.)
        mClusterManager = new ClusterManager<Blip>(this, mMap);

        // Point the map's listeners at the listeners implemented by the cluster
        // manager.
        mMap.setOnMarkerClickListener(mClusterManager);

        // Add cluster items (markers) to the cluster manager.
        //addItems();
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
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this,"onConnectionFailed",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
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
        mBackendClient.getNearbyBlipsSubject().subscribe(new Subscriber<List<Blip>>() {
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

                    // uncomment when there is real location data
//                    for (Blip blip : blips) {
//                        mClusterManager.addItem(blip);
//                    }
                    Toast.makeText(MainActivity.this, "Got a list of nearby Blips!", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
