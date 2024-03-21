package com.tsysinfo.billing;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.tsysinfo.billing.database.DataBaseAdapter;
import com.tsysinfo.billing.database.Models;

public class GpsService extends Service implements LocationListener{

    private final Context mContext;
    DataBaseAdapter dba;
    Models mod;

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;

    public Location mLastLocation;
    double latitude; // latitude
    double longitude; // longitude

    // Google client to interact with Google API
    private GoogleApiClient mGoogleApiClient;

    // boolean flag to toggle periodic location updates
    private boolean mRequestingLocationUpdates = false;

    private LocationRequest mLocationRequest;

    // Location updates intervals in sec
    private static int UPDATE_INTERVAL = 5000; // 5 sec
    private static int FATEST_INTERVAL = 3000; // 3 sec
    private static int DISPLACEMENT = 1; // 1 meters


    public GpsService(Context context) {
        this.mContext = context;

        // First we need to check availability of play services
        if (checkPlayServices()) {

            // Building the GoogleApi client
            buildGoogleApiClient();

            createLocationRequest();
        }
        /*if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
        */
        mGoogleApiClient.connect();

        if (mGoogleApiClient.isConnected() && mRequestingLocationUpdates) {
            startLocationUpdates();
        }

        getLocation();

        //References
        dba = new DataBaseAdapter(context);
        mod = new Models();

    }

    @SuppressLint("MissingPermission")
    public Location getLocation(){

        if (mGoogleApiClient.isConnected()) {
            Log.w("GpsService","connected");
        }

        mLastLocation = LocationServices.FusedLocationApi
                .getLastLocation(mGoogleApiClient);

        return mLastLocation;
    }

    @SuppressLint("MissingPermission")
    public double getLatitude(){

        mLastLocation = LocationServices.FusedLocationApi
                .getLastLocation(mGoogleApiClient);

        if(mLastLocation != null){
            latitude = mLastLocation.getLatitude();
        }
        // return latitude
        return latitude;
    }
    /**
     * Function to get longitude
     * */
    public double getLongitude(){
        if(mLastLocation != null){

            mLastLocation = LocationServices.FusedLocationApi
                    .getLastLocation(mGoogleApiClient);

            longitude = mLastLocation.getLongitude();
        }
        // return longitude
        return longitude;
    }

    /**
     * Method to toggle periodic location updates
     * */
    private void togglePeriodicLocationUpdates() {
        if (!mRequestingLocationUpdates) {
            // Changing the button text
            mRequestingLocationUpdates = true;

            // Starting the location updates
            startLocationUpdates();

            Log.d("GpsService", "Periodic location updates started!");

        } else {
            // Changing the button text
            mRequestingLocationUpdates = false;

            // Stopping the location updates
            stopLocationUpdates();

            Log.d("GpsService", "Periodic location updates stopped!");
        }
    }

    /**
     * Creating google api client object
     * */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                .addApi(LocationServices.API).build();
    }

    /**
     * Creating location request object
     * */
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FATEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    /**
     * Method to verify google play services on the device
     * */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(mContext);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, (Activity) mContext,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Toast.makeText(getApplicationContext(),
                        "This device is not supported.", Toast.LENGTH_LONG)
                        .show();
            }
            return false;
        }
        return true;
    }

    /**
     * Starting the location updates
     * */
    @SuppressLint("MissingPermission")
    protected void startLocationUpdates() {

        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, (LocationListener) mContext);

    }

    /**
     * Stopping location updates
     */
    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }

    /**
     * Google api callback methods
     */
   /* @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.w("GpsService", "Connection failed: ConnectionResult.getErrorCode() = "
                + result.getErrorCode());
    }*/

  /*  @Override
    public void onConnected(Bundle arg0) {
        Log.w("loc", "Connected");
        // Once connected with google api, get the location

        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        mGoogleApiClient.connect();
    }
*/
    @Override
    public void onLocationChanged(Location location) {
        // Assign the new location
        mLastLocation = location;

        Toast.makeText(getApplicationContext(), "Location changed!",
                Toast.LENGTH_SHORT).show();

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}