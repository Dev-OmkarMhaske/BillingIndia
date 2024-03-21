package com.tsysinfo.billing;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.tsysinfo.billing.database.DataBaseAdapter;
import com.tsysinfo.billing.database.Models;
import com.tsysinfo.billing.database.ReturnTable;
import com.tsysinfo.billing.database.SessionManager;

import java.util.ArrayList;

public class ReturnBaseAdapter extends BaseAdapter {
	private static ArrayList<ProductSearchResults> searchArrayList;

	private LayoutInflater mInflater;

	Context context;
	SessionManager session;
	DataBaseAdapter dba;
	Models mod;

	public ReturnBaseAdapter(Context context,ArrayList<ProductSearchResults> results) {
		searchArrayList = results;
		mInflater = LayoutInflater.from(context);
		this.context = context;
		session = new SessionManager(context);
		dba = new DataBaseAdapter(context);
		mod = new Models();

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
	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.return_product_row, null);
			holder = new ViewHolder();

			holder.txtName = (TextView) convertView
					.findViewById(R.id.productName);
			holder.txtDescription = (TextView) convertView
					.findViewById(R.id.productDescription);
			holder.txtPrice = (TextView) convertView
					.findViewById(R.id.productTotal);
			holder.txtMRP = (EditText) convertView
					.findViewById(R.id.productMRP);
			holder.txtDisc = (TextView) convertView
					.findViewById(R.id.productDiscPrice);
			holder.txtQuantity = (EditText) convertView
					.findViewById(R.id.productQuantity);
			holder.txtDiscount = (EditText) convertView
					.findViewById(R.id.productDiscount);
			holder.txtQuantity = (EditText) convertView
					.findViewById(R.id.productQuantity);
			holder.imgProduct = (ImageView) convertView
					.findViewById(R.id.list_image);
			holder.btnAddToCart = (ImageView) convertView
					.findViewById(R.id.productAddToCart);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		holder.txtName.setText(searchArrayList.get(position).getName());
		holder.txtDescription.setText(searchArrayList.get(position)
				.getDescription());
		holder.txtMRP.setText(searchArrayList.get(position).getPrice());
		
		final String productID = searchArrayList.get(position).getID();
		final String productName = searchArrayList.get(position).getName();
		final String productDesc = searchArrayList.get(position).getDescription();
		final String productImage = searchArrayList.get(position).getImage();
		final String productPrice = searchArrayList.get(position).getPrice();
		

		try {

			if (!searchArrayList.get(position).getImage().equalsIgnoreCase("")) {
				Bitmap image = BitmapFactory.decodeFile(searchArrayList.get(
						position).getImage());
				image = Bitmap.createScaledBitmap(image, 200, 200, false);
				holder.imgProduct.setImageBitmap(image);
			}

		}catch(Exception e){
			e.printStackTrace();
		}


