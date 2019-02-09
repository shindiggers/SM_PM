package com.catamount.pocketmoney.misc;

import android.graphics.Color;
import com.catamount.pocketmoney.views.PasswordActivity;
import java.util.ArrayList;

public class ColorExt {
    public static ArrayList<Integer> colors = null;

    public static ArrayList<Integer> getColors() {
        if (colors == null) {
            colors = new ArrayList();
            colors.add(Color.rgb(26, 146, 23));
            colors.add(Color.rgb(185, 0, 0));
            colors.add(Color.rgb(0, 197, 211));
            colors.add(Color.rgb(235, 131, 18));
            colors.add(Color.rgb(102, 102, 102));
            colors.add(Color.rgb(79, 22, 179));
            colors.add(Color.rgb(255, 0, 246));
            colors.add(Color.rgb(73, 199, 102));
            colors.add(Color.rgb(0, 130, 212));
            colors.add(Color.rgb(158, 68, 15));
            colors.add(Color.rgb(235, 18, 125));
            colors.add(Color.rgb(255, 72, 0));
            colors.add(Color.rgb(0, 0, 0));
            colors.add(Color.rgb(78, 246, 3));
            colors.add(Color.rgb(247, 126, 69));
            colors.add(Color.rgb(250, 130, 190));
            colors.add(Color.rgb(95, 137, 240));
            colors.add(Color.rgb(28, 255, 226));
            colors.add(Color.rgb(221, 188, 7));
            colors.add(Color.rgb(PasswordActivity.PASSWORD_CORRECT, 6, 146));
        }
        return colors;
    }

    public static int getColorAtIndex(int index) {
        return getColors().get(index % getColors().size());
    }
}
