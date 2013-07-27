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

package com.lidroid.xutils.db.table;

import com.lidroid.xutils.util.LogUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Date;

public class Column {

    private String columnName;
    private String defaultValue;

    private Method getMethod;
    private Method setMethod;

    private Field columnField;

    public Column(Class entityType, Field field) {
        this.columnField = field;
        this.columnName = ColumnUtils.getColumnNameByField(field);
        this.defaultValue = ColumnUtils.getColumnDefaultValue(field);
        this.getMethod = ColumnUtils.getColumnGetMethod(entityType, field);
        this.setMethod = ColumnUtils.getColumnSetMethod(entityType, field);
    }

    @SuppressWarnings("unchecked")
    public void setValue(Object entity, String valueStr) {
        Object value = null;
        if (valueStr != null) {
            Class columnType = columnField.getType();
            if (columnType.equals(String.class) || columnType.equals(CharSequence.class)) {
                value = valueStr;
            } else if (columnType.equals(int.class) || columnType.equals(Integer.class)) {
                value = Integer.valueOf(valueStr);
            } else if (columnType.equals(long.class) || columnType.equals(Long.class)) {
                value = Long.valueOf(valueStr);
            } else if (columnType.equals(java.sql.Date.class)) {
                value = new java.sql.Date(Long.valueOf(valueStr));
            } else if (columnType.equals(Date.class)) {
                value = new Date(Long.valueOf(valueStr));
            } else if (columnType.equals(boolean.class) || columnType.equals(Boolean.class)) {
                value = ColumnUtils.convert2Boolean(valueStr);
            } else if (columnType.equals(float.class) || columnType.equals(Float.class)) {
                value = Float.valueOf(valueStr);
            } else if (columnType.equals(double.class) || columnType.equals(Double.class)) {
                value = Double.valueOf(valueStr);
            } else if (columnType.equals(byte.class) || columnType.equals(Byte.class)) {
                value = Byte.valueOf(valueStr);
            } else if (columnType.equals(short.class) || columnType.equals(Short.class)) {
                value = Short.valueOf(valueStr);
            } else if (columnType.equals(char.class) || columnType.equals(Character.class)) {
                value = valueStr.charAt(0);
            }
        }

        if (setMethod != null) {
            try {
                setMethod.invoke(entity, value == null ? defaultValue : value);
            } catch (Exception e) {
                LogUtils.e(e.getMessage());
            }
        } else {
            try {
                this.columnField.setAccessible(true);
                this.columnField.set(entity, value == null ? defaultValue : value);
            } catch (Exception e) {
                LogUtils.e(e.getMessage());
            }
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T getValue(Object entity) {
        Object resultObj = null;
        if (entity != null) {
            if (getMethod != null) {
                try {
                    resultObj = getMethod.invoke(entity);
                } catch (Exception e) {
                    LogUtils.e(e.getMessage());
                }
            } else {
                try {
                    this.columnField.setAccessible(true);
                    resultObj = this.columnField.get(entity);
                } catch (Exception e) {
                    LogUtils.e(e.getMessage());
                }
            }
        }
        return (T) resultObj;
    }

    public String getColumnName() {
        return columnName;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public Field getColumnField() {
        return columnField;
    }
}
