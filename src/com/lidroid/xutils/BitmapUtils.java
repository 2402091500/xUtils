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
import android.text.TextUtils;
import android.widget.ImageView;
import com.lidroid.xutils.bitmap.BitmapDisplayConfig;
import com.lidroid.xutils.bitmap.BitmapGlobalConfig;
import com.lidroid.xutils.bitmap.callback.ImageLoadCallBack;
import com.lidroid.xutils.bitmap.download.Downloader;
import com.lidroid.xutils.util.core.CompatibleAsyncTask;

import java.lang.ref.WeakReference;
import java.util.HashMap;

public class BitmapUtils {

    private static boolean pauseTask = false;
    private static final Object pauseTaskLock = new Object();

    private static Context context;
    private static BitmapUtils instance;
    private static BitmapGlobalConfig globalConfig;

    /////////////////////////////////////////////// create ///////////////////////////////////////////////////
    private BitmapUtils(Context context, String diskCachePath) {
        BitmapUtils.context = context;
        globalConfig = new BitmapGlobalConfig(context, diskCachePath);
    }

    public static BitmapUtils create(Context ctx) {
        if (instance == null) {
            instance = new BitmapUtils(ctx.getApplicationContext(), null);
        }
        return instance;
    }

    public static BitmapUtils create(Context ctx, String diskCachePath) {
        if (instance == null) {
            instance = new BitmapUtils(ctx.getApplicationContext(), diskCachePath);
        }
        return instance;
    }

    public static BitmapUtils create(Context ctx, String diskCachePath, int memoryCacheSize) {
        if (instance == null) {
            instance = new BitmapUtils(ctx.getApplicationContext(), diskCachePath);
            globalConfig.setMemoryCacheSize(memoryCacheSize);
        }
        return instance;
    }

    public static BitmapUtils create(Context ctx, String diskCachePath, int memoryCacheSize, int diskCacheSize) {
        if (instance == null) {
            instance = new BitmapUtils(ctx.getApplicationContext(), diskCachePath);
            globalConfig.setMemoryCacheSize(memoryCacheSize);
            globalConfig.setDiskCacheSize(diskCacheSize);
        }
        return instance;
    }

    public static BitmapUtils create(Context ctx, String diskCachePath, float memoryCachePercent) {
        if (instance == null) {
            instance = new BitmapUtils(ctx.getApplicationContext(), diskCachePath);
            globalConfig.setMemCacheSizePercent(memoryCachePercent);
        }
        return instance;
    }

    public static BitmapUtils create(Context ctx, String diskCachePath, float memoryCachePercent, int diskCacheSize) {
        if (instance == null) {
            instance = new BitmapUtils(ctx.getApplicationContext(), diskCachePath);
            globalConfig.setMemCacheSizePercent(memoryCachePercent);
            globalConfig.setDiskCacheSize(diskCacheSize);
        }
        return instance;
    }

    //////////////////////////////////////// config ////////////////////////////////////////////////////////////////////

    public BitmapUtils configLoadingImage(Bitmap bitmap) {
        globalConfig.getDefaultDisplayConfig().setLoadingBitmap(bitmap);
        return this;
    }

    public BitmapUtils configLoadingImage(int resId) {
        globalConfig.getDefaultDisplayConfig().setLoadingBitmap(BitmapFactory.decodeResource(context.getResources(), resId));
        return this;
    }

    public BitmapUtils configLoadFailedImage(Bitmap bitmap) {
        globalConfig.getDefaultDisplayConfig().setLoadFailedBitmap(bitmap);
        return this;
    }

    public BitmapUtils configLoadFailedImage(int resId) {
        globalConfig.getDefaultDisplayConfig().setLoadFailedBitmap(BitmapFactory.decodeResource(context.getResources(), resId));
        return this;
    }

    public BitmapUtils configBitmapMaxHeight(int bitmapHeight) {
        globalConfig.getDefaultDisplayConfig().setBitmapHeight(bitmapHeight);
        return this;
    }

    public BitmapUtils configBitmapMaxWidth(int bitmapWidth) {
        globalConfig.getDefaultDisplayConfig().setBitmapWidth(bitmapWidth);
        return this;
    }

    public BitmapUtils configDownloader(Downloader downloader) {
        globalConfig.setDownloader(downloader);
        return this;
    }

    public BitmapUtils configImageLoadCallBack(ImageLoadCallBack imageLoadCallBack) {
        globalConfig.setImageLoadCallBack(imageLoadCallBack);
        return this;
    }

    public BitmapUtils configDefaultCompressFormat(CompressFormat format) {
        globalConfig.setDefaultCompressFormat(format);
        return this;
    }

    public BitmapUtils configThreadPoolSize(int poolSize) {
        globalConfig.setThreadPoolSize(poolSize);
        return this;
    }

