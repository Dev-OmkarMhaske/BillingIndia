package com.tsysinfo.billing;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.tsysinfo.billing.adapter.OrderListAdapter;
import com.tsysinfo.billing.database.DataBaseAdapter;
import com.tsysinfo.billing.database.Models;
import com.tsysinfo.billing.database.ProductTable;
import com.tsysinfo.billing.database.SessionManager;
import com.tsysinfo.billing.database.WebService;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class OrderActivity extends AppCompatActivity {
    // private int count = 0;
    SessionManager session;
    ImageView btnAddToCart;
    String act = "order";
    ArrayAdapter<String> adapterProductCat;
    DataBaseAdapter dba;
    Models mod;
    boolean first = true;
    ProgressDialog waitDialog;
    JSONArray serverResponse;
    static boolean errored = false;
    GridView lv;

    Spinner spinner;
    RecyclerView recycler;
    Spinner spinnerBrand;
    private Spinner spgrp;
    private String grpid;
    private ArrayList<String> listgrp;
    private EditText editextSearch;
    //    ProductBaseAdapter productBaseAdapter;
    OrderListAdapter orderListAdapter;

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_activity);

        dba = new DataBaseAdapter(this);
        mod = new Models();

        dba.open();

        mod.clearDatabase("orderr");
        // mod.clearDatabase("temp");
        dba.close();

        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.ll);
        linearLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideSoftKeyboard(OrderActivity.this);

                return false;
            }
        });
        session = new SessionManager(this);
        lv = (GridView) findViewById(R.id.list);
        spinnerBrand = (Spinner) findViewById(R.id.spinnerBrand);
        waitDialog = new ProgressDialog(this);
        waitDialog.setMessage("Please Wait..");
        waitDialog.setCancelable(false);
        btnAddToCart = (ImageView) findViewById(R.id.productAddToCart);
        getIntent().getStringExtra("");

        recycler = (RecyclerView) findViewById(R.id.recycler);
        recycler.setHasFixedSize(true);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getApplicationContext(), 2);
        recycler.setLayoutManager(gridLayoutManager);

        try {
            List<String> brands = new ArrayList<String>();

            String sql = "select distinct brand from products order by brand asc";
            dba.open();
            Cursor curBra = DataBaseAdapter.ourDatabase.rawQuery(sql, null);
            if (curBra.getCount() > 0) {
                brands.add("Select Brand");
                brands.add("All");
                while (curBra.moveToNext()) {
                    brands.add(curBra.getString(0).trim());
                }
            }
            dba.close();

            adapterProductCat = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, brands);
            adapterProductCat.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            if (MainActivity.OrderFlag == "S") {
            } else if (MainActivity.OrderFlag == "U") {
                setTitle("UnSchedule Order");
            } else if (MainActivity.OrderFlag == "R") {
                setTitle("Replacement Order");
            }


        } catch (Exception e) {
            e.printStackTrace();
            LogError lErr = new LogError();
            lErr.appendLog(e.toString());
        }
        spgrp = (Spinner) findViewById(R.id.spinnerGroups);
        listgrp = new ArrayList<>();
        dba.open();
        Cursor cursorgrp = mod.getSpinGrpData("subgroups");
        if (cursorgrp.getCount() > 0) {
            listgrp.add("Select SubGroup");
            while (cursorgrp.moveToNext()) {
                listgrp.add(cursorgrp.getString(0));
            }
        }

        ArrayAdapter aagrp = new ArrayAdapter(this, android.R.layout.simple_spinner_item, listgrp);
        aagrp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //Setting the ArrayAdapter data on the Spinner

        spgrp.setAdapter(aagrp);

        spinnerBrand.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (spinnerBrand.getSelectedItem().toString().equalsIgnoreCase("All")) {
                    listgrp.clear();
                    listgrp.add("Select SubGroup");
                    String sql = "select distinct subgroups from products order by subgroups asc";
                    dba.open();
                    Cursor cur = DataBaseAdapter.ourDatabase.rawQuery(sql, null);
                    if (cur.getCount() > 0) {

                        while (cur.moveToNext()) {
                            listgrp.add(cur.getString(cur.getColumnIndex("subgroups")));

                        }
                    }
                    cur.close();
                    dba.close();
                } else {
                    listgrp.clear();
                    String sql = "select distinct subgroups from products where brand ='" + spinnerBrand.getSelectedItem().toString() + "' order by subgroups asc";
                    dba.open();
                    Cursor cur = DataBaseAdapter.ourDatabase.rawQuery(sql, null);
                    if (cur.getCount() > 0) {
                        listgrp.add("Select SubGroup");
                        while (cur.moveToNext()) {
                            listgrp.add(cur.getString(cur.getColumnIndex("subgroups")));

                        }
                    }
                    cur.close();
                    dba.close();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spgrp.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ArrayList<ProductSearchResults> ProductSearchResults = GetProductSearchResults("other");
               /* productBaseAdapter = new ProductBaseAdapter(OrderActivity.this, ProductSearchResults);
                productBaseAdapter.notifyDataSetChanged();
                lv.setAdapter(productBaseAdapter);*/
                orderListAdapter = new OrderListAdapter(getApplicationContext(), ProductSearchResults);
                orderListAdapter.notifyDataSetChanged();
                recycler.setAdapter(orderListAdapter);
                recycler.setItemViewCacheSize(ProductSearchResults.size());

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub

            }
        });
        cursorgrp.close();
        dba.close();
        spinnerBrand.setAdapter(adapterProductCat);

        editextSearch = (EditText) findViewById(R.id.search);
        editextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                orderListAdapter.getFilter().filter(s.toString());
                //productBaseAdapter.getFilter().filter(s.toString());

            }
        });

        ArrayList<ProductSearchResults> ProductSearchResults = GetProductSearchResults("other");
       /* productBaseAdapter = new ProductBaseAdapter(OrderActivity.this, ProductSearchResults);
        productBaseAdapter.notifyDataSetChanged();
        lv.setAdapter(productBaseAdapter);*/
        lv.setTextFilterEnabled(true);
        orderListAdapter = new OrderListAdapter(getApplicationContext(), ProductSearchResults);
        orderListAdapter.notifyDataSetChanged();
        recycler.setAdapter(orderListAdapter);
        recycler.setItemViewCacheSize(ProductSearchResults.size());

    }

    private ArrayList<ProductSearchResults> GetProductSearchResults(String flag) {

        ArrayList<ProductSearchResults> results = new ArrayList<ProductSearchResults>();
        try {
            String sql = "";
            if (flag.equalsIgnoreCase("menu_flag")) {
                String brand = spinnerBrand.getSelectedItem().toString().trim();

                if (brand.equalsIgnoreCase("All")) {
                    sql = "select * from products order By name ";
                } else {
                    sql = "select * from products where brand='" + brand + "' order By name ";
                }
            } else {


                String group = spgrp.getSelectedItem().toString().trim();

                if (group.equalsIgnoreCase("Select SubGroup")) {
                    sql = "select * from products ";
                } else if (group.equalsIgnoreCase("")) {
                    sql = "select * from products";

                } else {

                    sql = "select * from products where subgroups='" + group + "' order By name";
                }
            }
            ProductSearchResults sr = new ProductSearchResults();

            dba.open();
            Cursor cur = DataBaseAdapter.ourDatabase.rawQuery(sql, null);
            if (cur.getCount() > 0) {
                while (cur.moveToNext()) {

                    sr = new ProductSearchResults();
                    sr.setID(cur.getString(0).trim());
                    sr.setName(cur.getString(1).trim());
                    sr.setDescription(cur.getString(2).trim());
                    sr.setPrice(cur.getString(7).trim());
                    sr.setDp(cur.getString(8).trim());
                    sr.setImage(cur.getString(10).trim());
                    sr.setAct(act);
                    sr.setRot(cur.getString(cur.getColumnIndex("rot")));
                    sr.setPrate(cur.getString(cur.getColumnIndex("prate")));
                    sr.setStcok(cur.getString(cur.getColumnIndex("Stock")));
                    sr.setCsz(cur.getString(cur.getColumnIndex(ProductTable.KEY_CARTON_SIZE)));
                    results.add(sr);
                }
            }
            cur.close();
            dba.close();
        } catch (Exception e) {
            e.printStackTrace();
            LogError lErr = new LogError();
            lErr.appendLog(e.toString());
        }
        return results;
    }

    @SuppressLint("NewApi")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.back);

        MenuItem menuItem = menu.findItem(R.id.actionCart);
        menuItem.setIcon(buildCounterDrawable(R.drawable.cart_action));

        MenuItem item = menu.findItem(R.id.spinner);
       /* spinner = (Spinner) MenuItemCompat.getActionView(item);
        spinner.setAdapter(adapterProductCat);

        spinner.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view,int position, long id) {
                // TODO Auto-generated method stub
                ArrayList<ProductSearchResults> ProductSearchResults = GetProductSearchResults("menu_flag");
                lv.setAdapter(new ProductBaseAdapter(OrderActivity.this,
                        ProductSearchResults));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub

            }
        });
*/
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.actionList:
                Intent intent = new Intent(getApplicationContext(), OrderListActivity.class);
                startActivity(intent);
                finish();
                return true;
            case R.id.actionCart:

                GPSTracker gps = new GPSTracker(this);

                if (gps.canGetLocation()) {
                    dba.open();
                    String sql = "select * from temp";
                    Cursor cur = DataBaseAdapter.ourDatabase.rawQuery(sql, null);
                    if (cur.getCount() > 0) {
                        Intent intents = new Intent(OrderActivity.this, CartActivity.class);
                        intents.putExtra("act", act);
                        startActivity(intents);
                        finish();
                    } else {
                        Toast.makeText(OrderActivity.this, "Add Item to cart first", Toast.LENGTH_LONG).show();
                    }
                    cur.close();
                    dba.close();
                } else {
                    gps.showSettingsAlert();
                }
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private Drawable buildCounterDrawable(int backgroundImageId) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.cart_button_layout, null);
        view.setBackgroundResource(backgroundImageId);
        int count = 0;
        dba.open();
        String sql = "select * from temp";
        Cursor cur = DataBaseAdapter.ourDatabase.rawQuery(sql, null);
        if (cur.getCount() > 0) {
            count = cur.getCount();
        } else {
            count = 0;
        }
        cur.close();
        dba.close();

        if (count == 0) {
            View counterTextPanel = view.findViewById(R.id.counterValuePanel);
            counterTextPanel.setVisibility(View.GONE);
        } else {
            TextView textView = (TextView) view.findViewById(R.id.count);
            textView.setText("" + count);
        }

        view.measure(View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED), View.MeasureSpec
                .makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());

        view.setDrawingCacheEnabled(true);
        view.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache());
        view.setDrawingCacheEnabled(false);

        return new BitmapDrawable(getResources(), bitmap);
    }

    public void doIncrease() {
        // count++;
        invalidateOptionsMenu();
    }

    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        super.onBackPressed();
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }

    private class AsyncCallWS extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            // Call Web Method
            try {

                Log.w("CartActivity", "Start");

                dba.open();
                serverResponse = WebService.getSubgroup(grpid, session.getBranchNo(), "GetSubGroup");
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
                        OrderActivity.this);
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

                            if (serverResponse.getJSONObject(0).getString("SBGName").trim().equalsIgnoreCase("No")) {
                                AlertDialog.Builder builder2 = new AlertDialog.Builder(OrderActivity.this);
                                builder2.setTitle("Failure..");
                                builder2.setMessage("Failed to get Response.");
                                builder2.setPositiveButton("OK",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(
                                                    DialogInterface dialog,
                                                    int which) {
                                                Log.e("info", "OK");
                                            }
                                        });
                                builder2.show();

                            } else {

                                for (int i = 0; i < serverResponse.length(); i++) {
                                    listgrp.add(serverResponse.getJSONObject(i).getString("SBGName"));

                                }
                            }
                        }
                        // Error status is true
                    } else {

                        Toast.makeText(OrderActivity.this, "Server Error", Toast.LENGTH_LONG).show();
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
