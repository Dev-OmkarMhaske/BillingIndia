package com.tsysinfo.billing;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Location;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.tsysinfo.billing.barcodes.FullScannerActivity;
import com.tsysinfo.billing.database.BillingTable;
import com.tsysinfo.billing.database.CustomerTable;
import com.tsysinfo.billing.database.DataBaseAdapter;
import com.tsysinfo.billing.database.LocationTable;
import com.tsysinfo.billing.database.Models;
import com.tsysinfo.billing.database.OfflineTable;
import com.tsysinfo.billing.database.OrderTable;
import com.tsysinfo.billing.database.OrderTempTable;
import com.tsysinfo.billing.database.ProductTable;
import com.tsysinfo.billing.database.ReturnTable;
import com.tsysinfo.billing.database.SessionLocation;
import com.tsysinfo.billing.database.SessionManager;
import com.tsysinfo.billing.database.Sync;
import com.tsysinfo.billing.database.SyncOffline;
import com.tsysinfo.billing.database.TempLocationTable;
import com.tsysinfo.billing.database.WebService;

import org.json.JSONArray;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class MenuActivity extends Activity implements GoogleApiClient.ConnectionCallbacks,

        GoogleApiClient.OnConnectionFailedListener, LocationListener{

    // LogCat tag
    private static final String TAG = MenuActivity.class.getSimpleName();

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;

    public static Location mLastLocation;

    // Google client to interact with Google API
    public static GoogleApiClient mGoogleApiClient;
    static int flagApiIsConnected = 0;
    // Location updates intervals in sec
    private static int UPDATE_INTERVAL = 1000; // 5 sec
    private static int FATEST_INTERVAL = 1000; // 3 sec
    private static int DISPLACEMENT = 5; // 5 meters
    Button btnProducts, btnDelivery, btnCustList, btnOrder, btnBilling,
            btnReturn, btnSync, btnExit, btnReceipt, btnHelp,btnVisitStatus,
            btnDiatance, btnFeedback, btnCustVisit;

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////
    LinearLayout fifthLayout;
    Double currLatitude = 0.0;
    Double currLongitude = 0.0;
    String custid = "0";
    SessionManager session;
    SessionLocation locSession;
    DataBaseAdapter dba;
    Models mod;
    Sync sData;
    SyncOffline sDataOffline;
    GPSTracker gps;
    ProgressDialog dialog;
    int syncFlag = 0;
    ToastService toastService;
    ProgressDialog waitDialog;
    JSONArray serverResponse;
    Dialog setting;
    Dialog trackVisit;
    int serverFlag = 0;
    String Data = "",str="";
    // boolean flag to toggle periodic location updates
    private boolean mRequestingLocationUpdates = false;
    private LocationRequest mLocationRequest;
    private PendingIntent pendingIntent;
    private PendingIntent pendingLocIntent;
    private AlarmManager manager;
    private AlarmManager managerL;
    Handler handler,handlerAlaram;

    SwipeRefreshLayout mSwipeRefreshLayout;
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

                    }
                    else
                    {

                        Toast.makeText(MenuActivity.this, "No User Found", Toast.LENGTH_SHORT).show();

                    }

                }

                dba.close();

                trackVisit = new Dialog(MenuActivity.this);
                // Set GUI of login screen
                trackVisit.requestWindowFeature(Window.FEATURE_NO_TITLE);
              /*  getWindow().setBackgroundDrawable(
                        new ColorDrawable(
                                android.graphics.Color.TRANSPARENT));*/

                trackVisit.setContentView(R.layout.cust_track_dialog);

                final TextView txtDate = (TextView) trackVisit
                        .findViewById(R.id.VisitDate);
                final Spinner spinCust = (Spinner) trackVisit
                        .findViewById(R.id.spinnerCustomer);
                Button btnSave1 = (Button) trackVisit
                        .findViewById(R.id.btnSave);
                Button btnCancel1 = (Button) trackVisit
                        .findViewById(R.id.btnCancel);
                ImageView imgClose1 = (ImageView) trackVisit
                        .findViewById(R.id.imageClose);

                ImageView imgBarcode = (ImageView) trackVisit
                        .findViewById(R.id.imageViewBarcode);

                imgBarcode.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(MenuActivity.this,BarcodeScannerActivity.class);
                        startActivityForResult(intent, 1);
                    }
                });
                currLatitude = gps.getLatitude();
                currLongitude = gps.getLongitude();

                ArrayList<String> customers = new ArrayList<String>();
                ArrayList<String> allCustomers = new ArrayList<String>();
                ArrayAdapter<String> adapterCustomer;
                int count = 0;


                toastService.stopTimer();

                dba.open();
                String sql1 = "select " + CustomerTable.KEY_NAME + "," + CustomerTable.KEY_LONGI + "," + CustomerTable.KEY_LATI +
                        " from " + CustomerTable.DATABASE_TABLE;
                Cursor custCur1 = DataBaseAdapter.ourDatabase.rawQuery(sql1, null);

                Log.w("CA", "SQL :" + sql);

                if (custCur1.getCount() > 0) {
                    while (custCur1.moveToNext()) {
                        allCustomers.add(custCur1.getString(0).trim());
                        if (CalculateDistance.distance(custCur1.getDouble(2), custCur1.getDouble(1), currLatitude, currLongitude) < Double.parseDouble(session.getDistance())) {
                            customers.add(custCur.getString(0).trim());
                            count++;
                        }
                    }

                    Log.w("MA", "Count: " + count);

                    if (count == 0) {
                        adapterCustomer = new ArrayAdapter<String>(MenuActivity.this,
                                android.R.layout.simple_spinner_item, allCustomers);
                        str = "Unable to find customer at this location please select manually";
                    } else if (count > 1) {
                        adapterCustomer = new ArrayAdapter<String>(MenuActivity.this,
                                android.R.layout.simple_spinner_item, customers);
                        str = "Multiple customers found please select correct one";
                    } else {
                        adapterCustomer = new ArrayAdapter<String>(MenuActivity.this,
                                android.R.layout.simple_spinner_item, customers);
                    }
                    adapterCustomer
                            .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinCust.setAdapter(adapterCustomer);

                }
                custCur.close();
                dba.close();


                Calendar c = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
                final String strDate = sdf.format(c.getTime());
                txtDate.setText(strDate);

                btnSave1.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String custId = "";
                        String strCust = spinCust.getSelectedItem().toString().trim();
                        dba.open();
                        String sqlCustNo = "select id from customer where name = '" + strCust + "'";
                        Cursor curCustNo = DataBaseAdapter.ourDatabase.rawQuery(sqlCustNo, null);
                        if (curCustNo.getCount() > 0) {
                            curCustNo.moveToFirst();
                            custId = curCustNo.getString(0).trim();
                        }
                        curCustNo.close();
                        dba.close();

                        Data = session.getEmpNo() + "," + custId + "," + strCust + "," + currLongitude + "," + currLatitude + "," + strDate;
                        serverFlag = 1;


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

                            Log.w("VisitStatus", "Online");

                        } else {
                            Log.w("VisitStatus", "Offline");
                            ContentValues cv = new ContentValues();
                            cv.put(OfflineTable.KEY_DATA, Data);
                            cv.put(OfflineTable.KEY_BRANCHNO, session.getBranchNo());
                            cv.put(OfflineTable.KEY_LONGITUDE, "N/A");
                            cv.put(OfflineTable.KEY_LATITUDE, "N/A");
                            cv.put(OfflineTable.KEY_METHOD, "CustVisitStatus");
                            dba.open();
                            mod.insertdata(OfflineTable.DATABASE_TABLE, cv);
                            dba.close();
                            trackVisit.cancel();
                        }



                    }
                });

                btnCancel1.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        trackVisit.cancel();
                    }
                });
                imgClose1.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        trackVisit.cancel();
                    }
                });
                Toast.makeText(MenuActivity.this, str, Toast.LENGTH_SHORT).show();
                trackVisit.show();

                ArrayAdapter myAdap = (ArrayAdapter) spinCust.getAdapter(); //cast to an ArrayAdapter

                int spinnerPosition = myAdap.getPosition(myString);

