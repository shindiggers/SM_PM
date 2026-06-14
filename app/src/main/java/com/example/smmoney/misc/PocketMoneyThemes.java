package com.example.smmoney.misc;

//import com.catamount.pocketmoney.R;

import android.graphics.Color;

import com.example.smmoney.R;

public class PocketMoneyThemes {
    private static final int black_theme_alternating_row_color = Color.parseColor("#ff333333");
    private static final int black_theme_background_color = Color.parseColor("#ff212121");
    private static final int black_theme_field_label_color = Color.parseColor("#fff7f7f7");
    private static final int black_theme_primary_row_color = Color.parseColor("#E6121212");
    private static final int black_theme_text = Color.parseColor("#fff7f7f7");
    private static final int black_theme_text_alt = Color.parseColor("#ff939393");
    private static final int black_theme_tint_color = Color.parseColor("#ffb8b8b8");

    private static final int blue_theme_alternating_row_color = -723718;
    private static final int blue_theme_background_color = -3813931;
    private static final int blue_theme_field_label_color = -13480059;
    //private static final int blue_theme_highlighted_color = -7105645;
    private static final int blue_theme_hint_color = -7829368;
    private static final int blue_theme_tint_color = -7560006;

    private static final int coffee_theme_alternating_row_color = -2305362;
    private static final int coffee_theme_background_color = -2305362;
    private static final int coffee_theme_field_label_color = -8372224;
    //private static final int coffee_theme_highlighted_color = -8372224;
    private static final int coffee_theme_tint_color = -8372224;

    private static final int gray_theme_alternating_row_color = -1644826;
    private static final int gray_theme_background_color = -3618616;
    private static final int gray_theme_field_label_color = -8553091;
    //private static final int gray_theme_highlighted_color = -8553091;
    private static final int gray_theme_tint_color = -8553091;

    private static final int green_theme_alternating_row_color = -920338;
    private static final int green_theme_background_color = -2433589;
    private static final int green_theme_field_label_color = -11690970;
    //private static final int green_theme_highlighted_color = -3218503;
    private static final int green_theme_tint_color = -8544931;

    public static final int kThemeBlack = 0;
    private static final int kThemeBlue = 1;
    private static final int kThemeCoffee = 5;
    private static final int kThemeGray = 4;
    private static final int kThemeGreen = 2;
    private static final int kThemePurple = 3;
    private static final int kThemeRuby = 6;
    private static final int kThemeWhite = 7;

    private static final int purple_theme_alternating_row_color = -267538;
    private static final int purple_theme_background_color = -267538;
    private static final int purple_theme_field_label_color = -8571043;
    //private static final int purple_theme_highlighted_color = -2577735;
    private static final int purple_theme_tint_color = -8571043;

    private static final int ruby_theme_alternating_row_color = -1735509;
    private static final int ruby_theme_background_color = -1400136;
    private static final int ruby_theme_field_label_color = -65468;
    //private static final int ruby_theme_highlighted_color = -65468;
    private static final int ruby_theme_tint_color = -7560006;

    private static final int theme_green_bar_color = -11690970;
    private static final int theme_green_deposit_color = -11690970;
    private static final int theme_orange_label_color = -31477;
    private static final int theme_red_bar_color = -3533804;
    private static final int theme_red_label_color = -3533804;
    private static final int theme_red_label_color_on_black = -39322;

    private static final int white_theme_alternating_row_color = -1513240;
    private static final int white_theme_background_color = -1;
    private static final int white_theme_field_label_color = -16777216;
    private static final int white_theme_primary_row_color = -1;
    private static final int white_theme_text = -16777216;
    private static final int white_theme_text_alt = -7105645;
    private static final int white_theme_tint_color = -4671304;

    private static int theme = white_theme_primary_row_color;

    public static void refreshTheme() {
        theme = white_theme_primary_row_color;
    }

    private static int getTheme() {
        if (theme == white_theme_primary_row_color) {
            String themeStr = Prefs.getStringPref(Prefs.THEME_COLOR);
            if (themeStr.equals(Locales.kLOC_THEME_COLOR_BLACK)) {
                theme = kThemeBlack;
            } else if (themeStr.equals(Locales.kLOC_THEME_COLOR_BLUE)) {
                theme = kThemeBlue;
            } else if (themeStr.equals(Locales.kLOC_THEME_COLOR_GREEN)) {
                theme = kThemeGreen;
            } else if (themeStr.equals(Locales.kLOC_THEME_COLOR_PURPLE)) {
                theme = kThemePurple;
            } else if (themeStr.equals(Locales.kLOC_THEME_COLOR_GRAY)) {
                theme = kThemeGray;
            } else if (themeStr.equals(Locales.kLOC_THEME_COLOR_COFFEE)) {
                theme = kThemeCoffee;
            }
        }
        return theme;
    }

