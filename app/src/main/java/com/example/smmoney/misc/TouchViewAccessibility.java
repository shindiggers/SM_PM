package com.example.smmoney.misc;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class TouchViewAccessibility extends View {
    private boolean touchOn;
    private boolean mDownTouch = false;

    public TouchViewAccessibility(Context context) {
        super(context);
        init();
    }

    public TouchViewAccessibility(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        touchOn = false;
    }

    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);

        int action = event.getAction();
/*
        if (action == MotionEvent.ACTION_DOWN) {
            touchOn = !touchOn;
            invalidate();
            return true;
        }

        return false;
*/
        // Listening for the down and up touch events
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:

                touchOn = !touchOn;
                invalidate();

                mDownTouch = true;
                return true;

            case MotionEvent.ACTION_UP:
                if (mDownTouch) {
                    mDownTouch = false;
                    performClick(); // Call this method to handle the response, and
                    // thereby enable accessibility services to
                    // perform this action for a user who cannot
                    // click the touchscreen.
                    return true;
                }
        }
        return false; // Return false for other touch events
    }

    @Override
    public boolean performClick() {
        // Calls the super implementation, which generates an AccessibilityEvent
        // and calls the onClick() listener on the view, if any
        super.performClick();

        // Handle the action for the custom click here

        return true;
    }

}

