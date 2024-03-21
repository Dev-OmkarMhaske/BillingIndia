package com.tsysinfo.billing;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.tsysinfo.billing.database.DataBaseAdapter;
import com.tsysinfo.billing.database.Models;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ProductBaseAdapter extends BaseAdapter implements Filterable {
    private static ArrayList<ProductSearchResults> searchArrayList;
    private static ArrayList<ProductSearchResults> orignalSearchArrayList;
    double stock = 0.0;
    private LayoutInflater mInflater;
    Context context;
    DataBaseAdapter dba;
    Models mod;

    ProductSearchResults temp = null;
    private ProductFilter productFilter;

    public void resetData() {
        searchArrayList = orignalSearchArrayList;
    }

    public ProductBaseAdapter(Context context, ArrayList<ProductSearchResults> results) {
        searchArrayList = results;
        mInflater = LayoutInflater.from(context);
        this.context = context;
        orignalSearchArrayList = results;
        dba = new DataBaseAdapter(context);
        mod = new Models();
        getFilter();
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

    @SuppressLint({"InflateParams", "ResourceAsColor"})
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        Log.d("getView", "============================ getView called =====================" + position + 1);
        String quantity = " " + position;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.product_row, null);
            holder = new ViewHolder();

            holder.txtName = (TextView) convertView.findViewById(R.id.productName);
            holder.txtDescription = (TextView) convertView.findViewById(R.id.productDescription);
            holder.txtPrice = (TextView) convertView.findViewById(R.id.productPrice);
            holder.imgProduct = (ImageView) convertView.findViewById(R.id.list_image);
            holder.rot = (TextView) convertView.findViewById(R.id.productROT);
            holder.productStock = (TextView) convertView.findViewById(R.id.productStock);
            holder.prate = (TextView) convertView.findViewById(R.id.productPRate);
            holder.btnAddToCart = (ImageView) convertView.findViewById(R.id.productAddToCart);
            holder.txtRemark = convertView.findViewById(R.id.productRemark);
            holder.quantity = (EditText) convertView.findViewById(R.id.Quantity);
            holder.qt = (LinearLayout) convertView.findViewById(R.id.qt);
            holder.spinnerUnit = (Spinner) convertView.findViewById(R.id.spinnUnit);

			/*List<String> categories = new ArrayList<String>();
			for(int i=1;i<=100;i++)

			{
				categories.add(String.valueOf(i));

			}
			ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, categories);

			// Drop down layout style - list view with radio button
			dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

			// attaching data adapter to spinner
			holder.quantity.setAdapter(dataAdapter);
*/
            convertView.setTag(holder);

        } else {

            holder = (ViewHolder) convertView.getTag();

        }

        holder.rot.setText(searchArrayList.get(position).getRot() + " (%)");
        holder.prate.setText(searchArrayList.get(position).getPrate());
        try {
            stock = Double.parseDouble(searchArrayList.get(position).getStcok());
        } catch (NumberFormatException e) {
            stock = 0; // your default value
        }

        holder.productStock.setText(new Double(stock).intValue() + "");

        if (stock > 0) {
            holder.txtName.setText(searchArrayList.get(position).getName());
            holder.txtName.setTextColor(Color.BLUE);
            holder.productStock.setTextColor(Color.BLUE);
        } else {
            holder.txtName.setText(searchArrayList.get(position).getName());
            holder.txtName.setTextColor(Color.RED);
            holder.productStock.setTextColor(Color.RED);
        }
        holder.txtDescription.setText("Rs. " + String.valueOf(Double.parseDouble(searchArrayList.get(position).getDp())));
        holder.txtPrice.setText("Rs. " + String.valueOf(Double.parseDouble(searchArrayList.get(position).getPrice())));

        final String productID = searchArrayList.get(position).getID();
        final String productName = searchArrayList.get(position).getName();
        final String productDesc = searchArrayList.get(position).getDescription();
        final String productImage = searchArrayList.get(position).getImage();
        final String productPrice = searchArrayList.get(position).getPrice();
        final String dp = searchArrayList.get(position).getDp();


        ArrayList<String> arrayList = new ArrayList();

        dba.open();
        String sql = "select distinct uqc from products";
        Cursor cur = DataBaseAdapter.ourDatabase.rawQuery(sql, null);
        if (cur.getCount() > 0) {

            while (cur.moveToNext()) {
                arrayList.add(cur.getString(0));


            }
        } else {

        }
        cur.close();
        dba.close();

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, arrayList);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        holder.spinnerUnit.setAdapter(dataAdapter);

        holder.spinnerUnit.setSelection(getIndex(holder.spinnerUnit, "PCS"));

        holder.imgProduct.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String p = Environment.getExternalStorageDirectory() + "/ItemImages/" + searchArrayList.get(position).getImage() + "";
                Intent intent = new Intent(context, ZoomImageViewActivity.class);
                intent.putExtra("img", p);
                context.startActivity(intent);

            }
        });
        try {

            String path = Environment.getExternalStorageDirectory() + "/ItemImages/" + searchArrayList.get(position).getImage() + "";
            File file = new File(path);
            Log.w("PBA", "Image: " + path);
            try {

                Picasso.get().load(file).fit().placeholder(R.drawable.noimage).into(holder.imgProduct);

            } catch (Exception e) {

            }


        } catch (Exception e) {
            e.printStackTrace();
        }


        if (searchArrayList.get(position).getAct().equalsIgnoreCase("product")) {

            holder.btnAddToCart.setVisibility(View.GONE);
            holder.qt.setVisibility(View.GONE);

        } else if (searchArrayList.get(position).getAct().equalsIgnoreCase("order")) {

            holder.btnAddToCart.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    double q = 1;
                    try {
                        q = Double.parseDouble(holder.quantity.getText().toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                        q = 0;
                    }
                    if (holder.quantity.getText().toString().equalsIgnoreCase("") || q == 0) {

                        Toast.makeText(context, "Please add quantity", Toast.LENGTH_SHORT).show();

                    } else {

                        dba.open();
                        String sql = "select * from temp where pid = '" + productID + "'";
                        Cursor cur = DataBaseAdapter.ourDatabase.rawQuery(sql, null);
                        if (cur.getCount() > 0) {
                            Toast.makeText(context, "Product already in cart. Choose" + " another product", Toast.LENGTH_LONG).show();
                        } else {
                            ContentValues cv = new ContentValues();
                            cv.put("pid", productID);
                            cv.put("pname", productName);
                            cv.put("pdesc", productDesc);
                            cv.put("image", productImage);
                            cv.put("price", productPrice);
                            cv.put("rot", searchArrayList.get(position).getRot());
                            cv.put("prate", holder.prate.getText().toString());
                            cv.put("qty", holder.quantity.getText().toString());
                            cv.put("dp", dp);
                            cv.put("csz", searchArrayList.get(position).getCsz());
                            cv.put("discount", "0");
                            cv.put("unit", holder.spinnerUnit.getSelectedItem().toString());
                            cv.put("remark", holder.txtRemark.getText().toString());
                            mod.insertdata("temp", cv);

                            if (context instanceof OrderActivity)
                                ((OrderActivity) context).doIncrease();

							/*if (context instanceof  UNOrderActivity)
								((UNOrderActivity) context).doIncrease();*/
                        }
                        cur.close();
                        dba.close();


                        Toast.makeText(context, productName + " Added To Cart", Toast.LENGTH_SHORT).show();

                    }
                }
            });

        } else if (searchArrayList.get(position).getAct().equalsIgnoreCase("billing")) {
            holder.btnAddToCart.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    int q = 1;
                    try {
                        q = Integer.parseInt(holder.quantity.getText().toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                        q = 0;
                    }
                    if (holder.quantity.getText().toString().equalsIgnoreCase("") || q == 0) {
                        Toast.makeText(context, "Please add quantity", Toast.LENGTH_SHORT).show();
                    } else {
                        dba.open();
                        String sql = "select * from temp where pid = '" + productID + "'";
                        Cursor cur = DataBaseAdapter.ourDatabase.rawQuery(sql, null);
                        if (cur.getCount() > 0) {
                            Toast.makeText(context, "Product already in cart. Choose" + " another product", Toast.LENGTH_LONG).show();
                        } else {
                            ContentValues cv = new ContentValues();
                            cv.put("pid", productID);
                            cv.put("pname", productName);
                            cv.put("pdesc", productDesc);
                            cv.put("image", productImage);
                            cv.put("price", productPrice);
                            cv.put("qty", holder.quantity.getText().toString());
                            cv.put("discount", "0");
                            mod.insertdata("temp", cv);

                            ((BillingActivity) context).doIncrease();
                        }
                        cur.close();
                        dba.close();
                        Toast.makeText(context, productName + " Added To Cart", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        return convertView;
    }

    //private method of your class
    private int getIndex(Spinner spinner, String myString) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(myString)) {
                return i;
            }
        }

        return 0;
    }

    static class ViewHolder {

        TextView txtName;
        TextView txtDescription, productStock;
        TextView txtPrice;
        ImageView btnAddToCart, imgProduct;
        EditText quantity;
        LinearLayout qt;
        Spinner spinnerUnit;
        TextView rot, prate;
        EditText txtRemark;
    }

    @Override
    public Filter getFilter() {
        if (productFilter == null)
            productFilter = new ProductFilter();

        return productFilter;
    }

    private class ProductFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            // We implement here the filter logic

            if (constraint == null || constraint.length() == 0) {
                // No filter implemented we return all the list
                results.values = orignalSearchArrayList;
                results.count = orignalSearchArrayList.size();


            } else {
                // We perform filtering operation
                List<ProductSearchResults> nPlanetList = new ArrayList<ProductSearchResults>();

                for (ProductSearchResults p : searchArrayList) {
                    if (p.getName().toUpperCase().contains(constraint.toString().toUpperCase())) {

                        nPlanetList.add(p);
                        Log.w("filter", p.getName());
                    }

                }

                results.values = nPlanetList;
                results.count = nPlanetList.size();

            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            if (results.count == 0)
                notifyDataSetInvalidated();
            else {
                searchArrayList = (ArrayList<ProductSearchResults>) results.values;
                notifyDataSetChanged();
            }

        }


    }
}