    public static int actionBarColor() {
        return switch (getTheme()) {
            case kThemeBlack /*0*/ -> black_theme_primary_row_color;
            case kThemeBlue /*1*/ -> blue_theme_tint_color;
            case kThemeGreen /*2*/ -> green_theme_tint_color;
            case kThemePurple /*3*/ -> purple_theme_tint_color;
            case kThemeGray /*4*/ -> gray_theme_tint_color;
            case kThemeCoffee /*5*/ -> coffee_theme_tint_color;
            case kThemeRuby /*6*/ -> ruby_theme_tint_color;
            default -> white_theme_tint_color;
        };
    }


    public static int currentTintColor() {
        return switch (getTheme()) {
            case kThemeBlack /*0*/ -> black_theme_tint_color;
            case kThemeBlue /*1*/ -> blue_theme_tint_color;
            case kThemeGreen /*2*/ -> green_theme_tint_color;
            case kThemePurple /*3*/ -> purple_theme_tint_color;
            case kThemeGray /*4*/ -> gray_theme_tint_color;
            case kThemeCoffee /*5*/ -> coffee_theme_tint_color;
            case kThemeRuby /*6*/ -> ruby_theme_tint_color;
            default -> white_theme_tint_color;
        };
    }

    public static int groupTableViewBackgroundColor() {
        return switch (getTheme()) {
            case kThemeBlack /*0*/ -> black_theme_background_color;
            case kThemeBlue /*1*/ -> blue_theme_background_color;
            case kThemeGreen /*2*/ -> green_theme_background_color;
            case kThemePurple /*3*/ -> purple_theme_background_color;
            case kThemeGray /*4*/ -> gray_theme_background_color;
            case kThemeCoffee /*5*/ -> coffee_theme_background_color;
            case kThemeRuby /*6*/ -> ruby_theme_background_color;
            default -> white_theme_background_color;
        };
    }

    public static int simpleListItem() {
        return switch (getTheme()) {
            case kThemeBlue /*1*/ -> R.layout.theme_simple_list_blue;
            case kThemeGreen /*2*/ -> R.layout.theme_simple_list_green; /*3*/
            case kThemePurple, kThemeRuby /*6*/ -> R.layout.theme_simple_list_purple; /*4*/
            case kThemeGray, kThemeWhite /*7*/ -> R.layout.theme_simple_list_gray;
            case kThemeCoffee /*5*/ -> R.layout.theme_simple_list_coffee;
            default -> R.layout.theme_simple_list_black;
        };
    }

    public static int alternatingRowColor() {
        return switch (getTheme()) {
            case kThemeBlue /*1*/ -> blue_theme_alternating_row_color;
            case kThemeGreen /*2*/ -> green_theme_alternating_row_color;
            case kThemePurple /*3*/ -> purple_theme_alternating_row_color;
            case kThemeGray /*4*/ -> gray_theme_alternating_row_color;
            case kThemeCoffee /*5*/ -> coffee_theme_alternating_row_color;
            case kThemeRuby /*6*/ -> ruby_theme_alternating_row_color;
            case kThemeWhite /*7*/ -> white_theme_alternating_row_color;
            default -> black_theme_alternating_row_color;
        };
    }

    public static int fieldLabelColor() {
        return switch (getTheme()) {
            case kThemeBlue /*1*/ -> blue_theme_field_label_color;
            case kThemeGreen /*2*/ -> green_theme_field_label_color;
            case kThemePurple /*3*/ -> purple_theme_field_label_color;
            case kThemeGray /*4*/ -> gray_theme_field_label_color;
            case kThemeCoffee /*5*/ -> coffee_theme_field_label_color;
            case kThemeRuby /*6*/ -> ruby_theme_field_label_color;
            case kThemeWhite /*7*/ -> white_theme_field_label_color;
            default -> black_theme_field_label_color;
        };
    }

