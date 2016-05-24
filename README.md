1.xUtils中的IOC框架

--------------------------------------------------------------------------------

使用xUtils的第一步就是必须创建自己的Application类，代码如下：
public class LYJApplication @Override publicvoidonCreate() { super.onCreate(); x.Ext.init(this);//Xutils初始化 } }

在AndroidManifest.xml的application标签中添加如下代码：
Android:name=”.LYJApplication”
这样初始化就算完成了。使用IOC框架的代码如下：

import org.xutils.view.annotation.ContentView; import org.xutils.view.annotation.Event; import org.xutils.view.annotation.ViewInject; import org.xutils.x; @ContentView(value = R.layout.activity_main) public class MainActivity extendsAppCompatActivity { @ViewInject(value = R.id.mybut) privateButton mybut; @Override protectedvoidonCreate(Bundle savedInstanceState) { super.onCreate(savedInstanceState); x.view().inject(this); } @Event(value = R.id.mybut,type = View.OnClickListener.class) privatevoidonButtonClick(View v){ switch(v.getId()){ caseR.id.mybut: Toast.makeText(this,"你好我是Xutils的IOC功能",Toast.LENGTH_SHORT).show(); break; } } }

需要解释的以下几点：
+其一：使用IOC必须全部为私有，不然无效，这里就做演示了，不信你可以把用到IOC框架的注解的成员变量及方法全部换成public ,那么全部会无效，当然除了ContentView例外。 +其二，所有用到IOC成员变量，使用的时候，必须在x.view().inject(this)后，如果写在前面，那么程序会崩溃。 +2.xUtils加载图片功能现在我们需要设置两个权限，如下：



接下来就是加载网络图片到imageView中：
x.image().bind(image,”http://pic.baike.soso.com/p/20090711/20090711101754-314944703.jpg“);

###也可以设置参数：
ImageOptions imageOptions =newImageOptions.Builder() .setSize(DensityUtil.dip2px(120), DensityUtil.dip2px(120))//图片大小 .setRadius(DensityUtil.dip2px(5))//ImageView圆角半径 .setCrop(true)// 如果ImageView的大小不是定义为wrap_content, 不要crop. .setImageScaleType(ImageView.ScaleType.CENTER_CROP) .setLoadingDrawableId(R.mipmap.ic_launcher)//加载中默认显示图片 .setFailureDrawableId(R.mipmap.ic_launcher)//加载失败后默认显示图片 .build(); x.image().bind(image,"http://pic.baike.soso.com/p/20090711/20090711101754-314944703.jpg",imageOptions);

你也可以将第2个参数设置为图片文件路径，那么将从SD卡中加载图片。
3.xUtils操作数据库
我们都知道，一个App中操作数据库的地方有很多，就像是否登录一样，有些地方必须登录后才能操作，那么肯定是全局变量，所以，必须将数据库的初始化放在Application，且必须提供获取数据库的方法，使得在应用程序的任何地方都可以直接获取数据库，并操作数据库，不然重复的获取与释放只能增加内存无谓的消耗。初始化数据库：

public class LYJApplication extendsApplication { privateDbManager.DaoConfig daoConfig; publicDbManager.DaoConfig getDaoConfig() { returndaoConfig; } @Override publicvoidonCreate() { super.onCreate(); x.Ext.init(this);//Xutils初始化 daoConfig =newDbManager.DaoConfig() .setDbName("lyj_db")//创建数据库的名称 .setDbVersion(1)//数据库版本号 .setDbUpgradeListener(newDbManager.DbUpgradeListener() { @Override publicvoidonUpgrade(DbManager db,intoldVersion,int newVersion) { // TODO: ... // db.addColumn(...); // db.dropTable(...); // ... } });//数据库更新操作 } }

上面的注释明了，有必要说明的一点是setDbDir(new File(“/sdcard”))，可以将数据库存储在你想存储的地方，如果不设置，那么数据库默认存储在/data/data/你的应用程序/database/xxx.db下。这里我们就默认放在应用程序下。
我们首先创建一个实体类，如下：
@Table(name="lyj_person") public class LYJPerson { @Column(name ="id", isId =true) privateintid; @Column(name ="name") privateString name; @Column(name ="age") privateString age; publicString getAge() { returnage; } publicvoidsetAge(String age) { this.age = age; } publicintgetId() { returnid; } publicvoidsetId(intid) { this.id = id; } publicString getName() { returnname; } publicvoidsetName(String name) { this.name = name; } }

###通过实体类可以直接操作数据库。
我们在Application中加入如下代码，向数据库添加数据：

DbManager db = x.getDb(daoConfig); LYJPerson person1=newLYJPerson(); person1.setName("liyuanjinglyj"); person1.setAge("23"); LYJPerson person2=newLYJPerson(); person2.setName("xutilsdemo"); person2.setAge("56"); try { db.save(person1); db.save(person2); } catch(DbException e) { e.printStackTrace(); }

####在Activity中操作获取数据库数据的代码如下：
DbManager db = x.getDb(((LYJApplication)getApplicationContext()).getDaoConfig()); try { List&lt;LYJPerson&gt; lyjPersons=db.selector(LYJPerson.class).findAll(); for(inti=0;i&lt;lyjPersons.size();i++){ Log.i("liyuanjinglyj","LYJPerson"+i+".name="+lyjPersons.get(i).getName()); Log.i("liyuanjinglyj","LYJPerson"+i+".name="+lyjPersons.get(i).getAge()); } } catch(DbException e) { e.printStackTrace(); }

那么肯定会得到如下结果：
*4.xUtils的网络请求 Android规定UI线程是不能涉及网络任务的，所以，这里主要简单介绍Xutils的异步网络请求，同步的自行探究。 *使用格式如下： RequestParams params =newRequestParams("http://blog.csdn.net/mobile/experts.html"); x.http().get(params,newCallback.CommonCallback&lt;String&gt;() { @Override publicvoidonSuccess(String result) { Document doc = Jsoup.parse(result); Element div = doc.select("div.list_3").get(0); Elements imgs = div.getElementsByTag("img"); for(inti = 0; i &lt; imgs.size(); i++) { Element img = imgs.get(i); Log.i("liyuanjinglyj",img.attr("alt")); } } @Override publicvoidonError(Throwable ex,booleanisOnCallback) { } @Override publicvoidonCancelled(Callback.CancelledException cex) { } @Override publicvoidonFinished() { } });

这里获取的是CSDN移动博客专家的HTML页面信息，看看下面的日志，就知道Xutils网络功能还是很强大的。
本文最后附带了一下粗略模仿CSDN APP的源码，有意者可以下载看看，里面用到另一个开发框架，我用来专门处理图片的（afinal）。都说xUtils是afinal的进化版，不过在图片方面，我们觉得xUtils还有点不足。 http://download.csdn.net/detail/liyuanjinglyj/9379103 5.导入xUtils工程到Android Studio 下载地址如下： https://github.com/wyouflf/xUtils3/tree/master ㈠将下载的工程复制到Project目录下：

㈡添加到settings.gradle文件： include ‘:app’,':xutils’ ㈢编译到工程中 dependencies { compile fileTree(dir: 'libs', include: ['*.jar']) compile 'com.android.support:appcompat-v7:23.0.1' compile project(':xutils') }

㈣将xutils文件夹下的build.gradle中的版本与最低版本调整到与创建工程一致
compileSdkVersion 23 buildToolsVersion "23.0.1"

defaultConfig { minSdkVersion 15 targetSdkVersion 23 versionCode 20151224 versionName version }

㈤添加如下代码到build.gradle（Project:XutilsDemo）中
dependencies { classpath 'com.android.tools.build:gradle:1.3.0' classpath 'com.jfrog.bintray.gradle:gradle-bintray-plugin:1.2' classpath 'com.github.dcendents:android-maven-gradle-plugin:1.3' // NOTE: Do not place your application dependencies here; they belong // in the individual module build.gradle files }

其中红色标记为添加的代码。点击Sync now就可以使用xUtils了。

## xUtils简介
* xUtils3 api变化较多, 已转至 https://github.com/wyouflf/xUtils3
* xUtils 2.x对Android 6.0兼容不是很好, 请尽快升级至xUtils3.
* xUtils 包含了很多实用的android工具。
* xUtils 支持大文件上传，更全面的http请求协议支持(10种谓词)，拥有更加灵活的ORM，更多的事件注解支持且不受混淆影响...
* xUitls 最低兼容android 2.2 (api level 8)

## 目前xUtils主要有四大模块：

* DbUtils模块：
  > * android中的orm框架，一行代码就可以进行增删改查；
  > * 支持事务，默认关闭；
  > * 可通过注解自定义表名，列名，外键，唯一性约束，NOT NULL约束，CHECK约束等（需要混淆的时候请注解表名和列名）；
  > * 支持绑定外键，保存实体时外键关联实体自动保存或更新；
  > * 自动加载外键关联实体，支持延时加载；
  > * 支持链式表达查询，更直观的查询语义，参考下面的介绍或sample中的例子。

* ViewUtils模块：
  > * android中的ioc框架，完全注解方式就可以进行UI，资源和事件绑定；
  > * 新的事件绑定方式，使用混淆工具混淆后仍可正常工作；
  > * 目前支持常用的20种事件绑定，参见ViewCommonEventListener类和包com.lidroid.xutils.view.annotation.event。

* HttpUtils模块：
  > * 支持同步，异步方式的请求；
  > * 支持大文件上传，上传大文件不会oom；
  > * 支持GET，POST，PUT，MOVE，COPY，DELETE，HEAD，OPTIONS，TRACE，CONNECT请求；
  > * 下载支持301/302重定向，支持设置是否根据Content-Disposition重命名下载的文件；
  > * 返回文本内容的请求(默认只启用了GET请求)支持缓存，可设置默认过期时间和针对当前请求的过期时间。

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
## 混淆时注意事项：

 * 添加Android默认混淆配置${sdk.dir}/tools/proguard/proguard-android.txt
 * 不要混淆xUtils中的注解类型，添加混淆配置：-keep class * extends java.lang.annotation.Annotation { *; }
 * 对使用DbUtils模块持久化的实体类不要混淆，或者注解所有表和列名称@Table(name="xxx")，@Id(column="xxx")，@Column(column="xxx"),@Foreign(column="xxx",foreign="xxx")；

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
List<Parent> list = db.findAll(Parent.class);//通过类型查找

Parent Parent = db.findFirst(Selector.from(Parent.class).where("name","=","test"));

// IS NULL
Parent Parent = db.findFirst(Selector.from(Parent.class).where("name","=", null));
// IS NOT NULL
Parent Parent = db.findFirst(Selector.from(Parent.class).where("name","!=", null));

// WHERE id<54 AND (age>20 OR age<30) ORDER BY id LIMIT pageSize OFFSET pageOffset
List<Parent> list = db.findAll(Selector.from(Parent.class)
                                   .where("id" ,"<", 54)
                                   .and(WhereBuilder.b("age", ">", 20).or("age", " < ", 30))
                                   .orderBy("id")
                                   .limit(pageSize)
                                   .offset(pageSize * pageIndex));

// op为"in"时，最后一个参数必须是数组或Iterable的实现类(例如List等)
Parent test = db.findFirst(Selector.from(Parent.class).where("id", "in", new int[]{1, 2, 3}));
// op为"between"时，最后一个参数必须是数组或Iterable的实现类(例如List等)
Parent test = db.findFirst(Selector.from(Parent.class).where("id", "between", new String[]{"1", "5"}));

DbModel dbModel = db.findDbModelAll(Selector.from(Parent.class).select("name"));//select("name")只取出name列
List<DbModel> dbModels = db.findDbModelAll(Selector.from(Parent.class).groupBy("name").select("name", "count(name)"));
...

List<DbModel> dbModels = db.findDbModelAll(sql); // 自定义sql查询
db.execNonQuery(sql) // 执行自定义sql
...

```

----
## ViewUtils使用方法
* 完全注解方式就可以进行UI绑定和事件绑定。
* 无需findViewById和setClickListener等。

```java
// xUtils的view注解要求必须提供id，以使代码混淆不受影响。
@ViewInject(R.id.textView)
TextView textView;

//@ViewInject(vale=R.id.textView, parentId=R.id.parentView)
//TextView textView;

@ResInject(id = R.string.label, type = ResType.String)
private String label;

// 取消了之前使用方法名绑定事件的方式，使用id绑定不受混淆影响
// 支持绑定多个id @OnClick({R.id.id1, R.id.id2, R.id.id3})
// or @OnClick(value={R.id.id1, R.id.id2, R.id.id3}, parentId={R.id.pid1, R.id.pid2, R.id.pid3})
// 更多事件支持参见ViewCommonEventListener类和包com.lidroid.xutils.view.annotation.event。
@OnClick(R.id.test_button)
public void testButtonClick(View v) { // 方法签名必须和接口中的要求一致
    ...
}
...
//在Activity中注入：
@Override
public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
    ViewUtils.inject(this); //注入view和事件
    ...
    textView.setText("some text...");
    ...
}
//在Fragment中注入：
@Override
public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.bitmap_fragment, container, false); // 加载fragment布局
    ViewUtils.inject(this, view); //注入view和事件
    ...
}
//在PreferenceFragment中注入：
public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    ViewUtils.inject(this, getPreferenceScreen()); //注入view和事件
    ...
}
// 其他重载
// inject(View view);
// inject(Activity activity)
// inject(PreferenceActivity preferenceActivity)
// inject(Object handler, View view)
// inject(Object handler, Activity activity)
// inject(Object handler, PreferenceGroup preferenceGroup)
// inject(Object handler, PreferenceActivity preferenceActivity)
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
        public void onLoading(long total, long current, boolean isUploading) {
            testTextView.setText(current + "/" + total);
        }

