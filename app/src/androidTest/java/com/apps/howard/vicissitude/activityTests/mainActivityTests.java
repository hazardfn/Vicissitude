package com.apps.howard.vicissitude.activityTests;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.test.ActivityUnitTestCase;
import android.view.View;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import com.apps.howard.vicissitude.R;
import com.apps.howard.vicissitude.activities.MainActivity;
import com.apps.howard.vicissitude.activities.fragments.SmsFragment;
import com.apps.howard.vicissitude.classes.SmsAlarm;
import com.apps.howard.vicissitude.classes.Utilities;
import com.apps.howard.vicissitude.classes.tasks.FetchAlarmDataTask;

import java.io.File;
import java.util.ArrayList;

public class mainActivityTests
        extends ActivityUnitTestCase<MainActivity> {

    private MainActivity mMainActivity;
    private SmsFragment fSmsFragment;
    private ListView mListView;

    public mainActivityTests(Class<MainActivity> activityClass) {
        super(activityClass);
    }
    public mainActivityTests() {
        super(MainActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        startActivity(new Intent(
                getInstrumentation().getTargetContext(), MainActivity.class
        ), null, null);

        mMainActivity = getActivity();
        fSmsFragment = (SmsFragment) mMainActivity
                .getSupportFragmentManager()
                .findFragmentById(R.id.fragment_sms);
        mListView =
                (ListView) mMainActivity
                .findViewById(R.id.listview_sms_alarms);
    }

    public void testPreconditions() {
        assertNotNull("mMainActivity is null", mMainActivity);
        assertNotNull("mListView is null", mListView);
        assertEquals(0, mListView.getCount());
        assertNotNull("fSmsFragment is null", fSmsFragment);
    }

    public void testAlarmAdd() {
        final Context appContext =
                mMainActivity.getApplicationContext();
        final String expectedTitle =
                "Test Alarm";
        final String expectedSummary =
                "Test Summary";
        final boolean expectedState =
                false;
        final SmsAlarm expectedAlarm =
                new SmsAlarm(expectedTitle,expectedSummary,""
                ,new ArrayList<String>(), new ArrayList<String>()
                ,0,0,0,0,appContext);

        //Save the expectedAlarm and refresh the list
        SmsAlarm.Save(expectedAlarm, appContext);
        fSmsFragment.mSmsAdapter.clear();
        loadAlarms();

        //Check list count
        assertEquals(mListView.getCount(), 1);

        //Check control values
        final View actualView =
                mListView.getChildAt(0);
        final String actualTitle = ((TextView)actualView
                .findViewById(R.id.alarmTitle)).getText()
                .toString();
        final String actualSummary = ((TextView)actualView
                .findViewById(R.id.alarmSummary)).getText()
                .toString();
        final boolean actualState = ((Switch)actualView
        .findViewById(R.id.alarmActivated)).isChecked();

        assertEquals(expectedTitle, actualTitle);
        assertEquals(expectedState, actualState);
        assertEquals(expectedSummary, actualSummary);
    }

    private void loadAlarms() {
        File[] files = Utilities.getAllJsonFiles(getActivity());

        if (files != null)
            new FetchAlarmDataTask(fSmsFragment).execute(files);
    }


}
