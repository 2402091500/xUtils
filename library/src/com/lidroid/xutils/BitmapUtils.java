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
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.animation.Animation;
import android.widget.ImageView;
import com.lidroid.xutils.bitmap.BitmapDisplayConfig;
import com.lidroid.xutils.bitmap.BitmapGlobalConfig;
import com.lidroid.xutils.bitmap.callback.ImageLoadCallBack;
import com.lidroid.xutils.bitmap.download.Downloader;
import com.lidroid.xutils.util.core.CompatibleAsyncTask;
import com.lidroid.xutils.util.core.LruDiskCache;

import java.lang.ref.WeakReference;

public class BitmapUtils {

    private boolean pauseTask = false;
    private final Object pauseTaskLock = new Object();

    private Context context;
    private BitmapGlobalConfig globalConfig;
    private BitmapDisplayConfig defaultDisplayConfig;

    /////////////////////////////////////////////// create ///////////////////////////////////////////////////
    public BitmapUtils(Context context) {
        this(context, null);
    }

    public BitmapUtils(Context context, String diskCachePath) {
        this.context = context;
        globalConfig = new BitmapGlobalConfig(context, diskCachePath);
        defaultDisplayConfig = new BitmapDisplayConfig(context);
    }

    public BitmapUtils(Context context, String diskCachePath, int memoryCacheSize) {
        this(context, diskCachePath);
        globalConfig.setMemoryCacheSize(memoryCacheSize);
    }

    public BitmapUtils(Context context, String diskCachePath, int memoryCacheSize, int diskCacheSize) {
        this(context, diskCachePath);
        globalConfig.setMemoryCacheSize(memoryCacheSize);
        globalConfig.setDiskCacheSize(diskCacheSize);
    }

    public BitmapUtils(Context context, String diskCachePath, float memoryCachePercent) {
        this(context, diskCachePath);
        globalConfig.setMemCacheSizePercent(memoryCachePercent);
    }

    public BitmapUtils(Context context, String diskCachePath, float memoryCachePercent, int diskCacheSize) {
        this(context, diskCachePath);
        globalConfig.setMemCacheSizePercent(memoryCachePercent);
        globalConfig.setDiskCacheSize(diskCacheSize);
    }

    //////////////////////////////////////// config ////////////////////////////////////////////////////////////////////

    public BitmapUtils configDefaultLoadingImage(Bitmap bitmap) {
        defaultDisplayConfig.setLoadingBitmap(bitmap);
        return this;
    }

    public BitmapUtils configDefaultLoadingImage(int resId) {
        defaultDisplayConfig.setLoadingBitmap(BitmapFactory.decodeResource(context.getResources(), resId));
        return this;
    }

    public BitmapUtils configDefaultLoadFailedImage(Bitmap bitmap) {
        defaultDisplayConfig.setLoadFailedBitmap(bitmap);
        return this;
    }

    public BitmapUtils configDefaultLoadFailedImage(int resId) {
        defaultDisplayConfig.setLoadFailedBitmap(BitmapFactory.decodeResource(context.getResources(), resId));
        return this;
    }

    public BitmapUtils configDefaultBitmapMaxWidth(int bitmapWidth) {
        defaultDisplayConfig.setBitmapMaxWidth(bitmapWidth);
        return this;
    }

    public BitmapUtils configDefaultBitmapMaxHeight(int bitmapHeight) {
        defaultDisplayConfig.setBitmapMaxHeight(bitmapHeight);
        return this;
    }

    public BitmapUtils configDefaultImageLoadAnimation(Animation animation) {
        defaultDisplayConfig.setAnimation(animation);
        return this;
    }

    public BitmapUtils configDefaultImageLoadCallBack(ImageLoadCallBack imageLoadCallBack) {
        defaultDisplayConfig.setImageLoadCallBack(imageLoadCallBack);
        return this;
    }

    public BitmapUtils configDefaultShowOriginal(boolean showOriginal) {
        defaultDisplayConfig.setShowOriginal(showOriginal);
        return this;
    }

    public BitmapUtils configDefaultBitmapConfig(Bitmap.Config config) {
        defaultDisplayConfig.setBitmapConfig(config);
        return this;
    }

    public BitmapUtils configDefaultDisplayConfig(BitmapDisplayConfig displayConfig) {
        defaultDisplayConfig = displayConfig;
        return this;
    }

    public BitmapUtils configDownloader(Downloader downloader) {
        globalConfig.setDownloader(downloader);
        return this;
    }

    public BitmapUtils configDefaultCacheExpiry(long defaultExpiry) {
        globalConfig.setDefaultCacheExpiry(defaultExpiry);
        return this;
    }

