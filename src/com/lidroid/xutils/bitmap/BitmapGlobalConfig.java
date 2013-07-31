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

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.DisplayMetrics;
import com.lidroid.xutils.bitmap.callback.ImageLoadCallBack;
import com.lidroid.xutils.bitmap.callback.SimpleImageLoadCallBack;
import com.lidroid.xutils.bitmap.core.BitmapCommonUtils;
import com.lidroid.xutils.bitmap.core.BitmapDownloadProcess;
import com.lidroid.xutils.bitmap.download.Downloader;
import com.lidroid.xutils.bitmap.download.SimpleDownloader;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Author: wyouflf
 * Date: 13-7-31
 * Time: 下午11:15
 */
public class BitmapGlobalConfig {

    private String diskCachePath;
    private int memCacheSize = 1024 * 1024 * 8; // 8MB
    private int diskCacheSize = 1024 * 1024 * 20;  // 20M
    private int originalDiskCacheSize = 1024 * 1024 * 50; // 50M

    private int defaultCompressQuality = 70;
    private Bitmap.CompressFormat defaultCompressFormat = Bitmap.CompressFormat.JPEG;

    private boolean memoryCacheEnabled = true;
    private boolean diskCacheEnabled = true;

    private int poolSize = 3;
    private ExecutorService bitmapLoadExecutor;
    private ImageLoadCallBack imageLoadCallBack;
    private Downloader downloader;
    private BitmapDownloadProcess bitmapDownloadProcess;

    private boolean _dirty_params_bitmapLoadExecutor = true;
    private boolean _dirty_params_bitmapDownloadProcess = true;

    private Context mContext;
    private BitmapDisplayConfig defaultDisplayConfig;
    private BitmapGlobalConfigChangeCallBack mCallBack;

    public BitmapGlobalConfig(Context context, BitmapGlobalConfigChangeCallBack callBack) {
        this.mContext = context;
        this.mCallBack = callBack;
        initDefaultDisplayConfig();
    }

    public void notifyMemoryCacheConfigChanged() {
        if (mCallBack != null) {
            mCallBack.onMemoryCacheConfigChanged(this);
        }
    }

    public void notifyDiskCacheConfigChanged() {
        if (mCallBack != null) {
            mCallBack.onDiskCacheConfigChanged(this);
        }
    }

    private void initDefaultDisplayConfig() {
        defaultDisplayConfig = new BitmapDisplayConfig();
        defaultDisplayConfig.setAnimation(null);
        defaultDisplayConfig.setAnimationType(BitmapDisplayConfig.AnimationType.fadeIn);
        //设置图片的显示最大尺寸（为屏幕的大小,默认为屏幕宽度的1/2）
        DisplayMetrics displayMetrics = mContext.getResources().getDisplayMetrics();
        int defaultWidth = (int) Math.floor(displayMetrics.widthPixels / 2);
        defaultDisplayConfig.setBitmapHeight(defaultWidth);
        defaultDisplayConfig.setBitmapWidth(defaultWidth);
    }

    public String getDiskCachePath() {
        if (diskCachePath == null) {
            diskCachePath = BitmapCommonUtils.getDiskCacheDir(mContext, "xBitmapCache").getAbsolutePath();
        }
        return diskCachePath;
    }

    public void setDiskCachePath(String diskCachePath) {
        _dirty_params_bitmapDownloadProcess = true;
        this.diskCachePath = diskCachePath;
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

    public Downloader getDownloader() {
        if (downloader == null) {
            downloader = new SimpleDownloader();
        }
        return downloader;
    }

    public void setDownloader(Downloader downloader) {
        _dirty_params_bitmapDownloadProcess = true;
        this.downloader = downloader;
    }

    public BitmapDownloadProcess getBitmapDownloadProcess() {
        if (_dirty_params_bitmapDownloadProcess || bitmapDownloadProcess == null) {
            bitmapDownloadProcess = new BitmapDownloadProcess(getDownloader(), getDiskCachePath(), getOriginalDiskCacheSize());
            _dirty_params_bitmapDownloadProcess = false;
        }
        return bitmapDownloadProcess;
    }

    public BitmapDisplayConfig getDefaultDisplayConfig() {
        return defaultDisplayConfig;
    }

    public void setDefaultDisplayConfig(BitmapDisplayConfig defaultDisplayConfig) {
        this.defaultDisplayConfig = defaultDisplayConfig;
    }

    public int getMemCacheSize() {
        return memCacheSize;
    }

    public void setMemCacheSize(int memCacheSize) {
        if (memCacheSize > 1024 * 1024 * 2) {
            this.memCacheSize = memCacheSize;
        } else {
            this.setMemCacheSizePercent(mContext, 0.3f);//设置默认的内存缓存大小
        }
    }

    /**
     * @param context
     * @param percent between 0.05 and 0.8 (inclusive)
     */
    public void setMemCacheSizePercent(Context context, float percent) {
        if (percent < 0.05f || percent > 0.8f) {
            throw new IllegalArgumentException("percent must be between 0.05 and 0.8 (inclusive)");
        }
        this.memCacheSize = Math.round(percent * getMemoryClass(context) * 1024 * 1024);
    }

    public int getDiskCacheSize() {
        return diskCacheSize;
    }

    public void setDiskCacheSize(int diskCacheSize) {
        if (diskCacheSize > 1024 * 1024 * 5) {
            this.diskCacheSize = diskCacheSize;
        }
    }

    public int getPoolSize() {
        return poolSize;
    }

    public void setPoolSize(int poolSize) {
        if (poolSize != this.poolSize) {
            _dirty_params_bitmapLoadExecutor = true;
            this.poolSize = poolSize;
        }
    }

    public int getOriginalDiskCacheSize() {
        return originalDiskCacheSize;
    }

    public void setOriginalDiskCacheSize(int originalDiskCacheSize) {
        _dirty_params_bitmapDownloadProcess = true;
        this.originalDiskCacheSize = originalDiskCacheSize;
    }

    public ExecutorService getBitmapLoadExecutor() {
        if (_dirty_params_bitmapLoadExecutor || bitmapLoadExecutor == null) {
            bitmapLoadExecutor = Executors.newFixedThreadPool(getPoolSize(), new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    Thread t = new Thread(r);
                    t.setPriority(Thread.NORM_PRIORITY - 1);
                    return t;
                }
            });
            _dirty_params_bitmapLoadExecutor = false;
        }
        return bitmapLoadExecutor;
    }

    public int getDefaultCompressQuality() {
        return defaultCompressQuality;
    }

    public void setDefaultCompressQuality(int defaultCompressQuality) {
        this.defaultCompressQuality = defaultCompressQuality;
    }

    public boolean isMemoryCacheEnabled() {
        return memoryCacheEnabled;
    }

    public void setMemoryCacheEnabled(boolean memoryCacheEnabled) {
        this.memoryCacheEnabled = memoryCacheEnabled;
    }

    public boolean isDiskCacheEnabled() {
        return diskCacheEnabled;
    }

    public void setDiskCacheEnabled(boolean diskCacheEnabled) {
        this.diskCacheEnabled = diskCacheEnabled;
    }

    public Bitmap.CompressFormat getDefaultCompressFormat() {
        return defaultCompressFormat;
    }

    public void setDefaultCompressFormat(Bitmap.CompressFormat defaultCompressFormat) {
        this.defaultCompressFormat = defaultCompressFormat;
    }

    private static int getMemoryClass(Context context) {
        return ((ActivityManager) context.getSystemService(
                Context.ACTIVITY_SERVICE)).getMemoryClass();
    }
}