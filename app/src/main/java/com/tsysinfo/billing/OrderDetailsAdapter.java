package com.tsysinfo.billing;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tsysinfo.billing.database.DataBaseAdapter;

import java.text.DecimalFormat;
import java.util.List;



import java.util.ArrayList;
import java.util.List;
import android.widget.Filter;
import android.widget.Filterable;
    /**
     * Created by tsysinfo on 10/30/2017.
     */
    public class OrderDetailsAdapter extends BaseAdapter {
        private static List<OrdDetailsEntity> searchArrayList;

        private static List<OrdDetailsEntity> orignalSearchArrayList;

        private LayoutInflater mInflater;

        Context context;

        DataBaseAdapter dba;
        public OrderDetailsAdapter(Context context, List<OrdDetailsEntity> results) {
            searchArrayList = results;
            mInflater = LayoutInflater.from(context);
            this.context = context;
            this.orignalSearchArrayList=results;
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
            OrdDetailsEntity orderEntity=searchArrayList.get(position);
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.detail_row, null);
                holder = new ViewHolder();

                holder.prdNo = (TextView) convertView.findViewById(R.id.productid);
                holder.textViewQ = (TextView) convertView.findViewById(R.id.productQ);
                holder.prdName = (TextView) convertView.findViewById(R.id.productName);
                holder.prdQty= (TextView) convertView.findViewById(R.id.productQuantity);
                holder.prdRate = (TextView) convertView.findViewById(R.id.rate);
                holder.prdRot = (TextView) convertView.findViewById(R.id.actualGstValue);
                holder.prdTotalGross = (TextView) convertView.findViewById(R.id.tot);
                holder.rotPercent = (TextView) convertView.findViewById(R.id.productGst);
                holder.prdGross = (TextView) convertView.findViewById(R.id.productGross);
                holder.llMain=(LinearLayout)convertView.findViewById(R.id.main);


                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            if (position % 2 == 1) {
                holder.llMain.setBackgroundColor(context.getResources().getColor(R.color.gridRow));
            } else {
                holder.llMain.setBackgroundColor(context.getResources().getColor(R.color.gridAlterow));
            }


            holder.prdName.setText(orderEntity.getName());
            holder.textViewQ.setText(orderEntity.getUQC());
            holder.prdNo.setText(orderEntity.getNo());
            holder.prdQty.setText(orderEntity.getQuanti());
            holder.prdRate.setText(orderEntity.getRate());
            holder.rotPercent.setText(orderEntity.getRot());
            holder.prdGross.setText(new DecimalFormat("#.##").format(Double.parseDouble(orderEntity.getRate().trim()) * Double.parseDouble(orderEntity.getQuanti().trim())));


            double grossAmt = Double.parseDouble(orderEntity.getRate().trim()) * Double.parseDouble(orderEntity.getQuanti().trim());




            grossAmt = Math.round(grossAmt * 100.0) / 100.0;

            double TaxPer=Double.parseDouble(searchArrayList.get(position).getRot());
            double gst= ((grossAmt*TaxPer)/100);

            holder.prdRot.setText(new DecimalFormat("#.##").format(gst));


            holder.prdTotalGross.setText(new DecimalFormat("#.##").format(Double.parseDouble(orderEntity.getTotal()) + gst));

            return convertView;
        }



        static class ViewHolder {

            TextView prdNo,prdName,prdQty,prdRate,prdGross,prdRot,rotPercent,prdTotalGross;
            Button button;
            LinearLayout llMain;
            TextView textViewQ;
        }






    }


