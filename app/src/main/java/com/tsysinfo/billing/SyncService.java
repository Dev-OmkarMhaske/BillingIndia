package com.tsysinfo.billing;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.tsysinfo.billing.database.DataBaseAdapter;
import com.tsysinfo.billing.database.Models;
import com.tsysinfo.billing.database.WebService;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * Created by administrator on 19/4/16.
 */
public class SyncService extends Service {
    static boolean errored = false;
    DataBaseAdapter dba;
    Models mod;
    String URL = "";
    JSONArray serverResponse;
    int flagPayment = 0;

    String Data = "";

    public SyncService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {

        // Toast.makeText(this, "Service was Created", Toast.LENGTH_LONG).show();
        dba = new DataBaseAdapter(this);
        mod = new Models();
    }

    @Override
    public void onStart(Intent intent, int startId) {

        Log.w("eBilling", "SyncService executed");
        AsyncCallWS task = new AsyncCallWS();
        // Call execute
        task.execute();

    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "Service Destroyed", Toast.LENGTH_LONG).show();
    }

    private class AsyncCallWS extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            // Call Web Method
            try {
                dba.open();
                Cursor cur = mod.getData("offline");
                Log.w("SS", "Count: " + cur.getCount());
                if (cur.getCount() > 0) {
                    while (cur.moveToNext()) {

                        serverResponse = WebService.makeTransaction(cur.getString(0).trim(), cur.getString(1).trim(),
                                    cur.getString(2).trim(), cur.getString(3).trim(), cur.getString(cur.getColumnIndex("method")).trim(),cur.getString(cur.getColumnIndex("TransType")).trim(), cur.getString(cur.getColumnIndex("Mode")));
                        Log.w("Receipt Activity",
                                "Data " + serverResponse.toString());
                        if (!errored) {
                            // Based on Boolean value returned from WebService
                            if (serverResponse != null) {
                                // Navigate to Home Screen
                                try {
                                    Log.w("Registration Activity", "Status : "
                                            + serverResponse.getJSONObject(0)
                                            .getString("Status"));
                                    if (serverResponse.getJSONObject(0)
                                            .getString("Status").trim()
                                            .equalsIgnoreCase("Success")) {
                                        String sql = "delete from offline where data='" + cur.getString(0).trim() + "' " +
                                                "and branchnumber='" + cur.getString(1).trim() + "' " +
                                                "and longi='" + cur.getString(2).trim() + "' " +
                                                "and lati='" + cur.getString(3).trim() + "' " +
                                                "and method='" + cur.getString(cur.getColumnIndex("method")).trim() + "'";

                                        DataBaseAdapter.ourDatabase.execSQL(sql);
                                    }
                                } catch (JSONException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }
                            }
                            // Error status is true
                        }
                    }
                }
                cur.close();
                dba.close();
            } catch (Exception e) {
                Log.w("Receipt Activity", "Timeout ");
            }
            return null;
        }
    }


}