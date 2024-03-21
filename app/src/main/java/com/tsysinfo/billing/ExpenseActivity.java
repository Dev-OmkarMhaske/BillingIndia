package com.tsysinfo.billing;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.location.Location;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
 
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.support.v7.app.AppCompatActivity;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.LocationServices;
import com.tsysinfo.billing.database.DataBaseAdapter;
import com.tsysinfo.billing.database.ExpenseSubGroup;
import com.tsysinfo.billing.database.Models;
import com.tsysinfo.billing.database.OfflineTable;
import com.tsysinfo.billing.database.SessionManager;
import com.tsysinfo.billing.database.TownTable;
import com.tsysinfo.billing.database.WebService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ExpenseActivity extends AppCompatActivity {
    // public static final String URL = "http://tempuri.org/BillingWebService.asmx/SaveExpense?BranchNo=1";
    Spinner s_select_expense, s_select_expensecategory;
    AutoCompleteTextView s_totown, s_fromtown;
    TextView tv_date;
    EditText editText_remark, editText_fare, et_date;
    Button bt_btn;
    ExpenseTownRowAdapter townRowAdapter;
    ArrayList<ExpenseTown> town;
    ArrayList<ExpenseType1> expense;
    ArrayList<ExpenseSupGroup1> mProductSearchResults;
    DataBaseAdapter dba;
    Models mod;

    ExpenseTypeAdapter exptypeAdapter;
    JSONArray serverResponse;
    static boolean errored = false;
    private ProgressDialog waitDialog;
    final Calendar myCalendar = Calendar.getInstance();
    SessionManager session;
    String Date, ExpenseType, ExpAmount, Remarks, EmpNo, s_totown1, s_fromtown1, s_select_expense1, spinnerSelectedText, SubGroup;
    String longi = "", lati = "";
    int position = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_branch);

        getSupportActionBar().setTitle(" Expense Details");
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        town = new ArrayList<>();
        expense = new ArrayList<>();
        mProductSearchResults = new ArrayList<>();
        dba = new DataBaseAdapter(this);
        mod = new Models();
        session = new SessionManager(this);

        s_totown = (AutoCompleteTextView) findViewById(R.id.s_totown);
        s_select_expense = (Spinner) findViewById(R.id.s_select_expense);
        s_select_expensecategory = (Spinner) findViewById(R.id.s_select_expensecategory);
        s_fromtown = (AutoCompleteTextView) findViewById(R.id.s_fromtown);
        tv_date = (TextView) findViewById(R.id.tv_date);
        editText_remark = (EditText) findViewById(R.id.editText_remark);
        editText_fare = (EditText) findViewById(R.id.editText_fare);
        bt_btn = (Button) findViewById(R.id.bt_btn);

        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        String strDate = sdf.format(c.getTime());
        tv_date.setText(strDate);

        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                // TODO Auto-generated method stub
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel();
            }
        };



        tv_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
//                new DatePickerDialog(ExpenseActivity.this, date, myCalendar
//                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
//                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
                Calendar mcurrentDate = Calendar.getInstance();
                int mYear = mcurrentDate.get(Calendar.YEAR);
                int mMonth = mcurrentDate.get(Calendar.MONTH);
                int mDay = mcurrentDate.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog mDatePicker = new DatePickerDialog(ExpenseActivity.this, new DatePickerDialog.OnDateSetListener() {
                    public void onDateSet(DatePicker datepicker, int selectedyear, int selectedmonth, int selectedday) {
                        // TODO Auto-generated method stub
                        selectedmonth = selectedmonth + 1;
                        String stringFromDate = String.valueOf(selectedmonth) + "/" + String.valueOf(selectedday) + "/" + String.valueOf(selectedyear);
                        tv_date.setText(stringFromDate);
                    }
                }, mYear, mMonth, mDay);

                //mDatePicker.setTitle("Select Expense date");
                mDatePicker.show();
            }
        });

