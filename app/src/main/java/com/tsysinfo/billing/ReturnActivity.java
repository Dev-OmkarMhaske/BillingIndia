package com.tsysinfo.billing;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources.NotFoundException;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.view.MenuItemCompat;
 
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.LocationServices;
import com.tsysinfo.billing.database.CustomerTable;
import com.tsysinfo.billing.database.DataBaseAdapter;
import com.tsysinfo.billing.database.Models;
import com.tsysinfo.billing.database.ProductTable;
import com.tsysinfo.billing.database.SessionManager;
import com.tsysinfo.billing.database.WebService;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ReturnActivity extends AppCompatActivity {

	// private int count = 0;
	ImageView btnAddToCart;

	static Spinner spinCustomer;

	String act = "return";
	ArrayAdapter<String> adapterProductCat;
	static String custid = "";
	EditText editBillNo;

	String Data = "",ReturnNo="";

	Double currLatitude;
	Double currLongitude;

	SessionManager session;
	DataBaseAdapter dba;
	Models mod;
	ProgressDialog waitDialog;
	JSONArray serverResponse;
	static boolean errored = false;

	public Location mLastLocation;

	String longi = "",lati = "";

    ToastService toastService;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.return_list_activity);

		// References
		session = new SessionManager(this);
		dba = new DataBaseAdapter(this);
		mod = new Models();

		waitDialog = new ProgressDialog(this);
		waitDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		waitDialog.setMessage("Please Wait");

		getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);


		editBillNo = (EditText) findViewById(R.id.textBillNo);
        spinCustomer = (Spinner) findViewById(R.id.spinnerCustomer);

		ArrayList<ProductSearchResults> ProductSearchResults = GetProductSearchResults();

		final ListView lv = (ListView) findViewById(R.id.list);
		lv.setAdapter(new ReturnBaseAdapter(this, ProductSearchResults));
		btnAddToCart = (ImageView) findViewById(R.id.productAddToCart);
        toastService = new ToastService(ReturnActivity.this);

        try {


            GPSTracker gps = new GPSTracker(this);


            // check if GPS enabled
            if (gps.canGetLocation()) {

                mLastLocation = LocationServices.FusedLocationApi
                        .getLastLocation(MainActivity.mGoogleApiClient);

                currLatitude = gps.getLatitude();
                currLongitude = gps.getLongitude();


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
                ArrayAdapter<String> adapterRelation = new ArrayAdapter<String>(this,
                        android.R.layout.simple_spinner_item, customers);
                adapterRelation
                        .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinCustomer.setAdapter(adapterRelation);


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
                                    spinCustomer.setSelection(i);
                                    dba.open();
                                    String sq = "select id from customer where name='" + custCur.getString(0).trim() + "'";
                                    Cursor c = DataBaseAdapter.ourDatabase.rawQuery(sq, null);
                                    if (c.getCount() > 0) {
                                        c.moveToFirst();
                                        custid = c.getString(0).trim();
                                    }
                                    c.close();
                                    dba.close();
                                    LocFlg = 1;
                                    break;
                                }
                            }
                        }
                    }

                    if(LocFlg == 0){
                        Toast.makeText(ReturnActivity.this, "Unable to connect to GPS Please Select Customer Manually", Toast.LENGTH_SHORT).show();
                    }
                }
                custCur.close();
                dba.close();

            }else{
                toastService.startTimer();
            }
            List<String> category = new ArrayList<String>();

            String sql2 = "select distinct brand from products";
            dba.open();
            Cursor curBra = DataBaseAdapter.ourDatabase.rawQuery(sql2, null);
            if (curBra.getCount() > 0) {
                category.add("All");
                while (curBra.moveToNext()) {
                    category.add(curBra.getString(0).trim());
                }
            }
            dba.close();


            adapterProductCat = new ArrayAdapter<String>(this,
                    android.R.layout.simple_spinner_item, category);
            adapterProductCat
                    .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        }catch (Exception e){
            e.printStackTrace();
        }


        spinCustomer.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                String custName = spinCustomer.getSelectedItem().toString().trim();
                if (!custName.equalsIgnoreCase("Select Customer")) {
                    dba.open();
                    String sql1 = "select id from customer where name='" + custName + "'";
                    Cursor cur1 = DataBaseAdapter.ourDatabase.rawQuery(sql1, null);
                    if (cur1.getCount() > 0) {
                        cur1.moveToFirst();
                        custid = cur1.getString(0).trim();
                    }
                    cur1.close();
                    dba.close();
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

	}




	private ArrayList<ProductSearchResults> GetProductSearchResults() {
		ArrayList<ProductSearchResults> results = new ArrayList<ProductSearchResults>();

        try{
		ProductSearchResults sr = new ProductSearchResults();

		dba.open();
		Cursor cur = mod.getData(ProductTable.DATABASE_TABLE);

		while (cur.moveToNext()) {

			sr = new ProductSearchResults();
			sr.setID(cur.getString(0).trim());
			sr.setName(cur.getString(1).trim());
			sr.setDescription(cur.getString(2).trim());
			sr.setPrice(cur.getString(7).trim());
			sr.setImage(cur.getString(10).trim());
			sr.setAct(act);
			results.add(sr);
		}
		cur.close();
		dba.close();
        }catch (Exception e){
            e.printStackTrace();
        }
		return results;
	}

	@SuppressLint("NewApi")
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeAsUpIndicator(R.drawable.back);

		MenuItem menuItem = menu.findItem(R.id.actionCart);
		menuItem.setIcon(buildCounterDrawable(R.drawable.return_cart));

		MenuItem item = menu.findItem(R.id.spinner);
		Spinner spinner = (Spinner) MenuItemCompat.getActionView(item);
		spinner.setAdapter(adapterProductCat);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case android.R.id.home:
			onBackPressed();
			return true;

		case R.id.actionCart:

			String billNo = editBillNo.getText().toString().trim();
            String custName111 = spinCustomer.getSelectedItem().toString().trim();

            if(!custName111.equalsIgnoreCase("Select Customer")) {

                if (billNo.length() > 0) {

                    dba.open();
                    String sql = "select * from retutntab";
                    Cursor cur = DataBaseAdapter.ourDatabase.rawQuery(sql, null);
                    if (cur.getCount() > 0) {

						Intent intent = new Intent(ReturnActivity.this,ReturnInvoice.class);
						intent.putExtra("custName",custName111);
						intent.putExtra("billNo",billNo);
						startActivity(intent);
						finish();


                    } else {
                        Toast.makeText(ReturnActivity.this, "Add Item to cart first",
                                Toast.LENGTH_LONG).show();
                    }
                    cur.close();
                    dba.close();
                } else {
                    Toast.makeText(ReturnActivity.this, "Enter Bill No",
                            Toast.LENGTH_LONG).show();
                }
            }
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private Drawable buildCounterDrawable(int backgroundImageId) {
		LayoutInflater inflater = LayoutInflater.from(this);
		View view = inflater.inflate(R.layout.cart_chekout_layout, null);
		view.setBackgroundResource(backgroundImageId);

		int count = 0;
		dba.open();
		String sql = "select * from retutntab";
		Cursor cur = DataBaseAdapter.ourDatabase.rawQuery(sql, null);
		if (cur.getCount() > 0) {
			count = cur.getCount();
		} else {
			count = 0;
		}
		cur.close();
		dba.close();

		if (count == 0) {
			View counterTextPanel = view.findViewById(R.id.counterValuePanel);
			counterTextPanel.setVisibility(View.GONE);
		} else {
			TextView textView = (TextView) view.findViewById(R.id.count);
			textView.setText("" + count);
		}

		view.measure(View.MeasureSpec.makeMeasureSpec(0,
				View.MeasureSpec.UNSPECIFIED), View.MeasureSpec
				.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
		view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());

		view.setDrawingCacheEnabled(true);
		view.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
		Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache());
		view.setDrawingCacheEnabled(false);

		return new BitmapDrawable(getResources(), bitmap);
	}

	void doIncrease() {
		// count++;
		invalidateOptionsMenu();
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		Intent intent = new Intent(getApplicationContext(), MainActivity.class);
		startActivity(intent);
		finish();
	}

	// WebService Code

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
						ReturnActivity.this);
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
										ReturnActivity.this);
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

								Toast.makeText(ReturnActivity.this,
										"Data Successfully Saved!!!",
										Toast.LENGTH_LONG);

								ReturnNo = serverResponse.getJSONObject(0)
										.getString("SaleReturnNo").trim();
								
								dba.open();
								Cursor ip = mod.getData("iptable");
								ip.moveToFirst();
								String ipadd = ip.getString(0).trim();
								String port = ip.getString(2).trim();
								String PreFix = "http://" + ipadd + ":"+ port;
								ip.close();
								dba.close();
								String url = PreFix + "/SalesReturn/" + ReturnNo + ".pdf";
								
								new DownloadFile().execute(url, ReturnNo + ".pdf");
								
							}

						}

						// Error status is true
					} else {

						Toast.makeText(ReturnActivity.this, "Server Error",
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
			
			Intent intent = new Intent(
					getApplicationContext(),
					MainActivity.class);
			startActivity(intent);
			finish();

			File pdfFile = new File(Environment.getExternalStorageDirectory()
					+ "/ebilling_images/SalesReturn/" + ReturnNo + "1.png");
			Uri path = Uri.fromFile(pdfFile);
			Intent pdfIntent = new Intent(Intent.ACTION_VIEW);
			pdfIntent.setDataAndType(path, "application/pdf");
			pdfIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

			try {
				startActivity(pdfIntent);
			} catch (ActivityNotFoundException e) {
				Toast.makeText(ReturnActivity.this,
						"No Application available to view PDF",
						Toast.LENGTH_SHORT).show();
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
