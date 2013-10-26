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

import com.lidroid.xutils.util.IOUtils;
import com.lidroid.xutils.util.LogUtils;
import com.lidroid.xutils.util.OtherUtils;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;


public class SimpleDownloader implements Downloader {

    /**
     * Download bitmap to outputStream by uri.
     *
     * @param uri
     * @param outputStream
     * @return The expiry time stamp or -1 if failed to download.
     */
    @Override
    public long downloadToStream(String uri, OutputStream outputStream) {
        URLConnection urlConnection = null;
        BufferedInputStream bis = null;

        OtherUtils.trustAllSSLForHttpsURLConnection();

        long result = -1;
        try {
            if (uri.startsWith("/")) {
                FileInputStream fileInputStream = new FileInputStream(uri);
                bis = new BufferedInputStream(fileInputStream);
                result = System.currentTimeMillis() + getDefaultExpiry();
            } else {
                final URL url = new URL(uri);
                urlConnection = url.openConnection();
                urlConnection.setConnectTimeout(1000 * 15);
                urlConnection.setReadTimeout(1000 * 30);
                bis = new BufferedInputStream(urlConnection.getInputStream());
                result = urlConnection.getExpiration(); // 如果header中不包含expires返回0
                result = result < System.currentTimeMillis() ? System.currentTimeMillis() + getDefaultExpiry() : result;
            }

            byte[] buffer = new byte[4096];
            int len = 0;
            while ((len = bis.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
            }
            outputStream.flush();
        } catch (Throwable e) {
            result = -1;
            LogUtils.e(e.getMessage(), e);
        } finally {
            IOUtils.closeQuietly(bis);
        }
        return result;
    }

    private long defaultExpiry;

    @Override
    public void setDefaultExpiry(long expiry) {
        this.defaultExpiry = expiry;
    }

    @Override
    public long getDefaultExpiry() {
        return this.defaultExpiry;
    }
}
