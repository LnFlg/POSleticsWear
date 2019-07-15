package com.example.posleticswear;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
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

public class DiscoveryActivity extends WearableActivity implements SensorEventListener {


    private static final int REQUESTING_LOCATION_UPDATES_KEY_INT = 2;
    private SensorManager mSensorManager;
    private final float[] accelerometerReading = new float[3];
    private final float[] magnetometerReading = new float[3];

    private final float[] rotationMatrix = new float[9];
    private final float[] orientationAngles = new float[3];

    private FusedLocationProviderClient fusedLocationClient;
    private Location lastKnownLoc;
    private TextView distanceTextView;
    private Pos pos;
    private LocationRequest locationRequest;
    float currentDegree = 0f;
    private LocationCallback locationCallback;
    private boolean requestingLocationUpdates = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discovery);


        //Für Locationdata
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        //Für Locationdata
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);


        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Log.w("2", "onLocationResult");
                if (locationResult == null) {
                    Log.w("2", "onLocationResult was null");
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    lastKnownLoc=location;
                }}};
        updateLocation();
        startLocationUpdates();


        // Intent zurück in Pos überführen
        Intent i = getIntent();
        ArrayList <String> hashtagList = i.getStringArrayListExtra("hashtagList");

        pos = new Pos(i.getExtras().getDouble("lat"),i.getExtras().getDouble("long"),
                i.getExtras().getInt("id"),i.getExtras().getInt("upvotes") );
        pos.setHighestHashtags(hashtagList);



        TextView hashtag1= (TextView) findViewById(R.id.textView_Hashtag_1);
        if (pos.getHighestHashtags().get(0) != null) {
            hashtag1.setText(pos.getHighestHashtags().get(0));
        }

        TextView hashtag2= (TextView) findViewById(R.id.textView_Hashtag_2);
        if (pos.getHighestHashtags().get(0) != null) {
            hashtag2.setText(pos.getHighestHashtags().get(1));
        }


        //Distanzmarkeirung richtig setzen
        distanceTextView= (TextView) findViewById(R.id.textView_distance);
        distanceTextView.setText(getString(R.string.distance, lastKnownLoc.distanceTo(pos.getLoc())));
    }

    private void updateLocation() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, REQUESTING_LOCATION_UPDATES_KEY_INT);
            return;
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

    // Get readings from accelerometer and magnetometer.
    @Override
    public void onSensorChanged(SensorEvent event) {

    /*        if(lastKnownLoc.distanceTo(pos.getLoc())<=5.0){
                startActivity(new Intent(this, ConfirmFoundPosActivity.class).putExtra("id",pos.getId()));
            }

            //calc und set distanz, rotation
            distanceTextView.setText(lastKnownLoc.distanceTo(pos.getLoc()) + "m");

            //Button drehen
            float head = (float) Math.toDegrees(event.values[0]);
            float bearTo = lastKnownLoc.bearingTo(pos.getLoc());

            //bearTo = The angle from true north to the destination location from the point we're your currently standing.
            //head = The angle that you've rotated your phone from true north.

            if (bearTo < 0) {
                bearTo = bearTo + 360;
                //bearTo = -100 + 360  = 260;
            }

            float direction = bearTo - head;

            // If the direction is smaller than 0, add 360 to get the rotation clockwise.
            if (direction < 0) {
                direction = direction + 360;
            }

            RotateAnimation ra = new RotateAnimation(currentDegree, direction, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            ra.setDuration(210);
            ra.setFillAfter(true);

            findViewById(R.id.imageView_NavArrow).startAnimation(ra);

            currentDegree = direction;*/

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    private void updateOrientationAngles() {
        // Update rotation matrix, which is needed to update orientation angles.
        SensorManager.getRotationMatrix(rotationMatrix, null,
                accelerometerReading, magnetometerReading);
        SensorManager.getOrientation(rotationMatrix, orientationAngles);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // for the system's orientation sensor registered listeners
        Sensor accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometer != null) {
            mSensorManager.registerListener(this, accelerometer,
                    SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
        }
        Sensor magneticField = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if (magneticField != null) {
            mSensorManager.registerListener(this, magneticField,
                    SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
        }
        startLocationUpdates();
    }

    @Override
    protected void onPause() {
        super.onPause();

        // to stop the listener and save battery
        mSensorManager.unregisterListener(this);
        stopLocationUpdates();
    }


    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
    private void updateLastKnown() {
        try {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                lastKnownLoc = location;
                            }
                        }
                    });
        } catch (SecurityException e) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    RuntimeData.LOCATION_PERMISSION_CODE);
        }
    }




    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case RuntimeData.LOCATION_PERMISSION_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, do the
                    // location-related task
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        updateLastKnown();
                    }

                } else {RuntimeData.getInstance().setDisableLocationServices(true);}
                return;
            }

        }
    }
}
