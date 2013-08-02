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

package com.lidroid.xutils;

import android.app.Activity;
import android.text.TextUtils;
import android.view.View;
import android.widget.AbsListView;
import com.lidroid.xutils.util.LogUtils;
import com.lidroid.xutils.view.annotation.Select;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.event.ViewCommonEventListener;

import java.lang.reflect.Field;

public class ViewUtils {

    private ViewUtils() {
    }

    public static void inject(Activity activity) {
        injectObject(activity, activity);
    }

    public static void inject(View view) {
        injectObject(view, view);
    }

    public static void inject(Object target, Activity activity) {
        injectObject(target, activity);
    }

    public static void inject(Object target, View view) {
        injectObject(target, view);
    }


    private static void injectObject(Object target, Activity activity) {
        Field[] fields = target.getClass().getDeclaredFields();
        if (fields != null && fields.length > 0) {
            for (Field field : fields) {
                ViewInject viewInject = field.getAnnotation(ViewInject.class);
                if (viewInject != null) {
                    int viewId = viewInject.id();
                    try {
                        field.setAccessible(true);
                        field.set(target, activity.findViewById(viewId));
                    } catch (Exception e) {
                        LogUtils.e(e.getMessage(), e);
                    }

                    String clickMethod = viewInject.click();
                    if (!TextUtils.isEmpty(clickMethod))
                        setViewClickListener(target, field, clickMethod);

                    String longClickMethod = viewInject.longClick();
                    if (!TextUtils.isEmpty(longClickMethod))
                        setViewLongClickListener(target, field, longClickMethod);

                    String itemClickMethod = viewInject.itemClick();
                    if (!TextUtils.isEmpty(itemClickMethod))
                        setItemClickListener(target, field, itemClickMethod);

                    String itemLongClickMethod = viewInject.itemLongClick();
                    if (!TextUtils.isEmpty(itemLongClickMethod))
                        setItemLongClickListener(target, field, itemLongClickMethod);

                    Select select = viewInject.select();
                    if (!TextUtils.isEmpty(select.selected()))
                        setViewSelectListener(target, field, select.selected(), select.noSelected());

                }
            }
        }
    }

    private static void injectObject(Object target, View view) {
        Field[] fields = target.getClass().getDeclaredFields();
        if (fields != null && fields.length > 0) {
            for (Field field : fields) {
                ViewInject viewInject = field.getAnnotation(ViewInject.class);
                if (viewInject != null) {
                    int viewId = viewInject.id();
                    try {
                        field.setAccessible(true);
                        field.set(target, view.findViewById(viewId));
                    } catch (Exception e) {
                        LogUtils.e(e.getMessage(), e);
                    }

                    String clickMethod = viewInject.click();
                    if (!TextUtils.isEmpty(clickMethod))
                        setViewClickListener(target, field, clickMethod);

                    String longClickMethod = viewInject.longClick();
                    if (!TextUtils.isEmpty(longClickMethod))
                        setViewLongClickListener(target, field, longClickMethod);

                    String itemClickMethod = viewInject.itemClick();
                    if (!TextUtils.isEmpty(itemClickMethod))
                        setItemClickListener(target, field, itemClickMethod);

                    String itemLongClickMethod = viewInject.itemLongClick();
                    if (!TextUtils.isEmpty(itemLongClickMethod))
                        setItemLongClickListener(target, field, itemLongClickMethod);

                    Select select = viewInject.select();
                    if (!TextUtils.isEmpty(select.selected()))
                        setViewSelectListener(target, field, select.selected(), select.noSelected());

                }
            }
        }
    }


    private static void setViewClickListener(Object target, Field field, String clickMethod) {
        try {
            Object obj = field.get(target);
            if (obj instanceof View) {
                ((View) obj).setOnClickListener(new ViewCommonEventListener(target).click(clickMethod));
            }
        } catch (Exception e) {
            LogUtils.e(e.getMessage(), e);
        }
    }

    private static void setViewLongClickListener(Object target, Field field, String clickMethod) {
        try {
            Object obj = field.get(target);
            if (obj instanceof View) {
                ((View) obj).setOnLongClickListener(new ViewCommonEventListener(target).longClick(clickMethod));
            }
        } catch (Exception e) {
            LogUtils.e(e.getMessage(), e);
        }
    }

    private static void setItemClickListener(Object target, Field field, String itemClickMethod) {
        try {
            Object obj = field.get(target);
            if (obj instanceof AbsListView) {
                ((AbsListView) obj).setOnItemClickListener(new ViewCommonEventListener(target).itemClick(itemClickMethod));
            }
        } catch (Exception e) {
            LogUtils.e(e.getMessage(), e);
        }
    }

    private static void setItemLongClickListener(Object target, Field field, String itemClickMethod) {
        try {
            Object obj = field.get(target);
            if (obj instanceof AbsListView) {
                ((AbsListView) obj).setOnItemLongClickListener(new ViewCommonEventListener(target).itemLongClick(itemClickMethod));
            }
        } catch (Exception e) {
            LogUtils.e(e.getMessage(), e);
        }
    }

    private static void setViewSelectListener(Object target, Field field, String select, String noSelect) {
        try {
            Object obj = field.get(target);
            if (obj instanceof View) {
                ((AbsListView) obj).setOnItemSelectedListener(new ViewCommonEventListener(target).select(select).noSelect(noSelect));
            }
        } catch (Exception e) {
            LogUtils.e(e.getMessage(), e);
        }
    }

}
