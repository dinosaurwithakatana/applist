package com.dwak.applist;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AppListFragment extends Fragment {
    private static final String TAG = AppListFragment.class.getSimpleName();
    public static final String FIRST_RUN = "first_run";
    private ListView mListView;
    private ArrayList<AppListItem> mApplicationList;
    private AppListAdapter mAdapter;
    private ProgressBar mProgressBar;
    private Gson mGson;
    private final String PLAY_URL = "https://play.google.com/store/apps/details?id=";
    private final int UNINSTALL = 1;

    public AppListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        boolean firstRun = sharedPreferences.getBoolean("first_run", true);

        if(firstRun){
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(R.string.first_run_popup);
            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
            sharedPreferences.edit().putBoolean(FIRST_RUN, false).commit();
        }
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.progress_bar);

        mListView = (ListView) rootView.findViewById(R.id.listview);
        mListView.setVisibility(View.GONE);
        mListView.setFastScrollEnabled(true);
        mListView.setFastScrollAlwaysVisible(true);
        mApplicationList = new ArrayList<AppListItem>();
        mAdapter = new AppListAdapter(getActivity(), android.R.layout.simple_list_item_1, mApplicationList);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String packageName = mApplicationList.get(i).getApplicationPackageName();
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(PLAY_URL+packageName));
                startActivity(intent);
            }
        });

        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int position, long l) {
                Uri packageUri = Uri.parse("package:" + mApplicationList.get(position).getApplicationPackageName());
                Intent uninstallIntent =
                        new Intent(Intent.ACTION_UNINSTALL_PACKAGE, packageUri);
                startActivityForResult(uninstallIntent, UNINSTALL);
                mProgressBar.setVisibility(View.VISIBLE);
                mListView.setVisibility(View.GONE);
                return false;
            }
        });

        final GsonBuilder builder = new GsonBuilder();
        builder.excludeFieldsWithoutExposeAnnotation();
        mGson = builder.create();
        new GetPackageList().execute();
        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        new GetPackageList().execute();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        Intent sendIntent = new Intent();
        String shareData = "";
        switch (id) {
            case R.id.action_export:
                Type myType = new TypeToken<ArrayList<AppListItem>>() {
                }.getType();
                shareData = mGson.toJson(mApplicationList, myType);
                break;
            case R.id.action_export_min:
                for(AppListItem listItem : mApplicationList){
                    shareData += listItem.getApplicationName() + '\n';
                }
                Log.d(TAG, shareData);
                break;
            case R.id.action_export_play_store:
                for(AppListItem listItem : mApplicationList){
                    shareData += "- " + listItem.getApplicationName() + "\n\t- " + PLAY_URL + listItem.getApplicationPackageName() + '\n';
                }
                break;
            case R.id.action_export_markdown:
                for(AppListItem listItem : mApplicationList){
                    shareData += "* [" + listItem.getApplicationName() + "](" + PLAY_URL + listItem.getApplicationPackageName() + ")\n";
                }
                break;
        }
        sendIntent.putExtra(Intent.EXTRA_TEXT, shareData);
        Log.d(TAG, shareData);
        sendIntent.putExtra(Intent.EXTRA_TITLE, "Installed Applications");
        sendIntent.setType("text/plain");
        sendIntent.setAction(Intent.ACTION_SEND);
        startActivity(sendIntent);
        return super.onOptionsItemSelected(item);
    }

    class GetPackageList extends AsyncTask<Void, Void, List<ApplicationInfo>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mApplicationList.clear();
        }

        @Override
        protected List<ApplicationInfo> doInBackground(Void... voids) {
            final List<PackageInfo> packageList = getActivity().getPackageManager().getInstalledPackages(0);
            for (PackageInfo packageInfo : packageList) {
                if (!isSystemPackage(packageInfo)) {
                    mApplicationList.add(new AppListItem((String) packageInfo.applicationInfo.loadLabel(getActivity().getPackageManager()),
                            packageInfo.applicationInfo.icon,
                            packageInfo.packageName,
                            packageInfo.applicationInfo.loadIcon(getActivity().getPackageManager())));
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<ApplicationInfo> applicationInfos) {
            super.onPostExecute(applicationInfos);
            Collections.sort(mApplicationList, new ApplicationComparator());
            mAdapter.notifyDataSetChanged();
            mProgressBar.setVisibility(View.GONE);
            mListView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Return whether the given PackgeInfo represents a system package or not.
     * User-installed packages (Market or otherwise) should not be denoted as
     * system packages.
     *
     * @param pkgInfo
     * @return
     */
    private boolean isSystemPackage(PackageInfo pkgInfo) {
        return ((pkgInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0);
    }
}

