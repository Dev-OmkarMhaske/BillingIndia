package com.tsysinfo.billing;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources.NotFoundException;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.view.MenuItemCompat;
 
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.LocationServices;
import com.tsysinfo.billing.barcodes.FullScannerActivity;
import com.tsysinfo.billing.database.CustomerTable;
import com.tsysinfo.billing.database.DataBaseAdapter;
import com.tsysinfo.billing.database.Models;
import com.tsysinfo.billing.database.ProductTable;
import com.tsysinfo.billing.database.ReturnTable;
import com.tsysinfo.billing.database.SessionManager;
import com.tsysinfo.billing.database.WebService;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RetAct extends AppCompatActivity {

	// private int count = 0;

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

	LinearLayout linearList;
	private String GLOBAL_CUST_ID;

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
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

					ArrayAdapter myAdap = (ArrayAdapter) spinCustomer.getAdapter(); //cast to an ArrayAdapter

					int spinnerPosition = myAdap.getPosition(myString);

//set the default according to value
					spinCustomer.setSelection(spinnerPosition);
				}else
				{
					String temp=GLOBAL_CUST_ID;
					temp = temp.replaceAll("[^\\d.]", "");
					if(temp.equalsIgnoreCase(""))
					{
						temp="1234567";
					}
					String sql1 = "select " + CustomerTable.KEY_NAME + " from "
							+ CustomerTable.DATABASE_TABLE+ " where "+CustomerTable.KEY_ID+" = "+temp+" ";
					Cursor custCur1 = DataBaseAdapter.ourDatabase.rawQuery(sql1, null);

					if(custCur1.getCount()>0)
					{
						custCur1.moveToFirst();
						myString=custCur1.getString(custCur1.getColumnIndex(CustomerTable.KEY_NAME)) ;

						ArrayAdapter myAdap = (ArrayAdapter) spinCustomer.getAdapter(); //cast to an ArrayAdapter

						int spinnerPosition = myAdap.getPosition(myString);

//set the default according to value
						spinCustomer.setSelection(spinnerPosition);
					}
					else
					{
						spinCustomer.setSelection(0);
						Toast.makeText(RetAct.this, "No Customer Found in this route", Toast.LENGTH_SHORT).show();

					}

				}

				dba.close();
				//the value you want the position for

			}
		}
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ret_list_act);


		ImageView barCode=(ImageView)findViewById(R.id.imageButtonBarcode);
		barCode.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(RetAct.this,FullScannerActivity.class);
				spinCustomer.setSelection(0);
				startActivityForResult(intent, 1);
			}
		});


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


        toastService = new ToastService(RetAct.this);

		linearList = (LinearLayout) findViewById(R.id.list);

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
                        Toast.makeText(RetAct.this, "Unable to connect to GPS Please Select Customer Manually", Toast.LENGTH_SHORT).show();
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


        CreateListView();

	}




    private void CreateListView() {

        try{


		dba.open();
		Cursor cur = mod.getData(ProductTable.DATABASE_TABLE);

			if (cur.getCount() > 0) {

				LinearLayout linlay;
				ImageView imgPrd;
				TextView tvProdName;
				TextView tvProdDesc;
				TextView tvDiscount;
				TextView tvTotAmt;
				TextView lblTotAmt;
				TextView lblQty;
				TextView lblDsc;
				TextView lblMrp;
				TextView lblDscPr;
				EditText edQty;
				EditText edDisc;
				EditText edMRP;
				//Buttons
				ImageView btnAddToCart;

				String id = "";

                if(linearList != null) {
                    linearList.removeAllViews();
                }
				while (cur.moveToNext()) {

					linearList.setPadding(5,5,5,5);
					linearList.setBackgroundColor(getResources().getColor(R.color.white));
                    /*sr = new ProductSearchResults();
                    sr.setID(cur.getString(0).trim());
                    sr.setName(cur.getString(1).trim());
                    sr.setDescription(cur.getString(2).trim());
                    sr.setPrice(cur.getString(7).trim());
                    sr.setImage(cur.getString(10).trim());
                    sr.setAct(act);
                    results.add(sr);*/

					id =cur.getString(0).trim();

					// linlay layout for views
					linlay = new LinearLayout(RetAct.this);
					LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
					linlay.setLayoutParams(layoutParams);
					linlay.setOrientation(LinearLayout.HORIZONTAL);
                    linlay.setBackgroundResource(R.drawable.list_down_style);
					linlay.setPadding(3,3,3,3);
					linearList.addView(linlay);

					//linimg for image
					LinearLayout linImg = new LinearLayout(RetAct.this);
					linImg.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT,3));
					linImg.setPadding(3,3,3,3);
					imgPrd = new ImageView(RetAct.this);
					imgPrd.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
					try {
						if (!cur.getString(3).trim().equalsIgnoreCase("")) {
							Bitmap image = BitmapFactory.decodeFile(cur.getString(10).trim());
							image = Bitmap.createScaledBitmap(image, 200, 200, false);
							imgPrd.setImageBitmap(image);
						}
					}catch (Exception e){
						e.printStackTrace();
					}
					linImg.addView(imgPrd);
					linlay.addView(linImg);


                    //linUpperViews layout for Parrent Layout
                    LinearLayout linUpperViews = new LinearLayout(RetAct.this);
                    linUpperViews.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT,1));
                    linUpperViews.setPadding(3,3,3,3);
                    linUpperViews.setWeightSum(4);
                    linUpperViews.setOrientation(LinearLayout.HORIZONTAL);
                    linlay.addView(linUpperViews);

					//linViews Child Layout for Textviews
					LinearLayout linViews = new LinearLayout(RetAct.this);
					LinearLayout.LayoutParams layPar = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT,1);
					layPar.setMargins(0,0,0,5);
					linViews.setLayoutParams(layPar);
					linViews.setPadding(3,3,3,3);
					linViews.setOrientation(LinearLayout.VERTICAL);
                    linUpperViews.addView(linViews);


                    //linViews Child Layout for Button
                    LinearLayout linButton = new LinearLayout(RetAct.this);
                    linButton.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT,3));
                    linButton.setPadding(1,1,1,1);
                    linButton.setGravity(Gravity.BOTTOM);
                    linUpperViews.addView(linButton);

                    btnAddToCart = new ImageView(RetAct.this);
                    btnAddToCart.setLayoutParams(new LinearLayout.LayoutParams(100, 100));
                    btnAddToCart.setImageResource(R.drawable.cart);
                    linButton.addView(btnAddToCart);

					//TextView ProductName
					tvProdName = new TextView(RetAct.this);
					tvProdName.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT ));
					tvProdName.setTextColor(getResources().getColor(R.color.water));
					tvProdName.setTextSize(18);
					tvProdName.setTypeface(Typeface.SANS_SERIF,Typeface.BOLD);
					tvProdName.setText(cur.getString(1).trim());
					linViews.addView(tvProdName);

					//TextView tvProdDesc
					tvProdDesc = new TextView(RetAct.this);
					tvProdDesc.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT ));
					tvProdDesc.setTextColor(Color.parseColor("#343434"));
					tvProdDesc.setTextSize(12);
					tvProdDesc.setText(cur.getString(2).trim());
					linViews.addView(tvProdDesc);

					//linQtyDsc layout for linearlayout qty desc
					LinearLayout linLayQtyDesc = new LinearLayout(RetAct.this);
					linLayQtyDesc.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT ));
					linLayQtyDesc.setOrientation(LinearLayout.HORIZONTAL);
					linViews.addView(linLayQtyDesc);

					//linQty layout for qty
					LinearLayout linQty = new LinearLayout(RetAct.this);
					linQty.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT,1 ));
					linQty.setOrientation(LinearLayout.HORIZONTAL);
					linLayQtyDesc.addView(linQty);

					//TextView lblQty
					lblQty = new TextView(RetAct.this);
					lblQty.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT ));
					lblQty.setTextColor(Color.parseColor("#343434"));
					lblQty.setPadding(2,2,2,2);
					lblQty.setEms(3);
					lblQty.setTextSize(12);
					lblQty.setText("Qty:  ");
					linQty.addView(lblQty);

					//EditText edQty
					edQty = new EditText(RetAct.this);
					edQty.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT ));
					edQty.setBackgroundResource(R.drawable.edittextstyle);
					edQty.setEms(5);
					edQty.setGravity(Gravity.CENTER);
					edQty.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_CLASS_PHONE);
					edQty.setPadding(2,2,2,2);
					edQty.setText("0");
					edQty.setTextSize(12);
					edQty.setTextColor(Color.parseColor("#343434"));
					linQty.addView(edQty);

					//linDesc layout for dsc
					LinearLayout linDesc = new LinearLayout(RetAct.this);
					linDesc.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT,1 ));
					linDesc.setOrientation(LinearLayout.HORIZONTAL);
					linLayQtyDesc.addView(linDesc);

					//TextView lblDsc
					lblDsc = new TextView(RetAct.this);
					lblDsc.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT ));
					lblDsc.setTextColor(Color.parseColor("#343434"));
					lblDsc.setPadding(2,2,2,2);
					lblDsc.setTextSize(12);
					lblDsc.setText("Disc %:  ");
					linDesc.addView(lblDsc);

					//EditText edDisc
					edDisc = new EditText(RetAct.this);
					edDisc.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT ));
					edDisc.setBackgroundResource(R.drawable.edittextstyle);
					edDisc.setEms(3);
					edDisc.setGravity(Gravity.CENTER);
					edDisc.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_CLASS_PHONE);
					edDisc.setPadding(2,2,2,2);
					edDisc.setText("0");
					edDisc.setTextSize(12);
					edDisc.setTextColor(Color.parseColor("#343434"));
					linDesc.addView(edDisc);

					//linLayMrpDp layout for linearlayout MRP DiscPrice
					LinearLayout linLayMrpDp = new LinearLayout(RetAct.this);
					linLayMrpDp.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT ));
					linLayMrpDp.setOrientation(LinearLayout.HORIZONTAL);
					linViews.addView(linLayMrpDp);

					//linMrp layout for MRP
					LinearLayout linMrp = new LinearLayout(RetAct.this);
					LinearLayout.LayoutParams layParMrp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT,1 );
					layParMrp.setMargins(0,3,0,0);
					linMrp.setLayoutParams(layParMrp);
					linMrp.setOrientation(LinearLayout.HORIZONTAL);
					linLayMrpDp.addView(linMrp);

					//TextView lblMrp
					lblMrp = new TextView(RetAct.this);
					lblMrp.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT ));
					lblMrp.setTextColor(Color.parseColor("#343434"));
					lblMrp.setPadding(2,2,2,2);
					lblMrp.setTextSize(12);
					lblMrp.setEms(3);
					lblMrp.setText("MRP: ");
					linMrp.addView(lblMrp);

					//EditText edMRP
					edMRP= new EditText(RetAct.this);
					edMRP.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT ));
					edMRP.setBackgroundResource(R.drawable.edittextstyle);
					edMRP.setEms(5);
					edMRP.setGravity(Gravity.CENTER);
					edMRP.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
					edMRP.setPadding(2,2,2,2);
					edMRP.setText(cur.getString(7).trim());
					edMRP.setTextSize(12);
					edMRP.setTextColor(Color.parseColor("#343434"));
					linMrp.addView(edMRP);


					//linDescPer layout for DescPer
					LinearLayout linDescPer = new LinearLayout(RetAct.this);
					linDescPer.setLayoutParams(layParMrp);
					linDescPer.setOrientation(LinearLayout.HORIZONTAL);
					linLayMrpDp.addView(linDescPer);

					//TextView lblDscPr
					lblDscPr = new TextView(RetAct.this);
					lblDscPr.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT ));
					lblDscPr.setTextColor(Color.parseColor("#343434"));
					lblDscPr.setPadding(2,2,2,2);
					lblDscPr.setTextSize(12);
					lblDscPr.setText("Disc: ");
					linDescPer.addView(lblDscPr);

					//TextView edDisc
					tvDiscount = new TextView(RetAct.this);
					tvDiscount.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT ));
					tvDiscount.setPadding(2,2,2,2);
					tvDiscount.setText("0.0");
					tvDiscount.setTypeface(Typeface.DEFAULT_BOLD);
					tvDiscount.setTextSize(12);
					tvDiscount.setTextColor(Color.parseColor("#343434"));
					linDescPer.addView(tvDiscount);

					//linLayTotal layout for Total Amount
					LinearLayout linLayTotal = new LinearLayout(RetAct.this);
					linLayTotal.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT ));
					linLayTotal.setOrientation(LinearLayout.HORIZONTAL);
					linViews.addView(linLayTotal);

					//TextView lblTotAmt
					lblTotAmt = new TextView(RetAct.this);
					lblTotAmt.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT ));
					lblTotAmt.setTextColor(Color.parseColor("#343434"));
					lblTotAmt.setTextSize(12);
					lblTotAmt.setText("Total Amount: ");
					linLayTotal.addView(lblTotAmt);

					//TextView tvTotAmt
					tvTotAmt = new TextView(RetAct.this);
					tvTotAmt.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT ));
					tvTotAmt.setTextColor(Color.parseColor("#343434"));
					tvTotAmt.setTextSize(12);
                    tvTotAmt.setEms(6);
					tvTotAmt.setTypeface(Typeface.DEFAULT_BOLD);
					tvTotAmt.setText("");
					linLayTotal.addView(tvTotAmt);

					final String finalId = id;
					final TextView finalTvProdName = tvProdName;


					final EditText finalEdMRP = edMRP;
					final TextView finalTvTotAmt = tvTotAmt;
					final EditText finalEdDisc = edDisc;
					final EditText finalEdQty = edQty;
					final TextView finalTvDiscount = tvDiscount;


					edMRP.addTextChangedListener(new TextWatcher() {
						@Override
						public void beforeTextChanged(CharSequence s, int start, int count, int after) {

						}

						@Override
						public void onTextChanged(CharSequence s, int start, int before, int count) {
							String strDisc = finalEdDisc.getText()
									.toString().trim();
							String strQty = finalEdQty.getText().toString()
									.trim();
							String strMRP = finalEdMRP.getText().toString()
									.trim();

							if (strDisc.equalsIgnoreCase("")
									|| strQty.equalsIgnoreCase("")
									|| strMRP.equalsIgnoreCase("")) {

							}else if(strMRP.equalsIgnoreCase("0")){
								Toast.makeText(RetAct.this, "MRP should be greater than 0", Toast.LENGTH_LONG).show();
							}else{

								double tot = Float.parseFloat(strQty)
										* Float.parseFloat(strMRP);

								double dis = tot
										* (Float.parseFloat(strDisc) * 0.01);

								tot = tot - dis;
								tot = Math.round(tot * 100.0) / 100.0;
								finalTvDiscount.setText(String.valueOf(dis));
								finalTvTotAmt.setText(String.valueOf(tot));

							}
						}

						@Override
						public void afterTextChanged(Editable s) {

						}
					});



					edQty.addTextChangedListener(new TextWatcher() {
						@Override
						public void beforeTextChanged(CharSequence s, int start, int count, int after) {

						}

						@Override
						public void onTextChanged(CharSequence s, int start, int before, int count) {
							String strDisc = finalEdDisc.getText()
									.toString().trim();
							String strQty = finalEdQty.getText().toString()
									.trim();
							String strMRP = finalEdMRP.getText().toString()
									.trim();

							if (strDisc.equalsIgnoreCase("")
									|| strQty.equalsIgnoreCase("")
									|| strMRP.equalsIgnoreCase("")) {

							}else if(strMRP.equalsIgnoreCase("0")){
								Toast.makeText(RetAct.this, "MRP should be greater than 0", Toast.LENGTH_LONG).show();
							}else{

								double tot = Float.parseFloat(strQty)
										* Float.parseFloat(strMRP);

								double dis = tot
										* (Float.parseFloat(strDisc) * 0.01);

								tot = tot - dis;
								tot = Math.round(tot * 100.0) / 100.0;
								finalTvDiscount.setText(String.valueOf(dis));
								finalTvTotAmt.setText(String.valueOf(tot));

							}
						}

						@Override
						public void afterTextChanged(Editable s) {

						}
					});



					edDisc.addTextChangedListener(new TextWatcher() {
						@Override
						public void beforeTextChanged(CharSequence s, int start, int count, int after) {

						}

						@Override
						public void onTextChanged(CharSequence s, int start, int before, int count) {
							String strDisc = finalEdDisc.getText()
									.toString().trim();
							String strQty = finalEdQty.getText().toString()
									.trim();
							String strMRP = finalEdMRP.getText().toString()
									.trim();

							if (strDisc.equalsIgnoreCase("")
									|| strQty.equalsIgnoreCase("")
									|| strMRP.equalsIgnoreCase("")) {

							}else if(strMRP.equalsIgnoreCase("0")){
								Toast.makeText(RetAct.this, "MRP should be greater than 0", Toast.LENGTH_LONG).show();
							}else{

								double tot = Float.parseFloat(strQty)
										* Float.parseFloat(strMRP);

								double dis = tot
										* (Float.parseFloat(strDisc) * 0.01);

								tot = tot - dis;
								tot = Math.round(tot * 100.0) / 100.0;
								finalTvDiscount.setText(String.valueOf(dis));
								finalTvTotAmt.setText(String.valueOf(tot));

							}
						}

						@Override
						public void afterTextChanged(Editable s) {

						}
					});


                    btnAddToCart.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            // TODO Auto-generated method stub

                            String strDisc = finalEdDisc.getText()
                                    .toString().trim();
                            String strQty = finalEdQty.getText().toString()
                                    .trim();
                            String strMRP = finalEdMRP.getText().toString()
                                    .trim();

                            if (strDisc.equalsIgnoreCase("")
                                    || strQty.equalsIgnoreCase("")
                                    || strMRP.equalsIgnoreCase("")) {

                            }else if(strMRP.equalsIgnoreCase("0")){
                                Toast.makeText(RetAct.this, "MRP should be greater than 0", Toast.LENGTH_LONG).show();
                            }else{

                                double tot = Float.parseFloat(strQty)
                                        * Float.parseFloat(strMRP);

                                double dis = tot
                                        * (Float.parseFloat(strDisc) * 0.01);

                                tot = tot - dis;
                                tot = Math.round(tot * 100.0) / 100.0;
                                finalTvDiscount.setText(String.valueOf(dis));
                                finalTvTotAmt.setText(String.valueOf(tot));

                            }


                            String strSpin = spinCustomer.getSelectedItem().toString().trim();
                            if (!strSpin.equalsIgnoreCase("Select Customer")) {
                                dba.open();
                                String sql = "select * from retutntab where pid = '" + finalId + "' and qty = '" + finalEdQty.getText().toString().trim() + "'";
                                Cursor cur = DataBaseAdapter.ourDatabase.rawQuery(sql, null);
                                if (cur.getCount() > 0) {
                                    Toast.makeText(RetAct.this, "Product already in cart. Choose" +
                                            " another product", Toast.LENGTH_LONG)
                                            .show();
                                } else {

                                    String del = "delete from retutntab where pid = '" + finalId
                                            + "'";
                                    DataBaseAdapter.ourDatabase.rawQuery(del, null);

                                    ContentValues cv = new ContentValues();
                                    cv.put("pid", finalId);
                                    cv.put("custid", custid);
                                    cv.put("empid", session.getEmpNo());
                                    cv.put("qty", finalEdQty.getText().toString().trim());
                                    cv.put("mrp", finalEdMRP.getText().toString().trim());
                                    cv.put("disc", finalEdDisc.getText().toString().trim());
                                    mod.insertdata(ReturnTable.DATABASE_TABLE, cv);

                                    doIncrease();
                                }
                                cur.close();
                                dba.close();


                                Toast.makeText(RetAct.this, finalTvProdName.getText().toString().trim() + " Added To Return Cart", Toast.LENGTH_LONG)
                                        .show();
                            }else{
                                Toast.makeText(RetAct.this, "Please Select Customer", Toast.LENGTH_LONG)
                                        .show();
                            }
                        }
                    });


				}
			}
		cur.close();
		dba.close();
        }catch (Exception e){
            e.printStackTrace();
        }

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

						Intent intent = new Intent(RetAct.this,ReturnInvoice.class);
						intent.putExtra("custName",custName111);
						intent.putExtra("billNo",billNo);
						startActivity(intent);
						finish();


                    } else {
                        Toast.makeText(RetAct.this, "Add Item to cart first",
                                Toast.LENGTH_LONG).show();
                    }
                    cur.close();
                    dba.close();
                } else {
                    Toast.makeText(RetAct.this, "Enter Bill No",
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
						RetAct.this);
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
										RetAct.this);
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

								Toast.makeText(RetAct.this,
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

						Toast.makeText(RetAct.this, "Server Error",
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
				Toast.makeText(RetAct.this,
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
