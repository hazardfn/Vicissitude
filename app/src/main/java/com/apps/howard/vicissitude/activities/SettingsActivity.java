package com.apps.howard.vicissitude.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.preference.SwitchPreference;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.apps.howard.vicissitude.R;
import com.apps.howard.vicissitude.classes.Utilities;
import com.apps.howard.vicissitude.controls.SeekBarPreference;
import com.apps.howard.vicissitude.services.AlertLogService;
import com.apps.howard.vicissitude.services.FloodProtectionService;

/**
 * A {@link PreferenceActivity} that presents a set of application settings.
 * <p/>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends PreferenceActivity
        implements Preference.OnPreferenceChangeListener {

    //region Preference Controls

    //region Flood Protection Controls
    private EditTextPreference floodLimit;
    private EditTextPreference floodTime;
    private EditTextPreference humanReadable;
    private SwitchPreference floodProtection;
    //endregion

    //region Sound Controls
    private SwitchPreference soundOverrideVolume;
    private RingtonePreference soundAlarmTone;
    private SeekBarPreference volumeSeek;
    //endregion

    //region Alert Controls
    private EditTextPreference alertsAlertRetention;
    //endregion

    //endregion

    //endregion

    //region Android Lifecycle

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add 'general' preferences, defined in the XML file
        addPreferencesFromResource(R.xml.pref_general);

        //region Control Instantiation
        floodLimit = (EditTextPreference) (findPreference(
                getString(R.string.pref_flood_protection_message_limit_key)
        ));
        floodTime = (EditTextPreference) (findPreference(
                getString(R.string.pref_flood_protection_time_key)
        ));
        humanReadable = (EditTextPreference) (findPreference(
                getString(R.string.human_readable_flood_protection)
        ));
        alertsAlertRetention = (EditTextPreference) (findPreference(
                getString(R.string.pref_alert_retention_key)
        ));

        soundAlarmTone = (RingtonePreference) (findPreference(
                getString(R.string.pref_sound_alarm_tone_key)
        ));

        volumeSeek = (SeekBarPreference) (findPreference(
                getString(R.string.pref_alert_volume_key)
        ));

        floodProtection = (SwitchPreference) (findPreference(
                getString(R.string.pref_flood_protection_key)
        ));
        soundOverrideVolume = (SwitchPreference) (findPreference(
                getString(R.string.pref_override_volume_key)
        ));
        //endregion

        //region Control Binding
        // For all preferences, attach an OnPreferenceChangeListener so the UI summary can be
        // updated when the preference changes.
        bindPreferenceSummaryToValue(alertsAlertRetention);
        bindPreferenceSummaryToValue(floodLimit);
        bindPreferenceSummaryToValue(floodTime);
        bindPreferenceSummaryToValue(soundAlarmTone);

        bindSwitchPreferenceEnabledToValue(soundOverrideVolume);
        bindSwitchPreferenceEnabledToValue(floodProtection);
        //endregion
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        //region Control Instantiation
        LinearLayout root = (LinearLayout) findViewById(
                android.R.id.list
        ).getParent().getParent().getParent();

        Toolbar bar = (Toolbar) LayoutInflater.from(this).inflate(
                R.layout.toolbar_settings, root, false
        );
        //endregion

        //region Action Bar Creation
        root.addView(bar, 0); // insert at top
        bar.setNavigationOnClickListener(settingsBackButtonClick());
        //endregion
    }
    //endregion

    //region Preference Binders
    private void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(this);

        // Trigger the listener immediately with the preference's
        // current value.
        onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    private void bindSwitchPreferenceEnabledToValue(SwitchPreference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(this);


        onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getBoolean(preference.getKey(), preference.isChecked()));

    }
    //endregion

    //region Preference Change Management
    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        //region Alert Settings
        /*
        Populate alert retention summary with input number value
         */
        if (preference.getKey().equals(alertsAlertRetention.getKey())) {
            if (Integer.valueOf(newValue.toString()) > 0) {

                preference.setSummary(newValue.toString() + " " + getString(R.string.days));

                Intent logIntent = new Intent(this, AlertLogService.class);
                logIntent.setAction("timer-changed"); //Informs the service of a new value
                this.startService(logIntent);

            } else {

                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
                settings.edit().putString(alertsAlertRetention.getKey(), alertsAlertRetention.getText()).apply();
                Utilities.makeToast(this, "0 is not a valid entry for this field!");

            }
        }
        /*
        //endregion

        //region Sound Settings
        /*
        Populate the summary of the ringtone preference with the tone name
         */
        if (preference.getKey().equals(soundAlarmTone.getKey())) {
            Uri ringtoneUri = Uri.parse(newValue.toString());

            if (newValue.toString() == null)
                ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);

            Ringtone ringtone = RingtoneManager.getRingtone(this, ringtoneUri);

            preference.setSummary(ringtone.getTitle(this));
        }
        /*
        Enable/disable volume seek control when override volume switch changed
         */
        if (preference.getKey().equals(soundOverrideVolume.getKey())) {
            volumeSeek.setEnabled((Boolean) newValue);
        }
        //endregion

        //region Flood Settings
        /*
        Set the flood limit in the summary of the control and update
        the helpful human readable portion of text
         */
        if (preference.getKey().equals(floodLimit.getKey())) {
            String messagesTranslated = getString(R.string.messages);

            preference.setSummary(
                    newValue.toString() +
                            " " +
                            messagesTranslated
            );

            String limitSummary = preference.getSummary().toString();
            String timeSummary = floodTime.getSummary().toString();

            humanReadable.setSummary(
                    getString(R.string.human_readable_flood_preamble) +
                            limitSummary +
                            getString(R.string.within) +
                            timeSummary
            );
        }
        /*
        Set the flood time in the summary of the control and update
        the helpful human readable portion of text
         */
        if (preference.getKey().equals(floodTime.getKey())) {
            preference.setSummary(
                    newValue.toString() +
                            " " +
                            getString(R.string.seconds)
            );

            humanReadable.setSummary(
                    getString(R.string.human_readable_flood_preamble) +
                            floodLimit.getSummary() +
                            getString(R.string.within) +
                            preference.getSummary()
            );
        }
        /*
        Disable the FloodProtectionTimer when disabled in settings
         */
        if (preference.getKey().equals(floodProtection.getKey())) {
            if (!(Boolean) newValue) {
                //Prepare Intent for the Flood Protection Service
                Intent floodProtectionIntent = new Intent(this, FloodProtectionService.class);
                floodProtectionIntent.setAction("disable-timer");
                this.startService(floodProtectionIntent);
            }


        }
        //endregion

        return true;
    }
    //endregion

    //region Internal Functions
    private View.OnClickListener settingsBackButtonClick() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        };
    }
    //endregion
}