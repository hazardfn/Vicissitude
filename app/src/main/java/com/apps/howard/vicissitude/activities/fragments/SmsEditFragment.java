package com.apps.howard.vicissitude.activities.fragments;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;

import com.apps.howard.vicissitude.R;
import com.apps.howard.vicissitude.classes.SmsAlarm;
import com.apps.howard.vicissitude.classes.Utilities;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * A placeholder fragment containing a simple view.
 */
public class SmsEditFragment extends Fragment {

    //region Fields

    /*
    Static ID's for creating TimePicker Dialogs
     */
    private static final int TIME_DIALOG_FROM_ID = 999;
    private static final int TIME_DIALOG_TO_ID = 998;

    /*
    Private fields needed for edits
     */
    private boolean isNew;
    private SmsAlarm currentAlarm;

    /*
    Stores TimePicker values
     */
    private int fromHour;
    private int toHour;
    private int fromMinute;
    private int toMinute;

    //endregion

    //region Fragment Controls
    private EditText Name;
    private EditText Summary;
    private EditText Numbers;
    private EditText Expressions;
    private EditText BodyTest;
    private EditText ClockFrom;
    private EditText ClockTo;
    private Button TestButton;
    //endregion

    //region Android Lifecycle

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        checkInvalidTime();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //region Control Instantiation
        Intent intent = getActivity().getIntent();
        View rootView = inflater.inflate(R.layout.fragment_sms_edit, container, false);

        Name = ((EditText) rootView.findViewById(
                R.id.txtName
        ));
        Summary = ((EditText) rootView.findViewById(
                R.id.txtSummary
        ));
        Numbers = ((EditText) rootView.findViewById(
                R.id.txtNumbers
        ));
        Expressions = ((EditText) rootView.findViewById(
                R.id.txtExpressions
        ));
        BodyTest = ((EditText) rootView.findViewById(
                R.id.txtBodyTest
        ));
        ClockTo = ((EditText) rootView.findViewById(
                R.id.clockTo
        ));
        ClockFrom = ((EditText) rootView.findViewById(
                R.id.clockFrom
        ));
        TestButton = ((Button) rootView.findViewById(
                R.id.btnTest
        ));
        //endregion

        setHasOptionsMenu(true);

        // Get data from intent
        this.currentAlarm = (SmsAlarm) intent.getExtras().get("alarm-class");
        this.isNew = intent.getExtras().getBoolean("is-new", false);

        // If the user has chosen to create a new alarm
        // we want to change the UI a little bit. We do not
        // allow name changes at this point because the validity
        // logic is handled in the MainActivity.
        if (this.isNew) {

            getActivity().setTitle(getString(R.string.sms_edit_alarm_new_title));
            resetClock();

        } else {

            this.fromHour = currentAlarm.fromHour;
            this.fromMinute = currentAlarm.fromMinute;
            this.toHour = currentAlarm.toHour;
            this.toMinute = currentAlarm.toMinute;

        }

