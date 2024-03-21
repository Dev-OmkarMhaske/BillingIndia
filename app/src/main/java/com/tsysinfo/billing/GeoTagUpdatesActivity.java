package com.tsysinfo.billing;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.SQLException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;

import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.ListView;
import android.widget.Toast;
import com.tsysinfo.billing.database.DataBaseAdapter;
import com.tsysinfo.billing.database.GeoTagUpdate;
import com.tsysinfo.billing.database.Models;
import com.tsysinfo.billing.database.SessionManager;
import com.tsysinfo.billing.database.WebService;
import org.json.JSONArray;
import java.util.ArrayList;

public class GeoTagUpdatesActivity extends AppCompatActivity {

    private SessionManager session;
    private DataBaseAdapter dba;
    private Models mod;
    private ProgressDialog waitDialog;
    private JSONArray serverResponse;
    private static boolean errored = false;
    private ArrayList<GeoTagUpdate> geoTagUpdateArrayList;
    private ListView listView;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lay_geotag_updates_activity);

        dba = new DataBaseAdapter(this);
        mod = new Models();
        session = new SessionManager(this);

        waitDialog = new ProgressDialog(this);
        waitDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        waitDialog.setMessage("Please Wait");

        geoTagUpdateArrayList=new ArrayList<>();
        listView= (ListView) findViewById(R.id.list);
        new AsyncCallWS().execute();
     /*    [{"CName":"No","GPSLatitude":"No","GPSLongitude":"No","UpdatedTStmp":"No"}]*/
    }

    private class AsyncCallWS extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            // Call Web Method
            try {

                Log.w("GeoTagUpdatesActivity", "Start");

                dba.open();
                serverResponse = WebService.GeoUpdate(session.getEmpNo(), "GetGeoTagUpdates");
                dba.close();

            } catch (Exception e) {
                Log.w("GeoTagUpdatesActivity", "Timeout");
            }
            return null;
        }

        @SuppressLint("ResourceAsColor")
        @Override
        // Once WebService returns response
        protected void onPostExecute(Void result) {

            Log.w("GeoTagUpdatesActivity", "TimeOutFlag : " + WebService.timeoutFlag);
            Log.w("GeoTagUpdatesActivity", "ResponseString : "
                    + WebService.responseString);

            // Make Progress Bar invisible
            waitDialog.cancel();
            Log.w("GeoTagUpdatesActivity", "Dialog Closed");
            if (WebService.timeoutFlag == 1) {
                Log.w("GeoTagUpdatesActivity", "Timeout");
                AlertDialog.Builder builder = new AlertDialog.Builder(
                        GeoTagUpdatesActivity.this);
                builder.setTitle("Connection Time Out!");

                builder.setMessage("Please Try Again!!!").setCancelable(false).setPositiveButton("Ok",

                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int id) {
                                        dialog.cancel();

                                    }
                                });

                AlertDialog alert = builder.create();
                alert.show();

            } else {
                Log.w("GeoTagUpdatesActivity", "Try");
                try {

                    if (!errored) {
                        // Based on Boolean value returned from WebService
                        if (serverResponse != null) {

                            for (int i = 0; i < serverResponse.length(); i++) {
                                GeoTagUpdate geoTagUpdate = new GeoTagUpdate();
                                geoTagUpdate.setCName(serverResponse.getJSONObject(i).getString("CName"));
                                geoTagUpdate.setGPSLatitude(serverResponse.getJSONObject(i).getString("GPSLatitude"));
                                geoTagUpdate.setGPSLongitude(serverResponse.getJSONObject(i).getString("GPSLongitude"));
                                geoTagUpdate.setUpdatedTStmp(serverResponse.getJSONObject(i).getString("UpdatedTStmp"));
                                geoTagUpdateArrayList.add(geoTagUpdate);

                            }
                            GeoTagUpdatesAdapter geoTagUpdatesAdapter=new GeoTagUpdatesAdapter(GeoTagUpdatesActivity.this,geoTagUpdateArrayList);
                            listView.setAdapter(geoTagUpdatesAdapter);
                        }
                        // Error status is true
                    } else {

                        Toast.makeText(GeoTagUpdatesActivity.this, "Server Error",
                                Toast.LENGTH_LONG).show();
                        // statusTV.setText("Error occured in invoking webservice");
                    }
                    errored = false;

                } catch (SQLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (Resources.NotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (Exception e) {
                    // TODO: handle exception
                    e.printStackTrace();
                }
            }
        }

        @Override
        // Make Progress Bar visible
        protected void onPreExecute() {
            waitDialog.show();
            waitDialog.setCanceledOnTouchOutside(false);
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }

    @SuppressLint("NewApi")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.cart, menu);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.back);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        super.onBackPressed();
        Intent intent = new Intent(getApplicationContext(),
                MainActivity.class);
        startActivity(intent);
        finish();

    }
}
