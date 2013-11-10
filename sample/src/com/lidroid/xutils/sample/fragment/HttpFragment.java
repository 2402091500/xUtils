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
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.HttpHandler;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.ResponseStream;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.lidroid.xutils.sample.R;
import com.lidroid.xutils.sample.service.DownloadService;
import com.lidroid.xutils.util.LogUtils;
import com.lidroid.xutils.view.ResType;
import com.lidroid.xutils.view.annotation.ResInject;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Author: wyouflf
 * Date: 13-9-14
 * Time: 下午3:35
 */
public class HttpFragment extends Fragment {

    //private HttpHandler handler;

    private DownloadListAdapter downloadListAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.http_fragment, container, false);
        ViewUtils.inject(this, view);

        downloadListAdapter = new DownloadListAdapter(this.getActivity());
        downloadList.setAdapter(downloadListAdapter);

        if (!isServiceRunning(this.getActivity(), DownloadService.class)) {
            Intent downloadSvr = new Intent("download.service.action");
            this.getActivity().startService(downloadSvr);
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        //downloadBtn.setEnabled(handler == null || handler.isStopped());
        //stopBtn.setEnabled(!downloadBtn.isEnabled());

        if (DownloadService.FileHttpHandlerMap.size() > 0) {
            downloadListAdapter.FileHttpHandlerMap.putAll(DownloadService.FileHttpHandlerMap);
            downloadListAdapter.notifyDataSetChanged();
        }
    }

    @ViewInject(R.id.download_addr_edit)
    private EditText downloadAddrEdit;

    @ViewInject(R.id.download_btn)
    private Button downloadBtn;

    @ViewInject(R.id.stop_btn)
    private Button stopBtn;

    @ViewInject(R.id.result_txt)
    private TextView resultText;

    @ViewInject(R.id.download_list)
    private ListView downloadList;

    @ResInject(id = R.string.download_label, type = ResType.String)
    private String label;

    @OnClick(R.id.download_btn)
    public void download(View view) {

        //downloadBtn.setEnabled(false);
        //stopBtn.setEnabled(true);

        String fileName = "/sdcard/" + System.currentTimeMillis() + "lzfile.apk";

        HttpUtils http = new HttpUtils();
        HttpHandler<File> handler = http.download(
                downloadAddrEdit.getText().toString(),
                fileName,
                true, // 如果目标文件存在，接着未完成的部分继续下载。服务器不支持RANGE时将从新下载。
                true, // 如果从请求返回信息中获取到文件名，下载完成后自动重命名。
                //new RequestCallBack<File>() { // userTag is null
                new RequestCallBack<File>("my custom tag") {

                    @Override
                    public void onStart() {
                        if (userTag == null) return;
                        DownloadUserTag tag = (DownloadUserTag) userTag;
                        TextView view = (TextView) tag.viewRef.get();
                        if (view != null) {
                            view.setText(tag.name + "\n conn...");
                        }
                    }

                    @Override
                    public void onLoading(long total, long current, boolean isUploading) {
                        if (userTag == null) return;
                        DownloadUserTag tag = (DownloadUserTag) userTag;
                        TextView view = (TextView) tag.viewRef.get();
                        if (view != null) {
                            view.setText(tag.name + ": " + current + "/" + total);
                        }
                    }

                    @Override
                    public void onSuccess(ResponseInfo<File> responseInfo) {
                        if (userTag == null) return;
                        DownloadUserTag tag = (DownloadUserTag) userTag;
                        TextView view = (TextView) tag.viewRef.get();
                        if (view != null) {
                            view.setText("downloaded:" + responseInfo.result.getPath());
                            //downloadBtn.setEnabled(false);
                            //stopBtn.setEnabled(false);
                        }
                    }

                    @Override
                    public void onFailure(HttpException error, String msg) {
                        // error.getCause(); //内部错误
                        if (userTag == null) return;
                        DownloadUserTag tag = (DownloadUserTag) userTag;
                        TextView view = (TextView) tag.viewRef.get();
                        if (view != null) {
                            view.setText(error.getExceptionCode() + ":" + msg);
                            //downloadBtn.setEnabled(true);
                            //stopBtn.setEnabled(false);
                        }
                    }
                });

        DownloadService.FileHttpHandlerMap.put(fileName, handler);
        downloadListAdapter.FileHttpHandlerMap.put(fileName, handler);
        downloadListAdapter.notifyDataSetChanged();
    }

    @OnClick(R.id.stop_btn)
    public void stop(View view) {
        //if (handler != null) {
        //    handler.stop();
        //    resultText.setText(resultText.getText() + " stopped");
        //    downloadBtn.setEnabled(true);
        //    stopBtn.setEnabled(false);
        //}
    }

    /////////////////////////////////////// other ////////////////////////////////////////////////////////////////

    //@OnClick(R.id.download_btn)
    public void testUpload(View view) {
        RequestParams params = new RequestParams();
        //params.addQueryStringParameter("method", "upload");
        //params.addQueryStringParameter("path", "/apps/测试应用/test.zip");
        // 请在百度的开放api测试页面找到自己的access_token
        //params.addQueryStringParameter("access_token",
        //        "3.9b885b6c56b8798ab69b3ba39238e4fc.2592000.1384929178.3590808424-248414");
        params.addBodyParameter("file", new File("/sdcard/test.zip"));

        HttpUtils http = new HttpUtils();
        http.send(HttpRequest.HttpMethod.POST,
                "http://192.168.1.6:8080/UploadServlet",
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

        /**
         * key: fileName
         * <p/>
         * 省去DownloadManager的包装 只是简单的示例， 需要更好的控制可以包装一个DownloadManager
         */
        public final LinkedHashMap<String, HttpHandler<File>> FileHttpHandlerMap
                = new LinkedHashMap<String, HttpHandler<File>>();

        private DownloadListAdapter(Context context) {
            mContext = context;
        }

        @Override
        public int getCount() {
            return FileHttpHandlerMap.size();
        }

        @Override
        public Object getItem(int i) {
            return FileHttpHandlerMap.entrySet().toArray()[i];
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @SuppressWarnings("unchecked")
        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = new TextView(mContext);
                view.setMinimumWidth(400);
            }
            Map.Entry<String, HttpHandler> entry = (Map.Entry<String, HttpHandler>) FileHttpHandlerMap.entrySet().toArray()[i];
            DownloadUserTag userTag = new DownloadUserTag();
            userTag.name = entry.getKey();
            ((TextView) view).setText(userTag.name);
            userTag.viewRef = new WeakReference<View>(view);
            entry.getValue().getRequestCallBack().setUserTag(userTag);
            return view;
        }
    }

    public class DownloadUserTag {
        public String name;
        public WeakReference<View> viewRef;
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
