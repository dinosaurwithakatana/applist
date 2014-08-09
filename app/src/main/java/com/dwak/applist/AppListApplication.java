package com.dwak.applist;

import android.app.Application;
import android.content.pm.PackageInfo;

import java.util.HashMap;
import java.util.Map;

public class AppListApplication extends Application{
    private boolean mShowSystemAppsEnabled;
    private static AppListApplication mApplication;
    private Map<String, PackageInfo> mInstalledPackages;

    public AppListApplication() {
        mInstalledPackages = new HashMap<String, PackageInfo>();
    }

    public static AppListApplication getInstance(){
        if(mApplication==null){
            mApplication = new AppListApplication();
        }
        return mApplication;
    }

    public boolean isShowSystemAppsEnabled() {
        return mShowSystemAppsEnabled;
    }

    public void setShowSystemAppsEnabled(boolean showSystemAppsEnabled) {
        mShowSystemAppsEnabled = showSystemAppsEnabled;
    }

    public Map<String, PackageInfo> getInstalledPackages() {
        return mInstalledPackages;
    }

    public void addInstalledPackage(String packageName, PackageInfo packageInfo){
        mInstalledPackages.put(packageName, packageInfo);
    }
}
