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
import android.os.AsyncTask;

import com.lidroid.xutils.bitmap.core.BitmapCache;
import com.lidroid.xutils.bitmap.core.BitmapCommonUtils;
import com.lidroid.xutils.bitmap.core.BitmapDownloadProcess;
import com.lidroid.xutils.bitmap.download.Downloader;
import com.lidroid.xutils.bitmap.download.SimpleDownloader;
import com.lidroid.xutils.util.LogUtils;

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
    private int memoryCacheSize = 1024 * 1024 * 8; // 8MB
    private int diskCacheSize = 1024 * 1024 * 20;  // 20M
    private int originalDiskCacheSize = 1024 * 1024 * 50; // 50M

    private Bitmap.CompressFormat defaultCompressFormat = Bitmap.CompressFormat.JPEG;

    private boolean memoryCacheEnabled = true;
    private boolean diskCacheEnabled = true;

    private Downloader downloader;
    private BitmapDownloadProcess bitmapDownloadProcess;
    private BitmapCache bitmapCache;

    private int threadPoolSize = 5;
    private boolean _dirty_params_bitmapLoadExecutor = true;
    private ExecutorService bitmapLoadExecutor;

    private long defaultCacheExpiry = 1000 * 60 * 60 * 24 * 30; // 默认30天过期

    private Context mContext;

    /**
     * @param context
     * @param diskCachePath if null, use default appCacheDir+"/xBitmapCache"
     */
    public BitmapGlobalConfig(Context context, String diskCachePath) {
        this.mContext = context;
        this.diskCachePath = diskCachePath;
        initBitmapCache();
    }

    private void initBitmapCache() {
        new BitmapCacheManagementTask().execute(BitmapCacheManagementTask.MESSAGE_INIT_MEMORY_CACHE);
        new BitmapCacheManagementTask().execute(BitmapCacheManagementTask.MESSAGE_INIT_DISK_CACHE);
    }

    public String getDiskCachePath() {
        if (diskCachePath == null) {
            diskCachePath = BitmapCommonUtils.getDiskCacheDir(mContext, "xBitmapCache").getAbsolutePath();
        }
        return diskCachePath;
    }

    public Downloader getDownloader() {
        if (downloader == null) {
            downloader = new SimpleDownloader();
            downloader.setDefaultExpiry(getDefaultCacheExpiry());
        }
        return downloader;
    }

    public void setDownloader(Downloader downloader) {
        this.downloader = downloader;
        this.downloader.setDefaultExpiry(getDefaultCacheExpiry());
        if (bitmapDownloadProcess != null) {
            bitmapDownloadProcess.setDownloader(downloader);
        }
    }

    public long getDefaultCacheExpiry() {
        return defaultCacheExpiry;
    }

    public void setDefaultCacheExpiry(long defaultCacheExpiry) {
        this.defaultCacheExpiry = defaultCacheExpiry;
    }

    public BitmapDownloadProcess getBitmapDownloadProcess() {
        if (bitmapDownloadProcess == null) {
            bitmapDownloadProcess = new BitmapDownloadProcess(
                    getDownloader(), getDiskCachePath(), getOriginalDiskCacheSize());
        }
        return bitmapDownloadProcess;
    }

    public BitmapCache getBitmapCache() {
        if (bitmapCache == null) {
            bitmapCache = new BitmapCache(this);
        }
        return bitmapCache;
    }

    public int getMemoryCacheSize() {
        return memoryCacheSize;
    }

    public void setMemoryCacheSize(int memoryCacheSize) {
        if (memoryCacheSize > 1024 * 1024 * 2) {
            this.memoryCacheSize = memoryCacheSize;
            if (bitmapCache != null) {
                bitmapCache.setMemoryCacheSize(this.memoryCacheSize);
            }
        } else {
            this.setMemCacheSizePercent(0.3f);//设置默认的内存缓存大小
        }
    }

    /**
     * @param percent between 0.05 and 0.8 (inclusive)
     */
    public void setMemCacheSizePercent(float percent) {
        if (percent < 0.05f || percent > 0.8f) {
            throw new IllegalArgumentException("percent must be between 0.05 and 0.8 (inclusive)");
        }
        this.memoryCacheSize = Math.round(percent * getMemoryClass() * 1024 * 1024);
        if (bitmapCache != null) {
            bitmapCache.setMemoryCacheSize(this.memoryCacheSize);
        }
    }

    public int getDiskCacheSize() {
        return diskCacheSize;
    }

    public void setDiskCacheSize(int diskCacheSize) {
        if (diskCacheSize > 1024 * 1024 * 5) {
            this.diskCacheSize = diskCacheSize;
            if (bitmapCache != null) {
                bitmapCache.setDiskCacheSize(this.diskCacheSize);
            }
        }
    }

    public int getThreadPoolSize() {
        return threadPoolSize;
    }

    public void setThreadPoolSize(int threadPoolSize) {
        if (threadPoolSize != this.threadPoolSize) {
            _dirty_params_bitmapLoadExecutor = true;
            this.threadPoolSize = threadPoolSize;
        }
    }

    public int getOriginalDiskCacheSize() {
        return originalDiskCacheSize;
    }

    public void setOriginalDiskCacheSize(int originalDiskCacheSize) {
        this.originalDiskCacheSize = originalDiskCacheSize;
        if (bitmapDownloadProcess != null) {
            bitmapDownloadProcess.setOriginalDiskCacheSize(originalDiskCacheSize);
        }
    }

    public ExecutorService getBitmapLoadExecutor() {
        if (_dirty_params_bitmapLoadExecutor || bitmapLoadExecutor == null) {
            bitmapLoadExecutor = Executors.newFixedThreadPool(getThreadPoolSize(), new ThreadFactory() {
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

    private int getMemoryClass() {
        return ((ActivityManager) mContext.getSystemService(
                Context.ACTIVITY_SERVICE)).getMemoryClass();
    }

    ////////////////////////////////// bitmap cache management task ///////////////////////////////////////
    private class BitmapCacheManagementTask extends AsyncTask<Object, Void, Void> {
        public static final int MESSAGE_INIT_MEMORY_CACHE = 0;
        public static final int MESSAGE_INIT_DISK_CACHE = 1;
        public static final int MESSAGE_FLUSH = 2;
        public static final int MESSAGE_CLOSE = 3;
        public static final int MESSAGE_CLEAR = 4;
        public static final int MESSAGE_CLEAR_MEMORY = 5;
        public static final int MESSAGE_CLEAR_DISK = 6;
        public static final int MESSAGE_CLEAR_BY_KEY = 7;
        public static final int MESSAGE_CLEAR_MEMORY_BY_KEY = 8;
        public static final int MESSAGE_CLEAR_DISK_BY_KEY = 9;

        @Override
        protected Void doInBackground(Object... params) {
            try {
                switch ((Integer) params[0]) {
                    case MESSAGE_INIT_MEMORY_CACHE:
                        initMemoryCacheInBackground();
                        break;
                    case MESSAGE_INIT_DISK_CACHE:
                        initDiskInBackground();
                        break;
                    case MESSAGE_FLUSH:
                        clearMemoryCacheInBackground();
                        flushCacheInBackground();
                        break;
                    case MESSAGE_CLOSE:
                        clearMemoryCacheInBackground();
                        closeCacheInBackground();
                    case MESSAGE_CLEAR:
                        clearCacheInBackground();
                        break;
                    case MESSAGE_CLEAR_MEMORY:
                        clearMemoryCacheInBackground();
                        break;
                    case MESSAGE_CLEAR_DISK:
                        clearDiskCacheInBackground();
                        break;
                    case MESSAGE_CLEAR_BY_KEY:
                        clearCacheInBackground(String.valueOf(params[1]));
                        break;
                    case MESSAGE_CLEAR_MEMORY_BY_KEY:
                        clearMemoryCacheInBackground(String.valueOf(params[1]));
                        break;
                    case MESSAGE_CLEAR_DISK_BY_KEY:
                        clearDiskCacheInBackground(String.valueOf(params[1]));
                        break;
                    default:
                        break;
                }
            } catch (Exception e) {
                LogUtils.e(e.getMessage(), e);
            }
            return null;
        }

        private void initMemoryCacheInBackground() {
            getBitmapCache().initMemoryCache();
        }

        private void initDiskInBackground() {
            getBitmapCache().initDiskCache();
            getBitmapDownloadProcess().initOriginalDiskCache();
        }

        private void clearCacheInBackground() {
            getBitmapCache().clearCache();
            getBitmapDownloadProcess().clearOriginalDiskCache();
        }

        private void clearMemoryCacheInBackground() {
            getBitmapCache().clearMemoryCache();
        }

        private void clearDiskCacheInBackground() {
            getBitmapCache().clearDiskCache();
            getBitmapDownloadProcess().clearOriginalDiskCache();
        }

        private void clearCacheInBackground(String key) {
            getBitmapCache().clearCache(key);
            getBitmapDownloadProcess().clearOriginalDiskCache(key);
        }

        private void clearDiskCacheInBackground(String key) {
            getBitmapCache().clearDiskCache(key);
            getBitmapDownloadProcess().clearOriginalDiskCache(key);
        }

        private void clearMemoryCacheInBackground(String key) {
            getBitmapCache().clearMemoryCache(key);
        }

        private void flushCacheInBackground() {
            getBitmapCache().flush();
            getBitmapDownloadProcess().flushOriginalDiskCache();
        }

        private void closeCacheInBackground() {
            getBitmapCache().close();
            getBitmapDownloadProcess().closeOriginalDiskCache();
        }
    }

    public void clearCache() {
        new BitmapCacheManagementTask().execute(BitmapCacheManagementTask.MESSAGE_CLEAR);
    }

    public void clearCache(String url) {
        new BitmapCacheManagementTask().execute(BitmapCacheManagementTask.MESSAGE_CLEAR_BY_KEY, url);
    }

    public void clearMemoryCache() {
        new BitmapCacheManagementTask().execute(BitmapCacheManagementTask.MESSAGE_CLEAR_MEMORY);
    }

    public void clearMemoryCache(String url) {
        new BitmapCacheManagementTask().execute(BitmapCacheManagementTask.MESSAGE_CLEAR_MEMORY_BY_KEY, url);
    }

    public void clearDiskCache() {
        new BitmapCacheManagementTask().execute(BitmapCacheManagementTask.MESSAGE_CLEAR_DISK);
    }

    public void clearDiskCache(String url) {
        new BitmapCacheManagementTask().execute(BitmapCacheManagementTask.MESSAGE_CLEAR_DISK_BY_KEY, url);
    }

    public void flushCache() {
        new BitmapCacheManagementTask().execute(BitmapCacheManagementTask.MESSAGE_FLUSH);
    }

    public void closeCache() {
        new BitmapCacheManagementTask().execute(BitmapCacheManagementTask.MESSAGE_CLOSE);
    }
}