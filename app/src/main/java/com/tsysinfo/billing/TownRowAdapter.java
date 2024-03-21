package com.tsysinfo.billing;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class TownRowAdapter extends BaseAdapter {

    private List<Town> objects = new ArrayList<Town>();

    private Context context;
    private LayoutInflater layoutInflater;

    public TownRowAdapter(Context context, ArrayList<Town> objects) {
        this.objects=objects;
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return objects.size();
    }

    @Override
    public Town getItem(int position) {
        return objects.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.town_row, null);
            convertView.setTag(new ViewHolder(convertView));
        }
        initializeViews((Town)getItem(position), (ViewHolder) convertView.getTag());
        return convertView;
    }

    private void initializeViews(Town object, ViewHolder holder) {


        holder.town.setText(object.getTown());
        holder.id.setText(object.getId());
        //TODO implement
    }

    protected class ViewHolder {
        private TextView id;
        private TextView town;

        public ViewHolder(View view) {
            id = (TextView) view.findViewById(R.id.id);
            town = (TextView) view.findViewById(R.id.town);
        }
    }
}