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
package com.lidroid.xutils.http;

import com.lidroid.xutils.http.client.callback.StringDownloadHandler;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;
import java.net.UnknownHostException;

public class SyncHttpHandler {

    private final AbstractHttpClient client;
    private final HttpContext context;
    private final StringDownloadHandler mStringDownloadHandler = new StringDownloadHandler();

    private int executionCount = 0;
    private String charset;

    public SyncHttpHandler(AbstractHttpClient client, HttpContext context, String charset) {
        this.client = client;
        this.context = context;
        this.charset = charset;
    }

    private Object makeRequestWithRetries(HttpRequestBase request) throws IOException {

        boolean retry = true;
        IOException ioException = null;
        HttpRequestRetryHandler retryHandler = client.getHttpRequestRetryHandler();
        while (retry) {
            try {
                HttpResponse response = client.execute(request, context);
                return mStringDownloadHandler.handleEntity(response.getEntity(), null, charset);
            } catch (UnknownHostException e) {
                ioException = e;
                retry = retryHandler.retryRequest(ioException, ++executionCount, context);
            } catch (IOException e) {
                ioException = e;
                retry = retryHandler.retryRequest(ioException, ++executionCount, context);
            } catch (NullPointerException e) {
                ioException = new IOException("NPE in HttpClient" + e.getMessage());
                retry = retryHandler.retryRequest(ioException, ++executionCount, context);
            } catch (Exception e) {
                ioException = new IOException("Exception" + e.getMessage());
                retry = retryHandler.retryRequest(ioException, ++executionCount, context);
            }
        }
        if (ioException != null) {
            throw ioException;
        } else {
            throw new IOException("未知网络错误");
        }

    }

    public Object sendRequest(HttpRequestBase... params) {
        try {
            return makeRequestWithRetries(params[0]);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
