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
import android.widget.ImageView;
import com.lidroid.xutils.bitmap.BitmapDisplayConfig;
import com.lidroid.xutils.bitmap.BitmapGlobalConfig;
import com.lidroid.xutils.bitmap.BitmapGlobalConfigChangeCallBack;
import com.lidroid.xutils.bitmap.callback.ImageLoadCallBack;
import com.lidroid.xutils.bitmap.core.BitmapCache;
import com.lidroid.xutils.bitmap.download.Downloader;
import com.lidroid.xutils.util.LogUtils;

import java.lang.ref.WeakReference;
import java.util.HashMap;

public class BitmapUtils implements BitmapGlobalConfigChangeCallBack {

    private static boolean mPause = false;
    private static boolean mPauseWork = false;
    private static final Object mPauseWorkLock = new Object();

    private static Context mContext;
    private static BitmapUtils instance;
    private static BitmapGlobalConfig mGlobalConfig;
    private static BitmapCache mImageCache;

    /////////////////////////////////////////////// create ///////////////////////////////////////////////////
    private BitmapUtils(Context context) {
        mContext = context;
        mGlobalConfig = new BitmapGlobalConfig(context, this);
        mImageCache = new BitmapCache(mGlobalConfig);
    }

    public static BitmapUtils create(Context ctx) {
        if (instance == null) {
            instance = new BitmapUtils(ctx.getApplicationContext());
        }
        mGlobalConfig.notifyMemoryCacheConfigChanged();
        mGlobalConfig.notifyDiskCacheConfigChanged();
        return instance;
    }

    public static BitmapUtils create(Context ctx, String diskCachePath) {
        if (instance == null) {
            instance = new BitmapUtils(ctx.getApplicationContext());
        }
        mGlobalConfig.setDiskCachePath(diskCachePath);

        mGlobalConfig.notifyMemoryCacheConfigChanged();
        mGlobalConfig.notifyDiskCacheConfigChanged();
        return instance;
    }

    public static BitmapUtils create(Context ctx, String diskCachePath, float memoryCachePercent) {
        if (instance == null) {
            instance = new BitmapUtils(ctx.getApplicationContext());
        }
        mGlobalConfig.setDiskCachePath(diskCachePath);
        mGlobalConfig.setMemCacheSizePercent(mContext, memoryCachePercent);

        mGlobalConfig.notifyMemoryCacheConfigChanged();
        mGlobalConfig.notifyDiskCacheConfigChanged();
        return instance;
    }

    public static BitmapUtils create(Context ctx, String diskCachePath, int memoryCacheSize) {
        if (instance == null) {
            instance = new BitmapUtils(ctx.getApplicationContext());
        }
        mGlobalConfig.setDiskCachePath(diskCachePath);
        mGlobalConfig.setMemCacheSize(memoryCacheSize);

        mGlobalConfig.notifyMemoryCacheConfigChanged();
        mGlobalConfig.notifyDiskCacheConfigChanged();
        return instance;
    }

    public static BitmapUtils create(Context ctx, String diskCachePath, float memoryCachePercent, int poolSize) {
        if (instance == null) {
            instance = new BitmapUtils(ctx.getApplicationContext());
        }
        mGlobalConfig.setDiskCachePath(diskCachePath);
        mGlobalConfig.setMemCacheSizePercent(mContext, memoryCachePercent);
        mGlobalConfig.setPoolSize(poolSize);

        mGlobalConfig.notifyMemoryCacheConfigChanged();
        mGlobalConfig.notifyDiskCacheConfigChanged();
        return instance;
    }

    public static BitmapUtils create(Context ctx, String diskCachePath, int memoryCacheSize, int poolSize) {
        if (instance == null) {
            instance = new BitmapUtils(ctx.getApplicationContext());
        }
        mGlobalConfig.setDiskCachePath(diskCachePath);
        mGlobalConfig.setMemCacheSize(memoryCacheSize);
        mGlobalConfig.setPoolSize(poolSize);

        mGlobalConfig.notifyMemoryCacheConfigChanged();
        mGlobalConfig.notifyDiskCacheConfigChanged();
        return instance;
    }