//set the default according to value
                spinCust.setSelection(spinnerPosition);
            }
        }
    }

    private final BroadcastReceiver abcd = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            finish();
        }
    };


    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu_activity);
        //	getActionBar().hide();
        locSession = new SessionLocation(this);

        registerReceiver(abcd, new IntentFilter("xyz"));
        waitDialog = new ProgressDialog(this);
        waitDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        waitDialog.setMessage("Please Wait");


        // First we need to check availability of play services
        if (checkPlayServices()) {

            Log.w("loc", "checkPlayServices()");

            // Building the GoogleApi client
            buildGoogleApiClient();

            createLocationRequest();
        }

        toastService = new ToastService(MenuActivity.this);


        // Retrieve a PendingIntent that will perform a broadcast
        Intent alarmIntent = new Intent(this, AlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, 0);

        // Retrieve a PendingIntent that will perform a broadcast
        Intent alarmLocation = new Intent(this, LocationReceiver.class);
        pendingLocIntent = PendingIntent.getBroadcast(this, 0, alarmLocation, 0);

        // References
        session = new SessionManager(this);
        dba = new DataBaseAdapter(this);
        mod = new Models();
        sData = new Sync();
        sDataOffline = new SyncOffline();

        dba.open();
        mod.clearDatabase(OrderTempTable.DATABASE_TABLE);
        mod.clearDatabase(OrderTable.DATABASE_TABLE);
        mod.clearDatabase(BillingTable.DATABASE_TABLE);
        mod.clearDatabase(ReturnTable.DATABASE_TABLE);
        dba.close();

        dialog = new ProgressDialog(this);
        dialog.setTitle("Syncing...");


        btnProducts = (Button) findViewById(R.id.btnProducts);
        btnDelivery = (Button) findViewById(R.id.btnDelivery);
        btnCustList = (Button) findViewById(R.id.btnCustList);
        btnOrder = (Button) findViewById(R.id.btnOrder);
        btnBilling = (Button) findViewById(R.id.btnBilling);
        btnReturn = (Button) findViewById(R.id.btnReturn);
        btnReceipt = (Button) findViewById(R.id.btnReceipt);
        btnSync = (Button) findViewById(R.id.btnSync);
        btnExit = (Button) findViewById(R.id.btnExit);
        btnHelp = (Button) findViewById(R.id.btnHelp);
        btnFeedback = (Button) findViewById(R.id.btnFeedBack);
        btnCustVisit = (Button) findViewById(R.id.btnCustVisit);
        btnVisitStatus = (Button) findViewById(R.id.btnVisitStatus);
        btnDiatance = (Button) findViewById(R.id.btnDistance);

        fifthLayout = (LinearLayout) findViewById(R.id.fifthRow);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.activity_main_swipe_refresh_layout);

        final Toast toast = Toast.makeText(MenuActivity.this,
                "Sync done Successfully!!!", Toast.LENGTH_LONG);

        final Toast sync = Toast.makeText(getApplicationContext(),
                "Please Sync First or ask admin to assign route", Toast.LENGTH_LONG);


        try {

            if (!session.isLoggedIn()) {

                Intent intent = new Intent(getApplicationContext(),
                        LoginActivity.class);
                startActivity(intent);
                finish();

            } else {

                gps = new GPSTracker(this);
                checkLocation();

                startA();
                startAlarm();

                syncFlag = 0;

                btnHelp.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        cancelAlarm();
                        //          cancelA();
                        syncFlag = 1;
                        dba.open();
                        Intent intent = new Intent(getApplicationContext(),
                                HelpActivity.class);
                        startActivity(intent);
                        finish();

                    }
                });


                btnFeedback.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getApplicationContext(),
                                FeedbackActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });

                btnCustVisit.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getApplicationContext(),
                                CustVisitActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });

                mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        mSwipeRefreshLayout.setRefreshing(false);


        /*                ConnectivityManager cm = ((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE));

                        if (cm == null) {
                            return;
                        }

                        // Now to check if we're actually connected
                        if (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected()) {
                            // Start the service to do our thing

                            dba.open();
                            Cursor cur = mod.getData(OfflineTable.DATABASE_TABLE);
                            int count = cur.getCount();
                            cur.close();
                            dba.close();
                            if(count > 0) {
                                new SyncDataOffline().execute();
                            }else{
                                mSwipeRefreshLayout.setRefreshing(false);
                                Toast.makeText(MenuActivity.this,"No Offline Data to Upload",Toast.LENGTH_LONG).show();
                            }

                            Log.w("mSwipeRefreshLayout", "Online");

                        } else {
                            mSwipeRefreshLayout.setRefreshing(false);
                            Log.w("mSwipeRefreshLayout", "Ofline");
                            Toast.makeText(MenuActivity.this,"Internet Connection not available. \n Turn ON internet",Toast.LENGTH_LONG).show();
                        }
*/
                    }
                });

                btnDiatance.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setting = new Dialog(MenuActivity.this);
                        // Set GUI of login screen
                        setting.requestWindowFeature(Window.FEATURE_NO_TITLE);
              /*  getWindow().setBackgroundDrawable(
                        new ColorDrawable(
                                android.graphics.Color.TRANSPARENT));*/

                        setting.setContentView(R.layout.setting_dialog);

                        Button btnIncrease = (Button) setting
                                .findViewById(R.id.btnIncrese);
                        Button btnDecrease = (Button) setting
                                .findViewById(R.id.btnDecrese);
                        Button btnSave = (Button) setting
                                .findViewById(R.id.btnSave);
                        Button btnCancel = (Button) setting
                                .findViewById(R.id.btnCancel);
                        ImageView imgClose = (ImageView) setting
                                .findViewById(R.id.imageClose);
                        final TextView txtDistance = (TextView) setting
                                .findViewById(R.id.txtDistance);

                        txtDistance.setBackgroundColor(Color.WHITE);

                        try {

                            int val = (int) (Double.parseDouble(session.getDistance()) / 0.000621371);

                            txtDistance.setText(String.valueOf(val));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        btnIncrease.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                int minteger = Integer.parseInt(txtDistance.getText().toString().trim());
                                minteger = minteger + 1;
                                txtDistance.setText(String.valueOf(minteger));
                            }
                        });

                        btnDecrease.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                int minteger = Integer.parseInt(txtDistance.getText().toString().trim());
                                int minDist = (int) Double.parseDouble(session.getMinDistance());
                                if (minteger >= minDist) {
                                    minteger = minteger - 1;
                                    txtDistance.setText(String.valueOf(minteger));
                                } else {
                                    Toast.makeText(MenuActivity.this, "Distance must be greater than " + session.getMinDistance() + " meter", Toast.LENGTH_LONG).show();
                                }
                            }
                        });

                        btnSave.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                int dis = Integer.parseInt(txtDistance.getText().toString().trim());
                                String userid = session.getUserId();
                                String password = session.getUserPassword();
                                String empno = session.getEmpNo();
                                String empname = session.getEmpName();
                                String branchno = session.getBranchNo();
                                String mindist = session.getMinDistance();
                                String addr = session.getAddresse();
                                String comp=session.getComputerName();
                                session.logoutUser();
                                session.createLoginSession(userid, password, empno, empname, branchno, String.valueOf(dis * 0.000621371), mindist,addr,comp);

                                serverFlag = 0;


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

                                    Log.w("Distance", "Online");

                                } else {

                                    setting.cancel();
                                }


                            }
                        });

                        btnCancel.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                setting.cancel();
                            }
                        });
                        imgClose.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                setting.cancel();
                            }
                        });

                        setting.show();
                    }
                });

                btnVisitStatus.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                       if (currLatitude != 0.0 && currLongitude != 0.0) {

                            trackVisit = new Dialog(MenuActivity.this);
                            // Set GUI of login screen
                            trackVisit.requestWindowFeature(Window.FEATURE_NO_TITLE);
              /*  getWindow().setBackgroundDrawable(
                        new ColorDrawable(
                                android.graphics.Color.TRANSPARENT));*/

                            trackVisit.setContentView(R.layout.cust_track_dialog);

                            final TextView txtDate = (TextView) trackVisit
                                    .findViewById(R.id.VisitDate);
                            final Spinner spinCust = (Spinner) trackVisit
                                    .findViewById(R.id.spinnerCustomer);
                            Button btnSave1 = (Button) trackVisit
                                    .findViewById(R.id.btnSave);
                            Button btnCancel1 = (Button) trackVisit
                                    .findViewById(R.id.btnCancel);
                            ImageView imgClose1 = (ImageView) trackVisit
                                    .findViewById(R.id.imageClose);

                        ImageView imgBarcode = (ImageView) trackVisit
                                .findViewById(R.id.imageViewBarcode);

                        imgBarcode.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(MenuActivity.this,FullScannerActivity.class);
                                spinCust.setSelection(0);
                                startActivityForResult(intent, 1);
                                trackVisit.dismiss();
                            }
                        });
                            currLatitude = gps.getLatitude();
                            currLongitude = gps.getLongitude();

                            ArrayList<String> customers = new ArrayList<String>();
                            ArrayList<String> allCustomers = new ArrayList<String>();
                            ArrayAdapter<String> adapterCustomer;
                            int count = 0;


                            toastService.stopTimer();

                            dba.open();
                            String sql = "select " + CustomerTable.KEY_NAME + "," + CustomerTable.KEY_LONGI + "," + CustomerTable.KEY_LATI +
                                    " from " + CustomerTable.DATABASE_TABLE;
                            Cursor custCur = DataBaseAdapter.ourDatabase.rawQuery(sql, null);

                            Log.w("CA", "SQL :" + sql);

                            if (custCur.getCount() > 0) {
                                while (custCur.moveToNext()) {
                                    allCustomers.add(custCur.getString(0).trim());
                                    if (CalculateDistance.distance(custCur.getDouble(2), custCur.getDouble(1), currLatitude, currLongitude) < Double.parseDouble(session.getDistance())) {
                                        customers.add(custCur.getString(0).trim());
                                        count++;
                                    }
                                }

                                Log.w("MA", "Count: " + count);

                                if (count == 0) {
                                    adapterCustomer = new ArrayAdapter<String>(MenuActivity.this,
                                            android.R.layout.simple_spinner_item, allCustomers);
                                    str = "Unable to find customer at this location please select manually";
                                } else if (count > 1) {
                                    adapterCustomer = new ArrayAdapter<String>(MenuActivity.this,
                                            android.R.layout.simple_spinner_item, customers);
                                    str = "Multiple customers found please select correct one";
                                } else {
                                    adapterCustomer = new ArrayAdapter<String>(MenuActivity.this,
                                            android.R.layout.simple_spinner_item, customers);
                                }
                                adapterCustomer
                                        .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                spinCust.setAdapter(adapterCustomer);

                            }
                            custCur.close();
                            dba.close();


                            Calendar c = Calendar.getInstance();
                            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
                            final String strDate = sdf.format(c.getTime());
                            txtDate.setText(strDate);

                            btnSave1.setOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    String custId = "";
                                    String strCust = spinCust.getSelectedItem().toString().trim();
                                    dba.open();
                                    String sqlCustNo = "select id from customer where name = '" + strCust + "'";
                                    Cursor curCustNo = DataBaseAdapter.ourDatabase.rawQuery(sqlCustNo, null);
                                    if (curCustNo.getCount() > 0) {
                                        curCustNo.moveToFirst();
                                        custId = curCustNo.getString(0).trim();
                                    }
                                    curCustNo.close();
                                    dba.close();

                                    Data = session.getEmpNo() + "," + custId + "," + strCust + "," + currLongitude + "," + currLatitude + "," + strDate;
                                    serverFlag = 1;


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

                                        Log.w("VisitStatus", "Online");

                                    } else {
                                        Log.w("VisitStatus", "Offline");
                                        ContentValues cv = new ContentValues();
                                        cv.put(OfflineTable.KEY_DATA, Data);
                                        cv.put(OfflineTable.KEY_BRANCHNO, session.getBranchNo());
                                        cv.put(OfflineTable.KEY_LONGITUDE, "N/A");
                                        cv.put(OfflineTable.KEY_LATITUDE, "N/A");
                                        cv.put(OfflineTable.KEY_METHOD, "CustVisitStatus");
                                        dba.open();
                                        mod.insertdata(OfflineTable.DATABASE_TABLE, cv);
                                        dba.close();
                                        trackVisit.cancel();
                                    }



                                }
                            });

                            btnCancel1.setOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    trackVisit.cancel();
                                }
                            });
                            imgClose1.setOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    trackVisit.cancel();
                                }
                            });
                            Toast.makeText(MenuActivity.this, str, Toast.LENGTH_SHORT).show();
                            trackVisit.show();
                        } else {
                            toastService.startTimer();
                        }
                    }
                });


                btnReturn.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        cancelAlarm();
                        //             cancelA();
                        syncFlag = 1;
                        if (gps.canGetLocation()) {

                            dba.open();
                            Cursor cur = mod.getData(CustomerTable.DATABASE_TABLE);
                            if (cur.getCount() > 0) {

                                String sql = "select insertt,print from authority where form = 'Sale Return'";
                                Cursor aut = DataBaseAdapter.ourDatabase
                                        .rawQuery(sql, null);
                                if (aut.getCount() > 0) {
                                    aut.moveToFirst();
                                    if (aut.getString(0).trim().equalsIgnoreCase("Yes") && aut.getString(1).trim().equalsIgnoreCase("Yes")) {
                                        Intent intent = new Intent(getApplicationContext(),
                                                RetAct.class);
                                        //	intent.putExtra("act", "return");
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        Toast.makeText(getApplicationContext(),
                                                "You dont have Authority",
                                                Toast.LENGTH_LONG).show();
                                    }
                                }
                                aut.close();

                            } else {
                                Toast.makeText(getApplicationContext(),
                                        "Please Sync First", Toast.LENGTH_LONG).show();
                            }
                            cur.close();
                            dba.close();

                        } else {
                            gps.showSettingsAlert();
                        }
                    }
                });

                btnReceipt.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        cancelAlarm();
                        //             cancelA();
                        syncFlag = 1;
                        if (gps.canGetLocation()) {
                            checkLocation();
                            dba.open();
                            Cursor cur = mod.getData(CustomerTable.DATABASE_TABLE);
                            if (cur.getCount() > 0) {

                                String sql = "select insertt,print from authority where form = 'Receipt Search'";
                                Cursor aut = DataBaseAdapter.ourDatabase
                                        .rawQuery(sql, null);
                                if (aut.getCount() > 0) {
                                    aut.moveToFirst();
                                    if (aut.getString(0).trim().equalsIgnoreCase("Yes") && aut.getString(1).trim().equalsIgnoreCase("Yes")) {
                                        Intent intent = new Intent(getApplicationContext(),
                                                ReceiptActivity.class);
                                        //	intent.putExtra("act", "receipt");
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        Toast.makeText(getApplicationContext(),
                                                "You dont have Authority",
                                                Toast.LENGTH_LONG).show();
                                    }
                                }
                                aut.close();

                            } else {
                                sync.show();
                            }
                            cur.close();
                            dba.close();
                        } else {
                            gps.showSettingsAlert();
                        }
                    }
                });

                btnOrder.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        cancelAlarm();
                        //            cancelA();
                        syncFlag = 1;
                        if (gps.canGetLocation()) {

                            dba.open();
                            Cursor cur = mod.getData(CustomerTable.DATABASE_TABLE);
                            if (cur.getCount() > 0) {
                                String sql = "select insertt,print from authority where form = 'Order Search'";
                                Cursor aut = DataBaseAdapter.ourDatabase
                                        .rawQuery(sql, null);
                                if (aut.getCount() > 0) {
                                    aut.moveToFirst();
                                    if (aut.getString(0).trim().equalsIgnoreCase("Yes") && aut.getString(1).trim().equalsIgnoreCase("Yes")) {
                                        Intent intent = new Intent(getApplicationContext(), OrderListActivity.class);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        Toast.makeText(getApplicationContext(),
                                                "You dont have Authority",
                                                Toast.LENGTH_LONG).show();
                                    }
                                }
                                aut.close();
                            } else {
                                sync.show();
                            }
                            cur.close();
                            dba.close();
                        } else {
                            gps.showSettingsAlert();
                        }

                    }
                });

                btnDelivery.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        cancelAlarm();
                        //              cancelA();
                        syncFlag = 1;
                        if (gps.canGetLocation()) {
                            //  checkLocation();
                            dba.open();
                            Cursor cur = mod.getData(CustomerTable.DATABASE_TABLE);
                            if (cur.getCount() > 0) {
                                String sql = "select insertt,print from authority where form = 'Delivery'";
                                Cursor aut = DataBaseAdapter.ourDatabase
                                        .rawQuery(sql, null);
                                if (aut.getCount() > 0) {
                                    aut.moveToFirst();
                                    if (aut.getString(0).trim().equalsIgnoreCase("Yes") && aut.getString(1).trim().equalsIgnoreCase("Yes")) {
                                        Intent intent = new Intent(getApplicationContext(),
                                                DeliveryActivity.class);
                                        //	intent.putExtra("act", "delivery");
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        Toast.makeText(getApplicationContext(),
                                                "You dont have Authority",
                                                Toast.LENGTH_LONG).show();
                                    }
                                }
                                aut.close();

                            } else {
                                sync.show();
                            }
                            cur.close();
                            dba.close();
                        } else {
                            gps.showSettingsAlert();
                        }
                    }
                });

                btnBilling.setOnClickListener(new OnClickListener() {

                                                  @Override
                                                  public void onClick(View v) {
                                                      // TODO Auto-generated method stub
                                                      cancelAlarm();
                                                      //                                          cancelA();
                                                      syncFlag = 1;
                                                      if (gps.canGetLocation()) {
                                                          checkLocation();
                                                          dba.open();
                                                          Cursor cur = mod.getData(CustomerTable.DATABASE_TABLE);
                                                          if (cur.getCount() > 0) {
                                                              String sql = "select insertt,print from authority where form = 'Bill'";
                                                              Cursor aut = DataBaseAdapter.ourDatabase
                                                                      .rawQuery(sql, null);
                                                              if (aut.getCount() > 0) {
                                                                  aut.moveToFirst();
                                                                  if (aut.getString(0).trim().equalsIgnoreCase("Yes") && aut.getString(1).trim().equalsIgnoreCase("Yes")) {
                                                                      Intent intent = new Intent(getApplicationContext(),
                                                                              BillingActivity.class);
                                                                      startActivity(intent);
                                                                      finish();
                                                                  } else {
                                                                      Toast.makeText(getApplicationContext(),
                                                                              "You dont have Authority",
                                                                              Toast.LENGTH_LONG).show();
                                                                  }
                                                              }
                                                              aut.close();
                                                          } else {
                                                              sync.show();
                                                          }
                                                          cur.close();
                                                          dba.close();
                                                      } else {
                                                          gps.showSettingsAlert();
                                                      }
                                                  }
                                              }

                );

                btnProducts.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        cancelAlarm();
                        //              cancelA();
                        syncFlag = 1;
                        dba.open();
                        Cursor cur = mod.getData(ProductTable.DATABASE_TABLE);
                        if (cur.getCount() > 0) {
                            Intent intent = new Intent(getApplicationContext(),
                                    ProductsActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            sync.show();
                        }
                        cur.close();
                        dba.close();

                    }
                });

                btnCustList.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        cancelAlarm();
                        //              cancelA();
                        syncFlag = 1;
                        Intent intent = new Intent(getApplicationContext(),
                                CustomerActivity.class);
                        intent.putExtra("act", "customer");
                        startActivity(intent);
                        finish();


                    }
                });

                btnSync.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub

                        ConnectivityManager cm = ((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE));

                        if (cm == null) {
                            return;
                        }

                        // Now to check if we're actually connected
                        if (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected()) {
                            // Start the service to do our thing
                            Log.w("Sync", "Online");

                            final String[] opt = new String[]{
                                    "Download Data?",
                                    "Upload Data?"
                            };

                            AlertDialog.Builder alertdialogbuilder = new AlertDialog.Builder(MenuActivity.this);

                            alertdialogbuilder.setTitle("Select Sync Type");

                            alertdialogbuilder.setItems(opt, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String selectedText = Arrays.asList(opt).get(which);
                                    if(selectedText.equalsIgnoreCase("Download Data?")){
                                        //    Log.w("MenuActivity","Option: "+selectedText);
                                        new SyncData().execute();
                                    }else if(selectedText.equalsIgnoreCase("Upload Data?")){
                                        //    Log.w("MenuActivity","Option: "+selectedText);
                                        dba.open();
                                        Cursor cur = mod.getData(OfflineTable.DATABASE_TABLE);
                                        int count = cur.getCount();
                                        cur.close();
                                        dba.close();
                                        if(count > 0) {
                                            new SyncDataOffline().execute();
                                        }else{
                                            mSwipeRefreshLayout.setRefreshing(false);
                                            Toast.makeText(MenuActivity.this,"No Offline Data to Upload",Toast.LENGTH_LONG).show();
                                        }
                                    }
                                    //   Toast.makeText(getApplicationContext(),selectedText,Toast.LENGTH_LONG).show();
                                }
                            });

                            AlertDialog dialog = alertdialogbuilder.create();
                            // dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            dialog.show();


                        } else {
                            mSwipeRefreshLayout.setRefreshing(false);
                            Log.w("Sync", "Ofline");
                            Toast.makeText(MenuActivity.this,"Internet Connection not available. \n Turn ON internet",Toast.LENGTH_LONG).show();
                        }


                    //    new SyncData().execute();

                    }
                });

                btnExit.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        syncFlag = 1;
                        cancelAlarm();
                        cancelA();
                        session.logoutUser();
                        Intent startMain = new Intent(Intent.ACTION_MAIN);
                        startMain.addCategory(Intent.CATEGORY_HOME);
                        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(startMain);
                        finish();
                        android.os.Process.killProcess(android.os.Process.myPid());
                        System.exit(1);

                    }
                });
            }

        }catch (Exception e){
            e.printStackTrace();
            LogError lErr = new LogError();
            lErr.appendLog(e.toString());
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    /**
     * Creating google api client object
     */
    protected synchronized void buildGoogleApiClient() {
        Log.w("loc", "buildGoogleApiClient()");
        mGoogleApiClient = new GoogleApiClient.Builder(MenuActivity.this)
                .addConnectionCallbacks(MenuActivity.this)
                .addOnConnectionFailedListener(MenuActivity.this)
                .addApi(LocationServices.API).build();
    }

    /**
     * Creating location request object
     */
    protected void createLocationRequest() {
        Log.w("loc", " createLocationRequest()");
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FATEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        //      mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    /**
     * Method to verify google play services on the device
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(MenuActivity.this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, MenuActivity.this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Toast.makeText(getApplicationContext(),
                        "This device is not supported.", Toast.LENGTH_LONG)
                        .show();
                finish();
            }
            return false;
        }
        return true;
    }

    /**
     * Starting the location updates
     */
    protected void startLocationUpdates() {

        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, MenuActivity.this);

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
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = "
                + result.getErrorCode());
    }

    @Override
    public void onConnected(Bundle arg0) {
        Log.w("loc", "Connected");
        // Once connected with google api, get the location
        //   displayLocation();
        flagApiIsConnected = 1;

//        checkLocation();

      /*  startLocationUpdates();
        startA();             //LocationReceiver
        startAlarm();
*/


        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        // Assign the new location

   /*     if (syncFlag == 0) {

            mLastLocation = location;

            double latitude = mLastLocation.getLatitude();
            double longitude = mLastLocation.getLongitude();

       *//* Toast.makeText(getApplicationContext(), "Location changed!\nLongitude: "+longitude+"\nLatitude: "+latitude,
                Toast.LENGTH_SHORT).show();
*//*
            Calendar c = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
            String strDate = sdf.format(c.getTime());

            Log.w("MenuActivity", "CurrentLongitude: " + longitude);
            Log.w("MenuActivity", "CurrentLatitude: " + latitude);



            ContentValues cv = new ContentValues();
            cv.put(LocationTable.KEY_LONGITUDE, longitude);
            cv.put(LocationTable.KEY_LATITUDE, latitude);
            cv.put(LocationTable.KEY_DATE, strDate);

            if (latitude != 0 || longitude != 0) {
                if (locSession.isLoggedIn()) {
                    if (latitude != Double.parseDouble(locSession.getLati()) && longitude != Double.parseDouble(locSession.getLongi())) {

                        Log.w("MenuActivity", "Location Insert");


                        locSession.logoutUser();
                        locSession.createLocationSession(String.valueOf(longitude), String.valueOf(latitude));
                        dba.open();
                        mod.insertdata(LocationTable.DATABASE_TABLE, cv);
                        dba.close();
                    }
                } else {
                    Log.w("MenuActivity", "Location Insert 1");
                    locSession.createLocationSession(String.valueOf(longitude), String.valueOf(latitude));
                    dba.open();
                    mod.insertdata(LocationTable.DATABASE_TABLE, cv);
                    dba.close();
                }
            }

        }*/
    }


