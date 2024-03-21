package com.tsysinfo.billing;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

public class GpsService1 extends Service implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private GoogleApiClient mLocationClient;

    private Location mCurrentLocation;
    LocationRequest mLocationRequest;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO do something useful

        mLocationClient.connect();


        Toast.makeText(this, "onStartCommand", Toast.LENGTH_SHORT).show();
        mLocationClient = new GoogleApiClient.Builder(GpsService1.this)
                .addApi(LocationServices.API).addConnectionCallbacks(GpsService1.this)
                .addOnConnectionFailedListener(GpsService1.this).build();

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        mLocationRequest.setFastestInterval(1000);
        return Service.START_NOT_STICKY;
    }

    @Override
    public void onLocationChanged(Location location) {
        // TODO Auto-generated method stub
        mCurrentLocation = location;
        Toast.makeText(this, mCurrentLocation.getLatitude() +", "+ mCurrentLocation.getLatitude(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(ConnectionResult arg0) {
        // TODO Auto-generated method stub
        Toast.makeText(this, "Connection failed", Toast.LENGTH_SHORT).show();
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onConnected(Bundle arg0) {
        // TODO Auto-generated method stub
        Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();
        //if(servicesConnected()) {
        LocationServices.FusedLocationApi.requestLocationUpdates(mLocationClient, mLocationRequest, this);
        //}

    }

    @Override
    public void onConnectionSuspended(int arg0) {
        // TODO Auto-generated method stub
        Toast.makeText(this, "Disconnected. Please re-connect.",
                Toast.LENGTH_SHORT).show();
    }



    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }
}