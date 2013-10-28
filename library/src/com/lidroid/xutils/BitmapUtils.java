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
import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.animation.Animation;
import android.widget.ImageView;
import com.lidroid.xutils.bitmap.BitmapCacheListener;
import com.lidroid.xutils.bitmap.BitmapDisplayConfig;
import com.lidroid.xutils.bitmap.BitmapGlobalConfig;
import com.lidroid.xutils.bitmap.callback.ImageLoadCallBack;
import com.lidroid.xutils.bitmap.download.Downloader;
import com.lidroid.xutils.util.core.CompatibleAsyncTask;
import com.lidroid.xutils.util.core.LruDiskCache;

import java.io.File;
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

    public BitmapUtils configDefaultLoadingImage(Drawable bitmap) {
        defaultDisplayConfig.setLoadingDrawable(bitmap);
        return this;
    }

    public BitmapUtils configDefaultLoadingImage(int resId) {
        defaultDisplayConfig.setLoadingDrawable(context.getResources().getDrawable(resId));
        return this;
    }

    public BitmapUtils configDefaultLoadFailedImage(Drawable bitmap) {
        defaultDisplayConfig.setLoadFailedDrawable(bitmap);
        return this;
    }

    public BitmapUtils configDefaultLoadFailedImage(int resId) {
        defaultDisplayConfig.setLoadFailedDrawable(context.getResources().getDrawable(resId));
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

    public BitmapUtils configThreadPoolSize(int threadPoolSize) {
        globalConfig.setThreadPoolSize(threadPoolSize);
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

    public BitmapUtils configBitmapCacheListener(BitmapCacheListener listener) {
        globalConfig.setBitmapCacheListener(listener);
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
            displayConfig.getImageLoadCallBack().loadFailed(uri, imageView, displayConfig.getLoadFailedDrawable());
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
                    displayConfig.getLoadingDrawable(),
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

    public File getBitmapFileFromDiskCache(String uri) {
        return globalConfig.getBitmapCache().getBitmapFileFromDiskCache(uri);
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

    private class AsyncBitmapDrawable extends Drawable {

        private final WeakReference<BitmapLoadTask> bitmapLoadTaskReference;

        private final Drawable baseDrawable;

        public AsyncBitmapDrawable(Drawable drawable, BitmapLoadTask bitmapWorkerTask) {
            if (drawable == null) {
                throw new IllegalArgumentException("drawable may not be null");
            }
            if (bitmapWorkerTask == null) {
                throw new IllegalArgumentException("bitmapWorkerTask may not be null");
            }
            baseDrawable = drawable;
            bitmapLoadTaskReference = new WeakReference<BitmapLoadTask>(bitmapWorkerTask);
        }

        public BitmapLoadTask getBitmapWorkerTask() {
            return bitmapLoadTaskReference.get();
        }

        @Override
        public void draw(Canvas canvas) {
            baseDrawable.draw(canvas);
        }

        @Override
        public void setAlpha(int i) {
            baseDrawable.setAlpha(i);
        }

        @Override
        public void setColorFilter(ColorFilter colorFilter) {
            baseDrawable.setColorFilter(colorFilter);
        }

        @Override
        public int getOpacity() {
            return baseDrawable.getOpacity();
        }

        @Override
        public void setBounds(int left, int top, int right, int bottom) {
            baseDrawable.setBounds(left, top, right, bottom);
        }

        @Override
        public void setBounds(Rect bounds) {
            baseDrawable.setBounds(bounds);
        }

        @Override
        public void setChangingConfigurations(int configs) {
            baseDrawable.setChangingConfigurations(configs);
        }

        @Override
        public int getChangingConfigurations() {
            return baseDrawable.getChangingConfigurations();
        }

        @Override
        public void setDither(boolean dither) {
            baseDrawable.setDither(dither);
        }

        @Override
        public void setFilterBitmap(boolean filter) {
            baseDrawable.setFilterBitmap(filter);
        }

        @Override
        public void invalidateSelf() {
            baseDrawable.invalidateSelf();
        }

        @Override
        public void scheduleSelf(Runnable what, long when) {
            baseDrawable.scheduleSelf(what, when);
        }

        @Override
        public void unscheduleSelf(Runnable what) {
            baseDrawable.unscheduleSelf(what);
        }

        @Override
        public void setColorFilter(int color, PorterDuff.Mode mode) {
            baseDrawable.setColorFilter(color, mode);
        }

        @Override
        public void clearColorFilter() {
            baseDrawable.clearColorFilter();
        }

        @Override
        public boolean isStateful() {
            return baseDrawable.isStateful();
        }

        @Override
        public boolean setState(int[] stateSet) {
            return baseDrawable.setState(stateSet);
        }

        @Override
        public int[] getState() {
            return baseDrawable.getState();
        }

        @Override
        public Drawable getCurrent() {
            return baseDrawable.getCurrent();
        }

        @Override
        public boolean setVisible(boolean visible, boolean restart) {
            return baseDrawable.setVisible(visible, restart);
        }

        @Override
        public Region getTransparentRegion() {
            return baseDrawable.getTransparentRegion();
        }

        @Override
        public int getIntrinsicWidth() {
            return baseDrawable.getIntrinsicWidth();
        }

        @Override
        public int getIntrinsicHeight() {
            return baseDrawable.getIntrinsicHeight();
        }

        @Override
        public int getMinimumWidth() {
            return baseDrawable.getMinimumWidth();
        }

        @Override
        public int getMinimumHeight() {
            return baseDrawable.getMinimumHeight();
        }

        @Override
        public boolean getPadding(Rect padding) {
            return baseDrawable.getPadding(padding);
        }

        @Override
        public Drawable mutate() {
            return baseDrawable.mutate();
        }

        @Override
        public ConstantState getConstantState() {
            return baseDrawable.getConstantState();
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
                    } catch (Throwable e) {
                    }
                }
            }

            // get cache from disk cache
            if (!this.isCancelled() && this.getTargetImageView() != null) {
                bitmap = globalConfig.getBitmapCache().getBitmapFromDiskCache(uri, displayConfig);
            }

            // download image
            if (bitmap == null && !this.isCancelled() && this.getTargetImageView() != null) {
                bitmap = globalConfig.getBitmapCache().downloadBitmap(uri, displayConfig);
            }

            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            final ImageView imageView = this.getTargetImageView();
            if (imageView != null) {
                if (bitmap != null) {
                    displayConfig.getImageLoadCallBack().loadCompleted(
                            this.uri,
                            imageView,
                            new BitmapDrawable(context.getResources(), bitmap),
                            displayConfig);
                } else {
                    displayConfig.getImageLoadCallBack().loadFailed(
                            this.uri,
                            imageView,
                            displayConfig.getLoadFailedDrawable());
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
