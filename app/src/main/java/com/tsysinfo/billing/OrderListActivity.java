package com.tsysinfo.billing;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Environment;
 
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.location.LocationServices;
import com.tsysinfo.billing.database.CustomerTable;
import com.tsysinfo.billing.database.DataBaseAdapter;
import com.tsysinfo.billing.database.Models;
import com.tsysinfo.billing.database.OfflineTable;
import com.tsysinfo.billing.database.OrderTempTable;
import com.tsysinfo.billing.database.SessionManager;
import com.tsysinfo.billing.database.WebService;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
public class OrderListActivity extends AppCompatActivity {

    static Spinner spinCustomer;
    private static String FILE = Environment.getExternalStorageDirectory()
            .toString() + "/ebilling_images/pdf/bill.pdf";
    public Location mLastLocation;
    SessionManager session;
    DataBaseAdapter dba;
    Models mod;
    ListView lv;
    List<OrderEntity> orderEntities;
    String custid = "";
    String Data = "";
    Double currLatitude;
    Double currLongitude;
    ProgressDialog waitDialog;
    JSONArray serverResponse;
    static boolean errored = false;

    String longi = "",lati = "";
    ToastService toastService;
    EditText editText;
    private String GLOBAL_CUST_ID;
    private OrderAdapter ordAdapter;
    LinearLayout linearLayout;
    OrderEntity  orderEntity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_list);
        session = new SessionManager(this);
        dba = new DataBaseAdapter(this);
        editText=(EditText)findViewById(R.id.sr);
        linearLayout= (LinearLayout) findViewById(R.id.linerlayout);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                ordAdapter.getFilter().filter(editText.getText().toString());


            }
        });
        mod = new Models();
        orderEntities= new ArrayList<>();

        waitDialog = new ProgressDialog(this);
        waitDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        waitDialog.setMessage("Please Wait");

        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        lv = (ListView) findViewById(R.id.list);
        toastService = new ToastService(OrderListActivity.this);


        try {

            new AsyncCallWS().execute();


        }catch (Exception e){
            e.printStackTrace();
            LogError lErr = new LogError();
            lErr.appendLog(e.toString());
        }
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

    public void generatePDF() {

    }
    private class AsyncCallWS extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            // Call Web Method
            try {

                Log.w("CartActivity", "Start");

                dba.open();
                serverResponse = WebService.getOrders(session.getBranchNo(),session.getEmpNo(), "GetOrder");
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
                        OrderListActivity.this);
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

                            if (serverResponse.getJSONObject(0).getString("CustomerName").trim().equalsIgnoreCase("No")) {

                                AlertDialog.Builder builder2 = new AlertDialog.Builder(OrderListActivity.this);
                                builder2.setTitle("Failure..");
                                builder2.setMessage("Failed to get Orders.");
                                builder2.setPositiveButton("OK",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(
                                                    DialogInterface dialog,
                                                    int which) {
                                                Log.e("info", "OK");
                                            }
                                        });
                                builder2.show();

                            } else if (!serverResponse.getJSONObject(0).getString("CustomerName").trim().equalsIgnoreCase("No")) {


                                for (int i=0;i<serverResponse.length();i++)

                                {
                                    OrderEntity  orderEntity= new OrderEntity();
                                    orderEntity.setCustomerName(serverResponse.getJSONObject(i).getString("CustomerName"));
                                    orderEntity.setOrderDate(serverResponse.getJSONObject(i).getString("OrderDate"));
                                    orderEntity.setOrderNo(serverResponse.getJSONObject(i).getString("OrderNo"));
                                    orderEntity.setSalesMan(session.getEmpName());
                                    orderEntity.setTotalAmt(serverResponse.getJSONObject(i).getString("TotalAmount"));
                                    orderEntity.setTransType(serverResponse.getJSONObject(i).getString("TransType"));

                                    orderEntities.add(orderEntity);


                                }

                               /* dba.open();
                                Cursor cur = mod.getData(OfflineTable.DATABASE_TABLE);
                                if (cur.getCount() > 0) {
                                    while (cur.moveToNext()) {

                                        OrderEntity  orderEntity= new OrderEntity();
                                        orderEntity.setCustomerName(cur.getString(cur.getColumnIndex("data")));
                                        orderEntity.setOrderDate(cur.getString(cur.getColumnIndex("unit")));
                                        orderEntity.setOrderNo(cur.getString(cur.getColumnIndex("unit")));
                                        orderEntity.setSalesMan(session.getEmpName());
                                        orderEntity.setTotalAmt(cur.getString(cur.getColumnIndex("unit")));
                                        orderEntity.setTransType(cur.getString(cur.getColumnIndex("unit")));

                                        results.add(sr);
                                    }
                                }
                                cur.close();
                                dba.close();*/

                                ordAdapter=new OrderAdapter(OrderListActivity.this, orderEntities);

                               lv.setAdapter(ordAdapter);

                            }

                        }

                        // Error status is true
                    } else {

                        Toast.makeText(OrderListActivity.this, "Server Error",
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

