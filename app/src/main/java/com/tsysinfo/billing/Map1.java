package com.tsysinfo.billing;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.tsysinfo.billing.database.AllCustomer;
import com.tsysinfo.billing.database.CustomerTable;
import com.tsysinfo.billing.database.DataBaseAdapter;
import com.tsysinfo.billing.database.Models;

public class Map1 extends Activity {

    DataBaseAdapter dba;
    Models mod;
    Double custLatitude;
    Double custLongitude;
    Double currLatitude;
    Double currLongitude;
    private GoogleMap map;

    GPSTracker gps;

    public Location mLastLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map1);

        dba = new DataBaseAdapter(this);
        mod = new Models();

        //replace GOOGLE MAP fragment in this Activity
        replaceMapFragment();
    }

    private void replaceMapFragment() {

        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map))
                .getMap();

        // Enable Zoom
        map.getUiSettings().setZoomGesturesEnabled(true);

        //set Map TYPE
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);


        dba.open();
        String sql = "select " + CustomerTable.KEY_LONGI + "," + CustomerTable.KEY_LATI + "," + CustomerTable.KEY_NAME + " from "
                + AllCustomer.DATABASE_TABLE + "";
        Cursor custCur = DataBaseAdapter.ourDatabase.rawQuery(sql, null);
        if (custCur.getCount() > 0) {

            while (custCur.moveToNext()) {
                custLongitude = custCur.getDouble(0);
                custLatitude = custCur.getDouble(1);
                LatLng loc = new LatLng(custLatitude, custLongitude);

                Marker marker;
                marker = map.addMarker(new MarkerOptions().position(loc).title(custCur.getString(2).trim()));
                marker.showInfoWindow();
            }
        }
        custCur.close();
        dba.close();
        gps = new GPSTracker(this);
        if (gps.canGetLocation()) {

            mLastLocation = LocationServices.FusedLocationApi
                    .getLastLocation(MainActivity.mGoogleApiClient);

            currLatitude = mLastLocation.getLatitude();
            currLongitude = mLastLocation.getLongitude();
            if(currLatitude != 0 || currLongitude != 0){
                Log.w("currLatitude: " + currLatitude, "currLongitude: "+currLongitude);
                LatLng loc1 = new LatLng(currLatitude, currLongitude);
                Marker marker;
                marker = map.addMarker(new MarkerOptions().position(loc1).title("You"));
                marker.showInfoWindow();
                marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
          //      map.animateCamera(CameraUpdateFactory.newLatLngZoom(loc1, 16.0f));
            }

        }

        /*Polyline line = map.addPolyline(new PolylineOptions()
                .add(new LatLng(51.5, -0.1), new LatLng(40.7, -74.0))
                .width(5)
                .color(Color.RED));*/



    }




    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(getApplicationContext(), CustomerActivity.class);
        intent.putExtra("act", "customer");
        startActivity(intent);
        finish();
    }
}
