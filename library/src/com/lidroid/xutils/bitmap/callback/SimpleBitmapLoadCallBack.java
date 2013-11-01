/*
 * Copyright (c) 2013. wyouflf (wyouflf@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lidroid.xutils.bitmap.callback;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.view.View;
import android.view.animation.Animation;
import com.lidroid.xutils.bitmap.BitmapDisplayConfig;

import java.lang.reflect.Method;

public class SimpleBitmapLoadCallBack<T extends View> implements BitmapLoadCallBack<T> {

    @Override
    public void onLoadStarted(T container, String uri, BitmapDisplayConfig config) {
    }

    @Override
    public void onLoadCompleted(T container, String url, Bitmap bitmap, BitmapDisplayConfig config, BitmapLoadFrom from) {
        Animation animation = config.getAnimation();
        if (animation == null) {
            fadeInDisplay(container, bitmap);
        } else {
            animationDisplay(container, bitmap, animation);
        }
    }

    @Override
    public void onLoadFailed(T container, String url, Drawable drawable) {
        if (bitmapSetter != null) {
            bitmapSetter.setDrawable(container, drawable);
        } else {
            container.setBackgroundDrawable(drawable);
        }
    }

    private BitmapSetter<T> bitmapSetter;

    @Override
    public void setBitmapSetter(BitmapSetter<T> bitmapSetter) {
        this.bitmapSetter = bitmapSetter;
    }

    @Override
    public BitmapSetter<T> getBitmapSetter() {
        return bitmapSetter;
    }

    private static final ColorDrawable transparentDrawable = new ColorDrawable(android.R.color.transparent);

    private void fadeInDisplay(T container, Bitmap bitmap) {
        final TransitionDrawable drawable =
                new TransitionDrawable(new Drawable[]{
                        transparentDrawable,
                        new BitmapDrawable(container.getResources(), bitmap)
                });
        if (bitmapSetter != null) {
            bitmapSetter.setDrawable(container, drawable);
        } else {
            container.setBackgroundDrawable(drawable);
        }
        drawable.startTransition(300);
    }

    private void animationDisplay(T container, Bitmap bitmap, Animation animation) {
        if (bitmapSetter != null) {
            bitmapSetter.setBitmap(container, bitmap);
        } else {
            container.setBackgroundDrawable(new BitmapDrawable(container.getResources(), bitmap));
        }
        try {
            Method cloneMethod = Animation.class.getDeclaredMethod("clone");
            cloneMethod.setAccessible(true);
            container.startAnimation((Animation) cloneMethod.invoke(animation));
        } catch (Throwable e) {
            container.startAnimation(animation);
        }
    }
}
