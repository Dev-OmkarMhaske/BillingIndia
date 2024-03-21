package com.tsysinfo.billing;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
 
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class PermissionActivityActivity extends AppCompatActivity {

    private static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;
    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean checkAndRequestPermissions() {
        Context context=getApplicationContext();
        int locationpermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION);
        int readphonestatepermission = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE);
        int permissionSendMessage = ContextCompat.checkSelfPermission(context,Manifest.permission.SEND_SMS);
        int writepermission = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int readexternalstoragepermission = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE);
        int coarsLocpermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION);


        List<String> listPermissionsNeeded = new ArrayList<>();

        if (locationpermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if (readphonestatepermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (permissionSendMessage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.SEND_SMS);
        }

        if (writepermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        if (readexternalstoragepermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (coarsLocpermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }
        if (!listPermissionsNeeded.isEmpty()) {
            requestPermissions(listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), REQUEST_ID_MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);


        if (requestCode == REQUEST_ID_MULTIPLE_PERMISSIONS) {



            if (grantResults.length > 0) {
                for (int i = 0; i < permissions.length; i++) {


                    if (permissions[i].equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            Log.e("msg", "location granted");


                        }
                    } else if (permissions[i].equals(Manifest.permission.READ_PHONE_STATE)) {
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            Log.e("msg", "read phone state granted");


                        }
                    } else if (permissions[i].equals(Manifest.permission.SEND_SMS)) {
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            Log.e("msg", "sms granted");


                        }

                    }else if (permissions[i].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            Log.e("msg", "write external granted");


                        }
                    }else if (permissions[i].equals(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            Log.e("msg", "read external storage granted");

                            Toast.makeText(PermissionActivityActivity.this, "Please Restart the Application....... ", Toast.LENGTH_LONG).show();
                        }
                    }else if (permissions[i].equals(Manifest.permission.READ_LOGS)) {
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            Log.e("msg", "read logs granted");



                        }
                    }else if (permissions[i].equals(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            Log.e("msg", "Coarse location granted ");



                        }
                    }


                }

            }


        }
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission_activity);
        if (!checkAndRequestPermissions()) {

            Toast.makeText(PermissionActivityActivity.this, "You should have to grant all permissions", Toast.LENGTH_SHORT).show();

            //finish();
        }else
        {
            finish();
            
        }
    }
}
