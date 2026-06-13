package com.example.smmoney.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.inputmethodservice.KeyboardView.OnKeyboardActionListener;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.example.smmoney.R;
import com.example.smmoney.misc.CurrencyExt;

import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@SuppressWarnings("deprecation")
public class CurrencyKeyboard extends KeyboardView implements OnKeyboardActionListener, OnKeyListener {
    private final Context context;
    private EditText editText;
    private Map<String, String> keyValues = null;
    private View toolbar;
    private boolean toolbarEnabled = true;

    private void init() {
        setKeyboard(new Keyboard(this.context, R.xml.keyboard));
        setEnabled(true);
        setPreviewEnabled(false);
        setOnKeyListener(this);
        setOnKeyboardActionListener(this);
        initKeyCodes();
    }

    public CurrencyKeyboard(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
        init();
    }

    public CurrencyKeyboard(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    private char decimalSeparator() {
        return new DecimalFormatSymbols().getDecimalSeparator();
    }

    @SuppressLint("ClickableViewAccessibility")
    public void setEditText(EditText editText, final Runnable r) {
        this.editText = editText;
        final EditText theEdit = editText;
        theEdit.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                CurrencyKeyboard.this.editText = theEdit;
                CurrencyKeyboard.this.show();
            }
        });
        theEdit.setOnFocusChangeListener(new OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                CurrencyKeyboard.this.editText = theEdit;
                if (hasFocus) {
                    CurrencyKeyboard.this.show();
                    if (r != null) {
                        r.run();
                        return;
                    }
                    return;
                }
                CurrencyKeyboard.this.processMath();
                CurrencyKeyboard.this.hide();
            }
        });
        theEdit.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                EditText editText1 = (EditText) v;
                int inType = editText1.getInputType();
                editText1.setInputType(InputType.TYPE_NULL);
                editText1.onTouchEvent(event);
                editText1.setInputType(inType);
                return true;
            }
        });
    }

    public void setToolbarView(View toolbar) {
        this.toolbar = toolbar;
    }

    private void setToolbarVisibility(int visibility) {
        if ((visibility != 0 || this.toolbarEnabled) && this.toolbar != null) {
            this.toolbar.setVisibility(visibility);
        }
    }

    public void setToolbarEnabled(boolean toolbarEnabled) {
        this.toolbarEnabled = toolbarEnabled;
    }

    private void processMath() {
        String newValue = null;
        try {
            newValue = processMath(this.editText.getText().toString());
        } catch (Exception e) {
            Log.e(com.example.smmoney.SMMoney.TAG, "Exception in processMath", e);
        }
        if (newValue != null) {
            this.editText.setText(newValue);
            this.editText.setSelection(this.editText.getText().toString().length());
            return;
        }
        this.editText.setText("0");
    }

    private void show() {
        ((InputMethodManager) Objects.requireNonNull(this.context.getSystemService(Context.INPUT_METHOD_SERVICE))).hideSoftInputFromWindow(this.editText.getWindowToken(), 0);
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            public void run() {
                if (CurrencyKeyboard.this.editText.hasFocus()) {
                    int i = CurrencyKeyboard.this.getVisibility();
                    if (i == View.INVISIBLE || i == View.GONE) {
                        CurrencyKeyboard.this.setVisibility(VISIBLE);
                        CurrencyKeyboard.this.setToolbarVisibility(VISIBLE);
                    }
                }
            }
        }, 150);
    }

    public boolean hide() {
        boolean isShowing = getVisibility() == VISIBLE;
        if (isShowing) {
            setVisibility(GONE);
            setToolbarVisibility(GONE);
        }
        return isShowing;
    }

    private void initKeyCodes() {
        this.keyValues = new HashMap<>();
        this.keyValues.put(String.valueOf(7), "0");
        this.keyValues.put(String.valueOf(8), "1");
        this.keyValues.put(String.valueOf(9), "2");
        this.keyValues.put(String.valueOf(10), "3");
        this.keyValues.put(String.valueOf(11), "4");
        this.keyValues.put(String.valueOf(12), "5");
        this.keyValues.put(String.valueOf(13), "6");
        this.keyValues.put(String.valueOf(14), "7");
        this.keyValues.put(String.valueOf(15), "8");
        this.keyValues.put(String.valueOf(16), "9");
        this.keyValues.put(String.valueOf(81), "+");
        this.keyValues.put(String.valueOf(69), "-");
        this.keyValues.put(String.valueOf(76), "/");
        this.keyValues.put(String.valueOf(17), "*");
        this.keyValues.put(String.valueOf(55), "00");
        this.keyValues.put(String.valueOf(56), String.valueOf(decimalSeparator()));
    }

    public String processMath(String currentValue) {
        if (currentValue == null || currentValue.isEmpty() || currentValue.equals("-")) {
            return "";
        }
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

    public void onKey(int primaryCode, int[] keyCodes) {
        Editable editable = this.editText.getText();
        int start = this.editText.getSelectionStart();
        if (primaryCode == 70 /*'Hide/Done' key*/) {
            hide();
        } else if (primaryCode == 28 /*'C' key */) {
            this.editText.setText("");
        } else if (primaryCode == 67 /*'del' key*/) {
            int end = this.editText.getSelectionEnd();
            if (start > 0 || start != end) {
                if (start == end) {
                    editable.delete(start - 1, start);
                } else {
                    editable.delete(start, end);
                }
            }
        } else if (primaryCode == 66 /*'Next' key*/) {
            View v = this.editText.focusSearch(FOCUS_DOWN);
            if (v != null) {
                v.requestFocus();
                ((EditText) v).setSelection(((EditText) v).getText().length());
                if (!(((EditText) v).getInputType() == 8194)) { /*8194 = HEX2002 = numberDecimal type see https://developer.android.com/reference/android/widget/TextView.html#attr_android%3AinputType*/
                    ((InputMethodManager) this.context.getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(v, 0);
                }
                return;
            }
            hide();
        } else {
            String val = this.keyValues.get(String.valueOf(primaryCode));
            if (val != null) {
                // Ignore operators (+, -, *, /) as they aren't supported by numberDecimal inputType
                if (primaryCode != 81 && primaryCode != 76 && primaryCode != 17 && primaryCode != 69) {
                    editable.insert(start, val);
                }
            }
        }
    }

    public boolean onKey(View v, int keyCode, KeyEvent event) {
        return false;
    }

    public void swipeUp() {
    }

    public void swipeRight() {
    }

    public void swipeLeft() {
    }

    public void swipeDown() {
    }

    public void onText(CharSequence text) {
    }

    public void onRelease(int primaryCode) {
    }

    public void onPress(int primaryCode) {
    }

    private static class MyScanner {
        String amount = null;
        final int end;
        int index = 0;
        char sign = '\u0000';
        StringBuilder strBuff;
        final char[] theChars;

        MyScanner(String s) {
            this.theChars = s.toCharArray();
            this.end = this.theChars.length;
        }

        boolean findNext() {
            if (this.index >= this.end) {
                return false;
            }
            int i = this.index;
            this.index = i + 1;
            this.sign = this.theChars[i];
            this.strBuff = new StringBuilder();
            while (this.index != this.end) {
                if (isDelimiter(this.theChars[this.index])) {
                    break;
                }
            }
            this.amount = this.strBuff.toString();
            return true;
        }

        private boolean isDelimiter(char c) {
            switch (c) {
                case '*':
                case '+':
                case '-':
                case '/':
                    return true;
                default:
                    this.strBuff.append(c);
                    this.index++;
                    return false;
            }
        }

        private void firstNumber() {
            this.strBuff = new StringBuilder();
            if (this.index == 0 && this.index != this.end && this.theChars[this.index] == '-') {
                this.index++;
            }
            while (this.index != this.end) {
                if (isDelimiter(this.theChars[this.index])) {
                    break;
                }
            }
            this.amount = this.strBuff.toString();
        }
    }
}
