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

import android.view.View;
import com.lidroid.xutils.util.LogUtils;
import com.lidroid.xutils.util.core.DoubleKeyValueMap;
import com.lidroid.xutils.view.annotation.event.EventBase;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;

public class ViewEventListenerManager {

    private ViewEventListenerManager() {
    }

    /**
     * k1: viewInjectInfo
     * k2: interface Type
     * value: listener
     */
    private final static DoubleKeyValueMap<ViewInjectInfo, Class<?>, Object> listenerCache =
            new DoubleKeyValueMap<ViewInjectInfo, Class<?>, Object>();

    public static void addEventMethod(
            ViewFinder finder,
            ViewInjectInfo info,
            Annotation eventAnnotation,
            Object handler,
            Method method) {
        try {
            View view = finder.findViewByInfo(info);
            if (view != null) {
                EventBase eventBase = eventAnnotation.annotationType().getAnnotation(EventBase.class);
                Class<?> eventListenerType = eventBase.eventListenerType();
                String eventListenerSetter = eventBase.eventListenerSetter();
                String methodName = eventBase.methodName();

                Object listener = listenerCache.get(info, eventListenerType);
                DynamicHandler dynamicHandler = null;
                if (listener != null) {
                    dynamicHandler = (DynamicHandler) Proxy.getInvocationHandler(listener);
                    if (handler != dynamicHandler.getHandler()) {
                        dynamicHandler.setHandler(handler);
                    }
                    dynamicHandler.addMethod(methodName, method);
                } else {
                    dynamicHandler = new DynamicHandler(handler);
                    dynamicHandler.addMethod(methodName, method);
                    listener = Proxy.newProxyInstance(
                            eventListenerType.getClassLoader(),
                            new Class<?>[]{eventListenerType},
                            dynamicHandler);

                    listenerCache.put(info, eventListenerType, listener);
                }

                Method setEventListenerMethod = view.getClass().getMethod(eventListenerSetter, eventListenerType);
                setEventListenerMethod.invoke(view, listener);
            }
        } catch (Throwable e) {
            LogUtils.e(e.getMessage(), e);
        }
    }

    public static class DynamicHandler implements InvocationHandler {
        private Object handler;
        private final HashMap<String, Method> methodMap = new HashMap<String, Method>(1);

        public DynamicHandler(Object handler) {
            this.handler = handler;
        }

        public void addMethod(String name, Method method) {
            methodMap.put(name, method);
        }

        public Object getHandler() {
            return handler;
        }

        public void setHandler(Object handler) {
            this.handler = handler;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            String methodName = method.getName();
            method = methodMap.get(methodName);
            if (method != null) {
                return method.invoke(handler, args);
            }
            return null;
        }
    }
}
