package com.dwak.applist;

import android.app.Application;

public class AppListApplication extends Application{
    private boolean mShowSystemAppsEnabled;
    private static AppListApplication mApplication = new AppListApplication();

    public static AppListApplication getInstance(){
        return mApplication;
    }

    public boolean isShowSystemAppsEnabled() {
        return mShowSystemAppsEnabled;
    }

    public void setShowSystemAppsEnabled(boolean showSystemAppsEnabled) {
        mShowSystemAppsEnabled = showSystemAppsEnabled;
    }
}
