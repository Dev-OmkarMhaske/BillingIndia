package com.tsysinfo.billing;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources.NotFoundException;
import android.database.Cursor;
import android.database.SQLException;
import android.location.Location;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
 
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.location.LocationServices;
import com.tsysinfo.billing.barcodes.FullScannerActivity;
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

public class DeliveryActivity extends AppCompatActivity {

    static Spinner spinCustomer;

    private static String FILE = Environment.getExternalStorageDirectory()
            .toString() + "/ebilling_images/pdf/bill.pdf";
    public Location mLastLocation;
    SessionManager session;
    DataBaseAdapter dba;
    Models mod;
    ListView lv;
    String custid = "";
    String Data = "";
    Double currLatitude;
    Double currLongitude;
    ProgressDialog waitDialog;
    JSONArray serverResponse;
    static boolean errored = false;

    String longi = "",lati = "";
    ToastService toastService;
    private String GLOBAL_CUST_ID;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                String myString = "";
                GLOBAL_CUST_ID = data.getStringExtra("BarCodeValue");

                dba.open();
                String sql = "select " + CustomerTable.KEY_NAME + " from "
                        + CustomerTable.DATABASE_TABLE+ " where "+CustomerTable.KEY_CUSTOMER_CODE+" = '"+GLOBAL_CUST_ID+"' ";
                Cursor custCur = DataBaseAdapter.ourDatabase.rawQuery(sql, null);
                if(custCur.getCount()>0)
                {
                    custCur.moveToFirst();
                    myString=custCur.getString(custCur.getColumnIndex(CustomerTable.KEY_NAME)) ;
                    ArrayAdapter myAdap = (ArrayAdapter) spinCustomer.getAdapter(); //cast to an ArrayAdapter

                    int spinnerPosition = myAdap.getPosition(myString);

//set the default according to value
                    spinCustomer.setSelection(spinnerPosition);
                }else
                {

                    String temp=GLOBAL_CUST_ID;
                    temp = temp.replaceAll("[^\\d.]", "");
                    if(temp.equalsIgnoreCase(""))
                    {
                        temp="1234567";
                    }
                    String sql1 = "select " + CustomerTable.KEY_NAME + " from "
                            + CustomerTable.DATABASE_TABLE+ " where "+CustomerTable.KEY_ID+"="+temp+" ";
                    Cursor custCur1 = DataBaseAdapter.ourDatabase.rawQuery(sql1, null);

                    if(custCur1.getCount()>0)
                    {
                        custCur1.moveToFirst();
                        myString=custCur1.getString(custCur1.getColumnIndex(CustomerTable.KEY_NAME)) ;
                        ArrayAdapter myAdap = (ArrayAdapter) spinCustomer.getAdapter(); //cast to an ArrayAdapter

                        int spinnerPosition = myAdap.getPosition(myString);

//set the default according to value
                        spinCustomer.setSelection(spinnerPosition);
                    }
                    else
                    {
                        spinCustomer.setSelection(0);
                        Toast.makeText(DeliveryActivity.this, "No User Found", Toast.LENGTH_SHORT).show();

                    }

                }


