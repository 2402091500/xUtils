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

import com.lidroid.xutils.db.annotation.NoAutoIncrement;
import com.lidroid.xutils.util.LogUtils;

import java.lang.reflect.Field;

public class Id extends Column {

    protected Id(Class<?> entityType, Field field) {
        super(entityType, field);
    }

    public boolean isAutoIncrement() {
        if (this.getColumnField().getAnnotation(NoAutoIncrement.class) != null) {
            return false;
        }
        Class<?> idType = this.getColumnField().getType();
        return idType.equals(int.class) ||
                idType.equals(Integer.class) ||
                idType.equals(long.class) ||
                idType.equals(Long.class);
    }

    public void setAutoIncrementId(Object entity, long value) {
        Object idValue = value;
        Class<?> columnFieldType = columnField.getType();
        if (columnFieldType.equals(int.class) || columnFieldType.equals(Integer.class)) {
            idValue = (int) value;
        }

        if (setMethod != null) {
            try {
                setMethod.invoke(entity, idValue);
            } catch (Throwable e) {
                LogUtils.e(e.getMessage(), e);
            }
        } else {
            try {
                this.columnField.setAccessible(true);
                this.columnField.set(entity, idValue);
            } catch (Throwable e) {
                LogUtils.e(e.getMessage(), e);
            }
        }
    }

    @Override
    public Object getColumnValue(Object entity) {
        Object idValue = super.getColumnValue(entity);
        if (idValue != null) {
            if (this.isAutoIncrement() && (idValue.equals(0) || idValue.equals(0L))) {
                return null;
            } else {
                return idValue;
            }
        } else {
            return null;
        }
    }
}
