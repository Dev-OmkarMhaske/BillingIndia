package com.tsysinfo.billing;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;

import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.LocationServices;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.tsysinfo.billing.barcodes.FullScannerActivity;
import com.tsysinfo.billing.database.CustomerTable;
import com.tsysinfo.billing.database.DataBaseAdapter;
import com.tsysinfo.billing.database.Models;
import com.tsysinfo.billing.database.OfflineFeedbackTable;
import com.tsysinfo.billing.database.SessionManager;
import com.tsysinfo.billing.database.WebService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class FeedbackActivity extends AppCompatActivity {

    private static final String TAG = "FeedbackActivity";

    Spinner spinnerCustomer;
    TextView txtDate;
    EditText editRemarks;
    ImageView imageView;
    Button btnSave,btnCancel;
    String Data = "";

    Double currLatitude;
    Double currLongitude;

    DataBaseAdapter dba;
    Models mod;

    SessionManager session;

    ProgressDialog waitDialog;
    JSONArray serverResponse;
    static boolean errored = false;


    String mCurrentPhotoPath;

    Bitmap image = null;

    byte[] bytes;
    String encodedString;
    byte[] b;
    static final int REQUEST_TAKE_PHOTO = 2;
   // static final int REQUEST_TAKE_PHOTO = 2;
    static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 2;
    File photoFile = null;

    ToastService toastService;
    public Location mLastLocation;
    private String GLOBAL_CUST_ID;


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG, "onActivityResult: " + this);
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == Activity.RESULT_OK) {
            setPic();
//			Bitmap bitmap = (Bitmap) data.getExtras().get("data");
//			if (bitmap != null) {
//                imageView.setImageBitmap(bitmap);
//				try {
//					sendPhoto(bitmap);
//				} catch (Exception e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
        }
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

                    ArrayAdapter myAdap = (ArrayAdapter) spinnerCustomer.getAdapter(); //cast to an ArrayAdapter

                    int spinnerPosition = myAdap.getPosition(myString);

