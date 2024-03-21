package com.tsysinfo.billing.adapter;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Environment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.tsysinfo.billing.BillingActivity;
import com.tsysinfo.billing.OrderActivity;
import com.tsysinfo.billing.ProductSearchResults;
import com.tsysinfo.billing.R;
import com.tsysinfo.billing.ZoomImageViewActivity;
import com.tsysinfo.billing.database.DataBaseAdapter;
import com.tsysinfo.billing.database.Models;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class OrderListAdapter extends RecyclerView.Adapter<OrderListAdapter.RecyclerViewHolder> implements Filterable {
    private final Context context;
    private List<ProductSearchResults> mOriginalValues; // Original Values
    private List<ProductSearchResults> mDisplayedValues;
    double stock = 0.0;
    DataBaseAdapter dba;
    Models mod;

    public OrderListAdapter(Context context, List<ProductSearchResults> data) {
        this.context = context;
        this.mOriginalValues = data;
        this.mDisplayedValues = data;
        dba = new DataBaseAdapter(context);
        mod = new Models();
    }

    @Override
    public RecyclerViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {

        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.product_row, viewGroup, false);
        return new RecyclerViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final RecyclerViewHolder holder, final int i) {

        try {

            ProductSearchResults item = mDisplayedValues.get(i);

            holder.rot.setText(item.getRot() + " (%)");
            holder.prate.setText(item.getPrate());
            try {
                stock = Double.parseDouble(item.getStcok());
            } catch (NumberFormatException e) {
                stock = 0; // your default value
            }

            holder.productStock.setText(new Double(stock).intValue() + "");

            if (stock > 0) {
                holder.txtName.setText(item.getName());
                holder.txtName.setTextColor(Color.BLUE);
                holder.productStock.setTextColor(Color.BLUE);
            } else {
                holder.txtName.setText(item.getName());
                holder.txtName.setTextColor(Color.RED);
                holder.productStock.setTextColor(Color.RED);
            }
            holder.txtDescription.setText("Rs. " + String.valueOf(Double.parseDouble(item.getDp())));
            holder.txtPrice.setText("Rs. " + String.valueOf(Double.parseDouble(item.getPrice())));

            final String productID = item.getID();
            final String productName = item.getName();
            final String productDesc = item.getDescription();
            final String productImage = item.getImage();
            final String productPrice = item.getPrice();
            final String dp = item.getDp();


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

            holder.imgProduct.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String p = Environment.getExternalStorageDirectory() + "/ItemImages/" + item.getImage() + "";
                    Intent intent = new Intent(context, ZoomImageViewActivity.class);
                    intent.putExtra("img", p);
                    context.startActivity(intent);

                }
            });
            try {

                String path = Environment.getExternalStorageDirectory() + "/ItemImages/" + item.getImage() + "";
                File file = new File(path);
                Log.w("PBA", "Image: " + path);
                try {

                    Picasso.get().load(file).fit().placeholder(R.drawable.noimage).into(holder.imgProduct);

                } catch (Exception e) {

                }


            } catch (Exception e) {
                e.printStackTrace();
            }


            if (item.getAct().equalsIgnoreCase("product")) {

                holder.btnAddToCart.setVisibility(View.GONE);
                holder.qt.setVisibility(View.GONE);

            } else if (item.getAct().equalsIgnoreCase("order")) {

                holder.btnAddToCart.setOnClickListener(new View.OnClickListener() {

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
                                cv.put("rot", item.getRot());
                                cv.put("prate", holder.prate.getText().toString());
                                cv.put("qty", holder.quantity.getText().toString());
                                cv.put("dp", dp);
                                cv.put("csz", item.getCsz());
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

            } else if (item.getAct().equalsIgnoreCase("billing")) {
                holder.btnAddToCart.setOnClickListener(new View.OnClickListener() {
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
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private int getIndex(Spinner spinner, String myString) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(myString)) {
                return i;
            }
        }
        return 0;
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {

                mDisplayedValues = (ArrayList<ProductSearchResults>) results.values; // has the filtered values
                notifyDataSetChanged();  // notifies the data with new filtered values
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();        // Holds the results of a filtering operation in values
                ArrayList<ProductSearchResults> FilteredArrList = new ArrayList<ProductSearchResults>();

                if (mOriginalValues == null) {
                    mOriginalValues = new ArrayList<ProductSearchResults>(mDisplayedValues); // saves the original data in mOriginalValues
                }

                if (constraint == null || constraint.length() == 0) {
                    results.count = mOriginalValues.size();
                    results.values = mOriginalValues;
                } else {
                    constraint = constraint.toString().toLowerCase();
                    for (ProductSearchResults p : mOriginalValues) {
                        if (p.getName().toUpperCase().contains(constraint.toString().toUpperCase())) {
                            FilteredArrList.add(p);
                            Log.w("filter", p.getName());
                        }
                    }
                    // set the Filtered result to return
                    results.count = FilteredArrList.size();
                    results.values = FilteredArrList;
                }
                return results;
            }
        };
        return filter;
    }

    @Override
    public int getItemCount() {
        return (null != mDisplayedValues ? mDisplayedValues.size() : 0);
    }

    static class RecyclerViewHolder extends RecyclerView.ViewHolder {
        TextView txtName;
        TextView txtDescription, productStock;
        TextView txtPrice;
        ImageView btnAddToCart, imgProduct;
        EditText quantity;
        LinearLayout qt;
        Spinner spinnerUnit;
        TextView rot, prate;
        EditText txtRemark;

        RecyclerViewHolder(View view) {
            super(view);
            txtName = (TextView) view.findViewById(R.id.productName);
            txtDescription = (TextView) view.findViewById(R.id.productDescription);
            txtPrice = (TextView) view.findViewById(R.id.productPrice);
            imgProduct = (ImageView) view.findViewById(R.id.list_image);
            productStock = (TextView) view.findViewById(R.id.productStock);
            prate = (TextView) view.findViewById(R.id.productPRate);
            btnAddToCart = (ImageView) view.findViewById(R.id.productAddToCart);
            txtRemark = view.findViewById(R.id.productRemark);
            quantity = (EditText) view.findViewById(R.id.Quantity);
            qt = (LinearLayout) view.findViewById(R.id.qt);
            spinnerUnit = (Spinner) view.findViewById(R.id.spinnUnit);
            rot = (TextView) view.findViewById(R.id.productROT);
        }
    }
}
