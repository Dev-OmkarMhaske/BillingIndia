package com.tsysinfo.billing.database;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

public class SyncOffline {

    static boolean errored = false;
    Models mod;
    DataBaseAdapter dba;
    SessionManager session;
    JSONArray serverResponse;
    String Data, BranchNo, Longitude, Latitude, webMethName;

    public void syncdata(Context context) {

        dba = new DataBaseAdapter(context);
        mod = new Models();

        try {
            dba.open();
            Cursor cur = mod.getData("offline");
            Log.w("SS", "Count: " + cur.getCount());
            if (cur.getCount() > 0) {
                while (cur.moveToNext()) {
                    String method=cur.getString(4);
                    if (method.equalsIgnoreCase(""))
                    {
                        String AllData=cur.getString(0).trim();
                        String[] DataArray=AllData.split("$");
                        serverResponse = WebService.makeTransactionReciept(DataArray[0],DataArray[1], cur.getString(1).trim(),
                                cur.getString(2).trim(), cur.getString(3).trim(), cur.getString(4).trim());
                        Log.w("Receipt Activity",
                                "Data " + serverResponse.toString());
                    }else {
                        serverResponse = WebService.makeTransaction(cur.getString(0).trim(), cur.getString(1).trim(),
                                cur.getString(2).trim(), cur.getString(3).trim(), cur.getString(cur.getColumnIndex("method")).trim(), cur.getString(cur.getColumnIndex("TransType")).trim(), cur.getString(cur.getColumnIndex("Mode")));
                        Log.w("Receipt Activity",
                                "Data " + serverResponse.toString());
                    }
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


            Cursor curFeed = mod.getData(OfflineFeedbackTable.DATABASE_TABLE);
            if(curFeed.getCount() > 0){
                while(curFeed.moveToNext()){
                    serverResponse = WebService.sendFeedback(curFeed.getString(0).trim(), curFeed.getString(1).trim()
                            , curFeed.getString(2).trim(), curFeed.getString(3).trim(), curFeed.getString(4).trim());
                    if (!errored) {
                        // Based on Boolean value returned from WebService
                        if (serverResponse != null) {
                            // Navigate to Home Screen
                            try {
                                Log.w("OflineSync Activity", "Status : "
                                        + serverResponse.getJSONObject(0)
                                        .getString("Status"));
                                if (serverResponse.getJSONObject(0)
                                        .getString("Status").trim()
                                        .equalsIgnoreCase("Success")) {
                                    String sql = "delete from offlinefeedback where empid='" + cur.getString(0).trim() + "' " +
                                            "and branchnumber='" + cur.getString(1).trim() + "' " +
                                            "and data='" + cur.getString(2).trim() + "' " +
                                            "and imagepath='" + cur.getString(3).trim() + "' " +
                                            "and method='" + cur.getString(4).trim() + "'";

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
            curFeed.close();


            dba.close();
        } catch (Exception e) {
            Log.w("Receipt Activity", "Timeout ");
        }


    }



}
