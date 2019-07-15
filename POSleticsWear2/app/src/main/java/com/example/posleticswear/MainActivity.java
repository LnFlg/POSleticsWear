package com.example.posleticswear;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.Map;


public class MainActivity extends WearableActivity implements SensorEventListener {


    private static final String REQUESTING_LOCATION_UPDATES_KEY = "2";
    private static final int REQUESTING_LOCATION_UPDATES_KEY_INT = 2;
    private Button btn_pos;
    private SensorManager mSensorManager;
    private final float[] accelerometerReading = new float[3];
    private final float[] magnetometerReading = new float[3];

    private final float[] rotationMatrix = new float[9];
    private final float[] orientationAngles = new float[3];

    private static FusedLocationProviderClient fusedLocationClient;
    private Location lastKnownLoc;
    private Pos nextPosOnRoute;
    private TextView txt_distance;
    float currentDegree = 0f;

    private static LocationRequest locationRequest;
    private static LocationCallback locationCallback;
    private static boolean requestingLocationUpdates;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        updateValuesFromBundle(savedInstanceState);

        setContentView(R.layout.activity_main);


        //Für zeige auf nächsten pos
        txt_distance = (TextView) findViewById(R.id.textView_distance);
        btn_pos = (Button) findViewById(R.id.button_pos);
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        btn_pos.requestFocus();

        //Für Locationdata
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

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


        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    lastKnownLoc=location;
                }}};


        startLocationUpdates();

        txt_distance.setText("No route");

        NetworkSingleton.getInstance(this.getApplicationContext()).getRouteFromServer(RuntimeData.getInstance().getUserId());

        updateNextPos();


        // Enables Always-on
        setAmbientEnabled();
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

    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            return;
        }

        // Update the value of requestingLocationUpdates from the Bundle.
        if (savedInstanceState.keySet().contains(REQUESTING_LOCATION_UPDATES_KEY)) {
            requestingLocationUpdates = savedInstanceState.getBoolean(REQUESTING_LOCATION_UPDATES_KEY);
        }


    }


    // Get readings from accelerometer and magnetometer.
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (nextPosOnRoute!= null && !RuntimeData.getInstance().isDisableLocationServices()) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                System.arraycopy(event.values, 0, accelerometerReading,
                        0, accelerometerReading.length);
            } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                System.arraycopy(event.values, 0, magnetometerReading,
                        0, magnetometerReading.length);
            }

            updateOrientationAngles();

            //Vorbereitung für CheckClosestPos
            Map<Integer, Pos> notOnRouteList = RuntimeData.getInstance().getAllPos();
            //exclude pos on route
            for (Pos p : RuntimeData.getInstance().getRoute()){
                notOnRouteList.remove(p.getId(), p);
            }
            checkClosestPos((Map<Integer, Pos>) notOnRouteList);

            if(lastKnownLoc.distanceTo(nextPosOnRoute.getLoc())<=5.0){
                updateNextPos();
            }

             //calc und set distanz, rotation
            float[] distance = new float[1];
            Location.distanceBetween(
                    lastKnownLoc.getLatitude(),
                    lastKnownLoc.getLongitude(),
                    nextPosOnRoute.getLat(),
                    nextPosOnRoute.getLng(),
                    distance);
            txt_distance.setText(distance[0] + "m");


            //Button drehen
            float head = (float) Math.toDegrees(orientationAngles[0]);
            float bearTo = lastKnownLoc.bearingTo(nextPosOnRoute.getLoc());

            //bearTo = The angle from true north to the destination location from the point we're your currently standing.
            //head = The angle that you've rotated your phone from true north.

            GeomagneticField geoField = new GeomagneticField(Double.valueOf(lastKnownLoc.getLatitude()).floatValue(), Double
                    .valueOf(lastKnownLoc.getLongitude()).floatValue(),
                    Double.valueOf(lastKnownLoc.getAltitude()).floatValue(),
                    System.currentTimeMillis());
            head -= geoField.getDeclination(); // converts magnetic north into true north

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

            btn_pos.startAnimation(ra);

            currentDegree = direction;
        }
    }


    private void updateNextPos(){
        if(!RuntimeData.getInstance().getRoute().isEmpty() && !RuntimeData.getInstance().isDisableLocationServices()){
            nextPosOnRoute= RuntimeData.getInstance().getRoute().remove(0);
        }else {
            RuntimeData.getInstance().setDiscoveryRadius(750d);
        }
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
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_GAME);

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
        mSensorManager.unregisterListener(this);
        stopLocationUpdates();
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(REQUESTING_LOCATION_UPDATES_KEY,
                requestingLocationUpdates);
        // ...
        super.onSaveInstanceState(outState);
    }




    public void sendPOS(View view){
        if (!RuntimeData.getInstance().isDisableLocationServices()) {
            Location locTimestamp = lastKnownLoc;
            boolean newPos=true;

            //check if there is a POS already arround to decide if upvote or new pos
            for(Map.Entry<Integer, Pos> entry : RuntimeData.getInstance().getAllPos().entrySet()) {

                //pos existiert schon (umkreis von 15 m ) -> finde id, upvote und verhindere neues erstellen
                if(entry.getValue().equals(locTimestamp)){
                    newPos=false;
                    NetworkSingleton.getInstance(this.getApplicationContext()).upvotePos(entry.getValue().getId());
                }
            }

            if(newPos) {
                if(lastKnownLoc==null){Log.w("2","Lastknownloc is null"); return;}
                NetworkSingleton.getInstance(this.getApplicationContext()).sendPOS(
                        lastKnownLoc.getLatitude(),
                        lastKnownLoc.getLongitude(),
                        RuntimeData.getInstance().getUserId()
                );
            }
        }
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

    private void checkClosestPos( Map<Integer,Pos> posList ){
        Location locSnapshot = lastKnownLoc;

        //initialize random closest
        double minDistance = locSnapshot.distanceTo((Location) posList.values().toArray()[0]);
        Location closestLoc = new Location((Location) posList.values().toArray()[0]);
        Pos closestPos = (Pos) posList. values().toArray()[0];


        for(Map.Entry<Integer,Pos> entry : posList.entrySet()){
            if(entry.getValue().getLoc().distanceTo(locSnapshot)<minDistance){
                minDistance = entry.getValue().getLoc().distanceTo(locSnapshot);
                closestLoc = entry.getValue().getLoc();
                closestPos = (Pos) entry.getValue();
            }
        }

        if((closestPos != null) && ((minDistance - 1) < RuntimeData.getInstance().getDiscoveryRadius())){

            if(closestPos.getUpvotes()<3) {





                // Vibrate for 500 milliseconds
                Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                    try {
                        v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                }
                Intent intent = new Intent(MainActivity.this, DiscoveryActivity.class);
                //Daten einzeln dranhängen, da Pos Datentyp nicht möglich
                intent.putExtra("lat", closestLoc.getLatitude());
                intent.putExtra("long", closestLoc.getLongitude());
                intent.putExtra("id", closestPos.getId());
                intent.putExtra("upvotes",closestPos.getUpvotes());
                intent.putStringArrayListExtra("hashtagList", closestPos.getHighestHashtags());
                startActivity(intent);
            }
            else{
                posList.remove(closestPos.getId(), closestPos);
                //rekursivere Aufruf mit verkürtzer Liste
                checkClosestPos(posList);
            }
        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if (event.getRepeatCount() == 0) {
            if (keyCode == KeyEvent.KEYCODE_STEM_1) {
                startActivity(new Intent (this, SettingsActivity.class));
            }
        }
        return super.onKeyDown(keyCode, event);
    }



    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

}