    public static BitmapUtils create(Context ctx, String diskCachePath, float memoryCachePercent, int diskCacheSize, int poolSize) {
        if (instance == null) {
            instance = new BitmapUtils(ctx.getApplicationContext());
        }
        mGlobalConfig.setDiskCachePath(diskCachePath);
        mGlobalConfig.setMemCacheSizePercent(mContext, memoryCachePercent);
        mGlobalConfig.setDiskCacheSize(diskCacheSize);
        mGlobalConfig.setPoolSize(poolSize);

        mGlobalConfig.notifyMemoryCacheConfigChanged();
        mGlobalConfig.notifyDiskCacheConfigChanged();
        return instance;
    }

    public static BitmapUtils create(Context ctx, String diskCachePath, int memoryCacheSize, int diskCacheSize, int poolSize) {
        if (instance == null) {
            instance = new BitmapUtils(ctx.getApplicationContext());
        }
        mGlobalConfig.setDiskCachePath(diskCachePath);
        mGlobalConfig.setMemCacheSize(memoryCacheSize);
        mGlobalConfig.setDiskCacheSize(diskCacheSize);
        mGlobalConfig.setPoolSize(poolSize);

        mGlobalConfig.notifyMemoryCacheConfigChanged();
        mGlobalConfig.notifyDiskCacheConfigChanged();
        return instance;
    }


    //////////////////////////////////////// config ////////////////////////////////////////////////////////////////////

    public BitmapUtils configLoadingImage(Bitmap bitmap) {
        mGlobalConfig.getDefaultDisplayConfig().setLoadingBitmap(bitmap);
        return this;
    }

    public BitmapUtils configLoadingImage(int resId) {
        mGlobalConfig.getDefaultDisplayConfig().setLoadingBitmap(BitmapFactory.decodeResource(mContext.getResources(), resId));
        return this;
    }

    public BitmapUtils configLoadFailedImage(Bitmap bitmap) {
        mGlobalConfig.getDefaultDisplayConfig().setLoadFailedBitmap(bitmap);
        return this;
    }

    public BitmapUtils configLoadFailedImage(int resId) {
        mGlobalConfig.getDefaultDisplayConfig().setLoadFailedBitmap(BitmapFactory.decodeResource(mContext.getResources(), resId));
        return this;
    }

    public BitmapUtils configBitmapMaxHeight(int bitmapHeight) {
        mGlobalConfig.getDefaultDisplayConfig().setBitmapHeight(bitmapHeight);
        return this;
    }

    public BitmapUtils configBitmapMaxWidth(int bitmapWidth) {
        mGlobalConfig.getDefaultDisplayConfig().setBitmapWidth(bitmapWidth);
        return this;
    }

    public BitmapUtils configDownloader(Downloader downloader) {
        mGlobalConfig.setDownloader(downloader);
        return this;
    }

    public BitmapUtils configImageLoadCallBack(ImageLoadCallBack imageLoadCallBack) {
        mGlobalConfig.setImageLoadCallBack(imageLoadCallBack);
        return this;
    }

    public BitmapUtils configDefaultCompressFormat(CompressFormat format) {
        mGlobalConfig.setDefaultCompressFormat(format);
        return this;
    }

    public BitmapUtils configCalculateBitmap(boolean neverCalculate) {
        mGlobalConfig.getBitmapDownloadProcess().configCalculateBitmap(neverCalculate);
        return this;
    }

    ////////////////////////// display ////////////////////////////////////
    private HashMap<String, BitmapDisplayConfig> displayConfigMap = new HashMap<String, BitmapDisplayConfig>();


    public void display(ImageView imageView, String uri) {
        doDisplay(imageView, uri, null, null);
    }

    public void display(ImageView imageView, String uri,
                        CompressFormat compressFormat) {
        doDisplay(imageView, uri, null, compressFormat);
    }

