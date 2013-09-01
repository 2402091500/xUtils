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
import android.util.DisplayMetrics;
import android.view.animation.Animation;

import com.lidroid.xutils.bitmap.callback.ImageLoadCallBack;
import com.lidroid.xutils.bitmap.callback.SimpleImageLoadCallBack;

public class BitmapDisplayConfig {

    private int bitmapMaxWidth = 0;
    private int bitmapMaxHeight = 0;

    private Animation animation;

    private Bitmap loadingBitmap;
    private Bitmap loadFailedBitmap;

    private ImageLoadCallBack imageLoadCallBack;

    private int compressQuality = 70;

    private Context mContext;

    public BitmapDisplayConfig(Context context) {
        mContext = context;
    }

    public int getBitmapMaxWidth() {
        if (bitmapMaxWidth == 0) {//图片的显示最大尺寸（为屏幕的大小,默认为屏幕宽度的1/2）
            DisplayMetrics displayMetrics = mContext.getResources().getDisplayMetrics();
            bitmapMaxWidth = (int) Math.floor(displayMetrics.widthPixels / 2);
            bitmapMaxHeight = bitmapMaxHeight == 0 ? bitmapMaxWidth : bitmapMaxHeight;
        }
        return bitmapMaxWidth;
    }

    public void setBitmapMaxWidth(int bitmapMaxWidth) {
        this.bitmapMaxWidth = bitmapMaxWidth;
    }

    public int getBitmapMaxHeight() {
        if (bitmapMaxHeight == 0) {//图片的显示最大尺寸（为屏幕的大小,默认为屏幕宽度的1/2）
            DisplayMetrics displayMetrics = mContext.getResources().getDisplayMetrics();
            bitmapMaxHeight = (int) Math.floor(displayMetrics.widthPixels / 2);
            bitmapMaxWidth = bitmapMaxWidth == 0 ? bitmapMaxHeight : bitmapMaxWidth;
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

    public Bitmap getLoadingBitmap() {
        return loadingBitmap;
    }

    public void setLoadingBitmap(Bitmap loadingBitmap) {
        this.loadingBitmap = loadingBitmap;
    }

    public Bitmap getLoadFailedBitmap() {
        return loadFailedBitmap;
    }

    public void setLoadFailedBitmap(Bitmap loadFailedBitmap) {
        this.loadFailedBitmap = loadFailedBitmap;
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

    public int getCompressQuality() {
        return compressQuality;
    }

    public void setCompressQuality(int compressQuality) {
        this.compressQuality = compressQuality;
    }

}
