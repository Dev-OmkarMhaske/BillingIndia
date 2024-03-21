package com.tsysinfo.billing;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.view.menu.MenuView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.tsysinfo.billing.database.BillingTable;
import com.tsysinfo.billing.database.DataBaseAdapter;
import com.tsysinfo.billing.database.Models;
import com.tsysinfo.billing.database.OrderTable;
import com.tsysinfo.billing.database.OrderTempTable;
import com.tsysinfo.billing.database.SessionManager;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class CartBaseAdapter extends BaseAdapter {
	private static ArrayList<ProductSearchResults> searchArrayList;
	private LayoutInflater mInflater;
	double gst=0.0;
	Context context;
	DataBaseAdapter dba;
	Models mod;
	SessionManager session;

	public CartBaseAdapter(Context context,
			ArrayList<ProductSearchResults> results) {
		searchArrayList = results;
		mInflater = LayoutInflater.from(context);
		this.context = context;
		session = new SessionManager(context);
		dba = new DataBaseAdapter(context);
		mod = new Models();

	}


	@Override
	public int getViewTypeCount() {
		return getCount();
	}

	@Override
	public int getItemViewType(int position) {
		return position;
	}

	public int getCount() {
		return searchArrayList.size();
	}

	public Object getItem(int position) {
		return searchArrayList.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	@SuppressLint("InflateParams")
	public View getView(final int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.cart_row, null);

			holder = new ViewHolder();
			holder.imgProduct = (ImageView) convertView.findViewById(R.id.list_image);
			holder.imgTick = (ImageView) convertView.findViewById(R.id.imageTick);
			holder.txtName = (TextView) convertView.findViewById(R.id.productName);
			holder.txtDescription = (TextView) convertView.findViewById(R.id.productDescription);
			holder.taxAmount = (TextView) convertView.findViewById(R.id.productTaxAmt);
			holder.txtMRP = (EditText) convertView.findViewById(R.id.productMRP);
			holder.rot = (TextView) convertView.findViewById(R.id.productROT);
			holder.prate = (TextView) convertView.findViewById(R.id.productPRate);
			holder.txtDisc = (TextView) convertView.findViewById(R.id.productDiscPrice);
			holder.txtQuantity = (EditText) convertView.findViewById(R.id.productQuantity);
			holder.txtDiscount = (EditText) convertView.findViewById(R.id.productDiscount);
			holder.btnConfirm = (TextView) convertView.findViewById(R.id.btnConfirm);
			holder.btnRemove = (TextView) convertView.findViewById(R.id.btnRemove);
			holder.DP = (TextView) convertView.findViewById(R.id.DP);
			holder.TotAmt = (TextView) convertView.findViewById(R.id.tot);
			holder.editDenom=convertView.findViewById(R.id.productDenom);
			holder.checkBox = (CheckBox) convertView.findViewById(R.id.check);
			holder.actualGst = (TextView) convertView.findViewById(R.id.actualGst);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		holder.txtName.setText(searchArrayList.get(position).getName());
		holder.txtDescription.setText(searchArrayList.get(position).getId());
		holder.rot.setText(""+Double.parseDouble(searchArrayList.get(position).getRot()));
		holder.prate.setText(""+Double.parseDouble(searchArrayList.get(position).getPrate()));
		holder.DP.setText(""+Double.parseDouble(searchArrayList.get(position).getDp()));

		dba.open();
		final String productID = searchArrayList.get(position).getID();
		String query="select * from temp where pid='"+productID+"'";
		Cursor cursor=DataBaseAdapter.ourDatabase.rawQuery(query,null);
		cursor.moveToFirst();
		holder.txtQuantity.setText(""+Double.parseDouble(cursor.getString(cursor.getColumnIndex("qty"))));
		dba.close();
		holder.txtMRP.setText(searchArrayList.get(position).getPrice());


		dba.open();

		final String Qtyold=holder.txtQuantity.getText().toString().trim();
		holder.editDenom.setText(searchArrayList.get(position).getUnit());

		String isChecked="";
		String s="Select * from orderr where pid = '" + productID
				+ "'";

		Cursor cursor1=DataBaseAdapter.ourDatabase.rawQuery(s,null);
		while (cursor1.moveToNext())
		{
			isChecked = cursor1.getString(cursor1.getColumnIndex("checked"));

		}
		dba.close();
		// holder.txtQuantity.setText("1");

		if (isChecked.equalsIgnoreCase("true"))
		{
			holder.checkBox.setChecked(true);
		}else {
			holder.checkBox.setChecked(false);
		}


		holder.imgTick.setEnabled(false);
		holder.imgTick.setVisibility(View.GONE);
		
		double grossAmt = Float.parseFloat(holder.txtQuantity.getText().toString().trim()) * Float.parseFloat(holder.DP.getText().toString().trim());

		double dis = grossAmt * (Float.parseFloat(holder.txtDiscount.getText().toString().trim()) * 0.01);

		grossAmt = grossAmt - dis;
        grossAmt = Math.round(grossAmt * 100.0) / 100.0;

		double TaxPer=Double.parseDouble(searchArrayList.get(position).getRot());
		gst= ((grossAmt*TaxPer)/100);
		holder.actualGst.setText(new DecimalFormat("#.##").format(gst));
		holder.TotAmt.setText(new DecimalFormat("#.##").format((grossAmt+gst)));

		holder.txtDisc.setText(String.valueOf(dis));
		holder.taxAmount.setText(new DecimalFormat("#.##").format(grossAmt));

		//NILIMA...

		holder.txtQuantity.setOnKeyListener(new View.OnKeyListener()
		{
			public boolean onKey(View v, int keyCode, KeyEvent event)
			{
				if (event.getAction() == KeyEvent.ACTION_DOWN)
				{
					switch (keyCode)
					{
						case KeyEvent.KEYCODE_DPAD_CENTER:
						case KeyEvent.KEYCODE_ENTER:

							CartActivity.adapterGross = Double.parseDouble(holder.taxAmount.getText().toString());
							CartActivity.adapterGst = Double.parseDouble(holder.actualGst.getText().toString());

							if(context instanceof CartActivity){
								((CartActivity)context).setFooter();
							}

							final String Qty=holder.txtQuantity.getText().toString().trim();
							Log.e("oldQty",""+Qtyold);
							Log.e("newQty",""+Qty);
							String Dp = holder.DP.getText().toString().trim();

							double TaxAmt = Float.parseFloat(Qty) * Float.parseFloat(Dp);

							holder.taxAmount.setText(new DecimalFormat("#.##").format(TaxAmt));

							double GstPercent= Double.parseDouble(holder.rot.getText().toString().trim());
							double Gst=Math.round(TaxAmt*(GstPercent%100))/100.0;
							holder.actualGst.setText(String.valueOf(Gst));
							holder.TotAmt.setText(new DecimalFormat("#.##").format((TaxAmt+Gst)));

							boolean b=true;

							dba.open();


							String sql1 = "update  orderr set qty='"+Qty+"',rate='"+ Dp+ "',rot= ' " + GstPercent + "',checked='"+ b +"' where pid = '" + productID
									+ "'";
							DataBaseAdapter.ourDatabase.execSQL(sql1);

							dba.close();

							dba.open();

							String sql2 = "update temp set qty='"+Qty+"',dp='"+Dp+"',rot='"+GstPercent+"' where pid = '" + productID
									+ "'";
							DataBaseAdapter.ourDatabase.execSQL(sql2);

							dba.close();

							CartActivity.adapterGross = -Double.parseDouble(holder.taxAmount.getText().toString());
							CartActivity.adapterGst = -Double.parseDouble(holder.actualGst.getText().toString());

							InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
							imm.hideSoftInputFromWindow(holder.txtQuantity.getWindowToken(), 0);

							if(context instanceof CartActivity){
								((CartActivity)context).setFooter();
							}

							return true;
						default:
							break;
					}
				}
				return false;
			}
		});

		holder.txtMRP.setOnFocusChangeListener(new OnFocusChangeListener() {

					@Override
					public void onFocusChange(View v, boolean hasFocus) {
						// TODO Auto-generated method stub
						String strDisc = holder.txtDiscount.getText().toString().trim();
						String strQty = holder.txtQuantity.getText().toString().trim();
						String Dp = holder.DP.getText().toString().trim();

						if (strDisc.equalsIgnoreCase("") || strQty.equalsIgnoreCase("") || Dp.equalsIgnoreCase("")) {

						}else if(Dp.equalsIgnoreCase("0")){
							Toast.makeText(context, "DP should be greater than 0", Toast.LENGTH_LONG).show();
						}else{

							double tot = Float.parseFloat(strQty) * Float.parseFloat(Dp);

							double dis = tot * (Float.parseFloat(strDisc) * 0.01);

							tot = tot - dis;
							tot = Math.round(tot * 100.0) / 100.0;
							holder.txtDisc.setText(String.valueOf(dis));
							holder.taxAmount.setText(String.valueOf(tot));

						}

					}
				});

		holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
				Log.e("UnAdChecked","fd");

					dba.open();

					String sql1 = "update  orderr set checked='"+b+"' where pid = '" + productID + "'";
					DataBaseAdapter.ourDatabase.execSQL(sql1);

					dba.close();

					searchArrayList.get(position).setChecked(b);

					if (!b)
					{
						Log.w("Unchecked","fd");
						CartActivity.adapterGross = Double.parseDouble(holder.taxAmount.getText().toString());
						CartActivity.adapterGst = Double.parseDouble(holder.actualGst.getText().toString());

					}else {

						Log.w("checked","fd");
						CartActivity.adapterGross = -Double.parseDouble(holder.taxAmount.getText().toString());
						CartActivity.adapterGst = -Double.parseDouble(holder.actualGst.getText().toString());
					}

				if(context instanceof CartActivity){
					((CartActivity)context).setFooter();
				}
			}
		});

		holder.txtQuantity
				.setOnFocusChangeListener(new OnFocusChangeListener() {

					@Override
					public void onFocusChange(View v, boolean hasFocus) {
						// TODO Auto-generated method stub
						String strDisc = holder.txtDiscount.getText().toString().trim();
						String strQty = holder.txtQuantity.getText().toString().trim();
						String Dp = holder.DP.getText().toString().trim();

						if (strDisc.equalsIgnoreCase("")
								|| strQty.equalsIgnoreCase("")
								|| Dp.equalsIgnoreCase("")) {

						}else if(Dp.equalsIgnoreCase("0")){
							Toast.makeText(context, "MRP should be greater than 0", Toast.LENGTH_LONG).show();
						}else{

//nilima
							/*double TaxAmt = Float.parseFloat(strQty)
									* Float.parseFloat(Dp);

							double dis = TaxAmt
									* (Float.parseFloat(strDisc) * 0.01);

							TaxAmt = TaxAmt - dis;
                            TaxAmt = Math.round(TaxAmt * 100.0) / 100.0;



							holder.txtDisc.setText(String.valueOf(dis));
							holder.taxAmount.setText(String.valueOf(TaxAmt));

							double TotTaxPer=Double.parseDouble(strQty)*Double.parseDouble(searchArrayList.get(position).getRot());
							double gst= ((TaxAmt*TotTaxPer)/100);
							holder.actualGst.setText(String.valueOf(gst));
							holder.TotAmt.setText(new DecimalFormat("#.##").format((TaxAmt+gst)));*/

						}

					}
				});



		holder.txtDiscount.setOnFocusChangeListener(new OnFocusChangeListener() {

					@Override
					public void onFocusChange(View v, boolean hasFocus) {
						// TODO Auto-generated method stub
						String strDisc = holder.txtDiscount.getText().toString().trim();
						String strQty = holder.txtQuantity.getText().toString().trim();
						String strMRP = holder.txtMRP.getText().toString().trim();

						if (strDisc.equalsIgnoreCase("") || strQty.equalsIgnoreCase("") || strMRP.equalsIgnoreCase("")) {

						}else if(strMRP.equalsIgnoreCase("0")){
							Toast.makeText(context, "MRP should be greater than 0", Toast.LENGTH_LONG).show();
						}else{

							double tot = Float.parseFloat(strQty) * Float.parseFloat(strMRP);

							double dis = tot * (Float.parseFloat(strDisc) * 0.01);

							tot = tot - dis;

                            tot = Math.round(tot * 100.0) / 100.0;

							holder.txtDisc.setText(String.valueOf(dis));
							holder.taxAmount.setText(String.valueOf(tot));

						}

					}
				});

		holder.btnRemove.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				dba.open();
				String sql = "delete from temp where pid = '" + productID + "'";
				DataBaseAdapter.ourDatabase.execSQL(sql);

				String sql1 = "delete from orderr where pid = '" + productID + "'";
				DataBaseAdapter.ourDatabase.execSQL(sql1);

				String sql2 = "delete from bill where pid = '" + productID + "'";
				DataBaseAdapter.ourDatabase.execSQL(sql2);
				dba.close();

				Toast.makeText(context, holder.txtName.getText().toString()+" Removed from Cart", Toast.LENGTH_LONG).show();

				Intent intent = new Intent(context, CartActivity.class);
				intent.putExtra("act", CartActivity.act);
				context.startActivity(intent);
				((Activity) context).finish();

			}
		});

		holder.btnConfirm.setOnClickListener(new OnClickListener() {

			@SuppressLint("SimpleDateFormat")
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String strDisc = holder.txtDiscount.getText().toString().trim();
				String strQty = holder.txtQuantity.getText().toString().trim();
				String DP = holder.DP.getText().toString().trim();

				if (strDisc.equalsIgnoreCase("") || strQty.equalsIgnoreCase("") || DP.equalsIgnoreCase("")) {

				}else if(DP.equalsIgnoreCase("0")){
					Toast.makeText(context, "MRP should be greater than 0", Toast.LENGTH_LONG).show();
				}else{

					double Taxamt = Float.parseFloat(strQty) * Float.parseFloat(DP);

					double dis = Taxamt * (Float.parseFloat(strDisc) * 0.01);

					Taxamt = Taxamt - dis;
					Taxamt = Math.round(Taxamt * 100.0) / 100.0;
					holder.txtDisc.setText(String.valueOf(dis));
					holder.taxAmount.setText(String.valueOf(Taxamt));

					double TotTaxPer=Double.parseDouble(strQty)*Double.parseDouble(searchArrayList.get(position).getRot());
					double gst= ((Taxamt*TotTaxPer)/100);
					holder.actualGst.setText(String.valueOf(gst));
					holder.TotAmt.setText(new DecimalFormat("#.##").format((Taxamt+gst)));

				}


				String strSpin = CartActivity.spinCustomer.getSelectedItem().toString().trim();

				Calendar cal = Calendar.getInstance();
				SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
				String strDate = sdf.format(cal.getTime());

				if (!strSpin.equalsIgnoreCase("Select Customer")) {
					
					if(DP.equalsIgnoreCase("0")){
						Toast.makeText(context, "MRP should be greater than 0", Toast.LENGTH_LONG).show();
					}else{
					
					dba.open();
					String custDet = "select id from customer where name='" + strSpin + "'";
					Cursor cur = DataBaseAdapter.ourDatabase.rawQuery(custDet, null);
					if (cur.getCount() > 0) {
						cur.moveToFirst();
						String custId = cur.getString(0).trim();
						if (!holder.imgTick.isEnabled()) {
							String sql = "select * from temp where pid = '" + productID + "'";
							Cursor curMrp = DataBaseAdapter.ourDatabase.rawQuery(sql, null);
							if (curMrp.getCount() > 0) {
                                    ContentValues cv = new ContentValues();
                                    cv.put(OrderTable.KEY_DATE, strDate);
                                    cv.put(OrderTable.KEY_CUSTID, custId);
                                    cv.put(OrderTable.KEY_PRODUCTID, productID);
                                    cv.put(OrderTable.KEY_QUANTITY, holder.txtQuantity.getText().toString());
                                    cv.put(OrderTable.KEY_EMPNO, session.getEmpNo());
                                    cv.put(OrderTable.KEY_RATE, holder.DP.getText().toString());
                                    cv.put(OrderTable.KEY_DISCOUNT, holder.txtDiscount.getText().toString());
                                    cv.put(OrderTable.KEY_DISCPRICE, holder.txtDisc.getText().toString());
									cv.put(OrderTable.KEY_PRATE, holder.prate.getText().toString());
									cv.put(OrderTable.KEY_ROT, holder.rot.getText().toString());
                                    if (CartActivity.act.equalsIgnoreCase("billing")) {
                                        cv.put(BillingTable.KEY_ORDERID, "0");
                                        cv.put(BillingTable.KEY_PAIDAMT, holder.taxAmount.getText().toString().trim());

                                        String oldPrd = "select * from "+BillingTable.DATABASE_TABLE+" where pid='"+productID+"'";
                                        Cursor oldPrdCur = DataBaseAdapter.ourDatabase.rawQuery(oldPrd,null);

                                        Log.w("CBA", "oldPrdCur: " + oldPrdCur.getCount());

                                        if(oldPrdCur.getCount() == 0) {
                                            mod.insertdata(BillingTable.DATABASE_TABLE, cv);
                                        }else if(oldPrdCur.getCount() > 0){
                                            DataBaseAdapter.ourDatabase.update(BillingTable.DATABASE_TABLE,cv,"pid='"+productID+"'",null);
                                        }
                                        oldPrdCur.close();
                                    } else if (CartActivity.act.equalsIgnoreCase("order")) {
                                        String oldPrd = "select * from "+OrderTable.DATABASE_TABLE+" where pid='"+productID+"'";
                                        Cursor oldPrdCur = DataBaseAdapter.ourDatabase.rawQuery(oldPrd,null);

                                        Log.w("CBA", "oldPrdCur: " + oldPrdCur.getCount());

                                        if(oldPrdCur.getCount() == 0) {
                                        mod.insertdata(OrderTable.DATABASE_TABLE, cv);
                                        }else if(oldPrdCur.getCount() > 0){
                                            DataBaseAdapter.ourDatabase.update(OrderTable.DATABASE_TABLE,cv,"pid='"+productID+"'",null);
                                        }
                                        oldPrdCur.close();
                                    }

                                    Toast.makeText(context, holder.txtName.getText().toString()+" Confirmed", Toast.LENGTH_LONG).show();


							}
							curMrp.close();
							
							holder.imgTick.setEnabled(true);
							holder.imgTick.setVisibility(View.VISIBLE);
                            holder.btnConfirm.setText("CHANGE");
                            holder.txtQuantity.setEnabled(false);
                            holder.txtDiscount.setEnabled(false);
                            holder.txtMRP.setEnabled(false);

						}else{
                            holder.imgTick.setEnabled(false);
                            holder.imgTick.setVisibility(View.GONE);
                            holder.btnConfirm.setText("CONFIRM");
                            holder.txtQuantity.setEnabled(true);
                            holder.txtDiscount.setEnabled(true);
                            holder.txtMRP.setEnabled(true);
                        }

					}
					cur.close();
					dba.close();
					}
				} else {
					Toast.makeText(context, "Please Select Customer", Toast.LENGTH_LONG).show();
				}

			}
		});

		return convertView;
	}

	static class ViewHolder {

		TextView txtName;
		TextView txtDescription;
		TextView taxAmount, txtDisc;
		TextView rot, prate;
		EditText txtQuantity, txtDiscount,txtMRP;
		TextView btnConfirm, btnRemove;
		TextView DP, TotAmt,actualGst;
		CheckBox checkBox;
		ImageView imgProduct, imgTick;
		EditText editDenom;

	}
}
