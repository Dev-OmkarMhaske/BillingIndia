package com.tsysinfo.billing;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ExpenseTownRowAdapter  extends ArrayAdapter<ExpenseTown> {
    LayoutInflater flater;

    public ExpenseTownRowAdapter(Activity context, int resouceId, int Textviewid, ArrayList<ExpenseTown> list){

        super(context,resouceId,Textviewid,list);
        flater = context.getLayoutInflater();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView =  flater.inflate(R.layout.expense_row, parent, false);
        }

        ExpenseTown courtName = getItem(position);

        TextView txtTitle = (TextView) convertView.findViewById(R.id.mtextid);

        txtTitle.setText(courtName.getTown());

        return convertView;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView =  flater.inflate(R.layout.expense_row, parent, false);
        }
        ExpenseTown courtName = getItem(position);
        TextView txtTitle = (TextView) convertView.findViewById(R.id.mtextid);

        txtTitle.setText(courtName.getTown());

        if (position == 0) {
            // Set the hint text color gray
            txtTitle.setTextColor(Color.GRAY);

        } else {
            txtTitle.setTextColor(Color.BLACK);

        }

        return convertView;
    }
    @Override
    public int getCount() {

        // TODO Auto-generated method stub
        int count = super.getCount();

        return count>0 ? count : count ;


    }
}
