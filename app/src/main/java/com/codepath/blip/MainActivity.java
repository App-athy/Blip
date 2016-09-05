package com.codepath.blip;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.codepath.blip.clients.BackendClient;
import com.codepath.blip.fragments.BlipListFragment;
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
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

import java.util.List;

import javax.inject.Inject;

import rx.Subscriber;
import rx.Subscription;


public class MainActivity extends AppCompatActivity implements
        OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, LocationListener, GoogleApiClient.OnConnectionFailedListener,
        ClusterManager.OnClusterItemClickListener<Blip>, ClusterManager.OnClusterClickListener<Blip> {
    @Inject BackendClient mBackendClient;

    private static final int MY_LOCATION_REQUEST_CODE = 101;
    private static final int LOGIN_REQUEST_CODE = 102;
    private static final int CREATE_BLIP_REQUEST_CODE = 103;
    private static final double BOUNDS_EPSILON = 0.0025;

    private ClusterManager<Blip> mClusterManager;
    private LocationRequest mLocationRequest;
    private LatLng mLatLng;
    private Subscription mNearbyBlips;

    public GoogleMap getMap() {
        return mMap;
    }

    private GoogleMap mMap;
    private GoogleApiClient mClient;

    private boolean didSetMapBounds = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((BlipApplication) getApplication()).getAppComponent().inject(this);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (!mBackendClient.isUserLoggedIn()) {
            launchLoginActivity();
        } else {
            initializeApplication();
        }
    }

    /**
     * Calls methods required to start the Blips application. Designed to be called from onCreate if user is already
     * logged in, or onActivityResult after the user logs in or registers through the log-in activity.
     */
    private void initializeApplication() {
        configureMap();
        subscribeToBlips();
        updateBlips();
        createFloatingActionButton();
    }

    /**
     * Launches activity to request user login or registration. Will not allow user to see the rest of the app unless
     * logged in. onActivityResult will loop repeatedly into this activity if login is not successful.
     */
    private void launchLoginActivity() {
        Intent i = new Intent(MainActivity.this, LoginActivity.class);
        startActivityForResult(i, LOGIN_REQUEST_CODE);
    }

    /**
     * Create FAB and assign onClick handler. This FAB will open the ComposeBlipActivity to allow users to create a
     * new Blip.
     */
    private void createFloatingActionButton() {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, ComposeBlipActivity.class);
                Bundle b = new Bundle();
                b.putParcelable("location", mLatLng);
                i.putExtra("bundle", b);
                startActivityForResult(i, CREATE_BLIP_REQUEST_CODE);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * Signals the backend client to update the NearbyBlipsSubject based on the current location and BOUNDS_EPSILON.
     * If mLatLng is not set, this method does nothing.
     * The updateBlips subscription should handle the successful or unsuccessful update of blips.
     */
    private void updateBlips() {
        if (mLatLng == null) {
            return;
        }
        mBackendClient.updateBlips(mLatLng, BOUNDS_EPSILON).subscribe(new Subscriber<Boolean>() {
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
                if (!aBoolean) {
                    Toast.makeText(MainActivity.this, "Failed to fetch new Blips!", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    /**
     * Retrieve and set up the main map for the application.
     */
    private void configureMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * Initializes Blips subscription. Configures the callback which occurs when the current list of nearby Blips is
     * updated. We also store the subscription here so we can un-subscribe when the activity ends to prevent a memory
     * leak.
     */
    private void subscribeToBlips() {
        mNearbyBlips = mBackendClient.getNearbyBlipsSubject().subscribe(new Subscriber<List<Blip>>() {
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
                    mClusterManager.clearItems();
                    mClusterManager.addItems(blips);
                    mClusterManager.cluster();
                }
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
        mMap.setMinZoomPreference(16.0f);

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

        mClient.connect();

        // Initialize the manager with the context and the map.
        // (Activity extends context, so we can pass 'this' in the constructor.)
        mClusterManager = new ClusterManager<Blip>(this, mMap);
        mClusterManager.setRenderer(new BlipRenderer());
        mClusterManager.setOnClusterClickListener(this);
        mClusterManager.setOnClusterItemClickListener(this);

        // Point the map's listeners at the listeners implemented by the cluster
        // manager.
        mMap.setOnMarkerClickListener(mClusterManager);

        // Add cluster items (markers) to the cluster manager.
        //addItems();
    }

    @SuppressWarnings("MissingPermission")
    @Override
    public void onConnected(Bundle bundle) {
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
        mLatLng = new LatLng(lat, lng);

        //zoom to current position:
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(mLatLng).zoom(16f).build();

        mMap.animateCamera(CameraUpdateFactory
                .newCameraPosition(cameraPosition));

        if (!didSetMapBounds) {
            LatLngBounds region = new LatLngBounds(
                    new LatLng(lat - BOUNDS_EPSILON, lng - BOUNDS_EPSILON),
                    new LatLng(lat + BOUNDS_EPSILON, lng + BOUNDS_EPSILON));
            // Constrain the camera target
            mMap.setLatLngBoundsForCameraTarget(region);
            didSetMapBounds = true;
        }
        updateBlips();

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

    // Clustering

    private class BlipRenderer extends DefaultClusterRenderer<Blip> {
        private BitmapDescriptor blipBitmap;
        private final int mDimension = 150;

        public BlipRenderer() {
            super(getApplicationContext(), getMap(), mClusterManager);
            Bitmap blipIcon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_blip2);
            blipIcon = Bitmap.createScaledBitmap(blipIcon, mDimension, mDimension, false);
            blipBitmap = BitmapDescriptorFactory.fromBitmap(blipIcon);
        }

        @Override
        protected void onBeforeClusterItemRendered(Blip blip, MarkerOptions markerOptions) {
            // Cache t
            markerOptions.icon(blipBitmap);
        }

        @Override
        protected boolean shouldRenderAsCluster(Cluster<Blip> cluster) {
            // start clustering if at least 2 items overlap
            return cluster.getSize() > 1;
        }
    }


    @Override
    public boolean onClusterClick(Cluster<Blip> cluster) {
        FragmentManager fm = getSupportFragmentManager();
        BlipListFragment blipListDialogFragment = BlipListFragment.newInstance(cluster.getItems());
        blipListDialogFragment.show(fm, "fragment_blip_list");
        return true;
    }

    @Override
    public boolean onClusterItemClick(Blip item) {
        FragmentManager fm = getSupportFragmentManager();
        BlipListFragment blipListDialogFragment = BlipListFragment.newInstance(item);
        blipListDialogFragment.show(fm, "fragment_blip_list");
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LOGIN_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                initializeApplication();
            } else {
                // Force user to login by repeatedly launching the activity until it returns successfully.
                // Ideally, we'd have some sort of home page or other intermediate screen to send them to rather than
                // just popping the same activity up again and again.
                launchLoginActivity();
            }
        } else if (requestCode == CREATE_BLIP_REQUEST_CODE && resultCode == RESULT_OK) {
            updateBlips();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mNearbyBlips != null) {
            mNearbyBlips.unsubscribe();
        }
    }

    public void onProfileView(MenuItem mi) {
        mBackendClient.getBlipsForUser().subscribe(new Subscriber<List<Blip>>() {
            @Override
            public void onCompleted() {
                // Nothing
            }

            @Override
            public void onError(Throwable e) {
                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onNext(List<Blip> blips) {
                FragmentManager fm = getSupportFragmentManager();
                BlipListFragment blipListDialogFragment = BlipListFragment.newInstance(blips);
                blipListDialogFragment.show(fm, "fragment_blip_list");
            }
        });
    }
}