    public BitmapUtils configThreadPoolSize(int poolSize) {
        globalConfig.setThreadPoolSize(poolSize);
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

    public BitmapUtils configDiskCacheFileNameGenerator(LruDiskCache.DiskCacheFileNameGenerator diskCacheFileNameGenerator) {
        globalConfig.setDiskCacheFileNameGenerator(diskCacheFileNameGenerator);
        return this;
    }

    public BitmapUtils configGlobalConfig(BitmapGlobalConfig globalConfig) {
        this.globalConfig = globalConfig;
        return this;
    }

    ////////////////////////// display ////////////////////////////////////

    public void display(ImageView imageView, String uri) {
        display(imageView, uri, null);
    }

    public void display(ImageView imageView, String uri, BitmapDisplayConfig displayConfig) {
        if (imageView == null) {
            return;
        }

        if (displayConfig == null) {
            displayConfig = defaultDisplayConfig;
        }

        if (TextUtils.isEmpty(uri)) {
            displayConfig.getImageLoadCallBack().loadFailed(imageView, displayConfig.getLoadFailedBitmap());
            return;
        }

        Bitmap bitmap = null;

        bitmap = globalConfig.getBitmapCache().getBitmapFromMemCache(uri, displayConfig);

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
            loadTask.executeOnExecutor(globalConfig.getBitmapLoadExecutor(), uri);
        }
    }

    /////////////////////////////////////////////// cache /////////////////////////////////////////////////////////////////

    public void clearCache() {
        globalConfig.clearCache();
    }

    public void clearMemoryCache() {
        globalConfig.clearMemoryCache();
    }

    public void clearDiskCache() {
        globalConfig.clearDiskCache();
    }

    public void clearCache(String uri, BitmapDisplayConfig config) {
        if (config == null) {
            config = defaultDisplayConfig;
        }
        globalConfig.clearCache(uri, config);
    }

    public void clearMemoryCache(String uri, BitmapDisplayConfig config) {
        if (config == null) {
            config = defaultDisplayConfig;
        }
        globalConfig.clearMemoryCache(uri, config);
    }

    public void clearDiskCache(String uri) {
        globalConfig.clearDiskCache(uri);
    }

    public void flushCache() {
        globalConfig.flushCache();
    }

    public void closeCache() {
        globalConfig.closeCache();
    }

    public Bitmap getBitmapFromMemCache(String uri, BitmapDisplayConfig displayConfig) {
        return globalConfig.getBitmapCache().getBitmapFromMemCache(uri, displayConfig);
    }

    ////////////////////////////////////////// tasks //////////////////////////////////////////////////////////////////////

    public void resumeTasks() {
        pauseTask = false;
        synchronized (pauseTaskLock) {
            pauseTaskLock.notifyAll();
        }
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

    private static boolean bitmapLoadTaskExist(ImageView imageView, String uri) {
        final BitmapLoadTask oldLoadTask = getBitmapTaskFromImageView(imageView);

        if (oldLoadTask != null) {
            final String oldUri = oldLoadTask.uri;
            if (TextUtils.isEmpty(oldUri) || !oldUri.equals(uri)) {
                oldLoadTask.cancel(true);
            } else {
                return true;
            }
        }
        return false;
    }

    private class AsyncBitmapDrawable extends BitmapDrawable {

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
        private String uri;
        private final WeakReference<ImageView> targetImageViewReference;
        private final BitmapDisplayConfig displayConfig;

        public BitmapLoadTask(ImageView imageView, BitmapDisplayConfig config) {
            targetImageViewReference = new WeakReference<ImageView>(imageView);
            displayConfig = config;
        }

        @Override
        protected Bitmap doInBackground(Object... params) {
            if (params != null && params.length > 0) {
                uri = (String) params[0];
            } else {
                return null;
            }
            Bitmap bitmap = null;

            synchronized (pauseTaskLock) {
                while (pauseTask && !this.isCancelled()) {
                    try {
                        pauseTaskLock.wait();
                    } catch (InterruptedException e) {
                    }
                }
            }

            // get cache from disk cache
            if (!pauseTask && !this.isCancelled() && this.getTargetImageView() != null) {
                bitmap = globalConfig.getBitmapCache().getBitmapFromDiskCache(uri, displayConfig);
            }

            // download image
            if (bitmap == null && !pauseTask && !this.isCancelled() && this.getTargetImageView() != null) {
                bitmap = globalConfig.getBitmapCache().downloadBitmap(uri, displayConfig);
            }

            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (isCancelled() || pauseTask) {
                bitmap = null;
            }

            final ImageView imageView = this.getTargetImageView();
            if (imageView != null) {
                if (bitmap != null) {
                    displayConfig.getImageLoadCallBack().loadCompleted(imageView, bitmap, displayConfig);
                } else {
                    displayConfig.getImageLoadCallBack().loadFailed(imageView, displayConfig.getLoadFailedBitmap());
                }
            }
        }

        @Override
        protected void onCancelled(Bitmap bitmap) {
            super.onCancelled(bitmap);
            synchronized (pauseTaskLock) {
                pauseTaskLock.notifyAll();
            }
        }

        private ImageView getTargetImageView() {
            final ImageView imageView = targetImageViewReference.get();
            final BitmapLoadTask bitmapWorkerTask = getBitmapTaskFromImageView(imageView);

            if (this == bitmapWorkerTask) {
                return imageView;
            }

            return null;
        }
    }
}
