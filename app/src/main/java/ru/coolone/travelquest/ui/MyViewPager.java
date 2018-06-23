package ru.coolone.travelquest.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

import lombok.Getter;
import lombok.Setter;
import ru.coolone.travelquest.R;

/**
 * My view pager implementation with Swipeable flag
 *
 * @author coolone
 * @since 23.06.18
 */
public class MyViewPager extends ViewPager{
    @Getter
    @Setter
    private boolean swipeable;

    public MyViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MyViewPager);
        try {
            swipeable = a.getBoolean(R.styleable.MyViewPager_swipeable, true);
        } finally {
            a.recycle();
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return swipeable ? super.onInterceptTouchEvent(event) : false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return swipeable ? super.onTouchEvent(event) : false;
    }
}
