package com.tsysinfo.billing;

import android.content.Intent;
import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HelpActivity extends AppCompatActivity {

    ExpandableListAdapter listAdapter;
    ExpandableListView expListView;
    List<String> listDataHeader;
    HashMap<String, List<String>> listDataChild;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        // get the listview
        expListView = (ExpandableListView) findViewById(R.id.lvExp);

        // preparing list data
        prepareListData();

        listAdapter = new ExpandableListAdapter(this, listDataHeader, listDataChild);

        // setting list adapter
        expListView.setAdapter(listAdapter);

        // Listview Group click listener
        expListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {

            @Override
            public boolean onGroupClick(ExpandableListView parent, View v,
                                        int groupPosition, long id) {
                // Toast.makeText(getApplicationContext(),
                // "Group Clicked " + listDataHeader.get(groupPosition),
                // Toast.LENGTH_SHORT).show();
                return false;
            }
        });

        // Listview Group expanded listener
        expListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {

            @Override
            public void onGroupExpand(int groupPosition) {
                /*Toast.makeText(getApplicationContext(),
                        listDataHeader.get(groupPosition) + " Expanded",
                        Toast.LENGTH_SHORT).show();*/
            }
        });

        // Listview Group collasped listener
        expListView.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {

            @Override
            public void onGroupCollapse(int groupPosition) {
               /* Toast.makeText(getApplicationContext(),
                        listDataHeader.get(groupPosition) + " Collapsed",
                        Toast.LENGTH_SHORT).show();*/

            }
        });

        // Listview on child click listener
        expListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {

            @Override
            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {
                // TODO Auto-generated method stub
              /*  Toast.makeText(
                        getApplicationContext(),
                        listDataHeader.get(groupPosition)
                                + " : "
                                + listDataChild.get(
                                listDataHeader.get(groupPosition)).get(
                                childPosition), Toast.LENGTH_SHORT)
                        .show();*/
                return false;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.cust, menu);

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
        super.onBackPressed();
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }

    /*
     * Preparing the list data
     */
    private void prepareListData() {
        listDataHeader = new ArrayList<String>();
        listDataChild = new HashMap<String, List<String>>();

        // Adding child data
        listDataHeader.add("START");
        listDataHeader.add("ORDER");
        listDataHeader.add("DELIVERY");
        listDataHeader.add("BILLING");
        listDataHeader.add("SALES RETURN");
        listDataHeader.add("RECEIPT");
        listDataHeader.add("PRODUCTS");
        listDataHeader.add("CUST LIST");


        // Adding child data
        List<String> start = new ArrayList<String>();
        start.add("1. After Login Sync the data by clicking sync menu in upper right corner");
        start.add("2. If route is not assigned to the employee then ask admin to assign the route then sync");

        List<String> order = new ArrayList<String>();
        order.add("1. Tap to open order window");
        order.add("2. Add products to the cart");
        order.add("3. Tap the upper righr cart icon to go to the cart");
        order.add("4. Make Changes to the product details (i.e: quantity,MRP,discount)");
        order.add("5. Confirm the product by clicking CONFIRM");
        order.add("6. Clik REMOVE to remove the product from cart");
        order.add("7. Clik on the upper right cart icon to place the order");

        List<String> delivery = new ArrayList<String>();
        delivery.add("1. Tap to open delivery window");
        delivery.add("2. Check the products to the deliver");
        delivery.add("3. Tap the upper righr cart icon to go to the cart");
        delivery.add("4. Clik on the upper right cart icon to save delivery");

        List<String> bill = new ArrayList<String>();
        bill.add("1. Tap to open billing window");
        bill.add("2. Add products to the cart");
        bill.add("3. Tap the upper righr cart icon to go to the cart");
        bill.add("4. Make Changes to the product details (i.e: quantity,MRP,discount)");
        bill.add("5. Confirm the product by clicking CONFIRM");
        bill.add("6. Clik REMOVE to remove the product from cart");
        bill.add("7. Clik on the upper right cart icon to proceed");
        bill.add("8. Enter Amount");
        bill.add("9. Select sales type");
        bill.add("10. Tap on Generate Bill to generate receipt");

        List<String> ret = new ArrayList<String>();
        ret.add("1. Tap to open sales return window");
        ret.add("2. Add products to the return cart");
        ret.add("3. Tap the upper righr cart icon to save the sales return details");

        List<String> receipt = new ArrayList<String>();
        receipt.add("1. Tap to open receipt window");
        receipt.add("2. Select Customer if not automatically selected");
        receipt.add("3. Tap on the bill you want to pay");
        receipt.add("4. Enter Amount");
        receipt.add("5. Make Payment gernerate receipt");

        List<String> products = new ArrayList<String>();
        products.add("1. Tap to open product window");
        products.add("2. This menu is to see the products and its details");

        List<String> cust = new ArrayList<String>();
        cust.add("1. Open Navigation by clicking drawer icon on upper left corner  and choose customer list option ");
        cust.add("2. It shows the todays route customers");
        cust.add("3. Location of customer can be updated by clicking update icon");
        cust.add("4. View the customer location by clicking on map icon");
        cust.add("5. View all customers on map by clicking upper right map pointing icon");
        cust.add("6. Customer can be added by clicking the upper right add icon");
        cust.add("7. Fill all the details and save customer");


        listDataChild.put(listDataHeader.get(0), start); // Header, Child data
        listDataChild.put(listDataHeader.get(1), order);
        listDataChild.put(listDataHeader.get(2), delivery);
        listDataChild.put(listDataHeader.get(3), bill);
        listDataChild.put(listDataHeader.get(4), ret);
        listDataChild.put(listDataHeader.get(5), receipt);
        listDataChild.put(listDataHeader.get(6), products);
        listDataChild.put(listDataHeader.get(7), cust);
    }
}