//
//        tv_date.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // TODO Auto-generated method stub
//
//                Calendar mcurrentDate = Calendar.getInstance();
//                int mYear = mcurrentDate.get(Calendar.YEAR);
//                int mMonth = mcurrentDate.get(Calendar.MONTH);
//                int mDay = mcurrentDate.get(Calendar.DAY_OF_MONTH);
//                DatePickerDialog mDatePicker = new DatePickerDialog(ExpenseActivity.this, new DatePickerDialog.OnDateSetListener() {
//                    public void onDateSet(DatePicker datepicker, int selectedyear, int selectedmonth, int selectedday) {
//                        // TODO Auto-generated method stub
//                        selectedmonth = selectedmonth + 1;
//                        String stringFromDate = String.valueOf(selectedmonth) + "/" + String.valueOf(selectedday) + "/" + String.valueOf(selectedyear);
//                        tv_date.setText(stringFromDate);
//                    }
//                }, mYear, mMonth, mDay);
//
//                mDatePicker.setTitle("Select Expense date");
//                mDatePicker.show();
//
//            }
//        });


////

        dba.open();
        ArrayList<String> categories = new ArrayList<String>();
        String sql = "Select * from " + TownTable.DATABASE_TABLE + "";//" where " + TownTable.KEY_TOWN + " like '" + alphabates + "%' order by " + TownTable.KEY_TOWN + "";
        Cursor cursor = DataBaseAdapter.ourDatabase.rawQuery(sql, null);
        categories.add("Select Town");
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {

                categories.add(cursor.getString(cursor.getColumnIndex(TownTable.KEY_TOWN)));
            }

        }

        dba.close();
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, categories);
        s_totown.setThreshold(1);
        s_totown.setAdapter(adapter);
        s_fromtown.setThreshold(1);
        s_fromtown.setAdapter(adapter);

//        s_select_expense.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
//                spinnerSelectedText = s_select_expense.getSelectedItem().toString();
//
//
//
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> adapterView) {
//
//            }
//        });
        s_select_expense.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                new AsyncCallWSearch().execute();
//                if (parent.getSelectedItemPosition() == 0)
//                {
//                    new AsyncCallWSearch().execute();
//                }
//                else if (parent.getSelectedItemPosition() == 1)
//                {
//                    new AsyncCallWSearch().execute();
//                }
//                else if (parent.getSelectedItemPosition() == 2)
//                {
//                    new AsyncCallWSearch().execute();
//                }
//                else if (parent.getSelectedItemPosition() == 3)
//                {
//
//
//                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


//        s_select_expense.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//
//            }
//        });



        bt_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Date = tv_date.getText().toString();
                ExpAmount = editText_fare.getText().toString();
                Remarks = editText_remark.getText().toString();
//                s_totown1 = town.get(s_totown.getSelectedItemPosition()).getId();
//                s_fromtown1 = town.get(s_fromtown.getSelectedItemPosition()).getId();

                s_totown1 = s_totown.getText().toString();
                s_fromtown1 = s_fromtown.getText().toString();
                ExpenseType = expense.get(s_select_expense.getSelectedItemPosition()).getId();


//

                String logFlag = "";
                GPSTracker gps = new GPSTracker(ExpenseActivity.this);
                if (gps.canGetLocation()) {
                    if (gps.getLongitude() != 0.0 && gps.getLatitude() != 0.0) {
                        longi = String.valueOf(gps.getLongitude());
                        lati = String.valueOf(gps.getLatitude());
                        logFlag = "LocationManager";
                    } else {
                        @SuppressLint("MissingPermission") Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(MainActivity.mGoogleApiClient);
                        longi = String.valueOf(mLastLocation.getLongitude());
                        lati = String.valueOf(mLastLocation.getLatitude());
                        logFlag = "GoogleApi";
                    }
                } else {
                    gps.showSettingsAlert();
                }
                new AsyncCallWS().execute();
            }
        });

