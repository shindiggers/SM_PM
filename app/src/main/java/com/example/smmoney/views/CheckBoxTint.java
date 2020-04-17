package com.example.smmoney.views;

import android.content.res.ColorStateList;
import android.support.v4.widget.CompoundButtonCompat;
import android.widget.CheckBox;

import com.example.smmoney.misc.PocketMoneyThemes;

public class CheckBoxTint {

    public CheckBoxTint() {
        // empty constructor
    }

    public static void colorCheckBox(CheckBox checkBox) {
        int[][] states = new int[][]{
                /*new int[] {-android.R.attr.state_enabled},*/ // disabled
                new int[]{-android.R.attr.state_checked}, // unchecked
                /*new int[] { android.R.attr.state_pressed},*/  // pressed
                new int[]{android.R.attr.state_enabled} // enabled
        };
        int[] colors = new int[]{
                PocketMoneyThemes.chkBoxColorUnchecked(),
                PocketMoneyThemes.chkBoxColorChecked()
        };
        ColorStateList colorStateList = new ColorStateList(states, colors);
        CompoundButtonCompat.setButtonTintList(checkBox, colorStateList);
    }
}
