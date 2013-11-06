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
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.os.StatFs;
import android.util.DisplayMetrics;
import android.widget.ImageView;
import com.lidroid.xutils.bitmap.callback.BitmapSetter;
import com.lidroid.xutils.util.LogUtils;

import java.io.File;

public class BitmapCommonUtils {

    /**
     * @param context
     * @param dirName Only the folder name, not contain full path.
     * @return app_cache_path/dirName
     */
    public static String getDiskCacheDir(Context context, String dirName) {
        final String cachePath = Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) ?
                context.getExternalCacheDir().getPath() : context.getCacheDir().getPath();

        return cachePath + File.separator + dirName;
    }

    public static long getAvailableSpace(File dir) {
        try {
            final StatFs stats = new StatFs(dir.getPath());
            return (long) stats.getBlockSize() * (long) stats.getAvailableBlocks();
        } catch (Throwable e) {
            LogUtils.e(e.getMessage(), e);
            return -1;
        }

    }

    private static int screenWidth = 0;
    private static int screenHeight = 0;

    public static int getScreenWidth(Context context) {
        if (screenWidth == 0) {
            DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
            screenWidth = displayMetrics.widthPixels;
            screenHeight = displayMetrics.heightPixels;
        }
        return screenWidth;
    }

    public static int getScreenHeight(Context context) {
        if (screenHeight == 0) {
            DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
            screenWidth = displayMetrics.widthPixels;
            screenHeight = displayMetrics.heightPixels;
        }
        return screenHeight;
    }

    public static final ImageViewSetter sDefaultImageViewSetter = new ImageViewSetter();

    public static class ImageViewSetter implements BitmapSetter<ImageView> {

        @Override
        public void setBitmap(ImageView container, Bitmap bitmap) {
            container.setImageBitmap(bitmap);
        }

        @Override
        public void setDrawable(ImageView container, Drawable drawable) {
            container.setImageDrawable(drawable);
        }

        @Override
        public Drawable getDrawable(ImageView container) {
            return container.getDrawable();
        }
    }
}