        holder.txtMRP
                .setOnFocusChangeListener(new View.OnFocusChangeListener() {

                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        // TODO Auto-generated method stub
                        String strDisc = holder.txtDiscount.getText()
                                .toString().trim();
                        String strQty = holder.txtQuantity.getText().toString()
                                .trim();
                        String strMRP = holder.txtMRP.getText().toString()
                                .trim();

                        if (strDisc.equalsIgnoreCase("")
                                || strQty.equalsIgnoreCase("")
                                || strMRP.equalsIgnoreCase("")) {

                        }else if(strMRP.equalsIgnoreCase("0")){
                            Toast.makeText(context, "MRP should be greater than 0", Toast.LENGTH_LONG).show();
                        }else{

                            double tot = Float.parseFloat(strQty)
                                    * Float.parseFloat(strMRP);

                            double dis = tot
                                    * (Float.parseFloat(strDisc) * 0.01);

                            tot = tot - dis;
                            tot = Math.round(tot * 100.0) / 100.0;
                            holder.txtDisc.setText(String.valueOf(dis));
                            holder.txtPrice.setText(String.valueOf(tot));

                        }

                    }
                });

        holder.txtQuantity
                .setOnFocusChangeListener(new View.OnFocusChangeListener() {

                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        // TODO Auto-generated method stub
                        String strDisc = holder.txtDiscount.getText()
                                .toString().trim();
                        String strQty = holder.txtQuantity.getText().toString()
                                .trim();
                        String strMRP = holder.txtMRP.getText().toString()
                                .trim();

                        if (strDisc.equalsIgnoreCase("")
                                || strQty.equalsIgnoreCase("")
                                || strMRP.equalsIgnoreCase("")) {

                        }else if(strMRP.equalsIgnoreCase("0")){
                            Toast.makeText(context, "MRP should be greater than 0", Toast.LENGTH_LONG).show();
                        }else{

                            double tot = Float.parseFloat(strQty)
                                    * Float.parseFloat(strMRP);

                            double dis = tot
                                    * (Float.parseFloat(strDisc) * 0.01);

                            tot = tot - dis;
                            tot = Math.round(tot * 100.0) / 100.0;
                            holder.txtDisc.setText(String.valueOf(dis));
                            holder.txtPrice.setText(String.valueOf(tot));

                        }

                    }
                });

        holder.txtDiscount
                .setOnFocusChangeListener(new View.OnFocusChangeListener() {

                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        // TODO Auto-generated method stub
                        String strDisc = holder.txtDiscount.getText()
                                .toString().trim();
                        String strQty = holder.txtQuantity.getText().toString()
                                .trim();
                        String strMRP = holder.txtMRP.getText().toString()
                                .trim();

                        if (strDisc.equalsIgnoreCase("")
                                || strQty.equalsIgnoreCase("")
                                || strMRP.equalsIgnoreCase("")) {

                        }else if(strMRP.equalsIgnoreCase("0")){
                            Toast.makeText(context, "MRP should be greater than 0", Toast.LENGTH_LONG).show();
                        }else{

                            double tot = Float.parseFloat(strQty) * Float.parseFloat(strMRP);

                            double dis = tot * (Float.parseFloat(strDisc) * 0.01);

                            tot = tot - dis;

                            tot = Math.round(tot * 100.0) / 100.0;

                            holder.txtDisc.setText(String.valueOf(dis));
                            holder.txtPrice.setText(String.valueOf(tot));

                        }

                    }
                });

		
			holder.btnAddToCart.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub

					String strDisc = holder.txtDiscount.getText()
							.toString().trim();
					String strQty = holder.txtQuantity.getText().toString()
							.trim();
					String strMRP = holder.txtMRP.getText().toString()
							.trim();

					if (strDisc.equalsIgnoreCase("")
							|| strQty.equalsIgnoreCase("")
							|| strMRP.equalsIgnoreCase("")) {

					}else if(strMRP.equalsIgnoreCase("0")){
						Toast.makeText(context, "MRP should be greater than 0", Toast.LENGTH_LONG).show();
					}else if(strQty.equalsIgnoreCase("0"))
					{
						Toast.makeText(context, "Please enter quantity", Toast.LENGTH_LONG).show();

					}else{

						double tot = Float.parseFloat(strQty)
								* Float.parseFloat(strMRP);

						double dis = tot
								* (Float.parseFloat(strDisc) * 0.01);

						tot = tot - dis;
						tot = Math.round(tot * 100.0) / 100.0;
						holder.txtDisc.setText(String.valueOf(dis));
						holder.txtPrice.setText(String.valueOf(tot));

					}


					String strSpin = ReturnActivity.spinCustomer.getSelectedItem()
							.toString().trim();
					if (!strSpin.equalsIgnoreCase("Select Customer")) {
						dba.open();
						String sql = "select * from retutntab where pid = '" + productID + "' and qty = '" + holder.txtQuantity.getText().toString().trim() + "'";
						Cursor cur = DataBaseAdapter.ourDatabase.rawQuery(sql, null);
						if (cur.getCount() > 0) {
							Toast.makeText(context, "Product already in cart. Choose" +
									" another product", Toast.LENGTH_LONG)
									.show();
						} else {

							String del = "delete from retutntab where pid = '" + productID
									+ "'";
							DataBaseAdapter.ourDatabase.rawQuery(del, null);

							ContentValues cv = new ContentValues();
							cv.put("pid", productID);
							cv.put("custid", ReturnActivity.custid);
							cv.put("empid", session.getEmpNo());
							cv.put("qty", holder.txtQuantity.getText().toString().trim());
							cv.put("mrp", holder.txtMRP.getText().toString().trim());
							cv.put("disc", holder.txtDisc.getText().toString().trim());
							mod.insertdata(ReturnTable.DATABASE_TABLE, cv);

							((ReturnActivity) context).doIncrease();
						}
						cur.close();
						dba.close();


						Toast.makeText(context, productName + " Added To Return Cart", Toast.LENGTH_LONG)
								.show();
					}else{
						Toast.makeText(context, "Please Select Customer", Toast.LENGTH_LONG)
								.show();
					}
				}
			});

		

		return convertView;
	}

	static class ViewHolder {

		TextView txtName;
		TextView txtDescription;
		TextView txtPrice, txtDisc;
		EditText txtQuantity, txtDiscount,txtMRP;
		ImageView btnAddToCart, imgProduct;
	}
}
