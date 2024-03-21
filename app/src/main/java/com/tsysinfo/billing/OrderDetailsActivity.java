package com.tsysinfo.billing;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.SQLException;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Environment;
 
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.tsysinfo.billing.database.DataBaseAdapter;
import com.tsysinfo.billing.database.Models;
import com.tsysinfo.billing.database.SessionManager;
import com.tsysinfo.billing.database.WebService;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class OrderDetailsActivity extends AppCompatActivity {

    static Spinner spinCustomer;
    private static String FILE = Environment.getExternalStorageDirectory()
            .toString() + "/ebilling_images/pdf/bill.pdf";
    public Location mLastLocation;
    SessionManager session;
    DataBaseAdapter dba;
    Models mod;
    ListView lv;
    List<OrdDetailsEntity> orderEntities;
    String custid = "";
    String Data = "";
    Double currLatitude;
    Double currLongitude;
    ProgressDialog waitDialog;
    JSONArray serverResponse;
    static boolean errored = false;

    String longi = "",lati = "";
    ToastService toastService;
    private String OrderId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);

        session = new SessionManager(this);
        dba = new DataBaseAdapter(this);
        mod = new Models();
        orderEntities= new ArrayList<>();
        Intent intent=getIntent();
        OrderId=intent.getStringExtra("id");

        waitDialog = new ProgressDialog(this);
        waitDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        waitDialog.setMessage("Please Wait");

        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        lv = (ListView) findViewById(R.id.list);
        toastService = new ToastService(OrderDetailsActivity.this);



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
                serverResponse = WebService.getOrderDetails(session.getBranchNo(),OrderId, "GetOrderDetails");
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
                        OrderDetailsActivity.this);
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
                                    .getString("KeySerial").trim()
                                    .equalsIgnoreCase("No")) {

                                AlertDialog.Builder builder2 = new AlertDialog.Builder(
                                        OrderDetailsActivity.this);
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

                            } else if (!serverResponse.getJSONObject(0).getString("KeySerial").trim().equalsIgnoreCase("No")) {


                                for (int i=0;i<serverResponse.length();i++)

                                {
                                    OrdDetailsEntity  orderEntity= new OrdDetailsEntity();



                                    orderEntity.setName(serverResponse.getJSONObject(i).getString("DESCR"));
                                    orderEntity.setDate(serverResponse.getJSONObject(i).getString("orderDate"));
                                    orderEntity.setNo(serverResponse.getJSONObject(i).getString("KeySerial"));
                                    orderEntity.setQuanti(serverResponse.getJSONObject(i).getString("ordQty"));
                                    orderEntity.setTotal(serverResponse.getJSONObject(i).getString("ordGValue"));
                                    orderEntity.setRate(serverResponse.getJSONObject(i).getString("ordRate"));
                                    orderEntity.setRot(serverResponse.getJSONObject(i).getString("ordROT"));
                                    orderEntity.setUQC(serverResponse.getJSONObject(i).getString("UQC"));
                                    orderEntities.add(orderEntity);


                                }




                                lv.setAdapter(new OrderDetailsAdapter(OrderDetailsActivity.this, orderEntities));

                            }

                        }

                        // Error status is true
                    } else {

                        Toast.makeText(OrderDetailsActivity.this, "Server Error",
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
