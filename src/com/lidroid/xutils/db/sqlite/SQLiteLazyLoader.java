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

package com.lidroid.xutils.db.sqlite;

import com.lidroid.xutils.db.table.Column;
import com.lidroid.xutils.db.table.ColumnUtils;
import com.lidroid.xutils.db.table.Foreign;
import com.lidroid.xutils.db.table.TableUtils;
import com.lidroid.xutils.exception.DbException;
import com.lidroid.xutils.util.LogUtils;

import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.List;

public class SQLiteLazyLoader<T> {

    private Class<T> foreignEntityType;

    private String foreignColumnName;
    private Foreign foreignColumn;

    private String valueStr;

    public SQLiteLazyLoader(String foreignColumnName, String valueStr) {

        this.foreignColumnName = foreignColumnName;
        this.valueStr = valueStr;
        HashMap<String, Column> columns = TableUtils.getColumnMap(foreignEntityType);
        if (columns != null && columns.containsKey(foreignColumnName)) {
            this.foreignColumn = (Foreign) columns.get(foreignColumnName);
        }

        setForeignEntityType(foreignColumn);
    }

    public SQLiteLazyLoader(Foreign foreignColumn, String valueStr) {

        this.foreignColumn = foreignColumn;
        this.foreignColumnName = foreignColumn.getForeignColumnName();
        this.valueStr = valueStr;

        setForeignEntityType(foreignColumn);
    }

    @SuppressWarnings("unchecked")
    private void setForeignEntityType(Foreign foreignColumn) {
        foreignEntityType = (Class<T>) foreignColumn.getColumnField().getType();
        if (foreignEntityType.equals(SQLiteLazyLoader.class)) {
            foreignEntityType = (Class) ((ParameterizedType) foreignColumn.getColumnField().getGenericType()).getActualTypeArguments()[0];
        } else if (foreignEntityType.equals(List.class)) {
            foreignEntityType = (Class) ((ParameterizedType) foreignColumn.getColumnField().getGenericType()).getActualTypeArguments()[0];
        }
    }

    public List<T> getListFromDb() throws DbException {
        List<T> entities = null;
        if (foreignColumn != null && foreignColumn.db != null) {
            Object columnValue = this.getColumnValue();
            entities = foreignColumn.db.findAll(Selector.from(foreignEntityType).where(WhereBuilder.b(foreignColumnName, "=", columnValue)));
        }
        return entities;
    }

    public T getOneFromDb() throws DbException {
        T entity = null;
        if (foreignColumn != null && foreignColumn.db != null) {
            Object columnValue = this.getColumnValue();
            entity = foreignColumn.db.findFirst(Selector.from(foreignEntityType).where(WhereBuilder.b(foreignColumnName, "=", columnValue)));
        }
        return entity;
    }

    public Object getColumnValue() {
        if (foreignColumn != null) {
            try {
                return ColumnUtils.valueStr2FieldValue(foreignEntityType.getDeclaredField(foreignColumnName).getType(), valueStr);
            } catch (NoSuchFieldException e) {
                LogUtils.d(e.getMessage(), e);
            }
        }
        return null;
    }
}
