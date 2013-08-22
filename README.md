## xUtils简介
* xUtils 包含了很多实用的android工具。
* xUtils 源于Afinal框架，对Afinal进行了大量重构，使得xUtils支持大文件上传，更全面的http请求协议支持，拥有更加灵活的ORM，更多的事件注解支持且不受混淆影响...


## 目前xUtils主要有四大模块：

* DbUtils模块：
  > * android中的orm框架，一行代码就可以进行增删改查；
  > * 支持事务，默认关闭；
  > * 可通过注解自定义表名，列名，外键，唯一性约束，NOT NULL约束，CHECK约束等（需要混淆的时候请注解表名和列名）；
  > * 支持绑定外键，保存实体时外键关联实体自动保存或更新；
  > * 自动加载外键关联实体，支持延时加载；
  > * 支持链式表达查询，参考下面的介绍或Demo中的例子。

* ViewUtils模块：
  > * android中的ioc框架，完全注解方式就可以进行UI绑定和事件绑定；
  > * 新的事件绑定方式，使用混淆工具混淆后仍可正常工作；
  > * 目前支持常用的11种事件绑定，参见ViewCommonEventListener类和包com.lidroid.xutils.view.annotation.event。

* HttpUtils模块：
  > * 支持同步，异步方式的请求；
  > * 支持大文件上传，上传大文件不会oom；
  > * 支持GET，POST，PUT，MOVE，COPY，DELETE，HEAD请求；
  > * 下载支持302重定向；
  > * 返回文本内容的GET请求支持缓存，可设置默认过期时间和针对当前请求的过期时间。

* BitmapUtils模块：
  > * 加载bitmap的时候无需考虑bitmap加载过程中出现的oom和android容器快速滑动时候出现的图片错位等现象；
  > * 支持加载网络图片和本地图片；
  > * 内存管理使用lru算法，更好的管理bitmap内存；
  > * 可配置线程加载线程数量，缓存大小，缓存路径，加载显示动画等...


----
## 使用xUtils快速开发框架需要有以下权限：

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
```

----
## DbUtils使用方法：

```java
DbUtils db = DbUtils.create(this);
User user = new User(); //这里需要注意的是User对象必须有id属性，或者有通过@ID注解的属性
user.setEmail("wyouflf@qq.com");
user.setName("wyouflf");
db.save(user); // 使用saveBindingId保存实体时会为实体的id赋值

...
// 查找
Parent entity = db.findById(Parent.class, parent.getId());
Parent entity = db.findFirst(entity);//通过entity的属性查找
List<Parent> list = db.findAll(entity);//通过entity的属性查找
Parent Parent = db.findFirst(Selector.from(Parent.class).where(WhereBuilder.b("name","=","test")));
List<Parent> list = db.findAll(Selector.from(Parent.class).where(WhereBuilder.b("id","<",54)).orderBy("id").limit(10));
DbModel dbModel = db.findDbModelAll(Selector.from(Parent.class).select("name"));//select("name")只取出name列
List<DbModel> dbModels = db.findDbModelAll(Selector.from(Parent.class).groupBy("name").select("name", "count(name)"));
...
```

----
## ViewUtils使用方法
* 完全注解方式就可以进行UI绑定和事件绑定。
* 无需findViewById和setClickListener等。

```java
@ViewInject(R.id.textView)
TextView textView;

// 取消了之前使用方法名绑定事件的方式，使用id绑定不受混淆影响
@OnClick(R.id.test_button)
public void testButtonClick(View v) {
    ...
}
...
//在使用注解对象之前调用(如onCreate中)：
@Override
public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);

    ViewUtils.inject(this);

    ...
    textView.setText("some text...");
    ...
}
```

----
## HttpUtils使用方法：
### 普通get方法

```java
HttpUtils http = new HttpUtils();
http.send(HttpRequest.HttpMethod.GET,
    "http://www.lidroid.com",
    new RequestCallBack<String>(){
        @Override
        public void onLoading(long total, long current) {
            testTextView.setText(current + "/" + total);
        }

        @Override
        public void onSuccess(String result) {
            textView.setText(result);
        }

        @Override
        public void onStart() {
        }

        @Override
        public void onFailure((Throwable error, String msg) {
        }
});
```

----
### 使用HttpUtils上传文件 或者 提交数据 到服务器（post方法）

```java
RequestParams params = new RequestParams();
params.addHeader("name", "value");
params.addQueryStringParameter("name", "value");

// 只包含字符串参数时默认使用BodyParamsEntity，
// 类似于UrlEncodedFormEntity（"application/x-www-form-urlencoded"）。
params.addBodyParameter("name", "value");

// 加入文件参数后默认使用MultipartEntity（"multipart/form-data"），
// 如需"multipart/related"，xUtils中提供的MultipartEntity支持设置subType为"related"。
// 使用params.setBodyEntity(httpEntity)可设置更多类型的HttpEntity（如：
// MultipartEntity,BodyParamsEntity,UploadFileEntity,UploadInputStreamEntity,StringEntity）。
params.addBodyParameter("file", new File("path"));
...

HttpUtils http = new HttpUtils();
http.send(HttpRequest.HttpMethod.POST,
    "uploadUrl....",
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
            testTextView.setText("upload response:" + result.getPath());
        }

        @Override
        public void onFailure(Throwable error, String msg) {
            testTextView.setText(msg);
        }
});
```

----
### 使用HttpUtils下载文件：
* 支持断点续传，随时停止下载任务，开始任务

```java
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

...
//调用stop()方法停止下载
handler.stop();
...
```

----
## BitmapUtils 使用方法

```java
BitmapUtils.create(this).display(testImageView, "http://bbs.lidroid.com/static/image/common/logo.png");
//BitmapUtils.create(this).display(testImageView, "/sdcard/test.jpg"); //支持加载本地图片
```

----
## 其他（***更多示例代码见Demo文件夹中的代码***）
### 输出日志 LogUtils

```java
// 自动添加TAG，格式： className[methodName, lineNumber]
// 可设置全局的allowD，allowE...，控制是否输出log。
LogUtils.d("wyouflf");
```

----
# 关于作者
* Email： <wyouflf@qq.com>, <wyouflf@gmail.com>
* 有任何建议都可以给我发邮件, 你也可以加入这个QQ群：330445659, 技术交流，idea分享 *_*

# 关于Afinal
* <https://github.com/yangfuhai/afinal>


