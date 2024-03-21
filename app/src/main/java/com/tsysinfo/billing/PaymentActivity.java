package com.tsysinfo.billing;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources.NotFoundException;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.location.Location;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
 
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.LocationServices;
import com.tsysinfo.billing.database.DataBaseAdapter;
import com.tsysinfo.billing.database.Models;
import com.tsysinfo.billing.database.OfflineTable;
import com.tsysinfo.billing.database.ReceiptTable;
import com.tsysinfo.billing.database.SessionManager;
import com.tsysinfo.billing.database.TempReciept;
import com.tsysinfo.billing.database.WebService;
import com.zj.btsdk.BluetoothService;
import com.zj.btsdk.PrintPic;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
public class PaymentActivity extends AppCompatActivity {
    //Print
    protected static final String TAG = "TAG";
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    static boolean errored = false;
    SessionManager session;
    DataBaseAdapter dba;
    Models mod;
    Spinner spinPayType;
    EditText editChqNo, editBank, editPaidAmount;
    TextView textChqDate, txtBillNo, txtBillDate, txtTotalAmount,
            txtBalanceAmount;
    Button btnPay, btnPrint, btnDone;
    String Data = "", DData = "", ReceiptNo = "", CustName = "";
    int sum = 0;
    Float balAmount;
    ProgressDialog waitDialog;
    RadioGroup radioGroup;
    JSONArray serverResponse;
    String longi = "0.0", lati = "0.0";
    byte FONT_TYPE;
    BluetoothService mService = null;
    BluetoothDevice con_dev = null;
    private String radioPayMod = "";
    EditText editTextNarration, editTextRemark, editDrawnOn;
    private String drawn;
    private String CustId = "";
    private double pendingAMT = 0;
    private double balanceAMT = 0;
    private String rcpBills = "";
    private String rcpValues = "";
    private String rcpPending="";
    private TableLayout tableLayoutCounterBilling;
    private TextView tv;
    TextView textViewName;
    Button[] delete = new Button[50];
    public  static  String recNo;
    private TextView txtRcpNo;
    private String remark="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.receipt_payment);
        textViewName=(TextView) findViewById(R.id.cstName);
        textViewName.setText(getIntent().getStringExtra("custName"));
        session = new SessionManager(this);
        dba = new DataBaseAdapter(this);
        mod = new Models();
        waitDialog = new ProgressDialog(this);
        waitDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        waitDialog.setMessage("Please Wait");
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        txtBillNo = (TextView) findViewById(R.id.billNo);
        txtBillDate = (TextView) findViewById(R.id.billDate);
        txtTotalAmount = (TextView) findViewById(R.id.totalAmount);
        txtRcpNo=(TextView)findViewById(R.id.rcpno);
        txtBalanceAmount = (TextView) findViewById(R.id.balanceAmount);
        editPaidAmount = (EditText) findViewById(R.id.editPaidAmount);
        spinPayType = (Spinner) findViewById(R.id.method);
        editBank = (EditText) findViewById(R.id.textBankName);
        editChqNo = (EditText) findViewById(R.id.textChequeNo);
        textChqDate = (TextView) findViewById(R.id.textChequeDate);
        radioGroup = (RadioGroup) findViewById(R.id.radios);
        editTextNarration = (EditText) findViewById(R.id.editNaration);
        editTextRemark = (EditText) findViewById(R.id.editRemark);
        editDrawnOn = (EditText) findViewById(R.id.editDrawnOn);
        btnPay = (Button) findViewById(R.id.buttonPay);
        btnPrint = (Button) findViewById(R.id.buttonPrint);
        btnDone = (Button) findViewById(R.id.buttonDone);


        Intent intent=getIntent();

        String paytype=  intent.getStringExtra("payttype");
        String editPaid= intent.getStringExtra("editPaid");
        String chqNo= intent.getStringExtra("chqNo");
        String chqdate= intent.getStringExtra("chqdate");
        remark=intent.getStringExtra("remark");
        String drawnOn= intent.getStringExtra("drawnOn");

        editPaidAmount.setText(editPaid);

        if (paytype.equalsIgnoreCase("Cash"))
        {
            spinPayType.setSelection(1);


        }else {

            spinPayType.setSelection(0);
            editChqNo.setText(chqNo);
            editDrawnOn.setText(chqdate);
            textChqDate.setText(drawnOn);

        }