    public void display(ImageView imageView, String uri, int imageWidth, int imageHeight) {
        display(imageView, uri, imageWidth, imageHeight, null);
    }

    public void display(ImageView imageView, String uri, int imageWidth, int imageHeight,
                        CompressFormat compressFormat) {
        BitmapDisplayConfig displayConfig = displayConfigMap.get(imageWidth + "_" + imageHeight);
        if (displayConfig == null) {
            displayConfig = mGlobalConfig.getDefaultDisplayConfig();
            displayConfig.setBitmapHeight(imageHeight);
            displayConfig.setBitmapWidth(imageWidth);
            displayConfigMap.put(imageWidth + "_" + imageHeight, displayConfig);
        }

        doDisplay(imageView, uri, displayConfig, compressFormat);
    }

    public void display(ImageView imageView, String uri, Bitmap loadingBitmap) {
        display(imageView, uri, loadingBitmap, (CompressFormat) null);
    }

    public void display(ImageView imageView, String uri, Bitmap loadingBitmap,
                        CompressFormat compressFormat) {
        BitmapDisplayConfig displayConfig = displayConfigMap.get(String.valueOf(loadingBitmap));
        if (displayConfig == null) {
            displayConfig = mGlobalConfig.getDefaultDisplayConfig();
            displayConfig.setLoadingBitmap(loadingBitmap);
            displayConfigMap.put(String.valueOf(loadingBitmap), displayConfig);
        }

        doDisplay(imageView, uri, displayConfig, compressFormat);
    }

    public void display(ImageView imageView, String uri, Bitmap loadingBitmap, Bitmap loadFailedBitmap) {
        display(imageView, uri, loadingBitmap, loadFailedBitmap, null);
    }

    public void display(ImageView imageView, String uri, Bitmap loadingBitmap, Bitmap loadFailedBitmap,
                        CompressFormat compressFormat) {
        BitmapDisplayConfig displayConfig = displayConfigMap.get(String.valueOf(loadingBitmap) + "_" + String.valueOf(loadFailedBitmap));
        if (displayConfig == null) {
            displayConfig = mGlobalConfig.getDefaultDisplayConfig();
            displayConfig.setLoadingBitmap(loadingBitmap);
            displayConfig.setLoadFailedBitmap(loadFailedBitmap);
            displayConfigMap.put(String.valueOf(loadingBitmap) + "_" + String.valueOf(loadFailedBitmap), displayConfig);
        }

        doDisplay(imageView, uri, displayConfig, compressFormat);
    }

    public void display(ImageView imageView, String uri, int imageWidth, int imageHeight, Bitmap loadingBitmap, Bitmap loadFailedBitmap) {
        display(imageView, uri, imageWidth, imageHeight, loadingBitmap, loadFailedBitmap, null);
    }

    public void display(ImageView imageView, String uri, int imageWidth, int imageHeight, Bitmap loadingBitmap, Bitmap loadFailedBitmap,
                        CompressFormat compressFormat) {
        BitmapDisplayConfig displayConfig = displayConfigMap.get(imageWidth + "_" + imageHeight + "_" + String.valueOf(loadingBitmap) + "_" + String.valueOf(loadFailedBitmap));
        if (displayConfig == null) {
            displayConfig = mGlobalConfig.getDefaultDisplayConfig();
            displayConfig.setBitmapHeight(imageHeight);
            displayConfig.setBitmapWidth(imageWidth);
            displayConfig.setLoadingBitmap(loadingBitmap);
            displayConfig.setLoadFailedBitmap(loadFailedBitmap);
            displayConfigMap.put(imageWidth + "_" + imageHeight + "_" + String.valueOf(loadingBitmap) + "_" + String.valueOf(loadFailedBitmap), displayConfig);
        }

        doDisplay(imageView, uri, displayConfig, compressFormat);
    }

    public void display(ImageView imageView, String uri, BitmapDisplayConfig displayConfig) {
        doDisplay(imageView, uri, displayConfig, null);
    }

