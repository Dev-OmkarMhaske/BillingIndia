package com.tsysinfo.billing;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
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
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.tsysinfo.billing.database.BillingTable;
import com.tsysinfo.billing.database.DataBaseAdapter;
import com.tsysinfo.billing.database.Models;
import com.tsysinfo.billing.database.OfflineTable;
import com.tsysinfo.billing.database.SalesTypeTable;
import com.tsysinfo.billing.database.SessionManager;
import com.tsysinfo.billing.database.WebService;
import com.zj.btsdk.BluetoothService;
import com.zj.btsdk.PrintPic;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static com.google.android.gms.location.LocationServices.FusedLocationApi;

public class BillPayment extends Activity {

    //Print
    protected static final String TAG = "TAG";
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    BluetoothService mService = null;
    BluetoothDevice con_dev = null;
    static boolean errored = false;

    SessionManager session;
    DataBaseAdapter dba;
    Models mod;
    Spinner spinPayType;
    TextView txtAmount;
    Button btnPay, btnPrint, btnDone;
    String Data = "", billNo, CustName = "";
    double sum = 0;
    int billStat = 0;
    ProgressDialog waitDialog;
    JSONArray serverResponse;
    RadioGroup radioGroup;
    RadioButton[] radio= new RadioButton[25];

    int k=0;
    String longi = "0.0", lati = "0.0";
    private String selectedbutton="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.billing);

        session = new SessionManager(this);
        dba = new DataBaseAdapter(this);
        mod = new Models();

        waitDialog = new ProgressDialog(this);
        waitDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        waitDialog.setMessage("Please Wait");

        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        spinPayType = (Spinner) findViewById(R.id.spinnerPayType);
        txtAmount = (TextView) findViewById(R.id.textAmount);
        btnPay = (Button) findViewById(R.id.buttonPay);
        btnPrint = (Button) findViewById(R.id.buttonPrint);
        btnDone = (Button) findViewById(R.id.buttonDone);
        radioGroup=(RadioGroup)findViewById(R.id.radios);

        btnPrint.setVisibility(View.GONE);
        btnDone.setVisibility(View.GONE);

        CustName = getIntent().getStringExtra("custName");

        List<String> categories = new ArrayList<String>();

