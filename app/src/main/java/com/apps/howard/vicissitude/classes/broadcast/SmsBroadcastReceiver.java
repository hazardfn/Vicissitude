//
//  SmsBroadcastReceiver.java
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

package com.apps.howard.vicissitude.classes.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import com.apps.howard.vicissitude.classes.Utilities;
import com.apps.howard.vicissitude.services.SmsAlertCheckService;

/**
 * Responsible for picking up received SMS's and passing them on
 * to the SMSAlertCheckService - in theory this could become the
 * SmsAlertCheckService but it felt nicer to separate the concerns.
 * <p/>
 * Future beta test results may sway that decision :P
 */
public class SmsBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // Retrieves a map of extended data from the intent.
        final Bundle bundle = intent.getExtras();
        // Make sure that bundle isn't null.
        assert bundle != null;

        try {

            final Object[] pdusObj = (Object[]) bundle.get("pdus");
            assert pdusObj != null;

            for (Object aPdusObj : pdusObj) {

                // Gather data from the message
                SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) aPdusObj);
                String phoneNumber = currentMessage.getDisplayOriginatingAddress();
                String message = currentMessage.getDisplayMessageBody();

                //Prepare Intent for the SMSAlertCheckService
                Intent messageCheckIntent = new Intent(context, SmsAlertCheckService.class);
                messageCheckIntent.putExtra("service-sender", phoneNumber);
                messageCheckIntent.putExtra("service-body", message);

                // Very bad but necessary as if the default message app
                // is slower than us it overtakes our notification sound
                // at least in the VM, might not for physical devices.
                //
                // It shouldn't do this as the notification is set to
                // Insistent. But better safe than sorry - what's 3 seconds... Right!?
                // TODO: Find a nicer way of doing this .
                Thread.sleep(3000);

                // Pass the message on.
                context.startService(messageCheckIntent);

            } // end for loop

        } catch (Exception e) {
            // Something unexpected happened, log it, show the user and move on.
            Log.e("SmsBroadcastReceiver", "Exception Processing Incoming SMS: " + e);
            Utilities.makeToast(context, "Exception Processing Incoming SMS: " + e);
        }
    }
}
