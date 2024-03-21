package com.tsysinfo.billing;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.tsysinfo.billing.database.DataBaseAdapter;
import com.tsysinfo.billing.database.LocationTable;
import com.tsysinfo.billing.database.Models;
import com.tsysinfo.billing.database.SessionLocation;
import com.tsysinfo.billing.database.SessionManager;
import com.tsysinfo.billing.database.WebService;

import org.json.JSONArray;

public class AlarmReceiver extends BroadcastReceiver {

    public static Context conn = null;
    public Location mLastLocation;
    int flag = 0;
    SessionManager session;
    SessionLocation locSession;
    String latitude;
    String longitude;
    JSONArray serverResponse;
    String count;
    String Data = "";
    DataBaseAdapter dba;
    Models mod;
    GPSTracker gps;

    @Override
    public void onReceive(Context context, Intent arg1) {
        // For our recurring task, we'll just display a message
        //	Toast.makeText(context, "I'm running", Toast.LENGTH_SHORT).show();
        this.conn = context;

        session = new SessionManager(context);
        dba = new DataBaseAdapter(context);
        mod = new Models();
        locSession = new SessionLocation(context);
        try {
            gps = new GPSTracker(context);

            latitude = String.valueOf(gps.getLatitude());
            longitude = String.valueOf(gps.getLongitude());
            Log.w("AlarmReceiver", "CurrentLongitude: " + longitude);
            Log.w("AlarmReceiver", "CurrentLongitude: " + latitude);

            // check if GPS enabled
     /*   if (gps.canGetLocation()) {

            latitude = String.valueOf(gps.getLatitude());
            longitude = String.valueOf(gps.getLongitude());
            //		Toast.makeText(conn, "latitude: "+latitude+"\nlongitude: "+longitude, Toast.LENGTH_LONG).show();

            Toast.makeText(context, "Location changed!\nLongitude: " + longitude + "\nLatitude: " + latitude,
                    Toast.LENGTH_SHORT).show();


            if (!latitude.equalsIgnoreCase("0.0") || !longitude.equalsIgnoreCase("0.0")) {
                Log.w("AlarmReceiver","CurrentLongitude: "+longitude);
                Log.w("AlarmReceiver","CurrentLongitude: "+latitude);
                Log.w("AlarmReceiver","SessionLongitude: "+locSession.getLongi());
                Log.w("AlarmReceiver","SessionLongitude: "+locSession.getLati());

                if (!latitude.equalsIgnoreCase(locSession.getLati()) || !longitude.equalsIgnoreCase(locSession.getLongi())) {

                    locSession.logoutUser();
                    locSession.createLocationSession(longitude,latitude);

                *//*    AsyncCallWS task = new AsyncCallWS();
                    // Call execute
                    task.execute();*//*
                }
            }
        }else{
            Log.w("AlarmReceiver","GPS Cant Get Location");
        }*/


            dba.open();
            Cursor curLoc = mod.getData(LocationTable.DATABASE_TABLE);
            if (curLoc.getCount() > 0) {
                int cnt = 1;
                double longi = 0;
                double lati = 0;
                while (curLoc.moveToNext()) {

                    /*String[] arrLon = curLoc.getString(0).split("\\.");
                    int[] intArrLon=new int[2];
                    intArrLon[0]=Integer.parseInt(arrLon[0]);
                //    intArrLon[1]=Integer.parseInt(arrLon[1]);
                    double preLon = intArrLon[0];

                    String[] arrLat = curLoc.getString(0).split("\\.");
                    int[] intArrLat=new int[2];
                    intArrLat[0]=Integer.parseInt(arrLat[0]);
                 //   intArrLat[1]=Integer.parseInt(arrLat[1]);
                    double preLat = intArrLat[0];*/

                    /*longi = longi + (curLoc.getDouble(0) - intArrLon[0]);
                    lati = lati + (curLoc.getDouble(1) - intArrLat[0]);*/

                    LogError lErr = new LogError();
                    lErr.appendLog(cnt+"  Longi: "+curLoc.getDouble(0)+"  Lati: "+curLoc.getDouble(1));

                    Data = Data+curLoc.getString(0) + "," + curLoc.getString(1) + "," + curLoc.getString(2) + "$";

                    Log.e("Tracking1",""+Data);

                    Toast.makeText(context, "Tracking1 : "+Data.toString(), Toast.LENGTH_SHORT).show();



                    cnt++;
                    /*if(cnt == 6) {
                        longi = longi / 5;
                        longi = preLon + longi;
                        lati = lati / 5;
                        lati = preLat + lati;
                        Data = Data + longi + "," + lati + "," + curLoc.getString(2) + "$";
                        cnt = 1;
                        longi = 0;
                        lati = 0;

                    }*/
                }
                AsyncCallWS task = new AsyncCallWS();
                // Call execute
                task.execute();
              //  Log.e("Location","getdata"+Data);

            }
            dba.close();

        } catch (Exception e) {
            e.printStackTrace();
            LogError lErr = new LogError();
            lErr.appendLog(e.toString());
        }

    }

    // WebService Code

    private class AsyncCallWS extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            // Call Web Method
            try {

                Log.w("Alarm Receiver", "Start");
                dba.open();

/*                String sql = "select " + CustomerTable.KEY_NAME + "," + CustomerTable.KEY_LONGI + "," + CustomerTable.KEY_LATI + "," + CustomerTable.KEY_ID + " from "
                        + CustomerTable.DATABASE_TABLE;
                Cursor custCur = DataBaseAdapter.ourDatabase.rawQuery(sql, null);

                Log.w("CA", "SQL :" + sql);

                if (custCur.getCount() > 0) {

                    //	custCur.moveToFirst();
                    while (custCur.moveToNext()) {

                        mLastLocation = LocationServices.FusedLocationApi
                                .getLastLocation(MenuActivity.mGoogleApiClient);

                        if (CalculateDistance.distance(custCur.getDouble(2), custCur.getDouble(1), mLastLocation.getLatitude(), mLastLocation.getLongitude()) < Double.parseDouble(session.getDistance())) {
                            //do what you want to do...

                            Log.w("CA", "Name :" + custCur.getString(0).trim());
                            Log.w("CA", "Lati :" + custCur.getString(2));
                            Log.w("CA", "Longi :" + custCur.getString(1));

                            Log.w("CA", "currLati :" + latitude);
                            Log.w("CA", "currLongi :" + longitude);

                            String DData = session.getEmpNo() + "," + custCur.getString(3).trim() + "," + custCur.getString(0).trim();
                            serverResponse = WebService.putData(DData, session.getBranchNo(), "CustVisit");
                            break;
                        }
                    }

                }
                custCur.close();
*/
                serverResponse = WebService.sendLocation(session.getEmpNo(), session.getBranchNo(), Data, "SetRouteLocation");

                Log.e("Tracking4",""+Data);


                LogError lErr2 = new LogError();
                lErr2.appendLog("Tracking4 : "+Data.toString());

                dba.close();
                Log.w("Alarm Receiver", "ResponseData: " + WebService.responseString);
                if (serverResponse.getJSONObject(0).getString("Status").trim().equalsIgnoreCase("Success")) {
                    dba.open();
                    mod.clearDatabase(LocationTable.DATABASE_TABLE);
                    dba.close();
                }
                serverResponse = null;

            } catch (Exception e) {
                Log.w("ALARM RECEIVER", "Timeout");
                LogError lErr = new LogError();
                lErr.appendLog(e.toString());
            }
            return null;

        }
    }

}

