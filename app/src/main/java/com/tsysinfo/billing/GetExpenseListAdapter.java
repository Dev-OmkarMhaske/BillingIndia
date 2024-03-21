package com.tsysinfo.billing;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


import com.tsysinfo.billing.database.DataBaseAdapter;
import com.tsysinfo.billing.database.Models;

import java.util.ArrayList;

public class GetExpenseListAdapter extends BaseAdapter {

    private static ArrayList<GetExpense> searchArrayList;
    private LayoutInflater mInflater;
    Context context;
    DataBaseAdapter dba;
    Models mod;


    int count=0;

    public GetExpenseListAdapter(Context context, ArrayList<GetExpense> results) {
        searchArrayList = results;
        mInflater = LayoutInflater.from(context);
        this.context = context;
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
    public View getView(final int position, View convertView, ViewGroup parent) {
        final GetExpenseListAdapter.ViewHolder holder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.getexpense_row, null);
            holder = new GetExpenseListAdapter.ViewHolder();
            holder.tv_date = (TextView) convertView.findViewById(R.id.tv_date);
            holder.tv_MainCategory = (TextView) convertView.findViewById(R.id.tv_MainCategory);
            holder.tv_SubCategory = (TextView) convertView.findViewById(R.id.tv_SubCategory);
            holder.tv_Remarks = (TextView) convertView.findViewById(R.id.tv_Remarks);
            holder.tv_Amount = (TextView) convertView.findViewById(R.id.tv_Amount);


            //holder.quantityViewDefault = (QuantityView) convertView.findViewById(R.id.quantityView_default);
            convertView.setTag(holder);
        } else {
            holder = (GetExpenseListAdapter.ViewHolder) convertView.getTag();
        }

        holder.tv_date.setText(searchArrayList.get(position).getDate());
        holder.tv_MainCategory.setText(searchArrayList.get(position).getMainCategory());
        holder.tv_SubCategory.setText(searchArrayList.get(position).getSubCategory());
        holder.tv_Remarks.setText(searchArrayList.get(position).getRemarks());
        holder.tv_Amount.setText(searchArrayList.get(position).getAmount());


        final String Date = searchArrayList.get(position).getDate();
        final String MainCategory = searchArrayList.get(position).getMainCategory();
        final String SubCategory = searchArrayList.get(position).getSubCategory();
        final String Remarks = searchArrayList.get(position).getRemarks();
        final String Amount = searchArrayList.get(position).getAmount();


//        Log.w("PBA", "Image: " + productImage);



        return convertView;
    }

    static class ViewHolder {
        TextView tv_date,tv_MainCategory;
        TextView tv_SubCategory,tv_Remarks,tv_Amount;

        //QuantityView quantityViewDefault;
    }

}
