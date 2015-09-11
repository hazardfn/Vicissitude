package com.apps.howard.vicissitude.activityTests;

import android.content.Intent;
import android.test.ActivityUnitTestCase;
import android.widget.Button;

import com.apps.howard.vicissitude.R;
import com.apps.howard.vicissitude.activities.MainActivity;

/**
 * Created by howardbeard-marlowe on 26/08/15.
 */
public class mainActivityTests
        extends ActivityUnitTestCase<MainActivity> {

    public mainActivityTests(Class<MainActivity> activityClass) {
        super(activityClass);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Intent mLaunchIntent = new Intent(getInstrumentation()
                .getTargetContext(), MainActivity.class);
        startActivity(mLaunchIntent, null, null);
    }

}
