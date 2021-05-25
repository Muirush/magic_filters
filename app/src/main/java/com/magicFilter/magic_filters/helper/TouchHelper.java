package com.magicFilter.magic_filters.helper;

import android.view.MotionEvent;
import android.view.View;

public class TouchHelper {

    public static void touchAlpha(final View... views) {
        for (final View view : views) {
            view.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        view.setAlpha(0.5f);
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        view.setAlpha(1.0f);
                    } else if (event.getAction() == MotionEvent.ACTION_CANCEL) {
                        view.setAlpha(1.0f);
                    }
                    return false;
                }
            });
        }
    }

}
