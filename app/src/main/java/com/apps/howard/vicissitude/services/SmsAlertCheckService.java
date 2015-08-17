//
//  SmsAlertCheckService.java
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

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.apps.howard.vicissitude.classes.SmsAlarm;
import com.apps.howard.vicissitude.classes.Utilities;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class SmsAlertCheckService extends Service {

    //region Android Lifecycle
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            Bundle data = intent.getExtras();

            if (data != null) {
                String messageBody = data.getString("service-body");
                String sender = data.getString("service-sender");

                SmsAlarm match = findMatch(messageBody, sender);

                if (match != null && canNotify(match))
                    triggerAlarm(messageBody, sender, match.name);
            }
        }

        stopSelf();
        return START_NOT_STICKY;
    }
    //endregion

    //region Internal Functions
    private SmsAlarm findMatch(String message, String sender) {
        File[] files = Utilities.getAllJsonFiles(this);
        ArrayList<SmsAlarm> smsAlarms = Utilities.parseAlarmJson(this, files);

        for (SmsAlarm alarm : smsAlarms) {
            if (alarm.activated) {
                if (alarm.triggeredSenders.contains(sender)) {
                    for (String expression : alarm.triggeredExpressions) {
                        if (message.matches(expression)) {
                            return alarm;
                        }
                    }
                }
            }
        }

        return null;
    }

    private void triggerAlarm(String message, String sender, String alarmName) {

        /**
         * All alarms pass through the flood protection service,
         * if flood protection is disabled the message just
         * passes right through to the notification service.
         */
        Intent floodProtectionIntent = new Intent(this, FloodProtectionService.class);
        floodProtectionIntent.putExtra("notification-sender", sender);
        floodProtectionIntent.putExtra("notification-message", message);
        floodProtectionIntent.putExtra("notification-alarm-name", alarmName);
        this.startService(floodProtectionIntent);

    }

    private boolean canNotify(SmsAlarm alarm) {
        Date fromTime;
        Date toTime;
        Calendar Now = Calendar.getInstance(TimeZone.getDefault());
        Date NowCompare;

        int hour = Now.get(Calendar.HOUR_OF_DAY);
        int minute = Now.get(Calendar.MINUTE);

        try {
            fromTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).parse(
                    String.valueOf(alarm.fromHour) + ":" + String.valueOf(alarm.fromMinute)
            );
            toTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).parse(
                    String.valueOf(alarm.toHour) + ":" + String.valueOf(alarm.toMinute)
            );
            NowCompare = new SimpleDateFormat("HH:mm", Locale.getDefault()).parse(
                    String.valueOf(hour) + ":" + String.valueOf(minute)
            );
        } catch (ParseException e) {
            Log.e("invalidTime()", e.getMessage());
            Utilities.makeToast(this, e.getMessage());

            /**
             * In the case of a "rare" invalid time
             * scenario it is better to notify
             * than not.
             */
            return true;
        }

        return (fromTime.before(NowCompare) && toTime.after(NowCompare));
    }
    //endregion
}