//
//        ExpenseTown exptown = new ExpenseTown();
//        exptown.setId("");
//        exptown.setTown("Select Town");
//        town.add(exptown);
//
//        dba.open();
//        String sql = "select * from town";
//        Cursor cursor = DataBaseAdapter.ourDatabase.rawQuery(sql, null);
//        if (cursor.getCount() > 0) {
//            while (cursor.moveToNext()) {
//                exptown = new ExpenseTown();
//                exptown.setId(cursor.getString(cursor.getColumnIndex("id")));
//                exptown.setTown(cursor.getString(cursor.getColumnIndex("townname")));
//                town.add(exptown);
//            }
//            townRowAdapter = new ExpenseTownRowAdapter(ExpenseActivity.this, android.R.layout.simple_spinner_item, R.id.mtextid, town);
//            townRowAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
//            s_totown.setAdapter(townRowAdapter);
//            s_fromtown.setAdapter(townRowAdapter);
//

//
//
//        }
//        dba.close();
//
        ExpenseType1 extype = new ExpenseType1();
        extype.setId("");
        extype.setexpensetype("Select Expense Type");
        expense.add(extype);

        dba.open();
        String sql2 = "select * from expensetype";
        Cursor cursor2 = DataBaseAdapter.ourDatabase.rawQuery(sql2, null);
        if (cursor2.getCount() > 0) {
            while (cursor2.moveToNext()) {
                extype = new ExpenseType1();
                extype.setId(cursor2.getString(cursor2.getColumnIndex("id")));
                extype.setexpensetype(cursor2.getString(cursor2.getColumnIndex("expensename")));
                expense.add(extype);
            }
//            exptypeAdapter = new ExpenseTypeAdapter(ExpenseActivity.this, android.R.layout.simple_spinner_item, R.id.mtextid, expense);
//            exptypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
//            s_select_expense.setAdapter(exptypeAdapter);
            exptypeAdapter = new ExpenseTypeAdapter(ExpenseActivity.this, android.R.layout.simple_spinner_item, R.id.mtextid, expense);
            exptypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
            s_select_expense.setAdapter(exptypeAdapter);
            exptypeAdapter.notifyDataSetChanged();
        }

        dba.close();
    }


    private class AsyncCallWSearch extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            // Call Web Method
            try {
                Log.w("ExpenseActivity", "Start");
                dba.open();
                String expid = expense.get(s_select_expense.getSelectedItemPosition()).getId();
                serverResponse = WebService.GetExpenseSubGroup(session.getBranchNo(), expid, "GetExpenseSubGroup");
                dba.close();
            } catch (Exception e) {
                Log.w("ExpenseActivity", "Timeout ");
            }
            return null;
        }

        @SuppressLint("ResourceAsColor")
        @Override
        // Once WebService returns response
        protected void onPostExecute(Void result) {

            Log.w("ExpenseActivity", "TimeOutFlag : " + WebService.timeoutFlag);
            Log.w("ExpenseActivity", "ResponseString : "
                    + WebService.responseString);

            // Make Progress Bar invisible
            //waitDialog.cancel();
            Log.w("ExpenseActivity", "Dialog Closed");
            if (WebService.timeoutFlag == 1) {
                Log.w("ExpenseActivity", "Timeout");
                AlertDialog.Builder builder = new AlertDialog.Builder(
                        ExpenseActivity.this);
                builder.setTitle("Connection Time Out!");
                builder.setMessage("Please Try Again!!!").setCancelable(false).setPositiveButton("Ok",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            } else {
                Log.w("ExpenseActivity", "Try");
                try {
                    /*
                     * Toast.makeText(LoginActivity.this, "JSON Data:  " +
                     * WebService.responseString, Toast.LENGTH_LONG).show();
                     */
                    // Error status is false
                    if (!errored) {
                        // Based on Boolean value returned from WebService
                        if (serverResponse != null) {
                            if (serverResponse.getJSONObject(0).getString("Keyserial").trim().equalsIgnoreCase("No")) {
                                Toast.makeText(ExpenseActivity.this, "No More Item Found", Toast.LENGTH_SHORT).show();
                            } else {
                                mProductSearchResults.clear();
                                if (serverResponse.length() > 0) {
                                    for (int i = 0; i < serverResponse.length(); i++) {
                                        ExpenseSupGroup1 subgroup = new ExpenseSupGroup1();
                                        subgroup.setId(serverResponse.getJSONObject(i).getString("Keyserial"));
                                        subgroup.setexpensesubgroup(serverResponse.getJSONObject(i).getString("expDescr"));
                                        mProductSearchResults.add(subgroup);
                                    }
                                    ExpenseSubGroupAdpter adapter = new ExpenseSubGroupAdpter(ExpenseActivity.this, android.R.layout.simple_spinner_item, R.id.mtextid, mProductSearchResults);
                                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
                                    s_select_expensecategory.setAdapter(adapter);
                                    adapter.notifyDataSetChanged();
                                }
                            }
                        }
                        // Error status is true
                    } else {
                        Toast.makeText(ExpenseActivity.this, "Server Error", Toast.LENGTH_LONG).show();
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
            //  waitDialog.show();
            // waitDialog.setCanceledOnTouchOutside(false);
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }


    private class AsyncCallWS extends AsyncTask<String, Void, Void> {
        @Override

        protected Void doInBackground(String... params) {
            // Call Web Method
            try {
                Log.w("ExpenseActivity", "Start");
                dba.open();
                String SubGroup = mProductSearchResults.get(s_select_expensecategory.getSelectedItemPosition()).getId();
                serverResponse = WebService.SaveExpense(session.getEmpNo(), Date, ExpenseType, s_fromtown1, s_totown1, ExpAmount, Remarks, longi, lati, session.getUserId(), SubGroup, "SaveExpense");
                dba.close();

            } catch (Exception e) {
                Log.w("ExpenseActivity", "Timeout ");
            }
            return null;
        }

        @SuppressLint("ResourceAsColor")
        @Override
        // Once WebService returns response
        protected void onPostExecute(Void result) {

            Log.w("ExpenseActivity", "TimeOutFlag : " + WebService.timeoutFlag);
            Log.w("ExpenseActivity", "ResponseString : "
                    + WebService.responseString);

            // Make Progress Bar invisible
            //waitDialog.cancel();
            Log.w("ExpenseActivity", "Dialog Closed");
            if (WebService.timeoutFlag == 1) {
                Log.w("ExpenseActivity", "Timeout");
                AlertDialog.Builder builder = new AlertDialog.Builder(ExpenseActivity.this);
                builder.setTitle("Connection Time Out!");
                builder.setMessage("Please Try Again!!!").setCancelable(false).setPositiveButton("Ok",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            } else {
                Log.w("ExpenseActivity", "Try");
                try {
                    if (!errored) {
                        if (serverResponse != null) {
                            if (serverResponse.getJSONObject(0).getString("Status").trim().equalsIgnoreCase("Fail")) {
                                AlertDialog.Builder builder2 = new AlertDialog.Builder(ExpenseActivity.this);
                                builder2.setTitle("Failure..");
                                builder2.setMessage("Failed to save Expense.");
                                builder2.setPositiveButton("OK",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                Log.e("info", "OK");
                                            }
                                        });
                                builder2.show();
                            } else if (serverResponse.getJSONObject(0).getString("Status").trim().equalsIgnoreCase("Success")) {

                                AlertDialog.Builder builder2 = new AlertDialog.Builder(ExpenseActivity.this);
                                builder2.setTitle(" Expense Saved Successfully..");
                                // builder2.setMessage("Expense " + serverResponse.getJSONObject(0).getString("Status").trim()	+"  Saved!!!");
                                //builder2.setMessage("Expense Saved Successfully.");
                                builder2.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        Log.e("info", "OK");
                                        Intent intent = new Intent(ExpenseActivity.this, MainActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                });
                                builder2.show();
                            }
                        }
                    } else {
                        Toast.makeText(ExpenseActivity.this, "Server Error", Toast.LENGTH_LONG).show();
                    }
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
            //  waitDialog.show();
            // waitDialog.setCanceledOnTouchOutside(false);
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                finish();
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    private void updateLabel() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        String strDate = sdf.format(c.getTime());
        tv_date.setText(strDate);
    }

}