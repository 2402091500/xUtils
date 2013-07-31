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

import com.lidroid.xutils.util.LogUtils;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;


public class SimpleDownloader implements Downloader {

    /**
     * 把网络或本地图片下载到文件的 outputStream
     *
     * @param urlString
     * @param outputStream
     * @return
     */
    public boolean downloadToLocalStreamByUrl(String urlString, OutputStream outputStream) {
        URLConnection urlConnection = null;
        BufferedInputStream ins = null;

        try {
            if (urlString.startsWith("/")) {
                FileInputStream fileInputStream = new FileInputStream(urlString);
                ins = new BufferedInputStream(fileInputStream);
            } else {
                final URL url = new URL(urlString);
                urlConnection = url.openConnection();
                ins = new BufferedInputStream(urlConnection.getInputStream());
            }

            byte[] buffer = new byte[4096];
            int len = 0;
            while ((len = ins.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
            }
            return true;
        } catch (Exception e) {
            LogUtils.e(e.getMessage(), e);
        } finally {
            if (ins != null) {
                try {
                    ins.close();
                } catch (IOException e) {
                    LogUtils.e(e.getMessage(), e);
                }
            }
            if (urlConnection != null) {
                if (urlConnection instanceof HttpURLConnection) {
                    ((HttpURLConnection) urlConnection).disconnect();
                }
            }
        }
        return false;
    }
}
