package com.tsysinfo.billing;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources.NotFoundException;
import android.database.Cursor;
import android.database.SQLException;
import android.location.Location;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
 
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.LocationServices;
import com.tsysinfo.billing.barcodes.FullScannerActivity;
import com.tsysinfo.billing.database.AllCustomer;
import com.tsysinfo.billing.database.BillingTable;
import com.tsysinfo.billing.database.CustomerTable;
import com.tsysinfo.billing.database.DataBaseAdapter;
import com.tsysinfo.billing.database.Models;
import com.tsysinfo.billing.database.OfflineTable;
import com.tsysinfo.billing.database.OrderTable;
import com.tsysinfo.billing.database.OrderTempTable;
import com.tsysinfo.billing.database.SessionManager;
import com.tsysinfo.billing.database.WebService;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class UNCartActivity extends AppCompatActivity {

	static String act = "";
	static Spinner spinCustomer;
	static TextView txtDeliveryDate;
	String Data = "";
	Double currLatitude;
	Double currLongitude;

//	Double distMiles=0.00932057;

	String mode="";

	SessionManager session;
	DataBaseAdapter dba;
	Models mod;
	ProgressDialog waitDialog;
	JSONArray serverResponse;
	static boolean errored = false;

	public static double adapterGst=0.0,adapterGross=0.0;
	public Location mLastLocation;

	ToastService toastService;

    String longi = "",lati = "";
	private String GLOBAL_CUST_ID;
	private String custId="";
	private TextView textViewGrs;
	private TextView textViewTot;
	private TextView textViewGst;

	double grs=0.0;
	double gst=0.0;

	/*@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		act = getIntent().getStringExtra("act");

		if (act.equalsIgnoreCase("order")) {
			setContentView(R.layout.order_cart_list_activity);
			txtDeliveryDate = (TextView) findViewById(R.id.deliveryDate);

			txtDeliveryDate.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					Calendar mcurrentDate = Calendar.getInstance();
					int mYear = mcurrentDate.get(Calendar.YEAR);
					int mMonth = mcurrentDate.get(Calendar.MONTH);
					int mDay = mcurrentDate.get(Calendar.DAY_OF_MONTH);

					DatePickerDialog mDatePicker = new DatePickerDialog(UNCartActivity.this, new OnDateSetListener() {
								public void onDateSet(DatePicker datepicker,
										int selectedyear, int selectedmonth,
										int selectedday) {
									// TODO Auto-generated method stub
									selectedmonth = selectedmonth + 1;

									String stringFromDate = String
											.valueOf(selectedmonth)
											+ "/"
											+ String.valueOf(selectedday)
											+ "/"
											+ String.valueOf(selectedyear);
									txtDeliveryDate.setText(stringFromDate);

								}
							}, mYear, mMonth, mDay);
					mDatePicker.setTitle("Select Delivery date");
					mDatePicker.show();
				}
			});

		} else {
			setContentView(R.layout.cart_list_activity);
		}

		ImageView barCode=(ImageView)findViewById(R.id.imageButtonBarcode);
		barCode.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(UNCartActivity.this,FullScannerActivity.class);
				spinCustomer.setSelection(0);
				startActivityForResult(intent, 1);
			}
		});


		// References
		session = new SessionManager(this);
		dba = new DataBaseAdapter(this);
		mod = new Models();

		toastService = new ToastService(UNCartActivity.this);

		waitDialog = new ProgressDialog(this);
		waitDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		waitDialog.setMessage("Please Wait");

		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		spinCustomer = (Spinner) findViewById(R.id.spinnerCustomer);



		spinCustomer.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

				if (!spinCustomer.getSelectedItem().toString().equalsIgnoreCase("Select Customer")) {
					dba.open();
					String custDet = "select id from all_customer where name='"
							+ spinCustomer.getSelectedItem().toString() + "'";
					Cursor cur = DataBaseAdapter.ourDatabase.rawQuery(custDet,
							null);
					if (cur.getCount() > 0) {
						cur.moveToFirst();
						custId = cur.getString(0).trim();
					}

					mode="Manual";
					dba.close();

				}
				else {
					custId="";

				}
			}
			@Override
			public void onNothingSelected(AdapterView<?> adapterView) {

			}
		});
	//	txtCustomer = (TextView) findViewById(R.id.txtCustomer);


        dba.open();
        ArrayList<String> customers = new ArrayList<String>();
        String sql1 = "select " + AllCustomer.KEY_NAME + " from "
                + AllCustomer.DATABASE_TABLE;
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
                    String sql = "select " + AllCustomer.KEY_NAME + "," + AllCustomer.KEY_LONGI + "," + AllCustomer.KEY_LATI + " from "
                            + AllCustomer.DATABASE_TABLE;
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

								
                                for (int i = 0; i < spinCustomer.getAdapter()
                                        .getCount(); i++) {
                                    if (spinCustomer.getAdapter().getItem(i)
                                            .toString().contains(custCur.getString(0).trim())) {
                                        spinCustomer.setSelection(i);
                                        LocFlg = 1;
										mode="Auto";
                                        break;
                                    }
                                }
                            }
                        }
                        if(LocFlg == 0){
                            Toast.makeText(UNCartActivity.this, "Unable to connect to GPS Please Select Customer Manually", Toast.LENGTH_SHORT).show();
                        }

                    }
                    custCur.close();
                    dba.close();
                }else{
                    toastService.startTimer();
                }

			}


			ArrayList<ProductSearchResults> productSearchResults = GetProductSearchResults();
			Calendar cal = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
			String strDate = sdf.format(cal.getTime());
			dba.open();


			for(int i=0;i<productSearchResults.size();i++)
			{

				if (productSearchResults.get(i).getUnit().equals("PCS") || productSearchResults.get(i).getUnit().equals("SET") ||  productSearchResults.get(i).getUnit().equals("DOZ"))
				{

					grs= grs + Double.parseDouble(productSearchResults.get(i).getQuantity()) * Double.parseDouble(productSearchResults.get(i).getDp());

					double taxamt=Double.parseDouble(productSearchResults.get(i).getQuantity()) * Double.parseDouble(productSearchResults.get(i).getDp());
					taxamt = Math.round(taxamt * 100.0) / 100.0;
					double TotTaxAmt=Double.parseDouble(productSearchResults.get(i).getRot());
					double singgst= ((taxamt*TotTaxAmt)/100);

					gst= gst + singgst;

					ContentValues cv = new ContentValues();
					cv.put(OrderTable.KEY_DATE, strDate);
					cv.put(OrderTable.KEY_CUSTID, "");
					cv.put(OrderTable.KEY_PRODUCTID, productSearchResults.get(i).getId());
					cv.put(OrderTable.KEY_QUANTITY,
							productSearchResults.get(i).getQuantity());

					Log.w("quantuity",productSearchResults.get(i).getQuantity());
					cv.put(OrderTable.KEY_EMPNO, session.getEmpNo());
					cv.put(OrderTable.KEY_RATE, productSearchResults.get(i).getDp());
					cv.put(OrderTable.KEY_DISCOUNT,"0");
					cv.put(OrderTable.KEY_DISCPRICE, "0");
					cv.put(OrderTable.KEY_PRATE,
							productSearchResults.get(i).getPrate());
					cv.put(OrderTable.KEY_ROT, productSearchResults.get(i).getRot());
					cv.put(OrderTable.KEY_CHECKED, "true");
					cv.put(OrderTable.KEY_UNIT, productSearchResults.get(i).getUnit());
					cv.put(OrderTable.KEY_REMARK, productSearchResults.get(i).getRemark());
					mod.insertdata(OrderTable.DATABASE_TABLE,
							cv);

				}else
				{

					grs = grs + Double.parseDouble(productSearchResults.get(i).getQuantity()) * Double.parseDouble(productSearchResults.get(i).getDp()) * Double.parseDouble(productSearchResults.get(i).getCsz());

					double taxamt=Double.parseDouble(productSearchResults.get(i).getQuantity()) * Double.parseDouble(productSearchResults.get(i).getDp()) * Double.parseDouble(productSearchResults.get(i).getCsz());
					taxamt = Math.round(taxamt * 100.0) / 100.0;
					double TotTaxAmt=Double.parseDouble(productSearchResults.get(i).getRot());
					double singgst= ((taxamt*TotTaxAmt)/100);

					gst= gst + singgst;

					ContentValues cv = new ContentValues();
					cv.put(OrderTable.KEY_DATE, strDate);
					cv.put(OrderTable.KEY_CUSTID, "");
					cv.put(OrderTable.KEY_PRODUCTID, productSearchResults.get(i).getId());
					cv.put(OrderTable.KEY_QUANTITY,
							productSearchResults.get(i).getQuantity());

					Log.w("quantuity",productSearchResults.get(i).getQuantity());
					cv.put(OrderTable.KEY_EMPNO, session.getEmpNo());
					cv.put(OrderTable.KEY_RATE, Double.parseDouble(productSearchResults.get(i).getDp()) * Double.parseDouble(productSearchResults.get(i).getCsz()) );
					cv.put(OrderTable.KEY_DISCOUNT,"0");
					cv.put(OrderTable.KEY_DISCPRICE, "0");
					cv.put(OrderTable.KEY_PRATE,
							productSearchResults.get(i).getPrate());
					cv.put(OrderTable.KEY_ROT, productSearchResults.get(i).getRot());
					cv.put(OrderTable.KEY_CHECKED, "true");
					cv.put(OrderTable.KEY_UNIT, productSearchResults.get(i).getUnit());
					cv.put(OrderTable.KEY_REMARK, productSearchResults.get(i).getRemark());
					mod.insertdata(OrderTable.DATABASE_TABLE,
							cv);

				}


			}
			dba.close();

			 textViewGrs=(TextView) findViewById(R.id.totGrs);
			 textViewGst=(TextView)findViewById(R.id.allGst);
			 textViewTot=(TextView)findViewById(R.id.total);


			 setFooter();

			final ListView lv = (ListView) findViewById(R.id.list);
			lv.setAdapter(new CartBaseAdapter(this, productSearchResults));
		}catch (Exception e){
			e.printStackTrace();
			LogError lErr = new LogError();
			lErr.appendLog(e.toString());
		}



	}


	public void setFooter()
	{


		grs=grs-adapterGross;
		gst=gst-adapterGst;


		Double t=grs+gst;
		textViewGrs.setText(""+new DecimalFormat("#.##").format(grs));
		textViewGst.setText(""+new DecimalFormat("#.##").format(gst));
		textViewTot.setText(""+new DecimalFormat("#.##").format(t));

		adapterGst=0.0;
		adapterGross=0.0;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 1) {
			if (resultCode == RESULT_OK) {
				String myString = "";
				GLOBAL_CUST_ID = data.getStringExtra("BarCodeValue");

				dba.open();
				String sql = "select " + AllCustomer.KEY_NAME + " from "
						+ AllCustomer.DATABASE_TABLE+ " where "+AllCustomer.KEY_CUSTOMER_CODE+" = '"+GLOBAL_CUST_ID+"' ";
				Cursor custCur = DataBaseAdapter.ourDatabase.rawQuery(sql, null);
				if(custCur.getCount()>0)
				{
					custCur.moveToFirst();
					myString=custCur.getString(custCur.getColumnIndex(AllCustomer.KEY_NAME)) ;
					ArrayAdapter myAdap = (ArrayAdapter) spinCustomer.getAdapter(); //cast to an ArrayAdapter

					int spinnerPosition = myAdap.getPosition(myString);
//set the default according to value
					spinCustomer.setSelection(spinnerPosition);

					mode="Barcode";
				}else
				{
					String temp=GLOBAL_CUST_ID;
					temp = temp.replaceAll("[^\\d.]", "");
					if(temp.equalsIgnoreCase(""))
					{
						temp="1234567";
					}
					String sql1 = "select " + AllCustomer.KEY_NAME + " from "
							+ AllCustomer.DATABASE_TABLE+ " where "+AllCustomer.KEY_ID+"="+temp+" ";
					Cursor custCur1 = DataBaseAdapter.ourDatabase.rawQuery(sql1, null);
					if(custCur1.getCount()>0)
					{
						custCur1.moveToFirst();
						myString=custCur1.getString(custCur1.getColumnIndex(AllCustomer.KEY_NAME)) ;
						ArrayAdapter myAdap = (ArrayAdapter) spinCustomer.getAdapter(); //cast to an ArrayAdapter
						int spinnerPosition = myAdap.getPosition(myString);
						spinCustomer.setSelection(spinnerPosition);
						mode="Barcode";
					}
					else
					{
						spinCustomer.setSelection(0);
						Toast.makeText(UNCartActivity.this, "No User Found", Toast.LENGTH_SHORT).show();

					}

				}
				dba.close();
				//the value you want the position for


			}
		}
	}
    @Override
    protected void onDestroy() {
        super.onDestroy();
        toastService.stopTimer();
    }

    private ArrayList<ProductSearchResults> GetProductSearchResults() {
		ArrayList<ProductSearchResults> results = new ArrayList<ProductSearchResults>();

		try {
			ProductSearchResults sr = new ProductSearchResults();

			dba.open();
			Cursor cur = mod.getData(OrderTempTable.DATABASE_TABLE);
			if (cur.getCount() > 0) {
				while (cur.moveToNext()) {

					sr = new ProductSearchResults();
					sr.setID(cur.getString(0).trim());
					sr.setName(cur.getString(1).trim());
					sr.setDp(String.valueOf(Double.parseDouble(cur.getString(cur.getColumnIndex("dp")).trim())   *  Double.parseDouble(cur.getString(cur.getColumnIndex("csz")).trim())  ));
					sr.setPrice(cur.getString(5).trim());
					sr.setImage(cur.getString(3).trim());
					sr.setQuantity(cur.getString(cur.getColumnIndex("qty")).trim());
					sr.setAct(act);
					sr.setRot(cur.getString(cur.getColumnIndex("rot")));
					sr.setPrate(cur.getString(cur.getColumnIndex("prate")));
					sr.setChecked(true);
					sr.setCsz(cur.getString(cur.getColumnIndex("csz")));
					sr.setUnit(cur.getString(cur.getColumnIndex("unit")));
					sr.setRemark(cur.getString(cur.getColumnIndex("remark")));

					results.add(sr);
				}
			}
			cur.close();
			dba.close();
		}catch (Exception e){
			e.printStackTrace();
			LogError lErr = new LogError();
			lErr.appendLog(e.toString());
		}
		return results;
	}

	@SuppressLint("NewApi")
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.cart, menu);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeAsUpIndicator(R.drawable.back);

		menu.findItem(R.id.action_chekout).setIcon(R.drawable.checkout_icon);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case android.R.id.home:
			onBackPressed();
			return true;

		case R.id.action_chekout:


            try {

                if (spinCustomer.getSelectedItem().toString().trim().length() == 0) {
                    Toast.makeText(UNCartActivity.this, "Select Customer",
                            Toast.LENGTH_LONG).show();
                } else {

                    dba.open();
                    Cursor curData = null;
                    if (act.equalsIgnoreCase("billing")) {
                        curData = mod.getData(BillingTable.DATABASE_TABLE);
                        if (curData.getCount() > 0) {

                            Intent intent = new Intent(UNCartActivity.this, BillPayment.class);
                            intent.putExtra("custName", spinCustomer.getSelectedItem().toString().trim());
                            startActivity(intent);
                            finish();

                        } else {
                            Toast.makeText(UNCartActivity.this,
                                    "Please Confirm Items First" +
											"", Toast.LENGTH_LONG)
                                    .show();
                        }
                        curData.close();
                    } else if (act.equalsIgnoreCase("order")) {


                    	if (!custId.equalsIgnoreCase("")) {


							String delDate = txtDeliveryDate.getText().toString().trim();

							if (delDate.length() > 0) {

								curData = mod.getDataWhere(OrderTable.DATABASE_TABLE,"true");

								if (curData.getCount() > 0) {
									Data = "";

									while (curData.moveToNext()) {


										if (Data.equalsIgnoreCase("")) {
											Data = curData.getString(0).trim()
													+ "," + custId
													+ "," + curData.getString(2).trim()
													+ "," + curData.getString(3).trim()
													+ "," + curData.getString(4).trim()
													+ "," + curData.getString(5).trim()
													+ "," + curData.getString(6).trim()
													+ "," + curData.getString(7).trim()
													+ "," + curData.getString(8).trim()
													+ "," + curData.getString(9).trim()
													+ "," + delDate + ","
													+ session.getUserId()+"," + curData.getString(11).trim()+"," + curData.getString(curData.getColumnIndex("remark")).trim();
										} else {

											Data = Data + "$" + curData.getString(0).trim()
													+ "," + custId
													+ "," + curData.getString(2).trim()
													+ "," + curData.getString(3).trim()
													+ "," + curData.getString(4).trim()
													+ "," + curData.getString(5).trim()
													+ "," + curData.getString(6).trim()
													+ "," + curData.getString(7).trim()
													+ "," + curData.getString(8).trim()
													+ "," + curData.getString(9).trim()
													+ "," + delDate + ","
													+ session.getUserId()+","+ curData.getString(11).trim()+","+ curData.getString(curData.getColumnIndex("remark")).trim();
										}


										Log.w("Data", Data);
									}

									String logFlag = "";
									GPSTracker gps = new GPSTracker(UNCartActivity.this);
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

										// Now to check if we're actually connected
										if (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected()) {
											// Start the service to do our thing

											AsyncCallWS task = new AsyncCallWS();
											// Call execute
											task.execute();

											dba.open();
											//mod.clearDatabase("orderr");
											mod.clearDatabase("temp");
											dba.close();

											Log.w("Order", "Online");

										} else {
											Log.w("Order", "Offline");
											ContentValues cv = new ContentValues();
											cv.put(OfflineTable.KEY_DATA, Data);
											cv.put(OfflineTable.KEY_BRANCHNO, session.getBranchNo());
											cv.put(OfflineTable.KEY_LONGITUDE, longi);
											cv.put(OfflineTable.KEY_LATITUDE, lati);
											cv.put(OfflineTable.KEY_METHOD, "saveOrder");
											dba.open();
											mod.insertdata(OfflineTable.DATABASE_TABLE, cv);
											dba.close();

											Toast.makeText(UNCartActivity.this,
													"Data Saved Offline...!!",
													Toast.LENGTH_LONG);

											Intent intent = new Intent(
													getApplicationContext(),
													UNOrderActivity.class);
											startActivity(intent);
											finish();

										}

									} else {
										gps.showSettingsAlert();
									}

								} else {
									Toast.makeText(UNCartActivity.this,
											"Please Confirm Items First", Toast.LENGTH_LONG)
											.show();
								}

								curData.close();
							} else {
								Toast.makeText(UNCartActivity.this, "Select Delivery Date",
										Toast.LENGTH_LONG).show();
							}


						}else {


							Toast.makeText(UNCartActivity.this, "Please Select Customer", Toast.LENGTH_SHORT).show();

						}




                    }
                    Log.w("CA", "Data: " + Data);

                    dba.close();


                }
            }catch (Exception e){
                e.printStackTrace();
            }
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		if (act.equalsIgnoreCase("order")) {
			Intent intent = new Intent(getApplicationContext(),
					OrderActivity.class);
			startActivity(intent);
			finish();
		} else if (act.equalsIgnoreCase("billing")) {
			Intent intent = new Intent(getApplicationContext(),
					BillingActivity.class);
			startActivity(intent);
			finish();
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
				if (act.equalsIgnoreCase("billing")) {
                    serverResponse = WebService.makeTransaction(Data, session.getBranchNo(), longi, lati, "saveBill");
				} else if (act.equalsIgnoreCase("order")) {
                    serverResponse = WebService.Saveorder(Data, session.getBranchNo(), longi, lati,"U",mode, "saveOrder");
				}
				dba.close();

			} catch (Exception e) {
				Log.w("CartActivity", "Timeout ");
				LogError lErr = new LogError();
				lErr.appendLog(e.toString());
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
						UNCartActivity.this);
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

					*//*
					 * Toast.makeText(LoginActivity.this, "JSON Data:  " +
					 * WebService.responseString, Toast.LENGTH_LONG).show();
					 *//*
					// Error status is false
					if (!errored) {
						// Based on Boolean value returned from WebService
						if (serverResponse != null) {

							if (serverResponse.getJSONObject(0)
									.getString("Status").trim()
									.equalsIgnoreCase("No")) {

								AlertDialog.Builder builder2 = new AlertDialog.Builder(
										UNCartActivity.this);
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

								Toast.makeText(UNCartActivity.this,
										"Data Successfully Saved!!!",
										Toast.LENGTH_LONG);

								Intent intent = new Intent(
										getApplicationContext(),
										UNOrderActivity.class);
								startActivity(intent);
								finish();

							}else if (serverResponse.getJSONObject(0)
									.getString("Status").trim()
									.equalsIgnoreCase("Failure")) {

								Toast.makeText(UNCartActivity.this,
										"Failed to save Data",
										Toast.LENGTH_LONG);


							}

						}

						// Error status is true
					} else {

						Toast.makeText(UNCartActivity.this, "Server Error",
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
	}*/
}
