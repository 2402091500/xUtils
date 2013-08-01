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
import android.app.Fragment;
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
        injectActivity(activity);
    }

    public static void inject(View view) {
        injectView(view);
    }

    public static void inject(Fragment fragment) {
        injectFragment(fragment);
    }

    private static void injectActivity(Activity activity) {
        Field[] fields = activity.getClass().getDeclaredFields();
        if (fields != null && fields.length > 0) {
            for (Field field : fields) {
                ViewInject viewInject = field.getAnnotation(ViewInject.class);
                if (viewInject != null) {
                    int viewId = viewInject.id();
                    try {
                        field.setAccessible(true);
                        field.set(activity, activity.findViewById(viewId));
                    } catch (Exception e) {
                        LogUtils.e(e.getMessage(), e);
                    }

                    String clickMethod = viewInject.click();
                    if (!TextUtils.isEmpty(clickMethod))
                        setViewClickListener(activity, field, clickMethod);

                    String longClickMethod = viewInject.longClick();
                    if (!TextUtils.isEmpty(longClickMethod))
                        setViewLongClickListener(activity, field, longClickMethod);

                    String itemClickMethod = viewInject.itemClick();
                    if (!TextUtils.isEmpty(itemClickMethod))
                        setItemClickListener(activity, field, itemClickMethod);

                    String itemLongClickMethod = viewInject.itemLongClick();
                    if (!TextUtils.isEmpty(itemLongClickMethod))
                        setItemLongClickListener(activity, field, itemLongClickMethod);

                    Select select = viewInject.select();
                    if (!TextUtils.isEmpty(select.selected()))
                        setViewSelectListener(activity, field, select.selected(), select.noSelected());

                }
            }
        }
    }

    private static void injectView(View view) {
        Field[] fields = view.getClass().getDeclaredFields();
        if (fields != null && fields.length > 0) {
            for (Field field : fields) {
                ViewInject viewInject = field.getAnnotation(ViewInject.class);
                if (viewInject != null) {
                    int viewId = viewInject.id();
                    try {
                        field.setAccessible(true);
                        field.set(view, view.findViewById(viewId));
                    } catch (Exception e) {
                        LogUtils.e(e.getMessage(), e);
                    }

                    String clickMethod = viewInject.click();
                    if (!TextUtils.isEmpty(clickMethod))
                        setViewClickListener(view, field, clickMethod);

                    String longClickMethod = viewInject.longClick();
                    if (!TextUtils.isEmpty(longClickMethod))
                        setViewLongClickListener(view, field, longClickMethod);

                    String itemClickMethod = viewInject.itemClick();
                    if (!TextUtils.isEmpty(itemClickMethod))
                        setItemClickListener(view, field, itemClickMethod);

                    String itemLongClickMethod = viewInject.itemLongClick();
                    if (!TextUtils.isEmpty(itemLongClickMethod))
                        setItemLongClickListener(view, field, itemLongClickMethod);

                    Select select = viewInject.select();
                    if (!TextUtils.isEmpty(select.selected()))
                        setViewSelectListener(view, field, select.selected(), select.noSelected());

                }
            }
        }
    }

    private static void injectFragment(Fragment fragment) {
        View fragmentView = fragment.getView();
        if (fragmentView == null) return;
        Field[] fields = fragment.getClass().getDeclaredFields();
        if (fields != null && fields.length > 0) {
            for (Field field : fields) {
                ViewInject viewInject = field.getAnnotation(ViewInject.class);
                if (viewInject != null) {
                    int viewId = viewInject.id();
                    try {
                        field.setAccessible(true);
                        field.set(fragment, fragmentView.findViewById(viewId));
                    } catch (Exception e) {
                        LogUtils.e(e.getMessage(), e);
                    }

                    String clickMethod = viewInject.click();
                    if (!TextUtils.isEmpty(clickMethod))
                        setViewClickListener(fragment, field, clickMethod);

                    String longClickMethod = viewInject.longClick();
                    if (!TextUtils.isEmpty(longClickMethod))
                        setViewLongClickListener(fragment, field, longClickMethod);

                    String itemClickMethod = viewInject.itemClick();
                    if (!TextUtils.isEmpty(itemClickMethod))
                        setItemClickListener(fragment, field, itemClickMethod);

                    String itemLongClickMethod = viewInject.itemLongClick();
                    if (!TextUtils.isEmpty(itemLongClickMethod))
                        setItemLongClickListener(fragment, field, itemLongClickMethod);

                    Select select = viewInject.select();
                    if (!TextUtils.isEmpty(select.selected()))
                        setViewSelectListener(fragment, field, select.selected(), select.noSelected());

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
