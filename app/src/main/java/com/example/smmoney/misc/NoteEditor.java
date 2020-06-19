package com.example.smmoney.misc;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.example.smmoney.R;

import java.util.Objects;

public class NoteEditor extends Activity {
    private static final int DELETE_ID = 1;
    private static final String ORIGINAL_CONTENT = "origContent";
    private static final int SAVE_ID = 2;
    private String mOriginalContent;
    private EditText mText;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mOriginalContent = getIntent().getStringExtra("note");
        if (mOriginalContent == null) {
            mOriginalContent = "";
        }
        setContentView(R.layout.note_editor);
        mText = findViewById(R.id.note_editor_edittext);
        mText.setTextColor(-16777216);
        if (savedInstanceState != null) {
            this.mOriginalContent = savedInstanceState.getString(ORIGINAL_CONTENT);
        }
    }

    protected void onResume() {
        super.onResume();
        mText.setTextKeepState(mOriginalContent); // Populate mText with savedInstanceState text
        mText.postDelayed(new ShowKeyboard(), 300); // focus mText and show keyboard
    }

    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(ORIGINAL_CONTENT, mOriginalContent);
    }

    @SuppressWarnings("EmptyMethod")
    protected void onPause() {
        super.onPause();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuItem menuItem = menu.add(0, SAVE_ID, 0, "Save");
        menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        menuItem = menu.add(0, DELETE_ID, 0, "Cancel");
        menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case DELETE_ID /*1*/:
                setResult(0);
                finish();
                break;
            case SAVE_ID /*2*/:
                doneEditing();
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void doneEditing() {
        Intent i = new Intent();
        i.putExtra("selection", this.mText.getText().toString());
        setResult(-1, i);
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode != 4) {
            return super.onKeyDown(keyCode, event);
        }
        doneEditing();
        finish();
        return true;
    }

    private class ShowKeyboard implements Runnable {
        @Override
        // runnable to show keyboard automatically when NoteEditor opens in UI
        public void run() {
            mText.setFocusableInTouchMode(true);
            mText.requestFocus();
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            ((InputMethodManager) Objects.requireNonNull(getSystemService(Context.INPUT_METHOD_SERVICE))).showSoftInput(mText, 0);
        }
    }
}
