package com.lidroid.xutils.sample.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import com.lidroid.xutils.http.HttpHandler;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Author: wyouflf
 * Date: 13-11-10
 * Time: 上午1:04
 */
public class DownloadService extends Service {

    /**
     * key: url
     */
    public static final ConcurrentHashMap<String, HttpHandler<File>> UrlHttpHandlerMap
            = new ConcurrentHashMap<String, HttpHandler<File>>();


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
