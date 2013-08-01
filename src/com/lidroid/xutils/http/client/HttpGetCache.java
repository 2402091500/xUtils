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

package com.lidroid.xutils.http.client;

import com.lidroid.xutils.util.KeyExpiryMap;
import com.lidroid.xutils.util.LruMemoryCache;

/**
 * Author: wyouflf
 * Date: 13-8-1
 * Time: 下午12:04
 */
public class HttpGetCache {

    /**
     * key: url
     * value: response result
     */
    private LruMemoryCache<String, String> mMemoryCache;

    public final static int DEFAULT_CACHE_SIZE = 1024 * 1024 * 1;// 1M
    public final static long DEFAULT_EXPIRY_TIME = 1000 * 60; // 60 seconds
    private final static long MIN_EXPIRY_TIME = 500;

    /**
     * key: url
     * value: expiry time
     */
    private KeyExpiryMap<String, Long> mUrlExpiryMap;

    private int cacheSize = DEFAULT_CACHE_SIZE;

    private long defaultExpiryTime = DEFAULT_EXPIRY_TIME;

    public HttpGetCache(int cacheSize, long defaultExpiryTime) {
        if (cacheSize > this.cacheSize) {
            this.cacheSize = cacheSize;
        }
        this.setDefaultExpiryTime(defaultExpiryTime);

        mMemoryCache = new LruMemoryCache<String, String>(cacheSize);
        mUrlExpiryMap = new KeyExpiryMap<String, Long>();
    }

    public void setCacheSize(int cacheSize) {
        this.clear();
        if (cacheSize > this.cacheSize) {
            this.cacheSize = cacheSize;
        }
        mMemoryCache = new LruMemoryCache<String, String>(cacheSize);
    }

    public void setDefaultExpiryTime(long defaultExpiryTime) {
        if (defaultExpiryTime > MIN_EXPIRY_TIME) {
            this.defaultExpiryTime = defaultExpiryTime;
        }
    }

    public void put(String url, String result) {
        put(url, result, defaultExpiryTime);
    }

    public void put(String url, String result, long expiry) {
        if (url == null || result == null) return;

        if (expiry < MIN_EXPIRY_TIME) {
            expiry = MIN_EXPIRY_TIME;
        }

        if (mMemoryCache != null) {
            mMemoryCache.remove(url);
            mMemoryCache.put(url, result);
            mUrlExpiryMap.put(url, expiry);
        }
    }

    public String get(String url) {
        if (mUrlExpiryMap.containsKey(url)) {
            return mMemoryCache.get(url);
        } else {
            mMemoryCache.remove(url);
        }
        return null;
    }

    public void clear() {
        mMemoryCache.evictAll();
        mUrlExpiryMap.clear();
    }

}
