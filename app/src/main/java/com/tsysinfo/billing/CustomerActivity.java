package com.tsysinfo.billing;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
 
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.support.v7.app.AppCompatActivity;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.tsysinfo.billing.database.AllCustomer;
import com.tsysinfo.billing.database.CustomerTable;
import com.tsysinfo.billing.database.DataBaseAdapter;
import com.tsysinfo.billing.database.Models;
import com.tsysinfo.billing.database.TownTable;

import java.util.ArrayList;
import java.util.List;

public class CustomerActivity extends AppCompatActivity {

    DataBaseAdapter dba;
    Models mod;

    ToastService toastService;

    String Name="",Town="";
    EditText name;
    AutoCompleteTextView town;
    EditText  Alpha;
    ListView lv;
    String selectionID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cust_list_activity);

        dba = new DataBaseAdapter(this);
        mod = new Models();
        name=(EditText)findViewById(R.id.editTextName);
        name.setEnabled(true);


        town=(AutoCompleteTextView) findViewById(R.id.spinner);
        Alpha=(EditText) findViewById(R.id.spinnerChar);
        toastService = new ToastService(CustomerActivity.this);

        dba.open();
        List<String> categories = new ArrayList<String>();
        String sql = "Select * from " + TownTable.DATABASE_TABLE + "";//" where " + TownTable.KEY_TOWN + " like '" + alphabates + "%' order by " + TownTable.KEY_TOWN + "";
        Cursor cursor = DataBaseAdapter.ourDatabase.rawQuery(sql, null);
        categories.add("Select Town");
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {

                categories.add(cursor.getString(cursor.getColumnIndex(TownTable.KEY_TOWN)));
            }

        }


        dba.close();
        ArrayAdapter adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1,categories);
        town.setThreshold(1);
        town.setAdapter(adapter);



        name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Name=name.getText().toString();

                ArrayList<CustomerSearchResults> CustomerSearchResults = GetCustomerSearchResults(Name,Town);
                lv.setAdapter(new CustomerBaseAdapter(CustomerActivity.this, CustomerSearchResults));

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        town.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Town=town.getText().toString();
                if(Town.equalsIgnoreCase("Select Town"))
                {
                    Toast.makeText(CustomerActivity.this, "Select town", Toast.LENGTH_SHORT).show();
                }
                else {
                    name.setEnabled(true);
                    ArrayList<CustomerSearchResults> CustomerSearchResults = GetCustomerSearchResults(Name, Town);
                    lv.setAdapter(new CustomerBaseAdapter(CustomerActivity.this, CustomerSearchResults));
                }
            }
        });
        town.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Town=town.getText().toString();
                    if(Town.equalsIgnoreCase("Select Town"))
                    {
                        Toast.makeText(CustomerActivity.this, "Select town", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        name.setEnabled(true);
                ArrayList<CustomerSearchResults> CustomerSearchResults = GetCustomerSearchResults(Name,Town);
                lv.setAdapter(new CustomerBaseAdapter(CustomerActivity.this, CustomerSearchResults));
            }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        lv = (ListView) findViewById(R.id.list);
        ArrayList<CustomerSearchResults> CustomerSearchResults = GetCustomerSearchResults("","");
        lv.setAdapter(new CustomerBaseAdapter(this, CustomerSearchResults));

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Object o = lv.getItemAtPosition(position);
                CustomerSearchResults fullObject = (CustomerSearchResults) o;

                String empNo = fullObject.getID();
                selectionID=  town.getText().toString();
                Intent intent= new Intent(CustomerActivity.this,AddCustomerActivity.class);
                intent.putExtra("key","update");
                intent.putExtra("ID",empNo);
                startActivity(intent);
            }
        });
        GPSTracker gps = new GPSTracker(CustomerActivity.this);
        if(gps.canGetLocation()){
            if(gps.getLatitude() != 0.0 && gps.getLongitude() != 0.0){

            }else{
                toastService.startTimer();
            }
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

    private ArrayList<CustomerSearchResults> GetCustomerSearchResults(String Cname,String Ctown) {
        ArrayList<CustomerSearchResults> results = new ArrayList<CustomerSearchResults>();

        try{

        CustomerSearchResults sr = new CustomerSearchResults();

        dba.open();
        Cursor cur = null;

        cur = DataBaseAdapter.ourDatabase.rawQuery("select * from "+ AllCustomer.DATABASE_TABLE+" where "+AllCustomer.KEY_NAME+" like '%"+Cname+"%' AND "+AllCustomer.KEY_AREA+" like '%"+Ctown+"%' order by "+AllCustomer.KEY_NAME+"",  null);

        if (cur.getCount() > 0) {

            while (cur.moveToNext()) {

                sr = new CustomerSearchResults();
                sr.setID(cur.getString(cur.getColumnIndex("id")).trim());
                sr.setName(cur.getString(cur.getColumnIndex("name")).trim());
                sr.setAddress(cur.getString(cur.getColumnIndex("address")).trim());
                sr.setPhone(cur.getString(cur.getColumnIndex("landline")).trim());
                sr.setLatl(cur.getString(cur.getColumnIndex("lati")).trim());
                sr.setLog(cur.getString(cur.getColumnIndex("longi")).trim());
                results.add(sr);
            }

        }
        cur.close();
        dba.close();
        }catch (Exception e){
            e.printStackTrace();
            LogError lErr = new LogError();
            lErr.appendLog(e.toString());
        }
        return results;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.cust_menu, menu);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.back);

        menu.findItem(R.id.action_all_loction).setIcon(R.drawable.all_locations);


        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;

            case R.id.action_all_loction:
                dba.open();
                Cursor cur = mod.getData(AllCustomer.DATABASE_TABLE);
                if (cur.getCount() > 0) {
                Intent intentMap1 = new Intent(getApplicationContext(), Map1.class);
                startActivity(intentMap1);
                finish();
                } else {
                    Toast.makeText(getApplicationContext(), "Customers not Available Please Sync First", Toast.LENGTH_LONG).show();
                }
                cur.close();
                dba.close();
                return true;

            case R.id.action_add_cust:
                Intent intent = new Intent(getApplicationContext(), AddCustomerActivityNew.class);
                intent.putExtra("key","new");
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


}