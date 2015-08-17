//
//  SmsAlarm.java
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

package com.apps.howard.vicissitude.classes;


import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.util.ArrayList;

/*
The data structure of an SmsAlarm which is later
serialized to JSON on the device.
 */
public class SmsAlarm implements Serializable {

    //region Alarm Fields
    public final String name;
    public final String summary;
    public final String body_test;

    public final String location;

    public final ArrayList<String> triggeredSenders;
    public final ArrayList<String> triggeredExpressions;

    public final int fromHour;
    public final int fromMinute;
    public final int toHour;
    public final int toMinute;

    public final boolean activated;
    //endregion

    private transient final Context appContext;

    //region Constructors

    /**
     * Constructs a new alarm.
     *
     * @param name       The alarm name
     * @param appContext Application context
     */
    public SmsAlarm(String name, Context appContext) {
        this.name = name;
        this.summary = "";
        this.body_test = "";

        this.location = "";

        this.appContext = appContext;

        this.triggeredSenders = new ArrayList<>();
        this.triggeredExpressions = new ArrayList<>();

        fromHour = 0;
        fromMinute = 0;
        toHour = 0;
        toMinute = 0;

        this.activated = false;
    }

    /**
     * Used when initially saving an alarm - sets activated to false
     *
     * @param name                 Alarm name
     * @param summary              Alarm summary
     * @param body_test            Test string input to test the alarm
     * @param triggeredSenders     List of senders that trigger an alarm
     * @param triggeredExpressions List of regular expressions that trigger an alarm
     * @param fromHour             From hour of the notification period
     * @param fromMinute           From minute of the notification period
     * @param toHour               To hour of the notification period
     * @param toMinute             To minute of the notification period
     * @param appContext           Application context
     */
    public SmsAlarm(String name, String summary, String body_test, ArrayList<String> triggeredSenders
            , ArrayList<String> triggeredExpressions, int fromHour, int fromMinute, int toHour
            , int toMinute, Context appContext) {
        this.name = name;
        this.summary = summary;
        this.body_test = body_test;
        this.appContext = appContext;

        String location = "";

        try {
            location = File.createTempFile("vic", "tude.json", appContext.getFilesDir()).getAbsolutePath();
        } catch (IOException e) {
            Log.e("SmsAlarm()", e.getMessage());
            Utilities.makeToast(this.appContext, e.getMessage());
        }

        this.location = location;

        this.triggeredSenders = triggeredSenders;
        this.triggeredExpressions = triggeredExpressions;

        this.fromHour = fromHour;
        this.toHour = toHour;
        this.fromMinute = fromMinute;
        this.toMinute = toMinute;

        this.activated = false;
    }

    /**
     * Used to set the activated status of an alarm
     *
     * @param name                 Alarm name
     * @param summary              Alarm summary
     * @param body_test            Test string input to test the alarm
     * @param triggeredSenders     List of senders that trigger an alarm
     * @param triggeredExpressions List of regular expressions that trigger an alarm
     * @param fromHour             From hour of the notification period
     * @param fromMinute           From minute of the notification period
     * @param toHour               To hour of the notification period
     * @param toMinute             To minute of the notification period
     * @param activated            True or false depending on if you want to activate or de-activate the alarm
     * @param appContext           Application context
     */
    public SmsAlarm(String name, String summary, String body_test, ArrayList<String> triggeredSenders
            , ArrayList<String> triggeredExpressions, int fromHour, int fromMinute, int toHour
            , int toMinute, String location, boolean activated, Context appContext) {
        this.name = name;
        this.summary = summary;
        this.body_test = body_test;
        this.appContext = appContext;

        this.location = location;

        this.triggeredSenders = triggeredSenders;
        this.triggeredExpressions = triggeredExpressions;

        this.fromHour = fromHour;
        this.toHour = toHour;
        this.fromMinute = fromMinute;
        this.toMinute = toMinute;

        this.activated = activated;
    }

    /**
     * Read in an alarm from a JSON file
     *
     * @param json       JSON File
     * @param appContext Application context
     * @throws IOException
     */
    public SmsAlarm(File json, Context appContext) throws IOException {

        this.appContext = appContext;

        Reader fileRead = null;

        try {
            fileRead = new FileReader(json.getAbsolutePath());
        } catch (FileNotFoundException e) {
            Log.e("SmsAlarm(File)", e.getMessage());
            Utilities.makeToast(this.appContext, e.getMessage());
        }

        assert fileRead != null;

        BufferedReader br = new BufferedReader(fileRead);
        String jsonString = "";
        String s;

        try {
            while ((s = br.readLine()) != null) {
                jsonString += s;
            }
        } catch (IOException e) {
            Log.e("SmsAlarm(File)", e.getMessage());
            Utilities.makeToast(this.appContext, e.getMessage());
        } finally {
            fileRead.close();
        }

        SmsAlarm readAlarm = new Gson().fromJson(jsonString, SmsAlarm.class);

        this.name = readAlarm.name;
        this.summary = readAlarm.summary;
        this.location = json.getAbsolutePath();
        this.body_test = readAlarm.body_test;
        this.triggeredSenders = readAlarm.triggeredSenders;
        this.triggeredExpressions = readAlarm.triggeredExpressions;
        this.fromHour = readAlarm.fromHour;
        this.fromMinute = readAlarm.fromMinute;
        this.toHour = readAlarm.toHour;
        this.toMinute = readAlarm.toMinute;
        this.activated = readAlarm.activated;

    }
    //endregion

    /**
     * Save an alarm
     *
     * @param alarm   Alarm to save
     * @param context Application context
     */
    public static void Save(SmsAlarm alarm, Context context) {
        String json = new Gson().toJson(alarm);

        try {
            FileWriter writer = new FileWriter(alarm.location, false);
            writer.write(json);
            writer.close();

        } catch (IOException e) {
            Log.e("saveAlarm()", e.getMessage());
            Utilities.makeToast(context, e.getMessage());
        }

    }
}
