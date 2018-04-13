package com.seismic.tech.locationtracker;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

public class ViewTripsAdapter extends BaseExpandableListAdapter {
    private Context context;
    private HashMap<String,ArrayList<Trip>> map;
    ArrayList<String> keys;
    public ViewTripsAdapter(Context context,HashMap<String,ArrayList<Trip>> map,ArrayList<String> keys)
    {
        this.context = context;
        this.map = map;
        this.keys = keys;
    }
    @Override
    public int getGroupCount() {
        return map.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return map.get(keys.get(groupPosition)).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return map.get(keys.get(groupPosition));
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return map.get(keys.get(groupPosition)).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        String key = keys.get(groupPosition);
        int childrenCount = map.get(key).size();
        if(convertView==null)
        {
            LayoutInflater layoutInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.list_group,null);
        }
        TextView tv = (TextView)convertView.findViewById(R.id.groupTV);
        tv.setText(key+"("+childrenCount+")");
        tv.setTypeface(null,Typeface.BOLD);
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        Trip childrenTrip = (Trip)getChild(groupPosition,childPosition);
        if(convertView==null)
        {
            LayoutInflater layoutInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.list_item,null);
        }
        TextView tv = (TextView)convertView.findViewById(R.id.itemTV);
        tv.setText("Distance: "+childrenTrip.distance+" Time: "+childrenTrip.time);
        tv.setTypeface(Typeface.DEFAULT);
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
