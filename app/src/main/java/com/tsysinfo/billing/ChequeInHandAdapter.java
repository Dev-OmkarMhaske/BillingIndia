package com.tsysinfo.billing;
import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tsysinfo.billing.database.DataBaseAdapter;

import java.util.ArrayList;

public class ChequeInHandAdapter extends BaseAdapter {
    private static ArrayList<ChequeIn> chequeInArrayList;
    private LayoutInflater mInflater;
    DataBaseAdapter dba;
    Context context;

    public ChequeInHandAdapter(Context context, ArrayList<ChequeIn> results) {
        chequeInArrayList = results;
        mInflater = LayoutInflater.from(context);
        this.context = context;
        dba= new DataBaseAdapter(context);

    }

    public int getCount() {
        return chequeInArrayList.size();
    }

    public Object getItem(int position) {
        return chequeInArrayList.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("InflateParams")
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ChequeInHandAdapter.ViewHolder holder;
        final ChequeIn chequeIn=chequeInArrayList.get(position);
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.chequeinadapter, null);
            holder = new ChequeInHandAdapter.ViewHolder();
            holder.DateofReceif = (TextView) convertView.findViewById(R.id.DateOfReceipt);
            holder.Chequeno = (TextView) convertView.findViewById(R.id.ChequeNo);
            holder.ChequeAmount = (TextView) convertView.findViewById(R.id.ChequeAmount);
            holder.chequedate = (TextView) convertView.findViewById(R.id.ChequeDate);

            holder.billNo = (TextView) convertView.findViewById(R.id.Billno);
            holder.BillDate = (TextView) convertView.findViewById(R.id.Billdate);

            holder.BillAmount = (TextView) convertView.findViewById(R.id.BillAmount);
            holder.AllocatedAmount = (TextView) convertView.findViewById(R.id.AllocatedAmount);
            holder.Rdays = (TextView) convertView.findViewById(R.id.Rdays);


            convertView.setTag(holder);
        } else {
            holder = (ChequeInHandAdapter.ViewHolder) convertView.getTag();
        }

        holder.DateofReceif.setText(chequeInArrayList.get(position).getDateOfReceipt());
        holder.Chequeno.setText(chequeInArrayList.get(position).getChequeNo());
        holder.ChequeAmount.setText(chequeInArrayList.get(position).getChequeAmount());

        holder.chequedate.setText(chequeInArrayList.get(position).getChequeDate());
        holder.billNo.setText(chequeInArrayList.get(position).getBillno());
        holder.BillDate.setText(chequeInArrayList.get(position).getBillDate());

        holder.BillAmount.setText(chequeInArrayList.get(position).getBillAmount());
        holder.AllocatedAmount.setText(chequeInArrayList.get(position).getAllocatedAmount());
        holder.Rdays.setText(chequeInArrayList.get(position).getRdays());

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

        TextView DateofReceif,Chequeno,chequedate,ChequeAmount,billNo,BillDate,BillAmount,AllocatedAmount,Rdays;;

    }
}
