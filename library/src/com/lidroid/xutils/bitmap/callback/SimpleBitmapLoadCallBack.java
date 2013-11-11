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
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.animation.Animation;
import com.lidroid.xutils.bitmap.BitmapDisplayConfig;

import java.lang.reflect.Method;

public class SimpleBitmapLoadCallBack<T extends View> extends BitmapLoadCallBack<T> {

    @Override
    public void onLoadCompleted(T container, String url, Bitmap bitmap, BitmapDisplayConfig config, BitmapLoadFrom from) {
        Animation animation = config.getAnimation();
        if (animation == null) {
            BitmapSetter<T> setter = this.getBitmapSetter();
            if (getBitmapSetter() != null) {
                setter.setBitmap(container, bitmap);
            } else {
                container.setBackgroundDrawable(new BitmapDrawable(container.getResources(), bitmap));
            }
        } else {
            animationDisplay(container, bitmap, animation);
        }
    }

    @Override
    public void onLoadFailed(T container, String url, Drawable drawable) {
        BitmapSetter<T> setter = this.getBitmapSetter();
        if (setter != null) {
            setter.setDrawable(container, drawable);
        } else {
            container.setBackgroundDrawable(drawable);
        }
    }

    private void animationDisplay(T container, Bitmap bitmap, Animation animation) {
        BitmapSetter<T> setter = this.getBitmapSetter();
        if (setter != null) {
            setter.setBitmap(container, bitmap);
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
