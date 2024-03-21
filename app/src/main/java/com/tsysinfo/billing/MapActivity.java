package com.tsysinfo.billing;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.tsysinfo.billing.database.AllCustomer;
import com.tsysinfo.billing.database.CustomerTable;
import com.tsysinfo.billing.database.DataBaseAdapter;
import com.tsysinfo.billing.database.Models;
import com.tsysinfo.billing.database.SessionManager;

public class MapActivity extends Activity {

    private TextView locationText;
    private TextView addressText;
    private GoogleMap map;

    DataBaseAdapter dba;
    Models mod;

    Double custLatitude;
    Double custLongitude;
    String custName;

    SessionManager session;
    GPSTracker gps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        dba = new DataBaseAdapter(this);
        mod = new Models();
        session = new SessionManager(this);
        gps = new GPSTracker(this);

        locationText = (TextView) findViewById(R.id.location);
        addressText = (TextView) findViewById(R.id.address);


        String custid = getIntent().getStringExtra("custid");
        dba.open();
        String sql = "select " + CustomerTable.KEY_LONGI + "," + CustomerTable.KEY_LATI + ","+CustomerTable.KEY_NAME+" from "
                + AllCustomer.DATABASE_TABLE + " where " + CustomerTable.KEY_ID + "='" + custid + "'";
        Cursor custCur = DataBaseAdapter.ourDatabase.rawQuery(sql, null);
        if (custCur.getCount() > 0) {
            custCur.moveToFirst();
            custLongitude = custCur.getDouble(0);
            custLatitude = custCur.getDouble(1);
            custName = custCur.getString(2).trim();
        }
        custCur.close();
        dba.close();

        Log.w("MapActivity", "Name: " + custName);
        Log.w("MapActivity", "custLongitude: " + custLongitude);
        Log.w("MapActivity", "custLatitude: " + custLatitude);

        //replace GOOGLE MAP fragment in this Activity
        if(custLatitude!=null && custLongitude!=null) {

            replaceMapFragment(custLongitude, custLatitude, custName);
        }else {

            replaceMapFragment(0.0, 0.0, custName);
        }
    }

    private void replaceMapFragment(double longitude, double latitude,String cName) {
        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map))
                .getMap();

        // Enable Zoom
        map.getUiSettings().setZoomGesturesEnabled(true);

        //set Map TYPE
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        //enable Current location Button
        //map.setMyLocationEnabled(true);


            if(gps.canGetLocation) {

                int val = (int) (Double.parseDouble(session.getDistance()) / 0.000621371);

                if(gps.getLatitude() != 0.0 && gps.getLongitude() != 0.0) {

                /*Circle circle = map.addCircle(new CircleOptions()
                        .center(new LatLng(latitude, longitude))
                        .radius(val)
                        .fillColor(R.color.cane)
                        .strokeColor(Color.BLUE));*/

                    map.addMarker(new MarkerOptions().position(new LatLng(gps.getLatitude(), gps.getLongitude())).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)).title("You")).showInfoWindow();
                    map.addCircle(new CircleOptions()
                            .center(new LatLng(latitude, longitude))
                            .radius(val)
                            .strokeWidth(1)
                            .strokeColor(Color.BLUE));
                }
            }




        //set "listener" for changing my location
     //       map.setOnMyLocationChangeListener(myLocationChangeListener());

        LatLng loc = new LatLng(latitude, longitude);

        map.addMarker(new MarkerOptions().position(loc).title(cName)).showInfoWindow();
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 16.0f));
        locationText.setText("Customer Location [" + longitude + " ; " + latitude + " ]");

        //get current address by invoke an AsyncTask object
        new GetAddressTask(MapActivity.this).execute(String.valueOf(latitude), String.valueOf(longitude));


    }



    public void callBackDataFromAsyncTask(String address) {
        addressText.setText(address);
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(getApplicationContext(),CustomerActivity.class);
        intent.putExtra("act", "customer");
        startActivity(intent);
        finish();
    }
}