//set the default according to value
                    spinnerCustomer.setSelection(spinnerPosition);
                }else
                {

                    String temp=GLOBAL_CUST_ID;
                    temp = temp.replaceAll("[^\\d.]", "");
                    if(temp.equalsIgnoreCase(""))
                    {
                        temp="1234567";
                    }
                    String sql1 = "select " + CustomerTable.KEY_NAME + " from "
                            + CustomerTable.DATABASE_TABLE+ " where "+CustomerTable.KEY_ID+"="+temp+" ";
                    Cursor custCur1 = DataBaseAdapter.ourDatabase.rawQuery(sql1, null);

                    if(custCur1.getCount()>0)
                    {
                        custCur1.moveToFirst();
                        myString=custCur1.getString(custCur1.getColumnIndex(CustomerTable.KEY_NAME)) ;


                        ArrayAdapter myAdap = (ArrayAdapter) spinnerCustomer.getAdapter(); //cast to an ArrayAdapter

                        int spinnerPosition = myAdap.getPosition(myString);

//set the default according to value
                        spinnerCustomer.setSelection(spinnerPosition);
                    }
                    else
                    {
                        spinnerCustomer.setSelection(0);
                        Toast.makeText(FeedbackActivity.this, "No User Found", Toast.LENGTH_SHORT).show();

                    }

                }
                dba.close();
                //the value you want the position for

            }
        }
    }

    private void sendPhoto(Bitmap bitmap) {
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);





        //References
        dba = new DataBaseAdapter(this);
        mod = new Models();
        session = new SessionManager(this);

        waitDialog = new ProgressDialog(this);
        waitDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        waitDialog.setMessage("Please Wait");

        spinnerCustomer = (Spinner) findViewById(R.id.spinnerCustomer);
        txtDate = (TextView) findViewById(R.id.feedbackDate);
        editRemarks = (EditText) findViewById(R.id.editRemarks);
        imageView = (ImageView) findViewById(R.id.feedbackImage);
        btnSave = (Button) findViewById(R.id.buttonSave);
        btnCancel = (Button) findViewById(R.id.buttonCancel);
        ImageView barCode=(ImageView)findViewById(R.id.imageButtonBarcode);
        barCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FeedbackActivity.this,FullScannerActivity.class);
                startActivityForResult(intent, 1);
            }
        });

        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        String strDate = sdf.format(c.getTime());
        txtDate.setText(strDate);

        dba.open();
        ArrayList<String> customers = new ArrayList<String>();
        String sql1 = "select " + CustomerTable.KEY_NAME + " from "
                + CustomerTable.DATABASE_TABLE;
        Cursor custCur1 = DataBaseAdapter.ourDatabase.rawQuery(sql1, null);
        if (custCur1.getCount() > 0) {
            customers.add("Select Customer");
            while (custCur1.moveToNext()) {
                customers.add(custCur1.getString(0).trim());
            }
        }
        custCur1.close();
        dba.close();

        // Creating adapter for spinner
        ArrayAdapter<String> adapterCust = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, customers);
        adapterCust
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCustomer.setAdapter(adapterCust);
        spinnerCustomer.setEnabled(false);


        GPSTracker gps = new GPSTracker(this);

        try {

            // check if GPS enabled
            if (gps.canGetLocation()) {

                mLastLocation = LocationServices.FusedLocationApi
                        .getLastLocation(MainActivity.mGoogleApiClient);

                currLatitude = gps.getLatitude();
                currLongitude = gps.getLongitude();

                if(currLatitude != 0.0 && currLongitude != 0.0) {

                    dba.open();
                    String sql = "select " + CustomerTable.KEY_NAME + "," + CustomerTable.KEY_LONGI + "," + CustomerTable.KEY_LATI + " from "
                            + CustomerTable.DATABASE_TABLE;
                    Cursor custCur = DataBaseAdapter.ourDatabase.rawQuery(sql, null);

                    Log.w("CA", "SQL :" + sql);

                    if (custCur.getCount() > 0) {
                        //	custCur.moveToFirst();

                        int LocFlg = 0;

                        while (custCur.moveToNext()) {

                            Log.w("CA", "currLati :" + currLatitude);
                            Log.w("CA", "currLongi :" + currLongitude);
                            if (CalculateDistance.distance(custCur.getDouble(2), custCur.getDouble(1), currLatitude, currLongitude) < Double.parseDouble(session.getDistance())) {
                                //do what you want to do...
                                //	txtCustomer.setText(custCur.getString(0).trim());

                                Log.w("CA", "Name :" + custCur.getString(0).trim());
                                Log.w("CA", "Lati :" + custCur.getString(2));
                                Log.w("CA", "Longi :" + custCur.getString(1));

                                Log.w("CA", "currLati :" + currLatitude);
                                Log.w("CA", "currLongi :" + currLongitude);



                                for (int i = 0; i < spinnerCustomer.getAdapter()
                                        .getCount(); i++) {
                                    if (spinnerCustomer.getAdapter().getItem(i)
                                            .toString().contains(custCur.getString(0).trim())) {
                                        spinnerCustomer.setSelection(i);
                                        LocFlg = 1;
                                        break;
                                    }
                                }
                            }
                        }
                        if(LocFlg == 0){
                            Toast.makeText(FeedbackActivity.this, "Unable to connect to GPS Please Select Customer Manually", Toast.LENGTH_SHORT).show();
                        }

                    }
                    custCur.close();
                    dba.close();
                }else{
                    toastService.startTimer();
                }

            }

        }catch (Exception e){
            e.printStackTrace();
            LogError lErr = new LogError();
            lErr.appendLog(e.toString());
        }

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });


        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                // Ensure that there's a camera activity to handle the intent
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    // Create the File where the photo should go
                    File photoFile = null;
                    try {
                        photoFile = createImageFile();
                    } catch (IOException ex) {
                        // Error occurred while creating the File

                    }
                    Log.w(TAG,"photoFile: "+photoFile);
                    // Continue only if the File was successfully created
                    if (photoFile != null) {
//                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
//                        startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
//                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                        startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);

                        takePictureIntent.putExtra(MediaStore.ACTION_IMAGE_CAPTURE, Uri.fromFile(photoFile));
                        startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
//
                   }
                }

            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String cust = spinnerCustomer.getSelectedItem().toString().trim();
                String dt = txtDate.getText().toString().trim();
                String remarks = editRemarks.getText().toString().trim();
                if(!cust.equalsIgnoreCase("Select Customer") || dt.length() > 0 || remarks.length() > 0){

                    dba.open();
                    String sql = "select id from customer where name='"+cust+"'";
                    Cursor cur = DataBaseAdapter.ourDatabase.rawQuery(sql,null);
                    if(cur.getCount() > 0){
                        cur.moveToFirst();
                        cust = cur.getString(0).trim();
                    }
                    cur.close();
                    dba.close();


                    Data = cust +","+dt+","+remarks;

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

                        Log.w("Feedback", "Online");

                    } else {
                        Log.w("Feedback", "Offline");
                        ContentValues cv = new ContentValues();
                        cv.put(OfflineFeedbackTable.KEY_EMPID, session.getEmpNo());
                        cv.put(OfflineFeedbackTable.KEY_BRANCHNO, session.getBranchNo());
                        cv.put(OfflineFeedbackTable.KEY_DATA, Data);
                        cv.put(OfflineFeedbackTable.KEY_IMAGEPATH, mCurrentPhotoPath);
                        cv.put(OfflineFeedbackTable.KEY_METHOD, "saveFeedback");
                        dba.open();
                        mod.insertdata(OfflineFeedbackTable.DATABASE_TABLE, cv);
                        dba.close();
                        Toast.makeText(FeedbackActivity.this,
                                "Data Saved Offline...!!",
                                Toast.LENGTH_LONG);

                        Intent intent = new Intent(
                                getApplicationContext(),
                                MainActivity.class);
                        startActivity(intent);
                        finish();
                    }

                }else{
                    Toast.makeText(getApplicationContext(),"Please Fill all Details",Toast.LENGTH_LONG).show();
                }



            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }
