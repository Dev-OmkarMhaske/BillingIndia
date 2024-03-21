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

public class DashBoardAdapter extends BaseAdapter {

    private static ArrayList<DashbaordBean> dashbaordBeanArrayList;
    private LayoutInflater mInflater;
    DataBaseAdapter dba;
    Context context;

    public DashBoardAdapter(Context context, ArrayList<DashbaordBean> results) {
        dashbaordBeanArrayList = results;
        mInflater = LayoutInflater.from(context);
        this.context = context;
        dba= new DataBaseAdapter(context);

    }

        public int getCount() {
        return dashbaordBeanArrayList.size();
    }

        public Object getItem(int position) {
        return dashbaordBeanArrayList.get(position);
    }

        public long getItemId(int position) {
        return position;
    }

        @SuppressLint("InflateParams")
        public View getView(final int position, View convertView, ViewGroup parent) {
        final DashBoardAdapter.ViewHolder holder;
        final DashbaordBean dashbaordBean=dashbaordBeanArrayList.get(position);
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.dashboardlayoutadapter, null);
            holder = new DashBoardAdapter.ViewHolder();
            holder.Head1 = (TextView) convertView.findViewById(R.id.Head1);
            holder.Particulars = (TextView) convertView.findViewById(R.id.Particulars);
            holder.Amount = (TextView) convertView.findViewById(R.id.Amount);
            holder.llmain = (LinearLayout) convertView.findViewById(R.id.linerlayout);

            convertView.setTag(holder);
        } else {
            holder = (DashBoardAdapter.ViewHolder) convertView.getTag();
        }

        holder.Head1.setText(dashbaordBeanArrayList.get(position).getHead1());
        holder.Particulars.setText(dashbaordBeanArrayList.get(position).getParticulars());
        holder.Amount.setText(dashbaordBeanArrayList.get(position).getAmount());

          if (dashbaordBeanArrayList.get(position).getHead1().equals("")) {
              holder.llmain.setBackgroundColor(context.getResources().getColor(R.color.gridAlterow));
            } else {
              holder.llmain.setBackgroundColor(context.getResources().getColor(R.color.gridRow));
            }


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

            TextView Head1,Particulars,Amount;
            LinearLayout llmain;

        }
}
