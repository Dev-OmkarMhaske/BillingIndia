package com.tsysinfo.billing;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tsysinfo.billing.database.GeoTagUpdate;

import java.util.ArrayList;
import java.util.List;

public class GeoTagUpdatesAdapter extends BaseAdapter {
    private Context mContext;
    private LayoutInflater inflater;
    private List<GeoTagUpdate> geoTagUpdateList = null;
    private ArrayList<GeoTagUpdate> geoTagUpdateArrayList;

    public GeoTagUpdatesAdapter(Context context, List<GeoTagUpdate> geoTagUpdateArrayList) {
        mContext = context;
        this.geoTagUpdateList = geoTagUpdateArrayList;
        inflater = LayoutInflater.from(mContext);
        this.geoTagUpdateArrayList = new ArrayList<GeoTagUpdate>();
        this.geoTagUpdateArrayList.addAll(geoTagUpdateList);
    }

    public class ViewHolder {
        TextView CName,GPSLatitude,GPSLongitude,UpdatedTStmp;
        LinearLayout linerlayout;
    }

    @Override
    public int getCount() {
        return geoTagUpdateList.size();
    }

    @Override
    public GeoTagUpdate getItem(int position) {
        return geoTagUpdateList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public View getView(final int position, View view, ViewGroup parent) {
        final GeoTagUpdatesAdapter.ViewHolder holder;
        if (view == null) {
            holder = new GeoTagUpdatesAdapter.ViewHolder();
            view = inflater.inflate(R.layout.lay_geotag_updates_adapter, null);
            holder.CName = (TextView) view.findViewById(R.id.CName);
            holder.GPSLatitude = (TextView) view.findViewById(R.id.GPSLatitude);
            holder.GPSLongitude = (TextView) view.findViewById(R.id.GPSLongitude);
            holder.UpdatedTStmp = (TextView) view.findViewById(R.id.UpdatedTStmp);
            holder.linerlayout = (LinearLayout) view.findViewById(R.id.linerlayout);
            view.setTag(holder);
        } else {
            holder = (GeoTagUpdatesAdapter.ViewHolder) view.getTag();
        }
        holder.CName.setText(geoTagUpdateList.get(position).getCName());
        holder.GPSLatitude.setText(geoTagUpdateList.get(position).getGPSLatitude());
        holder.GPSLongitude.setText(geoTagUpdateList.get(position).getGPSLongitude());
        holder.UpdatedTStmp.setText(geoTagUpdateList.get(position).getUpdatedTStmp());

        if (position % 2 == 1) {
            holder.linerlayout.setBackgroundColor(mContext.getResources().getColor(R.color.row));
        } else {
            holder.linerlayout.setBackgroundColor(mContext.getResources().getColor(R.color.white));
        }

        return view;
    }
}
