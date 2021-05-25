package com.magicFilter.magic_filters.widget;

import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.magicFilter.magic_filters.R;

public class LoadingDialog extends Dialog {

    public LoadingDialog(@NonNull Context context) {
        super(context, R.style.LoadingDialogStyle);
        init(context);
    }

    public LoadingDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        init(context);
    }

    protected LoadingDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        init(context);
    }

    private void init(Context c) {
        View view = View.inflate(c, R.layout.layout_loading, null);
        ImageView ivLoad = view.findViewById(R.id.iv_load);
        Glide.with(c)
                .asGif()
                .load(R.drawable.loading)
                .into(ivLoad);
        setContentView(view);
        setCancelable(false);
        show();
    }

    public void closeDialog() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                dismiss();
            }
        }, 800);
    }

}
