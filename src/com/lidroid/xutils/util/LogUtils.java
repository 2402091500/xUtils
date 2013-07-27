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

package com.lidroid.xutils.util;

import android.util.Log;

/**
 * Author: wyouflf
 * Date: 13-7-24
 * Time: 下午12:23
 */
public class LogUtils {

    private LogUtils() {
    }

    private static String generateTag(StackTraceElement caller) {
        String tag = "%s[%s, %d]";
        String callerClazzName = caller.getClassName();
        callerClazzName = callerClazzName.substring(callerClazzName.lastIndexOf(".") + 1);
        tag = String.format(tag, callerClazzName, caller.getMethodName(), caller.getLineNumber());
        return tag;
    }

    public static void d(String content) {
        StackTraceElement caller = ReflectUtils.getCallerMethodName();
        String tag = generateTag(caller);
        Log.d(tag, content);
    }

    public static void d(String content, Throwable tr) {
        StackTraceElement caller = ReflectUtils.getCallerMethodName();
        String tag = generateTag(caller);
        Log.d(tag, content, tr);
    }

    public static void e(String content) {
        StackTraceElement caller = ReflectUtils.getCallerMethodName();
        String tag = generateTag(caller);
        Log.e(tag, content);
    }

    public static void e(String content, Throwable tr) {
        StackTraceElement caller = ReflectUtils.getCallerMethodName();
        String tag = generateTag(caller);
        Log.e(tag, content, tr);
    }

    public static void i(String content) {
        StackTraceElement caller = ReflectUtils.getCallerMethodName();
        String tag = generateTag(caller);
        Log.i(tag, content);
    }

    public static void i(String content, Throwable tr) {
        StackTraceElement caller = ReflectUtils.getCallerMethodName();
        String tag = generateTag(caller);
        Log.i(tag, content, tr);
    }

    public static void v(String content) {
        StackTraceElement caller = ReflectUtils.getCallerMethodName();
        String tag = generateTag(caller);
        Log.v(tag, content);
    }

    public static void v(String content, Throwable tr) {
        StackTraceElement caller = ReflectUtils.getCallerMethodName();
        String tag = generateTag(caller);
        Log.v(tag, content, tr);
    }

    public static void w(String content) {
        StackTraceElement caller = ReflectUtils.getCallerMethodName();
        String tag = generateTag(caller);
        Log.w(tag, content);
    }

    public static void w(String content, Throwable tr) {
        StackTraceElement caller = ReflectUtils.getCallerMethodName();
        String tag = generateTag(caller);
        Log.w(tag, content, tr);
    }

    public static void w(Throwable tr) {
        StackTraceElement caller = ReflectUtils.getCallerMethodName();
        String tag = generateTag(caller);
        Log.w(tag, tr);
    }


    public static void wtf(String content) {
        StackTraceElement caller = ReflectUtils.getCallerMethodName();
        String tag = generateTag(caller);
        Log.wtf(tag, content);
    }

    public static void wtf(String content, Throwable tr) {
        StackTraceElement caller = ReflectUtils.getCallerMethodName();
        String tag = generateTag(caller);
        Log.wtf(tag, content, tr);
    }

    public static void wtf(Throwable tr) {
        StackTraceElement caller = ReflectUtils.getCallerMethodName();
        String tag = generateTag(caller);
        Log.wtf(tag, tr);
    }

}
