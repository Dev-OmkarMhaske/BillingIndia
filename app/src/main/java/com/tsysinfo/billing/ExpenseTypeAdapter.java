package com.tsysinfo.billing;

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

import java.util.ArrayList;
import java.util.List;

public class ExpenseTypeAdapter  extends ArrayAdapter<ExpenseType1> {
    LayoutInflater flater;

    public ExpenseTypeAdapter(Activity context, int resouceId, int Textviewid, ArrayList<ExpenseType1> list){

        super(context,resouceId,Textviewid,list);
        flater = context.getLayoutInflater();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView =  flater.inflate(R.layout.expense, parent, false);
        }

        ExpenseType1 courtName = getItem(position);

        TextView txtTitle = (TextView) convertView.findViewById(R.id.mtextid);

        txtTitle.setText(courtName.getexpensetype());

        return convertView;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView =  flater.inflate(R.layout.expense, parent, false);
        }
        ExpenseType1 courtName = getItem(position);
        TextView txtTitle = (TextView) convertView.findViewById(R.id.mtextid);

        txtTitle.setText(courtName.getexpensetype());

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