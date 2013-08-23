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
import android.preference.PreferenceActivity;
import android.view.View;
import android.widget.*;
import com.lidroid.xutils.util.LogUtils;
import com.lidroid.xutils.util.core.DoubleKeyValueMap;
import com.lidroid.xutils.view.ViewCommonEventListener;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

public class ViewUtils {

    private ViewUtils() {
    }

    public static void inject(View view) {
        injectObject(view, new Finder(view));
    }

    public static void inject(Activity activity) {
        injectObject(activity, new Finder(activity));
    }

    public static void inject(PreferenceActivity preferenceActivity) {
        injectObject(preferenceActivity, new Finder(preferenceActivity));
    }

    public static void inject(Object handler, View view) {
        injectObject(handler, new Finder(view));
    }

    public static void inject(Object handler, Activity activity) {
        injectObject(handler, new Finder(activity));
    }

    public static void inject(Object handler, PreferenceActivity preferenceActivity) {
        injectObject(handler, new Finder(preferenceActivity));
    }


    @SuppressWarnings("ConstantConditions")
    private static void injectObject(Object handler, Finder finder) {

        // inject view
        Field[] fields = handler.getClass().getDeclaredFields();
        if (fields != null && fields.length > 0) {
            for (Field field : fields) {
                ViewInject viewInject = field.getAnnotation(ViewInject.class);
                if (viewInject != null) {
                    try {
                        field.setAccessible(true);
                        field.set(handler, finder.findViewById(viewInject.value()));
                    } catch (Exception e) {
                        LogUtils.e(e.getMessage(), e);
                    }
                }
            }
        }

        // inject event
        Method[] methods = handler.getClass().getDeclaredMethods();
        if (methods != null && methods.length > 0) {
            String eventName = OnClick.class.getCanonicalName();
            String prefix = eventName.substring(0, eventName.lastIndexOf('.'));
            DoubleKeyValueMap<Object, Annotation, Method> id_annotation_method_map = new DoubleKeyValueMap<Object, Annotation, Method>();
            for (Method method : methods) {
                Annotation[] annotations = method.getDeclaredAnnotations();
                if (annotations != null && annotations.length > 0) {
                    for (Annotation annotation : annotations) {
                        if (annotation.annotationType().getCanonicalName().startsWith(prefix)) {
                            try {
                                // ProGuard：-keep class * extends java.lang.annotation.Annotation { *; }
                                Method valueMethod = annotation.annotationType().getDeclaredMethod("value");
                                Object value = valueMethod.invoke(annotation);
                                if (value.getClass().isArray()) {
                                    int len = Array.getLength(value);
                                    for (int i = 0; i < len; i++) {
                                        id_annotation_method_map.put(Array.get(value, i), annotation, method);
                                    }
                                } else {
                                    id_annotation_method_map.put(value, annotation, method);
                                }
                            } catch (Exception e) {
                                LogUtils.e(e.getMessage(), e);
                            }
                        }
                    }
                }
            }
            setEventListener(handler, finder, id_annotation_method_map);
        }
    }

