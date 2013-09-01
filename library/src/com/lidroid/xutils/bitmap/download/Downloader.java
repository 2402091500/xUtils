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

package com.lidroid.xutils.bitmap.download;

import java.io.OutputStream;

public interface Downloader {

    /**
     * 把网络或本地图片下载到文件的 outputStream
     *
     * @param uri
     * @param outputStream
     * @return 图片过期时间点； 小于零，下载失败。
     */
    long downloadToOutStreamByUri(String uri, OutputStream outputStream);


    /**
     * 设置图片过期时长
     *
     * @param expiry
     */
    void setDefaultExpiry(long expiry);

    /**
     * 获取图片过期时长
     *
     * @return
     */
    long getDefaultExpiry();
}
