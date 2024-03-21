package com.tsysinfo.billing;

import android.annotation.SuppressLint;
import android.app.Activity;
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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.LocationServices;
import com.tsysinfo.billing.barcodes.FullScannerActivity;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class CaAc extends AppCompatActivity {

	static String act = "";

	static Spinner spinCustomer;
	static TextView txtDeliveryDate;

	String Data = "";

	Double currLatitude;
	Double currLongitude;

//	Double distMiles=0.00932057;


	SessionManager session;
	DataBaseAdapter dba;
	Models mod;
	ProgressDialog waitDialog;
	JSONArray serverResponse;
	static boolean errored = false;

	public Location mLastLocation;

	ToastService toastService;

    String longi = "",lati = "";


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
                            + CustomerTable.DATABASE_TABLE+ " where "+CustomerTable.KEY_ID+"="+temp+" ";
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
                        Toast.makeText(CaAc.this, "No User Found", Toast.LENGTH_SHORT).show();

                    }

                }

                dba.close();
                //the value you want the position for

            }
        }
    }
	@Override
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

					DatePickerDialog mDatePicker = new DatePickerDialog(
							CaAc.this, new OnDateSetListener() {
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
			setContentView(R.layout.c_l_a);
		}

        ImageView barCode=(ImageView)findViewById(R.id.imageButtonBarcode);
        barCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CaAc.this,FullScannerActivity.class);
                startActivityForResult(intent, 1);
                spinCustomer.setSelection(0);
            }
        });
		// References
		session = new SessionManager(this);
		dba = new DataBaseAdapter(this);
		mod = new Models();

		toastService = new ToastService(CaAc.this);

		waitDialog = new ProgressDialog(this);
		waitDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		waitDialog.setMessage("Please Wait");

		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		spinCustomer = (Spinner) findViewById(R.id.spinnerCustomer);
		linearList = (LinearLayout) findViewById(R.id.list);

	//	txtCustomer = (TextView) findViewById(R.id.txtCustomer);


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

								
                                for (int i = 0; i < spinCustomer.getAdapter()
                                        .getCount(); i++) {
                                    if (spinCustomer.getAdapter().getItem(i)
                                            .toString().contains(custCur.getString(0).trim())) {
                                        spinCustomer.setSelection(i);
                                        LocFlg = 1;
                                        break;
                                    }
                                }
                            }
                        }
                        if(LocFlg == 0){
                            Toast.makeText(CaAc.this, "Unable to connect to GPS Please Select Customer Manually", Toast.LENGTH_SHORT).show();
                        }

                    }
                    custCur.close();
                    dba.close();
                }else{
                    toastService.startTimer();
                }

			}


            CreateListView();
		}catch (Exception e){
			e.printStackTrace();
			LogError lErr = new LogError();
			lErr.appendLog(e.toString());
		}
	}

    @Override
    protected void onDestroy() {
        super.onDestroy();
        toastService.stopTimer();
    }

    private void CreateListView() {

		try {
            dba.open();
            Cursor cur = mod.getData(OrderTempTable.DATABASE_TABLE);
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
                ImageView imgRem,imgConf;
                TextView btnRemove;
                TextView btnConfirm;

                String id = "";

                linearList.removeAllViews();

				while (cur.moveToNext()) {




                    id =cur.getString(0).trim();

                    // linlay layout for views
                    linlay = new LinearLayout(CaAc.this);
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    linlay.setLayoutParams(layoutParams);
                    linlay.setOrientation(LinearLayout.HORIZONTAL);
                    linlay.setPadding(3,3,3,3);
                    linearList.addView(linlay);

                    //linimg for image
                    LinearLayout linImg = new LinearLayout(CaAc.this);
                    linImg.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT,3));
                    linImg.setPadding(3,3,3,3);
                    imgPrd = new ImageView(CaAc.this);
                    imgPrd.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
                    try {
                        if (!cur.getString(3).trim().equalsIgnoreCase("")) {
                            Bitmap image = BitmapFactory.decodeFile(cur.getString(3).trim());
                            image = Bitmap.createScaledBitmap(image, 200, 200, false);
                            imgPrd.setImageBitmap(image);
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    linImg.addView(imgPrd);
                    linlay.addView(linImg);

                    //linViews layout for Textviews
                    LinearLayout linViews = new LinearLayout(CaAc.this);
                    LinearLayout.LayoutParams layPar = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT,1);
                    layPar.setMargins(0,0,0,5);
                    linViews.setLayoutParams(layPar);
                    linViews.setPadding(3,3,3,3);
                    linViews.setOrientation(LinearLayout.VERTICAL);
                    linlay.addView(linViews);

                    //TextView ProductName
                    tvProdName = new TextView(CaAc.this);
                    tvProdName.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT ));
                    tvProdName.setTextColor(getResources().getColor(R.color.water));
                    tvProdName.setTextSize(18);
                    tvProdName.setTypeface(Typeface.SANS_SERIF,Typeface.BOLD);
                    tvProdName.setText(cur.getString(1).trim());
                    linViews.addView(tvProdName);

                    //TextView tvProdDesc
                    tvProdDesc = new TextView(CaAc.this);
                    tvProdDesc.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT ));
                    tvProdDesc.setTextColor(Color.parseColor("#343434"));
                    tvProdDesc.setTextSize(12);
                    tvProdDesc.setText(cur.getString(2).trim());
                    linViews.addView(tvProdDesc);

                    //linQtyDsc layout for linearlayout qty desc
                    LinearLayout linLayQtyDesc = new LinearLayout(CaAc.this);
                    linLayQtyDesc.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT ));
                    linLayQtyDesc.setOrientation(LinearLayout.HORIZONTAL);
                    linViews.addView(linLayQtyDesc);

                    //linQty layout for qty
                    LinearLayout linQty = new LinearLayout(CaAc.this);
                    linQty.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT,1 ));
                    linQty.setOrientation(LinearLayout.HORIZONTAL);
                    linLayQtyDesc.addView(linQty);

                    //TextView lblQty
                    lblQty = new TextView(CaAc.this);
                    lblQty.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT ));
                    lblQty.setTextColor(Color.parseColor("#343434"));
                    lblQty.setPadding(2,2,2,2);
                    lblQty.setEms(3);
                    lblQty.setTextSize(12);
                    lblQty.setText("Qty:  ");
                    linQty.addView(lblQty);

                    //EditText edQty
                    edQty = new EditText(CaAc.this);
                    edQty.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT ));
                    edQty.setBackgroundResource(R.drawable.edittextstyle);
                    edQty.setEms(5);
                    edQty.setGravity(Gravity.CENTER);
                    edQty.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_CLASS_PHONE);
                    edQty.setPadding(2,2,2,2);
                    edQty.setText(cur.getString(4).trim());
                    edQty.setTextSize(12);
                    edQty.setTextColor(Color.parseColor("#343434"));
                    linQty.addView(edQty);

                    //linDesc layout for dsc
                    LinearLayout linDesc = new LinearLayout(CaAc.this);
                    linDesc.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT,1 ));
                    linDesc.setOrientation(LinearLayout.HORIZONTAL);
                    linLayQtyDesc.addView(linDesc);

                    //TextView lblDsc
                    lblDsc = new TextView(CaAc.this);
                    lblDsc.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT ));
                    lblDsc.setTextColor(Color.parseColor("#343434"));
                    lblDsc.setPadding(2,2,2,2);
                    lblDsc.setTextSize(12);
                    lblDsc.setText("Disc %:  ");
                    linDesc.addView(lblDsc);

                    //EditText edDisc
                    edDisc = new EditText(CaAc.this);
                    edDisc.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT ));
                    edDisc.setBackgroundResource(R.drawable.edittextstyle);
                    edDisc.setEms(3);
                    edDisc.setGravity(Gravity.CENTER);
                    edDisc.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_CLASS_PHONE);
                    edDisc.setPadding(2,2,2,2);
                    edDisc.setText(cur.getString(6).trim());
                    edDisc.setTextSize(12);
                    edDisc.setTextColor(Color.parseColor("#343434"));
                    linDesc.addView(edDisc);

                    //linLayMrpDp layout for linearlayout MRP DiscPrice
                    LinearLayout linLayMrpDp = new LinearLayout(CaAc.this);
                    linLayMrpDp.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT ));
                    linLayMrpDp.setOrientation(LinearLayout.HORIZONTAL);
                    linViews.addView(linLayMrpDp);

                    //linMrp layout for MRP
                    LinearLayout linMrp = new LinearLayout(CaAc.this);
                    LinearLayout.LayoutParams layParMrp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT,1 );
                    layParMrp.setMargins(0,3,0,0);
                    linMrp.setLayoutParams(layParMrp);
                    linMrp.setOrientation(LinearLayout.HORIZONTAL);
                    linLayMrpDp.addView(linMrp);

                    //TextView lblMrp
                    lblMrp = new TextView(CaAc.this);
                    lblMrp.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT ));
                    lblMrp.setTextColor(Color.parseColor("#343434"));
                    lblMrp.setPadding(2,2,2,2);
                    lblMrp.setTextSize(12);
                    lblMrp.setEms(3);
                    lblMrp.setText("MRP: ");
                    linMrp.addView(lblMrp);

                    //EditText edMRP
                    edMRP= new EditText(CaAc.this);
                    edMRP.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT ));
                    edMRP.setBackgroundResource(R.drawable.edittextstyle);
                    edMRP.setEms(5);
                    edMRP.setGravity(Gravity.CENTER);
                    edMRP.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                    edMRP.setPadding(2,2,2,2);
                    edMRP.setText(cur.getString(5).trim());
                    edMRP.setTextSize(12);
                    edMRP.setTextColor(Color.parseColor("#343434"));
                    linMrp.addView(edMRP);


                    //linDescPer layout for DescPer
                    LinearLayout linDescPer = new LinearLayout(CaAc.this);
                    linDescPer.setLayoutParams(layParMrp);
                    linDescPer.setOrientation(LinearLayout.HORIZONTAL);
                    linLayMrpDp.addView(linDescPer);

                    //TextView lblDscPr
                    lblDscPr = new TextView(CaAc.this);
                    lblDscPr.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT ));
                    lblDscPr.setTextColor(Color.parseColor("#343434"));
                    lblDscPr.setPadding(2,2,2,2);
                    lblDscPr.setTextSize(12);
                    lblDscPr.setText("Disc: ");
                    linDescPer.addView(lblDscPr);

                    //TextView edDisc
                    tvDiscount = new TextView(CaAc.this);
                    tvDiscount.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT ));
                    tvDiscount.setPadding(2,2,2,2);
                    tvDiscount.setText("0.0");
                    tvDiscount.setTypeface(Typeface.DEFAULT_BOLD);
                    tvDiscount.setTextSize(12);
                    tvDiscount.setTextColor(Color.parseColor("#343434"));
                    linDescPer.addView(tvDiscount);

                    //linLayTotal layout for Total Amount
                    LinearLayout linLayTotal = new LinearLayout(CaAc.this);
                    linLayTotal.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT ));
                    linLayTotal.setOrientation(LinearLayout.HORIZONTAL);
                    linViews.addView(linLayTotal);

                    //TextView lblTotAmt
                    lblTotAmt = new TextView(CaAc.this);
                    lblTotAmt.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT ));
                    lblTotAmt.setTextColor(Color.parseColor("#343434"));
                    lblTotAmt.setTextSize(12);
                    lblTotAmt.setText("Total Amount: ");
                    linLayTotal.addView(lblTotAmt);

                    //TextView tvTotAmt
                    tvTotAmt = new TextView(CaAc.this);
                    tvTotAmt.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT ));
                    tvTotAmt.setTextColor(Color.parseColor("#343434"));
                    tvTotAmt.setTextSize(12);
                    tvTotAmt.setTypeface(Typeface.DEFAULT_BOLD);
                    tvTotAmt.setText("");
                    linLayTotal.addView(tvTotAmt);


                    //linLayBtn layout for Buttons
                    LinearLayout linLayBtn = new LinearLayout(CaAc.this);
                    linLayBtn.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT ));
                    linLayBtn.setOrientation(LinearLayout.HORIZONTAL);
                    linLayBtn.setBackgroundResource(R.drawable.list_down_style);
                    linearList.addView(linLayBtn);

                    //linLayRemove layout for Remove
                    LinearLayout linLayRemove = new LinearLayout(CaAc.this);
                    linLayRemove.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT,1 ));
                    linLayRemove.setOrientation(LinearLayout.HORIZONTAL);
                    linLayRemove.setGravity(Gravity.CENTER);
                    linLayRemove.setBackgroundResource(R.drawable.list_button_style);
                    linLayBtn.addView(linLayRemove);

                    imgRem = new ImageView(CaAc.this);
                    LinearLayout.LayoutParams layImgRem = new LinearLayout.LayoutParams(20, 20);
                    layImgRem.setMargins(0,0,1,0);
                    imgRem.setLayoutParams(layImgRem);
                    imgRem.setImageResource(R.drawable.del);
                    linLayRemove.addView(imgRem);

                    btnRemove = new TextView(CaAc.this);
                    btnRemove.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT ));
                    btnRemove.setTextColor(Color.parseColor("#75BA40"));
                    btnRemove.setTextSize(15);
                    btnRemove.setTypeface(Typeface.DEFAULT_BOLD);
                    btnRemove.setText("REMOVE");
                    linLayRemove.addView(btnRemove);



                    //linLayConfirm layout for Confirm
                    LinearLayout linLayConfirm = new LinearLayout(CaAc.this);
                    linLayConfirm.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT,1 ));
                    linLayConfirm.setOrientation(LinearLayout.HORIZONTAL);
                    linLayConfirm.setGravity(Gravity.CENTER);
                    linLayBtn.addView(linLayConfirm);

                    imgConf = new ImageView(CaAc.this);
                    LinearLayout.LayoutParams layImgConf = new LinearLayout.LayoutParams(20, 20);
                    layImgConf.setMargins(0,0,1,0);
                    imgConf.setLayoutParams(layImgConf);
                    imgConf.setImageResource(R.drawable.confirm);
                    linLayConfirm.addView(imgConf);


                    btnConfirm = new TextView(CaAc.this);
                    btnConfirm.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT ));
                    btnConfirm.setTextColor(Color.parseColor("#75BA40"));
                    btnConfirm.setTextSize(15);
                    btnConfirm.setTypeface(Typeface.DEFAULT_BOLD);
                    btnConfirm.setText("CONFIRM");
                    linLayConfirm.addView(btnConfirm);

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
                                Toast.makeText(CaAc.this, "MRP should be greater than 0", Toast.LENGTH_LONG).show();
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
                                Toast.makeText(CaAc.this, "MRP should be greater than 0", Toast.LENGTH_LONG).show();
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
                                Toast.makeText(CaAc.this, "MRP should be greater than 0", Toast.LENGTH_LONG).show();
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



                    btnRemove.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            /*Toast.makeText(CaAc.this, finalTvProdName.getText().toString()+" Removed from Cart", Toast.LENGTH_LONG)
                                    .show();*/

                            dba.open();
                            String sql = "delete from temp where pid = '" + finalId + "'";
                            DataBaseAdapter.ourDatabase.execSQL(sql);

                            String sql1 = "delete from orderr where pid = '" + finalId
                                    + "'";
                            DataBaseAdapter.ourDatabase.execSQL(sql1);

                            String sql2 = "delete from bill where pid = '" + finalId
                                    + "'";
                            DataBaseAdapter.ourDatabase.execSQL(sql2);
                            dba.close();

                            Toast.makeText(CaAc.this, finalTvProdName.getText().toString()+" Removed from Cart", Toast.LENGTH_LONG)
                                    .show();
                            CreateListView();

                        }
                    });

                    final TextView finalBtnConfirm = btnConfirm;
                    btnConfirm.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {

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
                                Toast.makeText(CaAc.this, "MRP should be greater than 0", Toast.LENGTH_LONG).show();
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




                            String strSpin = spinCustomer.getSelectedItem()
                                    .toString().trim();

                            Calendar cal = Calendar.getInstance();
                            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
                            String strDate = sdf.format(cal.getTime());

                            if (!strSpin.equalsIgnoreCase("Select Customer")) {

                                if(strMRP.equalsIgnoreCase("0")){
                                    Toast.makeText(CaAc.this, "MRP should be greater than 0", Toast.LENGTH_LONG).show();
                                }else{

                                    dba.open();
                                    String custDet = "select id from customer where name='"
                                            + strSpin + "'";
                                    Cursor cur = DataBaseAdapter.ourDatabase.rawQuery(custDet,
                                            null);
                                    if (cur.getCount() > 0) {
                                        cur.moveToFirst();
                                        String custId = cur.getString(0).trim();

                                        if (finalBtnConfirm.getText().toString().equalsIgnoreCase("CONFIRM")) {


                                            String sql = "select * from temp where pid = '" + finalId + "'";
                                            Cursor curMrp = DataBaseAdapter.ourDatabase
                                                    .rawQuery(sql, null);
                                            if (curMrp.getCount() > 0) {

                                                ContentValues cv = new ContentValues();
                                                cv.put(OrderTable.KEY_DATE, strDate);
                                                cv.put(OrderTable.KEY_CUSTID, custId);
                                                cv.put(OrderTable.KEY_PRODUCTID, finalId);
                                                cv.put(OrderTable.KEY_QUANTITY,finalEdQty.getText().toString());
                                                cv.put(OrderTable.KEY_EMPNO, session.getEmpNo());
                                                cv.put(OrderTable.KEY_RATE, finalEdMRP.getText().toString());
                                                cv.put(OrderTable.KEY_DISCOUNT,finalEdDisc.getText().toString());
                                                cv.put(OrderTable.KEY_DISCPRICE, finalTvDiscount.getText().toString());

                                                if (act.equalsIgnoreCase("billing")) {
                                                    cv.put(BillingTable.KEY_ORDERID, "0");
                                                    cv.put(BillingTable.KEY_PAIDAMT, finalTvTotAmt.getText().toString().trim());

                                                    String oldPrd = "select * from "+BillingTable.DATABASE_TABLE+" where pid='"+finalId+"'";
                                                    Cursor oldPrdCur = DataBaseAdapter.ourDatabase.rawQuery(oldPrd,null);

                                                    Log.w("CBA", "oldPrdCur: " + oldPrdCur.getCount());

                                                    if(oldPrdCur.getCount() == 0) {
                                                        mod.insertdata(BillingTable.DATABASE_TABLE,
                                                                cv);
                                                    }else if(oldPrdCur.getCount() > 0){
                                                        DataBaseAdapter.ourDatabase.update(BillingTable.DATABASE_TABLE,cv,"pid='"+finalId+"'",null);
                                                    }
                                                    oldPrdCur.close();
                                                } else if (act.equalsIgnoreCase("order")) {
                                                    String oldPrd = "select * from "+OrderTable.DATABASE_TABLE+" where pid='"+finalId+"'";
                                                    Cursor oldPrdCur = DataBaseAdapter.ourDatabase.rawQuery(oldPrd,null);

                                                    Log.w("CBA", "oldPrdCur: " + oldPrdCur.getCount());

                                                    if(oldPrdCur.getCount() == 0) {
                                                        mod.insertdata(OrderTable.DATABASE_TABLE,
                                                                cv);
                                                    }else if(oldPrdCur.getCount() > 0){
                                                        DataBaseAdapter.ourDatabase.update(OrderTable.DATABASE_TABLE,cv,"pid='"+finalId+"'",null);
                                                    }
                                                    oldPrdCur.close();
                                                }

                                                ContentValues cvTemp = new ContentValues();
                                                cvTemp.put(OrderTempTable.KEY_PRICE,finalEdMRP.getText().toString());
                                                cvTemp.put(OrderTempTable.KEY_DISCOUNT,finalEdDisc.getText().toString());
                                                cvTemp.put(OrderTempTable.KEY_QUANTITY,finalEdQty.getText().toString());

                                                DataBaseAdapter.ourDatabase.update(OrderTempTable.DATABASE_TABLE,cvTemp,OrderTempTable.KEY_PRODUCTID+"='"+finalId+"'",null);

                                                Toast.makeText(CaAc.this, finalTvProdName.getText().toString()+" Confirmed", Toast.LENGTH_LONG)
                                                        .show();


                                            }
                                            curMrp.close();

                                           finalBtnConfirm.setText("CHANGE");
                                           finalEdQty.setEnabled(false);
                                           finalEdDisc.setEnabled(false);
                                           finalEdMRP.setEnabled(false);

                                        }else if (finalBtnConfirm.getText().toString().equalsIgnoreCase("CHANGE")) {
                                            finalBtnConfirm.setText("CONFIRM");
                                            finalEdQty.setEnabled(true);
                                            finalEdDisc.setEnabled(true);
                                            finalEdMRP.setEnabled(true);
                                        }

                                    }
                                    cur.close();
                                    dba.close();
                                }
                            } else {
                                Toast.makeText(CaAc.this, "Please Select Customer",Toast.LENGTH_LONG).show();
                            }




                        }
                    });


                }
			}
			cur.close();
			dba.close();
		}catch (Exception e){
			e.printStackTrace();
			LogError lErr = new LogError();
			lErr.appendLog(e.toString());
		}
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
                    Toast.makeText(CaAc.this, "Select Customer",
                            Toast.LENGTH_LONG).show();
                } else {

                    dba.open();
                    Cursor curData = null;
                    if (act.equalsIgnoreCase("billing")) {
                        curData = mod.getData(BillingTable.DATABASE_TABLE);
                        if (curData.getCount() > 0) {

                            Intent intent = new Intent(CaAc.this, BillPayment.class);
                            intent.putExtra("custName", spinCustomer.getSelectedItem().toString().trim());
                            startActivity(intent);
                            finish();

                        } else {
                            Toast.makeText(CaAc.this,
                                    "Please Confirm Items First", Toast.LENGTH_LONG)
                                    .show();
                        }
                        curData.close();
                    } else if (act.equalsIgnoreCase("order")) {

                        String delDate = txtDeliveryDate.getText().toString().trim();

                        if (delDate.length() > 0) {

                            curData = mod.getData(OrderTable.DATABASE_TABLE);

                            if (curData.getCount() > 0) {
                                Data = "";

                                while (curData.moveToNext()) {
                                    for (int i = 0; i < 8; i++) {

                                        if (i == 7) {
                                            Data = Data + curData.getString(i).trim()
                                                    + "," + delDate + ","
                                                    + session.getUserId() + "$";
                                        } else {
                                            Data = Data + curData.getString(i).trim()
                                                    + ",";
                                        }

                                    }
                                }

                                String logFlag = "";
                                GPSTracker gps = new GPSTracker(CaAc.this);
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

                                        Toast.makeText(CaAc.this,
                                                "Data Saved Offline...!!",
                                                Toast.LENGTH_LONG);

                                        Intent intent = new Intent(
                                                getApplicationContext(),
                                                MainActivity.class);
                                        startActivity(intent);
                                        finish();

                                    }

                                } else {
                                    gps.showSettingsAlert();
                                }

                            } else {
                                Toast.makeText(CaAc.this,
                                        "Please Confirm Items First", Toast.LENGTH_LONG)
                                        .show();
                            }

                            curData.close();
                        } else {
                            Toast.makeText(CaAc.this, "Select Delivery Date",
                                    Toast.LENGTH_LONG).show();
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
                    serverResponse = WebService.makeTransaction(Data, session.getBranchNo(), longi, lati, "saveBill","","");
				} else if (act.equalsIgnoreCase("order")) {
                    serverResponse = WebService.makeTransaction(Data, session.getBranchNo(), longi, lati, "saveOrder","","");
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
						CaAc.this);
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
										CaAc.this);
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

								Toast.makeText(CaAc.this,
										"Data Successfully Saved!!!",
										Toast.LENGTH_LONG);

								Intent intent = new Intent(
										getApplicationContext(),
										MainActivity.class);
								startActivity(intent);
								finish();

							}else if (serverResponse.getJSONObject(0)
									.getString("Status").trim()
									.equalsIgnoreCase("Failure")) {

								Toast.makeText(CaAc.this,
										"Failed to save Data",
										Toast.LENGTH_LONG);


							}

						}

						// Error status is true
					} else {

						Toast.makeText(CaAc.this, "Server Error",
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

}