    @SuppressWarnings("ConstantConditions")
    private static void setEventListener(Object handler, Finder finder, DoubleKeyValueMap<Object, Annotation, Method> id_annotation_method_map) {
        for (Object id : id_annotation_method_map.getFirstKeys()) {
            ConcurrentHashMap<Annotation, Method> annotation_method_map = id_annotation_method_map.get(id);
            for (Annotation annotation : annotation_method_map.keySet()) {
                try {
                    Method method = annotation_method_map.get(annotation);
                    if (annotation.annotationType().equals(OnClick.class)) {
                        View view = finder.findViewById((Integer) id);
                        view.setOnClickListener(new ViewCommonEventListener(handler).click(method));
                    } else if (annotation.annotationType().equals(OnLongClick.class)) {
                        View view = finder.findViewById((Integer) id);
                        view.setOnLongClickListener(new ViewCommonEventListener(handler).longClick(method));
                    } else if (annotation.annotationType().equals(OnItemClick.class)) {
                        View view = finder.findViewById((Integer) id);
                        ((AdapterView) view).setOnItemClickListener(new ViewCommonEventListener(handler).itemClick(method));
                    } else if (annotation.annotationType().equals(OnItemLongClick.class)) {
                        View view = finder.findViewById((Integer) id);
                        ((AdapterView) view).setOnItemLongClickListener(new ViewCommonEventListener(handler).itemLongClick(method));
                    } else if (annotation.annotationType().equals(OnCheckedChange.class)) {
                        View view = finder.findViewById((Integer) id);
                        if (view instanceof RadioGroup) {
                            ((RadioGroup) view).setOnCheckedChangeListener(new ViewCommonEventListener(handler).radioGroupCheckedChanged(method));
                        } else if (view instanceof CompoundButton) {
                            ((CompoundButton) view).setOnCheckedChangeListener(new ViewCommonEventListener(handler).compoundButtonCheckedChanged(method));
                        }
                    } else if (annotation.annotationType().equals(OnPreferenceChange.class)) {
                        Preference preference = finder.findPreference(id.toString());
                        preference.setOnPreferenceChangeListener(new ViewCommonEventListener(handler).preferenceChange(method));
                    } else if (annotation.annotationType().equals(OnTabChange.class)) {
                        View view = finder.findViewById((Integer) id);
                        ((TabHost) view).setOnTabChangedListener(new ViewCommonEventListener(handler).tabChanged(method));
                    } else if (annotation.annotationType().equals(OnScrollChanged.class)) {
                        View view = finder.findViewById((Integer) id);
                        view.getViewTreeObserver().addOnScrollChangedListener(new ViewCommonEventListener(handler).scrollChanged(method));
                    } else if (annotation.annotationType().equals(OnItemSelected.class)) {
                        View view = finder.findViewById((Integer) id);
                        ViewCommonEventListener listener = new ViewCommonEventListener(handler);
                        ConcurrentHashMap<Annotation, Method> a_m_map = id_annotation_method_map.get(id);
                        for (Annotation a : a_m_map.keySet()) {
                            if (a.annotationType().equals(OnItemSelected.class)) {
                                listener.selected(a_m_map.get(a));
                            } else if (a.annotationType().equals(OnNothingSelected.class)) {
                                listener.noSelected(a_m_map.get(a));
                            }
                        }
                        ((AdapterView) view).setOnItemSelectedListener(listener);
                    } else if (annotation.annotationType().equals(OnProgressChanged.class)) {
                        View view = finder.findViewById((Integer) id);
                        ViewCommonEventListener listener = new ViewCommonEventListener(handler);
                        ConcurrentHashMap<Annotation, Method> a_m_map = id_annotation_method_map.get(id);
                        for (Annotation a : a_m_map.keySet()) {
                            if (a.annotationType().equals(OnProgressChanged.class)) {
                                listener.preferenceChange(a_m_map.get(a));
                            } else if (a.annotationType().equals(OnStartTrackingTouch.class)) {
                                listener.startTrackingTouch(a_m_map.get(a));
                            } else if (a.annotationType().equals(OnStopTrackingTouch.class)) {
                                listener.stopTrackingTouch(a_m_map.get(a));
                            }
                        }
                        ((SeekBar) view).setOnSeekBarChangeListener(listener);
                    }
                } catch (Exception e) {
                    LogUtils.e(e.getMessage(), e);
                }
            }
        }
    }

    private static class Finder {
        private View view;
        private Activity activity;
        private PreferenceActivity preferenceActivity;

        public Finder(View view) {
            this.view = view;
        }

        public Finder(Activity activity) {
            this.activity = activity;
        }

        private Finder(PreferenceActivity preferenceActivity) {
            this.preferenceActivity = preferenceActivity;
            this.activity = preferenceActivity;
        }

        public View findViewById(int id) {
            return activity == null ? view.findViewById(id) : activity.findViewById(id);
        }

        public Preference findPreference(CharSequence key) {
            return preferenceActivity.findPreference(key);
        }
    }

}
