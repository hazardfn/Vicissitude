//
//  FloodProtectionService.java
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
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;

import com.apps.howard.vicissitude.R;
import com.apps.howard.vicissitude.classes.TimeSpan;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Notifications come through here first
 * to determine if they should be shown
 * or not.
 */
public class FloodProtectionService extends Service {

    /**
     * Handler contains the method the timer runs
     */
    private final Handler handler = new Handler();
    /**
     * Keeps track of the number of received messages
     * a timer resets this at the period specified
     * by the user
     */
    private int receivedMessages = 0;
    /**
     * Application context for displaying toast
     */
    private Context context;
    /**
     * Timer objects are what keep track
     * of the flood protection period.
     */
    private Timer timer;
    private TimerTask timerTask;
    /**
     * Preferences are required to get the set values
     * in flood protection settings
     */
    private SharedPreferences prefs;

    /**
     * The last set timer value
     */
    private long floodProtectionTimer = 0;

    //region Android Lifecycle
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        context = this;
    }

    @Override
    public void onDestroy() {
        // If the timer has been set at this point
        // destroy it to free resources.
        if (timer != null) {
            timer.cancel();
            timer.purge();
            timerTask.cancel();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        String action;
        try {
            action = intent.getAction();
        } catch (NullPointerException e) {
            action = null;
        }

        if (action != null) {
            if (maybeDisableTimer(action) != -1) {
                return START_NOT_STICKY;
            }
        }

        if (intent != null) {

            receivedMessages += 1; // Increment received message count

            maybeSetTimer();

            // Attempt to get extras if they exist
            Bundle intentExtras;
            try {
                intentExtras = intent.getExtras();
            } catch (NullPointerException e) {
                intentExtras = null;
            }

            if (intentExtras != null) {
                String message = intentExtras.getString("notification-message");
                String sender = intentExtras.getString("notification-sender");
                String alarmName = intentExtras.getString("notification-alarm-name");

                // If canNotify then pass the message onto the notification service
                // Else log it as something blocked by flood protection.
                if (canNotify()) {
                    Intent notificationIntent = new Intent(this, NotificationService.class);
                    notificationIntent.putExtra("notification-sender", sender);
                    notificationIntent.putExtra("notification-message", message);
                    notificationIntent.putExtra("notification-alarm-name", alarmName);
                    this.startService(notificationIntent);
                } else {
                    Intent logIntent = new Intent(this, AlertLogService.class);
                    logIntent.putExtra("log-service", "Flood Protection");
                    logIntent.putExtra("log-action", message +
                            " was discarded due to Flood Protection settings");
                    this.startService(logIntent);
                }
            }
        }

        return START_NOT_STICKY;
    }
    //endregion

    //region Internal Functions
    private int maybeDisableTimer(String action) {
        if (action == "disable-timer") {
            stopSelf();
            return START_NOT_STICKY;
        }

        return -1;
    }

    private void maybeSetTimer() {
        int settingsFloodProtectionTimer = Integer.valueOf(prefs.getString(
                getString(R.string.pref_flood_protection_time_key), "0"
        ));
        boolean settingsFloodProtectionEnabled = prefs.getBoolean(
                context.getString(R.string.pref_flood_protection_key), false
        );

        if (settingsFloodProtectionEnabled) {

            if (timer == null) {
                createNewTimer(settingsFloodProtectionTimer);
            }

            if (this.floodProtectionTimer !=
                    (new TimeSpan(settingsFloodProtectionTimer)
                            .TotalMilliseconds())) {

                timer.cancel();
                timer.purge();

                createNewTimer(settingsFloodProtectionTimer);

            }
        }
    }

    private void createNewTimer(int settingsFloodProtectionTimer) {
        this.floodProtectionTimer = new TimeSpan(
                settingsFloodProtectionTimer
        ).TotalMilliseconds();

        timer = new Timer();
        initializeTimerTask();
        timer.schedule(timerTask, this.floodProtectionTimer, this.floodProtectionTimer);
    }

    private void initializeTimerTask() {
        timerTask = new TimerTask() {
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        receivedMessages = 0;
                    }
                });
            }
        };
    }

    private boolean canNotify() {
        boolean settingsFloodProtectionEnabled = prefs.getBoolean(
                context.getString(R.string.pref_flood_protection_key), false
        );
        int settingsFloodProtectionLimit = Integer.valueOf(prefs.getString(
                context.getString(R.string.pref_flood_protection_message_limit_key), "0"
        ));

        return settingsFloodProtectionEnabled && receivedMessages <= settingsFloodProtectionLimit
                || (!settingsFloodProtectionEnabled);


    }
    //endregion

}
