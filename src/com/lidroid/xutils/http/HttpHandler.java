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

import android.os.SystemClock;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.client.HttpGetCache;
import com.lidroid.xutils.http.client.HttpRequest;
import com.lidroid.xutils.http.client.callback.*;
import com.lidroid.xutils.util.core.CompatibleAsyncTask;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.protocol.HttpContext;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;


public class HttpHandler<T> extends CompatibleAsyncTask<Object, Object, Object> implements RequestCallBackHandler {

    private final AbstractHttpClient client;
    private final HttpContext context;

    private final StringDownloadHandler mStringDownloadHandler = new StringDownloadHandler();
    private final FileDownloadHandler mFileDownloadHandler = new FileDownloadHandler();

    private DownloadRedirectHandler downloadRedirectHandler;

    public void setDownloadRedirectHandler(DownloadRedirectHandler downloadRedirectHandler) {
        this.downloadRedirectHandler = downloadRedirectHandler;
    }

    private final RequestCallBack callback;

    private int retriedTimes = 0;
    private String fileSavePath = null; // 下载的路径
    private boolean isDownloadingFile; // fileSavePath != null;
    private boolean isResume = false; // 是否断点续传
    private String charset;

    public HttpHandler(AbstractHttpClient client, HttpContext context, String charset, RequestCallBack callback) {
        this.client = client;
        this.context = context;
        this.callback = callback;
        this.charset = charset;
    }

    private String _getRequestUrl;// if not get method, it will be null.
    private long expiry = HttpGetCache.getDefaultExpiryTime();

    public void setExpiry(long expiry) {
        this.expiry = expiry;
    }

    // 执行请求
    private void doSendRequest(HttpRequestBase request) throws HttpException {
        if (isResume && isDownloadingFile) {
            File downloadFile = new File(fileSavePath);
            long fileLen = 0;
            if (downloadFile.isFile() && downloadFile.exists()) {
                fileLen = downloadFile.length();
            }
            if (fileLen > 0) {
                request.setHeader("RANGE", "bytes=" + fileLen + "-");
            }
        }

        boolean retry = true;
        HttpRequestRetryHandler retryHandler = client.getHttpRequestRetryHandler();
        while (retry) {
            try {
                if (request.getMethod().equals(HttpRequest.HttpMethod.GET.toString())) {
                    _getRequestUrl = request.getURI().toString();
                } else {
                    _getRequestUrl = null;
                }
                if (_getRequestUrl != null) {
                    String result = HttpUtils.sHttpGetCache.get(_getRequestUrl);
                    if (result != null) { // 未过期的返回字符串的get请求直接返回结果
                        publishProgress(UPDATE_SUCCESS, result);
                        return;
                    }
                }
                if (!isCancelled()) {
                    HttpResponse response = client.execute(request, context);
                    handleResponse(response);
                }
                return;
            } catch (UnknownHostException e) {
                publishProgress(UPDATE_FAILURE, e, e.getMessage());
                return;
            } catch (IOException e) {
                retry = retryHandler.retryRequest(e, ++retriedTimes, context);
            } catch (NullPointerException e) {
                retry = retryHandler.retryRequest(new IOException(e), ++retriedTimes, context);
            } catch (Exception e) {
                retry = retryHandler.retryRequest(new IOException(e), ++retriedTimes, context);
            }
        }
    }

    @Override
    protected Object doInBackground(Object... params) {
        if (params != null && params.length == 3) {
            fileSavePath = String.valueOf(params[1]);
            isDownloadingFile = fileSavePath != null;
            isResume = (Boolean) params[2];
        }
        try {
            publishProgress(UPDATE_START);
            doSendRequest((HttpRequestBase) params[0]);
        } catch (HttpException e) {
            publishProgress(UPDATE_FAILURE, e, e.getMessage());
        }

        return null;
    }

    private final static int UPDATE_START = 1;
    private final static int UPDATE_LOADING = 2;
    private final static int UPDATE_FAILURE = 3;
    private final static int UPDATE_SUCCESS = 4;

    @SuppressWarnings("unchecked")
    @Override
    protected void onProgressUpdate(Object... values) {
        int update = Integer.valueOf(String.valueOf(values[0]));
        switch (update) {
            case UPDATE_START:
                if (callback != null) {
                    callback.onStart();
                }
                break;
            case UPDATE_LOADING:
                if (callback != null) {
                    callback.onLoading(
                            Long.valueOf(String.valueOf(values[1])),
                            Long.valueOf(String.valueOf(values[2])));
                }
                break;
            case UPDATE_FAILURE:
                if (callback != null) {
                    callback.onFailure((Throwable) values[1], (String) values[2]);
                }
                break;
            case UPDATE_SUCCESS:
                if (callback != null) {
                    callback.onSuccess((T) values[1]);
                }
                break;
            default:
                break;
        }
        super.onProgressUpdate(values);
    }

    private void handleResponse(HttpResponse response) {
        if (response == null) {
            publishProgress(UPDATE_FAILURE, new HttpException("UNKNOWN ERROR"), "response is null");
            return;
        }
        StatusLine status = response.getStatusLine();
        if (status.getStatusCode() < 300) {
            try {
                HttpEntity entity = response.getEntity();
                Object responseBody = null;
                if (entity != null) {
                    lastUpdateTime = SystemClock.uptimeMillis();
                    if (isDownloadingFile) {
                        responseBody = mFileDownloadHandler.handleEntity(entity, this, fileSavePath, isResume);
                    } else {
                        responseBody = mStringDownloadHandler.handleEntity(entity, this, charset);
                        HttpUtils.sHttpGetCache.put(_getRequestUrl, (String) responseBody, expiry);
                    }

                }
                publishProgress(UPDATE_SUCCESS, responseBody);
            } catch (IOException e) {
                publishProgress(UPDATE_FAILURE, e, e.getMessage());
            }
        } else if (status.getStatusCode() == 302) {
            if (downloadRedirectHandler == null) {
                downloadRedirectHandler = new DefaultDownloadRedirectHandler();
            }
            HttpRequestBase request = downloadRedirectHandler.getDirectRequest(response);
            if (request != null) {
                try {
                    response = client.execute(request, context);
                    if (!isCancelled()) {
                        handleResponse(response);
                    }
                } catch (IOException e) {
                    publishProgress(UPDATE_FAILURE, e, e.getMessage());
                }
            }

        } else {
            String errorMsg = "response status error code:" + status.getStatusCode();
            if (status.getStatusCode() == 416 && isResume) {
                errorMsg += " \n maybe you have download complete.";
            }
            publishProgress(
                    UPDATE_FAILURE,
                    new HttpException(status.getStatusCode() + ": " + status.getReasonPhrase()),
                    errorMsg);
        }
    }

    private boolean mStop = false;

    /**
     * 停止下载任务
     */
    @Override
    public void stop() {
        this.mStop = true;
        this.cancel(true);
    }

    public boolean isStop() {
        return mStop;
    }

    private long lastUpdateTime;

    @Override
    public boolean updateProgress(long total, long current, boolean forceUpdateUI) {
        if (mStop) {
            return !mStop;
        }
        if (callback != null && callback.isProgress()) {
            if (forceUpdateUI) {
                publishProgress(UPDATE_LOADING, total, current);
            } else {
                long currTime = SystemClock.uptimeMillis();
                if (currTime - lastUpdateTime >= callback.getRate()) {
                    lastUpdateTime = currTime;
                    publishProgress(UPDATE_LOADING, total, current);
                }
            }
        }
        return !mStop;
    }

}
