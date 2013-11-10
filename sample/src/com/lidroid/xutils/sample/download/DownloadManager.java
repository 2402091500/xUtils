package com.lidroid.xutils.sample.download;

import android.content.Context;
import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.exception.DbException;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.HttpHandler;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.util.LogUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: wyouflf
 * Date: 13-11-10
 * Time: 下午8:10
 */
public class DownloadManager {

    private List<DownloadInfo> downloadInfoList;

    private int maxDownloadThread = 3;

    private Context mContext;
    private DbUtils db;

    public DownloadManager(Context context) {
        mContext = context;
        db = DbUtils.create(mContext);
        try {
            downloadInfoList = db.findAll(Selector.from(DownloadInfo.class));
        } catch (DbException e) {
            LogUtils.e(e.getMessage(), e);
        }
        if (downloadInfoList == null) {
            downloadInfoList = new ArrayList<DownloadInfo>();
        }
    }

    public int getDownloadInfoListCount() {
        return downloadInfoList.size();
    }

    public DownloadInfo getDownloadInfo(int index) {
        return downloadInfoList.get(index);
    }

    public void addNewDownload(String url, String fileName, String target,
                               boolean autoResume, boolean autoRename, final RequestCallBack<File> callback) {
        final DownloadInfo downloadInfo = new DownloadInfo();
        downloadInfo.setDownloadUrl(url);
        downloadInfo.setAutoRename(autoRename);
        downloadInfo.setAutoResume(autoResume);
        downloadInfo.setFileName(fileName);
        downloadInfo.setFileSavePath(target);
        HttpUtils http = new HttpUtils();
        http.configRequestThreadPoolSize(maxDownloadThread);
        HttpHandler<File> handler = http.download(url, target, autoResume, autoRename,
                new DownloadCallBack(downloadInfo, callback));
        downloadInfo.setHandler(handler);
        downloadInfoList.add(downloadInfo);
    }

    public void resumeDownload(int index, final RequestCallBack<File> callback) {
        final DownloadInfo downloadInfo = downloadInfoList.get(index);
        HttpUtils http = new HttpUtils();
        http.configRequestThreadPoolSize(maxDownloadThread);
        HttpHandler<File> handler = http.download(
                downloadInfo.getDownloadUrl(),
                downloadInfo.getFileSavePath(),
                downloadInfo.isAutoResume(),
                downloadInfo.isAutoRename(),
                new DownloadCallBack(downloadInfo, callback));
        downloadInfo.setHandler(handler);
    }

    public void removeDownload(int index) throws DbException {
        HttpHandler<File> handler = downloadInfoList.get(index).getHandler();
        if (handler != null && !handler.isStopped()) {
            handler.stop();
        }
        db.delete(downloadInfoList.get(index));
        downloadInfoList.remove(index);
    }

    public void stopDownload(int index) {
        HttpHandler<File> handler = downloadInfoList.get(index).getHandler();
        if (handler != null && !handler.isStopped()) {
            handler.stop();
        }
    }

    public void stopAllDownload() {
        for (DownloadInfo downloadInfo : downloadInfoList) {
            HttpHandler<File> handler = downloadInfo.getHandler();
            if (handler != null && !handler.isStopped()) {
                handler.stop();
            }
        }
    }

    public boolean isDownloadStarted(int index) {
        return downloadInfoList.get(index).isStarted();
    }

    public boolean isDownloadStopped(int index) {
        HttpHandler<File> handler = downloadInfoList.get(index).getHandler();
        if (handler != null) {
            return handler.isStopped();
        }
        return false;
    }

    public boolean isDownloadCompleted(int index) {
        return downloadInfoList.get(index).isDownloadCompleted();
    }

    public boolean isDownloadFailed(int index) {
        return downloadInfoList.get(index).isFailed();
    }

    public void backupDownloadInfoList() throws DbException {
        db.saveOrUpdateAll(downloadInfoList);
    }

    public int getMaxDownloadThread() {
        return maxDownloadThread;
    }

    public void setMaxDownloadThread(int maxDownloadThread) {
        this.maxDownloadThread = maxDownloadThread;
    }


    private class DownloadCallBack extends RequestCallBack<File> {

        private DownloadInfo downloadInfo;
        private RequestCallBack<File> callBack;

        private DownloadCallBack(DownloadInfo downloadInfo, RequestCallBack<File> callBack) {
            super();
            this.downloadInfo = downloadInfo;
            this.callBack = callBack;
        }

        @Override
        public void onSuccess(ResponseInfo<File> responseInfo) {
            downloadInfo.setDownloadCompleted(true);
            callBack.onSuccess(responseInfo);
        }

        @Override
        public void onFailure(HttpException error, String msg) {
            downloadInfo.setFailed(true);
            callBack.onFailure(error, msg);
        }

        @Override
        public Object getUserTag() {
            return callBack.getUserTag();
        }

        @Override
        public void setUserTag(Object userTag) {
            callBack.setUserTag(userTag);
        }

        @Override
        public void onStart() {
            downloadInfo.setStarted(true);
            downloadInfo.setFailed(false);
            downloadInfo.setDownloadCompleted(false);
            callBack.onStart();
        }

        @Override
        public void onStopped() {
            downloadInfo.setStarted(false);
            callBack.onStopped();
        }

        @Override
        public void onLoading(long total, long current, boolean isUploading) {
            downloadInfo.setFileLength(total);
            callBack.onLoading(total, current, isUploading);
        }
    }
}
