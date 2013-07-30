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

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;


public class SimpleHttpDownloader implements Downloader {

    private static final int IO_BUFFER_SIZE = 8 * 1024; //8k

    /**
     * 把网络或本地图片下载到文件的 outputStream
     *
     * @param urlString
     * @param outputStream
     * @return
     */
    public boolean downloadToLocalStreamByUrl(String urlString, OutputStream outputStream) {
        URLConnection urlConnection = null;
        BufferedOutputStream out = null;
        FlushedInputStream in = null;

        try {
            if (urlString.startsWith("/")) {
                FileInputStream fileInputStream = new FileInputStream(urlString);
                in = new FlushedInputStream(new BufferedInputStream(fileInputStream, IO_BUFFER_SIZE));
            } else {
                final URL url = new URL(urlString);
                urlConnection = url.openConnection();
                in = new FlushedInputStream(new BufferedInputStream(urlConnection.getInputStream(), IO_BUFFER_SIZE));
            }
            out = new BufferedOutputStream(outputStream, IO_BUFFER_SIZE);

            int b;
            while ((b = in.read()) != -1) {
                out.write(b);
            }
            return true;
        } catch (Exception e) {
            LogUtils.e(e.getMessage(), e);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    LogUtils.e(e.getMessage(), e);
                }
            }
            if (in != null) {
                try {
                    in.close();
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


    public class FlushedInputStream extends FilterInputStream {

        public FlushedInputStream(InputStream inputStream) {
            super(inputStream);
        }

        @Override
        public long skip(long n) throws IOException {
            long totalBytesSkipped = 0L;
            while (totalBytesSkipped < n) {
                long bytesSkipped = in.skip(n - totalBytesSkipped);
                if (bytesSkipped == 0L) {
                    int by_te = read();
                    if (by_te < 0) {
                        break; // we reached EOF
                    } else {
                        bytesSkipped = 1; // we read one byte
                    }
                }
                totalBytesSkipped += bytesSkipped;
            }
            return totalBytesSkipped;
        }
    }
}
