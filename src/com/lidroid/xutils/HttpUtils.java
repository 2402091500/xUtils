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

import com.lidroid.xutils.http.HttpHandler;
import com.lidroid.xutils.http.RequestCallBack;
import com.lidroid.xutils.http.RetryHandler;
import com.lidroid.xutils.http.SyncHttpHandler;
import com.lidroid.xutils.http.client.HttpRequest;
import com.lidroid.xutils.http.client.RequestParams;
import com.lidroid.xutils.http.client.callback.DownloadRedirectHandler;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;

import java.io.File;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class HttpUtils {

    private final DefaultHttpClient httpClient = new DefaultHttpClient();
    private final HttpContext httpContext = new BasicHttpContext();

    public HttpUtils() {
        httpClient.setHttpRequestRetryHandler(new RetryHandler(DEFAULT_RETRY_TIMES));
    }

    // ************************************    default settings & fields ****************************

    private String charset = HTTP.UTF_8;

    private final static int DEFAULT_RETRY_TIMES = 5;

    private static int httpThreadCount = 3;

    private static final ThreadFactory sThreadFactory = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);

        public Thread newThread(Runnable r) {
            Thread tread = new Thread(r, "HttpUtils #" + mCount.getAndIncrement());
            tread.setPriority(Thread.NORM_PRIORITY - 1);
            return tread;
        }
    };

    private static final Executor executor = Executors.newFixedThreadPool(httpThreadCount, sThreadFactory);

    public HttpClient getHttpClient() {
        return this.httpClient;
    }

    // ***************************************** config *******************************************

    public void configCharset(String charSet) {
        if (charSet != null && charSet.trim().length() != 0) {
            this.charset = charSet;
        }
    }

    public void configCookieStore(CookieStore cookieStore) {
        httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
    }

    public void configUserAgent(String userAgent) {
        HttpProtocolParams.setUserAgent(this.httpClient.getParams(), userAgent);
    }

    public void configTimeout(int timeout) {
        final HttpParams httpParams = this.httpClient.getParams();
        ConnManagerParams.setTimeout(httpParams, timeout);
        HttpConnectionParams.setSoTimeout(httpParams, timeout);
        HttpConnectionParams.setConnectionTimeout(httpParams, timeout);
    }

    public void configSSLSocketFactory(SSLSocketFactory sslSocketFactory) {
        Scheme scheme = new Scheme("https", sslSocketFactory, 443);
        this.httpClient.getConnectionManager().getSchemeRegistry().register(scheme);
    }

    public void configRequestExecutionRetryCount(int count) {
        this.httpClient.setHttpRequestRetryHandler(new RetryHandler(count));
    }

    // ***************************************** send request *******************************************

    public HttpHandler send(HttpRequest.HttpMethod method, String url,
                            RequestCallBack<? extends Object> callBack) {
        return send(method, url, null, callBack);
    }

    public HttpHandler send(HttpRequest.HttpMethod method, String url, RequestParams params,
                            RequestCallBack<? extends Object> callBack) {
        return send(method, url, params, null, callBack);
    }

    public HttpHandler send(HttpRequest.HttpMethod method, String url, RequestParams params, String contentType,
                            RequestCallBack<? extends Object> callBack) {
        HttpRequest request = new HttpRequest(method, url);
        return sendRequest(request, params, contentType, callBack);
    }

    public Object sendSync(HttpRequest.HttpMethod method, String url) {
        return sendSync(method, url, null);
    }

    public Object sendSync(HttpRequest.HttpMethod method, String url, RequestParams params) {
        return sendSync(method, url, params, null);
    }

    public Object sendSync(HttpRequest.HttpMethod method, String url, RequestParams params, String contentType) {
        HttpRequest request = new HttpRequest(method, url);
        return sendSyncRequest(request, params, contentType);
    }

    // ***************************************** download *******************************************

    public HttpHandler<File> download(String url, String target,
                                      RequestCallBack<File> callback) {
        return download(url, null, target, false, null, callback);
    }

    public HttpHandler<File> download(String url, String target,
                                      DownloadRedirectHandler downloadRedirectHandler, RequestCallBack<File> callback) {
        return download(url, null, target, false, downloadRedirectHandler, callback);
    }

    public HttpHandler<File> download(String url, RequestParams params, String target,
                                      RequestCallBack<File> callback) {
        return download(url, params, target, false, null, callback);
    }

    public HttpHandler<File> download(String url, RequestParams params, String target,
                                      DownloadRedirectHandler downloadRedirectHandler, RequestCallBack<File> callback) {
        return download(url, params, target, false, downloadRedirectHandler, callback);
    }

    public HttpHandler<File> download(String url, String target, boolean isResume,
                                      RequestCallBack<File> callback) {
        return download(url, null, target, isResume, null, callback);
    }

    public HttpHandler<File> download(String url, String target, boolean isResume,
                                      DownloadRedirectHandler downloadRedirectHandler, RequestCallBack<File> callback) {
        return download(url, null, target, isResume, downloadRedirectHandler, callback);
    }

    public HttpHandler<File> download(String url, RequestParams params, String target, boolean isResume,
                                      RequestCallBack<File> callback) {
        return download(url, params, target, isResume, null, callback);
    }

    public HttpHandler<File> download(String url, RequestParams params, String target, boolean isResume,
                                      DownloadRedirectHandler downloadRedirectHandler, RequestCallBack<File> callback) {
        HttpRequest request = new HttpRequest(HttpRequest.HttpMethod.GET, url);
        HttpHandler<File> handler = new HttpHandler<File>(httpClient, httpContext, charset, callback);
        request.setRequestParams(params, handler);
        handler.setDownloadRedirectHandler(downloadRedirectHandler);
        handler.executeOnExecutor(executor, request, target, isResume);
        return handler;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    private <T> HttpHandler<T> sendRequest(HttpRequest request, RequestParams params, String contentType, RequestCallBack<T> callBack) {
        if (contentType != null) {
            request.addHeader("Content-Type", contentType);
        }

        HttpHandler<T> handler = new HttpHandler<T>(httpClient, httpContext, charset, callBack);

        request.setRequestParams(params, handler);

        handler.executeOnExecutor(executor, request);
        return handler;
    }

    private Object sendSyncRequest(HttpRequest request, RequestParams params, String contentType) {
        if (contentType != null) {
            request.addHeader("Content-Type", contentType);
        }
        request.setRequestParams(params);
        return new SyncHttpHandler(httpClient, httpContext, charset).sendRequest(request);
    }
}
