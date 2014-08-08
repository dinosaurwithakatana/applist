package com.dwak.applist;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;

import java.util.List;

public class MainActivity extends Activity implements AppListFragment.AppListFragmentListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            AppListFragment appListFragment = new AppListFragment();
            getFragmentManager().beginTransaction()
                    .add(R.id.container, appListFragment)
                    .commit();
        }
        AppListApplication.getInstance().setShowSystemAppsEnabled(true);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public void onImport(List<AppListItem> appListItems) {
        AppListFragment importFragment = AppListFragment.newInstance(appListItems);
        getFragmentManager().beginTransaction()
                .replace(R.id.container, importFragment)
                .commit();
    }
}
