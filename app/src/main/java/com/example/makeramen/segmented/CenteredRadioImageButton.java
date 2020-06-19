package com.example.makeramen.segmented;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatRadioButton;

import com.example.smmoney.R;

public class CenteredRadioImageButton extends AppCompatRadioButton {
    private final Drawable image;

    public CenteredRadioImageButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.image = context.obtainStyledAttributes(attrs, R.styleable.CompoundButton, 0, 0).getDrawable(0);
        setButtonDrawable(R.drawable.segment_button);
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (this.image != null) {
            float scale;
            this.image.setState(getDrawableState());
            int imgHeight = this.image.getIntrinsicHeight();
            int imgWidth = this.image.getIntrinsicWidth();
            int btnWidth = getWidth();
            int btnHeight = getHeight();
            if (imgWidth > btnWidth || imgHeight > btnHeight) {
                scale = Math.min(((float) btnWidth) / ((float) imgWidth), ((float) btnHeight) / ((float) imgHeight));
            } else {
                scale = 1.0f;
            }
            int dx = (int) (((((float) btnWidth) - (((float) imgWidth) * scale)) * 0.5f) + 0.5f);
            int dy = (int) (((((float) btnHeight) - (((float) imgHeight) * scale)) * 0.5f) + 0.5f);
            this.image.setBounds(dx, dy, (int) (((float) dx) + (((float) imgWidth) * scale)), (int) (((float) dy) + (((float) imgWidth) * scale)));
            this.image.draw(canvas);
        }
    }
}