                dba.close();
                //the value you want the position for


            }
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cart_list_activity);

        session = new SessionManager(this);
        dba = new DataBaseAdapter(this);
        mod = new Models();


        ImageView barCode=(ImageView)findViewById(R.id.imageButtonBarcode);
        barCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DeliveryActivity.this,FullScannerActivity.class);
                spinCustomer.setSelection(0);
                startActivityForResult(intent, 1);

            }
        });
        waitDialog = new ProgressDialog(this);
        waitDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        waitDialog.setMessage("Please Wait");

        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        spinCustomer = (Spinner) findViewById(R.id.spinnerCustomer);
        spinCustomer.setEnabled(false);
        lv = (ListView) findViewById(R.id.list);
        toastService = new ToastService(DeliveryActivity.this);

        dba.open();
        ArrayList<String> customers = new ArrayList<String>();
        String sql1 = "select " + CustomerTable.KEY_NAME + " from "
                + CustomerTable.DATABASE_TABLE;
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
        ArrayAdapter<String> adapterRelation = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, customers);
        adapterRelation
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinCustomer.setAdapter(adapterRelation);

        GPSTracker gps = new GPSTracker(this);

        try {


            // check if GPS enabled
            if (gps.canGetLocation()) {

                mLastLocation = LocationServices.FusedLocationApi
                        .getLastLocation(MainActivity.mGoogleApiClient);

                currLatitude = gps.getLatitude();
                currLongitude = gps.getLongitude();


                dba.open();
                String sql = "select " + CustomerTable.KEY_NAME + "," + CustomerTable.KEY_LONGI + "," + CustomerTable.KEY_LATI + " from "
                        + CustomerTable.DATABASE_TABLE;
                Cursor custCur = DataBaseAdapter.ourDatabase.rawQuery(sql, null);

                Log.w("CA", "SQL :" + sql);

                if (custCur.getCount() > 0) {
                    //	custCur.moveToFirst();

                    int LocFlg = 0;

                    while (custCur.moveToNext()) {

                        Log.w("CA", "Name :" + custCur.getString(0).trim());
                        Log.w("CA", "Lati :" + custCur.getString(2));
                        Log.w("CA", "Longi :" + custCur.getString(1));

                        Log.w("CA", "currLati :" + currLatitude);
                        Log.w("CA", "currLongi :" + currLongitude);

                        if (CalculateDistance.distance(custCur.getDouble(2), custCur.getDouble(1), currLatitude, currLongitude) < Double.parseDouble(session.getDistance())) {
                            //do what you want to do...
                            //	txtCustomer.setText(custCur.getString(0).trim());
                            for (int i = 0; i < spinCustomer.getAdapter()
                                    .getCount(); i++) {
                                if (spinCustomer.getAdapter().getItem(i)
                                        .toString().contains(custCur.getString(0).trim())) {
                                    spinCustomer.setSelection(i);
                                    ArrayList<DeliverySearch> DeliverySearch = GetDeliverySearch();
                                    lv.setAdapter(new DeliveryBaseAdapter(DeliveryActivity.this, DeliverySearch));
                                    LocFlg = 1;
                                    break;
                                }
                            }
                        }
                    }
                    if(LocFlg == 0){
                        Toast.makeText(DeliveryActivity.this, "Unable to connect to GPS Please Select Customer Manually", Toast.LENGTH_SHORT).show();
                    }
                }
                custCur.close();
                dba.close();
            }else{
                toastService.startTimer();
            }

        }catch (Exception e){
            e.printStackTrace();
            LogError lErr = new LogError();
            lErr.appendLog(e.toString());
        }
        spinCustomer.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                String custName = spinCustomer.getSelectedItem().toString().trim();
                if (!custName.equalsIgnoreCase("Select Customer")) {
                    ArrayList<DeliverySearch> DeliverySearch = GetDeliverySearch();
                    lv.setAdapter(new DeliveryBaseAdapter(DeliveryActivity.this, DeliverySearch));
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        toastService.stopTimer();
    }

    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        super.onBackPressed();
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }

    private ArrayList<DeliverySearch> GetDeliverySearch() {
        ArrayList<DeliverySearch> results = new ArrayList<DeliverySearch>();

        DeliverySearch sr = new DeliverySearch();

        String custName = spinCustomer.getSelectedItem().toString().trim();

        try {


            dba.open();
            String sql1 = "select id from customer where name='" + custName + "'";
            Cursor cur1 = DataBaseAdapter.ourDatabase.rawQuery(sql1, null);
            if (cur1.getCount() > 0) {
                cur1.moveToFirst();
                custid = cur1.getString(0).trim();


                String sql = "select * from delivery where custid='" + custid + "'";
                Cursor cur = DataBaseAdapter.ourDatabase.rawQuery(sql, null);

                if (cur.getCount() > 0) {

                    while (cur.moveToNext()) {

                        sr = new DeliverySearch();
                        sr.setId(cur.getString(0).trim());
                        sr.setName(cur.getString(5));
                        sr.setQty(cur.getString(6).trim());
                        sr.setPrice("Price : "+cur.getString(8).trim());
                        sr.setBno(cur.getString(10).trim());
                        sr.setBdate(cur.getString(11).trim());
                        sr.setTotal(String.valueOf((cur.getInt(6) * cur.getInt(8))
                                - ((cur.getInt(6) * cur.getInt(8)) * (cur.getInt(7) * 0.01))));
                        String path = Environment.getExternalStorageDirectory()
                                .toString()
                                + "/ebilling_images/"
                                + cur.getString(4).trim() + ".JPG";
                        sr.setImage(path);
                        results.add(sr);

                    }

                } else {
                    lv.setAdapter(null);
                    Toast.makeText(getApplicationContext(),
                            "No Items to Deliver", Toast.LENGTH_LONG).show();
                }
                cur.close();

            }
            cur1.close();
            dba.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        return results;
    }

    @SuppressLint("NewApi")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.cart, menu);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.back);

        menu.findItem(R.id.action_chekout).setIcon(R.drawable.checkout_icon);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;

            case R.id.action_chekout:

                saveToServer();

			/*AlertDialog.Builder builder = new AlertDialog.Builder(DeliveryActivity.this);
            builder.setMessage("Generate receipt?")
			   .setCancelable(false)
			   .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			       public void onClick(DialogInterface dialog, int id) {
			    	   dialog.cancel();
			            
			            saveToServer();
			            
			       }
			   })
			   .setNegativeButton("No", new DialogInterface.OnClickListener() {
			       public void onClick(DialogInterface dialog, int id) {
			            dialog.cancel();
			            
			            saveToServer();
			       }
			   });
			AlertDialog alert = builder.create();
			alert.show();*/


                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void generatePDF() {

    }

    public void saveToServer() {

        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        String strDate = sdf.format(c.getTime());

        dba.open();
        String sql = "select id,pid,qty,price,discount,billno from delivery where status='1'";
        double totalamt = 0.0;
        Cursor curData = DataBaseAdapter.ourDatabase.rawQuery(sql, null);
        if (curData.getCount() > 0) {
            Data="";
            while (curData.moveToNext()) {

                totalamt = totalamt + ((curData.getInt(2) * curData.getInt(3))
                        - ((curData.getInt(2) * curData.getInt(3)) * (curData.getInt(4) * 0.01)));

                Data = Data + strDate + "," + custid + ","
                        + curData.getString(0).trim() + ","
                        + session.getUserId() + "," + curData.getString(1).trim() + "," + totalamt + "," + curData.getString(curData.getColumnIndex("billno")).trim()+"$";

            }

            String logFlag = "";
            GPSTracker gps = new GPSTracker(DeliveryActivity.this);
            if(gps.canGetLocation()) {
                if (gps.getLongitude() != 0.0 && gps.getLatitude() != 0.0) {
                    longi = String.valueOf(gps.getLongitude());
                    lati = String.valueOf(gps.getLatitude());
                    logFlag = "LocationManager";
                } else {
                    Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(MainActivity.mGoogleApiClient);
                    longi = String.valueOf(mLastLocation.getLongitude());
                    lati = String.valueOf(mLastLocation.getLatitude());
                    logFlag = "GoogleApi";
                }

                Log.w("LocationFlag", "" + logFlag);

                ConnectivityManager cm = ((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE));

                if (cm == null) {
                    return;
                }

                // Now to check if we're actually connected
                if (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected()) {
                    // Start the service to do our thing

                    AsyncCallWS task = new AsyncCallWS();
                    // Call execute
                    task.execute();

                    Log.w("Delivery", "Online");

                } else {
                    Log.w("Delivery", "Offline");
                    ContentValues cv = new ContentValues();
                    cv.put(OfflineTable.KEY_DATA, Data);
                    cv.put(OfflineTable.KEY_BRANCHNO, session.getBranchNo());
                    cv.put(OfflineTable.KEY_LONGITUDE, longi);
                    cv.put(OfflineTable.KEY_LATITUDE, lati);
                    cv.put(OfflineTable.KEY_METHOD, "SaveDelivery");
                    dba.open();
                    mod.insertdata(OfflineTable.DATABASE_TABLE, cv);
                    dba.close();

                    Toast.makeText(DeliveryActivity.this,
                            "Data Saved Offline...!!",
                            Toast.LENGTH_LONG);

                    Intent intent = new Intent(
                            getApplicationContext(),
                            MainActivity.class);
                    startActivity(intent);
                    finish();
                }

            }else{
                gps.showSettingsAlert();
            }

        } else {
            Toast.makeText(DeliveryActivity.this, "Please Confirm Items First", Toast.LENGTH_LONG).show();
        }
        curData.close();
        dba.close();


    }

    // WebService Code

    private class AsyncCallWS extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            // Call Web Method
            try {

                Log.w("CartActivity", "Start");

                dba.open();
                serverResponse = WebService.makeTransaction(Data, session.getBranchNo(), longi, lati, "SaveDelivery","","");
                dba.close();

            } catch (Exception e) {
                Log.w("CartActivity", "Timeout ");
            }
            return null;
        }

        @SuppressLint("ResourceAsColor")
        @Override
        // Once WebService returns response
        protected void onPostExecute(Void result) {

            Log.w("CartActivity", "TimeOutFlag : " + WebService.timeoutFlag);
            Log.w("CartActivity", "ResponseString : "
                    + WebService.responseString);

            // Make Progress Bar invisible
            waitDialog.cancel();
            Log.w("CartActivity", "Dialog Closed");
            if (WebService.timeoutFlag == 1) {
                Log.w("CartActivity", "Timeout");
                AlertDialog.Builder builder = new AlertDialog.Builder(
                        DeliveryActivity.this);
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
                                        DeliveryActivity.this);
                                builder2.setTitle("Failure..");
                                builder2.setMessage("Failed to make Payment.");
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

                                Toast.makeText(DeliveryActivity.this,
                                        "Payment Successfull!!!",
                                        Toast.LENGTH_LONG);

                                dba.open();
                                String sqlDel = "delete from delivery where status = '1'";
                                DataBaseAdapter.ourDatabase.execSQL(sqlDel);
                                dba.close();


                                Intent intent = new Intent(
                                        getApplicationContext(),
                                        MainActivity.class);
                                startActivity(intent);
                                finish();

                            }

                        }

                        // Error status is true
                    } else {

                        Toast.makeText(DeliveryActivity.this, "Server Error",
                                Toast.LENGTH_LONG).show();
                        // statusTV.setText("Error occured in invoking webservice");
                    }
                    // Re-initialize Error Status to False

                    errored = false;

                } catch (SQLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (NotFoundException e) {
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