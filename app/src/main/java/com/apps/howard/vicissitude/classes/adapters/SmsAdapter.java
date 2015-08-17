//
//  SmsAdapter.java
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

package com.apps.howard.vicissitude.classes.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.apps.howard.vicissitude.R;
import com.apps.howard.vicissitude.classes.SmsAlarm;
import com.apps.howard.vicissitude.classes.Utilities;

import java.util.ArrayList;

public class SmsAdapter extends ArrayAdapter<SmsAlarm> {

    //region Controls
    private TextView tvName;
    private TextView tvSummary;
    private Switch sActivated;
    //endregion

    //region Constructor

    /**
     * Constructs the custom data adapter
     *
     * @param context   Application context
     * @param smsAlarms Array of alarms
     */
    public SmsAdapter(Context context, ArrayList<SmsAlarm> smsAlarms) {
        super(context, 0, smsAlarms);
    }
    //endregion

    //region SmsAdapter View
    @Override
    public View getView(final int position, final View convertView, ViewGroup parent) {
        // Get the data item for this position
        final SmsAlarm alarm = getItem(position);
        final View inflatedConvertView;

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            inflatedConvertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_sms_alarms, parent, false);
        } else {
            inflatedConvertView = convertView;
        }
        // Lookup view for data population
        tvName = (TextView) inflatedConvertView.findViewById(R.id.alarmTitle);
        tvSummary = (TextView) inflatedConvertView.findViewById(R.id.alarmSummary);
        sActivated = (Switch) inflatedConvertView.findViewById(R.id.alarmActivated);

        // Populate the data into the template view using the data object
        tvName.setText(alarm.name);
        tvSummary.setText(alarm.summary);
        sActivated.setChecked(alarm.activated);

        sActivated.setOnCheckedChangeListener(alarmSwitchChanged(alarm));

        // Return the completed view to render on screen
        return inflatedConvertView;
    }
    //endregion

    //region Listeners
    private CompoundButton.OnCheckedChangeListener alarmSwitchChanged(final SmsAlarm alarm) {
        return new Switch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (sActivated.isChecked() != alarm.activated) {
                    SmsAlarm newStatusAlarm = new SmsAlarm(alarm.name, alarm.summary,
                            alarm.body_test, alarm.triggeredSenders, alarm.triggeredExpressions,
                            alarm.fromHour, alarm.fromMinute, alarm.toHour, alarm.toMinute,
                            alarm.location, isChecked, getContext());

                    SmsAlarm.Save(newStatusAlarm, getContext());
                    clear();
                    addAll(Utilities.parseAlarmJson(getContext(), Utilities.getAllJsonFiles(getContext())));
                }
            }
        };
    }
    //endregion
}