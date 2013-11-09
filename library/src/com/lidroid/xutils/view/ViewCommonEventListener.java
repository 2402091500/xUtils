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

package com.lidroid.xutils.view;

import android.preference.Preference;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.*;
import android.view.ViewTreeObserver;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import com.lidroid.xutils.util.LogUtils;
import com.lidroid.xutils.util.core.DoubleKeyValueMap;
import com.lidroid.xutils.view.annotation.event.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

public class ViewCommonEventListener implements
        OnClickListener,
        OnLongClickListener,
        OnFocusChangeListener,
        OnKeyListener,
        OnTouchListener,
        OnItemClickListener,
        OnItemLongClickListener,
        RadioGroup.OnCheckedChangeListener,
        CompoundButton.OnCheckedChangeListener,
        Preference.OnPreferenceClickListener,
        Preference.OnPreferenceChangeListener,
        TabHost.OnTabChangeListener,
        ViewTreeObserver.OnScrollChangedListener,
        AbsListView.OnScrollListener,
        OnItemSelectedListener,
        SeekBar.OnSeekBarChangeListener {

    private Object handler;

    private Method clickMethod;
    private Method longClickMethod;
    private Method focusChangeMethod;
    private Method keyMethod;
    private Method touchMethod;
    private Method itemClickMethod;
    private Method itemLongClickMethod;
    private Method radioGroupCheckedChangedMethod;
    private Method compoundButtonCheckedChangedMethod;
    private Method preferenceClickMethod;
    private Method preferenceChangeMethod;
    private Method tabChangedMethod;
    private Method scrollChangedMethod;

    // OnScrollListener
    private Method scrollStateChangedMethod;
    private Method scrollMethod;

    // OnItemSelectedListener
    private Method itemSelectMethod;
    private Method nothingSelectedMethod;

    // OnSeekBarChangeListener
    private Method progressChangedMethod;
    private Method startTrackingTouchMethod;
    private Method stopTrackingTouchMethod;

    public ViewCommonEventListener(Object handler) {
        this.handler = handler;
    }


    public ViewCommonEventListener click(Method method) {
        this.clickMethod = method;
        return this;
    }

    public ViewCommonEventListener longClick(Method method) {
        this.longClickMethod = method;
        return this;
    }

    public ViewCommonEventListener focusChange(Method method) {
        this.focusChangeMethod = method;
        return this;
    }

    public ViewCommonEventListener key(Method method) {
        this.keyMethod = method;
        return this;
    }

    public ViewCommonEventListener touch(Method method) {
        this.touchMethod = method;
        return this;
    }

    public ViewCommonEventListener itemClick(Method method) {
        this.itemClickMethod = method;
        return this;
    }

    public ViewCommonEventListener itemLongClick(Method method) {
        this.itemLongClickMethod = method;
        return this;
    }

    public ViewCommonEventListener radioGroupCheckedChanged(Method method) {
        this.radioGroupCheckedChangedMethod = method;
        return this;
    }

    public ViewCommonEventListener compoundButtonCheckedChanged(Method method) {
        this.compoundButtonCheckedChangedMethod = method;
        return this;
    }

    public ViewCommonEventListener preferenceClick(Method method) {
        this.preferenceClickMethod = method;
        return this;
    }

    public ViewCommonEventListener preferenceChange(Method method) {
        this.preferenceChangeMethod = method;
        return this;
    }

    public ViewCommonEventListener tabChanged(Method method) {
        this.tabChangedMethod = method;
        return this;
    }

    public ViewCommonEventListener scrollChanged(Method method) {
        this.scrollChangedMethod = method;
        return this;
    }

    public ViewCommonEventListener scrollStateChanged(Method method) {
        this.scrollStateChangedMethod = method;
        return this;
    }

    public ViewCommonEventListener scroll(Method method) {
        this.scrollMethod = method;
        return this;
    }

    public ViewCommonEventListener selected(Method method) {
        this.itemSelectMethod = method;
        return this;
    }

    public ViewCommonEventListener noSelected(Method method) {
        this.nothingSelectedMethod = method;
        return this;
    }

    public ViewCommonEventListener progressChanged(Method method) {
        this.progressChangedMethod = method;
        return this;
    }

    public ViewCommonEventListener startTrackingTouch(Method method) {
        this.startTrackingTouchMethod = method;
        return this;
    }

    public ViewCommonEventListener stopTrackingTouch(Method method) {
        this.stopTrackingTouchMethod = method;
        return this;
    }


    @Override
    public void onClick(View v) {
        try {
            clickMethod.invoke(handler, v);
        } catch (Throwable e) {
            LogUtils.e(e.getMessage(), e);
        }
    }

    @Override
    public boolean onLongClick(View v) {
        try {
            return (Boolean) longClickMethod.invoke(handler, v);
        } catch (Throwable e) {
            LogUtils.e(e.getMessage(), e);
        }
        return false;
    }

    @Override
    public void onFocusChange(View view, boolean b) {
        try {
            focusChangeMethod.invoke(handler, view, b);
        } catch (Throwable e) {
            LogUtils.e(e.getMessage(), e);
        }
    }

    @Override
    public boolean onKey(View view, int i, KeyEvent keyEvent) {
        try {
            return (Boolean) keyMethod.invoke(handler, view, i, keyEvent);
        } catch (Throwable e) {
            LogUtils.e(e.getMessage(), e);
        }
        return false;
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        try {
            return (Boolean) touchMethod.invoke(handler, view, motionEvent);
        } catch (Throwable e) {
            LogUtils.e(e.getMessage(), e);
        }
        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        try {
            itemClickMethod.invoke(handler, parent, view, position, id);
        } catch (Throwable e) {
            LogUtils.e(e.getMessage(), e);
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        try {
            return (Boolean) itemLongClickMethod.invoke(handler, parent, view, position, id);
        } catch (Throwable e) {
            LogUtils.e(e.getMessage(), e);
        }
        return false;
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        try {
            radioGroupCheckedChangedMethod.invoke(handler, group, checkedId);
        } catch (Throwable e) {
            LogUtils.e(e.getMessage(), e);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        try {
            compoundButtonCheckedChangedMethod.invoke(handler, buttonView, isChecked);
        } catch (Throwable e) {
            LogUtils.e(e.getMessage(), e);
        }
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        try {
            return (Boolean) preferenceClickMethod.invoke(handler, preference);
        } catch (Throwable e) {
            LogUtils.e(e.getMessage(), e);
        }
        return false;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        try {
            return (Boolean) preferenceChangeMethod.invoke(handler, preference, newValue);
        } catch (Throwable e) {
            LogUtils.e(e.getMessage(), e);
        }
        return false;
    }

    @Override
    public void onTabChanged(String tabId) {
        try {
            tabChangedMethod.invoke(handler, tabId);
        } catch (Throwable e) {
            LogUtils.e(e.getMessage(), e);
        }
    }

    @Override
    public void onScrollChanged() {
        try {
            scrollChangedMethod.invoke(handler);
        } catch (Throwable e) {
            LogUtils.e(e.getMessage(), e);
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView absListView, int i) {
        try {
            scrollStateChangedMethod.invoke(handler, absListView, i);
        } catch (Throwable e) {
            LogUtils.e(e.getMessage(), e);
        }
    }

    @Override
    public void onScroll(AbsListView absListView, int i, int i2, int i3) {
        try {
            scrollMethod.invoke(handler, absListView, i, i2, i3);
        } catch (Throwable e) {
            LogUtils.e(e.getMessage(), e);
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        try {
            itemSelectMethod.invoke(handler, parent, view, position, id);
        } catch (Throwable e) {
            LogUtils.e(e.getMessage(), e);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        try {
            nothingSelectedMethod.invoke(handler, parent);
        } catch (Throwable e) {
            LogUtils.e(e.getMessage(), e);
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        try {
            progressChangedMethod.invoke(handler, seekBar, progress, fromUser);
        } catch (Throwable e) {
            LogUtils.e(e.getMessage(), e);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        try {
            startTrackingTouchMethod.invoke(handler, seekBar);
        } catch (Throwable e) {
            LogUtils.e(e.getMessage(), e);
        }
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        try {
            stopTrackingTouchMethod.invoke(handler, seekBar);
        } catch (Throwable e) {
            LogUtils.e(e.getMessage(), e);
        }
    }

    @SuppressWarnings("ConstantConditions")
    public static void setEventListener(Object handler, ViewFinder finder, DoubleKeyValueMap<Object, Annotation, Method> value_annotation_method_map) {
        for (Object value : value_annotation_method_map.getFirstKeys()) {
            ConcurrentHashMap<Annotation, Method> annotation_method_map = value_annotation_method_map.get(value);
            for (Annotation annotation : annotation_method_map.keySet()) {
                try {
                    Method method = annotation_method_map.get(annotation);
                    if (annotation.annotationType().equals(OnClick.class)) {
                        View view = finder.findViewById((Integer) value);
                        if (view == null) break;
                        view.setOnClickListener(new ViewCommonEventListener(handler).click(method));
                    } else if (annotation.annotationType().equals(OnLongClick.class)) {
                        View view = finder.findViewById((Integer) value);
                        if (view == null) break;
                        view.setOnLongClickListener(new ViewCommonEventListener(handler).longClick(method));
                    } else if (annotation.annotationType().equals(OnFocusChange.class)) {
                        View view = finder.findViewById((Integer) value);
                        if (view == null) break;
                        view.setOnFocusChangeListener(new ViewCommonEventListener(handler).focusChange(method));
                    } else if (annotation.annotationType().equals(OnKey.class)) {
                        View view = finder.findViewById((Integer) value);
                        if (view == null) break;
                        view.setOnKeyListener(new ViewCommonEventListener(handler).key(method));
                    } else if (annotation.annotationType().equals(OnTouch.class)) {
                        View view = finder.findViewById((Integer) value);
                        if (view == null) break;
                        view.setOnTouchListener(new ViewCommonEventListener(handler).touch(method));
                    } else if (annotation.annotationType().equals(OnItemClick.class)) {
                        View view = finder.findViewById((Integer) value);
                        if (view == null) break;
                        ((AdapterView<?>) view).setOnItemClickListener(new ViewCommonEventListener(handler).itemClick(method));
                    } else if (annotation.annotationType().equals(OnItemLongClick.class)) {
                        View view = finder.findViewById((Integer) value);
                        if (view == null) break;
                        ((AdapterView<?>) view).setOnItemLongClickListener(new ViewCommonEventListener(handler).itemLongClick(method));
                    } else if (annotation.annotationType().equals(OnCheckedChange.class)) {
                        View view = finder.findViewById((Integer) value);
                        if (view == null) break;
                        if (view instanceof RadioGroup) {
                            ((RadioGroup) view).setOnCheckedChangeListener(new ViewCommonEventListener(handler).radioGroupCheckedChanged(method));
                        } else if (view instanceof CompoundButton) {
                            ((CompoundButton) view).setOnCheckedChangeListener(new ViewCommonEventListener(handler).compoundButtonCheckedChanged(method));
                        }
                    } else if (annotation.annotationType().equals(OnPreferenceClick.class)) {
                        Preference preference = finder.findPreference(value.toString());
                        if (preference == null) break;
                        preference.setOnPreferenceClickListener(new ViewCommonEventListener(handler).preferenceClick(method));
                    } else if (annotation.annotationType().equals(OnPreferenceChange.class)) {
                        Preference preference = finder.findPreference(value.toString());
                        if (preference == null) break;
                        preference.setOnPreferenceChangeListener(new ViewCommonEventListener(handler).preferenceChange(method));
                    } else if (annotation.annotationType().equals(OnTabChange.class)) {
                        View view = finder.findViewById((Integer) value);
                        if (view == null) break;
                        ((TabHost) view).setOnTabChangedListener(new ViewCommonEventListener(handler).tabChanged(method));
                    } else if (annotation.annotationType().equals(OnScrollChanged.class)) {
                        View view = finder.findViewById((Integer) value);
                        if (view == null) break;
                        view.getViewTreeObserver().addOnScrollChangedListener(new ViewCommonEventListener(handler).scrollChanged(method));
                    } else if (annotation.annotationType().equals(OnScrollStateChanged.class)) {
                        View view = finder.findViewById((Integer) value);
                        if (view == null) break;
                        ViewCommonEventListener listener = new ViewCommonEventListener(handler);
                        ConcurrentHashMap<Annotation, Method> a_m_map = value_annotation_method_map.get(value);
                        for (Annotation a : a_m_map.keySet()) {
                            if (a.annotationType().equals(OnScrollStateChanged.class)) {
                                listener.scrollStateChanged(a_m_map.get(a));
                            } else if (a.annotationType().equals(OnScroll.class)) {
                                listener.scroll(a_m_map.get(a));
                            }
                        }
                        ((AbsListView) view).setOnScrollListener(listener);
                    } else if (annotation.annotationType().equals(OnItemSelected.class)) {
                        View view = finder.findViewById((Integer) value);
                        if (view == null) break;
                        ViewCommonEventListener listener = new ViewCommonEventListener(handler);
                        ConcurrentHashMap<Annotation, Method> a_m_map = value_annotation_method_map.get(value);
                        for (Annotation a : a_m_map.keySet()) {
                            if (a.annotationType().equals(OnItemSelected.class)) {
                                listener.selected(a_m_map.get(a));
                            } else if (a.annotationType().equals(OnNothingSelected.class)) {
                                listener.noSelected(a_m_map.get(a));
                            }
                        }
                        ((AdapterView<?>) view).setOnItemSelectedListener(listener);
                    } else if (annotation.annotationType().equals(OnProgressChanged.class)) {
                        View view = finder.findViewById((Integer) value);
                        if (view == null) break;
                        ViewCommonEventListener listener = new ViewCommonEventListener(handler);
                        ConcurrentHashMap<Annotation, Method> a_m_map = value_annotation_method_map.get(value);
                        for (Annotation a : a_m_map.keySet()) {
                            if (a.annotationType().equals(OnProgressChanged.class)) {
                                listener.progressChanged(a_m_map.get(a));
                            } else if (a.annotationType().equals(OnStartTrackingTouch.class)) {
                                listener.startTrackingTouch(a_m_map.get(a));
                            } else if (a.annotationType().equals(OnStopTrackingTouch.class)) {
                                listener.stopTrackingTouch(a_m_map.get(a));
                            }
                        }
                        ((SeekBar) view).setOnSeekBarChangeListener(listener);
                    }
                } catch (Throwable e) {
                    LogUtils.e(e.getMessage(), e);
                }
            }
        }
    }
}