    public static int preferenceScreenTheme() {
        return switch (getTheme()) {
            case kThemeBlue /*1*/ -> R.style.MyTheme_Blue;
            case kThemeGreen /*2*/ -> R.style.MyTheme_Green;
            case kThemePurple /*3*/ -> R.style.MyTheme_Purple;
            case kThemeGray /*4*/ -> R.style.MyTheme_Gray;
            case kThemeCoffee /*5*/ -> R.style.MyTheme_Coffee;
            case kThemeRuby /*6*/ -> R.style.MyTheme_Ruby;
            case kThemeWhite /*7*/ -> R.style.MyTheme_White;
            default -> R.style.MyTheme_Black;
        };
    }

    public static int datePickerTheme() {
        return switch (getTheme()) {
            case kThemeBlue /*1*/ -> R.style.DatePicker_Blue;
            case kThemeGreen /*2*/ -> R.style.DatePicker_Green;
            case kThemePurple /*3*/ -> R.style.DatePicker_Purple;
            case kThemeGray /*4*/ -> R.style.DatePicker_Gray;
            case kThemeCoffee /*5*/ -> R.style.DatePicker_Coffee;
            case kThemeRuby /*6*/ -> R.style.DatePicker_Ruby;
            case kThemeWhite /*7*/ -> R.style.DatePicker_White;
            default -> R.style.DatePicker_Black;
        };
    }

    public static int dialogTheme() {
        return switch (getTheme()) {
            case kThemeBlue /*1*/ -> R.style.DialogTheme_Blue;
            case kThemeGreen /*2*/ -> R.style.DialogTheme_Green;
            case kThemePurple /*3*/ -> R.style.DialogTheme_Purple;
            case kThemeGray /*4*/ -> R.style.DialogTheme_Gray;
            case kThemeCoffee /*5*/ -> R.style.DialogTheme_Coffee;
            case kThemeRuby /*6*/ -> R.style.DialogTheme_Ruby;
            case kThemeWhite /*7*/ -> R.style.DialogTheme_White;
            default -> R.style.DialogTheme_Black;
        };
    }

    public static int toolbarTextColor() {
        return switch (getTheme()) {
            case kThemeBlue /*1*/ -> blue_theme_field_label_color;
            case kThemeGreen /*2*/ -> green_theme_field_label_color;
            case kThemePurple /*3*/ -> purple_theme_field_label_color;
            case kThemeGray /*4*/ -> gray_theme_field_label_color;
            case kThemeCoffee /*5*/ -> coffee_theme_field_label_color;
            case kThemeRuby /*6*/ -> ruby_theme_field_label_color;
            case kThemeWhite /*7*/ -> white_theme_field_label_color;
            default -> black_theme_field_label_color;
        };
    }

    public static int currentTintToolbarButtonDrawable() {
        return switch (getTheme()) {
            case kThemeBlue /*1*/ -> R.drawable.theme_toolbar_selector_blue;
            case kThemeGreen /*2*/ -> R.drawable.theme_toolbar_selector_green;
            case kThemePurple /*3*/ -> R.drawable.theme_toolbar_selector_purple;
            case kThemeGray /*4*/ -> R.drawable.theme_toolbar_selector_gray;
            case kThemeCoffee /*5*/ -> R.drawable.theme_toolbar_selector_coffee;
            default -> R.drawable.theme_toolbar_selector_black;
        };
    }

    public static int currentTintDrawable() {
        return switch (getTheme()) {
            case kThemeBlue /*1*/ -> R.drawable.theme_gradient_blue;
            case kThemeGreen /*2*/ -> R.drawable.theme_gradient_green;
            case kThemePurple /*3*/ -> R.drawable.theme_gradient_purple;
            case kThemeGray /*4*/ -> R.drawable.theme_gradient_gray;
            case kThemeCoffee /*5*/ -> R.drawable.theme_gradient_coffee;
            case kThemeRuby /*6*/ -> R.drawable.theme_gradient_ruby;
            case kThemeWhite /*7*/ -> R.drawable.theme_gradient_white;
            default -> R.drawable.theme_gradient_black;
        };
    }

    public static int alternateCellTextColor() {
        return switch (getTheme()) {
            case kThemeBlue/*1*/, kThemeGreen/*2*/, kThemePurple/*3*/, kThemeGray/*4*/, kThemeCoffee/*5*/, kThemeRuby/*6*/,
                 kThemeWhite /*7*/ -> white_theme_text_alt;
            default -> black_theme_text_alt;
        };
    }