    public BitmapUtils configCalculateBitmap(boolean neverCalculate) {
        globalConfig.getBitmapDownloadProcess().neverCalculate(neverCalculate);
        return this;
    }

    public BitmapUtils configMemoryCacheEnabled(boolean enabled) {
        globalConfig.setMemoryCacheEnabled(enabled);
        return this;
    }

    public BitmapUtils configDiskCacheEnabled(boolean enabled) {
        globalConfig.setDiskCacheEnabled(enabled);
        return this;
    }

    public BitmapUtils configGlobalConfig(BitmapGlobalConfig globalConfig) {
        BitmapUtils.globalConfig = globalConfig;
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
            displayConfig = globalConfig.getDefaultDisplayConfig();
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
            displayConfig = globalConfig.getDefaultDisplayConfig();
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
            displayConfig = globalConfig.getDefaultDisplayConfig();
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
            displayConfig = globalConfig.getDefaultDisplayConfig();
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
            displayConfig = globalConfig.getDefaultDisplayConfig();
        }

        Bitmap bitmap = null;

        bitmap = globalConfig.getBitmapCache().getBitmapFromMemCache(uri);

        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);

        } else if (!bitmapLoadTaskExist(imageView, uri)) {

            final BitmapLoadTask loadTask = new BitmapLoadTask(imageView, displayConfig);
            // set loading image
            final AsyncBitmapDrawable asyncBitmapDrawable = new AsyncBitmapDrawable(
                    context.getResources(),
                    displayConfig.getLoadingBitmap(),
                    loadTask);
            imageView.setImageDrawable(asyncBitmapDrawable);

            // load bitmap from uri or diskCache
            loadTask.executeOnExecutor(globalConfig.getBitmapLoadExecutor(), uri, compressFormat);
        }
    }


    /////////////////////////////////////////////// cache /////////////////////////////////////////////////////////////////

    public void clearCache() {
        globalConfig.clearCache();
    }

    public void clearCache(String url) {
        globalConfig.clearCache(url);
    }

    public void clearMemoryCache() {
        globalConfig.clearMemoryCache();
    }

    public void clearMemoryCache(String url) {
        globalConfig.clearMemoryCache(url);
    }

    public void clearDiskCache() {
        globalConfig.clearDiskCache();
    }

    public void clearDiskCache(String url) {
        globalConfig.clearDiskCache(url);
    }

    public void flushCache() {
        globalConfig.flushCache();
    }

    public void closeCache() {
        globalConfig.closeCache();
    }

    public Bitmap getBitmapFromMemCache(String uri) {
        return globalConfig.getBitmapCache().getBitmapFromMemCache(uri);
    }

    public BitmapGlobalConfig getBitmapGlobalConfig() {
        return globalConfig;
    }

    ////////////////////////////////////////// tasks //////////////////////////////////////////////////////////////////////

    public void resumeTasks() {
        pauseTask = false;
    }

    public void pauseTasks() {
        pauseTask = true;
        flushCache();
    }

    public void stopTasks() {
        pauseTask = true;
        synchronized (pauseTaskLock) {
            pauseTaskLock.notifyAll();
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

    private static boolean bitmapLoadTaskExist(ImageView imageView, Object uriData) {
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

    private class BitmapLoadTask extends CompatibleAsyncTask<Object, Void, Bitmap> {
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

            synchronized (pauseTaskLock) {
                while (pauseTask && !isCancelled()) {
                    try {
                        pauseTaskLock.wait();
                    } catch (InterruptedException e) {
                    }
                }
            }

            // 从缓存获取图片
            if (!isCancelled() && getAttachedImageView() != null && !pauseTask) {
                bitmap = globalConfig.getBitmapCache().getBitmapFromDiskCache(uri);
            }

            // 下载图片
            if (bitmap == null && !isCancelled() && getAttachedImageView() != null && !pauseTask) {
                bitmap = globalConfig.getBitmapDownloadProcess().downloadBitmap(uri, displayConfig);
            }

            // 加入缓存
            if (bitmap != null) {
                format = format == null ? globalConfig.getDefaultCompressFormat() : format;
                globalConfig.getBitmapCache().addBitmapToCache(uri, bitmap, format);
            }

            return bitmap;
        }

        // 获取图片任务完成
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (isCancelled() || pauseTask) {
                bitmap = null;
            }

            final ImageView imageView = getAttachedImageView();
            if (bitmap != null && imageView != null) {//显示图片
                globalConfig.getImageLoadCallBack().loadCompleted(imageView, bitmap, displayConfig);
            } else if (bitmap == null && imageView != null) {//显示获取错误图片
                globalConfig.getImageLoadCallBack().loadFailed(imageView, displayConfig.getLoadFailedBitmap());
            }
        }

        @Override
        protected void onCancelled(Bitmap bitmap) {
            super.onCancelled(bitmap);
            synchronized (pauseTaskLock) {
                pauseTaskLock.notifyAll();
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
}
