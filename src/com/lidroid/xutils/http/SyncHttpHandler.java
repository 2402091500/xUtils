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

import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.client.ResponseStream;
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

    private int retriedTimes = 0;

    private String charset;

    public SyncHttpHandler(AbstractHttpClient client, HttpContext context, String charset) {
        this.client = client;
        this.context = context;
        this.charset = charset;
    }

    private ResponseStream doSendRequest(HttpRequestBase request) throws HttpException {

        boolean retry = true;
        HttpException httpException = null;
        HttpRequestRetryHandler retryHandler = client.getHttpRequestRetryHandler();
        while (retry) {
            try {
                HttpResponse response = client.execute(request, context);
                return new ResponseStream(response, charset);
            } catch (UnknownHostException e) {
                httpException = new HttpException(e);
                retry = retryHandler.retryRequest(e, ++retriedTimes, context);
            } catch (IOException e) {
                httpException = new HttpException(e);
                retry = retryHandler.retryRequest(e, ++retriedTimes, context);
            } catch (NullPointerException e) {
                httpException = new HttpException(e);
                retry = retryHandler.retryRequest(new IOException(e), ++retriedTimes, context);
            } catch (Exception e) {
                httpException = new HttpException(e);
                retry = retryHandler.retryRequest(new IOException(e), ++retriedTimes, context);
            }
        }
        if (httpException != null) {
            throw httpException;
        } else {
            throw new HttpException("UNKNOWN ERROR");
        }

    }

    public ResponseStream sendRequest(HttpRequestBase... params) throws HttpException {
        return doSendRequest(params[0]);
    }
}
