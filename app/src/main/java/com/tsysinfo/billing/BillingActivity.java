package com.tsysinfo.billing;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.tsysinfo.billing.adapter.OrderListAdapter;
import com.tsysinfo.billing.database.DataBaseAdapter;
import com.tsysinfo.billing.database.Models;

import java.util.ArrayList;
import java.util.List;

public class BillingActivity extends AppCompatActivity {

    //	private int count = 0;
    ImageView btnAddToCart;

    String act = "billing";
    ArrayAdapter<String> adapterProductCat;
    OrderListAdapter orderListAdapter;
    DataBaseAdapter dba;
    Models mod;

    RecyclerView recycler;
    GridView lv;
    Spinner spinner;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_activity);

        dba = new DataBaseAdapter(this);
        mod = new Models();


        lv = (GridView) findViewById(R.id.list);
        recycler = (RecyclerView) findViewById(R.id.recycler);
        recycler.setHasFixedSize(true);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getApplicationContext(), 2);
        recycler.setLayoutManager(gridLayoutManager);

        btnAddToCart = (ImageView) findViewById(R.id.productAddToCart);

        getIntent().getStringExtra("");

        try {


            List<String> category = new ArrayList<String>();

            String sql = "select distinct brand from products";
            dba.open();
            Cursor curBra = DataBaseAdapter.ourDatabase.rawQuery(sql, null);
            if (curBra.getCount() > 0) {
                category.add("All");
                while (curBra.moveToNext()) {
                    category.add(curBra.getString(0).trim());
                }
            }
            dba.close();


            adapterProductCat = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, category);
            adapterProductCat.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            //	spinProdCat.setAdapter(adapterSearchFor);
        } catch (Exception e) {
            e.printStackTrace();
            LogError lErr = new LogError();
            lErr.appendLog(e.toString());
        }
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    private ArrayList<ProductSearchResults> GetProductSearchResults() {

        String brand = spinner.getSelectedItem().toString().trim();

        ArrayList<ProductSearchResults> results = new ArrayList<ProductSearchResults>();
        try {
            String sql = "";
            if (brand.equalsIgnoreCase("All")) {
                sql = "select * from products ";
            } else {
                sql = "select * from products where brand='" + brand + "'";
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
                    sr.setRot(cur.getString(cur.getColumnIndex("rot")).trim());
                    sr.setImage(cur.getString(10).trim());
                    sr.setAct(act);
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
        spinner = (Spinner) MenuItemCompat.getActionView(item);
        spinner.setAdapter(adapterProductCat);


        spinner.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // TODO Auto-generated method stub
                ArrayList<ProductSearchResults> ProductSearchResults = GetProductSearchResults();
                //    lv.setAdapter(new ProductBaseAdapter(BillingActivity.this, ProductSearchResults));

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
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;

            case R.id.actionCart:
                GPSTracker gps = new GPSTracker(this);

                if (gps.canGetLocation()) {
                    dba.open();
                    String sql = "select * from temp";
                    Cursor cur = DataBaseAdapter.ourDatabase.rawQuery(sql, null);
                    if (cur.getCount() > 0) {
                        Intent intent = new Intent(BillingActivity.this, CaAc.class);
                        intent.putExtra("act", act);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(BillingActivity.this, "Add Item to cart first", Toast.LENGTH_LONG).show();
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

        view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.setDrawingCacheEnabled(true);
        view.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache());
        view.setDrawingCacheEnabled(false);

        return new BitmapDrawable(getResources(), bitmap);
    }

    public void doIncrease() {
//		count++;
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


}
