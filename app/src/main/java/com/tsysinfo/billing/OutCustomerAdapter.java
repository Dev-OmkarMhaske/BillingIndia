package com.tsysinfo.billing;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.tsysinfo.billing.database.DataBaseAdapter;
import java.util.ArrayList;

public class OutCustomerAdapter extends BaseAdapter {

    private static ArrayList<ReceiptSearchResults> searchArrayList;
    private LayoutInflater mInflater;
    DataBaseAdapter dba;
    Context context;

    public OutCustomerAdapter(Context context,
                          ArrayList<ReceiptSearchResults> results) {
        searchArrayList = results;
        mInflater = LayoutInflater.from(context);
        this.context = context;
        dba= new DataBaseAdapter(context);

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
        final ReceiptSearchResults receiptSearchResults=searchArrayList.get(position);
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.outcustmerrow, null);
            holder = new ViewHolder();
            holder.Textcustomername = (TextView) convertView.findViewById(R.id.custName);
            holder.TextcustomerAmount = (TextView) convertView.findViewById(R.id.TotalbalanceAmount);
            holder.ChequeInHand=(TextView) convertView.findViewById(R.id.ChequeInHand);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.Textcustomername.setText(searchArrayList.get(position).getCustName());
        holder.TextcustomerAmount.setText(searchArrayList.get(position).getNetDue());
        holder.ChequeInHand.setText(null);

        final String CustName=searchArrayList.get(position).getCustName();
        final String AmountTotal=searchArrayList.get(position).getNetDue();

        holder.Textcustomername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent= new Intent(context,outActivity.class);
                intent.putExtra("custid",CustName);
                intent.putExtra("Tamount",AmountTotal);

                Log.e("Name"," "+AmountTotal.toString());
                context.startActivity(intent);
            }
        });

        holder.ChequeInHand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String custid=searchArrayList.get(position).getKEY_custNo();
                Intent intent= new Intent(context,ChequeInHandActivity.class);
                intent.putExtra("custid",custid);
                context.startActivity(intent);

            }
        });
        return convertView;
    }
    public int getViewTypeCount() {
        if (getCount() != 0)
            return getCount();
        return 1;
    }

    public int getItemViewType(int position) {
        return position;
    }

    static class ViewHolder {

        TextView Textcustomername,TextcustomerAmount,ChequeInHand;

    }
}