        tableLayoutCounterBilling = (TableLayout) findViewById(R.id.counterBillingTableLayout);
        /*List<String> categories = new ArrayList<String>();
        categories.add("[ Payment Mode ]");
        categories.add("Cash");
        categories.add("Cheque");
        categories.add("DD");
        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, categories);
        dataAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinPayType.setAdapter(dataAdapter);*/
        txtRcpNo.setText(getIntent().getStringExtra("recNo"));
        textChqDate.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Calendar mcurrentDate = Calendar.getInstance();
                int mYear = mcurrentDate.get(Calendar.YEAR);
                final int mMonth = mcurrentDate.get(Calendar.MONTH);
                int mDay = mcurrentDate.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog mDatePicker = new DatePickerDialog(
                        PaymentActivity.this, new OnDateSetListener() {
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
        editBank.setVisibility(View.GONE);
        editChqNo.setVisibility(View.GONE);
        editDrawnOn.setVisibility(View.GONE);
        textChqDate.setVisibility(View.GONE);
        btnPrint.setVisibility(View.GONE);
        btnDone.setVisibility(View.GONE);
        CustName = getIntent().getStringExtra("custName");
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(
                "MM/dd/yyyy");
        String strDate = sdf.format(c.getTime());
        dba.open();
        String sql = "select * from " + TempReciept.DATABASE_TABLE + "";
        rcpBills = "";
        rcpValues = "";
        double totalPaymentAmt = 0;
        Cursor cursor = DataBaseAdapter.ourDatabase.rawQuery(sql, null);
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                CustId = cursor.getString(cursor.getColumnIndex("custNo"));
                pendingAMT = pendingAMT + Double.parseDouble(cursor.getString(cursor.getColumnIndex("dbpending")));
                balanceAMT = balanceAMT + Double.parseDouble(cursor.getString(cursor.getColumnIndex("dbamount")));
                if (DData.equalsIgnoreCase("")) {
                    DData =  cursor.getString(cursor.getColumnIndex("dbno")) + "#"  + cursor.getString(cursor.getColumnIndex("dbpending")) + "#" + cursor.getString(cursor.getColumnIndex("dbpay"));
                } else {

                    DData = DData + "$" +cursor.getString(cursor.getColumnIndex("dbno")) + "#"  + cursor.getString(cursor.getColumnIndex("dbpending")) + "#" + cursor.getString(cursor.getColumnIndex("dbpay"));

                }
              /*  DData = strDate + "," + CustId + "," + cursor.getString(cursor.getColumnIndex("dbkey")) + "," + cursor.getString(cursor.getColumnIndex("dbprfx")) + "," + cursor.getString(cursor.getColumnIndex("dbno")) + "," + cursor.getString(cursor.getColumnIndex("dbdate")) + "," + cursor.getString(cursor.getColumnIndex("dbamount")) + "" + cursor.getString(cursor.getColumnIndex("dbsofar")) + "," + cursor.getString(cursor.getColumnIndex("dbpending")) + "," + cursor.getString(cursor.getColumnIndex("dbpay"));*/
                totalPaymentAmt = totalPaymentAmt + Double.parseDouble(cursor.getString(cursor.getColumnIndex("dbpay")));
                Log.w("Cust iD", CustId);
                Log.w("DDATA", DData);
                Log.w("Toatal PayAmt", String.valueOf(totalPaymentAmt));
                if (rcpBills.equalsIgnoreCase("")) {
                    rcpBills = cursor.getString(cursor.getColumnIndex("dbno"));
                    rcpValues = cursor.getString(cursor.getColumnIndex("dbpay"));
                    rcpPending= cursor.getString(cursor.getColumnIndex("dbpending"));
                } else {
                    rcpBills = rcpBills + "#" + cursor.getString(cursor.getColumnIndex("dbno"));
                    rcpValues = rcpValues + "#" + cursor.getString(cursor.getColumnIndex("dbpay"));
                    rcpPending=rcpPending + "#" + cursor.getString(cursor.getColumnIndex("dbpending"));
                }
                Log.w("rcpbils", rcpBills);
                Log.w("rcpvalues", rcpValues);
            }
        }
        dba.close();
        txtBillNo.setText(rcpBills);
        txtBillDate.setText(strDate);
        txtTotalAmount.setText(String.valueOf(pendingAMT));
        txtBalanceAmount.setText(String.valueOf(balanceAMT));
        editPaidAmount.setText(String.valueOf(totalPaymentAmt));
        editPaidAmount.setEnabled(false);


