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
        ExpandableListView.OnChildClickListener,
        ExpandableListView.OnGroupClickListener,
        ExpandableListView.OnGroupCollapseListener,
        ExpandableListView.OnGroupExpandListener,
        RadioGroup.OnCheckedChangeListener,
        CompoundButton.OnCheckedChangeListener,
        Preference.OnPreferenceClickListener,
        Preference.OnPreferenceChangeListener,
        TabHost.OnTabChangeListener,
        ViewTreeObserver.OnScrollChangedListener,
        AbsListView.OnScrollListener,
        OnItemSelectedListener,
        SeekBar.OnSeekBarChangeListener {

    private final Object handler;
    private final Method[] methods;

    public ViewCommonEventListener(Object handler, Method... methods) {
        this.handler = handler;
        this.methods = methods;
    }

    @Override
    public void onClick(View v) {
        try {
            methods[0].invoke(handler, v);
        } catch (Throwable e) {
            LogUtils.e(e.getMessage(), e);
        }
    }

    @Override
    public boolean onLongClick(View v) {
        try {
            return (Boolean) methods[0].invoke(handler, v);
        } catch (Throwable e) {
            LogUtils.e(e.getMessage(), e);
        }
        return false;
    }

    @Override
    public void onFocusChange(View view, boolean b) {
        try {
            methods[0].invoke(handler, view, b);
        } catch (Throwable e) {
            LogUtils.e(e.getMessage(), e);
        }
    }

    @Override
    public boolean onKey(View view, int i, KeyEvent keyEvent) {
        try {
            return (Boolean) methods[0].invoke(handler, view, i, keyEvent);
        } catch (Throwable e) {
            LogUtils.e(e.getMessage(), e);
        }
        return false;
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        try {
            return (Boolean) methods[0].invoke(handler, view, motionEvent);
        } catch (Throwable e) {
            LogUtils.e(e.getMessage(), e);
        }
        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        try {
            methods[0].invoke(handler, parent, view, position, id);
        } catch (Throwable e) {
            LogUtils.e(e.getMessage(), e);
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        try {
            return (Boolean) methods[0].invoke(handler, parent, view, position, id);
        } catch (Throwable e) {
            LogUtils.e(e.getMessage(), e);
        }
        return false;
    }

    @Override
    public boolean onChildClick(ExpandableListView expandableListView, View view, int i, int i2, long l) {
        try {
            return (Boolean) methods[0].invoke(handler, expandableListView, view, i, i2, l);
        } catch (Throwable e) {
            LogUtils.e(e.getMessage(), e);
        }
        return false;
    }

    @Override
    public boolean onGroupClick(ExpandableListView expandableListView, View view, int i, long l) {
        try {
            return (Boolean) methods[0].invoke(handler, expandableListView, view, i, l);
        } catch (Throwable e) {
            LogUtils.e(e.getMessage(), e);
        }
        return false;
    }

    @Override
    public void onGroupCollapse(int i) {
        try {
            methods[0].invoke(handler, i);
        } catch (Throwable e) {
            LogUtils.e(e.getMessage(), e);
        }
    }

    @Override
    public void onGroupExpand(int i) {
        try {
            methods[0].invoke(handler, i);
        } catch (Throwable e) {
            LogUtils.e(e.getMessage(), e);
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        try {
            methods[0].invoke(handler, group, checkedId);
        } catch (Throwable e) {
            LogUtils.e(e.getMessage(), e);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        try {
            methods[0].invoke(handler, buttonView, isChecked);
        } catch (Throwable e) {
            LogUtils.e(e.getMessage(), e);
        }
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        try {
            return (Boolean) methods[0].invoke(handler, preference);
        } catch (Throwable e) {
            LogUtils.e(e.getMessage(), e);
        }
        return false;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        try {
            return (Boolean) methods[0].invoke(handler, preference, newValue);
        } catch (Throwable e) {
            LogUtils.e(e.getMessage(), e);
        }
        return false;
    }

    @Override
    public void onTabChanged(String tabId) {
        try {
            methods[0].invoke(handler, tabId);
        } catch (Throwable e) {
            LogUtils.e(e.getMessage(), e);
        }
    }

    @Override
    public void onScrollChanged() {
        try {
            methods[0].invoke(handler);
        } catch (Throwable e) {
            LogUtils.e(e.getMessage(), e);
        }
    }

    // #region AbsListView.OnScrollListener
    @Override
    public void onScrollStateChanged(AbsListView absListView, int i) {
        try {
            methods[0].invoke(handler, absListView, i);
        } catch (Throwable e) {
            LogUtils.e(e.getMessage(), e);
        }
    }

    @Override
    public void onScroll(AbsListView absListView, int i, int i2, int i3) {
        try {
            methods[1].invoke(handler, absListView, i, i2, i3);
        } catch (Throwable e) {
            LogUtils.e(e.getMessage(), e);
        }
    }
    // #endregion AbsListView.OnScrollListener

    // #region OnItemSelectedListener
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        try {
            methods[0].invoke(handler, parent, view, position, id);
        } catch (Throwable e) {
            LogUtils.e(e.getMessage(), e);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        try {
            methods[1].invoke(handler, parent);
        } catch (Throwable e) {
            LogUtils.e(e.getMessage(), e);
        }
    }
    // #endregion OnItemSelectedListener


    // #region SeekBar.OnSeekBarChangeListener
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        try {
            methods[0].invoke(handler, seekBar, progress, fromUser);
        } catch (Throwable e) {
            LogUtils.e(e.getMessage(), e);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        try {
            methods[1].invoke(handler, seekBar);
        } catch (Throwable e) {
            LogUtils.e(e.getMessage(), e);
        }
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        try {
            methods[2].invoke(handler, seekBar);
        } catch (Throwable e) {
            LogUtils.e(e.getMessage(), e);
        }
    }
    // #endregion SeekBar.OnSeekBarChangeListener

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
                        view.setOnClickListener(new ViewCommonEventListener(handler, method));
                    } else if (annotation.annotationType().equals(OnLongClick.class)) {
                        View view = finder.findViewById((Integer) value);
                        if (view == null) break;
                        view.setOnLongClickListener(new ViewCommonEventListener(handler, method));
                    } else if (annotation.annotationType().equals(OnFocusChange.class)) {
                        View view = finder.findViewById((Integer) value);
                        if (view == null) break;
                        view.setOnFocusChangeListener(new ViewCommonEventListener(handler, method));
                    } else if (annotation.annotationType().equals(OnKey.class)) {
                        View view = finder.findViewById((Integer) value);
                        if (view == null) break;
                        view.setOnKeyListener(new ViewCommonEventListener(handler, method));
                    } else if (annotation.annotationType().equals(OnTouch.class)) {
                        View view = finder.findViewById((Integer) value);
                        if (view == null) break;
                        view.setOnTouchListener(new ViewCommonEventListener(handler, method));
                    } else if (annotation.annotationType().equals(OnItemClick.class)) {
                        View view = finder.findViewById((Integer) value);
                        if (view == null) break;
                        ((AdapterView<?>) view).setOnItemClickListener(new ViewCommonEventListener(handler, method));
                    } else if (annotation.annotationType().equals(OnItemLongClick.class)) {
                        View view = finder.findViewById((Integer) value);
                        if (view == null) break;
                        ((AdapterView<?>) view).setOnItemLongClickListener(new ViewCommonEventListener(handler, method));
                    } else if (annotation.annotationType().equals(OnChildClick.class)) {
                        View view = finder.findViewById((Integer) value);
                        if (view == null) break;
                        ((ExpandableListView) view).setOnChildClickListener(new ViewCommonEventListener(handler, method));
                    } else if (annotation.annotationType().equals(OnGroupClick.class)) {
                        View view = finder.findViewById((Integer) value);
                        if (view == null) break;
                        ((ExpandableListView) view).setOnGroupClickListener(new ViewCommonEventListener(handler, method));
                    } else if (annotation.annotationType().equals(OnGroupCollapse.class)) {
                        View view = finder.findViewById((Integer) value);
                        if (view == null) break;
                        ((ExpandableListView) view).setOnGroupCollapseListener(new ViewCommonEventListener(handler, method));
                    } else if (annotation.annotationType().equals(OnGroupExpand.class)) {
                        View view = finder.findViewById((Integer) value);
                        if (view == null) break;
                        ((ExpandableListView) view).setOnGroupExpandListener(new ViewCommonEventListener(handler, method));
                    } else if (annotation.annotationType().equals(OnCheckedChange.class)) {
                        View view = finder.findViewById((Integer) value);
                        if (view == null) break;
                        if (view instanceof RadioGroup) {
                            ((RadioGroup) view).setOnCheckedChangeListener(new ViewCommonEventListener(handler, method));
                        } else if (view instanceof CompoundButton) {
                            ((CompoundButton) view).setOnCheckedChangeListener(new ViewCommonEventListener(handler, method));
                        }
                    } else if (annotation.annotationType().equals(OnPreferenceClick.class)) {
                        Preference preference = finder.findPreference(value.toString());
                        if (preference == null) break;
                        preference.setOnPreferenceClickListener(new ViewCommonEventListener(handler, method));
                    } else if (annotation.annotationType().equals(OnPreferenceChange.class)) {
                        Preference preference = finder.findPreference(value.toString());
                        if (preference == null) break;
                        preference.setOnPreferenceChangeListener(new ViewCommonEventListener(handler, method));
                    } else if (annotation.annotationType().equals(OnTabChange.class)) {
                        View view = finder.findViewById((Integer) value);
                        if (view == null) break;
                        ((TabHost) view).setOnTabChangedListener(new ViewCommonEventListener(handler, method));
                    } else if (annotation.annotationType().equals(OnScrollChanged.class)) {
                        View view = finder.findViewById((Integer) value);
                        if (view == null) break;
                        view.getViewTreeObserver().addOnScrollChangedListener(new ViewCommonEventListener(handler, method));
                    } else if (annotation.annotationType().equals(OnScrollStateChanged.class)) {
                        View view = finder.findViewById((Integer) value);
                        if (view == null) break;
                        Method method0 = null, method1 = null;
                        ConcurrentHashMap<Annotation, Method> a_m_map = value_annotation_method_map.get(value);
                        for (Annotation a : a_m_map.keySet()) {
                            if (a.annotationType().equals(OnScrollStateChanged.class)) {
                                method0 = a_m_map.get(a);
                            } else if (a.annotationType().equals(OnScroll.class)) {
                                method1 = a_m_map.get(a);
                            }
                        }
                        ((AbsListView) view).setOnScrollListener(new ViewCommonEventListener(handler, method0, method1));
                    } else if (annotation.annotationType().equals(OnItemSelected.class)) {
                        View view = finder.findViewById((Integer) value);
                        if (view == null) break;
                        Method method0 = null, method1 = null;
                        ConcurrentHashMap<Annotation, Method> a_m_map = value_annotation_method_map.get(value);
                        for (Annotation a : a_m_map.keySet()) {
                            if (a.annotationType().equals(OnItemSelected.class)) {
                                method0 = a_m_map.get(a);
                            } else if (a.annotationType().equals(OnNothingSelected.class)) {
                                method1 = a_m_map.get(a);
                            }
                        }
                        ((AdapterView<?>) view).setOnItemSelectedListener(new ViewCommonEventListener(handler, method0, method1));
                    } else if (annotation.annotationType().equals(OnProgressChanged.class)) {
                        View view = finder.findViewById((Integer) value);
                        if (view == null) break;
                        Method method0 = null, method1 = null, method2 = null;
                        ConcurrentHashMap<Annotation, Method> a_m_map = value_annotation_method_map.get(value);
                        for (Annotation a : a_m_map.keySet()) {
                            if (a.annotationType().equals(OnProgressChanged.class)) {
                                method0 = a_m_map.get(a);
                            } else if (a.annotationType().equals(OnStartTrackingTouch.class)) {
                                method1 = a_m_map.get(a);
                            } else if (a.annotationType().equals(OnStopTrackingTouch.class)) {
                                method2 = a_m_map.get(a);
                            }
                        }
                        ((SeekBar) view).setOnSeekBarChangeListener(new ViewCommonEventListener(handler, method0, method1, method2));
                    }
                } catch (Throwable e) {
                    LogUtils.e(e.getMessage(), e);
                }
            }
        }
    }
}
