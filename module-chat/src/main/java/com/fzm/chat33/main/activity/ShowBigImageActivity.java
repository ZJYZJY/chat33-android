package com.fzm.chat33.main.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomViewTarget;
import com.bumptech.glide.request.transition.Transition;
import com.fuzamei.common.utils.BarUtils;
import com.fuzamei.common.utils.ImageUtils;
import com.fuzamei.common.utils.QRCodeUtil;
import com.fuzamei.common.utils.ScreenUtils;
import com.fuzamei.common.utils.ShowUtils;
import com.fuzamei.common.utils.VibrateUtils;
import com.fuzamei.common.widget.BottomPopupWindow;
import com.fuzamei.common.widget.LoadingDialog;
import com.fuzamei.common.widget.photoview.PhotoView;
import com.fuzamei.componentservice.app.AppRoute;
import com.fuzamei.componentservice.base.LoadableActivity;
import com.fzm.chat33.R;
import com.fzm.chat33.core.bean.param.DecryptParams;
import com.fzm.chat33.core.db.bean.ChatFile;
import com.fzm.chat33.core.db.bean.ChatMessage;
import com.fzm.chat33.core.manager.CipherManager;
import com.fzm.chat33.hepler.QRCodeHelper;
import com.fzm.chat33.hepler.glide.GlideApp;
import com.fzm.chat33.hepler.glide.SingleKeyEncrypt;
import com.google.zxing.Result;

import java.util.ArrayList;
import java.util.List;

@Route(path = AppRoute.BIG_IMAGE)
public class ShowBigImageActivity extends LoadableActivity {

    private TextView tv_indicator;
    private ViewPager vp_big_image;
    //自定义加载框
    private LoadingDialog dialog;

    private BigImageAdapter adapter;
    public int currentIndex = 0;
    public ArrayList<ChatMessage> imageList;
    public ArrayList<String> imageStrList;
    public int size = 0;
    private String formatUrl;
    private List<String> options = new ArrayList<>();
    private Result codeResult;
    public static final int SAVE_IMAGE_PERMISSION = 0;
    private final int FIND_TYPE_QR_CODE = 2;

