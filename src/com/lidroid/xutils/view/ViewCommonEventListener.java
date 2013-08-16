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

    private Method clickMethod;
    private Method longClickMethod;
    private Method itemClickMethod;
    private Method itemLongClickMethod;
    private Method radioGroupCheckedChangedMethod;
    private Method compoundButtonCheckedChangedMethod;
    private Method preferenceChangeMethod;
    private Method tabChangedMethod;
    private Method scrollChangedMethod;

    // ItemSelected
    private Method itemSelectMethod;
    private Method nothingSelectedMethod;

    // SeekBarChange
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
        } catch (Exception e) {
            LogUtils.e(e.getMessage(), e);
        }
    }

    @Override
    public boolean onLongClick(View v) {
        try {
            Object result = longClickMethod.invoke(handler, v);
            return result == null ? false : Boolean.valueOf(result.toString());
        } catch (Exception e) {
            LogUtils.e(e.getMessage(), e);
        }
        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        try {
            itemClickMethod.invoke(handler, parent, view, position, id);
        } catch (Exception e) {
            LogUtils.e(e.getMessage(), e);
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        try {
            Object result = itemLongClickMethod.invoke(handler, parent, view, position, id);
            return result == null ? false : Boolean.valueOf(result.toString());
        } catch (Exception e) {
            LogUtils.e(e.getMessage(), e);
        }
        return false;
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        try {
            radioGroupCheckedChangedMethod.invoke(handler, group, checkedId);
        } catch (Exception e) {
            LogUtils.e(e.getMessage(), e);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        try {
            compoundButtonCheckedChangedMethod.invoke(handler, buttonView, isChecked);
        } catch (Exception e) {
            LogUtils.e(e.getMessage(), e);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        try {
            Object result = preferenceChangeMethod.invoke(handler, preference, newValue);
            return result == null ? false : Boolean.valueOf(result.toString());
        } catch (Exception e) {
            LogUtils.e(e.getMessage(), e);
        }
        return false;
    }

    @Override
    public void onTabChanged(String tabId) {
        try {
            tabChangedMethod.invoke(handler, tabId);
        } catch (Exception e) {
            LogUtils.e(e.getMessage(), e);
        }
    }

    @Override
    public void onScrollChanged() {
        try {
            scrollChangedMethod.invoke(handler);
        } catch (Exception e) {
            LogUtils.e(e.getMessage(), e);
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        try {
            itemSelectMethod.invoke(handler, parent, view, position, id);
        } catch (Exception e) {
            LogUtils.e(e.getMessage(), e);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        try {
            nothingSelectedMethod.invoke(handler, parent);
        } catch (Exception e) {
            LogUtils.e(e.getMessage(), e);
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        try {
            progressChangedMethod.invoke(handler, seekBar, progress, fromUser);
        } catch (Exception e) {
            LogUtils.e(e.getMessage(), e);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        try {
            startTrackingTouchMethod.invoke(handler, seekBar);
        } catch (Exception e) {
            LogUtils.e(e.getMessage(), e);
        }
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        try {
            stopTrackingTouchMethod.invoke(handler, seekBar);
        } catch (Exception e) {
            LogUtils.e(e.getMessage(), e);
        }
    }
}
