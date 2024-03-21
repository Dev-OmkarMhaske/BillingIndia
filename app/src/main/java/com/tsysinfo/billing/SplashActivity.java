package com.tsysinfo.billing;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.tsysinfo.billing.database.DataBaseAdapter;
import com.tsysinfo.billing.database.Models;

import java.util.ArrayList;
import java.util.List;

@SuppressLint("NewApi")
public class SplashActivity extends Activity {

	DataBaseAdapter dba;
	Models mod;
	/**
	 * ATTENTION: This was auto-generated to implement the App Indexing API.
	 * See https://g.co/AppIndexing/AndroidStudio for more information.
	 */
	private GoogleApiClient client;
	private GPSTracker gps;

/*
	<uses-permission android:name="android.permission.BLUETOOTH" />
	<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
	<uses-permission android:name="android.permission.READ_PHONE_STATE" />
	<uses-permission android:name="android.permission.SEND_SMS" />
	<uses-permission android:name="android.permission.READ_LOGS" />
	<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
	<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
	<uses-permission android:name="info.devexchanges.googlelocation.permission.MAPS_RECEIVE" />
	<uses-permission android:name="android.permission.INTERNET" />
	<uses-permission android:name="android.permission.CHANGE_NETWORK_STATE" />
	<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
	<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
	<uses-permission android:name="android.permission.ACCESS_LOCATION" />
	<uses-permission android:name="android.permission.ACCESS_GPS" />
	<uses-permission android:name="android.permission.CAMERA" />
	<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />*/

	private static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;
	private boolean checkAndRequestPermissions() {
		Context context=getApplicationContext();
		int locationpermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION);
		int readphonestatepermission = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE);
		int permissionSendMessage = ContextCompat.checkSelfPermission(context,Manifest.permission.SEND_SMS);
		int writepermission = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
		int readexternalstoragepermission = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE);
		int coarsLocpermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION);
		int CAMERA = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA);


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
		if (CAMERA != PackageManager.PERMISSION_GRANTED) {
			listPermissionsNeeded.add(Manifest.permission.CAMERA);
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

							//Toast.makeText(SplashActivity.this, "Please Restart the Application....... ", Toast.LENGTH_LONG).show();
						}
					}else if (permissions[i].equals(Manifest.permission.READ_LOGS)) {
						if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
							Log.e("msg", "read logs granted");


						}
					}else if (permissions[i].equals(Manifest.permission.CAMERA)) {
						if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
							Log.e("msg", "read logs granted");


						}
					}else if (permissions[i].equals(Manifest.permission.ACCESS_COARSE_LOCATION)) {
						if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
							Log.e("msg", "Coarse location granted ");
							Intent im = getBaseContext().getPackageManager()
									.getLaunchIntentForPackage( getBaseContext().getPackageName() );
							im.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							startActivity(im);
							finish();



						}
					}


				}

			}


		}
	}
	@Override
	protected void onCreate(Bundle v) {
		// TODO Auto-generated method stub
		super.onCreate(v);
		setContentView(R.layout.splash);
		dba = new DataBaseAdapter(this);
		mod = new Models();

		//getActionBar().hide();
		//getSupportActionBar().hide();
		client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
		if (!checkAndRequestPermissions()) {

			Toast.makeText(SplashActivity.this, "You should have to grant all permissions", Toast.LENGTH_SHORT).show();

			//finish();
		}else
		{


		startService(new Intent(SplashActivity.this, UpdateService.class));

		Thread timer = new Thread() {

			@Override
			public void run() {

				try {

					sleep(4000);

				} catch (InterruptedException e) {

					e.printStackTrace();

				} finally {
					dba.open();
					Cursor cur = mod.getData("iptable");
					if (cur.getCount() > 0) {

						Intent openstart = new Intent(getApplicationContext(),MainActivity.class);
						startActivity(openstart);
						finish();

					} else {

						Intent openstart = new Intent(getApplicationContext(), SetIPActivity.class);
						startActivity(openstart);
						finish();
					}
					cur.close();
					dba.close();
				}

			}
		};

		timer.start();

		// ATTENTION: This was auto-generated to implement the App Indexing API.
		// See https://g.co/AppIndexing/AndroidStudio for more information.

	}
	}

	@Override
	public void onStart() {
		super.onStart();

		// ATTENTION: This was auto-generated to implement the App Indexing API.
		// See https://g.co/AppIndexing/AndroidStudio for more information.
		client.connect();
		Action viewAction = Action.newAction(
				Action.TYPE_VIEW, // TODO: choose an action type.
				"Splash Page", // TODO: Define a title for the content shown.
				// TODO: If you have web page content that matches this app activity's content,
				// make sure this auto-generated web page URL is correct.
				// Otherwise, set the URL to null.
				Uri.parse("http://host/path"),
				// TODO: Make sure this auto-generated app deep link URI is correct.
				Uri.parse("android-app://com.tsysinfo.billing/http/host/path")
		);
		AppIndex.AppIndexApi.start(client, viewAction);
	}

	@Override
	public void onStop() {
		super.onStop();

		// ATTENTION: This was auto-generated to implement the App Indexing API.
		// See https://g.co/AppIndexing/AndroidStudio for more information.
		Action viewAction = Action.newAction(
				Action.TYPE_VIEW, // TODO: choose an action type.
				"Splash Page", // TODO: Define a title for the content shown.
				// TODO: If you have web page content that matches this app activity's content,
				// make sure this auto-generated web page URL is correct.
				// Otherwise, set the URL to null.
				Uri.parse("http://host/path"),
				// TODO: Make sure this auto-generated app deep link URI is correct.
				Uri.parse("android-app://com.tsysinfo.billing/http/host/path")
		);
		AppIndex.AppIndexApi.end(client, viewAction);
		client.disconnect();
	}

}