/*

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        Log.i(TAG, "onActivityResult: " + this);
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == Activity.RESULT_OK) {
            setPic();
//			Bitmap bitmap = (Bitmap) data.getExtras().get("data");
//			if (bitmap != null) {
//				mImageView.setImageBitmap(bitmap);
//				try {
//					sendPhoto(bitmap);
//				} catch (Exception e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
        }
    }
*/

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

    // WebService Code

    private class AsyncCallWS extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            // Call Web Method
            try {

                Log.w("CartActivity", "Start");

                dba.open();
                serverResponse = WebService.sendFeedback(session.getEmpNo(), session.getBranchNo(), Data, mCurrentPhotoPath, "saveFeedback");
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

            Log.w("FeedbackActivity", "TimeOutFlag : " + WebService.timeoutFlag);
            Log.w("FeedbackActivity", "ResponseString : "
                    + serverResponse);

            // Make Progress Bar invisible
            waitDialog.cancel();
            Log.w("FeedbackActivity", "Dialog Closed");
            if (WebService.timeoutFlag == 1) {
                Log.w("FeedbackActivity", "Timeout");
                AlertDialog.Builder builder = new AlertDialog.Builder(
                        FeedbackActivity.this);
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
                                    .equalsIgnoreCase("Failed")) {

                                AlertDialog.Builder builder2 = new AlertDialog.Builder(
                                        FeedbackActivity.this);
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

                                Toast.makeText(FeedbackActivity.this, "Feedback saved Successfully!!!!",
                                        Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(FeedbackActivity.this,MainActivity.class);
                                startActivity(intent);
                                finish();

                            }

                        }

                        // Error status is true
                    } else {

                        Toast.makeText(FeedbackActivity.this, "Server Error",
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

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        String storageDir = Environment.getExternalStorageDirectory() + "/picupload";
        File dir = new File(storageDir);
        if (!dir.exists())
            dir.mkdir();

        File image = new File(storageDir + "/" + imageFileName + ".jpg");

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getPath();
        Log.w(TAG, "photo path = " + mCurrentPhotoPath);
        return image;
    }

    private void setPic() {
        // Get the dimensions of the View

        try {


            Bitmap bitmap = null;


          //      path = mImageCaptureUri.getPath();
                bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath);


            InputStream inputStream = new FileInputStream(mCurrentPhotoPath);

            byte[] buffer = new byte[8192];
            int bytesRead;
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            try {
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    output.write(buffer, 0, bytesRead);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            bytes = output.toByteArray();
            encodedString = Base64.encodeToString(bytes, Base64.DEFAULT);

            Log.w("FMA", "Base64: " + encodedString);
            Log.w("FMA", "BMP: " + bitmap);

            image = Bitmap.createScaledBitmap(bitmap, 100, 130, false);

            Log.w("FMA", "Path: " + mCurrentPhotoPath);
            imageView.setImageBitmap(image);

        } catch (Exception e) {
            Toast.makeText(FeedbackActivity.this,
                    "Something went embrassing", Toast.LENGTH_LONG).show();
        }


    }


}
