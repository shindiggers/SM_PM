package com.catamount.pocketmoney.misc;

//import com.catamount.pocketmoney.R;

import com.catamount.pocketmoney.R;

public class PocketMoneyThemes {
    private static final int black_theme_alternating_row_color = -13421773;
    private static final int black_theme_background_color = -16777216;
    private static final int black_theme_field_label_color = -526345;
    private static final int black_theme_primary_row_color = -16777216;
    private static final int black_theme_text = -526345;
    private static final int black_theme_text_alt = -7105645;
    private static final int black_theme_tint_color = -4671304;
    private static final int blue_theme_alternating_row_color = -723718;
    private static final int blue_theme_background_color = -3813931;
    private static final int blue_theme_field_label_color = -13480059;
    private static final int blue_theme_highlighted_color = -7105645;
    private static final int blue_theme_hint_color = -7829368;
    private static final int blue_theme_tint_color = -7560006;
    private static final int coffee_theme_alternating_row_color = -2305362;
    private static final int coffee_theme_background_color = -2305362;
    private static final int coffee_theme_field_label_color = -8372224;
    private static final int coffee_theme_highlighted_color = -8372224;
    private static final int coffee_theme_tint_color = -8372224;
    private static final int gray_theme_alternating_row_color = -1644826;
    private static final int gray_theme_background_color = -3618616;
    private static final int gray_theme_field_label_color = -8553091;
    private static final int gray_theme_highlighted_color = -8553091;
    private static final int gray_theme_tint_color = -8553091;
    private static final int green_theme_alternating_row_color = -920338;
    private static final int green_theme_background_color = -2433589;
    private static final int green_theme_field_label_color = -11690970;
    private static final int green_theme_highlighted_color = -3218503;
    private static final int green_theme_tint_color = -8544931;
    public static final int kThemeBlack = 0;
    public static final int kThemeBlue = 1;
    public static final int kThemeCoffee = 5;
    public static final int kThemeGray = 4;
    public static final int kThemeGreen = 2;
    public static final int kThemePurple = 3;
    public static final int kThemeRuby = 6;
    public static final int kThemeWhite = 7;
    private static final int purple_theme_alternating_row_color = -267538;
    private static final int purple_theme_background_color = -267538;
    private static final int purple_theme_field_label_color = -8571043;
    private static final int purple_theme_highlighted_color = -2577735;
    private static final int purple_theme_tint_color = -8571043;
    private static final int ruby_theme_alternating_row_color = -1735509;
    private static final int ruby_theme_background_color = -1400136;
    private static final int ruby_theme_field_label_color = -65468;
    private static final int ruby_theme_highlighted_color = -65468;
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
            if (themeStr.equals("Black")) {
                theme = kThemeBlack;
            } else if (themeStr.equals("Blue")) {
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

    public static int currentTintColor() {
        switch (getTheme()) {
            case kThemeBlue /*1*/:
                return ruby_theme_tint_color;
            case kThemeGreen /*2*/:
                return green_theme_tint_color;
            case kThemePurple /*3*/:
                return purple_theme_tint_color;
            case kThemeGray /*4*/:
                return gray_theme_tint_color;
            case kThemeCoffee /*5*/:
                return coffee_theme_tint_color;
            case kThemeRuby /*6*/:
                return ruby_theme_tint_color;
            default:
                return white_theme_tint_color;
        }
    }

    public static int groupTableViewBackgroundColor() {
        switch (getTheme()) {
            case kThemeBlue /*1*/:
                return blue_theme_background_color;
            case kThemeGreen /*2*/:
                return green_theme_background_color;
            case kThemePurple /*3*/:
                return purple_theme_background_color;
            case kThemeGray /*4*/:
                return gray_theme_background_color;
            case kThemeCoffee /*5*/:
                return coffee_theme_background_color;
            case kThemeRuby /*6*/:
                return ruby_theme_background_color;
            default:
                return white_theme_text;
        }
    }

    public static int simpleListItem() {
        switch (getTheme()) {
            case kThemeBlue /*1*/:
                return R.layout.theme_simple_list_blue;
            case kThemeGreen /*2*/:
                return R.layout.theme_simple_list_green;
            case kThemePurple /*3*/:
            case kThemeRuby /*6*/:
                return R.layout.theme_simple_list_purple;
            case kThemeGray /*4*/:
            case kThemeWhite /*7*/:
                return R.layout.theme_simple_list_gray;
            case kThemeCoffee /*5*/:
                return R.layout.theme_simple_list_coffee;
            default:
                return R.layout.theme_simple_list_black;
        }
    }

    public static int alternatingRowColor() {
        switch (getTheme()) {
            case kThemeBlue /*1*/:
                return blue_theme_alternating_row_color;
            case kThemeGreen /*2*/:
                return green_theme_alternating_row_color;
            case kThemePurple /*3*/:
                return purple_theme_background_color;
            case kThemeGray /*4*/:
                return gray_theme_alternating_row_color;
            case kThemeCoffee /*5*/:
                return coffee_theme_background_color;
            case kThemeRuby /*6*/:
                return ruby_theme_alternating_row_color;
            case kThemeWhite /*7*/:
                return white_theme_alternating_row_color;
            default:
                return black_theme_alternating_row_color;
        }
    }

    public static int fieldLabelColor() {
        switch (getTheme()) {
            case kThemeBlue /*1*/:
                return blue_theme_field_label_color;
            case kThemeGreen /*2*/:
                return theme_green_deposit_color;
            case kThemePurple /*3*/:
                return purple_theme_tint_color;
            case kThemeGray /*4*/:
                return gray_theme_tint_color;
            case kThemeCoffee /*5*/:
                return coffee_theme_tint_color;
            case kThemeRuby /*6*/:
                return ruby_theme_highlighted_color;
            case kThemeWhite /*7*/:
                return white_theme_text;
            default:
                return black_theme_text;
        }
    }

    public static int preferenceScreenTheme() {
        switch (getTheme()) {
            case kThemeBlue /*1*/:
                return R.style.MyTheme_Blue;
            case kThemeGreen /*2*/:
                return R.style.MyTheme_Green;
            case kThemePurple /*3*/:
                return R.style.MyTheme_Purple;
            case kThemeGray /*4*/:
                return R.style.MyTheme_Gray;
            case kThemeCoffee /*5*/:
                return R.style.MyTheme_Coffee;
            case kThemeRuby /*6*/:
                return R.style.MyTheme_Ruby;
            case kThemeWhite /*7*/:
                return R.style.MyTheme_White;
            default:
                return R.style.MyTheme_Black;
        }
    }

    public static int toolbarTextColor() {
        return white_theme_primary_row_color;
    }

    public static int currentTintToolbarButtonDrawable() {
        switch (getTheme()) {
            case kThemeBlue /*1*/:
                return R.drawable.theme_toolbar_selector_blue;
            case kThemeGreen /*2*/:
                return R.drawable.theme_toolbar_selector_green;
            case kThemePurple /*3*/:
                return R.drawable.theme_toolbar_selector_purple;
            case kThemeGray /*4*/:
                return R.drawable.theme_toolbar_selector_gray;
            case kThemeCoffee /*5*/:
                return R.drawable.theme_toolbar_selector_coffee;
            default:
                return R.drawable.theme_toolbar_selector_black;
        }
    }

    public static int currentTintDrawable() {
        switch (getTheme()) {
            case kThemeBlue /*1*/:
                return R.drawable.theme_gradient_blue;
            case kThemeGreen /*2*/:
                return R.drawable.theme_gradient_green;
            case kThemePurple /*3*/:
                return R.drawable.theme_gradient_purple;
            case kThemeGray /*4*/:
                return R.drawable.theme_gradient_gray;
            case kThemeCoffee /*5*/:
                return R.drawable.theme_gradient_coffee;
            case kThemeRuby /*6*/:
                return R.drawable.theme_gradient_ruby;
            case kThemeWhite /*7*/:
                return R.drawable.theme_gradient_white;
            default:
                return R.drawable.theme_gradient_black;
        }
    }

    public static int alternateCellTextColor() {
        switch (getTheme()) {
        }
        return white_theme_text_alt;
    }

    public static int primaryEditTextColor() {
        switch (getTheme()) {
            case kThemeBlue /*1*/:
            case kThemeGreen /*2*/:
            case kThemePurple /*3*/:
            case kThemeGray /*4*/:
            case kThemeCoffee /*5*/:
            case kThemeRuby /*6*/:
            case kThemeWhite /*7*/:
                return white_theme_text;
            default:
                return black_theme_text;
        }
    }

    public static int primaryEditTextColor_NJA() {
        switch (getTheme()) {
            case kThemeBlue /*1*/:
            case kThemeGreen /*2*/:
            case kThemePurple /*3*/:
            case kThemeGray /*4*/:
            case kThemeCoffee /*5*/:
            case kThemeRuby /*6*/:
            case kThemeWhite /*7*/:
                return white_theme_text;
            default:
                return white_theme_primary_row_color;
        }
    }

    public static int primaryHintTextColor() {
        switch (getTheme()) {
        }
        return blue_theme_hint_color;
    }

    public static int primaryCellTextColor() {
        switch (getTheme()) {
            case kThemeBlue /*1*/:
            case kThemeGreen /*2*/:
            case kThemePurple /*3*/:
            case kThemeGray /*4*/:
            case kThemeCoffee /*5*/:
            case kThemeRuby /*6*/:
            case kThemeWhite /*7*/:
                return white_theme_text;
            default:
                return black_theme_text;
        }
    }

    public static int primaryRowSelector() {
        switch (getTheme()) {
            case kThemeBlue /*1*/:
            case kThemeGreen /*2*/:
            case kThemePurple /*3*/:
            case kThemeGray /*4*/:
            case kThemeCoffee /*5*/:
            case kThemeRuby /*6*/:
            case kThemeWhite /*7*/:
                return R.drawable.list_selector_bg_white;
            default:
                return R.drawable.list_selector_bg_black;
        }
    }

    public static int alternatingRowSelector() {
        switch (getTheme()) {
            case kThemeBlue /*1*/:
                return R.drawable.list_selector_bg_blue_alt;
            case kThemeGreen /*2*/:
                return R.drawable.list_selector_bg_green_alt;
            case kThemePurple /*3*/:
                return R.drawable.list_selector_bg_purple_alt;
            case kThemeGray /*4*/:
                return R.drawable.list_selector_bg_gray_alt;
            case kThemeCoffee /*5*/:
                return R.drawable.list_selector_bg_coffee_alt;
            case kThemeRuby /*6*/:
                return R.drawable.list_selector_bg_ruby_alt;
            case kThemeWhite /*7*/:
                return R.drawable.list_selector_bg_white_alt;
            default:
                return R.drawable.list_selector_bg_black_alt;
        }
    }

    public static int redLabelColor() {
        switch (getTheme()) {
            case kThemeBlue /*1*/:
            case kThemeGreen /*2*/:
            case kThemePurple /*3*/:
            case kThemeGray /*4*/:
            case kThemeCoffee /*5*/:
            case kThemeRuby /*6*/:
            case kThemeWhite /*7*/:
                return theme_red_label_color;
            default:
                return theme_red_label_color_on_black;
        }
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
        return theme_green_deposit_color;
    }

    public static int redBarColor() {
        return theme_red_label_color;
    }
}
