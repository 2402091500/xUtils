package com.lidroid.xutils.sample.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import com.lidroid.xutils.http.HttpHandler;

import java.io.File;
import java.util.LinkedHashMap;

/**
 * Author: wyouflf
 * Date: 13-11-10
 * Time: 上午1:04
 */
public class DownloadService extends Service {

    /**
     * key: fileName
     * <p/>
     * 省去DownloadManager的包装 只是简单的示例， 需要更好的控制可以包装一个DownloadManager
     */
    public static final LinkedHashMap<String, HttpHandler<File>> FileHttpHandlerMap
            = new LinkedHashMap<String, HttpHandler<File>>();


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

        // 获取之前保存的地址和参数 根据需要开始未完成的操作
        // .....
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // 可以把下载的地址和参数保存起来
        // .....

        // 停止下载
        for (HttpHandler httpHandler : FileHttpHandlerMap.values()) {
            httpHandler.stop();
        }
    }
}
