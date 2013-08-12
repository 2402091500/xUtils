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

import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.db.sqlite.SQLiteLazyLoader;
import com.lidroid.xutils.exception.DbException;
import com.lidroid.xutils.util.LogUtils;

import java.lang.reflect.Field;
import java.util.List;

public class Foreign extends Column {

    /**
     * if this instance create by CursorUtils or SqlInfoBuilder.entity2KeyValueList, db will not be null.
     */
    public DbUtils db;

    private String foreignColumnName;

    public Foreign(Class entityType, Field field) {
        super(entityType, field);
        foreignColumnName = ColumnUtils.getForeignColumnNameByField(field);
    }

    public String getForeignColumnName() {
        return foreignColumnName;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setValue2Entity(Object entity, String valueStr) {

        Object value = null;
        if (valueStr != null) {
            Class columnType = columnField.getType();
            if (ColumnUtils.isSimpleColumnType(columnField)) {
                value = ColumnUtils.valueStr2FieldValue(columnType, valueStr);
            } else if (columnType.equals(SQLiteLazyLoader.class)) {
                value = new SQLiteLazyLoader(this, valueStr);
            } else if (columnType.equals(List.class)) {
                try {
                    value = new SQLiteLazyLoader(this, valueStr).getListFromDb();
                } catch (DbException e) {
                    LogUtils.e(e.getMessage(), e);
                }
            } else {
                try {
                    value = new SQLiteLazyLoader(this, valueStr).getOneFromDb();
                } catch (DbException e) {
                    LogUtils.e(e.getMessage(), e);
                }
            }
        }

        if (setMethod != null) {
            try {
                setMethod.invoke(entity, value);
            } catch (Exception e) {
                LogUtils.e(e.getMessage(), e);
            }
        } else {
            try {
                this.columnField.setAccessible(true);
                this.columnField.set(entity, value);
            } catch (Exception e) {
                LogUtils.e(e.getMessage(), e);
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object getColumnValue(Object entity) {
        Object resultObj = null;
        if (entity != null) {
            if (getMethod != null) {
                try {
                    resultObj = getMethod.invoke(entity);
                } catch (Exception e) {
                    LogUtils.e(e.getMessage(), e);
                }
            } else {
                try {
                    this.columnField.setAccessible(true);
                    resultObj = this.columnField.get(entity);
                } catch (Exception e) {
                    LogUtils.e(e.getMessage(), e);
                }
            }
        }

        if (resultObj != null) {
            Class columnType = columnField.getType();
            if (columnType.equals(SQLiteLazyLoader.class)) {
                resultObj = ((SQLiteLazyLoader) resultObj).getColumnValue();
            } else if (columnType.equals(List.class)) {
                try {
                    List foreignValues = (List) resultObj;
                    if (foreignValues != null && foreignValues.size() > 0) {

                        if (this.db != null) { // save associates to db
                            for (Object item : foreignValues) {
                                try {
                                    this.db.saveOrUpdate(item);
                                } catch (DbException e) {
                                    LogUtils.e(e.getMessage(), e);
                                }
                            }
                        }

                        Class foreignEntityType = ColumnUtils.getForeignEntityType(this);
                        Field foreignField = foreignEntityType.getDeclaredField(foreignColumnName);
                        Column column = new Column(foreignEntityType, foreignField);
                        resultObj = column.getColumnValue(foreignValues.get(0));
                    }
                } catch (NoSuchFieldException e) {
                    resultObj = null;
                    LogUtils.e(e.getMessage(), e);
                }
            } else {
                try {
                    if (this.db != null) {// save associates to db
                        try {
                            this.db.saveOrUpdate(resultObj);
                        } catch (DbException e) {
                            LogUtils.e(e.getMessage(), e);
                        }
                    }
                    Field foreignField = columnType.getDeclaredField(foreignColumnName);
                    Column column = new Column(columnType, foreignField);
                    resultObj = column.getColumnValue(resultObj);
                } catch (NoSuchFieldException e) {
                    resultObj = null;
                    LogUtils.e(e.getMessage(), e);
                }
            }
        }

        return ColumnUtils.convert2DbColumnValueIfNeeded(resultObj);
    }

    @Override
    public String getDbType() {
        try {
            return ColumnUtils.fieldType2DbType(ColumnUtils.getForeignEntityType(this).getDeclaredField(foreignColumnName).getType());
        } catch (NoSuchFieldException e) {
            return "TEXT";
        }
    }
}