    public void display(ImageView imageView, String uri, BitmapDisplayConfig displayConfig,
                        CompressFormat compressFormat) {
        doDisplay(imageView, uri, displayConfig, compressFormat);
    }


    private void doDisplay(ImageView imageView, String uri, BitmapDisplayConfig displayConfig, CompressFormat compressFormat) {
        if (TextUtils.isEmpty(uri) || imageView == null) {
            return;
        }

        if (displayConfig == null) {
            displayConfig = mGlobalConfig.getDefaultDisplayConfig();
        }

        Bitmap bitmap = null;

        if (mImageCache != null) {
            bitmap = mImageCache.getBitmapFromMemCache(uri);
        }

        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);

        } else if (!bitmapLoadTaskExist(uri, imageView)) {

            final BitmapLoadTask loadTask = new BitmapLoadTask(imageView, displayConfig);
            // set loading image
            final AsyncBitmapDrawable asyncBitmapDrawable = new AsyncBitmapDrawable(
                    mContext.getResources(),
                    displayConfig.getLoadingBitmap(),
                    loadTask);
            imageView.setImageDrawable(asyncBitmapDrawable);

            // load bitmap from uri or diskCache
            loadTask.executeOnExecutor(mGlobalConfig.getBitmapLoadExecutor(), uri, compressFormat);
        }
    }


    /////////////////////////////////////////////// cache /////////////////////////////////////////////////////////////////

    public void clearCache() {
        new BitmapCacheManagementTask().execute(BitmapCacheManagementTask.MESSAGE_CLEAR);
    }

    public void clearCache(String key) {
        new BitmapCacheManagementTask().execute(BitmapCacheManagementTask.MESSAGE_CLEAR_BY_KEY, key);
    }

    public void clearMemoryCache() {
        new BitmapCacheManagementTask().execute(BitmapCacheManagementTask.MESSAGE_CLEAR_MEMORY);
    }

    public void clearMemoryCache(String key) {
        new BitmapCacheManagementTask().execute(BitmapCacheManagementTask.MESSAGE_CLEAR_MEMORY_BY_KEY, key);
    }

    public void clearDiskCache() {
        new BitmapCacheManagementTask().execute(BitmapCacheManagementTask.MESSAGE_CLEAR_DISK);
    }

    public void clearDiskCache(String key) {
        new BitmapCacheManagementTask().execute(BitmapCacheManagementTask.MESSAGE_CLEAR_DISK_BY_KEY, key);
    }

    public void flushCache() {
        new BitmapCacheManagementTask().execute(BitmapCacheManagementTask.MESSAGE_FLUSH);
    }

    public void closeCache() {
        new BitmapCacheManagementTask().execute(BitmapCacheManagementTask.MESSAGE_CLOSE);
    }

    ////////////////////////////////////////// tasks //////////////////////////////////////////////////////////////////////

    public void resumeTasks() {
        mPause = false;
    }

    public void pauseTasks() {
        mPause = true;
        flushCache();
    }

    public void stopTasks() {
        mPause = true;
        synchronized (mPauseWorkLock) {
            mPauseWorkLock.notifyAll();
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

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
        final BitmapLoadTask oldLoadTask = getBitmapTaskFromImageView(imageView);

        if (oldLoadTask != null) {
            final Object oldUri = oldLoadTask.uriData;
            if (oldUri == null || !oldUri.equals(uriData)) {
                oldLoadTask.cancel(true);
            } else {
                // 同一个线程已经在执行
                return true;
            }
        }
        return false;
    }

    private Bitmap downloadBitmap(String uri, BitmapDisplayConfig config) {
        if (mGlobalConfig != null && mGlobalConfig.getBitmapDownloadProcess() != null) {
            return mGlobalConfig.getBitmapDownloadProcess().downloadBitmap(uri, config);
        }
        return null;
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
            CompressFormat format = null;
            if (params != null && params.length > 0) {
                uriData = params[0];
                if (params.length > 1) {
                    format = (CompressFormat) params[1];
                }
            }
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

            if (mImageCache != null && !isCancelled() && getAttachedImageView() != null && !mPause) {
                bitmap = mImageCache.getBitmapFromDiskCache(uri);
            }

            if (bitmap == null && !isCancelled() && getAttachedImageView() != null && !mPause) {
                bitmap = downloadBitmap(uri, displayConfig);
            }

            if (bitmap != null && mImageCache != null) {
                format = format == null ? mGlobalConfig.getDefaultCompressFormat() : format;
                mImageCache.addBitmapToCache(uri, bitmap, format);
            }

            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (isCancelled() || mPause) {
                bitmap = null;
            }

            // 判断线程和当前的ImageView是否是匹配
            final ImageView imageView = getAttachedImageView();
            if (bitmap != null && imageView != null) {
                mGlobalConfig.getImageLoadCallBack().loadCompleted(imageView, bitmap, displayConfig);
            } else if (bitmap == null && imageView != null) {
                mGlobalConfig.getImageLoadCallBack().loadFailed(imageView, displayConfig.getLoadFailedBitmap());
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
            if (mImageCache != null) {
                mImageCache.initMemoryCache();
            }
        }

        private void initDiskInBackground() {
            if (mImageCache != null) {
                mImageCache.initDiskCache();
            }
            if (mGlobalConfig != null && mGlobalConfig.getBitmapDownloadProcess() != null) {
                mGlobalConfig.getBitmapDownloadProcess().initOriginalDiskCache();
            }
        }

        private void clearCacheInBackground() {
            if (mImageCache != null) {
                mImageCache.clearCache();
            }
            if (mGlobalConfig != null && mGlobalConfig.getBitmapDownloadProcess() != null) {
                mGlobalConfig.getBitmapDownloadProcess().clearOriginalDiskCache();
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
            if (mGlobalConfig != null && mGlobalConfig.getBitmapDownloadProcess() != null) {
                mGlobalConfig.getBitmapDownloadProcess().clearOriginalDiskCache();
            }
        }

        private void clearCacheInBackground(String key) {
            if (mImageCache != null) {
                mImageCache.clearCache(key);
            }
            if (mGlobalConfig != null && mGlobalConfig.getBitmapDownloadProcess() != null) {
                mGlobalConfig.getBitmapDownloadProcess().clearOriginalDiskCache(key);
            }
        }

        private void clearDiskCacheInBackground(String key) {
            if (mImageCache != null) {
                mImageCache.clearDiskCache(key);
            }
            if (mGlobalConfig != null && mGlobalConfig.getBitmapDownloadProcess() != null) {
                mGlobalConfig.getBitmapDownloadProcess().clearOriginalDiskCache(key);
            }
        }

        private void clearMemoryCacheInBackground(String key) {
            if (mImageCache != null) {
                mImageCache.clearMemoryCache(key);
            }
        }

        private void flushCacheInBackground() {
            if (mImageCache != null) {
                mImageCache.flush();
            }
            if (mGlobalConfig != null && mGlobalConfig.getBitmapDownloadProcess() != null) {
                mGlobalConfig.getBitmapDownloadProcess().flushOriginalDiskCache();
            }
        }

        private void closeCacheInBackground() {
            if (mImageCache != null) {
                mImageCache.close();
            }
            if (mGlobalConfig != null && mGlobalConfig.getBitmapDownloadProcess() != null) {
                mGlobalConfig.getBitmapDownloadProcess().closeOriginalDiskCache();
            }
        }
    }

    @Override
    public void onMemoryCacheConfigChanged(BitmapGlobalConfig config) {
        new BitmapCacheManagementTask().execute(BitmapCacheManagementTask.MESSAGE_INIT_MEMORY_CACHE);
    }

    @Override
    public void onDiskCacheConfigChanged(BitmapGlobalConfig config) {
        new BitmapCacheManagementTask().execute(BitmapCacheManagementTask.MESSAGE_INIT_DISK_CACHE);
    }

}
