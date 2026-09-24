package com.example.smmoney.views;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.text.Editable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import com.example.smmoney.R;
import com.example.smmoney.misc.CurrencyExt;
import com.example.smmoney.misc.Locales;
import com.example.smmoney.misc.PocketMoneyThemes;

import java.text.DecimalFormatSymbols;

public class CurrencyKeyboard extends FrameLayout implements View.OnKeyListener {
    private Context context;
    private EditText editText;
    private View toolbar;
    private boolean toolbarEnabled = true;

    public CurrencyKeyboard(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public CurrencyKeyboard(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        this.context = context;
        LayoutInflater.from(context).inflate(R.layout.keyboard_layout, this, true);
        setupKeys();
        refreshTheme();
    }

    private void setupKeys() {
        int[] keyIds = {
                R.id.key_0, R.id.key_1, R.id.key_2, R.id.key_3, R.id.key_4,
                R.id.key_5, R.id.key_6, R.id.key_7, R.id.key_8, R.id.key_9,
                R.id.key_dot, R.id.key_minus, R.id.key_clear,
                R.id.key_delete, R.id.key_next, R.id.key_hide
        };

        for (int id : keyIds) {
            View v = findViewById(id);
            if (v != null) {
                v.setOnClickListener(this::onButtonClick);
            }
        }
    }

    private void onButtonClick(View v) {
        int id = v.getId();
        if (this.editText == null) return;
        
        Editable editable = this.editText.getText();
        int start = this.editText.getSelectionStart();
        int end = this.editText.getSelectionEnd();

        if (id == R.id.key_delete) {
            if (start > 0 || start != end) {
                if (start == end) {
                    editable.delete(start - 1, start);
                } else {
                    editable.delete(start, end);
                }
            }
        } else if (id == R.id.key_clear) {
            this.editText.setText("");
        } else if (id == R.id.key_hide) {
            hide();
        } else if (id == R.id.key_next) {
            View next = this.editText.focusSearch(FOCUS_DOWN);
            if (next != null) {
                next.requestFocus();
                if (next instanceof EditText et) {
                    et.setSelection(et.getText().length());
                }
                return;
            }
            hide();
        } else if (id == R.id.key_minus) {
            String text = editable.toString();
            if (text.startsWith("-")) {
                editable.delete(0, 1);
            } else if (!text.isEmpty() && !"0".equals(text)) {
                editable.insert(0, "-");
            }
        } else if (id == R.id.key_dot) {
            editable.replace(start, end, String.valueOf(decimalSeparator()));
        } else if (v instanceof Button btn) {
            String text = btn.getText().toString();
            if (!text.isEmpty()) {
                editable.replace(start, end, text);
            }
        }
    }

    public void refreshTheme() {
        boolean isDark = PocketMoneyThemes.isDarkTheme();
        int gridLineColor = isDark ? 0xFF333333 : 0xFFE0E0E0;
        int numKeyColor = isDark ? 0xFF121212 : 0xFFFFFFFF; // Softer black
        int sideKeyColor = isDark ? 0xFF2A2A2A : 0xFFF5F5F5; // Slightly lighter contrast
        int actionKeyColor = PocketMoneyThemes.currentTintColor();
        int textColor = isDark ? 0xFFFFFFFF : 0xFF000000;
        int rippleColor = isDark ? 0x22FFFFFF : 0x22000000;

        findViewById(R.id.keyboard_grid).setBackgroundColor(gridLineColor);

        int[] allKeys = {
                R.id.key_0, R.id.key_1, R.id.key_2, R.id.key_3, R.id.key_4,
                R.id.key_5, R.id.key_6, R.id.key_7, R.id.key_8, R.id.key_9,
                R.id.key_dot, R.id.key_minus, R.id.key_clear,
                R.id.key_delete, R.id.key_next, R.id.key_hide
        };

        for (int id : allKeys) {
            View v = findViewById(id);
            final boolean isNext = (id == R.id.key_next);
            final int bgColor;
            if (isNext) {
                bgColor = actionKeyColor;
            } else if (isSideKey(id)) {
                bgColor = sideKeyColor;
            } else {
                bgColor = numKeyColor;
            }

            Drawable background = v.getBackground();
            if (background instanceof RippleDrawable ripple) {
                ripple.setColor(ColorStateList.valueOf(isNext ? 0x44FFFFFF : rippleColor));
                
                Drawable shape = ripple.getDrawable(0);
                if (shape instanceof GradientDrawable gd) {
                    gd.setColor(bgColor);
                    gd.setStroke((int) (0.5f * getResources().getDisplayMetrics().density), gridLineColor);
                }
            }
            
            if (v instanceof Button btn) {
                btn.setTextColor(isNext ? Color.WHITE : textColor);
                if (isNext) btn.setText(Locales.kLOC_GENERAL_NEXT);
            } else if (v instanceof ImageButton imgBtn) {
                imgBtn.setColorFilter(textColor, PorterDuff.Mode.SRC_IN);
            }
        }
    }

    private boolean isSideKey(int id) {
        if (id == R.id.key_minus) return true;
        if (id == R.id.key_clear) return true;
        if (id == R.id.key_delete) return true;
        return id == R.id.key_hide;
    }

    private char decimalSeparator() {
        return new DecimalFormatSymbols().getDecimalSeparator();
    }

    @SuppressLint("ClickableViewAccessibility")
    public void setEditText(EditText editText, final Runnable r) {
        this.editText = editText;
        final EditText theEdit = editText;
        
        theEdit.setShowSoftInputOnFocus(false);

        theEdit.setOnClickListener(v -> {
            CurrencyKeyboard.this.editText = theEdit;
            CurrencyKeyboard.this.show();
        });
        
        theEdit.setOnFocusChangeListener((v, hasFocus) -> {
            CurrencyKeyboard.this.editText = theEdit;
            if (hasFocus) {
                CurrencyKeyboard.this.show();
                if (r != null) {
                    r.run();
                }
            } else {
                CurrencyKeyboard.this.internalProcessMath();
                CurrencyKeyboard.this.hide();
            }
        });
        
        theEdit.setOnTouchListener((v, event) -> {
            v.requestFocus();
            CurrencyKeyboard.this.show();
            return true;
        });
    }

    public void setToolbarView(View toolbar) {
        this.toolbar = toolbar;
    }

    public void setToolbarEnabled(boolean toolbarEnabled) {
        this.toolbarEnabled = toolbarEnabled;
    }

    private void setToolbarVisibility(int visibility) {
        if ((visibility != 0 || this.toolbarEnabled) && this.toolbar != null) {
            this.toolbar.setVisibility(visibility);
        }
    }

    private void internalProcessMath() {
        if (this.editText == null) return;
        String currentText = this.editText.getText() != null ? this.editText.getText().toString().trim() : "";
        if (currentText.isEmpty()) {
            return;
        }
        String newValue = null;
        try {
            newValue = processMath(currentText);
        } catch (Exception e) {
            Log.e("CurrencyKeyboard", "Exception in processMath", e);
        }
        if (newValue != null && !newValue.isEmpty()) {
            this.editText.setText(newValue);
            this.editText.setSelection(this.editText.getText().toString().length());
        }
    }

    public void show() {
        refreshTheme();
        setToolbarVisibility(VISIBLE);
        setVisibility(View.VISIBLE);
        
        if (this.editText != null) {
            this.editText.setShowSoftInputOnFocus(false);
            InputMethodManager imm = (InputMethodManager) this.context.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(this.editText.getWindowToken(), 0);
            }
        }
        
        // Lock the window to never show keyboard automatically
        if (this.context instanceof Activity) {
            ((Activity) this.context).getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
            );
        }
    }

    public boolean hide() {
        setToolbarVisibility(GONE);
        if (getVisibility() != View.VISIBLE) {
            return false;
        }
        setVisibility(View.GONE);
        return true;
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (hasWindowFocus && getVisibility() == VISIBLE && this.editText != null) {
            // Immediate request to hide, followed by a post-loop guard
            InputMethodManager imm = (InputMethodManager) this.context.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(this.editText.getWindowToken(), 0);
            }
            // Catch the system "restore" event by posting to the end of the current message queue
            post(() -> {
                if (this.editText != null && imm != null) {
                    imm.hideSoftInputFromWindow(this.editText.getWindowToken(), 0);
                }
            });
        }
    }

    public static String processMath(String currentValue) {
        if (currentValue == null || currentValue.trim().isEmpty()) {
            return "";
        }
        double savedDouble;
        MyScanner s = new MyScanner(currentValue);
        char savedSign = '\u0000';
        double plusDouble = 0.0d;
        s.firstNumber();
        if (s.amount == null || s.amount.isEmpty()) {
            savedDouble = CurrencyExt.amountFromString(currentValue);
        } else {
            savedDouble = CurrencyExt.amountFromString(s.amount);
        }
        while (s.findNext()) {
            double currentDouble = CurrencyExt.amountFromString(s.amount);
            if (s.sign == '-' || s.sign == '+') {
                if (savedSign == '\u0000') {
                    plusDouble = savedDouble;
                    savedSign = s.sign;
                    savedDouble = currentDouble;
                } else {
                    if (savedSign == '+') {
                        plusDouble += savedDouble;
                    } else {
                        plusDouble -= savedDouble;
                    }
                    savedDouble = currentDouble;
                    savedSign = s.sign;
                }
            } else if (s.sign == '*') {
                savedDouble *= currentDouble;
            } else {
                if (currentDouble == 0.0d) {
                    currentDouble = 1.0d;
                }
                savedDouble /= currentDouble;
            }
        }
        if (savedSign == '\u0000') {
            plusDouble = savedDouble;
        } else if (s.sign == '+') {
            plusDouble += savedDouble;
        } else {
            plusDouble -= savedDouble;
        }
        return CurrencyExt.exchangeRateAsString(plusDouble);
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        return false;
    }

    private static class MyScanner {
        String amount = null;
        final int end;
        int index = 0;
        char sign = '\u0000';
        StringBuilder strBuff;
        final char[] theChars;

        MyScanner(String amount) {
            this.theChars = amount.toCharArray();
            this.end = this.theChars.length;
        }

        boolean findNext() {
            this.strBuff = new StringBuilder();
            while (this.index < this.end) {
                char c = this.theChars[this.index];
                this.index++;
                if (isDelimiter(c)) {
                    this.sign = c;
                    while (this.index < this.end) {
                        char c2 = this.theChars[this.index];
                        if (isDelimiter(c2)) {
                            break;
                        }
                        this.strBuff.append(c2);
                        this.index++;
                    }
                    this.amount = this.strBuff.toString();
                    return true;
                }
            }
            return false;
        }

        private boolean isDelimiter(char c) {
            if (c == '+' || c == '-') {
                return true;
            }
            char decimalSeparator = new DecimalFormatSymbols().getDecimalSeparator();
            if (c == decimalSeparator || Character.isDigit(c)) {
                return false;
            }
            return c != ' ' && c != ',' && c != '$' && c != '£' && c != '€';
        }

        void firstNumber() {
            this.strBuff = new StringBuilder();
            while (this.index < this.end) {
                char c = this.theChars[this.index];
                if (isDelimiter(c)) {
                    break;
                }
                this.strBuff.append(c);
                this.index++;
            }
            this.amount = this.strBuff.toString();
        }
    }
}
