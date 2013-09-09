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
import android.graphics.BitmapFactory;
import com.lidroid.xutils.bitmap.BitmapDisplayConfig;
import com.lidroid.xutils.bitmap.BitmapGlobalConfig;
import com.lidroid.xutils.util.IOUtils;
import com.lidroid.xutils.util.LogUtils;
import com.lidroid.xutils.util.core.LruDiskCache;

import java.io.*;

public class BitmapDownloadProcess {
    private boolean isOriginalDiskCacheReadied = false;

    private LruDiskCache mOriginalDiskCache;//原始图片的路径，不进行任何的压缩操作
    private final Object mOriginalDiskCacheLock = new Object();
    private static final int ORIGINAL_DISK_CACHE_INDEX = 0;

    private File mOriginalCacheDir;

    private boolean neverCalculate = false;

    private BitmapGlobalConfig config;

    public BitmapDownloadProcess(BitmapGlobalConfig config) {
        this.config = config;
        this.mOriginalCacheDir = new File(config.getDiskCachePath() + "/original");
    }

    public void setOriginalDiskCacheSize(int originalDiskCacheSize) {
        if (mOriginalDiskCache != null) {
            mOriginalDiskCache.setMaxSize(originalDiskCacheSize);
        }
    }

    public void neverCalculate(boolean neverCalculate) {
        this.neverCalculate = neverCalculate;
    }

    public BitmapResult downloadBitmap(String uri, BitmapDisplayConfig displayConfig) {

        BitmapResult result = new BitmapResult();

        OutputStream outputStream = null;
        LruDiskCache.Snapshot snapshot = null;

        try {
            if (config.isDiskCacheEnabled()) {
                FileDescriptor fileDescriptor = null;
                synchronized (mOriginalDiskCacheLock) {
                    // Wait for disk cache to initialize
                    while (!isOriginalDiskCacheReadied) {
                        try {
                            mOriginalDiskCacheLock.wait();
                        } catch (InterruptedException e) {
                        }
                    }


                    if (mOriginalDiskCache != null) {
                        snapshot = mOriginalDiskCache.get(uri);
                        if (snapshot == null) {
                            LruDiskCache.Editor editor = mOriginalDiskCache.edit(uri);
                            if (editor != null) {
                                outputStream = editor.newOutputStream(ORIGINAL_DISK_CACHE_INDEX);
                                result.expiryTimestamp = config.getDownloader().downloadToOutStreamByUri(uri, outputStream);
                                if (result.expiryTimestamp < 0) {
                                    editor.abort();
                                } else {
                                    editor.setEntryExpiryTimestamp(result.expiryTimestamp);
                                    editor.commit();
                                }
                                snapshot = mOriginalDiskCache.get(uri);
                            }
                        }
                        if (snapshot != null) {
                            fileDescriptor = snapshot.getInputStream(ORIGINAL_DISK_CACHE_INDEX).getFD();
                        }
                    }
                }
                if (fileDescriptor != null) {
                    if (neverCalculate) {
                        result.bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor);
                    } else {
                        result.bitmap = BitmapDecoder.decodeSampledBitmapFromDescriptor(fileDescriptor, displayConfig.getBitmapMaxWidth(), displayConfig.getBitmapMaxHeight());
                    }
                }
            }

            if (!config.isDiskCacheEnabled() || mOriginalDiskCache == null || result.bitmap == null) {
                outputStream = new ByteArrayOutputStream();
                result.expiryTimestamp = config.getDownloader().downloadToOutStreamByUri(uri, outputStream);
                byte[] data = ((ByteArrayOutputStream) outputStream).toByteArray();

                if (neverCalculate) {
                    result.bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                } else {
                    result.bitmap = BitmapDecoder.decodeSampledBitmapFromByteArray(data, displayConfig.getBitmapMaxWidth(), displayConfig.getBitmapMaxHeight());
                }
            }
        } catch (Exception e) {
            LogUtils.e(e.getMessage(), e);
        } finally {
            IOUtils.closeQuietly(outputStream);
            IOUtils.closeQuietly(snapshot);
        }

        return result;
    }

    public Bitmap getBitmapFromDiskCache(String uri) {
        synchronized (mOriginalDiskCacheLock) {
            while (!isOriginalDiskCacheReadied) {
                try {
                    mOriginalDiskCacheLock.wait();
                } catch (InterruptedException e) {
                }
            }
            if (mOriginalDiskCache != null) {
                InputStream inputStream = null;
                try {
                    final LruDiskCache.Snapshot snapshot = mOriginalDiskCache.get(uri);
                    if (snapshot != null) {
                        inputStream = snapshot.getInputStream(ORIGINAL_DISK_CACHE_INDEX);
                        if (inputStream != null) {
                            final Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            return bitmap;
                        }
                    }
                } catch (final IOException e) {
                    LogUtils.e(e.getMessage(), e);
                } finally {
                    IOUtils.closeQuietly(inputStream);
                }
            }
            return null;
        }
    }

    public void initOriginalDiskCache() {
        if (!config.isDiskCacheEnabled()) return;

        if (!mOriginalCacheDir.exists()) {
            mOriginalCacheDir.mkdirs();
        }

        synchronized (mOriginalDiskCacheLock) {
            if (BitmapCommonUtils.getAvailableSpace(mOriginalCacheDir) > config.getOriginalDiskCacheSize()) {
                try {
                    mOriginalDiskCache = LruDiskCache.open(mOriginalCacheDir, 1, 1, config.getOriginalDiskCacheSize());
                } catch (IOException e) {
                    mOriginalDiskCache = null;
                    LogUtils.e(e.getMessage(), e);
                }
            }
            isOriginalDiskCacheReadied = true;
            mOriginalDiskCacheLock.notifyAll();
        }
    }

    public void clearOriginalDiskCache() {
        synchronized (mOriginalDiskCacheLock) {
            if (mOriginalDiskCache != null && !mOriginalDiskCache.isClosed()) {
                try {
                    mOriginalDiskCache.delete();
                } catch (IOException e) {
                    LogUtils.e(e.getMessage(), e);
                }
                mOriginalDiskCache = null;
                isOriginalDiskCacheReadied = false;
            }
        }
        initOriginalDiskCache();
    }

    public void clearOriginalDiskCache(String uri) {
        synchronized (mOriginalDiskCacheLock) {
            if (mOriginalDiskCache != null && !mOriginalDiskCache.isClosed()) {
                try {
                    mOriginalDiskCache.remove(uri);
                } catch (IOException e) {
                    LogUtils.e(e.getMessage(), e);
                }
            }
        }
    }

    public void flushOriginalDiskCache() {
        synchronized (mOriginalDiskCacheLock) {
            if (mOriginalDiskCache != null) {
                try {
                    mOriginalDiskCache.flush();
                } catch (IOException e) {
                    LogUtils.e(e.getMessage(), e);
                }
            }
        }
    }

    public void closeOriginalDiskCache() {
        synchronized (mOriginalDiskCacheLock) {
            if (mOriginalDiskCache != null) {
                try {
                    if (!mOriginalDiskCache.isClosed()) {
                        mOriginalDiskCache.close();
                        mOriginalDiskCache = null;
                    }
                } catch (IOException e) {
                    LogUtils.e(e.getMessage(), e);
                }
            }
        }
    }

}
