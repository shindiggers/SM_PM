package com.example.smmoney.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.text.Editable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
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
                if (next instanceof EditText) {
                    ((EditText) next).setSelection(((EditText) next).getText().length());
                }
                return;
            }
            hide();
        } else {
            String val = "";
            if (id == R.id.key_0) val = "0";
            else if (id == R.id.key_1) val = "1";
            else if (id == R.id.key_2) val = "2";
            else if (id == R.id.key_3) val = "3";
            else if (id == R.id.key_4) val = "4";
            else if (id == R.id.key_5) val = "5";
            else if (id == R.id.key_6) val = "6";
            else if (id == R.id.key_7) val = "7";
            else if (id == R.id.key_8) val = "8";
            else if (id == R.id.key_9) val = "9";
            else if (id == R.id.key_dot) val = String.valueOf(decimalSeparator());
            else if (id == R.id.key_minus) {
                String text = editable.toString();
                if (text.startsWith("-")) {
                    editable.delete(0, 1);
                } else if (!text.isEmpty() && !text.equals("0")) {
                    editable.insert(0, "-");
                }
                return;
            }

            if (!val.isEmpty()) {
                editable.replace(start, end, val);
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
            int bgColor = numKeyColor;
            
            if (id == R.id.key_minus || id == R.id.key_clear || id == R.id.key_delete || id == R.id.key_hide) {
                bgColor = sideKeyColor;
            } else if (id == R.id.key_next) {
                bgColor = actionKeyColor;
            }

            android.graphics.drawable.Drawable background = v.getBackground();
            if (background instanceof android.graphics.drawable.RippleDrawable) {
                android.graphics.drawable.RippleDrawable ripple = (android.graphics.drawable.RippleDrawable) background;
                ripple.setColor(android.content.res.ColorStateList.valueOf(id == R.id.key_next ? 0x44FFFFFF : rippleColor));
                
                android.graphics.drawable.Drawable shape = ripple.getDrawable(0);
                if (shape instanceof android.graphics.drawable.GradientDrawable) {
                    android.graphics.drawable.GradientDrawable gd = (android.graphics.drawable.GradientDrawable) shape;
                    gd.setColor(bgColor);
                    gd.setStroke((int) (0.5f * getResources().getDisplayMetrics().density), gridLineColor);
                }
            }
            
            if (v instanceof Button) {
                ((Button) v).setTextColor(id == R.id.key_next ? Color.WHITE : textColor);
                if (id == R.id.key_next) ((Button) v).setText(Locales.kLOC_GENERAL_NEXT);
            } else if (v instanceof ImageButton) {
                ((ImageButton) v).setColorFilter(textColor, PorterDuff.Mode.SRC_IN);
            }
        }
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
        String newValue = null;
        try {
            newValue = processMath(this.editText.getText().toString());
        } catch (Exception e) {
            Log.e("CurrencyKeyboard", "Exception in processMath", e);
        }
        if (newValue != null) {
            this.editText.setText(newValue);
            this.editText.setSelection(this.editText.getText().toString().length());
        } else {
            this.editText.setText("0");
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
        if (this.context instanceof android.app.Activity) {
            ((android.app.Activity) this.context).getWindow().setSoftInputMode(
                android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
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
        double savedDouble;
        MyScanner s = new MyScanner(currentValue);
        char savedSign = '\u0000';
        double plusDouble = 0.0d;
        s.firstNumber();
        if (s.amount == null || s.amount.length() <= 0) {
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
        int end;
        int index = 0;
        char sign = '\u0000';
        StringBuilder strBuff;
        char[] theChars;

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
