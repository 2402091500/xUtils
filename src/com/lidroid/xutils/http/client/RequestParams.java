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

package com.lidroid.xutils.http.client;

import com.lidroid.xutils.http.client.entity.BodyParamsEntity;
import com.lidroid.xutils.http.client.multipart.MultipartEntity;
import com.lidroid.xutils.http.client.multipart.content.ContentBody;
import com.lidroid.xutils.http.client.multipart.content.FileBody;
import com.lidroid.xutils.http.client.multipart.content.InputStreamBody;
import com.lidroid.xutils.http.client.multipart.content.StringBody;
import com.lidroid.xutils.util.LogUtils;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


public class RequestParams {

    private String charset = HTTP.UTF_8;

    private List<Header> headers;
    private List<NameValuePair> queryStringParams;
    private HttpEntity bodyEntity;
    private List<NameValuePair> bodyParams;
    private HashMap<String, ContentBody> fileParams;

    public RequestParams() {
    }

    public RequestParams(String charset) {
        this.charset = charset;
    }

    public void addHeader(Header header) {
        if (this.headers == null) {
            this.headers = new ArrayList<Header>();
        }
        this.headers.add(header);
    }

    public void addHeader(String name, String value) {
        if (this.headers == null) {
            this.headers = new ArrayList<Header>();
        }
        this.headers.add(new BasicHeader(name, value));
    }

    public void addHeaders(List<Header> headers) {
        if (this.headers == null) {
            this.headers = new ArrayList<Header>();
        }
        this.headers.addAll(headers);
    }

    public void addQueryStringParameter(String name, String value) {
        if (queryStringParams == null) {
            queryStringParams = new ArrayList<NameValuePair>();
        }
        queryStringParams.add(new BasicNameValuePair(name, value));
    }

    public void addQueryStringParameter(NameValuePair nameValuePair) {
        if (queryStringParams == null) {
            queryStringParams = new ArrayList<NameValuePair>();
        }
        queryStringParams.add(nameValuePair);
    }

    public void addQueryStringParameter(List<NameValuePair> nameValuePairs) {
        if (queryStringParams == null) {
            queryStringParams = new ArrayList<NameValuePair>();
        }
        queryStringParams.addAll(nameValuePairs);
    }

    public void addBodyParameter(String name, String value) {
        if (bodyParams == null) {
            bodyParams = new ArrayList<NameValuePair>();
        }
        bodyParams.add(new BasicNameValuePair(name, value));
    }

    public void addBodyParameter(NameValuePair nameValuePair) {
        if (bodyParams == null) {
            bodyParams = new ArrayList<NameValuePair>();
        }
        bodyParams.add(nameValuePair);
    }

    public void addBodyParameter(List<NameValuePair> nameValuePairs) {
        if (bodyParams == null) {
            bodyParams = new ArrayList<NameValuePair>();
        }
        bodyParams.addAll(nameValuePairs);
    }

    public void addBodyParameter(String key, File file) {
        if (fileParams == null) {
            fileParams = new HashMap<String, ContentBody>();
        }
        fileParams.put(key, new FileBody(file));
    }

    public void addBodyParameter(String key, File file, String mimeType) {
        if (fileParams == null) {
            fileParams = new HashMap<String, ContentBody>();
        }
        fileParams.put(key, new FileBody(file, mimeType));
    }

    public void addBodyParameter(String key, File file, String mimeType, String charset) {
        if (fileParams == null) {
            fileParams = new HashMap<String, ContentBody>();
        }
        fileParams.put(key, new FileBody(file, mimeType, charset));
    }

    public void addBodyParameter(String key, InputStream stream, long length, String fileName) {
        if (fileParams == null) {
            fileParams = new HashMap<String, ContentBody>();
        }
        fileParams.put(key, new InputStreamBody(stream, length, fileName));
    }

    public void addBodyParameter(String key, InputStream stream, long length, String mimeType, String fileName) {
        if (fileParams == null) {
            fileParams = new HashMap<String, ContentBody>();
        }
        fileParams.put(key, new InputStreamBody(stream, length, mimeType, fileName));
    }

    public void setBodyEntity(HttpEntity bodyEntity) {
        this.bodyEntity = bodyEntity;
        if (bodyParams != null) {
            bodyParams.clear();
            bodyParams = null;
        }
        if (fileParams != null) {
            fileParams.clear();
            fileParams = null;
        }
    }

    /**
     * Returns an HttpEntity containing all request parameters
     */
    public HttpEntity getEntity() {

        if (bodyEntity != null) {
            return bodyEntity;
        }

        HttpEntity result = null;

        if (fileParams != null && !fileParams.isEmpty()) {

            MultipartEntity multipartEntity = new MultipartEntity();

            if (bodyParams != null) {
                for (NameValuePair param : bodyParams) {
                    try {
                        multipartEntity.addPart(param.getName(), new StringBody(param.getValue()));
                    } catch (UnsupportedEncodingException e) {
                        LogUtils.e(e.getMessage(), e);
                    }
                }
            }

            for (ConcurrentHashMap.Entry<String, ContentBody> entry : fileParams.entrySet()) {
                multipartEntity.addPart(entry.getKey(), entry.getValue());
            }

            result = multipartEntity;
        } else if (bodyParams != null) {
            result = new BodyParamsEntity(bodyParams, charset);
        }

        return result;
    }

    public List<NameValuePair> getQueryStringParams() {
        return this.queryStringParams;
    }

    public List<Header> getHeaders() {
        return headers;
    }
}