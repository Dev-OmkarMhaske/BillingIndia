package com.tsysinfo.billing;


import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.LocationServices;
import com.tsysinfo.billing.database.DataBaseAdapter;
import com.tsysinfo.billing.database.LocationTable;
import com.tsysinfo.billing.database.Models;
import com.tsysinfo.billing.database.SessionLocation;
import com.tsysinfo.billing.database.SessionManager;
import com.tsysinfo.billing.database.TempLocationTable;
import com.tsysinfo.billing.database.WebService;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class LocationReceiver extends BroadcastReceiver {

    public static Context conn = null;
    public Location mLastLocation;
    SessionManager session;
    SessionLocation locSession;
    DataBaseAdapter dba;
    Models mod;

    @Override
    public void onReceive(Context context, Intent arg1) {
        // For our recurring task, we'll just display a message
        //	Toast.makeText(context, "I'm running", Toast.LENGTH_SHORT).show();
        this.conn = context;

        session = new SessionManager(context);
        dba = new DataBaseAdapter(context);
        mod = new Models();
        locSession = new SessionLocation(context);
        Log.w("LocationReceiver", ".1");
        try {

            if (MainActivity.mGoogleApiClient != null) {
                MainActivity.mGoogleApiClient.connect();
            }

            if (MainActivity.flagApiIsConnected == 1) {

                //        Log.w("LR", "LR Start");
                Log.w("LocationReceiver", ".2");

                mLastLocation = LocationServices.FusedLocationApi
                        .getLastLocation(MainActivity.mGoogleApiClient);

                GPSTracker gps = new GPSTracker(context);
                gps.getLocation();
                //    gpsService = new GpsService(context);

                if (gps.canGetLocation()) {
                    double latitude = gps.getLatitude();
                    double longitude = gps.getLongitude();

                    Calendar c = Calendar.getInstance();
                    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                    String strDate = sdf.format(c.getTime());
                    Log.w("LocationReceiver", ".....");
                    Log.w("LocationReceiver", "Tracker CurrentLongitude: " + longitude);
                    Log.w("LocationReceiver", "Tracker CurrentLatitude: " + latitude);
                    Log.w("LocationReceiver", "Tracker CurrentDATETIME: " + strDate);
           /*     Log.w("LocationReceiver", "API CurrentLongitude: " + mLastLocation.getLongitude());
                Log.w("LocationReceiver", "API CurrentLatitude: " + mLastLocation.getLatitude());*/

                    ContentValues cv = new ContentValues();
                    cv.put(LocationTable.KEY_LONGITUDE, longitude);
                    cv.put(LocationTable.KEY_LATITUDE, latitude);
                    cv.put(LocationTable.KEY_DATE, strDate);

                    if (latitude != 0 || longitude != 0) {
                        if (locSession.isLoggedIn()) {
                            if (latitude != Double.parseDouble(locSession.getLati()) || longitude != Double.parseDouble(locSession.getLongi())) {

                                //                     Toast.makeText(context, "LR Location changed! Longitude: " + longitude + "\nLatitude: " + latitude, Toast.LENGTH_SHORT).show();

                                String str = "Longitude: " + longitude + " Latitude: " + latitude + " Time: " + strDate;

                                Log.e("Tracking","date"+str);

                                Toast.makeText(conn , "Tracking : "+ str.toString(), Toast.LENGTH_SHORT).show();

                                LogError lErr2 = new LogError();
                                lErr2.appendLog("Tracking : "+str);

                                locSession.logoutUser();
                                locSession.createLocationSession(String.valueOf(longitude), String.valueOf(latitude),strDate);
                                dba.open();
                                mod.insertdata(LocationTable.DATABASE_TABLE, cv);
                                Log.e("insert",""+cv);
                                dba.close();


                                String data=cv.toString();
                                ContentValues cv1 = new ContentValues();
                                cv1.put(TempLocationTable.KEY_DATA,data);
                                dba.open();
                                mod.insertdata(TempLocationTable.DATABASE_TABLE, cv1);
                                Log.e("insertTemp",""+cv1);
                                dba.close();


                            }
                        } else {
                            locSession.createLocationSession(String.valueOf(longitude), String.valueOf(latitude),strDate);
                            dba.open();
                            mod.insertdata(LocationTable.DATABASE_TABLE, cv);
                            Log.e("insert1",""+cv);

                            dba.close();
                        }
                    }else{

                        if (locSession.isLoggedIn()) {

                            Date dtPrev = sdf.parse(locSession.getDateTime());
                            Date dtNow = sdf.parse(strDate);
                            long diff = dtPrev.getTime() - dtNow.getTime();
                            long diffInSec = TimeUnit.MILLISECONDS.toSeconds(diff);
                            long seconds = diffInSec % 60;
                            diffInSec /= 60;
                            long minutes = diffInSec % 60;
                            if (minutes > 1) {

                                Log.w("LR", "Mail");

                                AsyncMail task = new AsyncMail();
                                // Call execute
                                task.execute();

                            /*Thread thread=new Thread(){
                                @Override
                                public void run() {

                                    GMailSender sender = new GMailSender("bhoslenayan25@gmail.com","nayanbhosle");
                                    try {
                                        sender.sendMail("Email Subject", "<b>Hi</b><br/>Salesman "+ session.getEmpName() +" is offline.", "bhoslenayan25@gmail.com", "george.daniel@tsysinfo.com", "");
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                }
                            };
                            thread.start();*/
                            }
                        }else{
                            locSession.createLocationSession(String.valueOf(longitude), String.valueOf(latitude),strDate);
                        }

                    }
                }else{
                    Log.w("LR", "cannot get location");
                }


            }
        } catch (Exception e) {
            e.printStackTrace();
            LogError lErr = new LogError();
            lErr.appendLog(e.toString());
            Toast.makeText(conn , "GPS Tracker Error : " + e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    private class AsyncMail extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            // Call Web Method
            try {

                /*GMailSender sender = new GMailSender("bhoslenayan25@gmail.com","nayanbhosle");
                try {
                    sender.sendMail("Email Subject", "<b>Hi</b><br/>Salesman "+ session.getEmpName() +" is offline.", "bhoslenayan25@gmail.com", "george.daniel@tsysinfo.com", "");
                } catch (Exception e) {
                    e.printStackTrace();
                }*/

            } catch (Exception e) {
                Log.w("ALARM RECEIVER", "Timeout");
                LogError lErr = new LogError();
                lErr.appendLog(e.toString());
            }
            return null;

        }
    }


    //  GpsService gpsService;
}