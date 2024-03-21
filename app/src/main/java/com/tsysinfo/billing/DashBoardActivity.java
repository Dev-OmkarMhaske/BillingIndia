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
import android.widget.TextView;
import android.widget.Toast;

import com.tsysinfo.billing.database.DataBaseAdapter;
import com.tsysinfo.billing.database.GeoTagUpdate;
import com.tsysinfo.billing.database.Models;
import com.tsysinfo.billing.database.SessionManager;
import com.tsysinfo.billing.database.WebService;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class DashBoardActivity extends AppCompatActivity {

    ListView lv;
    SessionManager session;
    DataBaseAdapter dba;
    Models mod;
    TextView mTextGrandTotal;
    double sum;

    ProgressDialog waitDialog;
    JSONArray serverResponse;
    static boolean errored = false;
    ArrayList<DashbaordBean> dashbaordBeanArrayList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboardlayoutrecycle);

        lv = (ListView) findViewById(R.id.list);
        session = new SessionManager(this);
        dba = new DataBaseAdapter(this);
        mod = new Models();
        waitDialog = new ProgressDialog(this);
        waitDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        waitDialog.setMessage("Please Wait");

        dashbaordBeanArrayList=new ArrayList<>();

        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        new AsyncCallWS().execute();

        // [{"RowNo":"1","Head1":"Total Sales","Particulars":"Target","Amount":"42500000"}


    }

    private class AsyncCallWS extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            // Call Web Method
            try {

                Log.w("CartActivity", "Start");

                dba.open();
                serverResponse = WebService.getDashBoardDetail(session.getEmpNo(), "GetDashboard");
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
                        DashBoardActivity.this);
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

                                for (int i=0;i<serverResponse.length();i++)
                                {
                                       DashbaordBean dashbaordBean = new DashbaordBean();
                                       dashbaordBean.setRowNo(serverResponse.getJSONObject(i).getString("RowNo"));
                                       dashbaordBean.setAmount(serverResponse.getJSONObject(i).getString("Amount"));
                                       dashbaordBean.setParticulars(serverResponse.getJSONObject(i).getString("Particulars"));
                                       dashbaordBean.setHead1(serverResponse.getJSONObject(i).getString("Head1"));
                                       dashbaordBeanArrayList.add(dashbaordBean);

                                }

                                DashBoardAdapter dashBoardAdapter=new DashBoardAdapter(DashBoardActivity.this,dashbaordBeanArrayList);
                                lv.setAdapter(dashBoardAdapter);

                        }

                        // Error status is true
                    } else {

                        Toast.makeText(DashBoardActivity.this, "Server Error",
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
        Intent intent = new Intent(getApplicationContext(),
                MainActivity.class);
        startActivity(intent);
    }
}
