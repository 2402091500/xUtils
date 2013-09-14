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

package com.lidroid.xutils.bitmap.core;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.os.StatFs;
import android.widget.ImageView;
import com.lidroid.xutils.util.LogUtils;

import java.io.File;

public class BitmapCommonUtils {

    /**
     * 获取可以使用的缓存目录
     *
     * @param context
     * @param dirName 目录名称
     * @return
     */
    public static String getDiskCacheDir(Context context, String dirName) {
        final String cachePath = Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) ?
                context.getExternalCacheDir().getPath() : context.getCacheDir().getPath();

        return cachePath + File.separator + dirName;
    }


    /**
     * 获取bitmap的字节大小
     *
     * @param bitmap
     * @return
     */
    public static int getBitmapSize(Bitmap bitmap) {
        return bitmap.getRowBytes() * bitmap.getHeight();
    }

    /**
     * 获取文件路径空间大小
     *
     * @param path
     * @return
     */
    public static long getAvailableSpace(File path) {
        try {
            final StatFs stats = new StatFs(path.getPath());
            return (long) stats.getBlockSize() * (long) stats.getAvailableBlocks();
        } catch (Exception e) {
            LogUtils.e(e.getMessage(), e);
            return -1;
        }

    }

    public static void setBitmap2ImageView(ImageView imageView, Bitmap bitmap) {
        if (imageView == null || bitmap == null) return;

        // recycle old bitmap
        Drawable oldDrawable = imageView.getDrawable();
        if (oldDrawable != null && oldDrawable instanceof BitmapDrawable) {
            Bitmap oldBitmap = ((BitmapDrawable) oldDrawable).getBitmap();
            if (oldBitmap != null && !oldBitmap.equals(bitmap) && !oldBitmap.isRecycled()) {
                oldBitmap.recycle();
                oldBitmap = null;
            }
        }

        imageView.setImageBitmap(bitmap);
    }

}
