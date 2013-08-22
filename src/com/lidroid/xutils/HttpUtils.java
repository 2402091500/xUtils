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

import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.HttpHandler;
import com.lidroid.xutils.http.RequestCallBack;
import com.lidroid.xutils.http.RetryHandler;
import com.lidroid.xutils.http.SyncHttpHandler;
import com.lidroid.xutils.http.client.HttpGetCache;
import com.lidroid.xutils.http.client.HttpRequest;
import com.lidroid.xutils.http.client.RequestParams;
import com.lidroid.xutils.http.client.ResponseStream;
import com.lidroid.xutils.http.client.callback.DownloadRedirectHandler;
import com.lidroid.xutils.http.client.entity.GZipDecompressingEntity;
import org.apache.http.*;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class HttpUtils {

    private final DefaultHttpClient httpClient;
    private final HttpContext httpContext = new BasicHttpContext();

    public final static HttpGetCache sHttpGetCache = new HttpGetCache();

    public DownloadRedirectHandler downloadRedirectHandler;

    public HttpUtils() {
        this(HttpUtils.DEFAULT_CONN_TIMEOUT);
    }

    public HttpUtils(int connTimeout) {
        HttpParams params = new BasicHttpParams();

        ConnManagerParams.setTimeout(params, connTimeout);
        HttpConnectionParams.setSoTimeout(params, connTimeout);
        HttpConnectionParams.setConnectionTimeout(params, connTimeout);

        ConnManagerParams.setMaxConnectionsPerRoute(params, new ConnPerRouteBean(10));
        ConnManagerParams.setMaxTotalConnections(params, 10);

        HttpConnectionParams.setTcpNoDelay(params, true);
        HttpConnectionParams.setSocketBufferSize(params, 1024 * 8);
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);

        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));

        httpClient = new DefaultHttpClient(new ThreadSafeClientConnManager(params, schemeRegistry), params);

        httpClient.setHttpRequestRetryHandler(new RetryHandler(DEFAULT_RETRY_TIMES));

        httpClient.addRequestInterceptor(new HttpRequestInterceptor() {
            @Override
            public void process(org.apache.http.HttpRequest httpRequest, HttpContext httpContext) throws org.apache.http.HttpException, IOException {
                if (!httpRequest.containsHeader(HEADER_ACCEPT_ENCODING)) {
                    httpRequest.addHeader(HEADER_ACCEPT_ENCODING, ENCODING_GZIP);
                }
            }
        });

        httpClient.addResponseInterceptor(new HttpResponseInterceptor() {
            @Override
            public void process(HttpResponse response, HttpContext httpContext) throws org.apache.http.HttpException, IOException {
                final HttpEntity entity = response.getEntity();
                if (entity == null) {
                    return;
                }
                final Header encoding = entity.getContentEncoding();
                if (encoding != null) {
                    for (HeaderElement element : encoding.getElements()) {
                        if (element.getName().equalsIgnoreCase("gzip")) {
                            response.setEntity(new GZipDecompressingEntity(response.getEntity()));
                            return;
                        }
                    }
                }
            }
        });
    }

    // ************************************    default settings & fields ****************************

    private String charset = HTTP.UTF_8;

    private long currRequestExpiry = HttpGetCache.getDefaultExpiryTime(); // httpGetCache过期时间

    private final static int DEFAULT_CONN_TIMEOUT = 1000 * 10; // 默认10秒超时

    private final static int DEFAULT_RETRY_TIMES = 5;  // 默认错误重试次数

    private final static int HTTP_THREAD_POOL_SIZE = 3; // http线程池数量

    private static final String HEADER_ACCEPT_ENCODING = "Accept-Encoding";
    private static final String ENCODING_GZIP = "gzip";

    private static final ThreadFactory sThreadFactory = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);

        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r, "HttpUtils #" + mCount.getAndIncrement());
            thread.setPriority(Thread.NORM_PRIORITY - 1);
            return thread;
        }
    };

    private static final Executor executor = Executors.newFixedThreadPool(HTTP_THREAD_POOL_SIZE, sThreadFactory);

    public HttpClient getHttpClient() {
        return this.httpClient;
    }

    // ***************************************** config *******************************************

    public void configCharset(String charSet) {
        if (charSet != null && charSet.trim().length() != 0) {
            this.charset = charSet;
        }
    }

    public void configHttpGetCacheSize(int httpGetCacheSize) {
        sHttpGetCache.setCacheSize(httpGetCacheSize);
    }

    public void configDownloadRedirectHandler(DownloadRedirectHandler downloadRedirectHandler) {
        this.downloadRedirectHandler = downloadRedirectHandler;
    }

    public void configHttpGetCacheDefaultExpiry(long defaultExpiry) {
        HttpGetCache.setDefaultExpiryTime(defaultExpiry);
        currRequestExpiry = HttpGetCache.getDefaultExpiryTime();
    }

    public void configCurrRequestExpiry(long currRequestExpiry) {
        this.currRequestExpiry = currRequestExpiry;
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

    public ResponseStream sendSync(HttpRequest.HttpMethod method, String url) throws HttpException {
        return sendSync(method, url, null);
    }

    public ResponseStream sendSync(HttpRequest.HttpMethod method, String url, RequestParams params) throws HttpException {
        return sendSync(method, url, params, null);
    }

    public ResponseStream sendSync(HttpRequest.HttpMethod method, String url, RequestParams params, String contentType) throws HttpException {
        HttpRequest request = new HttpRequest(method, url);
        return sendSyncRequest(request, params, contentType);
    }

    // ***************************************** download *******************************************

    public HttpHandler<File> download(String url, String target,
                                      RequestCallBack<File> callback) {
        return download(url, null, target, false, callback);
    }

    public HttpHandler<File> download(String url, RequestParams params, String target,
                                      RequestCallBack<File> callback) {
        return download(url, params, target, false, callback);
    }

    public HttpHandler<File> download(String url, String target, boolean isResume,
                                      RequestCallBack<File> callback) {
        return download(url, null, target, isResume, callback);
    }

    public HttpHandler<File> download(String url, RequestParams params, String target, boolean isResume,
                                      RequestCallBack<File> callback) {

        HttpRequest request = new HttpRequest(HttpRequest.HttpMethod.GET, url);

        HttpHandler<File> handler = new HttpHandler<File>(httpClient, httpContext, charset, callback);

        handler.setExpiry(currRequestExpiry);
        handler.setDownloadRedirectHandler(downloadRedirectHandler);
        request.setRequestParams(params, handler);

        handler.executeOnExecutor(executor, request, target, isResume);
        return handler;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    private <T> HttpHandler<T> sendRequest(HttpRequest request, RequestParams params, String contentType, RequestCallBack<T> callBack) {
        if (contentType != null) {
            request.addHeader("Content-Type", contentType);
        }

        HttpHandler<T> handler = new HttpHandler<T>(httpClient, httpContext, charset, callBack);

        handler.setExpiry(currRequestExpiry);
        handler.setDownloadRedirectHandler(downloadRedirectHandler);
        request.setRequestParams(params, handler);

        handler.executeOnExecutor(executor, request);
        return handler;
    }

    private ResponseStream sendSyncRequest(HttpRequest request, RequestParams params, String contentType) throws HttpException {
        if (contentType != null) {
            request.addHeader("Content-Type", contentType);
        }

        SyncHttpHandler handler = new SyncHttpHandler(httpClient, httpContext, charset);

        handler.setExpiry(currRequestExpiry);
        handler.setDownloadRedirectHandler(downloadRedirectHandler);
        request.setRequestParams(params);

        return handler.sendRequest(request);
    }
}