        dba.open();
        Cursor curSalesType = mod.getData(SalesTypeTable.DATABASE_TABLE);
        if (curSalesType.getCount() > 0) {
            categories.add("[ Sales Type ]");
            while (curSalesType.moveToNext()) {
                categories.add(curSalesType.getString(1).trim());
                radio[k]= new RadioButton(this);
                radio[k].setText(curSalesType.getString(1).trim());
                radio[k].setGravity(Gravity.LEFT|Gravity.CENTER);
                Drawable drawableDivider = getResources().getDrawable(R.drawable.edittextstyle);
                radio[k].setBackground(drawableDivider);
                Drawable drawable = getResources().getDrawable(android.R.drawable.btn_radio);
                drawable.setBounds(0, 0, 57, 72);
                radio[k].setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                radio[k].setCompoundDrawables(null, null, drawable, null);
                radio[k].setButtonDrawable(null);

                radio[k].setId(k);
                radioGroup.addView(radio[k]);
                k++;
            }
        }
        curSalesType.close();
        dba.close();

        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, categories);

        dataAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinPayType.setAdapter(dataAdapter);

        dba.open();
        Cursor cur = mod.getData(BillingTable.DATABASE_TABLE);
        if (cur.getCount() > 0) {
            while (cur.moveToNext()) {

                sum = sum + Double.parseDouble(cur.getString(9).trim());

            }
        }
        cur.close();
        dba.close();
        sum = Math.round(sum * 100.0) / 100.0;
        txtAmount.setText("Amount: " + String.valueOf(sum));

        btnPay.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                try {
                    int selectedId = radioGroup.getCheckedRadioButtonId();
                    selectedbutton=radio[selectedId].getText().toString();
                    String saleType = radio[selectedId].getText().toString();
                    if (saleType.equalsIgnoreCase(null)) {
                        Toast.makeText(getApplicationContext(),
                                "Select Sales Type", Toast.LENGTH_LONG).show();
                    } else {

                        dba.open();
                        Cursor curData = mod.getData(BillingTable.DATABASE_TABLE);
                        if (curData.getCount() > 0) {
                            Data = "";
                            while (curData.moveToNext()) {
                                for (int i = 0; i < 10; i++) {

                                    Log.w("........", i + " " + curData.getString(i).trim());

                                    if (i == 9) {
                                        Data = Data + curData.getString(i).trim()
                                                + "," + session.getUserId() + ","
                                                + saleType + "$";
                                    } else {
                                        Data = Data + curData.getString(i).trim() + ",";
                                    }

                                }
                            }


                            String logFlag = "";
                            GPSTracker gps = new GPSTracker(BillPayment.this);
                            if (gps.canGetLocation()) {
                                if (gps.getLongitude() != 0.0 && gps.getLatitude() != 0.0) {
                                    longi = String.valueOf(gps.getLongitude());
                                    lati = String.valueOf(gps.getLatitude());
                                    logFlag = "LocationManager";
                                } else {
                                    Location mLastLocation = FusedLocationApi
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

                                    Log.w("BillPayment", "Online");

                                } else {
                                    Log.w("BillPayment", "Offline");
                                    ContentValues cv = new ContentValues();
                                    cv.put(OfflineTable.KEY_DATA, Data);
                                    cv.put(OfflineTable.KEY_BRANCHNO, session.getBranchNo());
                                    cv.put(OfflineTable.KEY_LONGITUDE, longi);
                                    cv.put(OfflineTable.KEY_LATITUDE, lati);
                                    cv.put(OfflineTable.KEY_METHOD, "saveBill");
                                    dba.open();
                                    mod.insertdata(OfflineTable.DATABASE_TABLE, cv);
                                    Cursor curRNo = mod.getData(OfflineTable.DATABASE_TABLE);
                                    billNo = "OB-" + String.valueOf(curRNo.getCount());
                                    curRNo.close();
                                    dba.close();


                                    billStat = 1;

                                    btnPay.setVisibility(View.GONE);
                                    btnPrint.setVisibility(View.VISIBLE);
                                    btnDone.setVisibility(View.VISIBLE);

                                    mService = new BluetoothService(BillPayment.this, mHandler);
                                    //�����������˳�����
                                    if (mService.isAvailable() == false) {
                                        Toast.makeText(BillPayment.this, "Bluetooth is not available", Toast.LENGTH_LONG).show();

                                    }
                                    if (!mService.isBTopen()) {
                                        Intent enableBtIntent = new Intent(
                                                BluetoothAdapter.ACTION_REQUEST_ENABLE);
                                        startActivityForResult(enableBtIntent,
                                                REQUEST_ENABLE_BT);
                                    } else {
                                        Intent connectIntent = new Intent(BillPayment.this,
                                                DeviceListActivity.class);
                                        startActivityForResult(connectIntent,
                                                REQUEST_CONNECT_DEVICE);
                                    }

                                }


                            } else {
                                gps.showSettingsAlert();
                            }


                        } else {
                            Toast.makeText(BillPayment.this,
                                    "Please Confirm Items First", Toast.LENGTH_LONG)
                                    .show();
                        }
                        curData.close();
                        dba.close();

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
                        Log.w("BillPrint", "Online");
                        printImage();

                    } else {
                        Log.w("BillPrint", "Offline");
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

        if (billStat == 0) {
            Intent intent = new Intent(getApplicationContext(), CaAc .class);
            intent.putExtra("act", "billing");
            startActivity(intent);
            finish();
        } else if (billStat == 1) {
            Intent intent = new Intent(
                    getApplicationContext(),
                    MainActivity.class);
            startActivity(intent);
            finish();
        }

    }

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
                    BILL = session.getAddresse()+"\n";
                    BILL = BILL + "-------------------------------------------";
                    BILL = BILL + "\nInvoice No: " +billNo+ "    "+ strDate+"\n";

                    BILL = BILL + "\nCust Name: "+CustName;
                    BILL = BILL + "\nSalesman:  "+session.getEmpName();
                    BILL = BILL + "\nBill Type: "+spinPayType.getSelectedItem().toString().trim()+"\n";

                    BILL = BILL + "-------------------------------------------\n";

                    BILL = BILL + "Product            Qty    MRP       Total\n";

                    dba.open();
                    Cursor cur = mod.getData(BillingTable.DATABASE_TABLE);
                    if (cur.getCount() > 0) {
                        while (cur.moveToNext()) {
                            String p = "";
                            String sql = "select name from products where pid='"+cur.getString(3).trim()+"'";
                            Cursor curProdName = DataBaseAdapter.ourDatabase.rawQuery(sql,null);
                            if(curProdName.getCount() > 0){
                                curProdName.moveToFirst();
                                p = curProdName.getString(0).trim();
                            }
                            curProdName.close();

                            String q = cur.getString(4).trim();
                            String m = cur.getString(6).trim();
                            String t = cur.getString(9).trim();

                            if(p.length() >= 17){
                                BILL = BILL + p.substring(0, Math.min(p.length(), 17))+"  ";
                            }else {
                                BILL = BILL + p.substring(0, Math.min(p.length(), 17));
                                for(int i = 0;i< 17 - p.length();i++){
                                    BILL = BILL+" ";
                                }
                                BILL = BILL+"  ";
                            }


                            BILL = BILL + q;
                            for(int i = 0;i< 5 - q.length();i++){
                                BILL = BILL+" ";
                            }
                            BILL = BILL+"  ";

                            BILL = BILL + m;
                            for(int i = 0;i< 8 - m.length();i++){
                                BILL = BILL+" ";
                            }
                            BILL = BILL+"  ";

                            BILL = BILL + t +"\n";
                            prQty = prQty + cur.getInt(4);
                            disTot = disTot + cur.getDouble(8);
                        }
                    }
                    cur.close();
                    dba.close();

                    BILL = BILL + "-------------------------------------------\n";
                    BILL = BILL + "Total Qty:  " + "     "+prQty+"\n";
                    BILL = BILL + "Total Disc: " + "     "+Math.round(disTot * 100.0) / 100.0+"\n";
                    BILL = BILL + "Total Value:" + "     "+sum+"\n";
                    BILL = BILL + "-------------------------------------------\n";
                    BILL = BILL + "Customer Sign                 Salesman Sign\n\n\n";
                    BILL = BILL + "             ****Thank You****             \n";
                    BILL = BILL + "       Powered By: www.istreame.com        \n";
                    //    os.write(BILL.getBytes());


                    PrintText.appendLog(BILL);
                    foo(BILL);
               //     createandDisplayPdf(BILL);
                    mService.sendMessage(BILL, "UTF_8");

                    //     OutputStream os = mBluetoothSocket.getOutputStream();
                    //     os.write(BILL.getBytes(StandardCharsets.ISO_8859_1));
                    //     os.flush();


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

    //Save As Image
    void foo(final String text) throws IOException {

        String extStorageDirectory = Environment
                .getExternalStorageDirectory().toString() + "/ebilling_images/print/print.png";


        final Rect bounds = new Rect();
        TextPaint textPaint = new TextPaint() {
            {
                setColor(Color.BLACK);
                setTextAlign(Paint.Align.LEFT);
                setTextSize(20f);
                setAntiAlias(true);
            }
        };
        textPaint.getTextBounds(text, 0, text.length(), bounds);
        StaticLayout mTextLayout = new StaticLayout(text, textPaint,
                bounds.width(), Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
        int maxWidth = -1;
        for (int i = 0; i < mTextLayout.getLineCount(); i++) {
            if (maxWidth < mTextLayout.getLineWidth(i)) {
                maxWidth = (int) mTextLayout.getLineWidth(i);
            }
        }
        final Bitmap bmp = Bitmap.createBitmap(maxWidth, mTextLayout.getHeight(),
                Bitmap.Config.ARGB_8888);
        bmp.eraseColor(Color.WHITE);// just adding white background
        final Canvas canvas = new Canvas(bmp);

        canvas.drawBitmap(bmp, new Matrix(), null);
        Bitmap logo = BitmapFactory.decodeResource(getResources(), R.drawable.abar);
     //   canvas.drawBitmap(logo, maxWidth / 2, mTextLayout.getHeight() / 2, null);
     //   logo = Bitmap.createScaledBitmap(logo, 300, 240, false);
        canvas.drawBitmap(logo, 0, 0, null);

        mTextLayout.draw(canvas);
        FileOutputStream stream = new FileOutputStream(extStorageDirectory); //create your FileOutputStream here
        bmp.compress(Bitmap.CompressFormat.PNG, 85, stream);
        bmp.recycle();
        stream.close();
    }

    private void printImage() throws FileNotFoundException {

      /*  String extStorageDirectory = Environment
                .getExternalStorageDirectory().toString() + "/ebilling_images/print/print.jpg";*/
        String extStorageDirectory = Environment
                .getExternalStorageDirectory().toString() + "/ebilling_images/bill/" + billNo+"1.png";

        Log.w("PA",""+extStorageDirectory);

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


    private void cropImage(){
        Paint paint = new Paint();

        String extStorageDirectory = Environment
                .getExternalStorageDirectory().toString() + "/ebilling_images/bill/" + billNo+"1.png";

        FileInputStream streamIn = null;
        try {
            streamIn = new FileInputStream(extStorageDirectory);

        Bitmap bitmapOrg = BitmapFactory.decodeStream(streamIn);

        final Canvas canvas = new Canvas();
        canvas.drawColor(Color.WHITE);
        // you need to insert a image flower_blue into res/drawable folder
        paint.setFilterBitmap(true);

            Log.w("PA","Width: "+bitmapOrg.getWidth());
            Log.w("PA","Height: "+bitmapOrg.getHeight());

        Bitmap croppedBmp = Bitmap.createBitmap(bitmapOrg, 0, 100, bitmapOrg.getWidth() , bitmapOrg.getHeight() - 100);
        FileOutputStream stream = new FileOutputStream(extStorageDirectory); //create your FileOutputStream here
            croppedBmp.compress(Bitmap.CompressFormat.PNG, 85, stream);
            croppedBmp.recycle();
        stream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }





    private class AsyncCallWS extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            // Call Web Method
            try {

                Log.w("CartActivity", "Start");

                dba.open();
                serverResponse = WebService.makeTransaction(Data, session.getBranchNo(), longi, lati, "saveBill","","");
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
                        BillPayment.this);
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
                                    .equalsIgnoreCase("No")) {

                                AlertDialog.Builder builder2 = new AlertDialog.Builder(
                                        BillPayment.this);
                                builder2.setTitle("Failure..");
                                builder2.setMessage("Failed to save Data.");
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

                                billNo = serverResponse.getJSONObject(0)
                                        .getString("BillNo").trim();


                                Toast.makeText(BillPayment.this,
                                        "Data Successfully Saved!!!",
                                        Toast.LENGTH_LONG);


                                billStat = 1;

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
                                String url = PreFix + "/Bill/" + billNo + "1.png";

                                Log.w("BP","BillNo: "+billNo);
                                Log.w("BP","URL: "+url);

                                new DownloadFile().execute(url, billNo + "1.png");

                            }

                        }

                        // Error status is true
                    } else {

                        Toast.makeText(BillPayment.this, "Server Error",
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
            File folder = new File(extStorageDirectory, "bill");
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

          //  pdfToImage();
            cropImage();;

            waitDialog.cancel();

            mService = new BluetoothService(BillPayment.this, mHandler);
            //�����������˳�����
            if (mService.isAvailable() == false) {
                Toast.makeText(BillPayment.this, "Bluetooth is not available", Toast.LENGTH_LONG).show();

            }
            if (!mService.isBTopen()) {
                Intent enableBtIntent = new Intent(
                        BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent,
                        REQUEST_ENABLE_BT);
            } else {
                Intent connectIntent = new Intent(BillPayment.this,
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




    /*public void pdfToImage(){

        //Converting PDF to Bitmap Image...
        StandardFontTF.mAssetMgr = getAssets();
        //Load document to get the first page
        try {

            String path = Environment
                    .getExternalStorageDirectory().toString() + "/ebilling_images/bill/" + billNo;


            String pathPDF = path+".pdf";
            Log.w("path",""+pathPDF);

            PDFDocument pdf = new PDFDocument(pathPDF, null);
            PDFPage page = pdf.getPage(0);

            //creating Bitmap and canvas to draw the page into
            int width = (int)Math.ceil(page.getDisplayWidth());
            int height = (int)Math.ceil(page.getDisplayHeight());

            Bitmap bm = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(bm);
            page.paintPage(c);

            //Saving the Bitmap
            OutputStream os = new FileOutputStream(path+".jpg");
            bm.compress(Bitmap.CompressFormat.JPEG, 80, os);
            os.close();
        } catch (PDFException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }*/



    //Save As PDF
   /* public void createandDisplayPdf(String text) {

        Document doc = new Document();

        try {
            String path = Environment
                    .getExternalStorageDirectory().toString() + "/ebilling_images/print";

            File dir = new File(path);
            if(!dir.exists())
                dir.mkdirs();

            File file = new File(dir, "print.pdf");
            FileOutputStream fOut = new FileOutputStream(file);

            PdfWriter.getInstance(doc, fOut);

            //open the document
            doc.open();

            Paragraph p1 = new Paragraph(text);
            doc.add(p1);

        } catch (DocumentException de) {
            Log.e("PDFCreator", "DocumentException:" + de);
        } catch (IOException e) {
            Log.e("PDFCreator", "ioException:" + e);
        } finally {
            doc.close();
        }

      //  viewPdf("newFile.pdf", "Dir");
    }*/


}
