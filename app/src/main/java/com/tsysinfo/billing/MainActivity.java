package com.tsysinfo.billing;

import android.annotation.SuppressLint;
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
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
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
import com.tsysinfo.billing.database.WebService;

import org.json.JSONArray;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,

        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    DrawerLayout drawerLayout;
    ActionBarDrawerToggle drawerToggle;
    NavigationView navigation;
    private static final String TAG = MainActivity.class.getSimpleName();

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;

    public static Location mLastLocation;

    // Google client to interact with Google API
    public static GoogleApiClient mGoogleApiClient;
    static int flagApiIsConnected = 0;
    int count = 0;

    public static String OrderFlag = "S";
    // Location updates intervals in sec
    private static int UPDATE_INTERVAL = 1000; // 5 sec
    private static int FATEST_INTERVAL = 1000; // 3 sec
    private static int DISPLACEMENT = 5; // 5 meters
    Handler handler, handlerAlaram;
    LinearLayout btnDelivery, btnCustList, btnOrder, btnBilling,
            btnReturn, btnSync, btnExit, btnReceipt, btnHelp, btnVisitStatus,
            btnDiatance, btnFeedback, btnCustVisit;
    ImageView btnProducts;
    Toast sync;
    ArrayList<String> customers;
    ArrayList<String> allCustomers;
    ArrayAdapter<String> adapterCustomer;
    LinearLayout fifthLayout;
    Double currLatitude = 0.0;
    Double currLongitude = 0.0;
    String custid = "0";
    SessionManager session;
    SessionLocation locSession;
    DataBaseAdapter dba, dba1;
    Models mod, mod1;
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
    String Data = "", DataGPS = "", str = "";
    // boolean flag to toggle periodic location updates
    private boolean mRequestingLocationUpdates = false;
    private LocationRequest mLocationRequest;
    private PendingIntent pendingIntent;
    private PendingIntent pendingLocIntent;
    private AlarmManager manager;
    private AlarmManager managerL;
    SwipeRefreshLayout mSwipeRefreshLayout;
    private String GLOBAL_CUST_ID;
    private TextView textVi3login;
   /* String latitude;
    String longitude;*/

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
                final Cursor custCur = DataBaseAdapter.ourDatabase.rawQuery(sql, null);
                if (custCur.getCount() > 0) {
                    custCur.moveToFirst();
                    myString = custCur.getString(custCur.getColumnIndex(CustomerTable.KEY_NAME));

                } else {
                    String temp = GLOBAL_CUST_ID;
                    temp = temp.replaceAll("[^\\d.]", "");
                    if (temp.equalsIgnoreCase("")) {
                        temp = "1234567";
                    }
                    String sql1 = "select " + CustomerTable.KEY_NAME + " from "
                            + CustomerTable.DATABASE_TABLE + " where " + CustomerTable.KEY_ID + "=" + temp + " ";
                    Cursor custCur1 = DataBaseAdapter.ourDatabase.rawQuery(sql1, null);

                    if (custCur1.getCount() > 0) {
                        custCur1.moveToFirst();
                        myString = custCur1.getString(custCur1.getColumnIndex(CustomerTable.KEY_NAME));

                    } else {

                        Toast.makeText(MainActivity.this, "No User Found", Toast.LENGTH_SHORT).show();

                    }

                }

                dba.close();

                trackVisit = new Dialog(MainActivity.this);
                // Set GUI of login screen
                trackVisit.requestWindowFeature(Window.FEATURE_NO_TITLE);
              /*  getWindow().setBackgroundDrawable(
                        new ColorDrawable(
                                android.graphics.Color.TRANSPARENT));*/

                trackVisit.setContentView(R.layout.cust_track_dialog);

                final TextView txtDate = (TextView) trackVisit.findViewById(R.id.VisitDate);
                final Spinner spinCust = (Spinner) trackVisit.findViewById(R.id.spinnerCustomer);
                Button btnSave1 = (Button) trackVisit.findViewById(R.id.btnSave);
                Button btnCancel1 = (Button) trackVisit.findViewById(R.id.btnCancel);
                ImageView imgClose1 = (ImageView) trackVisit.findViewById(R.id.imageClose);

                ImageView imgBarcode = (ImageView) trackVisit.findViewById(R.id.imageViewBarcode);


                Button refresh = (Button) trackVisit
                        .findViewById(R.id.refresh);

                refresh.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        allCustomers.clear();
                        adapterCustomer.clear();

                        dba.open();
                        String sql1 = "select " + CustomerTable.KEY_NAME + "," + CustomerTable.KEY_LONGI + "," + CustomerTable.KEY_LATI +
                                " from " + CustomerTable.DATABASE_TABLE;
                        Cursor custCur1 = DataBaseAdapter.ourDatabase.rawQuery(sql1, null);

                        currLatitude = gps.getLatitude();
                        currLongitude = gps.getLongitude();
                        count = 0;
                        if (custCur1.getCount() > 0) {
                            while (custCur1.moveToNext()) {
                                allCustomers.add(custCur1.getString(0).trim());
                                if (CalculateDistance.distance(custCur1.getDouble(2), custCur1.getDouble(1), currLatitude, currLongitude) < Double.parseDouble(session.getDistance())) {
                                    customers.add(custCur1.getString(0).trim());
                                    count++;
                                }
                            }
                            Log.w("MA", "Count: " + count);

                            if (count == 0) {
                                allCustomers.clear();
                                adapterCustomer = new ArrayAdapter<String>(MainActivity.this,
                                        android.R.layout.simple_spinner_item, allCustomers);
                                adapterCustomer.notifyDataSetChanged();
                                str = "Unable to find customer at this location please select manually";
                            } else if (count > 1) {
                                adapterCustomer = new ArrayAdapter<String>(MainActivity.this,
                                        android.R.layout.simple_spinner_item, customers);
                                adapterCustomer.notifyDataSetChanged();
                                str = "Multiple customers found please select correct one";
                                Toast.makeText(MainActivity.this, str, Toast.LENGTH_LONG).show();
                            } else {
                                adapterCustomer = new ArrayAdapter<String>(MainActivity.this,
                                        android.R.layout.simple_spinner_item, customers);
                                adapterCustomer.notifyDataSetChanged();
                            }

                            adapterCustomer
                                    .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinCust.setAdapter(adapterCustomer);
                            adapterCustomer.notifyDataSetChanged();

                        }
                        custCur.close();
                        dba.close();

                    }
                });


                imgBarcode.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(MainActivity.this, BarcodeScannerActivity.class);
                        trackVisit.dismiss();
                        startActivityForResult(intent, 1);
                    }
                });
                currLatitude = gps.getLatitude();
                currLongitude = gps.getLongitude();

                customers = new ArrayList<String>();
                allCustomers = new ArrayList<String>();

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
                            customers.add(custCur1.getString(0).trim());
                            count++;
                        }
                    }
                    if (!customers.contains(myString)) {
                        customers.add(myString.trim());

                    }


                    Log.w("MA", "Count: " + count);

                    if (count == 0) {
                        adapterCustomer = new ArrayAdapter<String>(MainActivity.this,
                                android.R.layout.simple_spinner_item, allCustomers);
                        str = "Unable to find customer at this location please select manually";
                    } else if (count > 1) {
                        adapterCustomer = new ArrayAdapter<String>(MainActivity.this,
                                android.R.layout.simple_spinner_item, customers);
                        str = "Multiple customers found please select correct one";
                    } else {
                        adapterCustomer = new ArrayAdapter<String>(MainActivity.this,
                                android.R.layout.simple_spinner_item, customers);
                    }

                    adapterCustomer
                            .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinCust.setAdapter(adapterCustomer);

                }
                custCur.close();
                dba.close();


                Calendar c = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
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
                Toast.makeText(MainActivity.this, str, Toast.LENGTH_SHORT).show();
                trackVisit.show();

                ArrayAdapter myAdap = (ArrayAdapter) spinCust.getAdapter(); //cast to an ArrayAdapter

                int spinnerPosition = myAdap.getPosition(myString);
