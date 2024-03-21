package com.tsysinfo.billing;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
 
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.location.LocationServices;
import com.tsysinfo.billing.database.CustomerTable;
import com.tsysinfo.billing.database.CustomerTypeTable;
import com.tsysinfo.billing.database.DataBaseAdapter;
import com.tsysinfo.billing.database.DistrictTable;
import com.tsysinfo.billing.database.Models;
import com.tsysinfo.billing.database.SessionManager;
import com.tsysinfo.billing.database.Sync;
import com.tsysinfo.billing.database.TownTable;
import com.tsysinfo.billing.database.WebService;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class AddCustomerActivity extends AppCompatActivity {

    static boolean errored = false;
    public Location mLastLocation;
    EditText editName, editPlace, editAddress, editRegNo, editTIN, editPIN, editCST, editShortName,
            editLongitude, editLatitude, editMobileNo, editLandLineNo, editCustomerCode,editEmailId;
    Spinner spinnerTown, spinnerCustomerType, spinnerIsActive,spinnerDistrict;
    CheckBox chekBox;
    String strName, strPlace, strAddress, strRegNo, strTIN, strPIN, strCST, strShortName,
            strLongitude, strLatitude, strMobileNo, strLandLineNo, strCustomerCode,
            strTown, strCustType, strIsActive, Data = "",district,emailId;
    Button btnSave, btnClear;
    ArrayList<String> arrTown, arrCustType, arrIsActive,arraDist;
    ArrayAdapter<String> adapterTown, adapterCustType, adapterIsActive,adapterDist;
    SessionManager session;
    DataBaseAdapter dba;
    Models mod;
    ProgressDialog dialog, waitDialog;
    JSONArray serverResponse;
    private String DistrictId="0";
    private String KEY;
    private String ID,customername;
    ContentValues cvup=new ContentValues();
    private int pos;

    String globtown;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_customer);

        // References
        session = new SessionManager(this);
        dba = new DataBaseAdapter(this);
        mod = new Models();

        waitDialog = new ProgressDialog(this);
        waitDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        waitDialog.setMessage("Please Wait");

        dialog = new ProgressDialog(this);

        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        editName = (EditText) findViewById(R.id.editName);
        editPlace = (EditText) findViewById(R.id.editPlace);
        editAddress = (EditText) findViewById(R.id.editAddress);
        editRegNo = (EditText) findViewById(R.id.editRegNo);
        editTIN = (EditText) findViewById(R.id.editTIN);
        editPIN = (EditText) findViewById(R.id.editPIN);
        editCST = (EditText) findViewById(R.id.editCST);
        editShortName = (EditText) findViewById(R.id.editShortName);
        editLongitude = (EditText) findViewById(R.id.editLongitude);
        editLatitude = (EditText) findViewById(R.id.editLatitude);
        editMobileNo = (EditText) findViewById(R.id.editMobileNo);
        editLandLineNo = (EditText) findViewById(R.id.editLandLineMobileNo);
        editEmailId = (EditText) findViewById(R.id.editEmailId);
        editCustomerCode = (EditText) findViewById(R.id.editCustomerCode);

        spinnerTown = (Spinner) findViewById(R.id.spinnerTown);
        spinnerDistrict = (Spinner) findViewById(R.id.spinnerDistric);
        spinnerCustomerType = (Spinner) findViewById(R.id.spinnerCustType);
        spinnerIsActive = (Spinner) findViewById(R.id.spinnerIsActive);

        chekBox = (CheckBox) findViewById(R.id.chekBox);

        btnSave = (Button) findViewById(R.id.buttonSave);
        btnClear = (Button) findViewById(R.id.buttonClear);

        Intent intent=getIntent();
        KEY=intent.getStringExtra("key");
        if(KEY.equalsIgnoreCase("update")) {

            ID = intent.getStringExtra("ID");
        }


        arrIsActive = new ArrayList<String>();
        arrIsActive.add("Select Status");
        arrIsActive.add("Yes");
        arrIsActive.add("No");

        adapterIsActive = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, arrIsActive);
        adapterIsActive.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerIsActive.setAdapter(adapterIsActive);

        dba.open();
        arraDist = new ArrayList<String>();
        String sqlDist = "select distinct " + DistrictTable.KEY_Dist + " from " + DistrictTable.DATABASE_TABLE +" " ;
        Cursor curDist = DataBaseAdapter.ourDatabase.rawQuery(sqlDist, null);
        arraDist.add("Select District");
        while (curDist.moveToNext()) {
            arraDist.add(curDist.getString(0).trim());
        }
        curDist.close();
        dba.close();

        adapterDist = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, arraDist);
        adapterDist.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDistrict.setAdapter(adapterDist);

        spinnerDistrict.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
               String town= spinnerDistrict.getSelectedItem().toString();
                setTown(town);
                if(KEY.equalsIgnoreCase("update"))
                {
                    selectSpinnerValue(spinnerTown,globtown);
                    Log.w("town",globtown);

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        dba.open();



        arrCustType = new ArrayList<String>();
        String sqlCustType = "select distinct " + CustomerTypeTable.KEY_CUSTTYPE + " from " + CustomerTypeTable.DATABASE_TABLE;
        final Cursor curCustType = DataBaseAdapter.ourDatabase.rawQuery(sqlCustType, null);
        arrCustType.add("Select Type");
        while (curCustType.moveToNext()) {
            arrCustType.add(curCustType.getString(0).trim());
        }
        curCustType.close();

        adapterCustType = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, arrCustType);
        adapterCustType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCustomerType.setAdapter(adapterCustType);
        dba.close();

        chekBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (chekBox.isChecked()) {
                    editLongitude.setEnabled(false);
                    editLatitude.setEnabled(false);
                    GPSTracker gps = new GPSTracker(AddCustomerActivity.this);
                    mLastLocation = LocationServices.FusedLocationApi
                            .getLastLocation(MainActivity.mGoogleApiClient);

                    if (gps.canGetLocation()) {
                        double currLatitude = gps.getLatitude();
                        double currLongitude = gps.getLongitude();
                        if (currLatitude == 0.0 && currLongitude == 0.0) {
                            Toast.makeText(AddCustomerActivity.this, "Not able to connect GPS\nPlease Move to the Open Space"+currLongitude,
                                    Toast.LENGTH_LONG).show();
                        } else {
                            editLongitude.setText(String.valueOf(currLongitude));
                            editLatitude.setText(String.valueOf(currLatitude));
                        }
                    } else {
                        gps.showSettingsAlert();
                    }
                } else {
                    editLongitude.setEnabled(true);
                    editLatitude.setEnabled(true);
                }
            }
        });

      spinnerTown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
          @Override
          public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

          }

          @Override
          public void onNothingSelected(AdapterView<?> parent) {

          }
      });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                strName = editName.getText().toString().trim();
                strLongitude = editLongitude.getText().toString().trim();
                strLatitude = editLatitude.getText().toString().trim();

                int Chkflag = 0;
                int fwd = 0;
                if (chekBox.isChecked()) {
                    Chkflag = 1;
                } else {
                    Chkflag = 0;
                }

                if (Chkflag == 1) {
                    if (strLongitude.length() > 0) {
                        if (strLatitude.length() > 0) {
                            fwd = 1;
                        } else {
                            fwd = 0;
                            Toast.makeText(getApplicationContext(), "Enter Latitude", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        fwd = 0;
                        Toast.makeText(getApplicationContext(), "Enter Longitude", Toast.LENGTH_LONG).show();
                    }
                }else {
                    Toast.makeText(AddCustomerActivity.this, "Check the Checkbox for the current location.", Toast.LENGTH_SHORT).show();
                }

                if (fwd == 1) {

                        Data = ";";
                        AsyncCallWS task = new AsyncCallWS();
                        // Call execute
                        task.execute();

                }


               /* strName = editName.getText().toString().trim();
                strPlace = editPlace.getText().toString().trim();
                strAddress = editAddress.getText().toString().trim();
                strRegNo = editRegNo.getText().toString().trim();
                strTIN = editTIN.getText().toString().trim();
                strPIN = editPIN.getText().toString().trim();
                strCST = editCST.getText().toString().trim();
                strShortName = editShortName.getText().toString().trim();
                strLongitude = editLongitude.getText().toString().trim();
                strLatitude = editLatitude.getText().toString().trim();
                strMobileNo = editMobileNo.getText().toString().trim();
                strLandLineNo = editLandLineNo.getText().toString().trim();
                strCustomerCode = editCustomerCode.getText().toString().trim();
                strTown = spinnerTown.getSelectedItem().toString().trim();
                strCustType = spinnerCustomerType.getSelectedItem().toString().trim();
                strIsActive = spinnerIsActive.getSelectedItem().toString().trim();
                district=spinnerDistrict.getSelectedItem().toString();
                emailId=editEmailId.getText().toString();





                cvup.put(CustomerTable.KEY_NAME,strName);
                cvup.put(CustomerTable.KEY_PLACE,strPlace);
                cvup.put(CustomerTable.KEY_ADDRESS,strAddress);
                cvup.put(CustomerTable.KEY_REGNO,strRegNo);
                cvup.put(CustomerTable.KEY_TIN,strTIN);
                cvup.put(CustomerTable.KEY_PIN,strPIN);
                cvup.put(CustomerTable.KEY_CSTNO,strCST);
                cvup.put(CustomerTable.KEY_SHORTNAME,strShortName);
                cvup.put(CustomerTable.KEY_LONGI,strLongitude);
                cvup.put(CustomerTable.KEY_LATI,strLatitude);
                cvup.put(CustomerTable.KEY_MOBILE,strMobileNo);
                cvup.put(CustomerTable.KEY_LANDLINE,strLandLineNo);
                cvup.put(CustomerTable.KEY_CUSTOMER_CODE,strCustomerCode);
                cvup.put(CustomerTable.KEY_AREA,strTown);
                cvup.put(CustomerTable.KEY_TYPE,strCustType);
                cvup.put(CustomerTable.KEY_STATUS,strIsActive);
                cvup.put(CustomerTable.KEY_District,district);
                cvup.put(CustomerTable.KEY_Email,emailId);

                int Chkflag = 0;
                int fwd = 0;
                if (chekBox.isChecked()) {
                    Chkflag = 1;
                } else {
                    Chkflag = 0;
                }

                if (strName.length() > 0) {

                    if (district.length() > 0) {
                        if (emailId.length() > 0)
                        {
                            if (strPlace.length() > 0) {
                                if (strAddress.length() > 0) {
                                    if (!strTown.equalsIgnoreCase("Select Town")) {
                                        if (!strCustType.equalsIgnoreCase("Select Type")) {
                                            if (Chkflag == 1) {
                                                if (strLongitude.length() > 0) {
                                                    if (strLatitude.length() > 0) {
                                                        fwd = 1;
                                                    } else {
                                                        fwd = 0;
                                                        Toast.makeText(getApplicationContext(), "Enter Latitude", Toast.LENGTH_LONG).show();
                                                    }
                                                } else {
                                                    fwd = 0;
                                                    Toast.makeText(getApplicationContext(), "Enter Longitude", Toast.LENGTH_LONG).show();
                                                }
                                            }
                                            if (fwd == 1) {


                                                if (!strIsActive.equalsIgnoreCase("Select Status")) {
                                                    String areaId = "", custTypeId = "",distID="";
                                                    dba.open();
                                                    String sqldis = "select "+DistrictTable.KEY_ID+" from "+DistrictTable.DATABASE_TABLE+" where "+DistrictTable.KEY_Dist+"='" + district + "'";
                                                    Cursor curdis = DataBaseAdapter.ourDatabase.rawQuery(sqldis, null);
                                                    if (curdis.getCount() > 0) {
                                                        curdis.moveToFirst();
                                                        distID = curdis.getString(0).trim();
                                                    }
                                                    curdis.close();


                                                    String sqlAC = "select id from town where townname='" + strTown + "'";
                                                    Cursor curAC = DataBaseAdapter.ourDatabase.rawQuery(sqlAC, null);
                                                    if (curAC.getCount() > 0) {
                                                        curAC.moveToFirst();
                                                        areaId = curAC.getString(0).trim();
                                                    }
                                                    curAC.close();



                                                    String sqlCT = "select id from custtype where type='" + strCustType + "'";
                                                    Cursor curCT = DataBaseAdapter.ourDatabase.rawQuery(sqlCT, null);
                                                    if (curCT.getCount() > 0) {
                                                        curCT.moveToFirst();
                                                        custTypeId = curCT.getString(0).trim();
                                                    }
                                                    curCT.close();
                                                    dba.close();



                                                    dba.open();
                                                    String sqlMobileCheck = "select mobile  from customer where mobile ='" + strMobileNo + "'";
                                                    Cursor sqlMobileCheckCursor = DataBaseAdapter.ourDatabase.rawQuery(sqlMobileCheck, null);
                                                    if (sqlMobileCheckCursor.getCount() > 0) {

                                                        if (KEY.equalsIgnoreCase("update"))
                                                        {
                                                            dba.close();

                                                            Data = Data + strName + "$" + strPlace + "$" + strAddress + "$" + strRegNo + "$"
                                                                    + strTIN + "$" + strPIN + "$" + strCST + "$" + areaId + "$" + custTypeId + "$0$"
                                                                    + strLatitude + "$" + strLongitude + "$" + strMobileNo + "$" + strLandLineNo + "$"
                                                                    + strShortName + "$0$" + strCustomerCode + "$" + distID + "$" + emailId;

                                                            AsyncCallWS task = new AsyncCallWS();
                                                            // Call execute
                                                            task.execute();
                                                        }
                                                        else
                                                        {
                                                            Toast.makeText(AddCustomerActivity.this, "Mobile Number already exist.", Toast.LENGTH_SHORT).show();
                                                        }



                                                    }
                                                    else {


                                                        dba.close();

                                                        Data = Data + strName + "$" + strPlace + "$" + strAddress + "$" + strRegNo + "$"
                                                                + strTIN + "$" + strPIN + "$" + strCST + "$" + areaId + "$" + custTypeId + "$0$"
                                                                + strLatitude + "$" + strLongitude + "$" + strMobileNo + "$" + strLandLineNo + "$"
                                                                + strShortName + "$0$" + strCustomerCode + "$" + distID + "$" + emailId;

                                                        AsyncCallWS task = new AsyncCallWS();
                                                        // Call execute
                                                        task.execute();
                                                    }
                                                } else {
                                                    Toast.makeText(getApplicationContext(), "Select IsActive", Toast.LENGTH_LONG).show();
                                                }


                                            }
                                            else
                                            {
                                                Toast.makeText(AddCustomerActivity.this, "Please check for the current location.", Toast.LENGTH_SHORT).show();
                                            }
                                        } else {
                                            Toast.makeText(getApplicationContext(), "Select Customer Type", Toast.LENGTH_LONG).show();
                                        }
                                    } else {
                                        Toast.makeText(getApplicationContext(), "Select Town", Toast.LENGTH_LONG).show();
                                    }
                                } else {
                                    Toast.makeText(getApplicationContext(), "Enter Address", Toast.LENGTH_LONG).show();
                                }
                            } else {
                                Toast.makeText(getApplicationContext(), "Enter Place", Toast.LENGTH_LONG).show();
                            }
                    }
                        else {
                            Toast.makeText(getApplicationContext(), "Enter Email id", Toast.LENGTH_LONG).show();
                        }
                }else{
                        Toast.makeText(getApplicationContext(), "Select District", Toast.LENGTH_LONG).show();
                    }

                } else {
                    Toast.makeText(getApplicationContext(), "Enter Name", Toast.LENGTH_LONG).show();
                }*/
            }
        });

        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editName.setText("");
               /* editPlace.setText("");
                editAddress.setText("");
                editRegNo.setText("");
                editTIN.setText("");
                editEmailId.setText("");
                editPIN.setText("");
                editCST.setText("");
                editShortName.setText("");*/
                editLongitude.setText("");
                editLatitude.setText("");
                /*editMobileNo.setText("");
                editLandLineNo.setText("");
                editCustomerCode.setText("");
                editEmailId.setText("");
                spinnerCustomerType.setSelection(0);
                spinnerDistrict.setSelection(0);
                spinnerTown.setSelection(0);
                spinnerIsActive.setSelection(0);
                spinnerDistrict.setSelection(0);*/


            }
        });


        if(KEY.equalsIgnoreCase("update"));
        {
            dba.open();
            String sql="select * from "+ CustomerTable.DATABASE_TABLE+" where "+CustomerTable.KEY_ID+" = '"+ID+"'";
            Cursor cursor=DataBaseAdapter.ourDatabase.rawQuery(sql,null);

            if(cursor.getCount()>0)
            {
                cursor.moveToFirst();
                customername=cursor.getString(cursor.getColumnIndex(CustomerTable.KEY_NAME));
                editName.setText(cursor.getString(cursor.getColumnIndex(CustomerTable.KEY_NAME)));
                editPlace.setText(cursor.getString(cursor.getColumnIndex(CustomerTable.KEY_PLACE)));
                editAddress.setText(cursor.getString(cursor.getColumnIndex(CustomerTable.KEY_ADDRESS)));
                editRegNo.setText(cursor.getString(cursor.getColumnIndex(CustomerTable.KEY_REGNO)));
                editTIN.setText(cursor.getString(cursor.getColumnIndex(CustomerTable.KEY_TIN)));
                editEmailId.setText(cursor.getString(cursor.getColumnIndex(CustomerTable.KEY_Email)));
                editPIN.setText(cursor.getString(cursor.getColumnIndex(CustomerTable.KEY_PIN)));
                editCST.setText(cursor.getString(cursor.getColumnIndex(CustomerTable.KEY_CSTNO)));
                editShortName.setText(cursor.getString(cursor.getColumnIndex(CustomerTable.KEY_SHORTNAME)));
                editLongitude.setText(cursor.getString(cursor.getColumnIndex(CustomerTable.KEY_LONGI)));
                editLatitude.setText(cursor.getString(cursor.getColumnIndex(CustomerTable.KEY_LATI)));
                editMobileNo.setText(cursor.getString(cursor.getColumnIndex(CustomerTable.KEY_MOBILE)));
                editLandLineNo.setText(cursor.getString(cursor.getColumnIndex(CustomerTable.KEY_LANDLINE)));
                editCustomerCode.setText(cursor.getString(cursor.getColumnIndex(CustomerTable.KEY_CUSTOMER_CODE)));

                String custType=cursor.getString(cursor.getColumnIndex(CustomerTable.KEY_TYPE));
                if(!custType.equalsIgnoreCase("")){
                    selectSpinnerValue(spinnerCustomerType,custType);
                }

                String dist=cursor.getString(cursor.getColumnIndex(CustomerTable.KEY_District));
                if(!dist.equalsIgnoreCase("")) {
                    selectSpinnerValue(spinnerDistrict, dist);
                    //setTown(dist);
                }

                String town=cursor.getString(cursor.getColumnIndex(CustomerTable.KEY_AREA));

                globtown=town;
                if(!town.equals("")) {

                    selectSpinnerValueTown(spinnerTown, town);
                    spinnerTown.setSelection(pos);
                }

               /* String isactive=cursor.getString(cursor.getColumnIndex(CustomerTable.KEY_STATUS));
                if(!isactive.equalsIgnoreCase("")) {

                    selectSpinnerValue(spinnerIsActive, isactive);
                }*/


                btnSave.setText("Update");


            }

            dba.close();

        }
    }
    public void selectSpinnerValue(Spinner spinner, String myString)
    {
        int index = 0;
        for(int i = 0; i < spinner.getCount(); i++){
            if(spinner.getItemAtPosition(i).toString().trim().equalsIgnoreCase(myString)){
                spinner.setSelection(i);

                break;
            }
        }
    }

    public void selectSpinnerValueTown(Spinner spinner, String myString)
    {
        int index = 0;
        for(int i = 0; i < spinner.getCount(); i++){
            if(spinner.getItemAtPosition(i).toString().trim().equalsIgnoreCase(myString)){
                pos=i;
                spinner.setSelection(i);
pos=i;
                break;
            }
        }
    }
    public void setTown(String town)
    {
        dba.open();

        String sqldis = "select "+DistrictTable.KEY_ID+" from "+DistrictTable.DATABASE_TABLE+" where "+DistrictTable.KEY_Dist+"='" + town + "'";
        Cursor curdis = DataBaseAdapter.ourDatabase.rawQuery(sqldis, null);
        if (curdis.getCount() > 0) {
            curdis.moveToFirst();
            DistrictId = curdis.getString(0).trim();
        }
        curdis.close();
        arrTown = new ArrayList<String>();
        String sqlTown = "select distinct " + TownTable.KEY_TOWN + " from " + TownTable.DATABASE_TABLE +" where "+TownTable.KEY_DistId+" = '"+DistrictId+"' " ;
        Cursor curTown = DataBaseAdapter.ourDatabase.rawQuery(sqlTown, null);
        arrTown.add("Select Town");
        while (curTown.moveToNext()) {
            arrTown.add(curTown.getString(0).trim());
        }
        curTown.close();

        dba.close();
        adapterTown = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, arrTown);
        adapterTown.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTown.setAdapter(adapterTown);
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
        finish();
    }


    // WebService Code

    private class AsyncCallWS extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            // Call Web Method
            try {
        if(KEY.equalsIgnoreCase("update"))
        {
        Log.w("AddCustomerActivity", "Start");
        dba.open();
        serverResponse = WebService.customerUpdate(session.getBranchNo(),ID, Data,strLatitude,strLongitude,session.getEmpNo(), "UpdateCustomer");
        dba.close();
        }else
        {
        Log.w("AddCustomerActivity", "Start");
        dba.open();
        serverResponse = WebService.customerRegistration(session.getBranchNo(), Data, "SaveCustomer");
        dba.close();
        }

            } catch (Exception e) {
                Log.w("AddCustomerActivity", "Timeout ");
            }
            return null;
        }

        @SuppressLint("ResourceAsColor")
        @Override
        // Once WebService returns response
        protected void onPostExecute(Void result) {

            Log.w("AddCustomerActivity", "TimeOutFlag : " + WebService.timeoutFlag);
            Log.w("AddCustomerActivity", "ResponseString : "
                    + WebService.responseString);

            // Make Progress Bar invisible
            waitDialog.cancel();
            Log.w("AddCustomerActivity", "Dialog Closed");
            if (WebService.timeoutFlag == 1) {
                Log.w("CartActivity", "Timeout");
                AlertDialog.Builder builder = new AlertDialog.Builder(AddCustomerActivity.this);
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
                Log.w("AddCustomerActivity", "Try");
                try {


                    // Error status is false
                    if (!errored) {
                        // Based on Boolean value returned from WebService
                        if (serverResponse != null) {

                            if (serverResponse.getJSONObject(0).getString("Status").trim().equalsIgnoreCase("No")) {

                                final AlertDialog.Builder builder2 = new AlertDialog.Builder(AddCustomerActivity.this);
                                builder2.setTitle("Failure..");
                                builder2.setMessage("Failed to save Data.");
                                builder2.setPositiveButton("OK",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                Log.e("info", "OK");

                                            }
                                        });
                                builder2.show();

                            } else if (serverResponse.getJSONObject(0).getString("Status").trim().equalsIgnoreCase("Success")) {


                                /*dba.open();
                                String where = "id=?";
                                String[] whereArgs = new String[] {String.valueOf(ID)};
                                DataBaseAdapter.ourDatabase.update(CustomerTable.DATABASE_TABLE,cvup,where,whereArgs);

                                dba.close();

*/
                                AlertDialog.Builder builder2 = new AlertDialog.Builder(
                                        AddCustomerActivity.this);
                                builder2.setTitle("Alert....!!");
                                if (KEY.equalsIgnoreCase("update")) {
                                    builder2.setMessage("Customer Updated Successfully...");

                                } else {
                                    builder2.setMessage("Customer Added Successfully...");
                                }
                                builder2.setPositiveButton("OK",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(
                                                    DialogInterface dialog,
                                                    int which) {
                                                Log.e("info", "OK");
                                                new SyncData().execute();
                                                finish();
                                            }
                                        });
                                builder2.show();

                            }

                        }
                        // Error status is true
                    } else {

                        Toast.makeText(AddCustomerActivity.this, "Server Error",
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


    class SyncData extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... strings) {
            try {
                dba.open();
                new Sync().syncdata(AddCustomerActivity.this);
                dba.close();
            } catch (Exception e) {
                e.printStackTrace();
            }


            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

        }
    }

}