//********************Alarm Manager **************

    /*public Runnable runLocation2 = new Runnable(){
        @Override
        public void run() {
            session = new SessionManager(getApplicationContext());
            dba = new DataBaseAdapter(getApplicationContext());
            mod = new Models();
            locSession = new SessionLocation(getApplicationContext());
            try {
                gps = new GPSTracker(getApplicationContext());

                currLatitude = gps.getLatitude();
                currLongitude = gps.getLongitude();
                Log.w("AlarmReceiver", "CurrentLongitude: " + currLatitude );
                Log.w("AlarmReceiver", "CurrentLongitude: " + currLongitude);

                dba.open();
                Cursor curLoc = mod.getData(LocationTable.DATABASE_TABLE);
                if (curLoc.getCount() > 0) {
                    int cnt = 1;
                    double longi = 0;
                    double lati = 0;
                    while (curLoc.moveToNext()) {
                        LogError lErr = new LogError();
                        lErr.appendLog(cnt+"  Longi: "+curLoc.getDouble(0)+"  Lati: "+curLoc.getDouble(1));

                        Data = Data+curLoc.getString(0) + "," + curLoc.getString(1) + "," + curLoc.getString(2) + "$";

                        Log.e("Tracking1",""+Data);

                        //Toast.makeText(context, "Tracking1 : "+Data.toString(), Toast.LENGTH_SHORT).show();

                        cnt++;
                    *//*if(cnt == 6) {
                        longi = longi / 5;
                        longi = preLon + longi;
                        lati = lati / 5;
                        lati = preLat + lati;
                        Data = Data + longi + "," + lati + "," + curLoc.getString(2) + "$";
                        cnt = 1;
                        longi = 0;
                        lati = 0;

                    }*//*
                    }
                    AsyncCallWSGPS task = new AsyncCallWSGPS();
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
            MenuActivity.this.handlerAlaram.postDelayed(MenuActivity.this.runLocation2, 60000);
        }
    };*/

    public void startAlarm() {
       /* manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        int interval = 1000 * 60; // 1 min

        manager.setRepeating(AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis(), interval, pendingIntent);
        Toast.makeText(MainActivity.this, "Alarm Set", Toast.LENGTH_SHORT).show();*/
        //handlerAlaram = new Handler();
        //handlerAlaram.postDelayed(runLocation2, 60000);
    }

    public void cancelAlarm() {
        /*if (manager != null) {
            manager.cancel(pendingIntent);
            Toast.makeText(MainActivity.this, "Alarm Canceled", Toast.LENGTH_SHORT).show();
        }*/
        //handlerAlaram.removeCallbacks(runLocation2);
    }

    private class AsyncCallWSGPS extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            // Call Web Method
            try {

                Log.w("Alarm Receiver", "Start");
                dba.open();

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

    //***************Location Service**************

    public Runnable runLocation = new Runnable(){
        @Override
        public void run() {

            session = new SessionManager(getApplicationContext());
            dba = new DataBaseAdapter(getApplicationContext());
            mod = new Models();
            locSession = new SessionLocation(getApplicationContext());
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

                    GPSTracker gps = new GPSTracker(getApplicationContext());
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

                                    //Toast.makeText(conn , "Tracking : "+ str.toString(), Toast.LENGTH_SHORT).show();

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
                //Toast.makeText(conn , "GPS Tracker Error : " + e.toString(), Toast.LENGTH_SHORT).show();
            }

            //MenuActivity.this.handler.postDelayed(MenuActivity.this.runLocation, 2000);
        }
    };


    public void startA() {
        /*managerL = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        int interval1 = 1000 * 5; // 5 sec

        managerL.setRepeating(AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis(), interval1, pendingLocIntent);
        Toast.makeText(MainActivity.this, "Alarm Set Loc", Toast.LENGTH_SHORT).show();*/
        //handler = new Handler();
        //handler.postDelayed(runLocation, 2000);
    }

    public void cancelA() {
        /*if (manager != null) {
            manager.cancel(pendingLocIntent);
            Toast.makeText(MainActivity.this, "Alarm Canceled Loc", Toast.LENGTH_SHORT).show();
        }*/
        //handler.removeCallbacks(runLocation);
    }


    public void checkLocation() {
        if (gps.canGetLocation()) {

            mLastLocation = LocationServices.FusedLocationApi
                    .getLastLocation(mGoogleApiClient);


            try {

                currLatitude = gps.getLatitude();
                currLongitude = gps.getLongitude();

                if (currLatitude != 0.0 && currLongitude != 0.0) {

                    toastService.stopTimer();

                    dba.open();
                    String sql = "select " + CustomerTable.KEY_ID + "," + CustomerTable.KEY_LONGI + "," + CustomerTable.KEY_LATI + " from "
                            + CustomerTable.DATABASE_TABLE;
                    Cursor custCur = DataBaseAdapter.ourDatabase.rawQuery(sql, null);

                    Log.w("CA", "SQL :" + sql);

                    if (custCur.getCount() > 0) {
                        //	custCur.moveToFirst();
                        while (custCur.moveToNext()) {
                            /*
                            Log.w("MA", "custName :" + custCur.getString(0).trim());
                            Log.w("MA", "custLati :" + custCur.getString(2));
                            Log.w("MA", "custLongi :" + custCur.getString(1));

                            Log.w("MA", "currentLati :" + currLatitude);
                            Log.w("MA", "currentLongi :" + currLongitude);*/

                            if (CalculateDistance.distance(custCur.getDouble(2), custCur.getDouble(1), currLatitude, currLongitude) < Double.parseDouble(session.getDistance())) {
                                custid = custCur.getString(0).trim();
                                Log.w("DA", "custId :" + custid);
                            }
                        }
                    }
                    custCur.close();
                    dba.close();
                } else {
                    toastService.startTimer();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            gps.showSettingsAlert();

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        toastService.stopTimer();
        unregisterReceiver(abcd);
    }



    class SyncData extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... strings) {
            try {
                dba.open();
                sData.syncdata(MenuActivity.this);
                dba.close();
            } catch (Exception e) {
                e.printStackTrace();
            }


            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            cancelAlarm();
            cancelA();
            syncFlag = 1;
            dialog.setMessage("Getting Data From Server");
            dialog.show();
            dialog.setCanceledOnTouchOutside(false);

        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            startAlarm();
            startA();
            dialog.cancel();
            syncFlag = 0;
            Toast.makeText(MenuActivity.this,
                    "Sync done Successfully!!!", Toast.LENGTH_LONG).show();
        }
    }


    class SyncDataOffline extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... strings) {
            try {
                sDataOffline.syncdata(MenuActivity.this);
            } catch (Exception e) {
                e.printStackTrace();
            }


            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            cancelAlarm();
            cancelA();
            syncFlag = 1;
            dialog.setMessage("Uploading Data to Server");
            dialog.show();
            dialog.setCanceledOnTouchOutside(false);

        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            startAlarm();
            startA();
            dialog.cancel();
            mSwipeRefreshLayout.setRefreshing(false);
            syncFlag = 0;
            Toast.makeText(MenuActivity.this,
                    "Sync done Successfully!!!", Toast.LENGTH_LONG).show();
        }
    }



    class AsyncCallWS extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... strings) {
            try {
                dba.open();
                if (serverFlag == 0) {
                    int val = (int) (Double.parseDouble(session.getDistance()) / 0.000621371);
                    serverResponse = WebService.sendDistance(session.getEmpNo(), session.getBranchNo(), String.valueOf(val), "SetDistance");
                } else if (serverFlag == 1) {
                    serverResponse = WebService.putData(Data, session.getBranchNo(), "CustVisitStatus");
                }
                dba.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            cancelAlarm();
            cancelA();
            syncFlag = 1;
            waitDialog.show();
            waitDialog.setCanceledOnTouchOutside(false);

        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            startAlarm();
            startA();
            waitDialog.cancel();
            syncFlag = 0;
            try {
                if (serverResponse.getJSONObject(0)
                        .getString("Status").trim()
                        .equalsIgnoreCase("No")) {
                    Toast.makeText(MenuActivity.this,
                            "Failed to save Data", Toast.LENGTH_LONG).show();
                } else if (serverResponse.getJSONObject(0).getString("Status").equalsIgnoreCase("Success")) {
                    Toast.makeText(MenuActivity.this,
                            "Data Saved Successfully!!!", Toast.LENGTH_LONG).show();
                    if(serverFlag == 0) {
                        setting.cancel();
                    }else if(serverFlag == 1){
                        trackVisit.cancel();
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}