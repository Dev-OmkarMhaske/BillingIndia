package com.tsysinfo.billing;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.tsysinfo.billing.database.CustomerTable;
import com.tsysinfo.billing.database.DataBaseAdapter;
import com.tsysinfo.billing.database.DeliveryTable;
import com.tsysinfo.billing.database.IPTable;
import com.tsysinfo.billing.database.Models;
import com.tsysinfo.billing.database.ProductTable;
import com.tsysinfo.billing.database.ReceiptTable;
import com.tsysinfo.billing.database.SalesTypeTable;
import com.tsysinfo.billing.database.SessionLocation;
import com.tsysinfo.billing.database.SessionManager;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class SetIPActivity extends Activity {

	EditText ipAddress, portNo,wcfPortNo;
	Button btnSetIp;
	String IPAddress, PORTNo, WCFPortNo;

	DataBaseAdapter dba;
	Models mod;
	ProgressDialog progressDialog;

    SessionManager session;
    SessionLocation locSession;
    private String FINIP;



    private class MyTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            progressDialog= new ProgressDialog(SetIPActivity.this);
            progressDialog.setMessage("Please Wait..");
            progressDialog.show();

        }

        @Override
        protected Boolean doInBackground(String... params) {

          return  isServerReachable(SetIPActivity.this,params[0]);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            boolean bResponse = result;
            progressDialog.dismiss();
            if (bResponse==true)
            {
                ContentValues cv = new ContentValues();
                cv.put(IPTable.KEY_IPADDRESS, IPAddress);
                cv.put(IPTable.KEY_WCF_PORT, WCFPortNo);
                cv.put(IPTable.KEY_PORT, PORTNo);

                dba.open();
                mod.clearDatabase(IPTable.DATABASE_TABLE);
                mod.insertdata(IPTable.DATABASE_TABLE, cv);

                mod.clearDatabase(ProductTable.DATABASE_TABLE);
                mod.clearDatabase(CustomerTable.DATABASE_TABLE);
                mod.clearDatabase(DeliveryTable.DATABASE_TABLE);
                mod.clearDatabase(ReceiptTable.DATABASE_TABLE);
                mod.clearDatabase(SalesTypeTable.DATABASE_TABLE);
                dba.close();

                session.logoutUser();
                locSession.logoutUser();

                Toast.makeText(getApplicationContext(),
                        "Your IP Set Successfully!!!", Toast.LENGTH_LONG)
                        .show();

                Intent chngInt = new Intent(getApplicationContext(),
                        SplashActivity.class);
                startActivity(chngInt);
                finish();
            }
            else
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(
                        SetIPActivity.this);
                builder.setTitle("Error");

                builder.setMessage("It Seems to entered URL is invalid. Please Enter Valid Url.")
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

            }
        }
    }
    static public boolean isServerReachable(Context context,String url) {
        ConnectivityManager connMan = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = connMan.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected()) {
            try {
                URL urlServer = new URL(url);
                HttpURLConnection urlConn = (HttpURLConnection) urlServer.openConnection();
                urlConn.setConnectTimeout(3000); //<- 3Seconds Timeout
                urlConn.connect();
                if (urlConn.getResponseCode() == 200) {
                    return true;
                } else {
                    return false;
                }
            } catch (MalformedURLException e1) {
                return false;
            } catch (IOException e) {
                return false;
            }
        }
        return false;
    }


    public static boolean exists(String URLName) {

        try {
            HttpURLConnection.setFollowRedirects(false);
            // note : you may also need
            // HttpURLConnection.setInstanceFollowRedirects(false)
            HttpURLConnection con = (HttpURLConnection) new URL(URLName)
                    .openConnection();
            con.setRequestMethod("HEAD");
            return (con.getResponseCode() == HttpURLConnection.HTTP_OK);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setip_activity);
		
		dba = new DataBaseAdapter(this);
		mod = new Models();
        session = new SessionManager(this);
        locSession = new SessionLocation(this);

		ipAddress = (EditText) findViewById(R.id.textIPAddress);
		portNo = (EditText) findViewById(R.id.textPortNo);
		btnSetIp = (Button) findViewById(R.id.buttonSetIp);
		wcfPortNo = (EditText) findViewById(R.id.textWcfPortNo);

		
		btnSetIp.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {


			    FINIP="";
				// TODO Auto-generated method stub
				IPAddress = ipAddress.getText().toString().trim();
				PORTNo = portNo.getText().toString().trim();
                WCFPortNo = wcfPortNo.getText().toString().trim();

               if(IPAddress.length() > 0 ) {

                   if (IPAddress.startsWith("http://") || IPAddress.startsWith("www"))
                   {
                       FINIP=IPAddress;
                   }else {
                       FINIP="http://"+IPAddress;
                   }

                  new MyTask().execute(FINIP);
             }else{
                    Toast.makeText(getApplicationContext(),"Please Enter URL/Ip Address",Toast.LENGTH_LONG).show();
                }


                    }
                });



	}

	
}
