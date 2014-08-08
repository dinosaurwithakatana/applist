package com.dwak.applist;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;

public class MainActivity extends Activity {

    private AppListFragment mAppListFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            mAppListFragment = new AppListFragment();
            getFragmentManager().beginTransaction()
                    .add(R.id.container, mAppListFragment)
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
}