        showtable();
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // find which radio button is selected
                if (checkedId == R.id.cash) {
                    radioPayMod = "Cash";
                    editChqNo.setVisibility(View.GONE);
                    textChqDate.setVisibility(View.GONE);
                    editDrawnOn.setVisibility(View.GONE);
                } else if (checkedId == R.id.cheque) {
                    radioPayMod = "Cheque";
                    editChqNo.setVisibility(View.VISIBLE);
                    textChqDate.setVisibility(View.VISIBLE);
                    editDrawnOn.setVisibility(View.VISIBLE);
                } else {
                    radioPayMod = "NEFT";
                    editBank.setVisibility(View.VISIBLE);
                    editChqNo.setVisibility(View.VISIBLE);
                    editChqNo.setHint("Enter NEFT No");
                    textChqDate.setVisibility(View.VISIBLE);
                    textChqDate.setHint("Enter Date");
                }
            }
        });
        spinPayType.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                // TODO Auto-generated method stub
                String str = spinPayType.getSelectedItem().toString().trim();
                if (str .equalsIgnoreCase("Cash")) {

                    radioPayMod = "Cash";
                    editChqNo.setVisibility(View.GONE);
                    textChqDate.setVisibility(View.GONE);
                    editDrawnOn.setVisibility(View.GONE);

                } else if (str .equalsIgnoreCase("Cheque")) {
                    radioPayMod = "Cheque";
                    editChqNo.setVisibility(View.VISIBLE);
                    textChqDate.setVisibility(View.VISIBLE);
                    editDrawnOn.setVisibility(View.VISIBLE);
                } else {


                    radioPayMod = "DD";
                    editChqNo.setVisibility(View.VISIBLE);
                    textChqDate.setVisibility(View.VISIBLE);
                    editDrawnOn.setVisibility(View.VISIBLE);
                    /*radioPayMod = "NEFT";
                    editBank.setVisibility(View.VISIBLE);
                    editChqNo.setVisibility(View.VISIBLE);
                    editChqNo.setHint("Enter NEFT No");
                    textChqDate.setVisibility(View.VISIBLE);
                    textChqDate.setHint("Enter Date");*/
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
            }
        });
        final String finalRcpValues = rcpValues;
        final String finalRcpBills = rcpBills;
        final double finalTotalPaymentAmt = totalPaymentAmt;
        btnPay.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                try {
                    String payMode = spinPayType.getSelectedItem().toString();
                    String bnkName, chqNo, chqDate;
                    String paidAmount = editPaidAmount.getText().toString().trim();
                    if (payMode.equalsIgnoreCase("[ Payment Mode ]")) {
                        Toast.makeText(getApplicationContext(), "Select Pay Mode",
                                Toast.LENGTH_LONG).show();
                    } else if (paidAmount.length() == 0) {
                        Toast.makeText(getApplicationContext(),
                                "Enter Amount to be Paid", Toast.LENGTH_LONG)
                                .show();
                    } else {
                        float pA = Float.parseFloat(paidAmount);
                        float bA = Float.parseFloat(txtBalanceAmount.getText()
                                .toString().trim());
                        if (pA > bA) {
                            Toast.makeText(
                                    getApplicationContext(),
                                    "Paid Amount should smaller than or equal to balance amount",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            balAmount = bA - pA;
                            int flag = 0;
                            if (payMode.equalsIgnoreCase("Cash")) {
                                // bnkName = "N/A";
                                chqDate = "N/A";
                                chqNo = "0";
                                drawn = "N/A";
                                flag = 1;
                            } else {
                                //bnkName = editBank.getText().toString().trim();
                                chqDate = textChqDate.getText().toString().trim();
                                chqNo = editChqNo.getText().toString().trim();
                                drawn = editDrawnOn.getText().toString();
                               /* if (bnkName.length() > 0) {*/
                                if (chqDate.length() > 0) {
                                    if (chqNo.length() > 0) {
                                        flag = 1;
                                    } else {
                                        if (payMode.equalsIgnoreCase("Cheque")) {
                                            Toast.makeText(
                                                    getApplicationContext(),
                                                    "Enter Cheque No",
                                                    Toast.LENGTH_LONG).show();
                                        } else if (payMode
                                                .equalsIgnoreCase("Cheque")) {
                                            Toast.makeText(
                                                    getApplicationContext(),
                                                    "Enter NEFT No",
                                                    Toast.LENGTH_LONG).show();
                                        }
                                    }
                                } else {
                                    Toast.makeText(getApplicationContext(),
                                            "Select Date", Toast.LENGTH_LONG)
                                            .show();
                                }

                                /*} else {

                                    Toast.makeText(getApplicationContext(),
                                            "Enter Bank Name", Toast.LENGTH_LONG)
                                            .show();
                                }*/
                            }
                            if (flag == 1) {
                                String Remark = editTextRemark.getText().toString();
                                String Narration = editTextNarration.getText().toString();
                                Calendar c = Calendar.getInstance();
                                SimpleDateFormat sdf = new SimpleDateFormat(
                                        "dd/MM/yyyy");
                                String strDate = sdf.format(c.getTime());
                                String balAmt = txtBalanceAmount.getText()
                                        .toString().trim();
                                String paidAmt = editPaidAmount.getText()
                                        .toString().trim();
                                String Mdata = session.getEmpNo() + "$"+getIntent().getStringExtra("custid") + "$"+ strDate + "$" + payMode + "$" + chqNo + "$" + chqDate + "$" + drawn + "$" + finalTotalPaymentAmt + "$"+ lati + "$" + longi + "$"+session.getEmpNo()+ "$"+remark;
                              //  String data = strDate + "," + getIntent().getStringExtra("custid") + "," + session.getEmpNo() + "," + chqNo + "," + chqDate + "," + drawn + "," + finalTotalPaymentAmt + "," + Narration + "," + finalRcpBills + "," + finalRcpValues + "," + Remark + "," + payMode;
                                Data = Mdata;
                              /*  dba.open();
                                String Sql="select * from "+ ReceiptTable.DATABASE_TABLE+" where "+ReceiptTable.KEY_CUSTID+"='"+getIntent().getStringExtra("custid")+"' ";
                                Cursor cursor=DataBaseAdapter.ourDatabase.rawQuery(Sql,null);
                                if (cur.getCount()>0)
                                {
                                    cursor.moveToFirst();



                                    DData =strDate+","+ getIntent().getStringExtra("custid") +","+cursor.getString(cursor.getColumnIndex("OutBkey"))+","+""+","+billid+","+cursor.getString(cursor.getColumnIndex("date"))+","+balAmt+","+cursor.getString(cursor.getColumnIndex("outrcvSofar"))+","+cursor.getString(cursor.getColumnIndex("outPending"))+","+paidAmt;
                                   // DData=cursor.getString(cursor.getColumnIndex("OutBkey"))+","+""*//*prfx blank*//*+","+billid+","+cursor.getString(cursor.getColumnIndex("date"))+","+balAmt+","+cursor.getString(cursor.getColumnIndex("outrcvSofar"))+","+cursor.getString(cursor.getColumnIndex("outPending"))+","+paidAmt;

                                }*/
                                Log.w("MDATA", Data);
                                String logFlag = "";
                                GPSTracker gps = new GPSTracker(PaymentActivity.this);
                                if (gps.canGetLocation()) {
                                    if (gps.getLongitude() != 0.0 && gps.getLatitude() != 0.0) {
                                        longi = String.valueOf(gps.getLongitude());
                                        lati = String.valueOf(gps.getLatitude());
                                        logFlag = "LocationManager";
                                    } else {
                                        Location mLastLocation = LocationServices.FusedLocationApi
                                                .getLastLocation(MainActivity.mGoogleApiClient);
                                        longi = String.valueOf(mLastLocation.getLongitude());
                                        lati = String.valueOf(mLastLocation.getLatitude());
                                        logFlag = "GoogleApi";
                                    }
                                    Log.w("LocationFlag", "" + logFlag);
                                    ConnectivityManager cm = ((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE));
                                    if (cm == null) {
                                        return;
                                    }
                                    // Now to check if we're actually connected
                                    if (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected()) {
                                        // Start the service to do our thing
                                        AsyncCallWS task = new AsyncCallWS();
                                        // Call execute
                                        task.execute();
                                        Log.w("ReceiptPayment", "Online");
                                    } else {
                                        Log.w("ReceiptPayment", "Offline");
                                        ContentValues cv = new ContentValues();
                                        cv.put(OfflineTable.KEY_DATA, Data + "$" + DData);
                                        cv.put(OfflineTable.KEY_BRANCHNO, session.getBranchNo());
                                        cv.put(OfflineTable.KEY_LONGITUDE, longi);
                                        cv.put(OfflineTable.KEY_LATITUDE, lati);
                                        cv.put(OfflineTable.KEY_METHOD, "SaveReceipt");
                                        dba.open();
                                        mod.insertdata(OfflineTable.DATABASE_TABLE, cv);
                                        Cursor curRNo = mod.getData(OfflineTable.DATABASE_TABLE);
                                        ReceiptNo = "OR-" + String.valueOf(curRNo.getCount());
                                        curRNo.close();
                                        dba.close();
                                        btnPay.setVisibility(View.GONE);
                                        btnPrint.setVisibility(View.VISIBLE);
                                        btnDone.setVisibility(View.VISIBLE);
                                        mService = new BluetoothService(PaymentActivity.this, mHandler);
                                        //�����������˳�����
                                        if (mService.isAvailable() == false) {
                                            Toast.makeText(PaymentActivity.this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
                                        }
                                        if (!mService.isBTopen()) {
                                            Intent enableBtIntent = new Intent(
                                                    BluetoothAdapter.ACTION_REQUEST_ENABLE);
                                            startActivityForResult(enableBtIntent,
                                                    REQUEST_ENABLE_BT);
                                        } else {
                                            Intent connectIntent = new Intent(PaymentActivity.this,
                                                    DeviceListActivity.class);
                                            startActivityForResult(connectIntent,
                                                    REQUEST_CONNECT_DEVICE);
                                        }
                                    }
                                } else {
                                    gps.showSettingsAlert();
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        btnPrint.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    ConnectivityManager cm = ((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE));
                    if (cm == null) {
                        return;
                    }
                    if (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected()) {
                        // Start the service to do our thing
                        Log.w("Receipt", "Online");
                        printImage();
                    } else {
                        Log.w("Receipt", "Offline");
                        printBill();
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });
        btnDone.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(
                        getApplicationContext(),
                        MainActivity.class);
                startActivity(intent);
                finish();
            }
        });



    }
    // WebService Code
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mService != null)
            mService.stop();
        mService = null;
    }
    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        super.onBackPressed();
        Intent intent = new Intent(getApplicationContext(),
                ReceiptActivity.class);
        startActivity(intent);
        finish();
    }
    // Print
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:      //���������
                if (resultCode == Activity.RESULT_OK) {   //�����Ѿ���
                    Toast.makeText(this, "Bluetooth open successful", Toast.LENGTH_LONG).show();
                } else {                 //�û������������
                    finish();
                }
                break;
            case REQUEST_CONNECT_DEVICE:     //��������ĳһ�����豸
                if (resultCode == Activity.RESULT_OK) {   //�ѵ�������б��е�ĳ���豸��
                    String address = data.getExtras()
                            .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);  //��ȡ�б������豸��mac��ַ
                    con_dev = mService.getDevByMac(address);
                    mService.connect(con_dev);
                }
                break;
        }
    }
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BluetoothService.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:   //������
                            Toast.makeText(getApplicationContext(), "Connect successful",
                                    Toast.LENGTH_SHORT).show();
                            break;
                        case BluetoothService.STATE_CONNECTING:  //��������
                            Log.d("��������", "��������.....");
                            break;
                        case BluetoothService.STATE_LISTEN:     //�������ӵĵ���
                        case BluetoothService.STATE_NONE:
                            Log.d("��������", "�ȴ�����.....");
                            break;
                    }
                    break;
                case BluetoothService.MESSAGE_CONNECTION_LOST:    //�����ѶϿ�����
                    Toast.makeText(getApplicationContext(), "Device connection was lost",
                            Toast.LENGTH_SHORT).show();
                    break;
                case BluetoothService.MESSAGE_UNABLE_CONNECT:     //�޷������豸
                    Toast.makeText(getApplicationContext(), "Unable to connect device",
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
    public void printBill() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        final String strDate = sdf.format(c.getTime());
        Thread t = new Thread() {
            public void run() {
                try {
                    String BILL = "";
                    int prQty = 0;
                    double disTot = 0.0;
                    //    BILL = "\nIsteram eSolutions UAE\n";
                    BILL = session.getAddresse() + "\n";
                    BILL = BILL + "-------------------------------------------";
                    BILL = BILL + "\nInvoice No:  " + ReceiptNo + "    " + strDate + "\n";
                    BILL = BILL + "\nCust Name:   " + CustName;
                    BILL = BILL + "\nSalesman:    " + session.getEmpName();
                    BILL = BILL + "\nPay Mode:    " + radioPayMod + "\n";
                    if (radioPayMod.equalsIgnoreCase("Cheque")) {
                        BILL = BILL + "Cheque No:   " + editChqNo.getText().toString().trim();
                        BILL = BILL + "\nCheque Date: " + textChqDate.getText().toString().trim();
                    } else if (radioPayMod.equalsIgnoreCase("NEFT")) {
                        BILL = BILL + "NEFT No:     " + editChqNo.getText().toString().trim();
                        BILL = BILL + "\nNEFT Date:   " + textChqDate.getText().toString().trim();
                    }
                    BILL = BILL + "\n-------------------------------------------";
                    BILL = BILL + "\nBill No:          " + rcpBills;
                    BILL = BILL + "\nBill Date:        " + txtBillDate.getText().toString().trim();
                    BILL = BILL + "\nBill Amount:      " + txtTotalAmount.getText().toString().trim();
                    BILL = BILL + "\nReceived Amount:  " + editPaidAmount.getText().toString().trim();
                    BILL = BILL + "\nPending Amount:   " + balAmount;
                    BILL = BILL + "-------------------------------------------\n";
                    BILL = BILL + "Customer Sign                 Salesman Sign\n\n\n";
                    BILL = BILL + "             ****Thank You****             \n";
                    BILL = BILL + "       Powered By: www.istreame.com        \n";
                    //    os.write(BILL.getBytes());
                    //This is printer specific code you can comment ==== > Start
                    PrintText.appendLog(BILL);
                    foo(BILL);
                    mService.sendMessage(BILL, "UTF_8");


              /*      String extStorageDirectory = Environment
                            .getExternalStorageDirectory().toString() + "/ebilling_images/";

                    InputStream is = openFileInput(extStorageDirectory+"print/print.txt");
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    byte[] b = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = is.read(b)) != -1) {
                        bos.write(b, 0, bytesRead);
                    }
                    byte[] bytes = bos.toByteArray();

                    byte[] printformat = { 27, 33, 0 }; //try adding this print format

                    os.write(printformat);
                    os.write(bytes);*/



         /*           String braNm = " ";
                    if(session.getBranchNo().length() == 1 ){
                        braNm = "0"+session.getBranchNo();
                    }else{
                        braNm = session.getBranchNo();
                    }

                    Calendar c = Calendar.getInstance();
                    SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy");
                    final String strDate = sdf.format(c.getTime());

                    String barCodeVal = "BIL"+braNm+billNo+strDate+"\n\n";// "HELLO12345678912345012";
                    System.out.println("Barcode Length : "
                            + barCodeVal.length());
                    int n1 = barCodeVal.length();
                    os.write(intToByteArray(n1));

                    for (int i = 0; i < barCodeVal.length(); i++) {
                        os.write((barCodeVal.charAt(i) + "").getBytes());
                    }
*/
                    /*String  thnkU = "             ****Thank You****             \n";
                            thnkU = thnkU + "             www.istreame.com              \n";
                    os.write(thnkU.getBytes());*/
                    //printer specific code you can comment ==== > End
                } catch (Exception e) {
                    Log.e("Main", "Exe ", e);
                }
            }
        };
        t.start();
    }
    void foo(final String text) throws IOException {
        String extStorageDirectory = Environment
                .getExternalStorageDirectory().toString() + "/ebilling_images/print/print.png";
        final Paint textPaint = new Paint() {
            {
                setColor(Color.WHITE);
                setTextAlign(Paint.Align.LEFT);
                setTextSize(20f);
                setAntiAlias(true);
            }
        };
        final Rect bounds = new Rect();
        textPaint.getTextBounds(text, 0, text.length(), bounds);
        final Bitmap bmp = Bitmap.createBitmap(bounds.width(), bounds.height(), Bitmap.Config.RGB_565); //use ARGB_8888 for better quality
        final Canvas canvas = new Canvas(bmp);
        canvas.drawText(text, 0, 20f, textPaint);
        FileOutputStream stream = new FileOutputStream(extStorageDirectory); //create your FileOutputStream here
        bmp.compress(Bitmap.CompressFormat.PNG, 85, stream);
        bmp.recycle();
        stream.close();
    }
    private void printImage() throws FileNotFoundException {

      /*  String extStorageDirectory = Environment
                .getExternalStorageDirectory().toString() + "/ebilling_images/print/print.jpg";*/
        String extStorageDirectory = Environment
                .getExternalStorageDirectory().toString() + "/ebilling_images/receipt/" + ReceiptNo + "1.png";
        Log.w("PA", "" + extStorageDirectory);
        FileInputStream streamIn = new FileInputStream(extStorageDirectory);
        Bitmap bitmap = BitmapFactory.decodeStream(streamIn);
        byte[] sendData = null;
        PrintPic pg = new PrintPic();
        pg.initCanvas(bitmap.getWidth());
        pg.initPaint();
        pg.drawImage(0, 0, extStorageDirectory);
        sendData = pg.printDraw();
        mService.write(sendData);   //��ӡbyte�����
        mService.stop();
    }


   public void showtable()
    {

        Cursor curItemSale = null;
        try {

            Log.w("TryBlock", "Start");

            dba.open();

            String sql = "select * from " + TempReciept.DATABASE_TABLE + "";
            curItemSale = DataBaseAdapter.ourDatabase.rawQuery(sql, null);

            Log.w("TryBlock", "GetData" + curItemSale);

            tableLayoutCounterBilling
                    .removeAllViewsInLayout();

            // Table Header Start

            tableLayoutCounterBilling
                    .setStretchAllColumns(true);
            TableRow tr = new TableRow(
                    PaymentActivity.this);
            tr.setBackgroundResource(R.drawable.rowbackimage);
            final TableRow.LayoutParams par = new TableRow.LayoutParams(
                    android.view.ViewGroup.LayoutParams.FILL_PARENT,
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT);
            par.setMargins(0, 0, 0, 0);
            final String[] columns = { "Bill No",
                    "Bill Date", "Bill Amount",
                    "Received so far", "Balance Due",
                    "Allocated"};
            Log.w("Tables Headers", "" + columns);

            for (int i = 0; i < 6; i++) {

                tv = new TextView(
                        PaymentActivity.this);

                tv.setText(columns[i]);

                tv.setGravity(Gravity.CENTER);
                tv.setTextColor(Color.WHITE);
                tv.setTextSize(12);
                tv.setPadding(5, 0, 5, 0);
                tv.setHeight(20);
                tv.setTextColor(getResources().getColor(R.color.HeaderTxt));
                tv.setBackgroundResource(R.drawable.cell_header);
                tv.setLayoutParams(new TableRow.LayoutParams(
                        android.view.ViewGroup.LayoutParams.FILL_PARENT,
                        android.view.ViewGroup.LayoutParams.FILL_PARENT));
                tv.setGravity(Gravity.CENTER);
                tr.addView(tv, par);

            }
            tableLayoutCounterBilling.addView(tr);

            // Table Header End
            int color = 0;
            int p = 1;

            while (curItemSale.moveToNext()) {

                TableRow sRow = new TableRow(
                        PaymentActivity.this);
                sRow.setMinimumHeight(40);

                sRow.setId(p);

                Log.w("TryBlock", "While Block");

                for (int i = 2; i < 8; i++) {

                    Log.w("TryBlock", "For Block");




                        final TextView repsValue = new TextView(
                                PaymentActivity.this);
                        sRow.addView(repsValue);

                        repsValue
                                .setGravity(Gravity.CENTER);

                        if (i==2)
                        {

                            repsValue.setText(curItemSale.getString(curItemSale.getColumnIndex("dbprfx")) +" "+curItemSale.getString(curItemSale.getColumnIndex("dbno")).trim());

                        }else if (i==3)
                        {
                            repsValue.setText(curItemSale.getString(curItemSale.getColumnIndex("dbdate")).trim());
                        }else if (i==4)
                        {repsValue.setText(curItemSale.getString(curItemSale.getColumnIndex("dbamount")).trim());

                        }else if (i==5)
                        {
                            repsValue.setText(curItemSale.getString(curItemSale.getColumnIndex("dbpending")).trim());
                        }else if (i==6)
                        {
                            repsValue.setText(curItemSale.getString(curItemSale.getColumnIndex("dbpending")).trim());
                        }else if (i==7)
                        {
                            repsValue.setText(curItemSale.getString(curItemSale.getColumnIndex("dbpay")).trim());

                        }


                        repsValue
                                .setTextColor(Color.BLACK);
                        repsValue.setTextSize(10);
                        repsValue.setPadding(10, 0, 10,
                                0);
                        repsValue
                                .setLayoutParams(new TableRow.LayoutParams(
                                        android.view.ViewGroup.LayoutParams.FILL_PARENT,
                                        android.view.ViewGroup.LayoutParams.FILL_PARENT));
                        repsValue
                                .setBackgroundResource(R.drawable.cell_normal);



                }

                if (color++ % 2 == 0) {
                    sRow.setBackgroundColor(getResources()
                            .getColor(
                                    R.color.gridAlterow));
                } else {
                    sRow.setBackgroundColor(getResources()
                            .getColor(R.color.gridRow));
                    // scheduleRow.getBackground().setAlpha(150);
                }

                tableLayoutCounterBilling.addView(sRow);

            }

            curItemSale.close();
            dba.close();
        } catch (Exception e) {
            e.printStackTrace();
            Log.w("QA TC", "chk db" + e);
            curItemSale.close();
            dba.close();
        }

    }


    private void cropImage() {
        Paint paint = new Paint();
        String extStorageDirectory = Environment
                .getExternalStorageDirectory().toString() + "/ebilling_images/receipt/" + ReceiptNo + "1.png";
        FileInputStream streamIn = null;
        try {
            streamIn = new FileInputStream(extStorageDirectory);
            Bitmap bitmapOrg = BitmapFactory.decodeStream(streamIn);
            final Canvas canvas = new Canvas();
            canvas.drawColor(Color.WHITE);
            // you need to insert a image flower_blue into res/drawable folder
            paint.setFilterBitmap(true);
            Log.w("PA", "Width: " + bitmapOrg.getWidth());
            Log.w("PA", "Height: " + bitmapOrg.getHeight());
            Bitmap croppedBmp = Bitmap.createBitmap(bitmapOrg, 0, 100, bitmapOrg.getWidth(), bitmapOrg.getHeight() - 100);
            Log.w("PA", "Width: " + croppedBmp.getWidth());
            Log.w("PA", "Height: " + croppedBmp.getHeight());
            FileOutputStream stream = new FileOutputStream(extStorageDirectory); //create your FileOutputStream here
            croppedBmp.compress(Bitmap.CompressFormat.PNG, 85, stream);
            croppedBmp.recycle();
            stream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }catch (Exception e)
        {
            e.printStackTrace();
            Toast.makeText(PaymentActivity.this, "No receipt found.", Toast.LENGTH_SHORT).show();

        }
    }
    private class AsyncCallWS extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            // Call Web Method
            try {
                Log.w("CartActivity", "Start");
                dba.open();
                serverResponse = WebService.makeTransactionReciept(Data, DData, session.getBranchNo(), longi, lati, "SaveReceipt");
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
            Log.w("CartActivity", "ResponseString : " + WebService.responseString);
            // Make Progress Bar invisible
            waitDialog.cancel();
            Log.w("CartActivity", "Dialog Closed");
            if (WebService.timeoutFlag == 1) {
                Log.w("CartActivity", "Timeout");
                AlertDialog.Builder builder = new AlertDialog.Builder(
                        PaymentActivity.this);
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
                                    .getString("Status").trim()
                                    .equalsIgnoreCase("Failure")) {
                                AlertDialog.Builder builder2 = new AlertDialog.Builder(
                                        PaymentActivity.this);
                                builder2.setTitle("Failure..");
                                builder2.setMessage("Failed to make Payment.");
                                builder2.setPositiveButton("OK",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(
                                                    DialogInterface dialog,
                                                    int which) {
                                                Log.e("info", "OK");
                                            }
                                        });
                                builder2.show();
                            } else if (serverResponse.getJSONObject(0)
                                    .getString("Status").trim()
                                    .equalsIgnoreCase("Success")) {
                                dba.open();


                               // Toast.makeText(PaymentActivity.this, "Saved Success", Toast.LENGTH_SHORT).show();

                               // Intent intent= new Intent(PaymentActivity.this,MainActivity.class);
                               // startActivity(intent);
                              //  finish();



                                String[] strARrcpVal=rcpValues.split("#");
                                String[] strARrcppending=rcpPending.split("#");
                                String[] strARrcpBills=rcpBills.split("#");

                                for (int i=0;i<strARrcpBills.length;i++)
                                {

                                   double bal=Double.parseDouble(strARrcppending[i])-Double.parseDouble(strARrcpVal[i]);
                                   String sql="update "+ ReceiptTable.DATABASE_TABLE+" set "+ReceiptTable.KEY_OUTSTANDINGAMT+"='"+String.valueOf(bal)+"' where billid='"+strARrcpBills[i]+"'";
                                    DataBaseAdapter.ourDatabase.execSQL(sql);




                                }
                                dba.close();
                                Toast.makeText(PaymentActivity.this,
                                        "Payment Successfull!!!",
                                        Toast.LENGTH_LONG);
                                ReceiptNo = serverResponse.getJSONObject(0)
                                        .getString("ReceiptNo").trim();
                                btnPay.setVisibility(View.GONE);
                                btnPrint.setVisibility(View.VISIBLE);
                                btnDone.setVisibility(View.VISIBLE);
                                dba.open();
                                Cursor ip = mod.getData("iptable");
                                ip.moveToFirst();
                                String ipadd = ip.getString(0).trim();
                                String port = ip.getString(2).trim();
                                String PreFix = "http://" + ipadd + ":" + port;
                                ip.close();
                                dba.close();
                                String url = PreFix + "/Receipt/" + ReceiptNo + "1.png";
                                new DownloadFile().execute(url, ReceiptNo + "1.png");
                            }
                        }
                        // Error status is true
                    } else {
                        Toast.makeText(PaymentActivity.this, "Server Error",
                                Toast.LENGTH_LONG).show();
                        // statusTV.setText("Error occured in invoking webservice");
                    }
                    // Re-initialize Error Status to False
                    errored = false;
                } catch (SQLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (NotFoundException e) {
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
    private class DownloadFile extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... strings) {
            String fileUrl = strings[0]; // ->
            // http://maven.apache.org/maven-1.x/maven.pdf
            String fileName = strings[1]; // -> maven.pdf
            String extStorageDirectory = Environment
                    .getExternalStorageDirectory().toString() + "/ebilling_images/";
            File folder = new File(extStorageDirectory, "receipt");
            folder.mkdir();
            String[] allFiles;
            if (folder.isDirectory()) {
                allFiles = folder.list();
                for (int i = 0; i < allFiles.length; i++) {
                    File myFile = new File(folder, allFiles[i]);
                    myFile.delete();
                }
            }
            File pdfFile = new File(folder, fileName);
            try {
                pdfFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            FileDownloader.downloadFile(fileUrl, pdfFile);
            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            waitDialog.cancel();
            cropImage();
            ;
            waitDialog.cancel();
            mService = new BluetoothService(PaymentActivity.this, mHandler);
            //�����������˳�����
            if (mService.isAvailable() == false) {
                Toast.makeText(PaymentActivity.this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            }
            if (!mService.isBTopen()) {
                Intent enableBtIntent = new Intent(
                        BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent,
                        REQUEST_ENABLE_BT);
            } else {
                Intent connectIntent = new Intent(PaymentActivity.this,
                        DeviceListActivity.class);
                startActivityForResult(connectIntent,
                        REQUEST_CONNECT_DEVICE);
            }
        }
        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
            waitDialog.show();
        }
    }
}
