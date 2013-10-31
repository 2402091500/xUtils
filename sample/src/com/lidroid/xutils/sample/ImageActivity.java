package com.lidroid.xutils.sample;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;
import com.lidroid.xutils.BitmapUtils;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.bitmap.BitmapDisplayConfig;
import com.lidroid.xutils.bitmap.callback.SimpleImageLoadCallBack;
import com.lidroid.xutils.bitmap.core.BitmapCommonUtils;
import com.lidroid.xutils.sample.fragment.BitmapFragment;
import com.lidroid.xutils.view.annotation.ViewInject;

/**
 * Author: wyouflf
 * Date: 13-10-9
 * Time: 下午5:26
 */
public class ImageActivity extends Activity {

    @ViewInject(R.id.big_img)
    private ImageView bigImage;

    private BitmapUtils bitmapUtils;

    private BitmapDisplayConfig bigPicDisplayConfig;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image);

        ViewUtils.inject(this);

        String imgUrl = getIntent().getStringExtra("url");

        bitmapUtils = BitmapFragment.bitmapUtils;
        if (bitmapUtils == null) {
            bitmapUtils = new BitmapUtils(this);
        }

        bigPicDisplayConfig = new BitmapDisplayConfig(this);
        //bigPicDisplayConfig.setShowOriginal(true); // 显示原始图片,不压缩, 尽量不要使用, 图片太大时容易OOM。
        bigPicDisplayConfig.setBitmapConfig(Bitmap.Config.RGB_565);
        bigPicDisplayConfig.setBitmapMaxWidth(BitmapCommonUtils.getScreenWidth(this));
        bigPicDisplayConfig.setBitmapMaxHeight(BitmapCommonUtils.getScreenHeight(this));

        bigPicDisplayConfig.setImageLoadCallBack(new SimpleImageLoadCallBack() {
            @Override
            public void onLoadStarted(String uri, BitmapDisplayConfig config) {
                Toast.makeText(getApplicationContext(), uri, 1000).show();
            }

            @Override
            public void onLoadCompleted(String uri, ImageView imageView, Drawable drawable, BitmapDisplayConfig config) {
                super.onLoadCompleted(uri, imageView, drawable, config);
                if (drawable instanceof BitmapDrawable) {
                    int w = ((BitmapDrawable) drawable).getBitmap().getWidth();
                    int h = ((BitmapDrawable) drawable).getBitmap().getHeight();
                    Toast.makeText(getApplicationContext(), w + "*" + h, 1000).show();
                }
            }
        });

        bitmapUtils.display(bigImage, imgUrl, bigPicDisplayConfig);
    }
}