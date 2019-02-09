package com.catamount.pocketmoney.views;

import android.app.Activity;
import android.content.Context;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.inputmethodservice.KeyboardView.OnKeyboardActionListener;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import com.catamount.pocketmoney.R;
import com.catamount.pocketmoney.misc.CurrencyExt;
import com.catamount.pocketmoney.misc.Enums;
import com.catamount.pocketmoney.views.lookups.LookupsListActivity;
import java.text.DecimalFormatSymbols;
import java.util.Hashtable;

public class CurrencyKeyboard extends KeyboardView implements OnKeyboardActionListener, OnKeyListener {
    Context context;
    EditText editText;
    private Hashtable<String, String> keyValues = null;
    int keyboardSize;
    int originalScrollViewHeight;
    View toolbar;
    boolean toolbarEnabled = true;

    private class MyScanner {
        String amount = null;
        int end = 0;
        int index = 0;
        char sign = '\u0000';
        StringBuffer strBuff;
        char[] theChars;

        public MyScanner(String s) {
            this.theChars = s.toCharArray();
            this.end = this.theChars.length;
        }

        public boolean findNext() {
            if (this.index >= this.end) {
                return false;
            }
            char[] cArr = this.theChars;
            int i = this.index;
            this.index = i + 1;
            this.sign = cArr[i];
            this.strBuff = new StringBuffer();
            while (this.index != this.end) {
                if (isDelimeter(this.theChars[this.index])) {
                    break;
                }
            }
            this.amount = this.strBuff.toString();
            return true;
        }

        private boolean isDelimeter(char c) {
            switch (c) {
                case Enums.kDesktopSyncStateReceivingPhotoACK /*42*/:
                case Enums.kDesktopSyncStatePhotoACKReceived /*43*/:
                case Enums.kDesktopSyncStatePhotoACKProcessed /*45*/:
                case Enums.kDesktopSyncStateSentRecentChanges /*47*/:
                    return true;
                default:
                    this.strBuff.append(c);
                    this.index++;
                    return false;
            }
        }

        private void firstNumber() {
            this.strBuff = new StringBuffer();
            if (this.index == 0 && this.index != this.end && this.theChars[this.index] == '-') {
                this.index++;
            }
            while (this.index != this.end) {
                if (isDelimeter(this.theChars[this.index])) {
                    break;
                }
            }
            this.amount = this.strBuff.toString();
        }
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

    private void init() {
        setKeyboard(new Keyboard(this.context, R.xml.keyboard));
        setEnabled(true);
        setPreviewEnabled(true);
        setOnKeyListener(this);
        setOnKeyboardActionListener(this);
        initKeyCodes();
        this.keyboardSize = (int) (((double) ((Activity) this.context).getWindowManager().getDefaultDisplay().getHeight()) * 0.4425d);
    }

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

    public char decimalSeperator() {
        return new DecimalFormatSymbols().getDecimalSeparator();
    }

    public void show() {
        ((InputMethodManager) this.context.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(this.editText.getWindowToken(), 0);
        new Handler().postDelayed(new Runnable() {
            public void run() {
                if (CurrencyKeyboard.this.editText.hasFocus()) {
                    switch (CurrencyKeyboard.this.getVisibility()) {
                        case View.INVISIBLE /*4*/:
                        case View.GONE /*8*/:
                            CurrencyKeyboard.this.setVisibility(VISIBLE);
                            CurrencyKeyboard.this.setToolbarVisibility(0);
                            return;
                        default:
                            return;
                    }
                }
            }
        }, 300);
    }

    public boolean hide() {
        boolean isShowing = getVisibility() == VISIBLE;
        if (isShowing) {
            setVisibility(GONE);
            setToolbarVisibility(8);
        }
        return isShowing;
    }

    private void processMath() {
        String newValue = null;
        try {
            newValue = processMath(this.editText.getText().toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (newValue != null) {
            this.editText.setText(newValue);
            this.editText.setSelection(this.editText.getText().toString().length());
            return;
        }
        this.editText.setText(0);
    }

    public String processMath(String currentValue) {
        if (currentValue == null || currentValue.length() == 0 || currentValue.equals("-")) {
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

    private void initKeyCodes() {
        this.keyValues = new Hashtable();
        this.keyValues.put(Integer.toString(7), "0");
        this.keyValues.put(Integer.toString(8), "1");
        this.keyValues.put(Integer.toString(9), "2");
        this.keyValues.put(Integer.toString(10), "3");
        this.keyValues.put(Integer.toString(11), "4");
        this.keyValues.put(Integer.toString(12), "5");
        this.keyValues.put(Integer.toString(13), "6");
        this.keyValues.put(Integer.toString(14), "7");
        this.keyValues.put(Integer.toString(15), "8");
        this.keyValues.put(Integer.toString(16), "9");
        this.keyValues.put(Integer.toString(81), "+");
        this.keyValues.put(Integer.toString(69), "-");
        this.keyValues.put(Integer.toString(76), "/");
        this.keyValues.put(Integer.toString(17), "*");
        this.keyValues.put(Integer.toString(55), "00");
        StringBuffer str = new StringBuffer();
        str.append(decimalSeperator());
        this.keyValues.put(Integer.toString(56), str.toString());
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

    public void onKey(int primaryCode, int[] keyCodes) {
        if (primaryCode == 70) {
            processMath();
        } else if (primaryCode == 28) {
            this.editText.setText("");
        } else if (primaryCode == 67) {
            if (this.editText.getText().toString().length() > 0) {
                int i = this.editText.getSelectionStart();
                String theText = this.editText.getText().toString();
                if (i == 0) {
                    return;
                }
                if (i == theText.length()) {
                    this.editText.setText(this.editText.getText().toString().substring(0, this.editText.getText().toString().length() - 1));
                    this.editText.setSelection(i - 1);
                    return;
                }
                String firstHalf = theText.substring(0, i - 1);
                this.editText.setText(new StringBuilder(String.valueOf(firstHalf)).append(theText.substring(i)).toString());
                this.editText.setSelection(i - 1);
            }
        } else if (primaryCode == 66) {
            View v = this.editText.focusSearch(FOCUS_DOWN);
            if (v != null) {
                v.requestFocus();
                ((InputMethodManager) this.context.getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(v, 0);
                return;
            }
            hide();
        } else {
            int i = this.editText.getSelectionStart();
            String theText = this.editText.getText().toString();
            if (i == 0) {
                if (primaryCode != 81 && primaryCode != 76 && primaryCode != 17 && primaryCode != 69) {
                    this.editText.setText(new StringBuilder(String.valueOf(this.keyValues.get(Integer.toString(primaryCode)))).append(this.editText.getText().toString()).toString());
                    this.editText.setSelection(1);
                }
            } else if (i == theText.length()) {
                this.editText.append(this.keyValues.get(Integer.toString(primaryCode)));
            } else {
                String firstHalf = theText.substring(0, i);
                this.editText.setText(new StringBuilder(String.valueOf(firstHalf)).append(this.keyValues.get(Integer.toString(primaryCode))).append(theText.substring(i)).toString());
                this.editText.setSelection(i + 1);
            }
        }
    }
}
