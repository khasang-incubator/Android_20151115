package com.khasang.forecast;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.khasang.forecast.location.*;


public class SplashScreenActivity
        extends AppCompatActivity
        implements LocationProvider.LocationCallback {

    private final static String TAG = SplashScreenActivity.class.getSimpleName();

    private Button chkButton;
    private LocationManager mAndroidLocationManager;
    private LocationProvider mLocationProvider;
    private LocationFactory mLocationFactory;

    private double mCurrentLatitude;
    private double mCurrentLongitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        mLocationProvider = new LocationProvider(this, this);
        mAndroidLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        mLocationFactory = new LocationFactory();

        chkButton = (Button) findViewById(R.id.chkLocatioBtn);
        chkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAndroidLocationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) {
                    GPSLocation currentGPSLocation = (GPSLocation) LocationFactory.getLocation(GPSLocation.factory);
                    currentGPSLocation.setLatitude(mCurrentLatitude);
                    currentGPSLocation.setLongitude(mCurrentLongitude);
                    Log.i(TAG, "Created gps location " + mCurrentLongitude + ", " + mCurrentLatitude);
                } else {
                    buildAlertMessageNoGps();
                }
            }
        });
    }

    private void buildAlertMessageNoGps() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .create()
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mLocationProvider.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mLocationProvider.disconnect();
    }

    @Override
    public void handleNewLocation(Location location) {
        Log.d(TAG, location.toString());

        mCurrentLatitude = location.getLatitude();
        mCurrentLongitude = location.getLongitude();
    }
}
