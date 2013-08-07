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

package com.lidroid.xutils.view.event;

import android.preference.Preference;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewTreeObserver;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import com.lidroid.xutils.util.LogUtils;

import java.lang.reflect.Method;

public class ViewCommonEventListener implements
        OnClickListener,
        OnLongClickListener,
        OnItemClickListener,
        OnItemLongClickListener,
        RadioGroup.OnCheckedChangeListener,
        CompoundButton.OnCheckedChangeListener,
        Preference.OnPreferenceChangeListener,
        TabHost.OnTabChangeListener,
        ViewTreeObserver.OnScrollChangedListener,
        OnItemSelectedListener,
        SeekBar.OnSeekBarChangeListener {

    private Object handler;

    private String clickMethod;
    private String longClickMethod;
    private String itemClickMethod;
    private String itemLongClickMethod;
    private String radioGroupCheckedChangedMethod;
    private String compoundButtonCheckedChangedMethod;
    private String preferenceChangeMethod;
    private String sharedPreferenceChangedMethod;
    private String tabChangedMethod;
    private String scrollChangedMethod;

    // ItemSelected
    private String itemSelectMethod;
    private String nothingSelectedMethod;

    // SeekBarChange
    private String progressChangedMethod;
    private String startTrackingTouchMethod;
    private String stopTrackingTouchMethod;

    public ViewCommonEventListener(Object handler) {
        this.handler = handler;
    }


    public ViewCommonEventListener click(String methodName) {
        this.clickMethod = methodName;
        return this;
    }

    public ViewCommonEventListener longClick(String methodName) {
        this.longClickMethod = methodName;
        return this;
    }

    public ViewCommonEventListener itemClick(String methodName) {
        this.itemClickMethod = methodName;
        return this;
    }

    public ViewCommonEventListener itemLongClick(String methodName) {
        this.itemLongClickMethod = methodName;
        return this;
    }

    public ViewCommonEventListener radioGroupCheckedChanged(String methodName) {
        this.radioGroupCheckedChangedMethod = methodName;
        return this;
    }

    public ViewCommonEventListener compoundButtonCheckedChanged(String methodName) {
        this.compoundButtonCheckedChangedMethod = methodName;
        return this;
    }

    public ViewCommonEventListener preferenceChange(String methodName) {
        this.preferenceChangeMethod = methodName;
        return this;
    }

    public ViewCommonEventListener sharedPreferenceChanged(String methodName) {
        this.sharedPreferenceChangedMethod = methodName;
        return this;
    }

    public ViewCommonEventListener tabChanged(String methodName) {
        this.tabChangedMethod = methodName;
        return this;
    }

    public ViewCommonEventListener scrollChanged(String methodName) {
        this.scrollChangedMethod = methodName;
        return this;
    }

    public ViewCommonEventListener selected(String methodName) {
        this.itemSelectMethod = methodName;
        return this;
    }

    public ViewCommonEventListener noSelected(String methodName) {
        this.nothingSelectedMethod = methodName;
        return this;
    }

    public ViewCommonEventListener progressChanged(String methodName) {
        this.progressChangedMethod = methodName;
        return this;
    }

    public ViewCommonEventListener startTrackingTouch(String methodName) {
        this.startTrackingTouchMethod = methodName;
        return this;
    }

    public ViewCommonEventListener stopTrackingTouch(String methodName) {
        this.stopTrackingTouchMethod = methodName;
        return this;
    }


    @Override
    public void onClick(View v) {
        try {
            Method method = handler.getClass().getDeclaredMethod(clickMethod, View.class);
            method.invoke(handler, v);
        } catch (Exception e) {
            LogUtils.e(e.getMessage(), e);
        }
    }

    @Override
    public boolean onLongClick(View v) {
        try {
            Method method = handler.getClass().getDeclaredMethod(longClickMethod, View.class);
            Object result = method.invoke(handler, v);
            return result == null ? false : Boolean.valueOf(result.toString());
        } catch (Exception e) {
            LogUtils.e(e.getMessage(), e);
        }
        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        try {
            Method method = handler.getClass().getDeclaredMethod(itemClickMethod, AdapterView.class, View.class, int.class, long.class);
            method.invoke(handler, parent, view, position, id);
        } catch (Exception e) {
            LogUtils.e(e.getMessage(), e);
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        try {
            Method method = handler.getClass().getDeclaredMethod(itemLongClickMethod, AdapterView.class, View.class, int.class, long.class);
            Object result = method.invoke(handler, parent, view, position, id);
            return result == null ? false : Boolean.valueOf(result.toString());
        } catch (Exception e) {
            LogUtils.e(e.getMessage(), e);
        }
        return false;
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        try {
            Method method = handler.getClass().getDeclaredMethod(radioGroupCheckedChangedMethod, RadioGroup.class, int.class);
            method.invoke(handler, group, checkedId);
        } catch (Exception e) {
            LogUtils.e(e.getMessage(), e);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        try {
            Method method = handler.getClass().getDeclaredMethod(compoundButtonCheckedChangedMethod, CompoundButton.class, boolean.class);
            method.invoke(handler, buttonView, isChecked);
        } catch (Exception e) {
            LogUtils.e(e.getMessage(), e);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        try {
            Method method = handler.getClass().getDeclaredMethod(preferenceChangeMethod, Preference.class, Object.class);
            Object result = method.invoke(handler, preference, newValue);
            return result == null ? false : Boolean.valueOf(result.toString());
        } catch (Exception e) {
            LogUtils.e(e.getMessage(), e);
        }
        return false;
    }

    @Override
    public void onTabChanged(String tabId) {
        try {
            Method method = handler.getClass().getDeclaredMethod(tabChangedMethod, String.class);
            method.invoke(handler, tabId);
        } catch (Exception e) {
            LogUtils.e(e.getMessage(), e);
        }
    }

    @Override
    public void onScrollChanged() {
        try {
            Method method = handler.getClass().getDeclaredMethod(scrollChangedMethod);
            method.invoke(handler);
        } catch (Exception e) {
            LogUtils.e(e.getMessage(), e);
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        try {
            Method method = handler.getClass().getDeclaredMethod(itemSelectMethod, AdapterView.class, View.class, int.class, long.class);
            method.invoke(handler, parent, view, position, id);
        } catch (Exception e) {
            LogUtils.e(e.getMessage(), e);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        try {
            Method method = handler.getClass().getDeclaredMethod(nothingSelectedMethod, AdapterView.class);
            method.invoke(handler, parent);
        } catch (Exception e) {
            LogUtils.e(e.getMessage(), e);
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        try {
            Method method = handler.getClass().getDeclaredMethod(progressChangedMethod, SeekBar.class, int.class, boolean.class);
            method.invoke(handler, seekBar, progress, fromUser);
        } catch (Exception e) {
            LogUtils.e(e.getMessage(), e);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        try {
            Method method = handler.getClass().getDeclaredMethod(startTrackingTouchMethod, SeekBar.class);
            method.invoke(handler, seekBar);
        } catch (Exception e) {
            LogUtils.e(e.getMessage(), e);
        }
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        try {
            Method method = handler.getClass().getDeclaredMethod(stopTrackingTouchMethod, SeekBar.class);
            method.invoke(handler, seekBar);
        } catch (Exception e) {
            LogUtils.e(e.getMessage(), e);
        }
    }
}
