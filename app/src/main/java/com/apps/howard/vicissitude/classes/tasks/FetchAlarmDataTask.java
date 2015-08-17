//
//  FetchAlarmDataTask.java
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

import android.os.AsyncTask;
import android.widget.ListView;

import com.apps.howard.vicissitude.R;
import com.apps.howard.vicissitude.activities.fragments.SmsFragment;
import com.apps.howard.vicissitude.classes.SmsAlarm;
import com.apps.howard.vicissitude.classes.Utilities;
import com.apps.howard.vicissitude.classes.adapters.SmsAdapter;

import java.io.File;
import java.util.ArrayList;

/*
Async Task for fetching the JSON Data.
*/
public class FetchAlarmDataTask extends AsyncTask<File, Void, ArrayList<SmsAlarm>> {
    /*
    Sms fragment that constains the smslistview
     */
    private final SmsFragment activity;
    /*
    Listview that will be populated by the adapter
     */
    private final ListView smsListView;

    /**
     * Constructs async task for retrieving alarm data
     *
     * @param activity Sms Fragment
     */
    public FetchAlarmDataTask(SmsFragment activity) {
        this.activity = activity;
        this.smsListView = (ListView) activity.getActivity().findViewById(R.id.listview_sms_alarms);
    }

    protected ArrayList<SmsAlarm> doInBackground(File... files) {
        return Utilities.parseAlarmJson(activity.getActivity(), files);
    }

    protected void onPostExecute(ArrayList<SmsAlarm> result) {
        activity.mSmsAdapter =
                new SmsAdapter(activity.getActivity(), result);

        activity.registerForContextMenu(smsListView);
        smsListView.setAdapter(activity.mSmsAdapter);

        activity.mSmsAdapter.notifyDataSetChanged();
    }
}