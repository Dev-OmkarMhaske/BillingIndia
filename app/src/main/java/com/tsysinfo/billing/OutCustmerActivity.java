package com.tsysinfo.billing;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;

import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.tsysinfo.billing.database.DataBaseAdapter;
import com.tsysinfo.billing.database.Models;
import com.tsysinfo.billing.database.SessionManager;
import java.util.ArrayList;

public class OutCustmerActivity extends AppCompatActivity {

    ListView lv;
    SessionManager session;
    DataBaseAdapter dba;
    Models mod;
    TextView mTextGrandTotal;
    double sum;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.out_custmer_recycle);

        lv = (ListView) findViewById(R.id.list);
        session = new SessionManager(this);
        dba = new DataBaseAdapter(this);
        mod = new Models();

        mTextGrandTotal= (TextView) findViewById(R.id.mGrandtextidTotal);

        ArrayList<ReceiptSearchResults> ReceiptSearchResults = GetReceiptSearchResults();
        lv.setAdapter(new OutCustomerAdapter(OutCustmerActivity.this,ReceiptSearchResults));

    }

    private ArrayList<ReceiptSearchResults> GetReceiptSearchResults() {


        ArrayList<ReceiptSearchResults> results = new ArrayList<ReceiptSearchResults>();

        ReceiptSearchResults sr = new ReceiptSearchResults();

        try {

            dba.open();

            String sql = "select replace(custname,',','') As custname,SUM(outPending) as outPending,custid from receipts Group by custname";
            Cursor cur = DataBaseAdapter.ourDatabase.rawQuery(sql, null);
            Log.w("CA", "SQL1 :" + sql);
            if (cur.getCount() > 0) {
                while (cur.moveToNext()) {
                    sr = new ReceiptSearchResults();
                    sr.setNetDue(String.valueOf(Math.round(cur.getDouble(1) * 100.0) / 100.0));
                    sr.setCustName(cur.getString(cur.getColumnIndex("custname")));
                    sr.setKEY_custNo(cur.getString(cur.getColumnIndex("custid")));
                    results.add(sr);
                }

            } else {

                lv.setAdapter(null);
                Toast.makeText(OutCustmerActivity.this,
                        "No Outstanding Payment", Toast.LENGTH_LONG).show();

            }
            dba.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {

            dba.open();
            String sql1 = "select SUM(outPending) as outPending  from receipts";
            Cursor cur1 = DataBaseAdapter.ourDatabase.rawQuery(sql1, null);
            Log.w("CA", "SQL1 :" + sql1);
            if (cur1.getCount() > 0) {
                while (cur1.moveToNext()) {
                    sr = new ReceiptSearchResults();
                    sr.setNetDue(cur1.getString(0));
                    mTextGrandTotal.setText(cur1.getString(0));
                    results.add(sr);
                }

            } else {
                mTextGrandTotal.setText(" ");
                lv.setAdapter(null);
                Toast.makeText(OutCustmerActivity.this,
                        "No Outstanding Payment", Toast.LENGTH_LONG).show();

            }
            dba.close();
        } catch (Exception e) {
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

