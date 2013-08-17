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

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import com.lidroid.xutils.bitmap.BitmapGlobalConfig;
import com.lidroid.xutils.util.LogUtils;
import com.lidroid.xutils.util.core.LruDiskCache;
import com.lidroid.xutils.util.core.LruMemoryCache;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class BitmapCache {

    private static final int DISK_CACHE_INDEX = 0;

    private LruDiskCache mDiskLruCache;
    private LruMemoryCache<String, Bitmap> mMemoryCache;

    private final Object mDiskCacheLock = new Object();
    private boolean isDiskCacheReadied = false;

    private BitmapGlobalConfig mConfig;

    /**
     * Creating a new ImageCache object using the specified parameters.
     *
     * @param config The cache parameters to use to initialize the cache
     */
    public BitmapCache(BitmapGlobalConfig config) {
        this.mConfig = config;
    }


    /**
     * Initialize the memory cache
     */
    public void initMemoryCache() {
        // Set up memory cache
        if (mConfig.isMemoryCacheEnabled()) {
            if (mMemoryCache != null) {
                try {
                    clearMemoryCache();
                } catch (Exception e) {
                }
            }
            mMemoryCache = new LruMemoryCache<String, Bitmap>(mConfig.getMemoryCacheSize()) {
                /**
                 * Measure item size in bytes rather than units which is more practical
                 * for a bitmap cache
                 */
                @Override
                protected int sizeOf(String key, Bitmap bitmap) {
                    return BitmapCommonUtils.getBitmapSize(bitmap);
                }
            };
        }
    }

    /**
     * Initializes the disk cache.  Note that this includes disk access so this should not be
     * executed on the main/UI thread. By default an ImageCache does not initialize the disk
     * cache when it is created, instead you should call initDiskCache() to initialize it on a
     * background thread.
     */
    public void initDiskCache() {
        // Set up disk cache
        synchronized (mDiskCacheLock) {
            if (mDiskLruCache == null || mDiskLruCache.isClosed()) {
                File diskCacheDir = new File(mConfig.getDiskCachePath());
                if (mConfig.isDiskCacheEnabled() && diskCacheDir != null) {
                    if (!diskCacheDir.exists()) {
                        diskCacheDir.mkdirs();
                    }
                    if (BitmapCommonUtils.getAvailableSpace(diskCacheDir) > mConfig.getDiskCacheSize()) {
                        try {
                            mDiskLruCache = LruDiskCache.open(diskCacheDir, 1, 1, mConfig.getDiskCacheSize());
                        } catch (final IOException e) {
                            LogUtils.e(e.getMessage(), e);
                        }
                    }
                }
            }
            isDiskCacheReadied = true;
            mDiskCacheLock.notifyAll();
        }
    }

    public void setMemoryCacheSize(int maxSize) {
        if (mMemoryCache != null) {
            mMemoryCache.setMaxSize(maxSize);
        }
    }

    public void setDiskCacheSize(int maxSize) {
        if (mDiskLruCache != null) {
            mDiskLruCache.setMaxSize(maxSize);
        }
    }

    /**
     * Adds a bitmap to both memory and disk cache.
     *
     * @param uri            Unique identifier for the bitmap to store
     * @param bitmap         The bitmap to store
     * @param compressFormat if null, use default value.
     */
    public void addBitmapToCache(String uri, Bitmap bitmap, CompressFormat compressFormat) {
        if (uri == null || bitmap == null) {
            return;
        }

        // add to memory cache
        if (mMemoryCache != null && mMemoryCache.get(uri) == null) {
            mMemoryCache.put(uri, bitmap);
        }

        // add to disk cache
        synchronized (mDiskCacheLock) {
            if (mDiskLruCache != null && mDiskLruCache.getDirectory() != null) {

                if (!mDiskLruCache.getDirectory().exists()) {
                    mDiskLruCache.getDirectory().mkdirs();
                }

                OutputStream out = null;
                try {
                    LruDiskCache.Snapshot snapshot = mDiskLruCache.get(uri);
                    if (snapshot == null) {
                        final LruDiskCache.Editor editor = mDiskLruCache.edit(uri);
                        if (editor != null) {
                            out = editor.newOutputStream(DISK_CACHE_INDEX);
                            CompressFormat format = compressFormat == null ? mConfig.getDefaultCompressFormat() : compressFormat;
                            bitmap.compress(format, mConfig.getDefaultCompressQuality(), out);
                            editor.commit();
                            out.close();
                        }
                    } else {
                        snapshot.getInputStream(DISK_CACHE_INDEX).close();
                    }
                } catch (final IOException e) {
                    LogUtils.e(e.getMessage(), e);
                } catch (Exception e) {
                    LogUtils.e(e.getMessage(), e);
                } finally {
                    try {
                        if (out != null) {
                            out.close();
                        }
                    } catch (IOException e) {
                    }
                }
            }
        }
    }

    /**
     * Get from memory cache.
     *
     * @param uri Unique identifier for which item to get
     * @return The bitmap if found in cache, null otherwise
     */
    public Bitmap getBitmapFromMemCache(String uri) {
        if (mMemoryCache != null) {
            final Bitmap memBitmap = mMemoryCache.get(uri);
            if (memBitmap != null) {
                return memBitmap;
            }
        }
        return null;
    }


    /**
     * 获取硬盘缓存
     *
     * @param uri
     * @return
     */
    public Bitmap getBitmapFromDiskCache(String uri) {
        synchronized (mDiskCacheLock) {
            while (!isDiskCacheReadied) {
                try {
                    mDiskCacheLock.wait();
                } catch (InterruptedException e) {
                }
            }
            if (mDiskLruCache != null) {
                InputStream inputStream = null;
                try {
                    final LruDiskCache.Snapshot snapshot = mDiskLruCache.get(uri);
                    if (snapshot != null) {
                        inputStream = snapshot.getInputStream(DISK_CACHE_INDEX);
                        if (inputStream != null) {
                            final Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            return bitmap;
                        }
                    }
                } catch (final IOException e) {
                    LogUtils.e(e.getMessage(), e);
                } finally {
                    try {
                        if (inputStream != null) {
                            inputStream.close();
                        }
                    } catch (IOException e) {
                    }
                }
            }
            return null;
        }
    }

    /**
     * Clears both the memory and disk cache associated with this ImageCache object. Note that
     * this includes disk access so this should not be executed on the main/UI thread.
     */
    public void clearCache() {
        clearMemoryCache();
        clearDiskCache();
    }

    public void clearDiskCache() {
        synchronized (mDiskCacheLock) {
            if (mDiskLruCache != null && !mDiskLruCache.isClosed()) {
                try {
                    mDiskLruCache.delete();
                } catch (IOException e) {
                    LogUtils.e(e.getMessage(), e);
                }
                mDiskLruCache = null;
                isDiskCacheReadied = false;
            }
        }
        initDiskCache();
    }

    public void clearMemoryCache() {
        if (mMemoryCache != null) {
            mMemoryCache.evictAll();
        }
    }

    public void clearCache(String uri) {
        clearMemoryCache(uri);
        clearDiskCache(uri);
    }

    public void clearDiskCache(String uri) {
        synchronized (mDiskCacheLock) {
            if (mDiskLruCache != null && !mDiskLruCache.isClosed()) {
                try {
                    mDiskLruCache.remove(uri);
                } catch (IOException e) {
                    LogUtils.e(e.getMessage(), e);
                }
            }
        }
    }

    public void clearMemoryCache(String uri) {
        if (mMemoryCache != null) {
            mMemoryCache.remove(uri);
        }
    }

    /**
     * Flushes the disk cache associated with this ImageCache object. Note that this includes
     * disk access so this should not be executed on the main/UI thread.
     */
    public void flush() {
        synchronized (mDiskCacheLock) {
            if (mDiskLruCache != null) {
                try {
                    mDiskLruCache.flush();
                } catch (IOException e) {
                    LogUtils.e(e.getMessage(), e);
                }
            }
        }
    }

    /**
     * Closes the disk cache associated with this ImageCache object. Note that this includes
     * disk access so this should not be executed on the main/UI thread.
     */
    public void close() {
        synchronized (mDiskCacheLock) {
            if (mDiskLruCache != null) {
                try {
                    if (!mDiskLruCache.isClosed()) {
                        mDiskLruCache.close();
                        mDiskLruCache = null;
                    }
                } catch (IOException e) {
                    LogUtils.e(e.getMessage(), e);
                }
            }
        }
    }
}
