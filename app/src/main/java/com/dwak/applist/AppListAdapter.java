package com.dwak.applist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class AppListAdapter extends ArrayAdapter{
    private final List<AppListItem> mItems;
    private final Context mContext;

    public AppListAdapter(Context context, int resource, List<AppListItem> items) {
        super(context, resource, items);
        mItems = items;
        mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.list_item, parent, false);
        TextView textView = (TextView) rowView.findViewById(R.id.item_name);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.item_icon);
        textView.setText(mItems.get(position).getApplicationName());
        imageView.setBackground(mItems.get(position).getApplicationIconDrawable());
        return rowView;
    }
}