    public static int primaryEditTextColor() {
        return switch (getTheme()) {
            case kThemeBlue/*1*/, kThemeGreen/*2*/, kThemePurple/*3*/, kThemeGray/*4*/, kThemeCoffee/*5*/, kThemeRuby/*6*/,
                 kThemeWhite /*7*/ -> white_theme_text;
            default -> black_theme_text;
        };
    }

    public static int primaryEditTextColor_NJA() {
        return switch (getTheme()) {
            case kThemeBlue/*1*/, kThemeGreen/*2*/, kThemePurple/*3*/, kThemeGray/*4*/, kThemeCoffee/*5*/, kThemeRuby/*6*/,
                 kThemeWhite /*7*/ -> white_theme_text;
            default -> white_theme_primary_row_color;
        };
    }

    public static int primaryHintTextColor() {
        //noinspection StatementWithEmptyBody
        switch (getTheme()) {
        }
        return blue_theme_hint_color;
    }

    public static int primaryCellTextColor() {
        return switch (getTheme()) {
            case kThemeBlue/*1*/, kThemeGreen/*2*/, kThemePurple/*3*/, kThemeGray/*4*/, kThemeCoffee/*5*/, kThemeRuby/*6*/,
                 kThemeWhite /*7*/ -> white_theme_text;
            default -> black_theme_text;
        };
    }

    public static int primaryRowSelector() {
        return switch (getTheme()) {
            case kThemeBlue/*1*/, kThemeGreen/*2*/, kThemePurple/*3*/, kThemeGray/*4*/, kThemeCoffee/*5*/, kThemeRuby/*6*/,
                 kThemeWhite /*7*/ -> R.drawable.list_selector_bg_white;
            default -> R.drawable.list_selector_bg_black;
        };
    }

    public static int alternatingRowSelector() {
        return switch (getTheme()) {
            case kThemeBlue /*1*/ -> R.drawable.list_selector_bg_blue_alt;
            case kThemeGreen /*2*/ -> R.drawable.list_selector_bg_green_alt;
            case kThemePurple /*3*/ -> R.drawable.list_selector_bg_purple_alt;
            case kThemeGray /*4*/ -> R.drawable.list_selector_bg_gray_alt;
            case kThemeCoffee /*5*/ -> R.drawable.list_selector_bg_coffee_alt;
            case kThemeRuby /*6*/ -> R.drawable.list_selector_bg_ruby_alt;
            case kThemeWhite /*7*/ -> R.drawable.list_selector_bg_white_alt;
            default -> R.drawable.list_selector_bg_black_alt;
        };
    }

    public static int redLabelColor() {
        return switch (getTheme()) {
            case kThemeBlue/*1*/, kThemeGreen/*2*/, kThemePurple/*3*/, kThemeGray/*4*/, kThemeCoffee/*5*/, kThemeRuby/*6*/,
                 kThemeWhite /*7*/ -> theme_red_label_color;
            default -> theme_red_label_color_on_black;
        };
    }

    public static int redOnBlackLabelColor() {
        return theme_red_label_color_on_black;
    }

    public static int orangeLabelColor() {
        return theme_orange_label_color;
    }

    public static int greenDepositColor() {
        return theme_green_deposit_color;
    }

    public static int greenBarColor() {
        return theme_green_bar_color;
    }

    public static int redBarColor() {
        return theme_red_bar_color;
    }

    public static int chkBoxColorChecked() {
        return switch (getTheme()) {
            case kThemeBlack /*0*/ -> black_theme_text_alt;
            case kThemeBlue /*1*/ -> blue_theme_tint_color;
            case kThemeGreen /*2*/ -> green_theme_tint_color;
            case kThemePurple /*3*/ -> purple_theme_tint_color;
            case kThemeGray /*4*/ -> gray_theme_tint_color;
            case kThemeCoffee /*5*/ -> coffee_theme_tint_color;
            case kThemeRuby /*6*/ -> ruby_theme_tint_color;
            default -> white_theme_tint_color;
        };

    }

    public static int chkBoxColorUnchecked() {
        return switch (getTheme()) {
            case kThemeBlack /*0*/ -> black_theme_text_alt;
            case kThemeBlue /*1*/ -> blue_theme_tint_color;
            case kThemeGreen /*2*/ -> green_theme_tint_color;
            case kThemePurple /*3*/ -> purple_theme_tint_color;
            case kThemeGray /*4*/ -> gray_theme_tint_color;
            case kThemeCoffee /*5*/ -> coffee_theme_tint_color;
            case kThemeRuby /*6*/ -> ruby_theme_tint_color;
            default -> white_theme_tint_color;
        };

    }
}
