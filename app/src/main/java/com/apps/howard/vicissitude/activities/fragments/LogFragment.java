package com.apps.howard.vicissitude.activities.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.apps.howard.vicissitude.R;
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

        return rootView;
    }
    //endregion

    //region Internal Methods
    private void loadLogData() {
        new FetchLogDataTask(this).execute();
    }
    //endregion
}
