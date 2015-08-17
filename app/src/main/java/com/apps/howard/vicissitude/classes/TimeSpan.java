//
//  TimeSpan.java
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

/*
An incredibly stripped down java version of C#'s timespan class
stripped down to only those methods required by this app.
 */
public class TimeSpan {

    private final int _days;
    private final int _hours;
    private final int _minutes;
    private final int _seconds;

    /**
     * Initiate a timespan class using just seconds
     *
     * @param s Seconds
     */
    public TimeSpan(int s) {
        this._days = 0;
        this._hours = 0;
        this._minutes = 0;
        this._seconds = s;
    }

    private int Days() {
        return this._days;
    }

    private int Hours() {
        return this._hours;
    }

    private int Minutes() {
        return this._minutes;
    }

    private int Seconds() {
        return this._seconds;
    }

    /**
     * Return total seconds.
     *
     * @return Total seconds
     */
    private int TotalSeconds() {
        return (((((this.Days() * 24) + this.Hours()) * 60) + this.Minutes()) * 60) + this.Seconds();
    }

    /**
     * Returns total milliseconds
     *
     * @return Total milliseconds
     */
    public long TotalMilliseconds() {
        return (TotalSeconds() * 1000);
    }

    @Override
    public String toString() {
        if (this.Days() != 0) {
            return String.format("%d:%02d:%02d:%02d", this.Days(), this.Hours(), this.Minutes(), this.Seconds());
        } else {
            return String.format("%02d:%02d:%02d", this.Hours(), this.Minutes(), this.Seconds());
        }
    }

}