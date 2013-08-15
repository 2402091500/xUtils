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

package com.lidroid.xutils.util.core;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Author: wyouflf
 * Date: 13-8-1
 * Time: 上午11:25
 */
public class KeyExpiryMap<K, V> extends ConcurrentHashMap<K, Long> {
    public KeyExpiryMap(int initialCapacity, float loadFactor, int concurrencyLevel) {
        super(initialCapacity, loadFactor, concurrencyLevel);
    }

    public KeyExpiryMap(int initialCapacity) {
        super(initialCapacity);
    }

    public KeyExpiryMap() {
        super();
    }

    @Override
    public Long get(Object key) {
        if (this.containsKey(key)) {
            return super.get(key);
        } else {
            return null;
        }
    }

    @Override
    public Long put(K key, Long value) {
        if (this.containsKey(key)) {
            this.remove(key);
        }
        putTimeStamps.put(key, System.currentTimeMillis());
        return super.put(key, value);
    }

    @Override
    public boolean containsKey(Object key) {
        boolean result = false;
        if (super.containsKey(key) && putTimeStamps.containsKey(key)) {
            if (System.currentTimeMillis() - putTimeStamps.get(key) < super.get(key)) {
                result = true;
            } else {
                this.remove(key);
            }
        }
        return result;
    }

    @Override
    public Long remove(Object key) {
        putTimeStamps.remove(key);
        return super.remove(key);
    }

    @Override
    public void clear() {
        putTimeStamps.clear();
        super.clear();
    }

    private ConcurrentHashMap<K, Long> putTimeStamps = new ConcurrentHashMap<K, Long>();
}
