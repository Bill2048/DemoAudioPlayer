package com.chaoxing.demo.audioplayer.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.SeekBar;

/**
 * Created by huwei on 2017/6/28.
 */

public class BufferedSeekBar extends SeekBar {


    private Sprite mSprite;

    public BufferedSeekBar(Context context) {
        super(context);
        init();
    }

    public BufferedSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BufferedSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    protected void init() {
        mSprite = new FadingCircle(){
            @Override
            public void drawChild(Canvas canvas) {
                super.drawChild(canvas);
                postInvalidate();
            }
        };
        mSprite.setColor(Color.GRAY);

        mSprite.start();
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            if (getThumb() != null) {
                Rect bounds = getThumb().getBounds();
                int w = getThumb().getIntrinsicWidth();
                Rect rf = new Rect(bounds.left + w - getThumbOffset() - 0, bounds.top+6, bounds.right + w - getThumbOffset() -12, bounds.bottom-6);
                mSprite.setBounds(rf);
                mSprite.draw(canvas);
            }
        }
    }

}
