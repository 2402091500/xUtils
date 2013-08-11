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
import android.preference.Preference;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.*;
import com.lidroid.xutils.util.LogUtils;
import com.lidroid.xutils.view.ViewCommonEventListener;
import com.lidroid.xutils.view.annotation.SeekBarChange;
import com.lidroid.xutils.view.annotation.Select;
import com.lidroid.xutils.view.annotation.ViewInject;

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

    public static void inject(Object handler, Activity activity) {
        injectObject(handler, activity);
    }

    public static void inject(Object handler, View view) {
        injectObject(handler, view);
    }


    private static void injectObject(Object handler, Activity activity) {
        Field[] fields = handler.getClass().getDeclaredFields();
        if (fields != null && fields.length > 0) {
            for (Field field : fields) {
                ViewInject viewInject = field.getAnnotation(ViewInject.class);
                if (viewInject != null) {
                    try {
                        field.setAccessible(true);
                        field.set(handler, activity.findViewById(viewInject.id()));
                        setEventListener(handler, field, viewInject);
                    } catch (Exception e) {
                        LogUtils.e(e.getMessage(), e);
                    }
                }
            }
        }
    }

    private static void injectObject(Object handler, View view) {
        Field[] fields = handler.getClass().getDeclaredFields();
        if (fields != null && fields.length > 0) {
            for (Field field : fields) {
                ViewInject viewInject = field.getAnnotation(ViewInject.class);
                if (viewInject != null) {
                    try {
                        field.setAccessible(true);
                        field.set(handler, view.findViewById(viewInject.id()));
                        setEventListener(handler, field, viewInject);
                    } catch (Exception e) {
                        LogUtils.e(e.getMessage(), e);
                    }
                }
            }
        }
    }


    private static void setEventListener(Object handler, Field field, ViewInject viewInject) {
        String methodName = viewInject.click();
        if (!TextUtils.isEmpty(methodName)) {
            setViewClickListener(handler, field, methodName);
        }

        methodName = viewInject.longClick();
        if (!TextUtils.isEmpty(methodName)) {
            setViewLongClickListener(handler, field, methodName);
        }

        methodName = viewInject.itemClick();
        if (!TextUtils.isEmpty(methodName)) {
            setItemClickListener(handler, field, methodName);
        }

        methodName = viewInject.itemLongClick();
        if (!TextUtils.isEmpty(methodName)) {
            setItemLongClickListener(handler, field, methodName);
        }

        methodName = viewInject.checkedChanged();
        if (!TextUtils.isEmpty(methodName)) {
            if (RadioGroup.class.isAssignableFrom(field.getType())) {
                setRadioGroupCheckedChangedListener(handler, field, methodName);
            } else if (CompoundButton.class.isAssignableFrom(field.getType())) {
                setCompoundButtonCheckedChangedListener(handler, field, methodName);
            }
        }

        methodName = viewInject.preferenceChange();
        if (!TextUtils.isEmpty(methodName)) {
            setPreferenceChangeListener(handler, field, methodName);
        }

        methodName = viewInject.tabChanged();
        if (!TextUtils.isEmpty(methodName)) {
            setTabChangedListener(handler, field, methodName);
        }

        methodName = viewInject.scrollChanged();
        if (!TextUtils.isEmpty(methodName)) {
            setScrollChangedListener(handler, field, methodName);
        }

        Select select = viewInject.select();
        if (!TextUtils.isEmpty(select.selected())) {
            setViewSelectListener(handler, field, select.selected(), select.noSelected());
        }

        SeekBarChange seekBarChange = viewInject.seekBarChange();
        if (!TextUtils.isEmpty(seekBarChange.progressChanged())) {
            setSeekBarChangeListener(handler, field, seekBarChange.progressChanged(), seekBarChange.startTrackingTouch(), seekBarChange.stopTrackingTouch());
        }
    }

    private static void setViewClickListener(Object handler, Field field, String methodName) {
        try {
            Object obj = field.get(handler);
            if (obj instanceof View) {
                ((View) obj).setOnClickListener(new ViewCommonEventListener(handler).click(methodName));
            }
        } catch (Exception e) {
            LogUtils.e(e.getMessage(), e);
        }
    }

    private static void setViewLongClickListener(Object handler, Field field, String methodName) {
        try {
            Object obj = field.get(handler);
            if (obj instanceof View) {
                ((View) obj).setOnLongClickListener(new ViewCommonEventListener(handler).longClick(methodName));
            }
        } catch (Exception e) {
            LogUtils.e(e.getMessage(), e);
        }
    }

    private static void setItemClickListener(Object handler, Field field, String methodName) {
        try {
            Object obj = field.get(handler);
            if (obj instanceof AbsListView) {
                ((AbsListView) obj).setOnItemClickListener(new ViewCommonEventListener(handler).itemClick(methodName));
            }
        } catch (Exception e) {
            LogUtils.e(e.getMessage(), e);
        }
    }

    private static void setItemLongClickListener(Object handler, Field field, String methodName) {
        try {
            Object obj = field.get(handler);
            if (obj instanceof AbsListView) {
                ((AbsListView) obj).setOnItemLongClickListener(new ViewCommonEventListener(handler).itemLongClick(methodName));
            }
        } catch (Exception e) {
            LogUtils.e(e.getMessage(), e);
        }
    }

    private static void setRadioGroupCheckedChangedListener(Object handler, Field field, String methodName) {
        try {
            Object obj = field.get(handler);
            if (obj instanceof RadioGroup) {
                ((RadioGroup) obj).setOnCheckedChangeListener(new ViewCommonEventListener(handler).radioGroupCheckedChanged(methodName));
            }
        } catch (Exception e) {
            LogUtils.e(e.getMessage(), e);
        }
    }

    private static void setCompoundButtonCheckedChangedListener(Object handler, Field field, String methodName) {
        try {
            Object obj = field.get(handler);
            if (obj instanceof CompoundButton) {
                ((CompoundButton) obj).setOnCheckedChangeListener(new ViewCommonEventListener(handler).compoundButtonCheckedChanged(methodName));
            }
        } catch (Exception e) {
            LogUtils.e(e.getMessage(), e);
        }
    }

    private static void setPreferenceChangeListener(Object handler, Field field, String methodName) {
        try {
            Object obj = field.get(handler);
            if (obj instanceof Preference) {
                ((Preference) obj).setOnPreferenceChangeListener(new ViewCommonEventListener(handler).preferenceChange(methodName));
            }
        } catch (Exception e) {
            LogUtils.e(e.getMessage(), e);
        }
    }

    private static void setTabChangedListener(Object handler, Field field, String methodName) {
        try {
            Object obj = field.get(handler);
            if (obj instanceof TabHost) {
                ((TabHost) obj).setOnTabChangedListener(new ViewCommonEventListener(handler).tabChanged(methodName));
            }
        } catch (Exception e) {
            LogUtils.e(e.getMessage(), e);
        }
    }

    private static void setScrollChangedListener(Object handler, Field field, String methodName) {
        try {
            Object obj = field.get(handler);
            if (obj instanceof ViewTreeObserver) {
                ((ViewTreeObserver) obj).addOnScrollChangedListener(new ViewCommonEventListener(handler).scrollChanged(methodName));
            }
        } catch (Exception e) {
            LogUtils.e(e.getMessage(), e);
        }
    }

    private static void setViewSelectListener(Object handler, Field field, String select, String noSelect) {
        try {
            Object obj = field.get(handler);
            if (obj instanceof View) {
                ((AbsListView) obj).setOnItemSelectedListener(new ViewCommonEventListener(handler).selected(select).noSelected(noSelect));
            }
        } catch (Exception e) {
            LogUtils.e(e.getMessage(), e);
        }
    }

    private static void setSeekBarChangeListener(Object handler, Field field, String progressChanged, String startTrackingTouch, String stopTrackingTouch) {
        try {
            Object obj = field.get(handler);
            if (obj instanceof SeekBar) {
                ((SeekBar) obj).setOnSeekBarChangeListener(new ViewCommonEventListener(handler).progressChanged(progressChanged).startTrackingTouch(startTrackingTouch).stopTrackingTouch(stopTrackingTouch));
            }
        } catch (Exception e) {
            LogUtils.e(e.getMessage(), e);
        }
    }

}
