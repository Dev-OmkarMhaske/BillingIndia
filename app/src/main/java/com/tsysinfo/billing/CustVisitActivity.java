package com.tsysinfo.billing;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.location.Location;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.LocationServices;
import com.tsysinfo.billing.barcodes.FullScannerActivity;
import com.tsysinfo.billing.database.AllCustomer;
import com.tsysinfo.billing.database.CustomerTable;
import com.tsysinfo.billing.database.DataBaseAdapter;
import com.tsysinfo.billing.database.Models;
import com.tsysinfo.billing.database.OfflineTable;
import com.tsysinfo.billing.database.SessionManager;
import com.tsysinfo.billing.database.WebService;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class CustVisitActivity extends AppCompatActivity {

    Spinner spinnerCustomer;
    TextView txtDate;
    EditText editPurpose;
    Button btnSave, btnCancel;
    String Data = "";
    Double currLatitude;
    Double currLongitude;
    DataBaseAdapter dba;
    Models mod;
    SessionManager session;
    ProgressDialog waitDialog;
    JSONArray serverResponse;
    static boolean errored = false;

    ToastService toastService;
    public Location mLastLocation;
    private String GLOBAL_CUST_ID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        try {

            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_customer_visit);

            ImageView barCode = (ImageView) findViewById(R.id.imageButtonBarcode);
            barCode.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                /*Intent intent = new Intent("com.google.zxing.client.android.SCAN");
                intent.putExtra("SCAN_MODE", "PRODUCT_MODE");
                startActivityForResult(intent, 1);*/
                    spinnerCustomer.setSelection(0);
                    Intent intent = new Intent(CustVisitActivity.this, FullScannerActivity.class);
                    spinnerCustomer.setSelection(0);
                    startActivityForResult(intent, 1);
                }
            });

            //References


            spinnerCustomer = (Spinner) findViewById(R.id.spinnerCustomer);
            txtDate = (TextView) findViewById(R.id.VisitDate);
            editPurpose = (EditText) findViewById(R.id.editPurpose);
            btnSave = (Button) findViewById(R.id.buttonSave);
            btnCancel = (Button) findViewById(R.id.buttonCancel);

            Calendar c = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            String strDate = sdf.format(c.getTime());
            txtDate.setText(strDate);

            Log.e("Date", "cus" + sdf.toString());

            dba.open();
            ArrayList<String> customers = new ArrayList<String>();
            String sql1 = "select " + AllCustomer.KEY_NAME + " from "
                    + AllCustomer.DATABASE_TABLE;
            Cursor custCur1 = DataBaseAdapter.ourDatabase.rawQuery(sql1, null);
            if (custCur1.getCount() > 0) {
                customers.add("Select Customer");
                while (custCur1.moveToNext()) {
                    customers.add(custCur1.getString(0).trim());
                }
            }
            custCur1.close();
            dba.close();


            // Creating adapter for spinner
            ArrayAdapter<String> adapterCust = new ArrayAdapter<String>(this,
                    android.R.layout.simple_spinner_item, customers);
            adapterCust
                    .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCustomer.setAdapter(adapterCust);
            spinnerCustomer.setEnabled(false);

            GPSTracker gps = new GPSTracker(this);

            try {

                // check if GPS enabled
                if (gps.canGetLocation()) {

                    mLastLocation = LocationServices.FusedLocationApi.getLastLocation(MainActivity.mGoogleApiClient);

                    currLatitude = gps.getLatitude();
                    currLongitude = gps.getLongitude();

                    if (currLatitude != 0.0 && currLongitude != 0.0) {

                        dba.open();
                        String sql = "select " + CustomerTable.KEY_NAME + "," + CustomerTable.KEY_LONGI + "," + CustomerTable.KEY_LATI + " from "
                                + CustomerTable.DATABASE_TABLE;
                        Cursor custCur = DataBaseAdapter.ourDatabase.rawQuery(sql, null);

                        Log.w("CA", "SQL :" + sql);

                        if (custCur.getCount() > 0) {
                            //	custCur.moveToFirst();

                            int LocFlg = 0;

                            while (custCur.moveToNext()) {

                                Log.w("CA", "currLati :" + currLatitude);
                                Log.w("CA", "currLongi :" + currLongitude);
                                if (CalculateDistance.distance(custCur.getDouble(2), custCur.getDouble(1), currLatitude, currLongitude) < Double.parseDouble(session.getDistance())) {
                                    //do what you want to do...
                                    //	txtCustomer.setText(custCur.getString(0).trim());

                                    Log.w("CA", "Name :" + custCur.getString(0).trim());
                                    Log.w("CA", "Lati :" + custCur.getString(2));
                                    Log.w("CA", "Longi :" + custCur.getString(1));

                                    Log.w("CA", "currLati :" + currLatitude);
                                    Log.w("CA", "currLongi :" + currLongitude);


                                    for (int i = 0; i < spinnerCustomer.getAdapter()
                                            .getCount(); i++) {
                                        if (spinnerCustomer.getAdapter().getItem(i)
                                                .toString().contains(custCur.getString(0).trim())) {
                                            spinnerCustomer.setSelection(i);
                                            break;
                                        }
                                    }
                                }
                            }
                            if (LocFlg == 0) {

                                Toast.makeText(CustVisitActivity.this, "Unable to connect to GPS Please Select Customer Manually", Toast.LENGTH_SHORT).show();
                            }

                        }
                        custCur.close();
                        dba.close();
                    } else {
                        toastService.startTimer();
                    }

                }

            } catch (Exception e) {
                e.printStackTrace();
                LogError lErr = new LogError();
                lErr.appendLog(e.toString());
            }

            btnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });

            btnSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String cust = spinnerCustomer.getSelectedItem().toString().trim();
                    String dt = txtDate.getText().toString().trim();
                    String purpose = editPurpose.getText().toString().trim();
                    if (!cust.equalsIgnoreCase("Select Customer") || dt.length() > 0 || purpose.length() > 0) {

                        dba.open();
                        String sql = "select id from customer where name='" + cust + "'";
                        Cursor cur = DataBaseAdapter.ourDatabase.rawQuery(sql, null);
                        if (cur.getCount() > 0) {
                            cur.moveToFirst();
                            cust = cur.getString(0).trim();
                        }
                        cur.close();
                        dba.close();


                        Data = cust + "," + dt + "," + purpose + "," + session.getEmpNo();

                        Log.e("dataCustmor", "" + Data);

                        ConnectivityManager cm = ((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE));

                        if (cm == null) {
                            return;
                        }

                        if (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected()) {
                            // Start the service to do our thing

                            AsyncCallWS task = new AsyncCallWS();
                            // Call execute
                            task.execute();

                            Log.w("CustVisit", "Online");

                        } else {
                            Log.w("CustVisit", "Offline");
                            ContentValues cv = new ContentValues();
                            cv.put(OfflineTable.KEY_DATA, Data);
                            cv.put(OfflineTable.KEY_BRANCHNO, session.getBranchNo());
                            cv.put(OfflineTable.KEY_LONGITUDE, "N/A");
                            cv.put(OfflineTable.KEY_LATITUDE, "N/A");
                            cv.put(OfflineTable.KEY_METHOD, "SaveCustVisitDetails");
                            dba.open();
                            mod.insertdata(OfflineTable.DATABASE_TABLE, cv);
                            dba.close();

                            Toast.makeText(CustVisitActivity.this, "Data Saved Offline...!!", Toast.LENGTH_LONG);

                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(intent);
                            finish();
                        }

                    } else {
                        Toast.makeText(getApplicationContext(), "Please Fill all Details", Toast.LENGTH_LONG).show();
                    }


                }
            });

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.cust, menu);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.back);


        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                String myString = "";
                GLOBAL_CUST_ID = data.getStringExtra("BarCodeValue");

                dba.open();
                String sql = "select " + CustomerTable.KEY_NAME + " from "
                        + CustomerTable.DATABASE_TABLE + " where " + CustomerTable.KEY_CUSTOMER_CODE + " = '" + GLOBAL_CUST_ID + "' ";
                Cursor custCur = DataBaseAdapter.ourDatabase.rawQuery(sql, null);
                if (custCur.getCount() > 0) {
                    custCur.moveToFirst();
                    myString = custCur.getString(custCur.getColumnIndex(CustomerTable.KEY_NAME));
                    ArrayAdapter myAdap = (ArrayAdapter) spinnerCustomer.getAdapter(); //cast to an ArrayAdapter

                    int spinnerPosition = myAdap.getPosition(myString);

//set the default according to value
                    spinnerCustomer.setSelection(spinnerPosition);

                } else {

                    String temp = GLOBAL_CUST_ID;
                    temp = temp.replaceAll("[^\\d.]", "");
                    if (temp.equalsIgnoreCase("")) {
                        temp = "1234567";
                    }
                    String sql1 = "select " + CustomerTable.KEY_NAME + " from "
                            + CustomerTable.DATABASE_TABLE + " where " + CustomerTable.KEY_ID + " = " + temp + " ";
                    Cursor custCur1 = DataBaseAdapter.ourDatabase.rawQuery(sql1, null);

                    if (custCur1.getCount() > 0) {
                        custCur1.moveToFirst();
                        myString = custCur1.getString(custCur1.getColumnIndex(CustomerTable.KEY_NAME));

                        ArrayAdapter myAdap = (ArrayAdapter) spinnerCustomer.getAdapter(); //cast to an ArrayAdapter

                        int spinnerPosition = myAdap.getPosition(myString);

//set the default according to value
                        spinnerCustomer.setSelection(spinnerPosition);
                    } else {
                        spinnerCustomer.setSelection(0);
                        Toast.makeText(CustVisitActivity.this, "No User Found", Toast.LENGTH_SHORT).show();

                    }

                }

                dba.close();

                //the value you want the position for


            }
        }
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

    // WebService Code

    private class AsyncCallWS extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            // Call Web Method
            try {

                Log.w("CartActivity", "Start");

                dba.open();
                serverResponse = WebService.customerRegistration(session.getBranchNo(), Data, "SaveCustVisitDetails");
                dba.close();

            } catch (Exception e) {
                Log.w("CartActivity", "Timeout");
            }
            return null;
        }

        @SuppressLint("ResourceAsColor")
        @Override
        // Once WebService returns response
        protected void onPostExecute(Void result) {

            Log.w("FeedbackActivity", "TimeOutFlag : " + WebService.timeoutFlag);
            Log.w("FeedbackActivity", "ResponseString : "
                    + WebService.responseString);

            // Make Progress Bar invisible
            waitDialog.cancel();
            Log.w("FeedbackActivity", "Dialog Closed");
            if (WebService.timeoutFlag == 1) {
                Log.w("FeedbackActivity", "Timeout");
                AlertDialog.Builder builder = new AlertDialog.Builder(
                        CustVisitActivity.this);
                builder.setTitle("Connection Time Out!");

                builder.setMessage("Please Try Again!!!")
                        .setCancelable(false)
                        .setPositiveButton("Ok",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int id) {
                                        dialog.cancel();

                                    }
                                });

                AlertDialog alert = builder.create();
                alert.show();

            } else {
                Log.w("CartActivity", "Try");
                try {

                    /*
                     * Toast.makeText(LoginActivity.this, "JSON Data:  " +
                     * WebService.responseString, Toast.LENGTH_LONG).show();
                     */
                    // Error status is false
                    if (!errored) {
                        // Based on Boolean value returned from WebService
                        if (serverResponse != null) {

                            if (serverResponse.getJSONObject(0)
                                    .getString("Status").trim()
                                    .equalsIgnoreCase("No")) {

                                AlertDialog.Builder builder2 = new AlertDialog.Builder(
                                        CustVisitActivity.this);
                                builder2.setTitle("Failure..");
                                builder2.setMessage("Failed to save Data.");
                                builder2.setPositiveButton("OK",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(
                                                    DialogInterface dialog,
                                                    int which) {
                                                Log.e("info", "OK");
                                            }
                                        });
                                builder2.show();

                            } else if (serverResponse.getJSONObject(0)
                                    .getString("Status").trim()
                                    .equalsIgnoreCase("Success")) {

                                Toast.makeText(CustVisitActivity.this, "Visit details saved Successfully!!!!",
                                        Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(CustVisitActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();

                            }

                        }

                        // Error status is true
                    } else {

                        Toast.makeText(CustVisitActivity.this, "Server Error",
                                Toast.LENGTH_LONG).show();
                        // statusTV.setText("Error occured in invoking webservice");
                    }
                    // Re-initialize Error Status to False

                    errored = false;

                } catch (SQLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (Resources.NotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (JSONException e) {
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
}