        return updateUI(rootView);
    }

    //endregion

    //region EditText listeners
    private EditText.OnClickListener timePickerSetup(final int dialogConst, final String Title) {
        return new EditText.OnClickListener() {
            @Override
            public void onClick(View view) {
                Dialog fromDialog = createDialog(dialogConst);

                fromDialog.setTitle(Title);
                fromDialog.show();
            }
        };
    }
    //endregion

    //region Button Listeners
    private Button.OnClickListener testRegexClick() {
        return new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                String testResult = regexTest().trim();

                if (!testResult.equals(""))
                    Utilities.makeToast(view.getContext(), regexTest());
            }
        };
    }
    //endregion

    //region Internal Functions
    //region UI Functions
    private View updateUI(View rootView) {
        ClockFrom.setOnClickListener(timePickerSetup(
                        TIME_DIALOG_FROM_ID, getString(R.string.from_timepicker_dialog_title))
        );
        ClockTo.setOnClickListener(timePickerSetup(
                        TIME_DIALOG_TO_ID, getString(R.string.to_timepicker_dialog_title))
        );

        Name.setText(currentAlarm.name);
        Summary.setText(currentAlarm.summary);
        Numbers.setText(Utilities.expandStringArray(currentAlarm.triggeredSenders, ","));
        Expressions.setText(Utilities.expandStringArray(currentAlarm.triggeredExpressions, System.getProperty("line.separator")));
        BodyTest.setText(currentAlarm.body_test);

        TestButton.setOnClickListener(testRegexClick());

        return rootView;
    }
    //endregion

    //region Time Functions

    private void resetClock() {
        fromHour = 0;
        fromMinute = 0;
        toHour = 23;
        toMinute = 59;
    }

    private void checkInvalidTime() {
        if (invalidTime()) {
            Utilities.makeToast(getActivity(), "Invalid time format - 'to time' was likely set before 'from time'.");
            resetClock();
        }

        ((EditText) getActivity().findViewById(R.id.clockFrom)).setText(new StringBuilder().append(Utilities.pad(fromHour))
                .append(":").append(Utilities.pad(fromMinute)));

        ((EditText) getActivity().findViewById(R.id.clockTo)).setText(new StringBuilder().append(Utilities.pad(toHour))
                .append(":").append(Utilities.pad(toMinute)));
    }

    private boolean invalidTime() {
        Date fromTime = new Date();

        try {
            fromTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).parse(
                    String.valueOf(fromHour) +
                            ":" +
                            String.valueOf(fromMinute));
        } catch (ParseException e) {
            Log.e("invalidTime()", e.getMessage());
            Utilities.makeToast(getActivity(), e.getMessage());
        }

        Date toTime = new Date();

        try {
            toTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).parse(
                    String.valueOf(toHour) +
                            ":" +
                            String.valueOf(toMinute));
        } catch (ParseException e) {
            Log.e("invalidTime()", e.getMessage());
            Utilities.makeToast(getActivity(), e.getMessage());
        }

        return (!toTime.after(fromTime));
    }

    private Dialog createDialog(int id) {
        return new TimePickerDialog(getActivity(),
                timePickerListener(id), fromHour, fromMinute, true);
    }

    private TimePickerDialog.OnTimeSetListener timePickerListener(final int dialogConst) {
        return new TimePickerDialog.OnTimeSetListener() {
            public void onTimeSet(TimePicker view, int selectedHour, int selectedMinute) {

                if (dialogConst == TIME_DIALOG_FROM_ID) {
                    // set current time into textview
                    ((EditText) getActivity().findViewById(R.id.clockFrom)).setText(
                            new StringBuilder().append(Utilities.pad(selectedHour))
                                    .append(":").append(Utilities.pad(selectedMinute))
                    );

                    fromHour = selectedHour;
                    fromMinute = selectedMinute;

                } else {
                    // set current time into textview
                    ((EditText) getActivity().findViewById(R.id.clockTo)).setText(
                            new StringBuilder().append(Utilities.pad(selectedHour))
                                    .append(":").append(Utilities.pad(selectedMinute))
                    );

                    toHour = selectedHour;
                    toMinute = selectedMinute;
                }


                checkInvalidTime();

            }
        };
    }
    //endregion

    private boolean saveAlarm() {
        if (!isValid())
            return false;

        String Name = this.Name.getText().toString();
        String Summary = this.Summary.getText().toString();

        ArrayList<String> Numbers = new ArrayList<>(
                Arrays.asList(
                        this.Numbers.getText().toString()
                                .split(",")
                )
        );

        ArrayList<String> Expressions = new ArrayList<>(
                Arrays.asList(
                        this.Expressions.getText().toString()
                                .split(System.getProperty("line.separator"))
                )
        );

        String BodyTest = this.BodyTest.getText().toString();

        SmsAlarm generatedAlarm;

        if (isNew) {
            generatedAlarm = new SmsAlarm(Name, Summary, BodyTest, Numbers, Expressions
                    , fromHour, fromMinute, toHour, toMinute, getActivity());
        } else {
            generatedAlarm = new SmsAlarm(Name, Summary, BodyTest, Numbers, Expressions
                    , fromHour, fromMinute, toHour, toMinute, currentAlarm.location, currentAlarm.activated, getActivity());
        }

        SmsAlarm.Save(generatedAlarm, this.getActivity());

        return true;
    }

    private Boolean isValid() {
        if (Name.getText().toString().equals("")) {
            Utilities.makeToast(getActivity(), "Your alarm has no name!");
            return false;
        }
        if (Numbers.getText().toString().equals("")) {
            Utilities.makeToast(getActivity(), "Your alarm has no numbers to filter by!");
            return false;
        }

        return (!isRegexInvalid());
    }


    private boolean isRegexInvalid() {
        ArrayList<String> Expressions = new ArrayList<>(
                Arrays.asList(
                        this.Expressions.getText().toString()
                                .split(System.getProperty("line.separator"))
                )
        );

        for (String expression : Expressions) {
            try {
                Pattern.compile(expression);
            } catch (PatternSyntaxException exception) {

                Utilities.makeToast(getActivity(),
                        "The expression: '" +
                                expression +
                                "' is invalid."
                );

                return true;
            }
        }

        return false;
    }

    private String regexTest() {
        if (isRegexInvalid())
            return "";

        String BodyTest = this.BodyTest.getText().toString();

        ArrayList<String> Expressions = new ArrayList<>(
                Arrays.asList(
                        this.Expressions.getText().toString()
                                .split(System.getProperty("line.separator"))
                )
        );

        int matches = 0;

        for (String expression : Expressions) {
            if (BodyTest.matches(expression))
                matches += 1;
        }

        return "Body test matched " +
                String.valueOf(matches) +
                " expression(s)";
    }

    //endregion

    //region Menu Functions
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        // Fired when user has chosen to save the alarm
        // at this point it does not matter if
        // they are in create new mode or edit.
        if (id == R.id.action_save_alarm) {
            if (!saveAlarm())
                return super.onOptionsItemSelected(item);
        }

        getActivity().onBackPressed();

        return super.onOptionsItemSelected(item);
    }
    //endregion
}
