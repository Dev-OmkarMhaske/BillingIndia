package com.tsysinfo.billing;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
 
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.LocationServices;
import com.tsysinfo.billing.barcodes.FullScannerActivity;
import com.tsysinfo.billing.database.CustomerTable;
import com.tsysinfo.billing.database.DataBaseAdapter;
import com.tsysinfo.billing.database.Models;
import com.tsysinfo.billing.database.SessionManager;
import com.tsysinfo.billing.database.TempReciept;
import com.tsysinfo.billing.database.WebService;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class outActivity extends AppCompatActivity {

    static Spinner spinCustomer;
    public Location mLastLocation;
    String custid, custname;
    TextView balAmount;
    ListView lv;
    int i;
    double sum = 0,sum1=0;
    String Data = "";
    Double currLatitude;
    Double currLongitude;
    SessionManager session;
    DataBaseAdapter dba;
    Models mod;
    JSONArray serverResponse;
    static boolean errored = false;    ToastService toastService;
    private String GLOBAL_CUST_ID;
    private Button procced;
    private ProgressDialog waitDialog;
    public String recNo="";
    private TextView txtRcpNo;
    private EditText editPaidAmount;
    private Spinner spinPayType;
    private EditText editBank;
    private EditText editChqNo;
    private TextView textChqDate;
    private EditText editDrawnOn;
    public static  double tot=0.0;
    private TextView txtRcpDate;
    public static double allocated=0.0;
    public static double remain=0.0;
    private TextView tvbal;
    private RadioGroup radioGroup;
    private EditText remark;
    private TextView mTextView;
    private AutoCompleteTextView autoCompleteTextView;
    String CustName1,TotalAmountCus;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                String myString = "";
                GLOBAL_CUST_ID = data.getStringExtra("BarCodeValue");

                dba.open();
                String sql = "select " + CustomerTable.KEY_NAME + " from "
                        + CustomerTable.DATABASE_TABLE+ " where "+CustomerTable.KEY_CUSTOMER_CODE+" = '"+GLOBAL_CUST_ID+"' ";
                Cursor custCur = DataBaseAdapter.ourDatabase.rawQuery(sql, null);
                if(custCur.getCount()>0)
                {
                    custCur.moveToFirst();
                    myString=custCur.getString(custCur.getColumnIndex(CustomerTable.KEY_NAME)) ;

                    ArrayAdapter myAdap = (ArrayAdapter) spinCustomer.getAdapter(); //cast to an ArrayAdapter

                    int spinnerPosition = myAdap.getPosition(myString);

//set the default according to value
                    spinCustomer.setSelection(spinnerPosition);
                }else
                {
                    String temp=GLOBAL_CUST_ID;
                    temp = temp.replaceAll("[^\\d.]", "");
                    if(temp.equalsIgnoreCase(""))
                    {
                        temp="1234567";
                    }
                    String sql1 = "select " + CustomerTable.KEY_NAME + " from " + CustomerTable.DATABASE_TABLE+ " where "+CustomerTable.KEY_ID+"="+temp+" ";
                    Cursor custCur1 = DataBaseAdapter.ourDatabase.rawQuery(sql1, null);

                    if(custCur1.getCount()>0)
                    {
                        custCur1.moveToFirst();
                        myString=custCur1.getString(custCur1.getColumnIndex(CustomerTable.KEY_NAME)) ;

                        ArrayAdapter myAdap = (ArrayAdapter) spinCustomer.getAdapter(); //cast to an ArrayAdapter

                        int spinnerPosition = myAdap.getPosition(myString);

//set the default according to value
                        spinCustomer.setSelection(spinnerPosition);
                    }
                    else
                    {
                        spinCustomer.setSelection(0);
                        Toast.makeText(outActivity.this, "No User Found", Toast.LENGTH_SHORT).show();

                    }

                }
                dba.close();
                //the value you want the position for

            }
        }
    }
    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

        setContentView(R.layout.out_activity);
        remark=(EditText)findViewById(R.id.editRemark);
        mTextView= (TextView) findViewById(R.id.mtextidTotal);
        autoCompleteTextView= (AutoCompleteTextView) findViewById(R.id.searchView);
        tot=0.0;
        // References
        session = new SessionManager(this);
        dba = new DataBaseAdapter(this);
        mod = new Models();
        dba.open();
        mod.clearDatabase(TempReciept.DATABASE_TABLE);
        dba.close();

        CustName1 = getIntent().getStringExtra("custid");
        //autoCompleteTextView.setText(CustName1);
        TotalAmountCus = getIntent().getStringExtra("Tamount");

        toastService = new ToastService(outActivity.this);
        radioGroup=(RadioGroup)findViewById(R.id.radios);
        waitDialog = new ProgressDialog(this);
        waitDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        waitDialog.setMessage("Please Wait");        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        spinCustomer = (Spinner) findViewById(R.id.spinnerCustomer);

        lv = (ListView) findViewById(R.id.list);
        lv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        lv.setItemsCanFocus(false);

        balAmount = (TextView) findViewById(R.id.textBalanceAmount);

        procced=(Button)findViewById(R.id.btnProcced);


        tvbal=(TextView)findViewById(R.id.bal);
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(
                "MM/dd/yyyy");
        String strDate = sdf.format(c.getTime());


        final LinearLayout linearLayoutCqDate=(LinearLayout)findViewById(R.id.llCqDate) ;
        final LinearLayout linearLayoutCqNo=(LinearLayout)findViewById(R.id.llCqNo) ;
        final LinearLayout linearLayoutCqBank=(LinearLayout)findViewById(R.id.llCqBank) ;

        txtRcpNo=(TextView)findViewById(R.id.rcpno);
        txtRcpDate=(TextView)findViewById(R.id.date);
        editPaidAmount = (EditText) findViewById(R.id.editPaidAmount);
        spinPayType = (Spinner) findViewById(R.id.method);
        editChqNo = (EditText) findViewById(R.id.textChequeNo);
        textChqDate = (TextView) findViewById(R.id.textChequeDate);
        editDrawnOn = (EditText) findViewById(R.id.editDrawnOn);

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // find which radio button is selected
                if (checkedId == R.id.cash) {
                    linearLayoutCqBank.setVisibility(View.GONE);
                    linearLayoutCqDate.setVisibility(View.GONE);
                    linearLayoutCqNo.setVisibility(View.GONE);
                } else if (checkedId == R.id.cheque) {
                    linearLayoutCqBank.setVisibility(View.VISIBLE);
                    linearLayoutCqDate.setVisibility(View.VISIBLE);
                    linearLayoutCqNo.setVisibility(View.VISIBLE);
                }
            }
        });


        editPaidAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                double d=Double.parseDouble(s.toString());
                allocated=d;
                remain=d;
                tvbal.setText("Rs."+d);

            }
        });

        spinPayType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (spinPayType.getSelectedItem().toString().equalsIgnoreCase("Cash"))
                {
                    linearLayoutCqBank.setVisibility(View.GONE);
                    linearLayoutCqDate.setVisibility(View.GONE);
                    linearLayoutCqNo.setVisibility(View.GONE);


                }else {

                    linearLayoutCqBank.setVisibility(View.VISIBLE);
                    linearLayoutCqDate.setVisibility(View.VISIBLE);
                    linearLayoutCqNo.setVisibility(View.VISIBLE);

                }


            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        txtRcpDate.setText(strDate);

        textChqDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Calendar mcurrentDate = Calendar.getInstance();
                int mYear = mcurrentDate.get(Calendar.YEAR);
                final int mMonth = mcurrentDate.get(Calendar.MONTH);
                int mDay = mcurrentDate.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog mDatePicker = new DatePickerDialog(
                        outActivity.this, new DatePickerDialog.OnDateSetListener() {
                    public String mmonth = "";
                    public void onDateSet(DatePicker datepicker,
                                          int selectedyear, int selectedmonth,
                                          int selectedday) {
                        // TODO Auto-generated method stub
                        int month = selectedmonth + 1;
                        if (month == 1) {
                            mmonth = "01";
                        } else if (month == 2) {
                            mmonth = "02";
                        } else if (month == 3) {
                            mmonth = "03";
                        } else if (month == 4) {
                            mmonth = "04";
                        } else if (month == 5) {
                            mmonth = "05";
                        } else if (month == 6) {
                            mmonth = "06";
                        } else if (month == 7) {
                            mmonth = "07";
                        } else if (month == 8) {
                            mmonth = "08";
                        } else if (month == 9) {
                            mmonth = "09";
                        } else if (month == 10) {
                            mmonth = "10";
                        } else if (month == 11) {
                            mmonth = "11";
                        } else if (month == 12) {
                            mmonth = "12";
                        }
                        String a = String.valueOf(selectedday);
                        if (a.length() == 1) {
                            a = "0" + selectedday;
                        }
                        String dt = mmonth + "/" + a + "/"
                                + selectedyear;
                        textChqDate.setText(dt);
                    }
                }, mYear, mMonth, mDay);
                mDatePicker.setTitle("Select Date");
                mDatePicker.show();
            }
        });


        procced.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                 String payttype="";

                if (radioGroup.getCheckedRadioButtonId() == R.id.cash) {
                    payttype="Cash";
                } else if (radioGroup.getCheckedRadioButtonId() == R.id.cheque) {
                    payttype="Cheque";
                }
                String editPaid=editPaidAmount.getText().toString();
                 String chqNo=editChqNo.getText().toString();
                 String chqdate=textChqDate.getText().toString();
                 String drawnOn=editDrawnOn.getText().toString();

                 if (payttype.equalsIgnoreCase("Cash"))
                 {
                    if( editPaid.toString().equalsIgnoreCase(""))
                    {
                        Toast.makeText(outActivity.this, "Enter Received amount", Toast.LENGTH_SHORT).show();
                        return;
                    }

                 }else {

                     if( editPaid.toString().equalsIgnoreCase(""))
                     {
                         Toast.makeText(outActivity.this, "Enter Received amount", Toast.LENGTH_SHORT).show();
                         return;
                     }
                     if( chqNo.toString().equalsIgnoreCase(""))
                     {
                         Toast.makeText(outActivity.this, "Enter Cheque No", Toast.LENGTH_SHORT).show();
                         return;
                     }
                     if( chqdate.toString().equalsIgnoreCase(""))
                     {
                         Toast.makeText(outActivity.this, "Select Cheque Date", Toast.LENGTH_SHORT).show();
                         return;
                     }
                     if( drawnOn.toString().equalsIgnoreCase(""))
                     {
                         Toast.makeText(outActivity.this, "Enter Bank Name", Toast.LENGTH_SHORT).show();
                         return;
                     }
                 }

                dba.open();
                Cursor cursor=mod.getData(TempReciept.DATABASE_TABLE);

                if (cursor.getCount()>0)
                {
                    Intent intent = new Intent(getApplicationContext(),
                            PaymentActivity.class);

                    intent.putExtra("custid", custid);
                    intent.putExtra("recNo", recNo);
                    intent.putExtra("custName",spinCustomer.getSelectedItem().toString().trim());
                    intent.putExtra("remark",remark.getText().toString());
                    intent.putExtra("payttype", payttype);
                    intent.putExtra("editPaid", editPaid);
                    intent.putExtra("chqNo", chqNo);
                    intent.putExtra("chqdate", chqdate);
                    intent.putExtra("drawnOn", drawnOn);

                    startActivity(intent);
                    finish();

                }
                else
                {

                    Toast.makeText(outActivity.this, "Please Select  and Confirm bills", Toast.LENGTH_SHORT).show();

                }

                dba.close();

            }
        });

        ImageView barCode=(ImageView)findViewById(R.id.imageButtonBarcode);
        barCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(outActivity.this,FullScannerActivity.class);
                spinCustomer.setSelection(0);
                startActivityForResult(intent, 1);
            }
        });

        GPSTracker gps = new GPSTracker(this);

        try {


            dba.open();
            ArrayList<String> customers = new ArrayList<String>();
            String sql1 = "select * from "
                    + CustomerTable.DATABASE_TABLE;
            Cursor custCur1 = DataBaseAdapter.ourDatabase.rawQuery(sql1, null);
            if (custCur1.getCount() > 0) {
                customers.add("Select Customer");
                while (custCur1.moveToNext()) {
                    customers.add(custCur1.getString(1).trim());
                }
            }
            custCur1.close();
            dba.close();

            // Creating adapter for spinner
            ArrayAdapter<String> adapterRelation = new ArrayAdapter<String>(this,
                    android.R.layout.simple_spinner_item, customers);
            adapterRelation
                    .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinCustomer.setAdapter(adapterRelation);
            autoCompleteTextView.setThreshold(1);
            autoCompleteTextView.setAdapter(adapterRelation);
            Log.w("CustName1", "CustName :" + CustName1);

            if(CustName1 != null && CustName1 != "") {
                int spinnerPosition = adapterRelation.getPosition(CustName1.toString().trim());
                Log.w("CustName1", "Position :" + spinnerPosition);
                spinCustomer.setSelection(spinnerPosition);

                ArrayList<ReceiptSearchResults> ReceiptSearchResults = GetReceiptSearchResults();
                lv.setAdapter(new OutBaseAdapter(outActivity.this, ReceiptSearchResults));
            }

            autoCompleteTextView.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    ArrayList<ReceiptSearchResults> ReceiptSearchResults = GetReceiptSearchResults();
                    lv.setAdapter(new OutBaseAdapter(outActivity.this, ReceiptSearchResults));
                }
            });


            // check if GPS enabled
            if (gps.canGetLocation()) {

                mLastLocation = LocationServices.FusedLocationApi
                        .getLastLocation(MainActivity.mGoogleApiClient);

                currLatitude = gps.getLatitude();
                currLongitude = gps.getLongitude();
                if (currLatitude != 0.0 && currLongitude != 0.0) {


                    dba.open();
                    String sql = "select " + CustomerTable.KEY_NAME + "," + CustomerTable.KEY_LONGI + "," + CustomerTable.KEY_LATI + " from "
                            + CustomerTable.DATABASE_TABLE;
                    Cursor custCur = DataBaseAdapter.ourDatabase.rawQuery(sql, null);

                    Log.w("CA", "SQL :" + sql);

                    if (custCur.getCount() > 0) {
                        //	custCur.moveToFirst();

                        int LocFlg = 0;

                        while (custCur.moveToNext()) {

                            Log.w("CA", "Name :" + custCur.getString(0).trim());
                            Log.w("CA", "Lati :" + custCur.getString(2));
                            Log.w("CA", "Longi :" + custCur.getString(1));

                            Log.w("CA", "currLati :" + currLatitude);
                            Log.w("CA", "currLongi :" + currLongitude);

                            if (CalculateDistance.distance(custCur.getDouble(2), custCur.getDouble(1), currLatitude, currLongitude) < Double.parseDouble(session.getDistance())) {
                                //do what you want to do...
                                //	txtCustomer.setText(custCur.getString(0).trim());
                                for (int i = 0; i < spinCustomer.getAdapter()
                                        .getCount(); i++) {
                                    if (spinCustomer.getAdapter().getItem(i)
                                            .toString().contains(custCur.getString(0).trim())) {
                                        //spinCustomer.setSelection(i);
                                        //ArrayList<ReceiptSearchResults> ReceiptSearchResults = GetReceiptSearchResults();
                                        //lv.setAdapter(new OutBaseAdapter(outActivity.this, ReceiptSearchResults));
                                        //LocFlg = 1;
                                        break;
                                    }
                                }
                            }
                        }

                        if(LocFlg == 0){
                            Toast.makeText(outActivity.this, "Unable to connect to GPS Please Select Customer Manually", Toast.LENGTH_SHORT).show();
                        }

                    }
                    custCur.close();
                    dba.close();
                } else {
                    toastService.startTimer();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        spinCustomer.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.w("CustName1", "SpinPosition :" + position);
                String custName = spinCustomer.getSelectedItem().toString().trim();

                    ArrayList<ReceiptSearchResults> ReceiptSearchResults = GetReceiptSearchResults();
                    lv.setAdapter(new OutBaseAdapter(outActivity.this, ReceiptSearchResults));



            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        lv.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> a, View v, int position,
                                    long id) {
              /*  Object o = lv.getItemAtPosition(position);
                ReceiptSearchResults fullObject = (ReceiptSearchResults) o;
                Toast.makeText(ReceiptActivity.this,
                        "You have chosen: " + " " + fullObject.getBillid(),
                        Toast.LENGTH_LONG).show();
*/
               /* Intent intent = new Intent(getApplicationContext(),
                        PaymentActivity.class);

                intent.putExtra("custid", custid);
                intent.putExtra("custName",spinCustomer.getSelectedItem().toString().trim());
                startActivity(intent);
                finish();*/
            }
        });

       // new AsyncCallWS().execute();
    }


    private ArrayList<ReceiptSearchResults> GetReceiptSearchResults() {


        ArrayList<ReceiptSearchResults> results = new ArrayList<ReceiptSearchResults>();

        if (spinCustomer.getSelectedItem().toString().equalsIgnoreCase("Select Customer"))
        {

            ReceiptSearchResults sr = new ReceiptSearchResults();


            try {



                dba.open();
                    String sql = "select * from receipts order by custname";
                    Cursor cur = DataBaseAdapter.ourDatabase.rawQuery(sql, null);
                    Log.w("CA", "SQL1 :" + sql);
                    if (cur.getCount() > 0) {
                        sum = 0;
                        while (cur.moveToNext()) {
                            sr = new ReceiptSearchResults();
                            sr.setBillid(cur.getString(cur.getColumnIndex("OutBkey")).trim());
                            sr.setDate(cur.getString(0).trim());
                            sr.setTotalamt(String.valueOf(Math.round(cur.getDouble(4) * 100.0) / 100.0));
                            sr.setOutstandingamt(String.valueOf(Math.round(cur.getDouble(5) * 100.0) / 100.0));
                            sr.setKEY_custNo(cur.getString(1));
                            sr.setKEY_dbkey(cur.getString(8));
                            sr.setKEY_dbsofar(cur.getString(9));
                            sr.setKEY_dbprfx(cur.getString(cur.getColumnIndex("prfx")));
                            sr.setCustName(cur.getString(cur.getColumnIndex("custname")));
                            sr.setPartPayment(cur.getString(cur.getColumnIndex("partpaymen")));
                            sr.setNotApproval(cur.getString(cur.getColumnIndex("Not_Aproval")));
                            sr.setDays(cur.getString(cur.getColumnIndex("days")));
                            sr.setSoShortName(cur.getString(cur.getColumnIndex("SoShort")));
                            sr.setNetDue(cur.getString(cur.getColumnIndex("outPending")));
                            sum = sum + Double.parseDouble(cur.getString(cur.getColumnIndex("outPending")).trim());

                            results.add(sr);
                            Log.e("CA", "SQL1 :" + sr.toString());

                        }

                        sum = Math.round(sum * 100.0) / 100.0;
                        mTextView.setText(String.valueOf(sum));

                    } else {

                        mTextView.setText("");

                        lv.setAdapter(null);
                        Toast.makeText(outActivity.this,
                                "No Outstanding Payment", Toast.LENGTH_LONG).show();

                }
                dba.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else {

            ReceiptSearchResults sr = new ReceiptSearchResults();
            String custName = spinCustomer.getSelectedItem().toString().trim();

            try {


                dba.open();
                String sql1 = "select id from customer where name='" + custName + "'";
                Cursor cur1 = DataBaseAdapter.ourDatabase.rawQuery(sql1, null);
                if (cur1.getCount() > 0) {
                    cur1.moveToFirst();
                    custid = cur1.getString(0).trim();

                    String sql = "select * from receipts where custid='" + custid + "'";
                    Cursor cur = DataBaseAdapter.ourDatabase.rawQuery(sql, null);
                    Log.w("CA", "SQL1 :" + sql);
                    if (cur.getCount() > 0) {
                        sum = 0;
                        while (cur.moveToNext()) {
                            sr = new ReceiptSearchResults();
                            sr.setBillid(cur.getString(cur.getColumnIndex("OutBkey")).trim());
                            sr.setDate(cur.getString(0).trim());
                            sr.setTotalamt(String.valueOf(Math.round(cur.getDouble(4) * 100.0) / 100.0));
                            sr.setOutstandingamt(String.valueOf(Math.round(cur.getDouble(5) * 100.0) / 100.0));
                            sr.setKEY_custNo(cur.getString(1));
                            sr.setKEY_dbkey(cur.getString(8));
                            sr.setKEY_dbsofar(cur.getString(9));
                            sr.setKEY_dbprfx(cur.getString(cur.getColumnIndex("prfx")));
                            sr.setCustName(cur.getString(cur.getColumnIndex("custname")));
                            sr.setPartPayment(cur.getString(cur.getColumnIndex("partpaymen")));
                            sr.setNotApproval(cur.getString(cur.getColumnIndex("Not_Aproval")));
                            sr.setDays(cur.getString(cur.getColumnIndex("days")));
                            sr.setSoShortName(cur.getString(cur.getColumnIndex("SoShort")));
                            sr.setNetDue(cur.getString(cur.getColumnIndex("outPending")));
                            sum = sum + Double.parseDouble(cur.getString(cur.getColumnIndex("outPending")).trim());
                            results.add(sr);
                        }

                        sum = Math.round(sum * 100.0) / 100.0;
                        mTextView.setText(String.valueOf(sum));


                    } else {
                        mTextView.setText("");
                        lv.setAdapter(null);
                        Toast.makeText(outActivity.this,
                                "No Outstanding Payment", Toast.LENGTH_LONG).show();
                    }
                    cur.close();
                }
                dba.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

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
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        toastService.stopTimer();
    }

    public void setBal() {
        tvbal.setText("Rs."+remain);
        editPaidAmount.setEnabled(false);

    }


    private class AsyncCallWS extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            // Call Web Method
            try {

                Log.w("CartActivity", "Start");

                dba.open();
                serverResponse = WebService.getRecNo(session.getBranchNo(),session.getEmpNo(), "GetReceiptNo");
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
                        outActivity.this);
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
                                    .getString("RecNo").trim()
                                    .equalsIgnoreCase("No")) {

                                AlertDialog.Builder builder2 = new AlertDialog.Builder(
                                        outActivity.this);
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

                            } else if (!serverResponse.getJSONObject(0)
                                    .getString("RecNo").trim()
                                    .equalsIgnoreCase("No")) {



                                recNo=serverResponse.getJSONObject(0)
                                        .getString("RecNo").trim();



                                txtRcpNo.setText(recNo);




                            }

                        }

                        // Error status is true
                    } else {

                        Toast.makeText(outActivity.this, "Server Error",
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
