package com.tsysinfo.billing;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ActivityNotFoundException;
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
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.LocationServices;
import com.tsysinfo.billing.database.DataBaseAdapter;
import com.tsysinfo.billing.database.Models;
import com.tsysinfo.billing.database.OfflineTable;
import com.tsysinfo.billing.database.ReturnTable;
import com.tsysinfo.billing.database.SessionManager;
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
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Set;
import java.util.UUID;

public class ReturnInvoice extends Activity{

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
    TextView txtReturnProd;
    Button btnSaveReturn, btnPrint, btnDone;
    String Data = "", ReturnNo = "", billNo, CustName = "", retProd = "";
    ProgressDialog waitDialog;
    JSONArray serverResponse;
    String longi = "", lati = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.return_invoice);

        session = new SessionManager(this);
        dba = new DataBaseAdapter(this);
        mod = new Models();

        waitDialog = new ProgressDialog(this);
        waitDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        waitDialog.setMessage("Please Wait");

        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        txtReturnProd = (TextView) findViewById(R.id.textReturnProd);
        btnSaveReturn = (Button) findViewById(R.id.buttonPay);
        btnPrint = (Button) findViewById(R.id.buttonPrint);
        btnDone = (Button) findViewById(R.id.buttonDone);

        btnPrint.setVisibility(View.GONE);
        btnDone.setVisibility(View.GONE);

        billNo = getIntent().getStringExtra("billNo");
        CustName = getIntent().getStringExtra("custName");

        int prQty = 0;
        double tot = 0.0;
        double disc = 0.0;

        dba.open();
        Cursor cur = mod.getData(ReturnTable.DATABASE_TABLE);
        if (cur.getCount() > 0) {

            retProd = "Bill No: " + billNo + "\n";

            while (cur.moveToNext()) {
                String p = "";
                String sql = "select name from products where pid='" + cur.getString(0).trim() + "'";
                Cursor curProdName = DataBaseAdapter.ourDatabase.rawQuery(sql, null);
                if (curProdName.getCount() > 0) {
                    curProdName.moveToFirst();
                    p = curProdName.getString(0).trim();
                }
                int q = cur.getInt(3);
                double t = cur.getDouble(4) * q ;

                retProd = retProd + p + " x " + q + "\n";

                prQty = prQty + q;
                tot = tot + t;
                disc = disc + cur.getDouble(5);

                curProdName.close();
            }
        }

        cur.close();
        dba.close();

        retProd = retProd + "Total Qty:    " + prQty + "\n";
        retProd = retProd + "Total MRP: " + tot + "\n";
        retProd = retProd + "Total Disc: " + disc + "\n";
        retProd = retProd + "Total Amount: " + (tot - disc);
        txtReturnProd.setText(retProd);

        btnSaveReturn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                try {

                    dba.open();
                    String sql = "select * from retutntab";
                    Cursor cur = DataBaseAdapter.ourDatabase.rawQuery(sql, null);
                    if (cur.getCount() > 0) {

                        Data = "";
                        while (cur.moveToNext()) {
                            for (int i = 0; i < 6; i++) {

                                if (i == 5) {
                                    Data = Data + cur.getString(i).trim() + "," + billNo + "," + session.getUserId() + "$";
                                } else {
                                    Data = Data + cur.getString(i).trim() + ",";
                                }

                            }
                        }

                        String logFlag = "";
                        GPSTracker gps = new GPSTracker(ReturnInvoice.this);
                        if (gps.canGetLocation()) {
                            if (gps.getLongitude() != 0.0 && gps.getLatitude() != 0.0) {
                                longi = String.valueOf(gps.getLongitude());
                                lati = String.valueOf(gps.getLatitude());
                                logFlag = "LocationManager";
                            }  else {
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

                                Log.w("Return", "Online");

                            } else {
                                Log.w("Return", "Offline");
                                ContentValues cv = new ContentValues();
                                cv.put(OfflineTable.KEY_DATA, Data);
                                cv.put(OfflineTable.KEY_BRANCHNO, session.getBranchNo());
                                cv.put(OfflineTable.KEY_LONGITUDE, longi);
                                cv.put(OfflineTable.KEY_LATITUDE, lati);
                                cv.put(OfflineTable.KEY_METHOD, "saveSalesReturn");
                                dba.open();
                                mod.insertdata(OfflineTable.DATABASE_TABLE, cv);
                                Cursor curRNo = mod.getData(OfflineTable.DATABASE_TABLE);
                                ReturnNo = "ORet-" + String.valueOf(curRNo.getCount());
                                curRNo.close();
                                dba.close();

                                btnSaveReturn.setVisibility(View.GONE);
                                btnPrint.setVisibility(View.VISIBLE);
                                btnDone.setVisibility(View.VISIBLE);

                                mService = new BluetoothService(ReturnInvoice.this, mHandler);
                                //�����������˳�����
                                if (mService.isAvailable() == false) {
                                    Toast.makeText(ReturnInvoice.this, "Bluetooth is not available", Toast.LENGTH_LONG).show();

                                }
                                if (!mService.isBTopen()) {
                                    Intent enableBtIntent = new Intent(
                                            BluetoothAdapter.ACTION_REQUEST_ENABLE);
                                    startActivityForResult(enableBtIntent,
                                            REQUEST_ENABLE_BT);
                                } else {
                                    Intent connectIntent = new Intent(ReturnInvoice.this,
                                            DeviceListActivity.class);
                                    startActivityForResult(connectIntent,
                                            REQUEST_CONNECT_DEVICE);
                                }

                            }

                        } else {
                            gps.showSettingsAlert();
                        }


                    }
                    cur.close();
                    dba.close();
                }catch (Exception e){
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
                        Log.w("ReturnPrint", "Online");
                        printImage();

                    } else {
                        Log.w("ReturnPrint", "Offline");
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

        Intent intent = new Intent(
                getApplicationContext(),
                MainActivity.class);
        startActivity(intent);
        finish();

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
                    double tot = 0.0;

                    //    BILL = "\nIsteram eSolutions UAE\n";
                    BILL = session.getAddresse() + "\n";
                    BILL = BILL + "-------------------------------------------";
                    BILL = BILL + "\nInvoice No: " + ReturnNo + "      " + strDate;
                    BILL = BILL + "\nBill No:    " + billNo + "\n";

                    BILL = BILL + "\nCust Name:  " + CustName;
                    BILL = BILL + "\nSalesman:   " + session.getEmpName() + "\n";

                    BILL = BILL + "-------------------------------------------\n";

                    BILL = BILL + "Product               Qty      MRP      \n";

                    dba.open();
                    Cursor cur = mod.getData(ReturnTable.DATABASE_TABLE);
                    if (cur.getCount() > 0) {
                        while (cur.moveToNext()) {
                            String p = "";
                            String sql = "select name,mrp from products where pid='" + cur.getString(0).trim() + "'";
                            Cursor curProdName = DataBaseAdapter.ourDatabase.rawQuery(sql, null);
                            if (curProdName.getCount() > 0) {
                                curProdName.moveToFirst();
                                p = curProdName.getString(0).trim();
                            }


                            int q = cur.getInt(3);
                            double t = curProdName.getDouble(1) * q;

                            if (p.length() >= 18) {
                                BILL = BILL + p.substring(0, Math.min(p.length(), 18)) + "  ";
                            } else {
                                BILL = BILL + p;
                                for (int i = 0; i < 18 - p.length(); i++) {
                                    BILL = BILL + " ";
                                }
                                BILL = BILL + "    ";
                            }


                            BILL = BILL + q;
                            for (int i = 0; i < 6 - String.valueOf(q).length(); i++) {
                                BILL = BILL + " ";
                            }
                            BILL = BILL + "   ";

                            BILL = BILL + t + "\n";
                            prQty = prQty + cur.getInt(3);
                            tot = tot + t;

                            curProdName.close();
                        }
                    }

                    cur.close();
                    dba.close();

                    BILL = BILL + "-------------------------------------------\n";
                    BILL = BILL + "Total Qty:      " + prQty + "\n";
                    BILL = BILL + "Total Amount:   " + tot + "\n";
                    BILL = BILL + "-------------------------------------------\n";
                    BILL = BILL + "Customer Sign                 Salesman Sign\n\n\n";
                    BILL = BILL + "             ****Thank You****             \n";
                    BILL = BILL + "       Powered By: www.istreame.com        \n";

                    //This is printer specific code you can comment ==== > Start

                    // Setting height

                    mService.sendMessage(BILL, "UTF_8");

         /*         String braNm = " ";
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


    private void printImage() throws FileNotFoundException {

        String extStorageDirectory = Environment.getExternalStorageDirectory()
                + "/ebilling_images/SalesReturn/" + ReturnNo + "1.png";

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

        String extStorageDirectory = Environment.getExternalStorageDirectory()
                + "/ebilling_images/SalesReturn/" + ReturnNo + "1.png";

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
                serverResponse = WebService.makeTransaction(Data, session.getBranchNo(), longi, lati, "saveSalesReturn","","");
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
                        ReturnInvoice.this);
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
                                        ReturnInvoice.this);
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

                                Toast.makeText(ReturnInvoice.this,
                                        "Data Successfully Saved!!!",
                                        Toast.LENGTH_LONG);

                                ReturnNo = serverResponse.getJSONObject(0)
                                        .getString("SaleReturnNo").trim();

                                btnSaveReturn.setVisibility(View.GONE);
                                btnPrint.setVisibility(View.VISIBLE);
                                btnDone.setVisibility(View.VISIBLE);

                                dba.open();
                                Cursor ip = mod.getData("iptable");
                                ip.moveToFirst();
                                String ipadd = ip.getString(0).trim();
                                String port = ip.getString(2).trim();
                                String PreFix = "http://" + ipadd + ":"+ port;
                                ip.close();
                                dba.close();
                                String url = PreFix + "/SalesReturn/" + ReturnNo + "1.png";

                                new DownloadFile().execute(url, ReturnNo + "1.png");


                            }

                        }

                        // Error status is true
                    } else {

                        Toast.makeText(ReturnInvoice.this, "Server Error",
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
            File folder = new File(extStorageDirectory, "SalesReturn");
            folder.mkdir();

            String[] allFiles;
            if(folder.isDirectory()){
                allFiles = folder.list();
                for (int i=0; i<allFiles.length; i++) {
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

            waitDialog.cancel();

            mService = new BluetoothService(ReturnInvoice.this, mHandler);
            //�����������˳�����
            if (mService.isAvailable() == false) {
                Toast.makeText(ReturnInvoice.this, "Bluetooth is not available", Toast.LENGTH_LONG).show();

            }
            if (!mService.isBTopen()) {
                Intent enableBtIntent = new Intent(
                        BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent,
                        REQUEST_ENABLE_BT);
            } else {
                Intent connectIntent = new Intent(ReturnInvoice.this,
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
