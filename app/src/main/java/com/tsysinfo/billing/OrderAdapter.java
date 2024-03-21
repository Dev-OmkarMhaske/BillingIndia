package com.tsysinfo.billing;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tsysinfo.billing.database.DataBaseAdapter;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import android.widget.Filter;
import android.widget.Filterable;
/**
 * Created by tsysinfo on 10/30/2017.
 */
public class OrderAdapter extends BaseAdapter  implements Filterable{
    private static List<OrderEntity> searchArrayList;

    private static List<OrderEntity> orignalSearchArrayList;

    private LayoutInflater mInflater;

    Context context;

    DataBaseAdapter dba;
    private ProductFilter productFilter;

    public OrderAdapter(Context context, List<OrderEntity> results) {
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
        OrderEntity orderEntity=searchArrayList.get(position);
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.order_row, null);
            holder = new ViewHolder();

            holder.txtordNo = (TextView) convertView.findViewById(R.id.orderNo);
            holder.txtCustName = (TextView) convertView.findViewById(R.id.custName);
            holder.txtSalesman= (TextView) convertView.findViewById(R.id.salesman);
            holder.txtDate = (TextView) convertView.findViewById(R.id.ordDate);
            holder.txtTotal = (TextView) convertView.findViewById(R.id.productTotal);
            holder.button=convertView.findViewById(R.id.action);
            holder.llmain=convertView.findViewById(R.id.main);
            holder.txtTranstype=convertView.findViewById(R.id.productTransType);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }


        if (position % 2 == 1) {
            holder.llmain.setBackgroundColor(context.getResources().getColor(R.color.gridRow));
        } else {
            holder.llmain.setBackgroundColor(context.getResources().getColor(R.color.gridAlterow));
        }


        holder.txtordNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent= new Intent(context,OrderDetailsActivity.class);
                intent.putExtra("id",holder.txtordNo.getText().toString());
                context.startActivity(intent);
            }
        });
        holder.txtordNo.setPaintFlags(holder.txtordNo.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        holder.txtTotal.setText("Rs. "+new DecimalFormat("#.##").format(Double.parseDouble(orderEntity.getTotalAmt())));
        holder.txtCustName.setText(orderEntity.getCustomerName());
        holder.txtDate.setText(orderEntity.getOrderDate());
        holder.txtSalesman.setText(orderEntity.getSalesMan());
        holder.txtordNo.setText(orderEntity.getOrderNo());
        holder.txtTranstype.setText(orderEntity.getTransType());
        holder.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent= new Intent(context,OrderDetailsActivity.class);
                intent.putExtra("id",holder.txtordNo.getText().toString());
                context.startActivity(intent);

            }
        });


        if(orderEntity.getTransType().equals("U"))
        {
            holder.txtDate.setTextColor(Color.RED);
            holder.txtCustName.setTextColor(Color.RED);
            holder.txtTotal.setTextColor(Color.RED);
            holder.txtTranstype.setTextColor(Color.RED);
            holder.txtordNo.setTextColor(Color.RED);

        }
        else if(orderEntity.getTransType().equals("R"))
        {
            holder.txtDate.setTextColor(Color.BLUE);
            holder.txtCustName.setTextColor(Color.BLUE);
            holder.txtTotal.setTextColor(Color.BLUE);
            holder.txtTranstype.setTextColor(Color.BLUE);
            holder.txtordNo.setTextColor(Color.BLUE);

        }
        else if(orderEntity.getTransType().equals("S"))
        {
            holder.txtDate.setTextColor(Color.BLACK);
            holder.txtCustName.setTextColor(Color.BLACK);
            holder.txtTotal.setTextColor(Color.BLACK);
            holder.txtTranstype.setTextColor(Color.BLACK);
            holder.txtordNo.setTextColor(Color.BLACK);
        }

        return convertView;
    }



    static class ViewHolder {

        TextView txtordNo,txtDate,txtCustName,txtSalesman,txtTotal,txtTranstype;
        Button button;
        LinearLayout llmain;

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


            }
            else {
                // We perform filtering operation
                List<OrderEntity> nPlanetList = new ArrayList<OrderEntity>();

                for (OrderEntity p : searchArrayList) {
                    if (p.getCustomerName().toUpperCase().startsWith(constraint.toString().toUpperCase())) {

                        nPlanetList.add(p);
                        Log.w("filter",p.getCustomerName());
                    }

                }

                results.values = nPlanetList;
                results.count = nPlanetList.size();

            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint,
                                      FilterResults results) {

            // Now we have to inform the adapter about the new list filtered
            if (results.count == 0)
                notifyDataSetInvalidated();
            else {
                searchArrayList = (ArrayList<OrderEntity>) results.values;
                notifyDataSetChanged();
            }

        }


    }



}
