package com.magicFilter.magic_filters.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.magicFilter.magic_filters.BuildConfig;
import com.magicFilter.magic_filters.R;
import com.magicFilter.magic_filters.adapter.FilterAdapter;
import com.magicFilter.magic_filters.bean.FilterBean;
import com.magicFilter.magic_filters.filter.BigBrotherFilter;
import com.magicFilter.magic_filters.filter.BrickFilter;
import com.magicFilter.magic_filters.filter.BrightContrastFilter;
import com.magicFilter.magic_filters.filter.EdgeFilter;
import com.magicFilter.magic_filters.filter.FeatherFilter;
import com.magicFilter.magic_filters.model.IImageFilter;
import com.magicFilter.magic_filters.filter.Image;
import com.magicFilter.magic_filters.filter.NoiseFilter;
import com.magicFilter.magic_filters.filter.RainBowFilter;
import com.magicFilter.magic_filters.filter.RectMatrixFilter;
import com.magicFilter.magic_filters.filter.RippleFilter;
import com.magicFilter.magic_filters.filter.WaveFilter;
import com.magicFilter.magic_filters.helper.TouchHelper;
import com.magicFilter.magic_filters.model.RecyclerViewItemClick;
import com.magicFilter.magic_filters.utils.ImageUtils;
import com.magicFilter.magic_filters.utils.SPUtils;
import com.magicFilter.magic_filters.widget.LoadingDialog;
import com.magicFilter.magic_filters.widget.ReviewTool;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ShowActivity extends BaseActivity implements View.OnClickListener {

    private List<IImageFilter> imageFilters;
    private List<FilterBean> adapterDataList;

    private Handler mHandler = new Handler(Objects.requireNonNull(Looper.myLooper()));
 //   private ProgressBar pbPic;
    private ImageView ivPhoto;
    private RecyclerView recyclerView;
    private FilterAdapter adapter;
    private LoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show);

        initView();
        initFilterMap();
        initAdapterData();
        initData();



    }

    @Override
    public void onBackPressed() {

//        Dialog dialog = new Dialog(this);
//        dialog.setContentView(R.layout.exit_dialog);
//        dialog.setCancelable(true);
//        dialog.setTitle("No internet");
//        dialog.show();
//        Window window = this.getWindow();
//        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        new AlertDialog.Builder(this).setTitle("Exit?")
                .setIcon(R.drawable.dialog_image)
                .setMessage("Are you sure you want to exit?"+"\n"+"Your current changes will be discarded")
                .setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ShowActivity.super.onBackPressed();
                    }
                })
                .setNegativeButton("No",null)
                .show();

    }

    private void initView() {
        ImageView ivBottomRound = findViewById(R.id.iv_bottom_round);
        ImageView ivSave = findViewById(R.id.iv_save);
        ImageView ivClose = findViewById(R.id.iv_close);

        ivPhoto = findViewById(R.id.iv_photo_show);
       // pbPic = findViewById(R.id.pb_pic);
        recyclerView = findViewById(R.id.rv_menu_fun);
      //  pbPic.setVisibility(View.GONE);
        ivSave.setOnClickListener(this);
        ivClose.setOnClickListener(this);
        TouchHelper.touchAlpha(ivSave, ivClose);

       /* ivBottomRound.post(new Runnable() {
            @Override
            public void run() {
                int padding = DisplayUtils.dip2px(ShowActivity.this , 10);
                ivPhoto.setPadding(padding, padding, padding, 0);
            }
        });*/
//        loadingDialog = new LoadingDialog(this);

    }

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            if (msg.what == 1) {
                loadingDialog.closeDialog();
                adapter.setFilterLock((boolean) msg.obj);

                if (SPUtils.getBoolean(ShowActivity.this, "per") && SPUtils.getBoolean(ShowActivity.this, "dpus")) {
                    new ReviewTool(ShowActivity.this, null);
                    SPUtils.putBoolean(ShowActivity.this, "per", true);
                }
            }
            return false;
        }
    });

    private void initData() {
        String path = getIntent().getStringExtra("imagePath");
        final Bitmap mOriginBt = BitmapFactory.decodeFile(path);
        ivPhoto.setImageBitmap(mOriginBt);
        /*Glide.with(this)
                .load(path)
                .into(ivPhoto);*/
       // final Bitmap mOriginBt = BitmapFactory.decodeFile(path);

        adapter = new FilterAdapter(this, adapterDataList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.HORIZONTAL);
        /*CircleLayoutManager layoutManager = new CircleLayoutManager(this);
        layoutManager.setGravity(CircleLayoutManager.TOP);
        layoutManager.setRadius(1400);
        layoutManager.setAngleInterval(12);
        layoutManager.setDistanceToBottom(-50);*/

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        adapter.setRecyclerViewItemClick(new RecyclerViewItemClick() {
            @Override
            public void onClick(View view, final int position) {
              //  pbPic.setVisibility(View.VISIBLE);
                loadingDialog = new LoadingDialog(ShowActivity.this);
                adapter.singleChoose(position);
                new Thread() {
                    @Override
                    public void run() {
                        final Bitmap composedBmp = makeFilter(mOriginBt, imageFilters.get(position));
                        mHandler.removeCallbacksAndMessages(null);
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                ivPhoto.setImageBitmap(composedBmp);
                                ivPhoto.setTag(imageFilters.get(position).getClass().getSimpleName().toLowerCase());
                           //     pbPic.setVisibility(View.GONE);
                                loadingDialog.closeDialog();
                            }
                        });
                    }
                }.start();
            }
        });
    }

    private void initFilterMap() {
        imageFilters = new ArrayList<>();
        imageFilters.add(new BigBrotherFilter());
        imageFilters.add(new BrickFilter());
        imageFilters.add(new BrightContrastFilter(0.15f, 0.0f));
        imageFilters.add(new EdgeFilter());
        imageFilters.add(new FeatherFilter());
        imageFilters.add(new NoiseFilter());
        imageFilters.add(new RainBowFilter());
        imageFilters.add(new RectMatrixFilter());
        imageFilters.add(new RippleFilter(38, 15, true));
        imageFilters.add(new WaveFilter(25, 10));
    }

    private void initAdapterData() {
        adapterDataList = new ArrayList<>();
        adapterDataList.add(new FilterBean(R.drawable.bigbroteherfilter, "BigBrotherFilter"));
        adapterDataList.add(new FilterBean(R.drawable.brickfilter, "BrickFilter"));
        adapterDataList.add(new FilterBean(R.drawable.brightcontrastfilter, "BrightContrastFilter"));
        adapterDataList.add(new FilterBean(R.drawable.edgefilter, "EdgeFilter"));
        adapterDataList.add(new FilterBean(R.drawable.featherfilter, "FeatherFilter"));
        adapterDataList.add(new FilterBean(R.drawable.noisefilter, "NoiseFilter"));
        adapterDataList.add(new FilterBean(R.drawable.rainbowfilter, "RainbowFilter"));
        adapterDataList.add(new FilterBean(R.drawable.rectmatrixfilter, "RectMatrixFilter"));
        adapterDataList.add(new FilterBean(R.drawable.ripplefilter, "RippleFilter"));
        adapterDataList.add(new FilterBean(R.drawable.wave, "WaveFilter"));
    }

    private void saveFilterPic() {
        if (ivPhoto.getTag() == null) {
            Toast.makeText(getApplicationContext(), "no filter applied!", Toast.LENGTH_SHORT).show();
        } else {
            Bitmap save = ((BitmapDrawable) ivPhoto.getDrawable()).getBitmap();
            String filterName = ivPhoto.getTag() + "-" + System.currentTimeMillis();
            ImageUtils.saveBitmapImage(getApplicationContext(), save, filterName);
            Toast.makeText(getApplicationContext(), "saved success: /sdcard/Pictures/MagicAlbum/" + filterName, Toast.LENGTH_SHORT).show();
        }
    }

    private Bitmap makeFilter(Bitmap bt, IImageFilter filter) {
        try {
            Image img = new Image(bt);
            if (filter != null) {
                img = filter.process(img);
                img.copyPixelsFromBuffer();
            }
            return img.getImage();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_save:
                saveFilterPic();
                break;
            case R.id.iv_close:
                finish();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 88 && data != null) {
            if (data.getBooleanExtra("isUnc", false)) {
                adapter.setFilterLock(false);
            }
        }
    }

    public void share(View view) {
        Drawable drawable = ivPhoto.getDrawable();
        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
        try {
            File file = new File(getApplicationContext().getExternalCacheDir(), File.separator + "BeautifyWallpapers.jpg");
            FileOutputStream fOut = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
            fOut.flush();
            fOut.close();
            file.setReadable(true, false);
            final Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Uri photoUri = FileProvider.getUriForFile(getApplicationContext(), BuildConfig.APPLICATION_ID + ".provider", file);

            intent.putExtra(Intent.EXTRA_STREAM, photoUri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setType("image/jpg");
            startActivity(Intent.createChooser(intent, "Share image Via..."));


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }}