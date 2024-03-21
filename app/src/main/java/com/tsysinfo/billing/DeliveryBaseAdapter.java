package com.tsysinfo.billing;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.tsysinfo.billing.database.DataBaseAdapter;

import java.util.ArrayList;

public class DeliveryBaseAdapter extends BaseAdapter {
	private static ArrayList<DeliverySearch> searchArrayList;

	private LayoutInflater mInflater;
	
	Context context;
	
	DataBaseAdapter dba;
	

	public DeliveryBaseAdapter(Context context, ArrayList<DeliverySearch> results) {
		searchArrayList = results;
		mInflater = LayoutInflater.from(context);
		this.context = context;
		
		dba = new DataBaseAdapter(context);
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
			convertView = mInflater.inflate(R.layout.delivery_row, null);
			holder = new ViewHolder();
			
			holder.imgProduct = (ImageView) convertView.findViewById(R.id.list_image);
			holder.txtId = (TextView) convertView.findViewById(R.id.productId);
			holder.txtName = (TextView) convertView.findViewById(R.id.productName);
			holder.txtQty = (TextView) convertView.findViewById(R.id.productQuantity);
			holder.txtAmount = (TextView) convertView.findViewById(R.id.productPrice);
			holder.txtDiscount = (TextView) convertView.findViewById(R.id.productDiscount);
			holder.txtTotal = (TextView) convertView.findViewById(R.id.productTotal);
			holder.chkConfirm = (CheckBox) convertView.findViewById(R.id.checkConfirm);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.txtId.setText(searchArrayList.get(position).getId());
		holder.txtName.setText("Bill No : "+searchArrayList.get(position).getBno());// this is billNo
		holder.txtQty.setText(searchArrayList.get(position).getQty());
		holder.txtAmount.setText(searchArrayList.get(position).getPrice());
		holder.txtDiscount.setText(searchArrayList.get(position).getBdate()); // bill date
		holder.txtTotal.setText(searchArrayList.get(position).getTotal());

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
		final String productID = searchArrayList.get(position).getId();
		
		holder.chkConfirm.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO Auto-generated method stub
				String sql = "";
				dba.open();
				if(holder.chkConfirm.isChecked()){
					Log.w("DelBaseAdapter", "Cheaked "+productID);
					
					ContentValues args = new ContentValues();
				    args.put("status", "1");
				    DataBaseAdapter.ourDatabase.update("delivery", args, "id" + "='"+ productID+"'" , null);
				
				}else if(!holder.chkConfirm.isChecked()){
					Log.w("DelBaseAdapter", "UnCheaked "+productID);
					
					ContentValues args = new ContentValues();
				    args.put("status", "0");
				    DataBaseAdapter.ourDatabase.update("delivery", args, "id" + "='"+productID+"'" , null);
				
				}
				dba.close();
				
				
				
			}
		});
		
		
		return convertView;
	}

	static class ViewHolder {
		
		TextView txtId,txtName,txtQty,txtAmount,txtDiscount,txtTotal;
		CheckBox chkConfirm;
		ImageView imgProduct;
	}
}
