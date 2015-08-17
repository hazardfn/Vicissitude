//
//  Utilities.java
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

package com.apps.howard.vicissitude.classes;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;

/*
General utilities class for the app such as a wrapper for making toast (lol).
 */
public class Utilities {

    //region Generic Utility Functions
    /*
    Generic Utility Functions
     */

    /**
     * Displays a toast given a context and the text.
     *
     * @param context Application context
     * @param text    Text to display
     */
    public static void makeToast(Context context, String text) {
        Toast.makeText(context, text, Toast.LENGTH_LONG).show();
    }

    /**
     * @param context Application context
     * @return List of deserializable JSON alarms (See: SmsAlarm)
     */
    public static File[] getAllJsonFiles(Context context) {
        File dataDir = context.getFilesDir();

        return dataDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".json");
            }
        });
    }

    /**
     * Parses a list of JSON files and deserializes them to type SMSAlarm
     *
     * @param context Application context
     * @param files   List of deserializable JSON of type SMSAlarm
     * @return List of SMS Alarm objects
     */
    public static ArrayList<SmsAlarm> parseAlarmJson(Context context, File[] files) {
        ArrayList<SmsAlarm> retVal = new ArrayList<>();


        for (File file : files) {
            try {
                retVal.add(new SmsAlarm(file, context));
            } catch (IOException e) {
                Log.e("parseAlarmJson", e.getMessage());
            } catch (Exception ex) {
                Log.e("parseAlarmJson", "Corrupt Alarm: " + ex.getMessage());
                if (!file.delete())
                    Log.e("parseAlarmJson", "Corrupt Alarm could not be removed");
            }
        }

        return retVal;
    }

    /**
     * Pads an integer in order to display time appropriately
     * for example 1 becomes "01"
     *
     * @param c Hour/Minute/Second digit.
     * @return Padded digit (e.g 1 -> 01)
     */
    public static String pad(int c) {
        if (c >= 10)
            return String.valueOf(c);
        else
            return "0" + String.valueOf(c);
    }

    /**
     * Expands an array of strings to one string with a separator
     *
     * @param array     Array to expand
     * @param separator Separator to use
     * @return Single string with each value separated by separator value
     */
    public static String expandStringArray(ArrayList<String> array, String separator) {
        String retVal = "";

        for (String line : array) {

            retVal += line + separator;
        }

        if (retVal.length() > 0)
            return retVal.substring(0, retVal.length() - 1);

        return "";
    }

    //endregion

}
