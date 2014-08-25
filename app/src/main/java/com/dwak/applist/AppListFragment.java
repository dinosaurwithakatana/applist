package com.dwak.applist;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
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
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.nononsenseapps.filepicker.FilePickerActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AppListFragment extends Fragment {
    public static final String FIRST_RUN = "first_run";
    public static final int FILE_EXPORT_CODE = 12321;
    public static final int FILE_IMPORT_CODE = 12322;
    private static final String TAG = AppListFragment.class.getSimpleName();
    private final String PLAY_URL = "https://play.google.com/store/apps/details?id=";
    private final int UNINSTALL = 1;
    private ListView mListView;
    private List<AppListItem> mApplicationList;
    private AppListAdapter mAdapter;
    private AppListCheckAdapter mCheckedAdapter;
    private ProgressBar mProgressBar;
    private Gson mGson;
    private boolean mIsCheckMode = false;
    private String mShareData;
    private AppListFragmentListener mListener;
    private boolean mImport;

    public AppListFragment() {
    }

    public static AppListFragment newInstance(List<AppListItem> appListItems, boolean isImport) {
        AppListFragment appListFragment = new AppListFragment();
        appListFragment.mApplicationList = appListItems;
        appListFragment.mImport = isImport;
        return appListFragment;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == MainActivity.RESULT_OK) {
            switch (requestCode) {
                case UNINSTALL:
                    new GetPackageList().execute();
                    break;
                case FILE_EXPORT_CODE:
                    Uri exportUri = handleFileOperation(data);
                    File jsonFile = new File(exportUri.getPath(), "applist.json");
                    FileOutputStream outputStream;
                    try {
                        outputStream = new FileOutputStream(jsonFile);
                        outputStream.write(mShareData.getBytes());
                        outputStream.close();
                        Toast.makeText(getActivity(), "File exported!", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case FILE_IMPORT_CODE:
                    ProgressDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    final AlertDialog alertDialog = builder.setMessage("Loading items").create();
                    Uri importUri = handleFileOperation(data);
                    Log.d(TAG, importUri.getPath());
                    Type listType = new TypeToken<List<AppListItem>>() {
                    }.getType();
                    List<AppListItem> appListItems = gsonFromJsonFile(importUri.getPath(), List.class, listType);
                    Log.d(TAG, appListItems.toString());
                    alertDialog.dismiss();
                    mListener.onImport(appListItems);
                    break;
            }
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (AppListFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement AppListFragmentListener");
        }
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

        if (firstRun) {
            new AlertDialog.Builder(getActivity())
                    .setMessage(R.string.first_run_popup)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    })
                    .create()
                    .show();
            sharedPreferences.edit().putBoolean(FIRST_RUN, false).commit();
        }
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.progress_bar);

        mListView = (ListView) rootView.findViewById(R.id.listview);
        mListView.setVisibility(View.GONE);
        mListView.setFastScrollEnabled(true);
        if (mApplicationList == null) {
            mApplicationList = new ArrayList<AppListItem>();
            new GetPackageList().execute();
        }
        else {
            getActivity().setTitle("Import");
            mProgressBar.setVisibility(View.GONE);
            mListView.setVisibility(View.VISIBLE);
        }
        mAdapter = new AppListAdapter(getActivity(), android.R.layout.simple_list_item_1, mApplicationList);
        mCheckedAdapter = new AppListCheckAdapter(getActivity(), android.R.layout.simple_list_item_multiple_choice, mApplicationList);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String packageName = mApplicationList.get(i).getApplicationPackageName();
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(PLAY_URL + packageName));
                startActivity(intent);
            }
        });

        if(!mImport) {
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
        }

        final GsonBuilder builder = new GsonBuilder();
        builder.excludeFieldsWithoutExposeAnnotation();
        mGson = builder.create();
        return rootView;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        Intent sendIntent = new Intent();
        mShareData = "";
        boolean isShare = false;

        Type myType = new TypeToken<ArrayList<AppListItem>>() {
        }.getType();

        switch (id) {
            case R.id.action_import_json:
                isShare = false;
                Intent importIntent = new Intent(getActivity(), FilePickerActivity.class);
                importIntent.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false);
                importIntent.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, false);
                importIntent.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_FILE);
                startActivityForResult(importIntent, FILE_IMPORT_CODE);
                break;
            case R.id.action_export_share:
                mShareData = mGson.toJson(mApplicationList, myType);
                isShare = true;
                break;
            case R.id.action_export_file:
                mShareData = mGson.toJson(mApplicationList, myType);
                isShare = false;
                // This always works
                Intent exportIntent = new Intent(getActivity(), FilePickerActivity.class);
                // This works if you defined the intent filter
                // Set these depending on your use case. These are the defaults.
                exportIntent.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false);
                exportIntent.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, true);
                exportIntent.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_DIR);

                startActivityForResult(exportIntent, FILE_EXPORT_CODE);
                break;
            case R.id.action_export_min:
                for (AppListItem listItem : mApplicationList) {
                    mShareData += listItem.getApplicationName() + '\n';
                }
                isShare = true;
                break;
            case R.id.action_export_play_store:
                for (AppListItem listItem : mApplicationList) {
                    mShareData += "- " + listItem.getApplicationName() + "\n\t- " + PLAY_URL + listItem.getApplicationPackageName() + '\n';
                }
                isShare = true;
                break;
            case R.id.action_export_markdown:
                for (AppListItem listItem : mApplicationList) {
                    mShareData += "* [" + listItem.getApplicationName() + "](" + PLAY_URL + listItem.getApplicationPackageName() + ")\n";
                }
                isShare = true;
                break;
        }
        if (isShare) {
            sendIntent.putExtra(Intent.EXTRA_TEXT, mShareData);
            sendIntent.putExtra(Intent.EXTRA_TITLE, "Installed Applications");
            sendIntent.setType("text/plain");
            sendIntent.setAction(Intent.ACTION_SEND);
            startActivity(sendIntent);
        }
        return super.onOptionsItemSelected(item);
    }

    private Uri handleFileOperation(Intent data) {
        Uri uri = null;
        if (data.getBooleanExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false)) {
            // For JellyBean and above
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                ClipData clip = data.getClipData();

                if (clip != null) {
                    for (int i = 0; i < clip.getItemCount(); i++) {
                        uri = clip.getItemAt(i).getUri();
                        // Do something with the URI
                    }
                }
                // For Ice Cream Sandwich
            }
            else {
                ArrayList<String> paths = data.getStringArrayListExtra
                        (FilePickerActivity.EXTRA_PATHS);

                if (paths != null) {
                    for (String path : paths) {
                        uri = Uri.parse(path);
                        // Do something with the URI
                    }
                }
            }

        }
        else {
            uri = data.getData();
        }

        if (uri != null) {
            return uri;
        }
        else {
            return null;
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

    private <T> T gsonFromJsonFile(final String filePath, Class<T> clazz, Type type) {
        InputStream itemJson = null;
        try {
            itemJson = new FileInputStream(filePath);
        } catch (IOException e) {
            // TODO - assert if file does not exist (Log fatal)
            e.printStackTrace();
        }
        Reader reader = new InputStreamReader(itemJson);

        if (type != null) {
            return mGson.fromJson(reader, type);
        }
        else {
            return mGson.fromJson(reader, clazz);
        }
    }

    public interface AppListFragmentListener {
        void onImport(List<AppListItem> appListItems);
    }

    class GetPackageList extends AsyncTask<Void, Void, List<ApplicationInfo>> {

        @Override
        protected List<ApplicationInfo> doInBackground(Void... voids) {
            final List<PackageInfo> packageList = getActivity().getPackageManager().getInstalledPackages(0);
            for (PackageInfo packageInfo : packageList) {
                if (!isSystemPackage(packageInfo)) {
                    mApplicationList.add(
                            new AppListItem(
                                    (String) packageInfo.applicationInfo.loadLabel(getActivity().getPackageManager()),
                                    packageInfo.applicationInfo.icon,
                                    packageInfo.packageName,
                                    packageInfo.applicationInfo.loadIcon(getActivity().getPackageManager())
                            )
                    );
                }
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mApplicationList.clear();
        }

        @Override
        protected void onPostExecute(List<ApplicationInfo> applicationInfos) {
            super.onPostExecute(applicationInfos);
            Collections.sort(mApplicationList, new ApplicationComparator());
            mAdapter.notifyDataSetChanged();
            mProgressBar.setVisibility(View.GONE);
            mListView.setVisibility(View.VISIBLE);
            getActivity().setTitle(mApplicationList.size() + " apps");
        }
    }
}

