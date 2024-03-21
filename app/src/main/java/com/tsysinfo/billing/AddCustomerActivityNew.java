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
import com.tsysinfo.billing.database.TownTable;
import com.tsysinfo.billing.database.WebService;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class AddCustomerActivityNew extends AppCompatActivity {

    static boolean errored = false;
    public Location mLastLocation;
    EditText editName, editPlace, editAddress, editRegNo, editTIN, editShortName,
            editLongitude, editLatitude, editMobileNo, editLandLineNo, editCustomerCode,editEmailId,editRateCode,editPan,add2,add3;
    Spinner spinnerTown, spinnerCustomerType;
    CheckBox chekBox;
    String strName, strPlace, strAddress1,strAddress2,strAddress3, strRegNo, strRoutName, strPIN, strCST, strShortName,
            strLongitude, strLatitude, strMobileNo, strLandLineNo, strCustomerCode,
            strTown, strCustType, Data = "",district,emailId;
    Button btnSave, btnClear;
    ArrayList<String> arrTown, arrCustType, arrIsActive,arraDist;
    ArrayAdapter<String> adapterTown, adapterCustType, adapterIsActive;
    SessionManager session;
    DataBaseAdapter dba;
    Models mod;
    ProgressDialog dialog, waitDialog;
    JSONArray serverResponse;
    private String DistrictId="0";
    private String KEY;
    private String ID;
    ContentValues cvup=new ContentValues();
    private int pos;

    String globtown;
    private String strPan="";
    private String strRateCode="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_customer_new);

        // References
        session = new SessionManager(this);
        dba = new DataBaseAdapter(this);
        mod = new Models();

        waitDialog = new ProgressDialog(this);
        waitDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        waitDialog.setMessage("Please Wait");

        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        editName = (EditText) findViewById(R.id.editName);
        editPlace = (EditText) findViewById(R.id.editPlace);
        editAddress = (EditText) findViewById(R.id.editAddress1);
        editRegNo = (EditText) findViewById(R.id.editRegNo);
        editTIN = (EditText) findViewById(R.id.editTIN);
          editShortName = (EditText) findViewById(R.id.editShortName);
        editLongitude = (EditText) findViewById(R.id.editLongitude);
        editLatitude = (EditText) findViewById(R.id.editLatitude);
        editMobileNo = (EditText) findViewById(R.id.editMobileNo);
        editRateCode=(EditText)findViewById(R.id.editRateCode) ;
        editPan=(EditText)findViewById(R.id.editPan) ;
        add2=(EditText)findViewById(R.id.editAddress2) ;
        add3=(EditText)findViewById(R.id.editAddress3) ;
        editLandLineNo = (EditText) findViewById(R.id.editLandLineMobileNo);
        editEmailId = (EditText) findViewById(R.id.editEmailId);
        editCustomerCode = (EditText) findViewById(R.id.editCustomerCode);
        spinnerTown = (Spinner) findViewById(R.id.spinnerTown);
         spinnerCustomerType = (Spinner) findViewById(R.id.spinnerCustType);


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

        adapterIsActive = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, arrIsActive);
        adapterIsActive
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);




                setTown();

                dba.open();

        arrCustType = new ArrayList<String>();
        String sqlCustType = "select distinct " + CustomerTypeTable.KEY_CUSTTYPE + " from " + CustomerTypeTable.DATABASE_TABLE;
        final Cursor curCustType = DataBaseAdapter.ourDatabase.rawQuery(sqlCustType, null);
        arrCustType.add("Select Type");
        while (curCustType.moveToNext()) {
            arrCustType.add(curCustType.getString(0).trim());
        }
        curCustType.close();

        adapterCustType = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, arrCustType);
        adapterCustType
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCustomerType.setAdapter(adapterCustType);
        dba.close();

        chekBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (chekBox.isChecked()) {
                    editLongitude.setEnabled(false);
                    editLatitude.setEnabled(false);
                    GPSTracker gps = new GPSTracker(AddCustomerActivityNew.this);
                    mLastLocation = LocationServices.FusedLocationApi
                            .getLastLocation(MainActivity.mGoogleApiClient);

                    if (gps.canGetLocation()) {
                        double currLatitude = gps.getLatitude();
                        double currLongitude = gps.getLongitude();
                        if (currLatitude == 0.0 && currLongitude == 0.0) {
                            Toast.makeText(AddCustomerActivityNew.this, "Not able to connect GPS\nPlease Move to the Open Space"+currLongitude,
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

try {


                strName = editName.getText().toString().trim();
                strPlace = editPlace.getText().toString().trim();
                strAddress1 = editAddress.getText().toString().trim();
                strAddress2 = add2.getText().toString().trim();
                strAddress3 = add3.getText().toString().trim();

                strRegNo = editRegNo.getText().toString().trim();
                strPan=editPan.getText().toString();

                strTown = spinnerTown.getSelectedItem().toString().trim();
                strCustType = spinnerCustomerType.getSelectedItem().toString().trim();
                strRateCode=editRateCode.getText().toString().trim();
                strLatitude = editLatitude.getText().toString().trim();
                strLongitude = editLongitude.getText().toString().trim();
                emailId=editEmailId.getText().toString();
                strMobileNo = editMobileNo.getText().toString().trim();
                strLandLineNo = editLandLineNo.getText().toString().trim();
                strShortName = editShortName.getText().toString().trim();
                strRoutName = editTIN.getText().toString().trim();
                strCustomerCode = editCustomerCode.getText().toString().trim();



                int Chkflag = 0;
                int fwd = 0;
                if (chekBox.isChecked()) {
                    Chkflag = 1;
                } else {
                    Chkflag = 0;
                }

                if (strName.length() > 0) {


                        if (emailId.length() > 0)
                        {
                            if (strPlace.length() > 0) {
                                if (strAddress1.length() > 0) {
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








                                                        Data = strName + "$" + strPlace + "$" + strAddress1 + "$" + strAddress2 + "$"
                                                                + strAddress3 + "$$" + strRegNo + "$" + strPan + "$$"
                                                                + areaId + "$" + custTypeId + "$" + strRateCode + "$" + strLatitude + "$"
                                                                + strLongitude + "$"+ emailId+"$" + strMobileNo + "$" + strLandLineNo + "$" + strShortName+"$"+strRoutName+"$"+strCustomerCode+"$";

                                                        AsyncCallWS task = new AsyncCallWS();
                                                        // Call execute
                                                        task.execute();




                                            }
                                            else
                                            {
                                                Toast.makeText(AddCustomerActivityNew.this, "Please check for the current location.", Toast.LENGTH_SHORT).show();
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


                } else {
                    Toast.makeText(getApplicationContext(), "Enter Name", Toast.LENGTH_LONG).show();
                }





}catch (Exception e )
{
    e.printStackTrace();
}





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
    public void setTown()
    {
        dba.open();

        arrTown = new ArrayList<String>();
        String sqlTown = "select distinct " + TownTable.KEY_TOWN + " from " + TownTable.DATABASE_TABLE +" " ;
        Cursor curTown = DataBaseAdapter.ourDatabase.rawQuery(sqlTown, null);
        arrTown.add("Select Town");
        while (curTown.moveToNext()) {
            arrTown.add(curTown.getString(0).trim());
        }
        curTown.close();

        dba.close();
        adapterTown = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, arrTown);
        adapterTown
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
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

        Log.w("AddCustomerActivityNew", "Start");
        dba.open();
        serverResponse = WebService.customerRegistration(session.getBranchNo(), Data, "SaveCustomer");
        dba.close();


            } catch (Exception e) {
                Log.w("AddCustomerActivityNew", "Timeout ");
            }
            return null;
        }

        @SuppressLint("ResourceAsColor")
        @Override
        // Once WebService returns response
        protected void onPostExecute(Void result) {

            Log.w("AddCustomerActivityNew", "TimeOutFlag : " + WebService.timeoutFlag);
            Log.w("AddCustomerActivityNew", "ResponseString : "
                    + WebService.responseString);

            // Make Progress Bar invisible
            waitDialog.cancel();
            Log.w("AddCustomerActivityNew", "Dialog Closed");
            if (WebService.timeoutFlag == 1) {
                Log.w("CartActivity", "Timeout");
                AlertDialog.Builder builder = new AlertDialog.Builder(
                        AddCustomerActivityNew.this);
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
                Log.w("AddCustomerActivity", "Try");
                try {


                    // Error status is false
                    if (!errored) {
                        // Based on Boolean value returned from WebService
                        if (serverResponse != null) {

                            if (serverResponse.getJSONObject(0)
                                    .getString("Status").trim()
                                    .equalsIgnoreCase("Failed")) {
/*
                                final AlertDialog.Builder builder2 = new AlertDialog.Builder(
                                        AddCustomerActivityNew.this);
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
                                builder2.show();*/
                                Toast.makeText(AddCustomerActivityNew.this, "Added successful", Toast.LENGTH_SHORT).show();
                                finish();

                            } else if (serverResponse.getJSONObject(0)
                                    .getString("Status").trim()
                                    .equalsIgnoreCase("Success")) {


                                /*dba.open();
                                String where = "id=?";
                                String[] whereArgs = new String[] {String.valueOf(ID)};
                                DataBaseAdapter.ourDatabase.update(CustomerTable.DATABASE_TABLE,cvup,where,whereArgs);

                                dba.close();

*/
                                AlertDialog.Builder builder2 = new AlertDialog.Builder(
                                        AddCustomerActivityNew.this);
                                builder2.setTitle("Alert....!!");
                                if(KEY.equalsIgnoreCase("update"))
                                {
                                    builder2.setMessage("Customer Updated Successfully...");

                                }else {builder2.setMessage("Customer Added Successfully...");
                                }
                                builder2.setPositiveButton("OK",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(
                                                    DialogInterface dialog,
                                                    int which) {
                                                Log.e("info", "OK");
                                                finish();
                                            }
                                        });
                                builder2.show();

                            }

                        }

                        // Error status is true
                    } else {

                        Toast.makeText(AddCustomerActivityNew.this, "Server Error",
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
