package com.example.test;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.lidroid.xutils.BitmapUtils;
import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.exception.DbException;
import com.lidroid.xutils.http.HttpHandler;
import com.lidroid.xutils.http.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.lidroid.xutils.http.client.RequestParams;
import com.lidroid.xutils.util.LogUtils;
import com.lidroid.xutils.view.annotation.ViewInject;

import java.io.File;
import java.util.Date;
import java.util.List;

public class MyActivity extends Activity {
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        ViewUtils.inject(this);
    }

    @ViewInject(id = R.id.textView)
    TextView testTextView;

    @ViewInject(id = R.id.button, click = "testButtonClick")
    Button testButton;

    @ViewInject(id = R.id.imageView)
    ImageView testImageView;

    public void testButtonClick(View v) {

        testDb();

        BitmapUtils.create(this).display(testImageView, "http://bbs.lidroid.com/static/image/common/logo.png");

        testDownload();
        //testUpload();
    }

    private void testDownload() {
        HttpUtils http = new HttpUtils();
        HttpHandler handler = http.download("http://apache.dataguru.cn/httpcomponents/httpclient/source/httpcomponents-client-4.2.5-src.zip",
                "/sdcard/httpcomponents-client-4.2.5-src.zip",
                new RequestCallBack<File>() {

                    @Override
                    public void onStart() {
                        testTextView.setText("conn...");
                    }

                    @Override
                    public void onLoading(long total, long current) {
                        testTextView.setText(current + "/" + total);
                    }

                    @Override
                    public void onSuccess(File result) {
                        testTextView.setText("downloaded:" + result.getPath());
                    }


                    @Override
                    public void onFailure(Throwable error, String msg) {
                        testTextView.setText(msg);
                    }
                });
    }

    private void testUpload() {
        RequestParams params = new RequestParams();
        params.addQueryStringParameter("method", "upload");
        params.addQueryStringParameter("path", "/apps/测试应用/test.zip");
        params.addQueryStringParameter("access_token", "3.1042851f652496c9362b1cd77d4f849b.2592000.1377530363.3590808424-248414");
        params.addBodyParameter("file", new File("/sdcard/test.zip"));

        HttpUtils http = new HttpUtils();
        http.send(HttpRequest.HttpMethod.POST,
                "https://pcs.baidu.com/rest/2.0/pcs/file",
                params,
                new RequestCallBack<String>() {

                    @Override
                    public void onStart() {
                        testTextView.setText("conn...");
                    }

                    @Override
                    public void onLoading(long total, long current) {
                        testTextView.setText(current + "/" + total);
                    }

                    @Override
                    public void onSuccess(String result) {
                        testTextView.setText("uploaded:" + result);
                    }


                    @Override
                    public void onFailure(Throwable error, String msg) {
                        testTextView.setText(msg);
                    }
                });
    }

    private void testDb() {
        TestEntity testEntity = new TestEntity();
        testEntity.name = "测试";
        testEntity.isVIP = true;
        testEntity.setAdmin(true);
        testEntity.setEmail("wyouflf@gmail.com");
        testEntity.setTime(new Date());
        testEntity.setTime2(new java.sql.Date(new Date().getTime()));

        try {
            DbUtils db = DbUtils.create(this);
            db.saveBindingId(testEntity);

            List<TestEntity> list = db.findAll(TestEntity.class);
            LogUtils.d("wyouflf size:" + list.size());
            LogUtils.d("wyouflf testEntity:" + list.get(list.size() - 1).toString());

            testEntity.name = "hahaha";
            db.update(testEntity);

            TestEntity entity = db.findById(TestEntity.class, testEntity.getId());
            LogUtils.d("wyouflf testEntity:" + entity.toString());

        } catch (DbException e) {
            e.printStackTrace();
        }
    }
}