    private String picSave;
    private String picForward;
    private String picScan;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_show_big_image;
    }

    @Override
    protected void initView() {
        vp_big_image = findViewById(R.id.vp_big_image);
        tv_indicator = findViewById(R.id.tv_indicator);

        picSave = getString(R.string.chat_action_large_pic_save);
        picForward = getString(R.string.chat_action_large_pic_forward);
        picScan = getString(R.string.chat_action_large_pic_qr_scan);
    }

    @Override
    protected void initData() {
        formatUrl = "?x-oss-process=image/resize,h_" + ScreenUtils.dp2px(this, 150) + "/quality,q_70/format,jpg/interlace,1";
        options.add(picSave);
        options.add(picForward);
        imageList = (ArrayList<ChatMessage>) getIntent().getSerializableExtra("image_list");
        imageStrList = (ArrayList<String>) getIntent().getSerializableExtra("images");
        if (imageList != null) {
            size = imageList.size();
        } else if (imageStrList != null) {
            size = imageStrList.size();
        }
        currentIndex = getIntent().getIntExtra("currentIndex", 0);

        if (size == 1) {
            tv_indicator.setVisibility(View.GONE);
        } else {
            tv_indicator.setVisibility(View.VISIBLE);
            tv_indicator.setText("1/" + size);
        }
    }

    @Override
    protected void setEvent() {
        adapter = new BigImageAdapter(this, imageList, imageStrList);
        vp_big_image.setOffscreenPageLimit(3);
        vp_big_image.setAdapter(adapter);
        vp_big_image.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                codeResult = null;
                options.clear();
                options.add(picSave);
                options.add(picForward);
                tv_indicator.setText((position + 1) + "/" + size);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        vp_big_image.setCurrentItem(currentIndex);
    }

    @Override
    protected void setStatusBar() {
        BarUtils.setStatusBarColor(this, ContextCompat.getColor(this, android.R.color.black), 0);
    }

    class BigImageAdapter extends PagerAdapter {

        private Context context;
        private List<ChatMessage> messageImages;
        private List<String> imageStrs;

        public BigImageAdapter(Context context, List<ChatMessage> messageImages, List<String> imageStrs) {
            this.context = context;
            this.messageImages = messageImages;
            this.imageStrs = imageStrs;
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position) {
            View convertView = LayoutInflater.from(context).inflate(R.layout.item_big_image, null);
            final PhotoView photoView = convertView.findViewById(R.id.pv_big_image);
            photoView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });

            RequestOptions options = new RequestOptions()
                    .centerInside()
                    .diskCacheStrategy(DiskCacheStrategy.DATA);
            if (messageImages != null) {
                if (!TextUtils.isEmpty(messageImages.get(position).msg.getLocalPath())) {
                    if (messageImages.get(position).msg.getLocalPath().endsWith("gif")) {
                        displayGif(photoView, position, options);
                    } else {
                        displayImage(photoView, position, options);
                    }
                }
            } else if (imageStrs != null) {
                if (imageStrs.get(position).endsWith("gif")) {
                    displayGif(photoView, position, options);
                } else {
                    displayImage(photoView, position, options);
                }
            }

            container.addView(convertView);
            return convertView;
        }

        private void displayGif(PhotoView photoView, int position, RequestOptions options) {
            if (messageImages != null) {
                GlideApp.with(context).asGif().load(messageImages.get(position)).apply(options).into(photoView);
            } else if (imageStrs != null) {
                GlideApp.with(context).asGif()
                        .load(new SingleKeyEncrypt(imageStrs.get(position), CipherManager.getPublicKey()))
                        .apply(options)
                        .into(photoView);
            }
        }

        private void displayImage(PhotoView photoView, int position, RequestOptions options) {
            CustomViewTarget<PhotoView, Bitmap> simpleTarget = new CustomViewTarget<PhotoView, Bitmap>(photoView) {
                @Override
                protected void onResourceLoading(@Nullable Drawable placeholder) {
//                    photoView.setImageDrawable(new ColorDrawable(Color.parseColor("#6C6C6C")));
                }

                @Override
                public void onLoadFailed(@Nullable Drawable errorDrawable) {

                }

                @Override
                public void onResourceReady(@NonNull Bitmap bitmap, @Nullable Transition<? super Bitmap> transition) {
                    photoView.setImageBitmap(bitmap);
                    setOnLongClickListener(photoView, bitmap, position);
                }

                @Override
                protected void onResourceCleared(@Nullable Drawable placeholder) {

                }
            };

            if (messageImages != null) {
                GlideApp.with(context).asBitmap().apply(options).load(messageImages.get(position)).into(simpleTarget);
            } else if (imageStrs != null) {
                GlideApp.with(context).asBitmap()
                        .load(new SingleKeyEncrypt(imageStrs.get(position), CipherManager.getPublicKey()))
                        .apply(options)
                        .into(photoView);
            }
        }

        private void setOnLongClickListener(final ImageView photoView, final Bitmap bitmap, final int pos) {
            photoView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    VibrateUtils.simple(instance, 50);
                    BottomPopupWindow popupWindow = new BottomPopupWindow(instance, options, new BottomPopupWindow.OnItemClickListener() {
                        @Override
                        public void onItemClick(View view, PopupWindow popupWindow, int position) {
                            popupWindow.dismiss();
                            if (position == 0) {
                                save(photoView, pos);
                            } else if (position == 1) {
                                ChatFile chatFile = ChatFile.newImage(imageList.get(pos).msg.getImageUrl(), "", bitmap.getHeight(), bitmap.getWidth());
                                ARouter.getInstance().build(AppRoute.CONTACT_SELECT)
                                        .withSerializable("params", new DecryptParams(imageList.get(pos)))
                                        .withSerializable("chatFile", chatFile)
                                        .navigation();
                            } else {
                                QRCodeHelper.process(ShowBigImageActivity.this, codeResult.getText());
                            }
                        }
                    });
                    popupWindow.showAtLocation(photoView, Gravity.BOTTOM, 0, 0);
                    if (codeResult == null) {
                        codeResult = QRCodeUtil.decodeFromPicture(bitmap);
                        if (codeResult != null) {
                            if (!options.contains(picScan)) {
                                options.add(picScan);
                                popupWindow.notifyItemInserted(popupWindow.optionSize() - 1);
                            }
                        }
                    } else {
                        if (!options.contains(picScan)) {
                            options.add(picScan);
                            popupWindow.notifyItemInserted(popupWindow.optionSize() - 1);
                        }
                    }
                    return false;
                }
            });
        }

        private void save(ImageView photoView, int position) {
            if (!isGrantExternalRW(SAVE_IMAGE_PERMISSION, instance)) {
                tempImg = photoView;
                tempPos = position;
                return;
            }
            if (photoView.getDrawable() != null) {
                if (!TextUtils.isEmpty(messageImages.get(position).msg.getLocalPath())
                        || !TextUtils.isEmpty(imageStrs.get(position))) {
                    loading(true);
                    ImageUtils.saveImageToGallery(photoView.getDrawable());
                    dismiss();
                    ShowUtils.showToastNormal(instance, getString(R.string.chat_tips_img_saved));
                }
            }
        }

        @Override
        public int getCount() {
            return messageImages == null ? (imageStrs == null ? 0 : imageStrs.size()) : messageImages.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }

    private boolean isGrantExternalRW(int requestCode, Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && activity.checkSelfPermission(
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(activity, new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, requestCode);

            return false;
        }
        return true;
    }

    private ImageView tempImg;
    private int tempPos;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SAVE_IMAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                adapter.save(tempImg, tempPos);
                tempImg = null;
                tempPos = 0;
            } else {
                ShowUtils.showToast(this, getString(R.string.chat_permission_storage));
            }
        }
    }
}
