package com.example.demo;

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
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.db.sqlite.WhereBuilder;
import com.lidroid.xutils.db.table.DbModel;
import com.lidroid.xutils.exception.DbException;
import com.lidroid.xutils.http.HttpHandler;
import com.lidroid.xutils.http.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.lidroid.xutils.http.client.RequestParams;
import com.lidroid.xutils.util.LogUtils;
import com.lidroid.xutils.view.annotation.ViewInject;

import java.io.File;
import java.text.SimpleDateFormat;
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

        bitmapUtils = BitmapUtils.create(this);
    }

    BitmapUtils bitmapUtils;

    @ViewInject(id = R.id.textView)
    TextView testTextView;

    @ViewInject(id = R.id.button, click = "testButtonClick")
    Button testButton;

    @ViewInject(id = R.id.imageView)
    ImageView testImageView;

    public void testButtonClick(View v) {

        testDb();
        /*try {
            DbUtils.create(this).dropDb();
        } catch (DbException e) {
            e.printStackTrace();
        }*/

        bitmapUtils.display(testImageView, "http://bbs.lidroid.com/static/image/common/logo.png");//"/sdcard/test.jpg");

        testDownload();
        //testUpload();
        //testPost();
        //testGet();
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
                        testTextView.setText("upload response:" + result);
                    }


                    @Override
                    public void onFailure(Throwable error, String msg) {
                        testTextView.setText(msg);
                    }
                });
    }

    private void testGet() {
        RequestParams params = new RequestParams();
        params.addQueryStringParameter("method", "info");
        params.addQueryStringParameter("access_token",
                "3.1042851f652496c9362b1cd77d4f849b.2592000.1377530363.3590808424-248414");

        HttpUtils http = new HttpUtils();
        http.configCurrRequestExpiry(1000 * 10);
        http.send(HttpRequest.HttpMethod.GET,
                "https://pcs.baidu.com/rest/2.0/pcs/quota",
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
                        testTextView.setText("response:" + result);
                    }


                    @Override
                    public void onFailure(Throwable error, String msg) {
                        testTextView.setText(msg);
                    }
                });
    }

    private void testPost() {
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
                        testTextView.setText("conn...");
                    }

                    @Override
                    public void onLoading(long total, long current) {
                        testTextView.setText(current + "/" + total);
                    }

                    @Override
                    public void onSuccess(String result) {
                        testTextView.setText("upload response:" + result);
                    }


                    @Override
                    public void onFailure(Throwable error, String msg) {
                        testTextView.setText(msg);
                    }
                });
    }

    private void testDb() {

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        Parent parent = new Parent();
        parent.name = "测试";
        parent.isVIP = false;
        parent.setAdmin(true);
        parent.setEmail("wyouflf@gmail.com");

        /*Parent parent2 = new Parent();
        parent2.name = "测试2";
        parent2.isVIP = false;*/

        try {

            DbUtils db = DbUtils.create(this);
            db.configAllowTransaction(true);

            Child child = new Child();
            child.name = "child name";
            //db.saveBindingId(parent);
            //child.parent = new SQLiteLazyLoader<Parent>(Child.class, "parentId", parent.getId());
            child.parent = parent;

            try {
                Parent test = db.findFirst(parent);//通过entity的属性查找
                LogUtils.d("wyouflf :" + test);
            } catch (Exception e) {
                LogUtils.e(e.getMessage(), e);
            }

            parent.setTime(new Date());
            parent.setTime2(new java.sql.Date(new Date().getTime()));

            db.saveBindingId(child);//保存对象关联数据库生成的id

            List<Child> children = db.findAll(Selector.from(Child.class));
            LogUtils.d("wyouflf size:" + children.size());
            if (children.size() > 0) {
                LogUtils.d("wyouflf child:" + children.get(children.size() - 1).parent);
            }

            List<Parent> list = db.findAll(Selector.from(Parent.class).where(WhereBuilder.b("id", "<", 54)).orderBy("id").limit(10));
            LogUtils.d("wyouflf size:" + list.size());
            if (list.size() > 0) {
                LogUtils.d("wyouflf parent:" + list.get(list.size() - 1).toString());
            }

            //parent.name = "hahaha123";
            //db.update(parent);

            Parent entity = db.findById(Parent.class, parent.getId());
            LogUtils.d("wyouflf parent:" + entity.toString());

            List<DbModel> dbModels = db.findDbModelAll(Selector.from(Parent.class).groupBy("name").select("name", "count(name)"));
            LogUtils.d("wyouflf:" + dbModels.size());

        } catch (DbException e) {
            LogUtils.e(e.getMessage(), e);
        }
    }
}
