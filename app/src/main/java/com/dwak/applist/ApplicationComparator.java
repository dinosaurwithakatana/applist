package com.dwak.applist;

import java.util.Comparator;

public class ApplicationComparator implements Comparator<AppListItem> {
    @Override
    public int compare(AppListItem appListItem, AppListItem appListItem2) {
        return appListItem.getApplicationName().compareToIgnoreCase(appListItem2.getApplicationName());
    }
}
