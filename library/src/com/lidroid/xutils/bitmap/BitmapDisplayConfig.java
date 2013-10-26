/*
 * Copyright (c) 2013. wyouflf (wyouflf@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lidroid.xutils.bitmap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.animation.Animation;
import com.lidroid.xutils.bitmap.callback.ImageLoadCallBack;
import com.lidroid.xutils.bitmap.callback.SimpleImageLoadCallBack;
import com.lidroid.xutils.bitmap.core.BitmapCommonUtils;

public class BitmapDisplayConfig {

    private int bitmapMaxWidth = 0;
    private int bitmapMaxHeight = 0;

    private Animation animation;

    private Drawable loadingDrawable;
    private Drawable loadFailedDrawable;

    private ImageLoadCallBack imageLoadCallBack;

    private boolean showOriginal = false;

    private Bitmap.Config bitmapConfig = Bitmap.Config.RGB_565;

    private Context mContext;

    private static final Drawable TRANSPARENT_DRAWABLE = new ColorDrawable(Color.TRANSPARENT);

    public BitmapDisplayConfig(Context context) {
        if (context == null) throw new IllegalArgumentException("context may not be null");
        mContext = context;
    }

    public int getBitmapMaxWidth() {
        if (bitmapMaxWidth == 0) {// default max width = screen_width/3
            bitmapMaxWidth = BitmapCommonUtils.getScreenWidth(mContext) / 3;
        }
        return bitmapMaxWidth;
    }

    public void setBitmapMaxWidth(int bitmapMaxWidth) {
        this.bitmapMaxWidth = bitmapMaxWidth;
    }

    public int getBitmapMaxHeight() {
        if (bitmapMaxHeight == 0) {// default max height = screen_width/3
            bitmapMaxHeight = BitmapCommonUtils.getScreenHeight(mContext) / 3;
        }
        return bitmapMaxHeight;
    }

    public void setBitmapMaxHeight(int bitmapMaxHeight) {
        this.bitmapMaxHeight = bitmapMaxHeight;
    }

    public Animation getAnimation() {
        return animation;
    }

    public void setAnimation(Animation animation) {
        this.animation = animation;
    }

    public Drawable getLoadingDrawable() {
        return loadingDrawable == null ? TRANSPARENT_DRAWABLE : loadingDrawable;
    }

    public void setLoadingDrawable(Drawable loadingDrawable) {
        this.loadingDrawable = loadingDrawable;
    }

    public Drawable getLoadFailedDrawable() {
        return loadFailedDrawable == null ? TRANSPARENT_DRAWABLE : loadFailedDrawable;
    }

    public void setLoadFailedDrawable(Drawable loadFailedDrawable) {
        this.loadFailedDrawable = loadFailedDrawable;
    }

    public ImageLoadCallBack getImageLoadCallBack() {
        if (imageLoadCallBack == null) {
            imageLoadCallBack = new SimpleImageLoadCallBack();
        }
        return imageLoadCallBack;
    }

    public void setImageLoadCallBack(ImageLoadCallBack imageLoadCallBack) {
        this.imageLoadCallBack = imageLoadCallBack;
    }

    public boolean isShowOriginal() {
        return showOriginal;
    }

    public void setShowOriginal(boolean showOriginal) {
        this.showOriginal = showOriginal;
    }

    public Bitmap.Config getBitmapConfig() {
        return bitmapConfig;
    }

    public void setBitmapConfig(Bitmap.Config bitmapConfig) {
        this.bitmapConfig = bitmapConfig;
    }

    @Override
    public String toString() {
        return isShowOriginal() ? "" : "-" + getBitmapMaxWidth() + "-" + getBitmapMaxHeight();
    }
}
