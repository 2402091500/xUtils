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
package com.lidroid.xutils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.widget.ImageView;
import com.lidroid.xutils.bitmap.callback.ImageLoadCallBack;
import com.lidroid.xutils.bitmap.callback.SimpleImageLoadCallBack;
import com.lidroid.xutils.bitmap.core.BitmapCache;
import com.lidroid.xutils.bitmap.core.BitmapCommonUtils;
import com.lidroid.xutils.bitmap.core.BitmapDisplayConfig;
import com.lidroid.xutils.bitmap.core.BitmapDownloadProcess;
import com.lidroid.xutils.bitmap.download.Downloader;
import com.lidroid.xutils.bitmap.download.SimpleDownloader;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class BitmapUtils {

    private BitmapGlobalConfig mGlobalConfig;
    private static BitmapCache mImageCache;

    private boolean mExitTasksEarly = false;
    private boolean mPauseWork = false;
    private final Object mPauseWorkLock = new Object();
    protected Context mContext;

    private static ExecutorService bitmapLoadExecutor;

    private static BitmapUtils instance;

    /////////////////////////////////////////////// create ///////////////////////////////////////////////////
    private BitmapUtils(Context context) {
        mContext = context;
        mGlobalConfig = new BitmapGlobalConfig();
    }

    /**
     * must call it when created an instance or change mGlobalConfig
     *
     * @return
     */
    private BitmapUtils refreshGlobalConfig() {
        mGlobalConfig.refreshConfig();
        new BitmapCacheTask().execute(BitmapCacheTask.MESSAGE_INIT_DISK_CACHE);
        return this;
    }

    public static BitmapUtils create(Context ctx) {
        if (instance == null) {
            instance = new BitmapUtils(ctx.getApplicationContext());
            instance.refreshGlobalConfig();
        }
        return instance;
    }

    public static BitmapUtils create(Context ctx, String diskCachePath) {
        if (instance == null) {
            instance = new BitmapUtils(ctx.getApplicationContext());
            instance.configDiskCachePath(diskCachePath);
            instance.refreshGlobalConfig();
        }
        return instance;

    }

    public static BitmapUtils create(Context ctx, String diskCachePath, float memoryCacheSizePercent) {
        if (instance == null) {
            instance = new BitmapUtils(ctx.getApplicationContext());
            instance.configDiskCachePath(diskCachePath);
            instance.configMemoryCachePercent(memoryCacheSizePercent);
            instance.refreshGlobalConfig();
        }

        return instance;
    }

    public static BitmapUtils create(Context ctx, String diskCachePath, int memoryCacheSize) {
        if (instance == null) {
            instance = new BitmapUtils(ctx.getApplicationContext());
            instance.configDiskCachePath(diskCachePath);
            instance.configMemoryCacheSize(memoryCacheSize);
            instance.refreshGlobalConfig();
        }

        return instance;
    }

    public static BitmapUtils create(Context ctx, String diskCachePath, float memoryCacheSizePercent, int threadSize) {
        if (instance == null) {
            instance = new BitmapUtils(ctx.getApplicationContext());
            instance.configDiskCachePath(diskCachePath);
            instance.configBitmapLoadThreadSize(threadSize);
            instance.configMemoryCachePercent(memoryCacheSizePercent);
            instance.refreshGlobalConfig();
        }

        return instance;
    }

    public static BitmapUtils create(Context ctx, String diskCachePath, int memoryCacheSize, int threadSize) {
        if (instance == null) {
            instance = new BitmapUtils(ctx.getApplicationContext());
            instance.configDiskCachePath(diskCachePath);
            instance.configBitmapLoadThreadSize(threadSize);
            instance.configMemoryCacheSize(memoryCacheSize);
            instance.refreshGlobalConfig();
        }

        return instance;
    }

    public static BitmapUtils create(Context ctx, String diskCachePath, float memoryCacheSizePercent, int diskCacheSize, int threadSize) {
        if (instance == null) {
            instance = new BitmapUtils(ctx.getApplicationContext());
            instance.configDiskCachePath(diskCachePath);
            instance.configBitmapLoadThreadSize(threadSize);
            instance.configMemoryCachePercent(memoryCacheSizePercent);
            instance.configDiskCacheSize(diskCacheSize);
            instance.refreshGlobalConfig();
        }

        return instance;
    }

    public static BitmapUtils create(Context ctx, String diskCachePath, int memoryCacheSize, int diskCacheSize, int threadSize) {
        if (instance == null) {
            instance = new BitmapUtils(ctx.getApplicationContext());
            instance.configDiskCachePath(diskCachePath);
            instance.configBitmapLoadThreadSize(threadSize);
            instance.configMemoryCacheSize(memoryCacheSize);
            instance.configDiskCacheSize(diskCacheSize);
            instance.refreshGlobalConfig();
        }

        return instance;
    }


    //////////////////////////////////////// config ////////////////////////////////////////////////////////////////////

    public BitmapUtils configLoadingImage(Bitmap bitmap) {
        mGlobalConfig.defaultDisplayConfig.setLoadingBitmap(bitmap);
        return this;
    }

    public BitmapUtils configLoadingImage(int resId) {
        mGlobalConfig.defaultDisplayConfig.setLoadingBitmap(BitmapFactory.decodeResource(mContext.getResources(), resId));
        return this;
    }

    public BitmapUtils configLoadFailedImage(Bitmap bitmap) {
        mGlobalConfig.defaultDisplayConfig.setLoadFailedBitmap(bitmap);
        return this;
    }

    public BitmapUtils configLoadFailedImage(int resId) {
        mGlobalConfig.defaultDisplayConfig.setLoadFailedBitmap(BitmapFactory.decodeResource(mContext.getResources(), resId));
        return this;
    }

    public BitmapUtils configBitmapMaxHeight(int bitmapHeight) {
        mGlobalConfig.defaultDisplayConfig.setBitmapHeight(bitmapHeight);
        return this;
    }

    public BitmapUtils configBitmapMaxWidth(int bitmapWidth) {
        mGlobalConfig.defaultDisplayConfig.setBitmapWidth(bitmapWidth);
        return this;
    }

    public BitmapUtils configDownloader(Downloader downloader) {
        mGlobalConfig.downloader = downloader;
        return this;
    }

    public BitmapUtils configImageLoadCallBack(ImageLoadCallBack imageLoadCallBack) {
        mGlobalConfig.imageLoadCallBack = imageLoadCallBack;
        return this;
    }

    public BitmapUtils configCompressFormat(CompressFormat format) {
        mImageCache.setCompressFormat(format);
        return this;
    }

    public BitmapUtils configCalculateBitmapSizeWhenDecode(boolean neverCalculate) {
        if (mGlobalConfig != null && mGlobalConfig.bitmapProcess != null)
            mGlobalConfig.bitmapProcess.configCalculateBitmap(neverCalculate);
        return this;
    }

    ////////////////////////// display ////////////////////////////////////

    private HashMap<String, BitmapDisplayConfig> displayConfigMap = new HashMap<String, BitmapDisplayConfig>();


    public void display(ImageView imageView, String uri) {
        doDisplay(imageView, uri, null);
    }

    public void display(ImageView imageView, String uri, int imageWidth, int imageHeight) {
        BitmapDisplayConfig displayConfig = displayConfigMap.get(imageWidth + "_" + imageHeight);
        if (displayConfig == null) {
            displayConfig = getDisplayConfig();
            displayConfig.setBitmapHeight(imageHeight);
            displayConfig.setBitmapWidth(imageWidth);
            displayConfigMap.put(imageWidth + "_" + imageHeight, displayConfig);
        }

        doDisplay(imageView, uri, displayConfig);
    }

    public void display(ImageView imageView, String uri, Bitmap loadingBitmap) {
        BitmapDisplayConfig displayConfig = displayConfigMap.get(String.valueOf(loadingBitmap));
        if (displayConfig == null) {
            displayConfig = getDisplayConfig();
            displayConfig.setLoadingBitmap(loadingBitmap);
            displayConfigMap.put(String.valueOf(loadingBitmap), displayConfig);
        }

        doDisplay(imageView, uri, displayConfig);
    }

    public void display(ImageView imageView, String uri, Bitmap loadingBitmap, Bitmap loadFailedBitmap) {
        BitmapDisplayConfig displayConfig = displayConfigMap.get(String.valueOf(loadingBitmap) + "_" + String.valueOf(loadFailedBitmap));
        if (displayConfig == null) {
            displayConfig = getDisplayConfig();
            displayConfig.setLoadingBitmap(loadingBitmap);
            displayConfig.setLoadFailedBitmap(loadFailedBitmap);
            displayConfigMap.put(String.valueOf(loadingBitmap) + "_" + String.valueOf(loadFailedBitmap), displayConfig);
        }

        doDisplay(imageView, uri, displayConfig);
    }

    public void display(ImageView imageView, String uri, int imageWidth, int imageHeight, Bitmap loadingBitmap, Bitmap loadFailedBitmap) {
        BitmapDisplayConfig displayConfig = displayConfigMap.get(imageWidth + "_" + imageHeight + "_" + String.valueOf(loadingBitmap) + "_" + String.valueOf(loadFailedBitmap));
        if (displayConfig == null) {
            displayConfig = getDisplayConfig();
            displayConfig.setBitmapHeight(imageHeight);
            displayConfig.setBitmapWidth(imageWidth);
            displayConfig.setLoadingBitmap(loadingBitmap);
            displayConfig.setLoadFailedBitmap(loadFailedBitmap);
            displayConfigMap.put(imageWidth + "_" + imageHeight + "_" + String.valueOf(loadingBitmap) + "_" + String.valueOf(loadFailedBitmap), displayConfig);
        }

        doDisplay(imageView, uri, displayConfig);
    }

    public void display(ImageView imageView, String uri, BitmapDisplayConfig displayConfig) {
        doDisplay(imageView, uri, displayConfig);
    }


    private void doDisplay(ImageView imageView, String uri, BitmapDisplayConfig displayConfig) {
        if (TextUtils.isEmpty(uri) || imageView == null) {
            return;
        }

        if (displayConfig == null) {
            displayConfig = mGlobalConfig.defaultDisplayConfig;
        }

        Bitmap bitmap = null;

        if (mImageCache != null) {
            bitmap = mImageCache.getBitmapFromMemCache(uri);
        }

        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);

        } else if (!bitmapLoadTaskExist(uri, imageView)) {

            final BitmapLoadTask task = new BitmapLoadTask(imageView, displayConfig);
            // set loading image
            final AsyncBitmapDrawable asyncBitmapDrawable = new AsyncBitmapDrawable(
                    mContext.getResources(),
                    displayConfig.getLoadingBitmap(),
                    task);
            imageView.setImageDrawable(asyncBitmapDrawable);

            // load bitmap from uri or diskCache
            task.executeOnExecutor(bitmapLoadExecutor, uri);
        }
    }


    /////////////////////////////////////////////// cache /////////////////////////////////////////////////////////////////

    public void clearCache() {
        new BitmapCacheTask().execute(BitmapCacheTask.MESSAGE_CLEAR);
    }

    public void clearCache(String key) {
        new BitmapCacheTask().execute(BitmapCacheTask.MESSAGE_CLEAR_KEY, key);
    }

    public void clearMemoryCache() {
        new BitmapCacheTask().execute(BitmapCacheTask.MESSAGE_CLEAR_MEMORY);
    }

    public void clearMemoryCache(String key) {
        new BitmapCacheTask().execute(BitmapCacheTask.MESSAGE_CLEAR_KEY_IN_MEMORY, key);
    }

    public void clearDiskCache() {
        new BitmapCacheTask().execute(BitmapCacheTask.MESSAGE_CLEAR_DISK);
    }

    public void clearDiskCache(String key) {
        new BitmapCacheTask().execute(BitmapCacheTask.MESSAGE_CLEAR_KEY_IN_DISK, key);
    }

    public void flushCache() {
        new BitmapCacheTask().execute(BitmapCacheTask.MESSAGE_FLUSH);
    }

    public void closeCache() {
        new BitmapCacheTask().execute(BitmapCacheTask.MESSAGE_CLOSE);
    }

    ////////////////////////////////////////// task //////////////////////////////////////////////////////////////////////

    public void onResume() {
        setExitTasksEarly(false);
    }

    public void onPause() {
        setExitTasksEarly(true);
        flushCache();
    }

    public void onDestroy() {
        closeCache();
    }

    public void setExitTasksEarly(boolean exitTasksEarly) {
        mExitTasksEarly = exitTasksEarly;
    }

    public void exitTasksEarly(boolean exitTasksEarly) {
        mExitTasksEarly = exitTasksEarly;
        if (exitTasksEarly) {
            pauseWork(false);//让暂停的线程结束
        }
    }

    public void pauseWork(boolean pauseWork) {
        synchronized (mPauseWorkLock) {
            mPauseWork = pauseWork;
            if (!mPauseWork) {
                mPauseWorkLock.notifyAll();
            }
        }
    }

    ///////////////////////////////////////////

    private static BitmapLoadTask getBitmapTaskFromImageView(ImageView imageView) {
        if (imageView != null) {
            final Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AsyncBitmapDrawable) {
                final AsyncBitmapDrawable asyncBitmapDrawable = (AsyncBitmapDrawable) drawable;
                return asyncBitmapDrawable.getBitmapWorkerTask();
            }
        }
        return null;
    }

    private static boolean bitmapLoadTaskExist(Object uriData, ImageView imageView) {
        final BitmapLoadTask bitmapWorkerTask = getBitmapTaskFromImageView(imageView);

        if (bitmapWorkerTask != null) {
            final Object bitmapData = bitmapWorkerTask.uriData;
            if (bitmapData == null || !bitmapData.equals(uriData)) {
                bitmapWorkerTask.cancel(true);
            } else {
                // 同一个线程已经在执行
                return true;
            }
        }
        return false;
    }

    private static class AsyncBitmapDrawable extends BitmapDrawable {


        private final WeakReference<BitmapLoadTask> bitmapLoadTaskReference;

        public AsyncBitmapDrawable(Resources res, Bitmap bitmap, BitmapLoadTask bitmapWorkerTask) {
            super(res, bitmap);
            bitmapLoadTaskReference = new WeakReference<BitmapLoadTask>(bitmapWorkerTask);
        }

        public BitmapLoadTask getBitmapWorkerTask() {
            return bitmapLoadTaskReference.get();
        }
    }

    private class BitmapLoadTask extends AsyncTask<Object, Void, Bitmap> {
        private Object uriData;
        private final WeakReference<ImageView> imageViewReference;
        private final BitmapDisplayConfig displayConfig;

        public BitmapLoadTask(ImageView imageView, BitmapDisplayConfig config) {
            imageViewReference = new WeakReference<ImageView>(imageView);
            displayConfig = config;
        }

        @Override
        protected Bitmap doInBackground(Object... params) {
            uriData = params[0];
            final String uri = String.valueOf(uriData);
            Bitmap bitmap = null;

            synchronized (mPauseWorkLock) {
                while (mPauseWork && !isCancelled()) {
                    try {
                        mPauseWorkLock.wait();
                    } catch (InterruptedException e) {
                    }
                }
            }

            if (mImageCache != null && !isCancelled() && getAttachedImageView() != null && !mExitTasksEarly) {
                bitmap = mImageCache.getBitmapFromDiskCache(uri);
            }

            if (bitmap == null && !isCancelled() && getAttachedImageView() != null && !mExitTasksEarly) {
                bitmap = downloadBitmap(uri, displayConfig);
            }

            if (bitmap != null && mImageCache != null) {
                mImageCache.addBitmapToCache(uri, bitmap);
            }

            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (isCancelled() || mExitTasksEarly) {
                bitmap = null;
            }

            // 判断线程和当前的ImageView是否是匹配
            final ImageView imageView = getAttachedImageView();
            if (bitmap != null && imageView != null) {
                mGlobalConfig.imageLoadCallBack.loadCompleted(imageView, bitmap, displayConfig);
            } else if (bitmap == null && imageView != null) {
                mGlobalConfig.imageLoadCallBack.loadFailed(imageView, displayConfig.getLoadFailedBitmap());
            }
        }

        @Override
        protected void onCancelled(Bitmap bitmap) {
            super.onCancelled(bitmap);
            synchronized (mPauseWorkLock) {
                mPauseWorkLock.notifyAll();
            }
        }

        /**
         * 获取线程匹配的imageView,防止出现闪动的现象
         *
         * @return
         */
        private ImageView getAttachedImageView() {
            final ImageView imageView = imageViewReference.get();
            final BitmapLoadTask bitmapWorkerTask = getBitmapTaskFromImageView(imageView);

            if (this == bitmapWorkerTask) {
                return imageView;
            }

            return null;
        }
    }

    private class BitmapCacheTask extends AsyncTask<Object, Void, Void> {
        public static final int MESSAGE_CLEAR = 0;
        public static final int MESSAGE_INIT_DISK_CACHE = 1;
        public static final int MESSAGE_FLUSH = 2;
        public static final int MESSAGE_CLOSE = 3;
        public static final int MESSAGE_CLEAR_MEMORY = 4;
        public static final int MESSAGE_CLEAR_DISK = 5;
        public static final int MESSAGE_CLEAR_KEY = 6;
        public static final int MESSAGE_CLEAR_KEY_IN_MEMORY = 7;
        public static final int MESSAGE_CLEAR_KEY_IN_DISK = 8;

        @Override
        protected Void doInBackground(Object... params) {
            switch ((Integer) params[0]) {
                case MESSAGE_CLEAR:
                    clearCacheInternalInBackground();
                    break;
                case MESSAGE_INIT_DISK_CACHE:
                    initDiskCacheInternalInBackground();
                    break;
                case MESSAGE_FLUSH:
                    clearMemoryCacheInBackground();
                    flushCacheInternalInBackground();
                    break;
                case MESSAGE_CLOSE:
                    clearMemoryCacheInBackground();
                    closeCacheInternalInBackground();
                case MESSAGE_CLEAR_MEMORY:
                    clearMemoryCacheInBackground();
                    break;
                case MESSAGE_CLEAR_DISK:
                    clearDiskCacheInBackground();
                    break;
                case MESSAGE_CLEAR_KEY:
                    clearCacheInBackground(String.valueOf(params[1]));
                    break;
                case MESSAGE_CLEAR_KEY_IN_MEMORY:
                    clearMemoryCacheInBackground(String.valueOf(params[1]));
                    break;
                case MESSAGE_CLEAR_KEY_IN_DISK:
                    clearDiskCacheInBackground(String.valueOf(params[1]));
                    break;
                default: {
                    break;
                }
            }
            return null;
        }
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////////////

    private class BitmapGlobalConfig {

        public String cachePath;

        public ImageLoadCallBack imageLoadCallBack;
        public Downloader downloader;
        public BitmapDownloadProcess bitmapProcess;
        public BitmapDisplayConfig defaultDisplayConfig;
        public float memCacheSizePercent;//缓存百分比，android系统分配给每个apk内存的大小
        public int memCacheSize;//内存缓存百分比
        public int diskCacheSize;//磁盘百分比
        public int poolSize = 3;//默认的线程池线程并发数量
        public int originalDiskCacheSize = 30 * 1024 * 1024;//50M


        public BitmapGlobalConfig() {
            defaultDisplayConfig = new BitmapDisplayConfig();

            defaultDisplayConfig.setAnimation(null);
            defaultDisplayConfig.setAnimationType(BitmapDisplayConfig.AnimationType.fadeIn);

            //设置图片的显示最大尺寸（为屏幕的大小,默认为屏幕宽度的1/2）
            DisplayMetrics displayMetrics = mContext.getResources().getDisplayMetrics();
            int defaultWidth = (int) Math.floor(displayMetrics.widthPixels / 2);
            defaultDisplayConfig.setBitmapHeight(defaultWidth);
            defaultDisplayConfig.setBitmapWidth(defaultWidth);
        }

        public void refreshConfig() {

            if (cachePath == null) {
                cachePath = BitmapCommonUtils.getDiskCacheDir(mContext, "xBitmapCache").getAbsolutePath();
            }

            if (downloader == null) {
                downloader = new SimpleDownloader();
            }

            bitmapProcess = new BitmapDownloadProcess(downloader, cachePath, originalDiskCacheSize);

            if (imageLoadCallBack == null) {
                imageLoadCallBack = new SimpleImageLoadCallBack();
            }

            BitmapCache.ImageCacheParams imageCacheParams = new BitmapCache.ImageCacheParams(mGlobalConfig.cachePath);
            if (mGlobalConfig.memCacheSizePercent > 0.05 && mGlobalConfig.memCacheSizePercent < 0.8) {
                imageCacheParams.setMemCacheSizePercent(mContext, mGlobalConfig.memCacheSizePercent);
            } else {
                if (mGlobalConfig.memCacheSize > 1024 * 1024 * 2) {
                    imageCacheParams.setMemCacheSize(mGlobalConfig.memCacheSize);
                } else {
                    //设置默认的内存缓存大小
                    imageCacheParams.setMemCacheSizePercent(mContext, 0.3f);
                }
            }
            if (mGlobalConfig.diskCacheSize > 1024 * 1024 * 5) {
                imageCacheParams.setDiskCacheSize(mGlobalConfig.diskCacheSize);
            }
            mImageCache = new BitmapCache(imageCacheParams);

            bitmapLoadExecutor = Executors.newFixedThreadPool(mGlobalConfig.poolSize, new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    Thread t = new Thread(r);
                    t.setPriority(Thread.NORM_PRIORITY - 1);
                    return t;
                }
            });
        }

    }

    private BitmapUtils configDiskCachePath(String strPath) {
        if (!TextUtils.isEmpty(strPath)) {
            mGlobalConfig.cachePath = strPath;
        }
        return this;
    }

    private BitmapUtils configMemoryCacheSize(int size) {
        mGlobalConfig.memCacheSize = size;
        return this;
    }

    private BitmapUtils configMemoryCachePercent(float percent) {
        mGlobalConfig.memCacheSizePercent = percent;
        return this;
    }

    private BitmapUtils configDiskCacheSize(int size) {
        mGlobalConfig.diskCacheSize = size;
        return this;
    }

    private BitmapUtils configBitmapLoadThreadSize(int size) {
        if (size >= 1)
            mGlobalConfig.poolSize = size;
        return this;
    }

    private BitmapDisplayConfig getDisplayConfig() {
        BitmapDisplayConfig config = new BitmapDisplayConfig();
        config.setAnimation(mGlobalConfig.defaultDisplayConfig.getAnimation());
        config.setAnimationType(mGlobalConfig.defaultDisplayConfig.getAnimationType());
        config.setBitmapHeight(mGlobalConfig.defaultDisplayConfig.getBitmapHeight());
        config.setBitmapWidth(mGlobalConfig.defaultDisplayConfig.getBitmapWidth());
        config.setLoadFailedBitmap(mGlobalConfig.defaultDisplayConfig.getLoadFailedBitmap());
        config.setLoadingBitmap(mGlobalConfig.defaultDisplayConfig.getLoadingBitmap());
        return config;
    }

    private void initDiskCacheInternalInBackground() {
        if (mImageCache != null) {
            mImageCache.initDiskCache();
        }
        if (mGlobalConfig != null && mGlobalConfig.bitmapProcess != null) {
            mGlobalConfig.bitmapProcess.initHttpDiskCache();
        }
    }

    private void clearCacheInternalInBackground() {
        if (mImageCache != null) {
            mImageCache.clearCache();
        }
        if (mGlobalConfig != null && mGlobalConfig.bitmapProcess != null) {
            mGlobalConfig.bitmapProcess.clearCacheInternal();
        }
    }

    private void clearMemoryCacheInBackground() {
        if (mImageCache != null) {
            mImageCache.clearMemoryCache();
        }
    }

    private void clearDiskCacheInBackground() {
        if (mImageCache != null) {
            mImageCache.clearDiskCache();
        }
        if (mGlobalConfig != null && mGlobalConfig.bitmapProcess != null) {
            mGlobalConfig.bitmapProcess.clearCacheInternal();
        }
    }

    private void clearCacheInBackground(String key) {
        if (mImageCache != null) {
            mImageCache.clearCache(key);
        }
    }

    private void clearDiskCacheInBackground(String key) {
        if (mImageCache != null) {
            mImageCache.clearDiskCache(key);
        }
    }

    private void clearMemoryCacheInBackground(String key) {
        if (mImageCache != null) {
            mImageCache.clearMemoryCache(key);
        }
    }

    private void flushCacheInternalInBackground() {
        if (mImageCache != null) {
            mImageCache.flush();
        }
        if (mGlobalConfig != null && mGlobalConfig.bitmapProcess != null) {
            mGlobalConfig.bitmapProcess.flushCacheInternal();
        }
    }

    /**
     * instance will be null
     */
    private void closeCacheInternalInBackground() {
        if (mImageCache != null) {
            mImageCache.close();
            mImageCache = null;
            instance = null;
        }
        if (mGlobalConfig != null && mGlobalConfig.bitmapProcess != null) {
            mGlobalConfig.bitmapProcess.clearCacheInternal();
        }
    }

    private Bitmap downloadBitmap(String uri, BitmapDisplayConfig config) {
        if (mGlobalConfig != null && mGlobalConfig.bitmapProcess != null) {
            return mGlobalConfig.bitmapProcess.downloadBitmap(uri, config);
        }
        return null;
    }

}
