//
//  AlertLogService.java
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

package com.apps.howard.vicissitude.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;

import com.apps.howard.vicissitude.R;
import com.apps.howard.vicissitude.classes.database.AlertLogContract;
import com.apps.howard.vicissitude.classes.database.AlertLogDbHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * All services forward messages here so they are logged
 * in the activity log database.
 */
public class AlertLogService extends Service {

    /**
     * Access to preferences is required
     * to determine when to clean the DB.
     */
    private SharedPreferences prefs;
    /**
     * Calendar for configuring an alarm
     * to regularly clear out the logs
     */
    private Calendar calendar;

    /**
     * DbHelper exposes some helpful functions
     * for handling the SQLLite DB
     */
    private AlertLogDbHelper mDbHelper;

    //region Android Lifecycle
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        mDbHelper = new AlertLogDbHelper(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!(intent == null)) {

            // Try and get the extras from the intent
            Bundle data;
            try {
                data = intent.getExtras();
            } catch (NullPointerException e) {
                data = null;
            }

            // If extras exist write them to the db
            if (data != null) {

                String service = data.getString("log-service");
                String action = data.getString("log-action");

                write(service, action);
            }

            // Try and get an action from the intent
            String action;
            try {
                action = intent.getAction();
            } catch (NullPointerException e) {
                action = null;
            }
        }

        return START_STICKY;
    }
    //endregion

    private void write(String service, String action) {
        // Get alert retention settings
        int settingsLogRetentionDays = Integer.valueOf(prefs.getString(
                this.getString(R.string.pref_alert_retention_key), "30"
        ));

        Date dateInstance = new Date();

        Calendar cal = Calendar.getInstance();
        cal.setTime(dateInstance);
        cal.add(Calendar.DATE, -(settingsLogRetentionDays));
        Date purgeBefore = cal.getTime();

        purgeOld(purgeBefore);

        // Gets the data repository in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();

        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date();

        values.put(AlertLogContract.AlertLogEntry.COLUMN_NAME_SERVICE, service);
        values.put(AlertLogContract.AlertLogEntry.COLUMN_NAME_ACTION, action);
        values.put(AlertLogContract.AlertLogEntry.COLUMN_NAME_ADDED, dateFormat.format(date));

        db.insert(
                AlertLogContract.AlertLogEntry.TABLE_NAME,
                AlertLogContract.AlertLogEntry.NULL_COLUMN_HACK,
                values);
    }

    private void purgeOld(Date purgeBefore) {
        // Gets the data repository in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        mDbHelper.purgeOld(db, purgeBefore);
    }
    //endregion
}
