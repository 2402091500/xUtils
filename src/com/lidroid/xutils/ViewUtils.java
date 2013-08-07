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
import com.lidroid.xutils.view.annotation.SeekBarChange;
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
                    try {
                        field.setAccessible(true);
                        field.set(target, activity.findViewById(viewInject.id()));
                        setEventListener(target, field, viewInject);
                    } catch (Exception e) {
                        LogUtils.e(e.getMessage(), e);
                    }
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
                    try {
                        field.setAccessible(true);
                        field.set(target, view.findViewById(viewInject.id()));
                        setEventListener(target, field, viewInject);
                    } catch (Exception e) {
                        LogUtils.e(e.getMessage(), e);
                    }
                }
            }
        }
    }


    private static void setEventListener(Object target, Field field, ViewInject viewInject) {
        String methodName = viewInject.click();
        if (!TextUtils.isEmpty(methodName)) {
            setViewClickListener(target, field, methodName);
        }

        methodName = viewInject.longClick();
        if (!TextUtils.isEmpty(methodName)) {
            setViewLongClickListener(target, field, methodName);
        }

        methodName = viewInject.itemClick();
        if (!TextUtils.isEmpty(methodName)) {
            setItemClickListener(target, field, methodName);
        }

        methodName = viewInject.itemLongClick();
        if (!TextUtils.isEmpty(methodName)) {
            setItemLongClickListener(target, field, methodName);
        }

        methodName = viewInject.checkedChanged();
        if (!TextUtils.isEmpty(methodName)) {
            if (RadioGroup.class.isAssignableFrom(field.getType())) {
                setRadioGroupCheckedChangedListener(target, field, methodName);
            } else if (CompoundButton.class.isAssignableFrom(field.getType())) {
                setCompoundButtonCheckedChangedListener(target, field, methodName);
            }
        }

        methodName = viewInject.preferenceChange();
        if (!TextUtils.isEmpty(methodName)) {
            setPreferenceChangeListener(target, field, methodName);
        }

        methodName = viewInject.tabChanged();
        if (!TextUtils.isEmpty(methodName)) {
            setTabChangedListener(target, field, methodName);
        }

        methodName = viewInject.scrollChanged();
        if (!TextUtils.isEmpty(methodName)) {
            setScrollChangedListener(target, field, methodName);
        }

        Select select = viewInject.select();
        if (!TextUtils.isEmpty(select.selected())) {
            setViewSelectListener(target, field, select.selected(), select.noSelected());
        }

        SeekBarChange seekBarChange = viewInject.seekBarChange();
        if (!TextUtils.isEmpty(seekBarChange.progressChanged())) {
            setSeekBarChangeListener(target, field, seekBarChange.progressChanged(), seekBarChange.startTrackingTouch(), seekBarChange.stopTrackingTouch());
        }
    }

    private static void setViewClickListener(Object target, Field field, String methodName) {
        try {
            Object obj = field.get(target);
            if (obj instanceof View) {
                ((View) obj).setOnClickListener(new ViewCommonEventListener(target).click(methodName));
            }
        } catch (Exception e) {
            LogUtils.e(e.getMessage(), e);
        }
    }

    private static void setViewLongClickListener(Object target, Field field, String methodName) {
        try {
            Object obj = field.get(target);
            if (obj instanceof View) {
                ((View) obj).setOnLongClickListener(new ViewCommonEventListener(target).longClick(methodName));
            }
        } catch (Exception e) {
            LogUtils.e(e.getMessage(), e);
        }
    }

    private static void setItemClickListener(Object target, Field field, String methodName) {
        try {
            Object obj = field.get(target);
            if (obj instanceof AbsListView) {
                ((AbsListView) obj).setOnItemClickListener(new ViewCommonEventListener(target).itemClick(methodName));
            }
        } catch (Exception e) {
            LogUtils.e(e.getMessage(), e);
        }
    }

    private static void setItemLongClickListener(Object target, Field field, String methodName) {
        try {
            Object obj = field.get(target);
            if (obj instanceof AbsListView) {
                ((AbsListView) obj).setOnItemLongClickListener(new ViewCommonEventListener(target).itemLongClick(methodName));
            }
        } catch (Exception e) {
            LogUtils.e(e.getMessage(), e);
        }
    }

    private static void setRadioGroupCheckedChangedListener(Object target, Field field, String methodName) {
        try {
            Object obj = field.get(target);
            if (obj instanceof RadioGroup) {
                ((RadioGroup) obj).setOnCheckedChangeListener(new ViewCommonEventListener(target).radioGroupCheckedChanged(methodName));
            }
        } catch (Exception e) {
            LogUtils.e(e.getMessage(), e);
        }
    }

    private static void setCompoundButtonCheckedChangedListener(Object target, Field field, String methodName) {
        try {
            Object obj = field.get(target);
            if (obj instanceof CompoundButton) {
                ((CompoundButton) obj).setOnCheckedChangeListener(new ViewCommonEventListener(target).compoundButtonCheckedChanged(methodName));
            }
        } catch (Exception e) {
            LogUtils.e(e.getMessage(), e);
        }
    }

    private static void setPreferenceChangeListener(Object target, Field field, String methodName) {
        try {
            Object obj = field.get(target);
            if (obj instanceof Preference) {
                ((Preference) obj).setOnPreferenceChangeListener(new ViewCommonEventListener(target).preferenceChange(methodName));
            }
        } catch (Exception e) {
            LogUtils.e(e.getMessage(), e);
        }
    }

    private static void setTabChangedListener(Object target, Field field, String methodName) {
        try {
            Object obj = field.get(target);
            if (obj instanceof TabHost) {
                ((TabHost) obj).setOnTabChangedListener(new ViewCommonEventListener(target).tabChanged(methodName));
            }
        } catch (Exception e) {
            LogUtils.e(e.getMessage(), e);
        }
    }

    private static void setScrollChangedListener(Object target, Field field, String methodName) {
        try {
            Object obj = field.get(target);
            if (obj instanceof ViewTreeObserver) {
                ((ViewTreeObserver) obj).addOnScrollChangedListener(new ViewCommonEventListener(target).scrollChanged(methodName));
            }
        } catch (Exception e) {
            LogUtils.e(e.getMessage(), e);
        }
    }

    private static void setViewSelectListener(Object target, Field field, String select, String noSelect) {
        try {
            Object obj = field.get(target);
            if (obj instanceof View) {
                ((AbsListView) obj).setOnItemSelectedListener(new ViewCommonEventListener(target).selected(select).noSelected(noSelect));
            }
        } catch (Exception e) {
            LogUtils.e(e.getMessage(), e);
        }
    }

    private static void setSeekBarChangeListener(Object target, Field field, String progressChanged, String startTrackingTouch, String stopTrackingTouch) {
        try {
            Object obj = field.get(target);
            if (obj instanceof SeekBar) {
                ((SeekBar) obj).setOnSeekBarChangeListener(new ViewCommonEventListener(target).progressChanged(progressChanged).startTrackingTouch(startTrackingTouch).stopTrackingTouch(stopTrackingTouch));
            }
        } catch (Exception e) {
            LogUtils.e(e.getMessage(), e);
        }
    }

}
