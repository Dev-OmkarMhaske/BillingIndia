package com.tsysinfo.billing;

import android.app.Activity;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class ExpenseSubGroupAdpter  extends ArrayAdapter<ExpenseSupGroup1> {
    LayoutInflater flater;

    public ExpenseSubGroupAdpter(Activity context, int resouceId, int Textviewid, ArrayList<ExpenseSupGroup1> list) {
        super(context, resouceId, Textviewid, list);
        flater = context.getLayoutInflater();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = flater.inflate(R.layout.expense_subgroup, parent, false);
        }
        ExpenseSupGroup1 courtName = getItem(position);
        TextView txtTitle = (TextView) convertView.findViewById(R.id.mtextid);
        txtTitle.setText(courtName.getexpensesubgroup());
        return convertView;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = flater.inflate(R.layout.expense_subgroup, parent, false);
        }
        ExpenseSupGroup1 courtName = getItem(position);
        TextView txtTitle = (TextView) convertView.findViewById(R.id.mtextid);
        txtTitle.setText(courtName.getexpensesubgroup());

        if (position == 0) {
            // Set the hint text color gray
            txtTitle.setTextColor(Color.GRAY);
        } else {
            txtTitle.setTextColor(Color.BLACK);

        }
        return convertView;
    }
//
    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        int count = super.getCount();
        return count > 0 ? count  : count;
    }
}