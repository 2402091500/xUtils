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

import android.graphics.Bitmap;
import com.lidroid.xutils.util.core.KeyExpiryMap;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Author: wyouflf
 * Date: 13-8-13
 * Time: 下午2:35
 */
public class CacheUtils {

    /**
     * @param allowMemoryCache
     * @param diskCachePath    if the value is null, disk cache will be disabled.
     */
    public CacheUtils(boolean allowMemoryCache, String diskCachePath) {
        if (allowMemoryCache) {
            this.allowMemoryCache = true;
            memoryCache = new LinkedHashMap<Object, Object>(0, 0.75f, true);
        }
        if (diskCachePath != null && diskCachePath.length() > 0) {
            diskCacheFolder = new File(diskCachePath);
            if (!diskCacheFolder.exists()) {
                diskCacheFolder.mkdirs();
            }
            this.allowMemoryCache = true;
            initDiskCache();
        }
    }

    public void configMemoryCacheMaxSize(long memoryCacheMaxSize) {
        this.memoryCacheMaxSize = memoryCacheMaxSize;
    }

    public void configDiskCacheMaxSize(long diskCacheMaxSize) {
        this.diskCacheMaxSize = diskCacheMaxSize;
    }

    public void configMemoryDefaultCacheExpiryTime(long memoryDefaultCacheExpiryTime) {
        if (memoryDefaultCacheExpiryTime > MEMORY_MIN_EXPIRY_TIME) {
            this.memoryDefaultCacheExpiryTime = memoryDefaultCacheExpiryTime;
        }
    }

    public void configDiskDefaultCacheExpiryTime(long diskDefaultCacheExpiryTime) {
        if (diskDefaultCacheExpiryTime > DISK_MIN_EXPIRY_TIME) {
            this.diskDefaultCacheExpiryTime = diskDefaultCacheExpiryTime;
        }
    }

    //////////////////////////////////////// operations ///////////////////////////////////////////////////////////

    /**
     * only put to memory cache
     *
     * @param key
     * @param value
     */
    public void put(Object key, String value) {
        if (memoryCache.containsKey(key)) {
            memoryCache.remove(key);
        }
        memoryCache.put(key, value);
        while (getMemoryCacheSize() > memoryCacheMaxSize) {
            Map.Entry<Object, Object> toEvict = memoryCache.entrySet().iterator().next();
            memoryCache.remove(toEvict.getKey());
        }
    }

    public void put(Object key, File value) {
        memoryCache.put(key, value);
    }

    public void put(Object key, Bitmap value) {
        memoryCache.put(key, value);
    }

    public String getString(Object key) {
        return (String) memoryCache.get(key);
    }

    public File getFile(Object key) {
        return (File) memoryCache.get(key);
    }

    public Bitmap getBitmap(Object key) {
        return (Bitmap) memoryCache.get(key);
    }


    private void initDiskCache() {
        File[] files = diskCacheFolder.listFiles();
        diskCache = new LinkedHashMap<Object, File>(files.length, 0.75f, true);
        for (File file : files) {
            diskCache.put(file.getName(), file);
        }
    }

    private long getDiskCacheSize() {
        long result = 0;
        for (File file : diskCache.values()) {
            result += file.length();
        }
        return result;
    }

    // 估算内存大小，非准确值。
    private long getMemoryCacheSize() {
        long result = 0;
        for (Object value : memoryCache.values()) {
            if (value != null) {
                if (value instanceof String) {
                    result += ((CharSequence) value).length() * 8;
                } else if (value instanceof Bitmap) {
                    Bitmap bitmap = (Bitmap) value;
                    result += bitmap.getRowBytes() * bitmap.getHeight();
                } else if (value instanceof File) {
                    result += ((File) value).length();
                } else {
                    result += 8;
                }
            }
        }
        return result;
    }

    /////////////////////////////////////// private fields ////////////////////////////////////////////////////////////

    private boolean allowMemoryCache;
    private LinkedHashMap<Object, Object> memoryCache;
    private long memoryCacheMaxSize = MEMORY_DEFAULT_CACHE_SIZE;
    private long memoryDefaultCacheExpiryTime = MEMORY_DEFAULT_EXPIRY_TIME;

    private boolean allowDiskCache;
    private File diskCacheFolder;
    private LinkedHashMap<Object, File> diskCache;
    private long diskCacheMaxSize = DISK_DEFAULT_CACHE_SIZE;
    private long diskDefaultCacheExpiryTime = DISK_DEFAULT_EXPIRY_TIME;

    // const
    private final static long MEMORY_DEFAULT_CACHE_SIZE = 1024 * 1024 * 1;// 1M
    private final static long MEMORY_DEFAULT_EXPIRY_TIME = 1000 * 60; // 1 min
    private final static long MEMORY_MIN_EXPIRY_TIME = 500; //0.5 second
    private final static long DISK_DEFAULT_CACHE_SIZE = 1024 * 1024 * 20; // 20M
    private final static long DISK_DEFAULT_EXPIRY_TIME = Long.MAX_VALUE;
    private final static long DISK_MIN_EXPIRY_TIME = 1000 * 60; // 1 min

    /**
     * value: expiry time
     */
    private KeyExpiryMap<Object, Long> mUrlExpiryMap = new KeyExpiryMap<Object, Long>();
}
