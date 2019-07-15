package com.example.posleticswear;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;

public class DiscoveryPictureActivity extends WearableActivity {


    private static final int REQUESTING_LOCATION_UPDATES_KEY_INT = 2;
    private LocationRequest locationRequest;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private Location lastKnownLoc;
    private boolean requestingLocationUpdates=false;
    private Pos pos;
    private TextView txt_distance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discovery_picture);

        Intent i = getIntent();
        ArrayList<String> hashtagList = i.getStringArrayListExtra("hashtagList");

        pos = new Pos(i.getExtras().getDouble("lat"),i.getExtras().getDouble("long"),
                i.getExtras().getInt("id"),i.getExtras().getInt("upvotes") );
        pos.setHighestHashtags(hashtagList);



        TextView hashtag1= (TextView) findViewById(R.id.textView_hashtags1_picture);
        if (pos.getHighestHashtags().get(0) != null) {
            hashtag1.setText(pos.getHighestHashtags().get(0));
        }

        TextView hashtag2= (TextView) findViewById(R.id.textView_hashtags2_picture);
        if (pos.getHighestHashtags().get(0) != null) {
            hashtag2.setText(pos.getHighestHashtags().get(1));
        }


        //Für Locationdata
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        updateLocation();
        startLocationUpdates();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {

                if (locationResult == null) {
                    Log.w("2", "onLocationResult was null");
                    return;
                }
                lastKnownLoc=locationResult.getLastLocation();
                Log.w("2", "onLocationResult:" + lastKnownLoc.getLatitude() + ", " +lastKnownLoc.getLongitude());

                // `lastKnownLoc` kann noch null sein, wenn der Sensor ein onChange triggert, die Location
                // der Uhr, aber noch nicht abgefragt wurde.
                if(lastKnownLoc == null)  return;
                txt_distance = findViewById(R.id.textView_distance_picture);
                txt_distance.setText(getString(R.string.distance, lastKnownLoc.distanceTo(pos.getLoc())));
                }
            };



        // Enables Always-on
        setAmbientEnabled();
    }

    private void updateLocation() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, REQUESTING_LOCATION_UPDATES_KEY_INT);
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            lastKnownLoc=location;
                            return;
                        }
                        Log.w("2", "getLastLocation returned null");
                    }
                });
    }

    private void startLocationUpdates() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, REQUESTING_LOCATION_UPDATES_KEY_INT);
            Log.w("2","permissions for location not granted in start location updates, requesting...");
            return;
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback,
                null );
        requestingLocationUpdates=true;
    }

    @Override
    protected void onResume() {
        super.onResume();

/*        // for the system's orientation sensor registered listeners
        Sensor accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometer != null) {
            mSensorManager.registerListener(this, accelerometer,
                    SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
        }
        Sensor magneticField = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if (magneticField != null) {
            mSensorManager.registerListener(this, magneticField,
                    SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);*/


        if (!requestingLocationUpdates) {
            startLocationUpdates();
        }

    }

    private void stopLocationUpdates() {
        if (fusedLocationClient != null && requestingLocationUpdates) {
            try {
                fusedLocationClient.removeLocationUpdates(locationCallback);
                requestingLocationUpdates=false;
            }
            catch (SecurityException exp) {
                Log.d("2", " Security exception while removeLocationUpdates");
            }
        }

    }

    @Override
    protected void onPause() {
        super.onPause();

        // to stop the listener and save battery
        // mSensorManager.unregisterListener(this);
        stopLocationUpdates();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUESTING_LOCATION_UPDATES_KEY_INT: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startLocationUpdates();
                    Log.i("2","Location permissions request accepted");
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                    }

                } else {RuntimeData.getInstance().setDisableLocationServices(true);}
                return;
            }

        }
    }
}
