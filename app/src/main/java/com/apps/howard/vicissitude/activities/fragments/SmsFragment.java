package com.apps.howard.vicissitude.activities.fragments;

//region Imports

/*
Android imports.
 */

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import com.apps.howard.vicissitude.R;
import com.apps.howard.vicissitude.activities.LogActivity;
import com.apps.howard.vicissitude.activities.SmsEditActivity;
import com.apps.howard.vicissitude.classes.SmsAlarm;
import com.apps.howard.vicissitude.classes.Utilities;
import com.apps.howard.vicissitude.classes.adapters.SmsAdapter;
import com.apps.howard.vicissitude.classes.tasks.FetchAlarmDataTask;

import java.io.File;

/*
Application imports.
 */
/*
Java imports.
 */

//endregion

public class SmsFragment extends Fragment {

    //region Fields

    /*
    SmsAlarmAdapter for the MainActivity ListView
     */
    public SmsAdapter mSmsAdapter;

    /*
    Used as the title for the New Alarm dialog.
     */
    private String dialog_text = "";

    //endregion

    //region Android Lifecyle

    /*
    Android lifecycle.
     */
    @Override
    public void onStart() {
        super.onStart();
        loadAlarms();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadAlarms();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }
    //endregion

    //region Menu Code

    /*
    Inflate the SMS Menu fragment
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_sms_fragment, menu);
    }

    /*
    Logic for actionbar items
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_add_alarm) {
            addAlarm();
        }
        if (id == R.id.action_activate_all) {
            boolean ACTIVATE_ALL = true;
            switchAll(true);
        }
        if (id == R.id.action_deactivate_all) {
            boolean DEACTIVATE_ALL = false;
            switchAll(false);
        }
        if (id == R.id.action_view_logs) {
            Intent viewLogs = new Intent(this.getActivity(), LogActivity.class);
            startActivity(viewLogs);
            return true;
        }
        if (id == R.id.action_help) {
            Utilities.makeToast(this.getActivity(), getString(R.string.spartan_easter_egg));
        }

        return super.onOptionsItemSelected(item);
    }

    /*
    Create main activity
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_sms, container, false);

        ListView l = (ListView) rootView.findViewById(R.id.listview_sms_alarms);
        l.setOnItemClickListener(listViewClick());

        return rootView;
    }

    /*
    ListView context menu
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId() == R.id.listview_sms_alarms) {
            MenuInflater inflater = getActivity().getMenuInflater();
            inflater.inflate(R.menu.menu_list_actions, menu);
        }
    }

    /*
    Action Bar Buttons
     */
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.edit:
                editAlarm(info.position);
                return true;
            case R.id.delete:
                deleteAlarm(info.position);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }
    //endregion

    //region Internal Methods
    private void loadAlarms() {
        File[] files = Utilities.getAllJsonFiles(getActivity());

        if (files != null)
            new FetchAlarmDataTask(this).execute(files);
    }

    private void addAlarm() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.sms_fragment_new_alarm_dialog_title);

        builder.setPositiveButton("OK", positiveDialogButton(builder));
        builder.setNegativeButton("Cancel", negativeDialogButton());

        builder.show();
    }

    private void editAlarm(int position) {
        SmsAlarm alarm = mSmsAdapter.getItem(position);
        Intent smsAlertEditIntent = new Intent(getActivity(), SmsEditActivity.class)
                .putExtra("alarm-class", alarm);
        startActivity(smsAlertEditIntent);
    }

    private void deleteAlarm(int position) {
        SmsAlarm alarm = mSmsAdapter.getItem(position);
        File json = new File(alarm.location);

        if (json.delete())
            mSmsAdapter.remove(alarm);
    }

    private void switchAll(boolean Switch) {
        ListView l = (ListView) this.getActivity().findViewById(R.id.listview_sms_alarms);

        for (int i = 0; i < l.getCount(); i++) {
            SmsAlarm alarm = (SmsAlarm) l.getItemAtPosition(i);
            SmsAlarm.Save(new SmsAlarm(alarm.name, alarm.summary, alarm.body_test,
                    alarm.triggeredSenders, alarm.triggeredExpressions,
                    alarm.fromHour, alarm.fromMinute, alarm.toHour,
                    alarm.toMinute, alarm.location, Switch,
                    this.getActivity()), this.getActivity());
        }

        mSmsAdapter.clear();
        loadAlarms();
    }

    //region Listeners
    private AdapterView.OnItemClickListener listViewClick() {
        return new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                editAlarm(position);
            }
        };
    }

    private DialogInterface.OnClickListener positiveDialogButton(AlertDialog.Builder builder) {
        /*
        Create the text box
         */
        final EditText input = new EditText(getActivity());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        /*
        Create positive button
         */
        return new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog_text = input.getText().toString().trim();

                if (dialog_text.isEmpty()) {
                    Utilities.makeToast(getActivity(), getString(R.string.create_sms_blank_name));
                    dialog.cancel();
                    return;
                }

                SmsAlarm newAlarm = new SmsAlarm(dialog_text, getActivity());

                Intent smsAlertEditIntent = new Intent(getActivity(), SmsEditActivity.class)
                        .putExtra("alarm-class", newAlarm)
                        .putExtra("is-new", true);

                startActivity(smsAlertEditIntent);
            }
        };
    }

    private DialogInterface.OnClickListener negativeDialogButton() {
        /*
        Create negative button
         */
        return new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        };
    }
    //endregion

    //endregion
}
