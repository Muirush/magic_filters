package com.magicFilter.magic_filters.widget;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

public class FontTextView extends AppCompatTextView {

    public FontTextView(@NonNull Context context) {
        super(context);
        setFontForAssets(context);
    }

    public FontTextView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setFontForAssets(context);
    }

    public FontTextView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setFontForAssets(context);
    }

    private void setFontForAssets(Context context) {
        Typeface typeface = Typeface.createFromAsset(context.getAssets(),"AMRCANXB.TTF");
        setTypeface(typeface);
    }

}
