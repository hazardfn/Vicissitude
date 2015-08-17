//
//  AlertLogContract.java
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

package com.apps.howard.vicissitude.classes.database;

import android.provider.BaseColumns;

public final class AlertLogContract {

    /* Inner class that defines the table contents */
    public static abstract class AlertLogEntry implements BaseColumns {

        public static final String TABLE_NAME = "alertlog";
        public static final String COLUMN_NAME_SERVICE = "service";
        public static final String COLUMN_NAME_ACTION = "action";
        public static final String COLUMN_NAME_ADDED = "date_added";
        public static final String NULL_COLUMN_HACK = "nullness";
    }
}
