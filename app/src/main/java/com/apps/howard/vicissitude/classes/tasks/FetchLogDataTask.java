//
//  FetchLogDataTask.java
//
//  Author:
//       Howard Beard-Marlowe <howardbm@live.se>
//
//  Copyright (c) 2015 Howard Beard-Marlowe
//
//  This program is free software: you can redistribute it and/or modify
//  it under the terms of the GNU General Public License as published by
//  the Free Software Foundation, either version 3 of the License, or
//  (at your option) any later version.
//
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU General Public License for more details.
//
//  You should have received a copy of the GNU General Public License
//  along with this program.  If not, see <http://www.gnu.org/licenses/>.

package com.apps.howard.vicissitude.classes.tasks;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.v4.widget.SimpleCursorAdapter;
import android.widget.ListView;

import com.apps.howard.vicissitude.R;
import com.apps.howard.vicissitude.activities.fragments.LogFragment;
import com.apps.howard.vicissitude.classes.database.AlertLogContract;
import com.apps.howard.vicissitude.classes.database.AlertLogDbHelper;


public class FetchLogDataTask extends AsyncTask<Void, Void, Cursor> {
    /*
    Activity that contains the listview_log
     */
    private final LogFragment activity;
    /*
    List view that will be binded to the adapter
     */
    private final ListView smsLogView;

    /**
     * Constructs async task for retrieving log data
     *
     * @param activity Log Fragment
     */
    public FetchLogDataTask(LogFragment activity) {
        this.activity = activity;
        this.smsLogView = (ListView) activity.getActivity().findViewById(R.id.listview_log);
    }

    protected Cursor doInBackground(Void... params) {
        AlertLogDbHelper dbHelper = new AlertLogDbHelper(activity.getActivity());
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                AlertLogContract.AlertLogEntry.COLUMN_NAME_SERVICE,
                AlertLogContract.AlertLogEntry.COLUMN_NAME_ACTION,
                AlertLogContract.AlertLogEntry.COLUMN_NAME_ADDED,
                "_id"
        };

        // How you want the results sorted in the resulting Cursor
        String sortOrder =
                AlertLogContract.AlertLogEntry.COLUMN_NAME_ADDED + " DESC";

        return db.query(
                AlertLogContract.AlertLogEntry.TABLE_NAME,  // The table to query
                projection,                               // The columns to return
                null,
                null,
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );
    }

    protected void onPostExecute(Cursor result) {

        String[] Columns = new String[]{
                AlertLogContract.AlertLogEntry.COLUMN_NAME_SERVICE,
                AlertLogContract.AlertLogEntry.COLUMN_NAME_ACTION,
                AlertLogContract.AlertLogEntry.COLUMN_NAME_ADDED
        };

        int[] To = new int[]{
                R.id.serviceTitle,
                R.id.logAction,
                R.id.logDate
        };

        activity.mLogAdapter =
                new SimpleCursorAdapter(activity.getActivity(), R.layout.list_item_alert_log, result, Columns, To, 0);

        smsLogView.setAdapter(activity.mLogAdapter);

        activity.mLogAdapter.notifyDataSetChanged();
    }
}
