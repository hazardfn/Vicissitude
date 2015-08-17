//
//  NotificationService.java
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

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import com.apps.howard.vicissitude.R;

import java.util.HashMap;

/**
 * Service responsible for displaying the alerts
 * (Including sounds and vibration)
 */
public class NotificationService extends Service {

    /**
     * Hashmap allows for grouping alerts by sender
     * TODO: Evaluate whether this should actually be grouped by alert name
     */
    private final HashMap<String, String> lastNotificationMessage = new HashMap<>();
    /**
     * Vibration pattern, short controlled bursts
     */
    private final long vibrationPattern[] = {0, 200, 500};
    /**
     * Notification manager which handles notifications
     */
    private NotificationManager notificationManager;
    /**
     * Vibrator which uses a pattern to vibrate the phone
     */
    private Vibrator vibrator;
    /**
     * Preferences required for override volume
     * and tone settings
     */
    private SharedPreferences prefs;

    //region Android Lifecycle
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        notificationManager =
                (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        vibrator =
                (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        notificationManager.cancelAll();
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Attempt to get extras if they exist
        Bundle intentExtras;
        try {
            intentExtras = intent.getExtras();
        } catch (NullPointerException e) {
            intentExtras = null;
        }
        // Attempt to get action if it exists
        String action;
        try {
            action = intent.getAction();
        } catch (NullPointerException e) {
            action = null;
        }

        if (intentExtras != null) {
            // Extract extras
            String message = intentExtras.getString("notification-message");
            String sender = intentExtras.getString("notification-sender");
            String alarmName = intentExtras.getString("notification-alarm-name");

            logMessage(alarmName, message);
            buildNotification(message, sender);
        }

        if (action != null)
            maybeCancelNotification(action);

        return START_NOT_STICKY;
    }
    //endregion

    //region Internal Functions
    private void logMessage(String alarmName, String message) {
        Intent logIntent = new Intent(this, AlertLogService.class);
        logIntent.putExtra("log-service", "Notification Service");
        logIntent.putExtra("log-action", message +
                " triggered alarm:" + alarmName);

        this.startService(logIntent);
    }

    private void maybeCancelNotification(String action) {
        if (!action.equals("")) {
            vibrator.cancel();
            lastNotificationMessage.remove(action);
        }
    }

    private Uri getAlarmTone() {
        return Uri.parse(prefs.getString(
                        getString(R.string.pref_sound_alarm_tone_key),

                        RingtoneManager.getDefaultUri(
                                RingtoneManager.TYPE_ALARM)
                                .toString()
                )
        );
    }

    private String maybeSetNotificationMessage(String sender) {
        String lastMessage = this.lastNotificationMessage.get(sender);

        if (lastMessage != null)
            return lastMessage;

        return "";
    }

    private NotificationCompat.Builder getNotificationBuilder(String message, String sender) {
        return new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_stat_name)
                .setContentTitle(getString(R.string.notification_title_notification_service) + sender)
                .setStyle((new NotificationCompat.BigTextStyle())
                        .bigText(message)
                        .setBigContentTitle(getString(R.string.notification_title_notification_service) + sender)
                        .setSummaryText("Vicissitude"))
                .setGroup(sender)
                .setSound(getAlarmTone(), AudioManager.STREAM_ALARM)
                .setAutoCancel(true);
    }

    private void buildNotification(String message, String sender) {
        String newNotificationMessage = maybeSetNotificationMessage(sender);

        newNotificationMessage +=
                message +
                        System.getProperty("line.separator");

        NotificationCompat.Builder mBuilder =
                getNotificationBuilder(message, sender);


        // Creates an explicit intent for an Activity in your app
        Notification mNotification;

        Intent resultIntent = new Intent(this, NotificationService.class);
        resultIntent.setAction(sender);

        PendingIntent resultPendingIntent = PendingIntent.getService(this, 0, resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.setContentIntent(resultPendingIntent);
        mBuilder.setDeleteIntent(resultPendingIntent);

        mNotification = mBuilder.build();
        mNotification.flags |= Notification.FLAG_INSISTENT;

        this.lastNotificationMessage.put(sender, newNotificationMessage);

        maybeOverrideVolumeBeforeNotify();

        notificationManager.notify(sender, 1, mNotification);
        vibrator.vibrate(vibrationPattern, 0);
    }

    private void maybeOverrideVolumeBeforeNotify() {

        boolean settingsOverrideVolumeEnabled = prefs.getBoolean(
                this.getString(R.string.pref_override_volume_key), false
        );
        int settingsOverrideVolume = prefs.getInt(
                this.getString(R.string.pref_alert_volume_key), 1
        );

        AudioManager audioManager =
                (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        if (settingsOverrideVolumeEnabled) {
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM,
                    settingsOverrideVolume,
                    0);
        }
    }
    //endregion
}
