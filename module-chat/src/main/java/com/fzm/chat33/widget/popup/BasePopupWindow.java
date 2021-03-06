package com.fzm.chat33.widget.popup;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.PopupWindow;

import java.lang.reflect.Field;

/**
 * Created by wdl on 2018/5/18.
 */

public class BasePopupWindow extends PopupWindow implements View.OnClickListener {
    protected Context context;
    protected View mRootView;

    public BasePopupWindow(Context context, View popupView){
        super(popupView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, true);
        this.context = context;
        mRootView = popupView;
        setOutsideTouchable(true);
        //setClippingEnabled(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                Field mLayoutInScreen = PopupWindow.class.getDeclaredField("mLayoutInScreen");
                mLayoutInScreen.setAccessible(true);
                mLayoutInScreen.set(this, true);
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        setBackgroundDrawable(new BitmapDrawable());
        setFocusable(true);
        setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
    }

    @Override
    public void onClick(View view) {

    }

    @Override
    public void showAsDropDown(View anchor) {
        if (Build.VERSION.SDK_INT < 24) {
            super.showAsDropDown(anchor);
        } else {
            Rect rect = new Rect();
            anchor.getGlobalVisibleRect(rect);
            int h = anchor.getResources().getDisplayMetrics().heightPixels - rect.bottom;
            setHeight(h);
            super.showAsDropDown(anchor);
        }
    }
}