//set the default according to value
                spinCust.setSelection(spinnerPosition);
            }
        }
    }

    public void setDrawerLayoutEnable(boolean what) {
        if (what) {
            // Drawer will be open through swipe by user
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        } else {
            // User will not be able to open Drawer but it can be open from application programmatically
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
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
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initInstances();

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

        toastService = new ToastService(MainActivity.this);


        /*// Retrieve a PendingIntent that will perform a broadcast
        Intent alarmIntent = new Intent(this, AlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, 0);

        // Retrieve a PendingIntent that will perform a broadcast
        Intent alarmLocation = new Intent(this, LocationReceiver.class);
        pendingLocIntent = PendingIntent.getBroadcast(this, 0, alarmLocation, 0);*/
        // References

        session = new SessionManager(this);
        dba = new DataBaseAdapter(this);
        dba1 = new DataBaseAdapter(this);
        mod = new Models();
        sData = new Sync();
        sDataOffline = new SyncOffline();


        textVi3login = (TextView) findViewById(R.id.textVi3);
        textVi3login.setText(session.getEmpName());

        dba.open();
        //mod.clearDatabase(OrderTempTable.DATABASE_TABLE);
        //mod.clearDatabase(OrderTable.DATABASE_TABLE);
        mod.clearDatabase(BillingTable.DATABASE_TABLE);
        mod.clearDatabase(ReturnTable.DATABASE_TABLE);
        dba.close();

        dialog = new ProgressDialog(this);
        dialog.setTitle("Syncing...");
        final Toast toast = Toast.makeText(MainActivity.this,
                "Sync done Successfully!!!", Toast.LENGTH_LONG);

        sync = Toast.makeText(getApplicationContext(),
                "Please Sync First or ask admin to assign route", Toast.LENGTH_LONG);


        btnProducts = (ImageView) findViewById(R.id.btnProducts);

        //    mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.activity_main_swipe_refresh_layout);

        btnProducts.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                cancelAlarm();
                //              cancelA();
                syncFlag = 1;
                dba.open();
                Cursor cur = mod.getData(ProductTable.DATABASE_TABLE);
                if (cur.getCount() > 0) {
                    Intent intent = new Intent(getApplicationContext(), ProductsActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    sync.show();
                }
                cur.close();
                dba.close();

            }
        });
        btnDelivery = (LinearLayout) findViewById(R.id.btnDelivery);
        btnDelivery.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
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
        btnCustList = (LinearLayout) findViewById(R.id.btnCustList);
        btnOrder = (LinearLayout) findViewById(R.id.btnOrder);
        btnBilling = (LinearLayout) findViewById(R.id.btnBilling);
        btnBilling.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
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
        });
        btnReturn = (LinearLayout) findViewById(R.id.btnReturn);
        btnReceipt = (LinearLayout) findViewById(R.id.btnReceipt);
        btnSync = (LinearLayout) findViewById(R.id.btnSync);
        btnExit = (LinearLayout) findViewById(R.id.btnExit);
        btnHelp = (LinearLayout) findViewById(R.id.btnHelp);
        btnHelp.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                cancelAlarm();
                //          cancelA();
                syncFlag = 1;
                Intent intent2 = new Intent(getApplicationContext(), HelpActivity.class);
                startActivity(intent2);
                finish();

            }
        });
        btnFeedback = (LinearLayout) findViewById(R.id.btnFeedBack);
        btnCustVisit = (LinearLayout) findViewById(R.id.btnCustVisit);
        btnVisitStatus = (LinearLayout) findViewById(R.id.btnVisitStatus);
        btnDiatance = (LinearLayout) findViewById(R.id.btnDistance);
        fifthLayout = (LinearLayout) findViewById(R.id.fifthRow);


        // btnVisitStatus.setEnabled(false);
        // mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.activity_main_swipe_refresh_layout);


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

              /*  mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        mSwipeRefreshLayout.setRefreshing(false);*/


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
                /*    }
                });*/


                btnSync.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ConnectivityManager cm = ((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE));

                        if (cm == null) {
                            return;
                        }

                        // Now to check if we're actually connected
                        if (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected()) {
                            // Start the service to do our thing
                            Log.w("Sync", "Online");

                            final String[] opt = new String[]{
                                    "Download Data",
                                    "Download Product",
                                    "Download Product Images",
                                    "Upload Data"
                            };

                            AlertDialog.Builder alertdialogbuilder = new AlertDialog.Builder(MainActivity.this);

                            alertdialogbuilder.setTitle("Select Sync Type");

                            alertdialogbuilder.setItems(opt, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String selectedText = Arrays.asList(opt).get(which);
                                    if (selectedText.equalsIgnoreCase("Download Data")) {
                                        //    Log.w("MenuActivity","Option: "+selectedText);
                                        new SyncData().execute();
                                    } else if (selectedText.equalsIgnoreCase("Upload Data")) {
                                        //    Log.w("MenuActivity","Option: "+selectedText);
                                        dba.open();
                                        Cursor cur = mod.getData(OfflineTable.DATABASE_TABLE);
                                        int count = cur.getCount();
                                        cur.close();
                                        dba.close();
                                        if (count > 0) {
                                            new SyncDataOffline().execute();
                                        } else {
                                            //mSwipeRefreshLayout.setRefreshing(false);
                                            Toast.makeText(MainActivity.this, "No Offline Data to Upload", Toast.LENGTH_LONG).show();
                                        }
                                    } else if (selectedText.equalsIgnoreCase("Download Product")) {
                                        new SyncProduc().execute();

                                    } else if (selectedText.equalsIgnoreCase("Download Product Images")) {
                                        new DownloadImages().execute();

                                    }
                                    //   Toast.makeText(getApplicationContext(),selectedText,Toast.LENGTH_LONG).show();
                                }
                            });

                            AlertDialog dialog = alertdialogbuilder.create();
                            // dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            dialog.show();


                        } else {

                            //  mSwipeRefreshLayout.setRefreshing(false);
                            Log.w("Sync", "Ofline");
                            Toast.makeText(MainActivity.this, "Internet Connection not available. \n Turn ON internet", Toast.LENGTH_LONG).show();
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
                        OrderFlag = "S";
                        if (gps.canGetLocation()) {

                            dba.open();
                            Cursor cur = mod.getData(CustomerTable.DATABASE_TABLE);
                            cur.moveToNext();
                            if (cur.getCount() > 0) {
                                String sql = "select insertt,print from authority where form = 'Order Search'";
                                Cursor aut = DataBaseAdapter.ourDatabase.rawQuery(sql, null);

                                if (aut.getCount() > 0) {
                                    aut.moveToFirst();

                                    dba.open();
                                    String sql1 = "select * from temp";
                                    Cursor cur1 = DataBaseAdapter.ourDatabase.rawQuery(sql1, null);
                                    if (cur1.getCount() > 0) {
                                        DialogBox();
                                    } else {

                                        Intent intent = new Intent(getApplicationContext(), OrderActivity.class);
                                        startActivity(intent);
                                        finish();

                                    }
                                }
                                dba.close();
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
                btnCustList.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        cancelAlarm();
                        //              cancelA();
                        syncFlag = 1;
                        Intent intent3 = new Intent(getApplicationContext(), CustomerActivity.class);
                        intent3.putExtra("act", "customer");
                        startActivity(intent3);
                        finish();

                    }
                });
                btnCustVisit.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (currLatitude != 0.0 && currLongitude != 0.0) {

                            trackVisit = new Dialog(MainActivity.this);
                            // Set GUI of login screen
                            trackVisit.getWindow();
                            trackVisit.requestWindowFeature(Window.FEATURE_NO_TITLE);
              /*  getWindow().setBackgroundDrawable(
                        new ColorDrawable(android.graphics.Color.TRANSPARENT));*/

                            trackVisit.setContentView(R.layout.cust_track_dialog);

                            final TextView txtDate = (TextView) trackVisit.findViewById(R.id.VisitDate);
                            final Spinner spinCust = (Spinner) trackVisit.findViewById(R.id.spinnerCustomer);
                            Button btnSave1 = (Button) trackVisit.findViewById(R.id.btnSave);
                            Button btnCancel1 = (Button) trackVisit.findViewById(R.id.btnCancel);
                            ImageView imgClose1 = (ImageView) trackVisit.findViewById(R.id.imageClose);
                            ImageView imgBarcode = (ImageView) trackVisit.findViewById(R.id.imageViewBarcode);
                            Button refresh = (Button) trackVisit.findViewById(R.id.refresh);

                            refresh.setOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    allCustomers.clear();
                                    adapterCustomer.clear();
                                    dba.open();
                                    String sql1 = "select " + CustomerTable.KEY_NAME + "," + CustomerTable.KEY_LONGI + "," + CustomerTable.KEY_LATI +
                                            " from " + CustomerTable.DATABASE_TABLE;
                                    Cursor custCur1 = DataBaseAdapter.ourDatabase.rawQuery(sql1, null);

                                    count = 0;

                                    if (custCur1.getCount() > 0) {
                                        while (custCur1.moveToNext()) {
                                            allCustomers.add(custCur1.getString(0).trim());
                                            if (CalculateDistance.distance(custCur1.getDouble(2), custCur1.getDouble(1), currLatitude, currLongitude) < Double.parseDouble(session.getDistance())) {
                                                customers.add(custCur1.getString(0).trim());
                                                count++;
                                            }
                                        }
                                        Log.w("MA", "Count: " + count);

                                        if (count == 0) {
                                            allCustomers.clear();
                                            adapterCustomer = new ArrayAdapter<String>(MainActivity.this,
                                                    android.R.layout.simple_spinner_item, allCustomers);
                                            adapterCustomer.notifyDataSetChanged();
                                            str = "Unable to find customer at this location please select manually";
                                        } else if (count > 1) {
                                            adapterCustomer = new ArrayAdapter<String>(MainActivity.this,
                                                    android.R.layout.simple_spinner_item, customers);
                                            adapterCustomer.notifyDataSetChanged();
                                            str = "Multiple customers found please select correct one";
                                        } else {
                                            adapterCustomer = new ArrayAdapter<String>(MainActivity.this,
                                                    android.R.layout.simple_spinner_item, customers);
                                            adapterCustomer.notifyDataSetChanged();
                                        }

                                        adapterCustomer
                                                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                        spinCust.setAdapter(adapterCustomer);
                                        adapterCustomer.notifyDataSetChanged();

                                    }
                                    custCur1.close();
                                    dba.close();

                                }
                            });

                            imgBarcode.setOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent(MainActivity.this, FullScannerActivity.class);
                                    spinCust.setSelection(0);
                                    startActivityForResult(intent, 1);
                                    trackVisit.dismiss();
                                    finish();
                                }
                            });


                            currLatitude = gps.getLatitude();
                            currLongitude = gps.getLongitude();

                            customers = new ArrayList<String>();
                            allCustomers = new ArrayList<String>();

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
                                    allCustomers.clear();
                                    adapterCustomer = new ArrayAdapter<String>(MainActivity.this,
                                            android.R.layout.simple_spinner_item, allCustomers);
                                    str = "Unable to find customer at this location please select manually";
                                } else if (count > 1) {
                                    adapterCustomer = new ArrayAdapter<String>(MainActivity.this,
                                            android.R.layout.simple_spinner_item, customers);
                                    str = "Multiple customers found please select correct one";
                                } else {
                                    adapterCustomer = new ArrayAdapter<String>(MainActivity.this,
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
                            Toast.makeText(MainActivity.this, str, Toast.LENGTH_SHORT).show();
                            trackVisit.show();
                        } else {
                            toastService.startTimer();
                        }
                    }
                });

                btnBilling.setOnClickListener(new OnClickListener() {
                                                  @Override
                                                  public void onClick(View v) {


                                                  }
                                              }

                );

            }
        } catch (Exception e) {
            e.printStackTrace();
            LogError lErr = new LogError();
            lErr.appendLog(e.toString());
        }

    }
    private void initInstances() {
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        drawerToggle = new ActionBarDrawerToggle(MainActivity.this, drawerLayout, R.string.hello_world, R.string.hello_world);
        drawerLayout.setDrawerListener(drawerToggle);

        navigation = (NavigationView) findViewById(R.id.navigation_view);
        navigation.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                try {


                    int id = menuItem.getItemId();
                    drawerLayout.closeDrawers();
                    switch (id) {

                        case R.id.sync:

                            ConnectivityManager cm = ((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE));

                            if (cm == null) {
                                return false;
                            }

                            // Now to check if we're actually connected
                            if (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected()) {
                                // Start the service to do our thing
                                Log.w("Sync", "Online");

                                final String[] opt = new String[]{
                                        "Download Data",
                                        "Download Product",
                                        "Download Product Images",
                                        "Upload Data"
                                };

                                AlertDialog.Builder alertdialogbuilder = new AlertDialog.Builder(MainActivity.this);

                                alertdialogbuilder.setTitle("Select Sync Type");

                                alertdialogbuilder.setItems(opt, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        String selectedText = Arrays.asList(opt).get(which);
                                        if (selectedText.equalsIgnoreCase("Download Data")) {
                                            //    Log.w("MenuActivity","Option: "+selectedText);
                                            new SyncData().execute();
                                        } else if (selectedText.equalsIgnoreCase("Upload Data")) {
                                            //    Log.w("MenuActivity","Option: "+selectedText);
                                            dba.open();
                                            Cursor cur = mod.getData(OfflineTable.DATABASE_TABLE);
                                            int count = cur.getCount();
                                            cur.close();
                                            dba.close();
                                            if (count > 0) {
                                                new SyncDataOffline().execute();
                                            } else {
                                                //mSwipeRefreshLayout.setRefreshing(false);
                                                Toast.makeText(MainActivity.this, "No Offline Data to Upload", Toast.LENGTH_LONG).show();
                                            }
                                        } else if (selectedText.equalsIgnoreCase("Download Product")) {
                                            new SyncProduc().execute();

                                        } else if (selectedText.equalsIgnoreCase("Download Product Images")) {

                                            new DownloadImages().execute();
                                        }
                                        //   Toast.makeText(getApplicationContext(),selectedText,Toast.LENGTH_LONG).show();
                                    }
                                });

                                AlertDialog dialog = alertdialogbuilder.create();
                                // dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                dialog.show();


                            } else {
                                //  mSwipeRefreshLayout.setRefreshing(false);
                                Log.w("Sync", "Ofline");
                                Toast.makeText(MainActivity.this, "Internet Connection not available. \n Turn ON internet", Toast.LENGTH_LONG).show();
                            }


                            //    new SyncData().execute();


                            break;

                        case R.id.custList:
                            cancelAlarm();
                            //              cancelA();
                            syncFlag = 1;
                            Intent intent3 = new Intent(getApplicationContext(), CustomerActivity.class);
                            intent3.putExtra("act", "customer");
                            startActivity(intent3);
                            finish();

                            break;

                        case R.id.cust_unchedule:


                           /* Intent intents = new Intent(getApplicationContext(),
                                    UNOrderActivity.class);
                            startActivity(intents);
                            finish();*/
                            cancelAlarm();
                            //            cancelA();
                            syncFlag = 1;
                            OrderFlag = "U";

                            if (gps.canGetLocation()) {

                                dba.open();
                                Cursor cur = mod.getData(CustomerTable.DATABASE_TABLE);
                                cur.moveToNext();
                                if (cur.getCount() > 0) {
                                    String sql = "select insertt,print from authority where form = 'Order Search'";
                                    Cursor aut = DataBaseAdapter.ourDatabase
                                            .rawQuery(sql, null);

                                    if (aut.getCount() > 0) {
                                        aut.moveToFirst();

                                        dba.open();
                                        String sql1 = "select * from temp";
                                        Cursor cur1 = DataBaseAdapter.ourDatabase.rawQuery(sql1, null);
                                        if (cur1.getCount() > 0) {
                                            DialogBox();
                                        } else {

                                            Intent intent = new Intent(getApplicationContext(), OrderActivity.class);
                                            startActivity(intent);
                                            finish();

                                        }
                                    }
                                    dba.close();
                                    aut.close();
                                } else {
                                    sync.show();
                                }
                                cur.close();
                                dba.close();
                            } else {
                                gps.showSettingsAlert();
                            }


                            break;

                        case R.id.cust_replacement:
                            cancelAlarm();
                            //            cancelA();
                            syncFlag = 1;
                            OrderFlag = "R";

                            if (gps.canGetLocation()) {

                                dba.open();
                                Cursor cur = mod.getData(CustomerTable.DATABASE_TABLE);
                                cur.moveToNext();
                                if (cur.getCount() > 0) {
                                    String sql = "select insertt,print from authority where form = 'Order Search'";
                                    Cursor aut = DataBaseAdapter.ourDatabase
                                            .rawQuery(sql, null);

                                    if (aut.getCount() > 0) {
                                        aut.moveToFirst();

                                        dba.open();
                                        String sql1 = "select * from temp";
                                        Cursor cur1 = DataBaseAdapter.ourDatabase.rawQuery(sql1, null);
                                        if (cur1.getCount() > 0) {
                                            DialogBox();
                                        } else {

                                            Intent intent = new Intent(getApplicationContext(), OrderActivity.class);
                                            startActivity(intent);
                                            finish();

                                        }
                                    }
                                    dba.close();
                                    aut.close();
                                } else {
                                    sync.show();
                                }
                                cur.close();
                                dba.close();
                            } else {
                                gps.showSettingsAlert();
                            }

                            break;

                        case R.id.cust_visit:
                            Intent intent = new Intent(getApplicationContext(), CustVisitActivity.class);
                            startActivity(intent);
                            finish();
                            break;
                        case R.id.townList:
                            Intent intentt = new Intent(getApplicationContext(), TownActivity.class);
                            startActivity(intentt);


                            break;
                        case R.id.GeoTagUpdates:
                            Intent intent1 = new Intent(getApplicationContext(), GeoTagUpdatesActivity.class);
                            startActivity(intent1);


                            break;
                        case R.id.CustmerVisite:
                            Intent intentVisite = new Intent(getApplicationContext(), CustomerVisiteActivity.class);
                            startActivity(intentVisite);

                            break;
                        case R.id.expense:
                            Intent intentVisite1 = new Intent(getApplicationContext(), ExpenseActivity.class);
                            startActivity(intentVisite1);
                            break;

                        case R.id.getexpense:
                            Intent intent22 = new Intent(getApplicationContext(), ViewExpenses.class);
                            startActivity(intent22);
                            break;
                        case R.id.cust_visit_status:
                            if (currLatitude != 0.0 && currLongitude != 0.0) {

                                trackVisit = new Dialog(MainActivity.this);
                                // Set GUI of login screen
                                trackVisit.requestWindowFeature(Window.FEATURE_NO_TITLE);
                                getWindow().setBackgroundDrawable(
                                        new ColorDrawable(
                                                android.graphics.Color.TRANSPARENT));

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
                                        Intent intent = new Intent(MainActivity.this, FullScannerActivity.class);
                                        spinCust.setSelection(0);
                                        startActivityForResult(intent, 1);
                                        trackVisit.dismiss();
                                    }
                                });
                                currLatitude = gps.getLatitude();
                                currLongitude = gps.getLongitude();

                                customers = new ArrayList<String>();
                                allCustomers = new ArrayList<String>();


                                Button refresh = (Button) trackVisit
                                        .findViewById(R.id.refresh);

                                refresh.setOnClickListener(new OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        adapterCustomer.clear();
                                        dba.open();
                                        String sql1 = "select " + CustomerTable.KEY_NAME + "," + CustomerTable.KEY_LONGI + "," + CustomerTable.KEY_LATI +
                                                " from " + CustomerTable.DATABASE_TABLE;
                                        Cursor custCur1 = DataBaseAdapter.ourDatabase.rawQuery(sql1, null);
                                        currLatitude = gps.getLatitude();
                                        currLongitude = gps.getLongitude();
                                        count = 0;
                                        if (custCur1.getCount() > 0) {
                                            while (custCur1.moveToNext()) {
                                                allCustomers.add(custCur1.getString(0).trim());
                                                if (CalculateDistance.distance(custCur1.getDouble(2), custCur1.getDouble(1), currLatitude, currLongitude) < Double.parseDouble(session.getDistance())) {
                                                    customers.add(custCur1.getString(0).trim());
                                                    count++;
                                                }
                                            }
                                            Log.w("MA", "Count: " + count);

                                            if (count == 0) {
                                                allCustomers.clear();
                                                adapterCustomer = new ArrayAdapter<String>(MainActivity.this,
                                                        android.R.layout.simple_spinner_item, allCustomers);
                                                adapterCustomer.notifyDataSetChanged();
                                                str = "Unable to find customer at this location please select manually";
                                            } else if (count > 1) {
                                                adapterCustomer = new ArrayAdapter<String>(MainActivity.this,
                                                        android.R.layout.simple_spinner_item, customers);
                                                str = "Multiple customers found please select correct one";
                                                adapterCustomer.notifyDataSetChanged();
                                                Toast.makeText(MainActivity.this, str, Toast.LENGTH_LONG).show();
                                            } else {
                                                adapterCustomer = new ArrayAdapter<String>(MainActivity.this,
                                                        android.R.layout.simple_spinner_item, customers);
                                                adapterCustomer.notifyDataSetChanged();
                                            }
                                            adapterCustomer
                                                    .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                            spinCust.setAdapter(adapterCustomer);
                                            adapterCustomer.notifyDataSetChanged();
                                        }
                                        custCur1.close();
                                        dba.close();
                                    }
                                });

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
                                    adapterCustomer = new ArrayAdapter<String>(MainActivity.this,
                                            android.R.layout.simple_spinner_item, customers);
                                /*if (count == 0) {
                                    adapterCustomer = new ArrayAdapter<String>(MainActivity.this,
                                            android.R.layout.simple_spinner_item, allCustomers);
                                    str = "Unable to find customer at this location please select manually";
                                } else if (count > 1) {
                                    adapterCustomer = new ArrayAdapter<String>(MainActivity.this,
                                            android.R.layout.simple_spinner_item, customers);
                                    str = "Multiple customers found please select correct one";
                                } else {
                                    adapterCustomer = new ArrayAdapter<String>(MainActivity.this,
                                            android.R.layout.simple_spinner_item, customers);
                                }*/
                                    adapterCustomer
                                            .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                    spinCust.setAdapter(adapterCustomer);
                                    //spinCust.setEnabled(false);

                                }
                                custCur.close();
                                dba.close();


                                Calendar c = Calendar.getInstance();
                                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
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
                                Toast.makeText(MainActivity.this, str, Toast.LENGTH_SHORT).show();
                                trackVisit.show();
                            } else {
                                toastService.startTimer();
                            }
                            break;


                        case R.id.dash_board:
                            Intent intentDashBoard = new Intent(getApplicationContext(),
                                    DashBoardActivity.class);
                            startActivity(intentDashBoard);
                            break;

                        case R.id.exit:
                            syncFlag = 1;
                            cancelAlarm();
                            cancelA();
                            session.logoutUser();
                            Intent startMain = new Intent(Intent.ACTION_MAIN);
                            startMain.addCategory(Intent.CATEGORY_HOME);
                            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(startMain);
                            finish();
                            // android.os.Process.killProcess(android.os.Process.myPid());
                            System.exit(1);

                            break;
                        case R.id.help:
                            cancelAlarm();
                            //          cancelA();
                            syncFlag = 1;
                            Intent intent2 = new Intent(getApplicationContext(), HelpActivity.class);
                            startActivity(intent2);
                            finish();
                            break;
                        case R.id.distance:
                            setting = new Dialog(MainActivity.this);
                            // Set GUI of login screen
                            setting.requestWindowFeature(Window.FEATURE_NO_TITLE);
                            getWindow().setBackgroundDrawable(
                                    new ColorDrawable(
                                            android.graphics.Color.TRANSPARENT));

                            setting.setContentView(R.layout.setting_dialog);

                            Button btnIncrease = (Button) setting.findViewById(R.id.btnIncrese);
                            Button btnDecrease = (Button) setting.findViewById(R.id.btnDecrese);
                            Button btnSave = (Button) setting.findViewById(R.id.btnSave);
                            Button btnCancel = (Button) setting.findViewById(R.id.btnCancel);
                            ImageView imgClose = (ImageView) setting.findViewById(R.id.imageClose);
                            final TextView txtDistance = (TextView) setting.findViewById(R.id.txtDistance);

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
                                        Toast.makeText(MainActivity.this, "Distance must be greater than " + session.getMinDistance() + " meter", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });

                            btnSave.setOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    int dis = Integer.parseInt(txtDistance.getText().toString().trim());

                                    if (dis > 15) {

                                        String userid = session.getUserId();
                                        String password = session.getUserPassword();
                                        String empno = session.getEmpNo();
                                        String empname = session.getEmpName();
                                        String branchno = session.getBranchNo();
                                        String mindist = session.getMinDistance();
                                        String addr = session.getAddresse();
                                        String comp = session.getComputerName();
                                        session.logoutUser();
                                        session.createLoginSession(userid, password, empno, empname, branchno, String.valueOf(dis * 0.000621371), mindist, addr, comp);

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


                                    } else {

                                        Toast.makeText(MainActivity.this, "Min Distance should be 15.", Toast.LENGTH_SHORT).show();
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
                            break;
                        case R.id.feedback:
                            Intent intentfeedback = new Intent(getApplicationContext(),
                                    FeedbackActivity.class);
                            startActivity(intentfeedback);
                            finish();
                            break;
                    }

                } catch (Exception e) {

                    e.printStackTrace();
                }

                return false;
            }
        });

    }

    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.global, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        try {

            if (drawerToggle.onOptionsItemSelected(item))
                return true;

            // Handle action bar item clicks here. The action bar will
            // automatically handle clicks on the Home/Up button, so long
            // as you specify a parent activity in AndroidManifest.xml.
            int id = item.getItemId();

            if (id == R.id.action_outstanding) {

                Intent intent = new Intent(this, OutCustmerActivity.class);
                startActivity(intent);


            }
            if (id == R.id.action_list) {
                Intent intent = new Intent(this, OrderListActivity.class);
                startActivity(intent);
                finish();
            }
            //noinspection SimplifiableIfStatement
            if (id == R.id.action_sync) {
                ConnectivityManager cm = ((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE));

                if (cm == null) {
                    return false;
                }

                // Now to check if we're actually connected
                if (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected()) {
                    // Start the service to do our thing
                    Log.w("Sync", "Online");

                    final String[] opt = new String[]{
                            "Download Data",
                            "Download Product",
                            "Download Product Images",
                            "Upload Data"
                    };

                    AlertDialog.Builder alertdialogbuilder = new AlertDialog.Builder(MainActivity.this);

                    alertdialogbuilder.setTitle("Select Sync Type");

                    alertdialogbuilder.setItems(opt, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String selectedText = Arrays.asList(opt).get(which);
                            if (selectedText.equalsIgnoreCase("Download Data")) {
                                //    Log.w("MenuActivity","Option: "+selectedText);
                                new SyncData().execute();
                            } else if (selectedText.equalsIgnoreCase("Upload Data")) {
                                //    Log.w("MenuActivity","Option: "+selectedText);
                                dba.open();
                                Cursor cur = mod.getData(OfflineTable.DATABASE_TABLE);
                                int count = cur.getCount();
                                cur.close();
                                dba.close();
                                if (count > 0) {
                                    new SyncDataOffline().execute();
                                } else {
                                    //mSwipeRefreshLayout.setRefreshing(false);
                                    Toast.makeText(MainActivity.this, "No Offline Data to Upload", Toast.LENGTH_LONG).show();
                                }
                            } else if (selectedText.equalsIgnoreCase("Download Product")) {
                                new SyncProduc().execute();

                            } else if (selectedText.equalsIgnoreCase("Download Product Images")) {

                                new DownloadImages().execute();
                            }
                            //   Toast.makeText(getApplicationContext(),selectedText,Toast.LENGTH_LONG).show();
                        }
                    });

                    AlertDialog dialog = alertdialogbuilder.create();
                    // dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    dialog.show();


                } else {
                    //mSwipeRefreshLayout.setRefreshing(false);
                    Log.w("Sync", "Ofline");
                    Toast.makeText(MainActivity.this, "Internet Connection not available. \n Turn ON internet", Toast.LENGTH_LONG).show();
                }


                //    new SyncData().execute();


                return true;
            }
        } catch (Exception e) {

            e.printStackTrace();
        }

        return super.onOptionsItemSelected(item);
    }

    public void DialogBox() {
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                //set icon
                .setIcon(android.R.drawable.ic_dialog_alert)
                //set title
                .setTitle("Confirm")
                //set message
                .setMessage("Do you want to clear cart?")
                //set positive button
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //set what would happen when positive button is clicked
                        dba.open();
                        mod.clearDatabase(OrderTempTable.DATABASE_TABLE);
                        mod.clearDatabase(OrderTable.DATABASE_TABLE);
                        dba.close();
                        Intent intent = new Intent(getApplicationContext(), OrderActivity.class);
                        startActivity(intent);


                    }
                })
                //set negative button
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //set what should happen when negative button is clicked

                        Intent intent = new Intent(getApplicationContext(),
                                OrderActivity.class);
                        startActivity(intent);
                    }
                })
                .show();
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
        mGoogleApiClient = new GoogleApiClient.Builder(MainActivity.this)
                .addConnectionCallbacks(MainActivity.this)
                .addOnConnectionFailedListener(MainActivity.this)
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
                .isGooglePlayServicesAvailable(MainActivity.this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, MainActivity.this,
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
                mGoogleApiClient, mLocationRequest, MainActivity.this);

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

       /* mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {
            Toast.makeText(MainActivity.this, "Latitude"+String.valueOf(mLastLocation.getLatitude())+"   Long"+String.valueOf(mLastLocation.getLongitude()), Toast.LENGTH_SHORT).show();

        }*/
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

            Log.w("MainActivity", "CurrentLongitude: " + longitude);
            Log.w("MainActivity", "CurrentLatitude: " + latitude);



            ContentValues cv = new ContentValues();
            cv.put(LocationTable.KEY_LONGITUDE, longitude);
            cv.put(LocationTable.KEY_LATITUDE, latitude);
            cv.put(LocationTable.KEY_DATE, strDate);

            if (latitude != 0 || longitude != 0) {
                if (locSession.isLoggedIn()) {
                    if (latitude != Double.parseDouble(locSession.getLati()) && longitude != Double.parseDouble(locSession.getLongi())) {

                        Log.w("MainActivity", "Location Insert");


                        locSession.logoutUser();
                        locSession.createLocationSession(String.valueOf(longitude), String.valueOf(latitude));
                        dba.open();
                        mod.insertdata(LocationTable.DATABASE_TABLE, cv);
                        dba.close();
                    }
                } else {
                    Log.w("MainActivity", "Location Insert 1");
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
            //session = new SessionManager(getApplicationContext());
            locSession = new SessionLocation(getApplicationContext());
            try {
                gps = new GPSTracker(getApplicationContext());
                currLatitude = gps.getLatitude();
                currLongitude = gps.getLongitude();
                Log.w("AlarmReceiver", "CurrentLongitude: " + currLatitude);
                Log.w("AlarmReceiver", "CurrentLongitude: " + currLongitude);

                dba.open();
                DataGPS="";
                Cursor curLoc = mod.getData(LocationTable.DATABASE_TABLE);
                Log.e("BeforeTracking1",""+curLoc.getCount());
                if (curLoc.getCount() > 0) {
                    int cnt = 1;
                    double longi = 0;
                    double lati = 0;
                    while (curLoc.moveToNext()) {
                        LogError lErr = new LogError();
                        lErr.appendLog(cnt+"  Longi: "+curLoc.getDouble(0)+"  Lati: "+curLoc.getDouble(1));

                        DataGPS = DataGPS+curLoc.getString(0) + "," + curLoc.getString(1) + "," + curLoc.getString(2) + "$";

                        Log.e("Tracking1",""+DataGPS);
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
            MainActivity.this.handlerAlaram.postDelayed(MainActivity.this.runLocation2, 60000);
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
                dba1.open();
                serverResponse = WebService.sendLocation(session.getEmpNo(), session.getBranchNo(), DataGPS, "SetRouteLocation");
                Log.e("Tracking4", "" + serverResponse.toString() + "  :  " + serverResponse.getJSONObject(0).getString("Status").trim());
                dba1.close();
                Log.w("Alarm Receiver", "ResponseData: " + WebService.responseString);
                if (serverResponse.getJSONObject(0).getString("Status").trim().equalsIgnoreCase("Success")) {
                    Log.e("Tracking5", "DB Cleared Entered");
                    dba1.open();
                    mod.clearDatabase(LocationTable.DATABASE_TABLE);
                    dba1.close();
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

    public Runnable runLocation = new Runnable() {
        @Override
        public void run() {
            //session = new SessionManager(getApplicationContext());
            //dba = new DataBaseAdapter(getApplicationContext());
            //mod = new Models();
            locSession = new SessionLocation(getApplicationContext());
            Log.w("LocationReceiver", ".1");
            try {

                if (MainActivity.mGoogleApiClient != null) {
                    MainActivity.mGoogleApiClient.connect();
                }
                if (MainActivity.flagApiIsConnected == 1) {
                    //        Log.w("LR", "LR Start");
                    Log.w("LocationReceiver", ".2");
                    mLastLocation = LocationServices.FusedLocationApi.getLastLocation(MainActivity.mGoogleApiClient);
                    GPSTracker gps = new GPSTracker(getApplicationContext());
                    gps.getLocation();
                    //    gpsService = new GpsService(context);
                    if (gps.canGetLocation()) {
                        currLatitude = gps.getLatitude();
                        currLongitude = gps.getLongitude();
                        Calendar c = Calendar.getInstance();
                        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                        String strDate = sdf.format(c.getTime());
                        Log.w("LocationReceiver", ".....");
                        Log.w("LocationReceiver", "Tracker CurrentLongitude: " + currLongitude);
                        Log.w("LocationReceiver", "Tracker CurrentLatitude: " + currLatitude);
                        Log.w("LocationReceiver", "Tracker CurrentDATETIME: " + strDate);
                       /*     Log.w("LocationReceiver", "API CurrentLongitude: " + mLastLocation.getLongitude());
                            Log.w("LocationReceiver", "API CurrentLatitude: " + mLastLocation.getLatitude());*/
                        ContentValues cv = new ContentValues();
                        cv.put(LocationTable.KEY_LONGITUDE, currLongitude);
                        cv.put(LocationTable.KEY_LATITUDE, currLatitude);
                        cv.put(LocationTable.KEY_DATE, strDate);
                        if (currLatitude != 0 || currLongitude != 0) {
                            if (locSession.isLoggedIn()) {
                                if (currLatitude != Double.parseDouble(locSession.getLati()) || currLongitude != Double.parseDouble(locSession.getLongi())) {
                                    //                     Toast.makeText(context, "LR Location changed! Longitude: " + longitude + "\nLatitude: " + latitude, Toast.LENGTH_SHORT).show();
                                    String str = "Longitude: " + currLongitude + " Latitude: " + currLatitude + " Time: " + strDate;
                                    Log.e("Tracking", "date" + str);
                                    //Toast.makeText(conn , "Tracking : "+ str.toString(), Toast.LENGTH_SHORT).show();
                                    LogError lErr2 = new LogError();
                                    lErr2.appendLog("Tracking : " + str);
                                    locSession.logoutUser();
                                    locSession.createLocationSession(String.valueOf(currLongitude), String.valueOf(currLatitude), strDate);
                                    dba1.open();
                                    mod.insertdata(LocationTable.DATABASE_TABLE, cv);
                                    Log.e("insert", "" + cv);
                                    dba1.close();
                                }
                            } else {
                                locSession.createLocationSession(String.valueOf(currLongitude), String.valueOf(currLatitude), strDate);
                                dba1.open();
                                mod.insertdata(LocationTable.DATABASE_TABLE, cv);
                                Log.e("insert1", "" + cv);
                                dba1.close();
                            }
                        } else {
                            if (locSession.isLoggedIn()) {
                                Date dtPrev = sdf.parse(locSession.getDateTime());
                                Date dtNow = sdf.parse(strDate);
                                long diff = dtPrev.getTime() - dtNow.getTime();
                                long diffInSec = TimeUnit.MILLISECONDS.toSeconds(diff);
                                long seconds = diffInSec % 60;
                                diffInSec /= 60;
                                long minutes = diffInSec % 60;
                            } else {
                                locSession.createLocationSession(String.valueOf(currLongitude), String.valueOf(currLatitude), strDate);
                            }
                        }
                    } else {
                        Log.w("LR", "cannot get location");
                    }

                    DataGPS = "";
                    dba1.open();
                    Cursor curLoc = mod.getData(LocationTable.DATABASE_TABLE);
                    Log.e("BeforeTracking1", "" + curLoc.getCount());
                    if (curLoc.getCount() > 6) {
                        while (curLoc.moveToNext()) {
                            DataGPS = DataGPS + curLoc.getString(0) + "," + curLoc.getString(1) + "," + curLoc.getString(2) + "$";
                            Log.e("Tracking1", "" + DataGPS);
                        }
                        AsyncCallWSGPS task = new AsyncCallWSGPS();
                        task.execute();
                    }
                    dba1.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
                LogError lErr = new LogError();
                lErr.appendLog(e.toString());
                //Toast.makeText(conn , "GPS Tracker Error : " + e.toString(), Toast.LENGTH_SHORT).show();
            }
            // MainActivity.this.handler.postDelayed(MainActivity.this.runLocation, 60000);
        }
    };


    public void startA() {
        /*managerL = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        int interval1 = 1000 * 5; // 5 sec

        managerL.setRepeating(AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis(), interval1, pendingLocIntent);
        Toast.makeText(MainActivity.this, "Alarm Set Loc", Toast.LENGTH_SHORT).show();*/
        handler = new Handler();
        handler.postDelayed(runLocation, 60000);
    }

    public void cancelA() {
        /*if (manager != null) {
            manager.cancel(pendingLocIntent);
            Toast.makeText(MainActivity.this, "Alarm Canceled Loc", Toast.LENGTH_SHORT).show();
        }*/
        handler.removeCallbacks(runLocation);
    }


    public void checkLocation() {
        if (gps.canGetLocation()) {

            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);


            try {

                currLatitude = gps.getLatitude();
                currLongitude = gps.getLongitude();

                if (currLatitude != 0.0 && currLongitude != 0.0) {

                    toastService.stopTimer();

                    dba1.open();
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
                    dba1.close();
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
                sData.syncdata(MainActivity.this);
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
            Toast.makeText(MainActivity.this,
                    "Sync done Successfully!!!", Toast.LENGTH_LONG).show();
        }
    }


    class SyncProduc extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... strings) {
            try {

                System.out.println(">>>>>>> SyncProduc >>>> doInBackground >>>>.");
                dba.open();
                sData.syncProduct(MainActivity.this);
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
            Toast.makeText(MainActivity.this,
                    "Sync done Successfully!!!", Toast.LENGTH_LONG).show();
        }
    }


    class DownloadImages extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... strings) {
            try {
                dba.open();
                sData.downloadImages();
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
            Toast.makeText(MainActivity.this,
                    "Sync done Successfully!!!", Toast.LENGTH_LONG).show();
        }
    }

    class SyncDataOffline extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... strings) {
            try {
                sDataOffline.syncdata(MainActivity.this);
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

            if (mSwipeRefreshLayout != null) {
                mSwipeRefreshLayout.setRefreshing(false);
            }

            syncFlag = 0;
            Toast.makeText(MainActivity.this,
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
                    Toast.makeText(MainActivity.this,
                            "Failed to save Data", Toast.LENGTH_LONG).show();
                } else if (serverResponse.getJSONObject(0).getString("Status").equalsIgnoreCase("Success")) {
                    Toast.makeText(MainActivity.this,
                            "Data Saved Successfully!!!", Toast.LENGTH_LONG).show();
                    if (serverFlag == 0) {
                        setting.cancel();
                    } else if (serverFlag == 1) {
                        trackVisit.cancel();
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}