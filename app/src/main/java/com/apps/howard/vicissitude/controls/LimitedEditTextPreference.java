//
//  LimitedEditTextPreference.java
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

package com.apps.howard.vicissitude.controls;


import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.EditText;
import android.widget.Toast;

/**
 * EditText subclass created to enforce limit of the lines number in editable
 * text field
 */
public class LimitedEditTextPreference extends EditText {

    /**
     * Max characters to be present in editable text field
     */
    private final int maxCharacters = 109;
    /**
     * application context;
     */
    private final Context context;
    /**
     * Max lines to be present in editable text field
     */
    private int maxLines = 3;

    public LimitedEditTextPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
    }

    public LimitedEditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public LimitedEditTextPreference(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    public int getMaxLines() {
        return maxLines;
    }

    @Override
    public void setMaxLines(int maxLines) {
        this.maxLines = maxLines;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        TextWatcher watcher = new TextWatcher() {

            private String text;
            private int beforeCursorPosition = 0;

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                //TODO sth
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                text = s.toString();
                beforeCursorPosition = start;
            }

            @Override
            public void afterTextChanged(Editable s) {

            /* turning off listener */
                removeTextChangedListener(this);

            /* handling lines limit exceed */
                if (LimitedEditTextPreference.this.getLineCount() > maxLines) {
                    LimitedEditTextPreference.this.setText(text);
                    LimitedEditTextPreference.this.setSelection(beforeCursorPosition);
                }

            /* handling character limit exceed */
                if (s.toString().length() > maxCharacters) {
                    LimitedEditTextPreference.this.setText(text);
                    LimitedEditTextPreference.this.setSelection(beforeCursorPosition);
                    Toast.makeText(context, "text too long", Toast.LENGTH_SHORT)
                            .show();
                }

            /* turning on listener */
                addTextChangedListener(this);

            }
        };

        this.addTextChangedListener(watcher);
    }

}