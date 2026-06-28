package com.example.smmoney.misc;

import android.content.Context;
import androidx.core.content.ContextCompat;
import com.example.smmoney.R;
import com.example.smmoney.SMMoney;

public class PocketMoneyThemes {

    public static final int kThemeBlack = 0;
    private static final int kThemeBlue = 1;
    private static final int kThemeCoffee = 5;
    private static final int kThemeGray = 4;
    private static final int kThemeGreen = 2;
    private static final int kThemePurple = 3;
    private static final int kThemeRuby = 6;
    private static final int kThemeWhite = 7;

    private static int theme = -1;

    private static int getColor(int id) {
        Context context = SMMoney.getAppContext();
        if (context == null) return 0;
        return ContextCompat.getColor(context, id);
    }

    public static void refreshTheme() {
        theme = -1;
    }

    public static void setTheme(String themeStr) {
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
        } else if (themeStr.equals("Ruby")) {
            theme = kThemeRuby;
        } else if (themeStr.equals("White")) {
            theme = kThemeWhite;
        }
    }

    private static int getTheme() {
        if (theme == -1) {
            String themeStr = Prefs.getStringPref(Prefs.THEME_COLOR);
            setTheme(themeStr);
            if (theme == -1) theme = kThemeWhite;
        }
        return theme;
    }

    public static int actionBarColor() {
        return switch (getTheme()) {
            case kThemeBlack -> getColor(R.color.black_theme_background_color);
            case kThemeBlue -> getColor(R.color.blue_theme_tint_color);
            case kThemeGreen -> getColor(R.color.green_theme_tint_color);
            case kThemePurple -> getColor(R.color.purple_theme_tint_color);
            case kThemeGray -> getColor(R.color.gray_theme_tint_color);
            case kThemeCoffee -> getColor(R.color.coffee_theme_tint_color);
            case kThemeRuby -> getColor(R.color.ruby_theme_tint_color);
            default -> getColor(R.color.white_theme_tint_color);
        };
    }

    public static int currentTintColor() {
        return switch (getTheme()) {
            case kThemeBlack -> getColor(R.color.black_theme_tint_color);
            case kThemeBlue -> getColor(R.color.blue_theme_tint_color);
            case kThemeGreen -> getColor(R.color.green_theme_tint_color);
            case kThemePurple -> getColor(R.color.purple_theme_tint_color);
            case kThemeGray -> getColor(R.color.gray_theme_tint_color);
            case kThemeCoffee -> getColor(R.color.coffee_theme_tint_color);
            case kThemeRuby -> getColor(R.color.ruby_theme_tint_color);
            default -> getColor(R.color.white_theme_tint_color);
        };
    }

    public static int groupTableViewBackgroundColor() {
        return switch (getTheme()) {
            case kThemeBlack -> getColor(R.color.black_theme_background_color);
            case kThemeBlue -> getColor(R.color.blue_theme_background_color);
            case kThemeGreen -> getColor(R.color.green_theme_background_color);
            case kThemePurple -> getColor(R.color.purple_theme_background_color);
            case kThemeGray -> getColor(R.color.gray_theme_background_color);
            case kThemeCoffee -> getColor(R.color.coffee_theme_background_color);
            case kThemeRuby -> getColor(R.color.ruby_theme_background_color);
            default -> getColor(R.color.white_theme_background_color);
        };
    }

    public static int simpleListItem() {
        return switch (getTheme()) {
            case kThemeBlue -> R.layout.theme_simple_list_blue;
            case kThemeGreen -> R.layout.theme_simple_list_green;
            case kThemePurple, kThemeRuby -> R.layout.theme_simple_list_purple;
            case kThemeGray, kThemeWhite -> R.layout.theme_simple_list_gray;
            case kThemeCoffee -> R.layout.theme_simple_list_coffee;
            default -> R.layout.theme_simple_list_black;
        };
    }

    public static int alternatingRowColor() {
        return switch (getTheme()) {
            case kThemeBlack -> getColor(R.color.black_theme_alternating_row_color);
            case kThemeBlue -> getColor(R.color.blue_theme_alternating_row_color);
            case kThemeGreen -> getColor(R.color.green_theme_alternating_row_color);
            case kThemePurple -> getColor(R.color.purple_theme_alternating_row_color);
            case kThemeGray -> getColor(R.color.gray_theme_alternating_row_color);
            case kThemeCoffee -> getColor(R.color.coffee_theme_alternating_row_color);
            case kThemeRuby -> getColor(R.color.ruby_theme_alternating_row_color);
            case kThemeWhite -> getColor(R.color.white_theme_alternating_row_color);
            default -> getColor(R.color.black_theme_alternating_row_color);
        };
    }

    public static int fieldLabelColor() {
        return switch (getTheme()) {
            case kThemeBlue -> getColor(R.color.blue_theme_field_label_color);
            case kThemeGreen -> getColor(R.color.green_theme_field_label_color);
            case kThemePurple -> getColor(R.color.purple_theme_field_label_color);
            case kThemeGray -> getColor(R.color.gray_theme_field_label_color);
            case kThemeCoffee -> getColor(R.color.coffee_theme_field_label_color);
            case kThemeRuby -> getColor(R.color.ruby_theme_field_label_color);
            case kThemeWhite -> getColor(R.color.white_theme_field_label_color);
            default -> getColor(R.color.black_theme_field_label_color);
        };
    }

    public static int preferenceScreenTheme() {
        return switch (getTheme()) {
            case kThemeBlue -> R.style.MyTheme_Blue;
            case kThemeGreen -> R.style.MyTheme_Green;
            case kThemePurple -> R.style.MyTheme_Purple;
            case kThemeGray -> R.style.MyTheme_Gray;
            case kThemeCoffee -> R.style.MyTheme_Coffee;
            case kThemeRuby -> R.style.MyTheme_Ruby;
            case kThemeWhite -> R.style.MyTheme_White;
            default -> R.style.MyTheme_Black;
        };
    }

    public static int datePickerTheme() {
        return switch (getTheme()) {
            case kThemeBlue -> R.style.DatePicker_Blue;
            case kThemeGreen -> R.style.DatePicker_Green;
            case kThemePurple -> R.style.DatePicker_Purple;
            case kThemeGray -> R.style.DatePicker_Gray;
            case kThemeCoffee -> R.style.DatePicker_Coffee;
            case kThemeRuby -> R.style.DatePicker_Ruby;
            case kThemeWhite -> R.style.DatePicker_White;
            default -> R.style.DatePicker_Black;
        };
    }

    public static int timePickerTheme() {
        return datePickerTheme();
    }

    public static int dialogTheme() {
        return switch (getTheme()) {
            case kThemeBlue -> R.style.DialogTheme_Blue;
            case kThemeGreen -> R.style.DialogTheme_Green;
            case kThemePurple -> R.style.DialogTheme_Purple;
            case kThemeGray -> R.style.DialogTheme_Gray;
            case kThemeCoffee -> R.style.DialogTheme_Coffee;
            case kThemeRuby -> R.style.DialogTheme_Ruby;
            case kThemeWhite -> R.style.DialogTheme_White;
            default -> R.style.DialogTheme_Black;
        };
    }

    public static int toolbarTextColor() {
        return switch (getTheme()) {
            case kThemeBlue -> getColor(R.color.blue_theme_field_label_color);
            case kThemeGreen -> getColor(R.color.green_theme_field_label_color);
            case kThemePurple -> getColor(R.color.purple_theme_field_label_color);
            case kThemeGray -> getColor(R.color.gray_theme_field_label_color);
            case kThemeCoffee -> getColor(R.color.coffee_theme_field_label_color);
            case kThemeRuby -> getColor(R.color.ruby_theme_field_label_color);
            case kThemeWhite -> getColor(R.color.white_theme_field_label_color);
            default -> getColor(R.color.black_theme_field_label_color);
        };
    }

    public static int currentTintToolbarButtonDrawable() {
        return switch (getTheme()) {
            case kThemeBlue -> R.drawable.theme_toolbar_selector_blue;
            case kThemeGreen -> R.drawable.theme_toolbar_selector_green;
            case kThemePurple -> R.drawable.theme_toolbar_selector_purple;
            case kThemeGray -> R.drawable.theme_toolbar_selector_gray;
            case kThemeCoffee -> R.drawable.theme_toolbar_selector_coffee;
            default -> R.drawable.theme_toolbar_selector_black;
        };
    }

    public static int currentTintDrawable() {
        return switch (getTheme()) {
            case kThemeBlue -> R.drawable.theme_gradient_blue;
            case kThemeGreen -> R.drawable.theme_gradient_green;
            case kThemePurple -> R.drawable.theme_gradient_purple;
            case kThemeGray -> R.drawable.theme_gradient_gray;
            case kThemeCoffee -> R.drawable.theme_gradient_coffee;
            case kThemeRuby -> R.drawable.theme_gradient_ruby;
            case kThemeWhite -> R.drawable.theme_gradient_white;
            default -> R.drawable.theme_gradient_black;
        };
    }

    public static int alternateCellTextColor() {
        return switch (getTheme()) {
            case kThemeBlue, kThemeGreen, kThemePurple, kThemeGray, kThemeCoffee, kThemeRuby, kThemeWhite -> getColor(R.color.white_theme_text_alt);
            default -> getColor(R.color.black_theme_text_alt);
        };
    }

    public static int primaryEditTextColor() {
        return switch (getTheme()) {
            case kThemeBlue, kThemeGreen, kThemePurple, kThemeGray, kThemeCoffee, kThemeRuby, kThemeWhite -> getColor(R.color.white_theme_text);
            default -> getColor(R.color.black_theme_text);
        };
    }

    public static int primaryEditTextColor_NJA() {
        return switch (getTheme()) {
            case kThemeBlue, kThemeGreen, kThemePurple, kThemeGray, kThemeCoffee, kThemeRuby, kThemeWhite -> getColor(R.color.white_theme_text);
            default -> getColor(R.color.white_theme_primary_row_color);
        };
    }

    public static int primaryHintTextColor() {
        return getColor(R.color.blue_theme_hint_color);
    }

    public static int primaryCellTextColor() {
        return switch (getTheme()) {
            case kThemeBlue, kThemeGreen, kThemePurple, kThemeGray, kThemeCoffee, kThemeRuby, kThemeWhite -> getColor(R.color.white_theme_text);
            default -> getColor(R.color.black_theme_text);
        };
    }

    public static int primaryRowSelector() {
        return switch (getTheme()) {
            case kThemeBlue, kThemeGreen, kThemePurple, kThemeGray, kThemeCoffee, kThemeRuby, kThemeWhite -> R.drawable.list_selector_bg_white;
            default -> R.drawable.list_selector_bg_black;
        };
    }

    public static int alternatingRowSelector() {
        return switch (getTheme()) {
            case kThemeBlue -> R.drawable.list_selector_bg_blue_alt;
            case kThemeGreen -> R.drawable.list_selector_bg_green_alt;
            case kThemePurple -> R.drawable.list_selector_bg_purple_alt;
            case kThemeGray -> R.drawable.list_selector_bg_gray_alt;
            case kThemeCoffee -> R.drawable.list_selector_bg_coffee_alt;
            case kThemeRuby -> R.drawable.list_selector_bg_ruby_alt;
            case kThemeWhite -> R.drawable.list_selector_bg_white_alt;
            case kThemeBlack -> R.drawable.list_selector_bg_black_alt;
            default -> R.drawable.list_selector_bg_black_alt;
        };
    }

    public static int settingsRowSelector() {
        return switch (getTheme()) {
            case kThemeBlack -> R.drawable.list_selector_bg_black_settings;
            case kThemeBlue, kThemeGreen, kThemePurple, kThemeGray, kThemeCoffee, kThemeRuby, kThemeWhite -> R.drawable.list_selector_bg_white;
            default -> R.drawable.list_selector_bg_white;
        };
    }

    public static int editRowSelector(int index) {
        return settingsRowSelector();
    }

    public static int redLabelColor() {
        return switch (getTheme()) {
            case kThemeBlue, kThemeGreen, kThemePurple, kThemeGray, kThemeCoffee, kThemeRuby, kThemeWhite -> getColor(R.color.theme_red_label_color);
            default -> getColor(R.color.theme_red_label_color_on_black);
        };
    }

    public static int redOnBlackLabelColor() {
        return getColor(R.color.theme_red_label_color_on_black);
    }

    public static int orangeLabelColor() {
        return getColor(R.color.theme_orange_label_color);
    }

    public static int greenDepositColor() {
        return getColor(R.color.theme_green_deposit_color);
    }

    public static int greenBarColor() {
        return getColor(R.color.theme_green_bar_color);
    }

    public static int redBarColor() {
        return getColor(R.color.theme_red_bar_color);
    }

    public static int balanceBarBackgroundColor() {
        return switch (getTheme()) {
            case kThemeBlack, kThemeBlue, kThemeGreen, kThemePurple, kThemeGray, kThemeCoffee, kThemeRuby, kThemeWhite -> getColor(R.color.black);
            default -> getColor(R.color.black);
        };
    }

    public static int balanceBarArrowColor() {
        return getColor(R.color.black_theme_text);
    }

    public static int balanceBarTextViewColor() {
        return getColor(R.color.black_theme_text);
    }

    public static int chkBoxColorChecked() {
        return switch (getTheme()) {
            case kThemeBlack -> getColor(R.color.black_theme_text_alt);
            case kThemeBlue -> getColor(R.color.blue_theme_tint_color);
            case kThemeGreen -> getColor(R.color.green_theme_tint_color);
            case kThemePurple -> getColor(R.color.purple_theme_tint_color);
            case kThemeGray -> getColor(R.color.gray_theme_tint_color);
            case kThemeCoffee -> getColor(R.color.coffee_theme_tint_color);
            case kThemeRuby -> getColor(R.color.ruby_theme_tint_color);
            default -> getColor(R.color.white_theme_tint_color);
        };
    }

    public static int chkBoxColorUnchecked() {
        return chkBoxColorChecked();
    }
}
