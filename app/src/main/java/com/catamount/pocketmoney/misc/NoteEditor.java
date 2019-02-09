package com.catamount.pocketmoney.misc;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import com.catamount.pocketmoney.R;

public class NoteEditor extends Activity {
    private static final int DELETE_ID = 1;
    private static final String ORIGINAL_CONTENT = "origContent";
    private static final int SAVE_ID = 2;
    private String mOriginalContent;
    private EditText mText;

    public static class LinedEditText extends android.support.v7.widget.AppCompatEditText {
        private Paint mPaint = new Paint();
        private Rect mRect = new Rect();

        public LinedEditText(Context context, AttributeSet attrs) {
            super(context, attrs);
            this.mPaint.setStyle(Style.STROKE);
            this.mPaint.setColor(-2147483393);
        }

        protected void onDraw(Canvas canvas) {
            int count = getLineCount();
            Rect r = this.mRect;
            Paint paint = this.mPaint;
            for (int i = 0; i < count; i += NoteEditor.DELETE_ID) {
                int baseline = getLineBounds(i, r);
                canvas.drawLine((float) r.left, (float) (baseline + NoteEditor.DELETE_ID), (float) r.right, (float) (baseline + NoteEditor.DELETE_ID), paint);
            }
            super.onDraw(canvas);
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mOriginalContent = getIntent().getStringExtra("note");
        if (this.mOriginalContent == null) {
            this.mOriginalContent = "";
        }
        setContentView(R.layout.note_editor);
        this.mText = findViewById(R.id.note);
        this.mText.setTextColor(-16777216);
        if (savedInstanceState != null) {
            this.mOriginalContent = savedInstanceState.getString(ORIGINAL_CONTENT);
        }
    }

    protected void onResume() {
        super.onResume();
        this.mText.setTextKeepState(this.mOriginalContent);
    }

    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(ORIGINAL_CONTENT, this.mOriginalContent);
    }

    protected void onPause() {
        super.onPause();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, SAVE_ID, 0, "save").setShortcut('0', 's').setIcon(R.drawable.savings);
        menu.add(0, DELETE_ID, 0, "cancel").setShortcut('0', 'd').setIcon(R.drawable.abouticon);
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

    public void doneEditing() {
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
}
