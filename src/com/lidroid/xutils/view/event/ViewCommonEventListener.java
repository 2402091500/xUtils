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

import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import com.lidroid.xutils.exception.ViewException;
import com.lidroid.xutils.util.LogUtils;

import java.lang.reflect.Method;

public class ViewCommonEventListener implements OnClickListener, OnLongClickListener, OnItemClickListener, OnItemSelectedListener, OnItemLongClickListener {

    private Object handler;

    private String clickMethod;
    private String longClickMethod;
    private String itemClickMethod;
    private String itemLongClickMethod;
    private String itemSelectMethod;
    private String nothingSelectedMethod;

    public ViewCommonEventListener(Object handler) {
        this.handler = handler;
    }


    public ViewCommonEventListener click(String method) {
        this.clickMethod = method;
        return this;
    }

    public ViewCommonEventListener longClick(String method) {
        this.longClickMethod = method;
        return this;
    }

    public ViewCommonEventListener itemClick(String method) {
        this.itemClickMethod = method;
        return this;
    }

    public ViewCommonEventListener itemLongClick(String method) {
        this.itemLongClickMethod = method;
        return this;
    }

    public ViewCommonEventListener select(String method) {
        this.itemSelectMethod = method;
        return this;
    }

    public ViewCommonEventListener noSelect(String method) {
        this.nothingSelectedMethod = method;
        return this;
    }


    public void onClick(View v) {
        invokeClickMethod(handler, clickMethod, v);
    }

    public boolean onLongClick(View v) {
        return invokeLongClickMethod(handler, longClickMethod, v);
    }

    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        invokeItemClickMethod(handler, itemClickMethod, parent, view, position, id);
    }

    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        return invokeItemLongClickMethod(handler, itemLongClickMethod, parent, view, position, id);
    }

    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        invokeItemSelectMethod(handler, itemSelectMethod, parent, view, position, id);
    }

    public void onNothingSelected(AdapterView<?> parent) {
        invokeNoSelectMethod(handler, nothingSelectedMethod, parent);
    }


    private static Object invokeClickMethod(Object handler, String methodName, Object... params) {
        if (handler == null) return null;
        Method method = null;
        try {
            // void onClick(View v)
            method = handler.getClass().getDeclaredMethod(methodName, View.class);
            if (method != null) {
                return method.invoke(handler, params);
            } else {
                throw new ViewException("no such method:" + methodName);
            }
        } catch (Exception e) {
            LogUtils.e(e.getMessage(), e);
        }

        return null;
    }

    private static boolean invokeLongClickMethod(Object handler, String methodName, Object... params) {
        if (handler == null) return false;
        Method method = null;
        try {
            // boolean onLongClick(View v)
            method = handler.getClass().getDeclaredMethod(methodName, View.class);
            if (method != null) {
                Object obj = method.invoke(handler, params);
                return obj == null ? false : Boolean.valueOf(obj.toString());
            } else {
                throw new ViewException("no such method:" + methodName);
            }
        } catch (Exception e) {
            LogUtils.e(e.getMessage(), e);
        }

        return false;
    }

    private static Object invokeItemClickMethod(Object handler, String methodName, Object... params) {
        if (handler == null) return null;
        Method method = null;
        try {
            // void onItemClick(AdapterView<?> parent, View view, int position, long id)
            method = handler.getClass().getDeclaredMethod(methodName, AdapterView.class, View.class, int.class, long.class);
            if (method != null) {
                return method.invoke(handler, params);
            } else {
                throw new ViewException("no such method:" + methodName);
            }
        } catch (Exception e) {
            LogUtils.e(e.getMessage(), e);
        }

        return null;
    }

    private static boolean invokeItemLongClickMethod(Object handler, String methodName, Object... params) {
        if (handler == null) return false;
        Method method = null;
        try {
            // boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
            method = handler.getClass().getDeclaredMethod(methodName, AdapterView.class, View.class, int.class, long.class);
            if (method != null) {
                Object obj = method.invoke(handler, params);
                return Boolean.valueOf(obj == null ? false : Boolean.valueOf(obj.toString()));
            } else {
                throw new ViewException("no such method:" + methodName);
            }
        } catch (Exception e) {
            LogUtils.e(e.getMessage(), e);
        }

        return false;
    }

    private static Object invokeItemSelectMethod(Object handler, String methodName, Object... params) {
        if (handler == null) return null;
        Method method = null;
        try {
            // void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            method = handler.getClass().getDeclaredMethod(methodName, AdapterView.class, View.class, int.class, long.class);
            if (method != null) {
                return method.invoke(handler, params);
            } else {
                throw new ViewException("no such method:" + methodName);
            }
        } catch (Exception e) {
            LogUtils.e(e.getMessage(), e);
        }

        return null;
    }

    private static Object invokeNoSelectMethod(Object handler, String methodName, Object... params) {
        if (handler == null) return null;
        Method method = null;
        try {
            // void onNothingSelected(AdapterView<?> parent)
            method = handler.getClass().getDeclaredMethod(methodName, AdapterView.class);
            if (method != null) {
                return method.invoke(handler, params);
            } else {
                throw new ViewException("no such method:" + methodName);
            }
        } catch (Exception e) {
            LogUtils.e(e.getMessage(), e);
        }

        return null;
    }


}