        @Override
        public void onSuccess(ResponseInfo<String> responseInfo) {
            textView.setText(responseInfo.result);
        }

        @Override
        public void onStart() {
        }

        @Override
        public void onFailure(HttpException error, String msg) {
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
// MultipartEntity,BodyParamsEntity,FileUploadEntity,InputStreamUploadEntity,StringEntity）。
// 例如发送json参数：params.setBodyEntity(new StringEntity(jsonStr,charset));
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
        public void onLoading(long total, long current, boolean isUploading) {
            if (isUploading) {
                testTextView.setText("upload: " + current + "/" + total);
            } else {
                testTextView.setText("reply: " + current + "/" + total);
            }
        }

        @Override
        public void onSuccess(ResponseInfo<String> responseInfo) {
            testTextView.setText("reply: " + responseInfo.result);
        }

        @Override
        public void onFailure(HttpException error, String msg) {
            testTextView.setText(error.getExceptionCode() + ":" + msg);
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
    true, // 如果目标文件存在，接着未完成的部分继续下载。服务器不支持RANGE时将从新下载。
    true, // 如果从请求返回信息中获取到文件名，下载完成后自动重命名。
    new RequestCallBack<File>() {

        @Override
        public void onStart() {
            testTextView.setText("conn...");
        }

        @Override
        public void onLoading(long total, long current, boolean isUploading) {
            testTextView.setText(current + "/" + total);
        }

        @Override
        public void onSuccess(ResponseInfo<File> responseInfo) {
            testTextView.setText("downloaded:" + responseInfo.result.getPath());
        }


        @Override
        public void onFailure(HttpException error, String msg) {
            testTextView.setText(msg);
        }
});

...
//调用cancel()方法停止下载
handler.cancel();
...
```

----
## BitmapUtils 使用方法

```java
BitmapUtils bitmapUtils = new BitmapUtils(this);

// 加载网络图片
bitmapUtils.display(testImageView, "http://bbs.lidroid.com/static/image/common/logo.png");

// 加载本地图片(路径以/开头， 绝对路径)
bitmapUtils.display(testImageView, "/sdcard/test.jpg");

// 加载assets中的图片(路径以assets开头)
bitmapUtils.display(testImageView, "assets/img/wallpaper.jpg");

// 使用ListView等容器展示图片时可通过PauseOnScrollListener控制滑动和快速滑动过程中时候暂停加载图片
listView.setOnScrollListener(new PauseOnScrollListener(bitmapUtils, false, true));
listView.setOnScrollListener(new PauseOnScrollListener(bitmapUtils, false, true, customListener));
```

----
## 其他（***更多示例代码见sample文件夹中的代码***）
### 输出日志 LogUtils

```java
// 自动添加TAG，格式： className.methodName(L:lineNumber)
// 可设置全局的LogUtils.allowD = false，LogUtils.allowI = false...，控制是否输出log。
// 自定义log输出LogUtils.customLogger = new xxxLogger();
LogUtils.d("wyouflf");
```

----
# 关于作者
* Email： <wyouflf@qq.com>, <wyouflf@gmail.com>
* 有任何建议或者使用中遇到问题都可以给我发邮件, 你也可以加入QQ群：330445659(已满), 275967695, 257323060，技术交流，idea分享 *_*
