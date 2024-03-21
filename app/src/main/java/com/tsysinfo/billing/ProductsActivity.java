package com.tsysinfo.billing;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Spinner;

import com.tsysinfo.billing.adapter.OrderListAdapter;
import com.tsysinfo.billing.database.DataBaseAdapter;
import com.tsysinfo.billing.database.Models;
import com.tsysinfo.billing.database.ProductTable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ProductsActivity extends AppCompatActivity {
    String act = "product";
    DataBaseAdapter dba;
    Models mod;
    // ProductBaseAdapter productBaseAdapter;
    OrderListAdapter orderListAdapter;
    RecyclerView recycler;
    ArrayAdapter<String> adapterProductCat;
    GridView lv;

    Spinner spinner, spgrp, spbrand;
    private ArrayList<String> listgrp;
    private EditText editextSearch;
    private ArrayList<ProductSearchResults> productlist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_activity);


        dba = new DataBaseAdapter(this);
        mod = new Models();

        lv = (GridView) findViewById(R.id.list);
        spgrp = (Spinner) findViewById(R.id.spinnerGroups);
        spbrand = (Spinner) findViewById(R.id.spinnerBrand);

        recycler = (RecyclerView) findViewById(R.id.recycler);
        recycler.setHasFixedSize(true);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getApplicationContext(), 2);
        recycler.setLayoutManager(gridLayoutManager);

        List<String> list = new ArrayList<>();
        dba.open();
        Cursor cursor = mod.getSpinGrpData("brand");
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                list.add(cursor.getString(0));
            }
            Collections.sort(list);

        }
        ArrayAdapter aa = new ArrayAdapter(this, android.R.layout.simple_spinner_item, list);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spbrand.setAdapter(aa);

        dba.close();
        spbrand.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (spbrand.getSelectedItem().toString().equalsIgnoreCase("All")) {
                    listgrp.clear();
                    String sql = "select distinct subgroups from products ";
                    dba.open();
                    Cursor cur = DataBaseAdapter.ourDatabase.rawQuery(sql, null);
                    if (cur.getCount() > 0) {
                        listgrp.add("Select SubGroup");

                        while (cur.moveToNext()) {
                            listgrp.add(cur.getString(cur.getColumnIndex("subgroups")));

                        }

                    }

                } else {
                    listgrp.clear();
                    String sql = "select distinct subgroups from products where brand ='" + spbrand.getSelectedItem().toString() + "'";
                    dba.open();
                    Cursor cur = DataBaseAdapter.ourDatabase.rawQuery(sql, null);
                    if (cur.getCount() > 0) {
                        listgrp.add("Select SubGroup");
                        while (cur.moveToNext()) {
                            listgrp.add(cur.getString(cur.getColumnIndex("subgroups")));

                        }

                    }

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

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

        spgrp.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                productlist = GetProductSearchResults("other");
               /* productBaseAdapter = new ProductBaseAdapter(ProductsActivity.this, productlist);
                lv.setAdapter(productBaseAdapter);
                productBaseAdapter.notifyDataSetChanged();*/

                orderListAdapter = new OrderListAdapter(getApplicationContext(), productlist);
                orderListAdapter.notifyDataSetChanged();
                recycler.setAdapter(orderListAdapter);
                recycler.setItemViewCacheSize(productlist.size());

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub

            }
        });
        dba.close();

        getIntent().getStringExtra("");

        List<String> category = new ArrayList<String>();

        String sql = "select distinct brand from products order by brand asc";
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
        spbrand.setAdapter(adapterProductCat);

        productlist = GetProductSearchResults("menu_flag");

        Log.w("productlist", productlist.toString());

        System.out.println(">>>>> size >>>>>" + productlist.size());

//        productBaseAdapter = new ProductBaseAdapter(ProductsActivity.this, productlist);
//        lv.setAdapter(productBaseAdapter);

        orderListAdapter = new OrderListAdapter(getApplicationContext(), productlist);
        orderListAdapter.notifyDataSetChanged();
        recycler.setAdapter(orderListAdapter);
        recycler.setItemViewCacheSize(productlist.size());

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
                // productBaseAdapter.getFilter().filter(s.toString());

            }
        });
    }

    private ArrayList<ProductSearchResults> GetProductSearchResults(String flag) {
        String sql = "";
        if (flag.equalsIgnoreCase("menu_flag")) {
            String brand = spbrand.getSelectedItem().toString().trim();

            System.out.println(">>>>>> GetProductSearchResults >>>>> menuFlag >>>>>>");

            if (brand.equalsIgnoreCase("All")) {
                sql = "select * from products order by name asc";

            } else {
                sql = "select * from products where brand='" + brand + "' order by name asc";

            }

        } else {
            String group = spgrp.getSelectedItem().toString().trim();

            if (group.equalsIgnoreCase("Select SubGroup")) {
                sql = "select * from products order by name asc";
            } else if (!group.equalsIgnoreCase("Select SubGroup")) {
                sql = "select * from products where subgroups='" + group + "' order by name asc";
            }
        }

        ArrayList<ProductSearchResults> results = new ArrayList<ProductSearchResults>();

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
                sr.setRot(cur.getString(cur.getColumnIndex("rot")).trim());
                sr.setAct(act);
                sr.setPrate(cur.getString(cur.getColumnIndex("prate")));
                sr.setStcok(cur.getString(cur.getColumnIndex("Stock")));
                sr.setCsz(cur.getString(cur.getColumnIndex(ProductTable.KEY_CARTON_SIZE)));
                results.add(sr);
            }
        }
        cur.close();
        dba.close();
        return results;
    }


    @SuppressLint("NewApi")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

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


    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        super.onBackPressed();
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }

}