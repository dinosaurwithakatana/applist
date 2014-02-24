package com.dwak.applist;

import android.graphics.drawable.Drawable;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by vishnu on 2/23/14.
 */
public class AppListItem {
    @Expose(serialize = true) @SerializedName("applicationName")
    private String mApplicationName;
    @Expose(serialize = true) @SerializedName("applicationIcon")
    private int mApplicationIcon;
    @Expose(serialize = true) @SerializedName("applicationPackageName")
    private String mApplicationPackageName;
    @Expose(serialize = false)
    private Drawable mApplicationIconDrawable;

    public AppListItem(String applicationName, int applicationIcon, String applicationPackageName, Drawable applicationIconDrawable) {
        mApplicationName = applicationName;
        mApplicationIcon = applicationIcon;
        mApplicationPackageName = applicationPackageName;
        mApplicationIconDrawable = applicationIconDrawable;
    }

    public String getApplicationName() {
        return mApplicationName;
    }

    public void setApplicationName(String applicationName) {
        mApplicationName = applicationName;
    }

    public int getApplicationIcon() {
        return mApplicationIcon;
    }

    public void setApplicationIcon(int applicationIcon) {
        mApplicationIcon = applicationIcon;
    }

    public String getApplicationPackageName() {
        return mApplicationPackageName;
    }

    public void setApplicationPackageName(String applicationPackageName) {
        mApplicationPackageName = applicationPackageName;
    }

    public Drawable getApplicationIconDrawable() {
        return mApplicationIconDrawable;
    }

    public void setApplicationIconDrawable(Drawable applicationIconDrawable) {
        mApplicationIconDrawable = applicationIconDrawable;
    }
}
