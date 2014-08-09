package com.dwak.applist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class AppListAdapter extends ArrayAdapter<AppListItem>{
    private final List<AppListItem> mItems;
    private final Context mContext;

    public AppListAdapter(Context context, int resource, List<AppListItem> items) {
        super(context, resource, items);
        mItems = items;
        mContext = context;
    }
    private boolean isPackageInstalled(String packageName, Context context) {
        return AppListApplication.getInstance().getInstalledPackages().containsKey(packageName);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewHolder viewHolder;
        if(convertView == null){
            convertView = inflater.inflate(R.layout.list_item, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.mTextView.setText(getItem(position).getApplicationName());
        viewHolder.mImageView.setBackground(getItem(position).getApplicationIconDrawable());

        if(!isPackageInstalled(getItem(position).getApplicationPackageName(), mContext)){
            viewHolder.mRootView.setBackgroundColor(mContext.getResources().getColor(android.R.color.holo_red_light));
        }
        else {
            viewHolder.mRootView.setBackgroundColor(mContext.getResources().getColor(android.R.color.transparent));
        }
        return convertView;
    }

    class ViewHolder{
        View mRootView;
        TextView mTextView;
        ImageView mImageView;

        ViewHolder(View view) {
            mRootView = view;
            mTextView = (TextView) view.findViewById(R.id.item_name);
            mImageView = (ImageView) view.findViewById(R.id.item_icon);
        }
    }
}
