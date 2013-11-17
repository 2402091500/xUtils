package com.lidroid.xutils.sample.fragment;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.exception.DbException;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.HttpHandler;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.ResponseStream;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.lidroid.xutils.sample.R;
import com.lidroid.xutils.sample.download.DownloadInfo;
import com.lidroid.xutils.sample.download.DownloadManager;
import com.lidroid.xutils.sample.download.DownloadService;
import com.lidroid.xutils.util.LogUtils;
import com.lidroid.xutils.view.ResType;
import com.lidroid.xutils.view.annotation.ResInject;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.List;

/**
 * Author: wyouflf
 * Date: 13-9-14
 * Time: 下午3:35
 */
public class HttpFragment extends Fragment {

    //private HttpHandler handler;

    private DownloadListAdapter downloadListAdapter;

    private Context mAppContext;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.http_fragment, container, false);
        ViewUtils.inject(this, view);

        if (!isServiceRunning(this.getActivity(), DownloadService.class)) {
            Intent downloadSvr = new Intent("download.service.action");
            this.getActivity().startService(downloadSvr);
        }

        mAppContext = this.getActivity().getApplicationContext();
        downloadListAdapter = new DownloadListAdapter(mAppContext);
        downloadList.setAdapter(downloadListAdapter);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        //downloadBtn.setEnabled(handler == null || handler.isStopped());
        //stopBtn.setEnabled(!downloadBtn.isEnabled());

        downloadListAdapter.downloadManager = DownloadService.DOWNLOAD_MANAGER;
        downloadListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroy() {
        try {
            if (downloadListAdapter != null && downloadListAdapter.downloadManager != null) {
                downloadListAdapter.downloadManager.backupDownloadInfoList();
            }
        } catch (DbException e) {
            LogUtils.e(e.getMessage(), e);
        }
        super.onDestroy();
    }

    @ViewInject(R.id.download_addr_edit)
    private EditText downloadAddrEdit;

    @ViewInject(R.id.download_btn)
    private Button downloadBtn;

    @ViewInject(R.id.result_txt)
    private TextView resultText;

    @ViewInject(R.id.download_list)
    private ListView downloadList;

    @ResInject(id = R.string.download_label, type = ResType.String)
    private String label;

    @OnClick(R.id.download_btn)
    public void download(View view) {
        String target = "/sdcard/xUtils/" + System.currentTimeMillis() + "lzfile.apk";
        downloadListAdapter.downloadManager.addNewDownload(downloadAddrEdit.getText().toString(),
                "力卓文件",
                target,
                true, // 如果目标文件存在，接着未完成的部分继续下载。服务器不支持RANGE时将从新下载。
                false, // 如果从请求返回信息中获取到文件名，下载完成后自动重命名。
                new DownloadRequestCallBack());
        downloadListAdapter.notifyDataSetChanged();
    }

    /////////////////////////////////////// other ////////////////////////////////////////////////////////////////

    //@OnClick(R.id.download_btn)
    public void testUpload(View view) {
        RequestParams params = new RequestParams("GBK");
        //params.addQueryStringParameter("method", "upload");
        //params.addQueryStringParameter("path", "/apps/测试应用/test.zip");
        // 请在百度的开放api测试页面找到自己的access_token
        //params.addQueryStringParameter("access_token",
        //        "3.9b885b6c56b8798ab69b3ba39238e4fc.2592000.1384929178.3590808424-248414");

        //params.addBodyParameter("file", new File("/sdcard/test.zip"));
        params.addBodyParameter("msg", "测试");

        HttpUtils http = new HttpUtils();
        http.configResponseTextCharset("GBK");
        http.send(HttpRequest.HttpMethod.POST,
                "http://192.168.43.144:8080/UploadServlet",
                params,
                new RequestCallBack<String>() {

                    @Override
                    public void onStart() {
                        resultText.setText("conn...");
                    }

                    @Override
                    public void onLoading(long total, long current, boolean isUploading) {
                        if (isUploading) {
                            resultText.setText("upload: " + current + "/" + total);
                        } else {
                            resultText.setText("reply: " + current + "/" + total);
                        }
                    }

                    @Override
                    public void onSuccess(ResponseInfo<String> responseInfo) {
                        resultText.setText("reply: " + responseInfo.result);
                    }


                    @Override
                    public void onFailure(HttpException error, String msg) {
                        resultText.setText(msg);
                    }
                });
    }

    //@OnClick(R.id.download_btn)
    public void testGet(View view) {

        //RequestParams params = new RequestParams();
        //params.addHeader("name", "value");
        //params.addQueryStringParameter("name", "value");

        HttpUtils http = new HttpUtils();
        http.configCurrentHttpGetCacheExpiry(1000 * 10);
        http.send(HttpRequest.HttpMethod.GET,
                "http://www.baidu.com",
                //params,
                new RequestCallBack<String>() {

                    @Override
                    public void onStart() {
                        resultText.setText("conn...");
                    }

                    @Override
                    public void onLoading(long total, long current, boolean isUploading) {
                        resultText.setText(current + "/" + total);
                    }

                    @Override
                    public void onSuccess(ResponseInfo<String> responseInfo) {
                        resultText.setText("response:" + responseInfo.result);
                    }


                    @Override
                    public void onFailure(HttpException error, String msg) {
                        resultText.setText(msg);
                    }
                });
    }

    //@OnClick(R.id.download_btn)
    public void testPost(View view) {
        RequestParams params = new RequestParams();
        params.addQueryStringParameter("method", "mkdir");
        params.addQueryStringParameter("access_token", "3.1042851f652496c9362b1cd77d4f849b.2592000.1377530363.3590808424-248414");
        params.addBodyParameter("path", "/apps/测试应用/test文件夹");

        HttpUtils http = new HttpUtils();
        http.send(HttpRequest.HttpMethod.POST,
                "https://pcs.baidu.com/rest/2.0/pcs/file",
                params,
                new RequestCallBack<String>() {

                    @Override
                    public void onStart() {
                        resultText.setText("conn...");
                    }

                    @Override
                    public void onLoading(long total, long current, boolean isUploading) {
                        resultText.setText(current + "/" + total);
                    }

                    @Override
                    public void onSuccess(ResponseInfo<String> responseInfo) {
                        resultText.setText("upload response:" + responseInfo.result);
                    }

                    @Override
                    public void onFailure(HttpException error, String msg) {
                        resultText.setText(msg);
                    }
                });
    }

    // 同步请求 必须在异步块儿中执行
    private String testGetSync() {
        RequestParams params = new RequestParams();
        params.addQueryStringParameter("wd", "lidroid");

        HttpUtils http = new HttpUtils();
        http.configCurrentHttpGetCacheExpiry(1000 * 10);
        try {
            ResponseStream responseStream = http.sendSync(HttpRequest.HttpMethod.GET, "http://www.baidu.com/s", params);
            //int statusCode = responseStream.getStatusCode();
            //Header[] headers = responseStream.getBaseResponse().getAllHeaders();
            return responseStream.readString();
        } catch (Exception e) {
            LogUtils.e(e.getMessage(), e);
        }
        return null;
    }

    private class DownloadListAdapter extends BaseAdapter {

        private final Context mContext;
        private final LayoutInflater mInflater;

        public DownloadManager downloadManager;

        private DownloadListAdapter(Context context) {
            mContext = context;
            mInflater = LayoutInflater.from(mContext);
            if (DownloadService.DOWNLOAD_MANAGER != null) {
                downloadManager = DownloadService.DOWNLOAD_MANAGER;
            } else {
                downloadManager = new DownloadManager(mContext);
            }
            DownloadService.DOWNLOAD_MANAGER = downloadManager;
        }

        @Override
        public int getCount() {
            if (downloadManager == null) return 0;
            return downloadManager.getDownloadInfoListCount();
        }

        @Override
        public Object getItem(int i) {
            return downloadManager.getDownloadInfo(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @SuppressWarnings("unchecked")
        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            DownloadItemViewHolder holder = null;
            DownloadInfo downloadInfo = downloadManager.getDownloadInfo(i);
            if (view == null) {
                view = mInflater.inflate(R.layout.download_item, null);
                holder = new DownloadItemViewHolder(downloadInfo);
                ViewUtils.inject(holder, view);
                view.setTag(holder);
            } else {
                holder = (DownloadItemViewHolder) view.getTag();
            }

            holder.updateDownloadInfo(downloadInfo);

            HttpHandler<File> handler = downloadInfo.getHandler();
            if (handler != null) {
                handler.getRequestCallBack().setUserTag(
                        new WeakReference<DownloadItemViewHolder>(holder));
            }

            return view;
        }
    }

    public class DownloadItemViewHolder {
        @ViewInject(R.id.download_label)
        TextView label;
        @ViewInject(R.id.download_state)
        TextView state;
        @ViewInject(R.id.download_pb)
        ProgressBar progressBar;
        @ViewInject(R.id.download_stop_btn)
        Button stopBtn;
        @ViewInject(R.id.download_remove_btn)
        Button removeBtn;

        public DownloadInfo downloadInfo;

        public DownloadItemViewHolder(DownloadInfo downloadInfo) {
            this.downloadInfo = downloadInfo;
        }

        @OnClick(R.id.download_stop_btn)
        public void stop(View view) {
            if (downloadInfo.getState() != HttpHandler.State.STOPPED) {
                downloadListAdapter.downloadManager.stopDownload(downloadInfo);
            } else {
                downloadListAdapter.downloadManager.resumeDownload(downloadInfo, new DownloadRequestCallBack());
                downloadListAdapter.notifyDataSetChanged();
            }
        }

        @OnClick(R.id.download_remove_btn)
        public void remove(View view) {
            try {
                downloadListAdapter.downloadManager.removeDownload(downloadInfo);
                downloadListAdapter.notifyDataSetChanged();
            } catch (DbException e) {
                LogUtils.e(e.getMessage(), e);
            }
        }

        public void updateDownloadInfo(DownloadInfo downloadInfo) {
            this.downloadInfo = downloadInfo;
            this.refresh();
        }

        public void refresh() {
            label.setText(downloadInfo.getFileName());
            state.setText(downloadInfo.getState().toString());
            if (downloadInfo.getFileLength() != 0) {
                progressBar.setProgress((int) (downloadInfo.getProgress() * 100 / downloadInfo.getFileLength()));
            }
            HttpHandler.State state = downloadInfo.getState();
            if (state == HttpHandler.State.SUCCESS) {
                stopBtn.setVisibility(View.INVISIBLE);
            } else {
                stopBtn.setVisibility(View.VISIBLE);
                stopBtn.setText(
                        state == HttpHandler.State.FAILURE ? mAppContext.getString(R.string.retry) :
                                state == HttpHandler.State.STOPPED ?
                                        mAppContext.getString(R.string.resume) : mAppContext.getString(R.string.stop));
            }
        }
    }

    private class DownloadRequestCallBack extends RequestCallBack<File> {

        @SuppressWarnings("unchecked")
        private void refreshListItem() {
            if (userTag == null) return;
            WeakReference<DownloadItemViewHolder> tag = (WeakReference<DownloadItemViewHolder>) userTag;
            DownloadItemViewHolder holder = tag.get();
            if (holder != null) {
                holder.refresh();
            }
        }

        @Override
        public void onStart() {
            refreshListItem();
        }

        @Override
        public void onLoading(long total, long current, boolean isUploading) {
            refreshListItem();
        }

        @Override
        public void onSuccess(ResponseInfo<File> responseInfo) {
            refreshListItem();
        }

        @Override
        public void onFailure(HttpException error, String msg) {
            refreshListItem();
        }

        @Override
        public void onStopped() {
            refreshListItem();
        }
    }

    public boolean isServiceRunning(Context context, Class serviceClass) {
        boolean isRunning = false;

        ActivityManager activityManager =
                (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> serviceList
                = activityManager.getRunningServices(Integer.MAX_VALUE);

        if (serviceList.size() < 1) {
            return false;
        }

        for (int i = 0; i < serviceList.size(); i++) {
            if (serviceList.get(i).service.getClassName().equals(serviceClass.getName())) {
                isRunning = true;
                break;
            }
        }
        return isRunning;
    }
}
