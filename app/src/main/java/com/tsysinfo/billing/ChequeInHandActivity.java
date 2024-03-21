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
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.Toast;
import com.tsysinfo.billing.database.DataBaseAdapter;
import com.tsysinfo.billing.database.Models;
import com.tsysinfo.billing.database.SessionManager;
import com.tsysinfo.billing.database.WebService;
import org.json.JSONArray;


import java.util.ArrayList;
public class ChequeInHandActivity extends AppCompatActivity {
    ListView lv;
    SessionManager session;
    DataBaseAdapter dba;
    Models mod;
    double sum;
    ProgressDialog waitDialog;
    JSONArray serverResponse;
    static boolean errored = false;
    ArrayList<ChequeIn> chequeInArrayList;
    String CustNameid;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.chequerecyclelistactivity);

        lv = (ListView) findViewById(R.id.list);
        session = new SessionManager(this);
        dba = new DataBaseAdapter(this);
        mod = new Models();
        waitDialog = new ProgressDialog(this);
        waitDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        waitDialog.setMessage("Please Wait");
        chequeInArrayList=new ArrayList<>();

        CustNameid = getIntent().getStringExtra("custid");

        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        new AsyncCallWS().execute();
    }

    private class AsyncCallWS extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            // Call Web Method
            try {

                Log.w("CartActivity", "Start");

                dba.open();
                serverResponse = WebService.getChequeDetails(CustNameid, "GetChequeDetails");
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
                        ChequeInHandActivity.this);
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

                    if (!errored) {
                        // Based on Boolean value returned from WebService
                        if (serverResponse != null) {

                            if (!serverResponse.getJSONObject(0).getString("DateOfReceipt").trim().equalsIgnoreCase("No")) {

                                for (int i = 0; i < serverResponse.length(); i++) {
                                    ChequeIn chequeIn = new ChequeIn();
                                    String itemdate = serverResponse.getJSONObject(i).getString("DateOfReceipt");
                                    String date = itemdate.substring(0, 10);
                                    chequeIn.setDateOfReceipt(date);
                                    String itemdate1 = serverResponse.getJSONObject(i).getString("ChequeDate");
                                    String date1 = itemdate1.substring(0, 10);
                                    chequeIn.setChequeNo(serverResponse.getJSONObject(i).getString("ChequeNo"));
                                    chequeIn.setChequeDate(date1);
                                    chequeIn.setChequeAmount(serverResponse.getJSONObject(i).getString("ChequeAmount"));
                                    chequeIn.setBillno(serverResponse.getJSONObject(i).getString("Bill"));
                                    chequeIn.setBillDate(serverResponse.getJSONObject(i).getString("BillDate"));

                                    chequeIn.setBillAmount(serverResponse.getJSONObject(i).getString("BillAmount"));
                                    chequeIn.setAllocatedAmount(serverResponse.getJSONObject(i).getString("AllocatedAmount"));
                                    chequeIn.setRdays(serverResponse.getJSONObject(i).getString("Rdays"));
                                    chequeInArrayList.add(chequeIn);

                                }

                                ChequeInHandAdapter chequeInHandAdapter = new ChequeInHandAdapter(ChequeInHandActivity.this, chequeInArrayList);
                                lv.setAdapter(chequeInHandAdapter);
                            } else {
                                Toast.makeText(ChequeInHandActivity.this, "Data Not Found",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }

                        // Error status is true
                    } else {

                        Toast.makeText(ChequeInHandActivity.this, "Server Error",
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
                }catch (Exception e) {
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

        // menu.findItem(R.id.actionChekout).setIcon(R.drawable.checkout_icon);

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
        finish();
    }
}
