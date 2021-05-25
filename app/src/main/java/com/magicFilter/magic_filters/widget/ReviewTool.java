package com.magicFilter.magic_filters.widget;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.cardview.widget.CardView;

import com.magicFilter.magic_filters.BuildConfig;
import com.magicFilter.magic_filters.R;

public class ReviewTool extends Dialog {

    private ReviewDel reviewDel;
    private View view;
    private Context context;
    private CardView cardView;
    private ScoreView scoreView;// 动画
    private ImageView submitBtn;
    private static String googlePlay = "com.android.vending";

    Float rank = (float) 0.0;

    public ReviewTool(final Context context, ReviewDel reviewDel) {
        /*
         * 这里是Dialog显示效果的关键，
         * 一定要把themeResId传进去。
         */
        super(context, R.style.LoadingDialogStyle);
        this.context = context;
        this.reviewDel = reviewDel;
        // 加载布局文件

        view = LayoutInflater.from(context).inflate(R.layout.dialog_review, null, false);

        initView();
   //     setCancelable(false);
        // 将动画设置在相应的位置
//        setContentView(dialogView, new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT));

        setContentView(cardView);

        Window window = getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        DisplayMetrics d = context.getResources().getDisplayMetrics();
        lp.width = (int) (d.widthPixels * 0.7); // 宽度设置为屏幕的0.9
        lp.height = LinearLayout.LayoutParams.WRAP_CONTENT;
        window.setAttributes(lp);

        if (!isFirstShow()) {
            show();
        }

    }

    private void initView() {
        // 获取布局文件上的控件
        cardView = (CardView) view.findViewById(R.id.cardView);
        submitBtn = (ImageView) view.findViewById(R.id.iv_submit);
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hide();
                firstShow();
                if (reviewDel != null) {
                    reviewDel.submitClicked();
                }
                if (rank >= 3.5) {
                    goShopReview(context, BuildConfig.APPLICATION_ID, googlePlay);
                }
            }
        });
        scoreView = (ScoreView) view.findViewById(R.id.score);
        scoreView.setStarChangeLister(new ScoreView.OnStarChangeListener() {
            @Override
            public void onStarChange(Float mark) {
                rank = mark;

            }
        });
    }

    public void goShopReview(Context context, String myAppPkg, String shopPkg) {

        if (TextUtils.isEmpty(myAppPkg)) {
            return;
        }

        try {
            Uri uri = Uri.parse("market://details?id=" + myAppPkg);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            if (!TextUtils.isEmpty(shopPkg)) {
                intent.setPackage(shopPkg);
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            // 如果没有该应用商店，则显示系统弹出的应用商店列表供用户选择
            goShopReview(context, myAppPkg, "");
        }
    }

    private boolean isFirstShow() {
        boolean showed = false;
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("review", Context.MODE_PRIVATE);
        String status = sharedPreferences.getString("review", "0");
        if (status.equals("1")) {
            showed = true;
        }
        return showed;
    }

    private void firstShow() {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("review", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("review", "1");
        editor.commit();
    }
}
