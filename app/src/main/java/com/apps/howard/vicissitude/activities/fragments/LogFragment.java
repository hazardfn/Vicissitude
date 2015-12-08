package com.apps.howard.vicissitude.activities.fragments;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.apps.howard.vicissitude.R;
import com.apps.howard.vicissitude.classes.database.AlertLogDbHelper;
import com.apps.howard.vicissitude.classes.tasks.FetchLogDataTask;

/**
 * A placeholder fragment containing a simple view.
 */
public class LogFragment extends Fragment {

    public SimpleCursorAdapter mLogAdapter;

    //region Android Lifecyle
    /*
    Android lifecycle.
     */
    @Override
    public void onStart() {
        super.onStart();
        loadLogData();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadLogData();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_alert_log, container, false);

        ListView l = (ListView) rootView.findViewById(R.id.listview_log);

        l.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });

        setHasOptionsMenu(true);

        return rootView;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id==R.id.action_clear_logs)
        {
            AlertLogDbHelper dbHelper = new AlertLogDbHelper(this.getActivity());
            SQLiteDatabase db = dbHelper.getReadableDatabase();

            dbHelper.onUpgrade(db,0,0);
            loadLogData();

            return super.onOptionsItemSelected(item);
        }

        getActivity().onBackPressed();

        return super.onOptionsItemSelected(item);
    }

    //endregion

    //region Internal Methods
    private void loadLogData() {
        new FetchLogDataTask(this).execute();
    }
    //endregion
}
