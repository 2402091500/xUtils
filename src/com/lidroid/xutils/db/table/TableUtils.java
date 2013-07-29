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

import com.lidroid.xutils.db.annotation.Id;
import com.lidroid.xutils.db.annotation.Table;
import com.lidroid.xutils.util.LogUtils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class TableUtils {

    private TableUtils() {
    }

    public static String getTableName(Class<?> entityType) {
        Table table = entityType.getAnnotation(Table.class);
        if (table == null || table.name().trim().length() == 0) {
            //当没有注解的时候默认用类的名称作为表名,并把点（.）替换为下划线(_)
            return entityType.getName().replace('.', '_');
        }
        return table.name();
    }

    /**
     * key: entityType.className
     */
    private static ConcurrentHashMap<String, HashMap<String, Column>> entityColumnsMap = new ConcurrentHashMap<String, HashMap<String, Column>>();

    /**
     * @param entityType
     * @return key: columnName
     */
    public static HashMap<String, Column> getColumnMap(Class<?> entityType) {

        if (entityColumnsMap.containsKey(entityType.getCanonicalName())) {
            return entityColumnsMap.get(entityType.getCanonicalName());
        }
        HashMap<String, Column> columnMap = new HashMap<String, Column>();
        try {
            Field[] fields = entityType.getDeclaredFields();
            String primaryKeyFieldName = getPrimaryKeyFieldName(entityType);
            for (Field field : fields) {
                if (ColumnUtils.isTransient(field)) {
                    continue;
                }
                if (ColumnUtils.isSimpleColumnType(field)) {
                    if (!field.getName().equals(primaryKeyFieldName)) {
                        Column column = new Column(entityType, field);
                        columnMap.put(column.getColumnName(), column);
                    }
                } else if (ColumnUtils.isForeign(field)) {
                    Foreign column = new Foreign(entityType, field);
                    columnMap.put(column.getColumnName(), column);
                }
            }
        } catch (Exception e) {
            LogUtils.e(e.getMessage(), e);
        }

        if (columnMap != null && entityColumnsMap.containsKey(entityType.getCanonicalName())) {
            entityColumnsMap.put(entityType.getCanonicalName(), columnMap);
        }
        return columnMap;
    }

    public static Field getPrimaryKeyField(Class<?> entityType) {
        Field primaryKeyField = null;
        Field[] fields = entityType.getDeclaredFields();
        if (fields != null) {

            for (Field field : fields) {
                if (field.getAnnotation(Id.class) != null) {
                    primaryKeyField = field;
                    break;
                }
            }

            if (primaryKeyField == null) {
                for (Field field : fields) {
                    if ("id".equals(field.getName()) || "_id".equals(field.getName())) {
                        primaryKeyField = field;
                        break;
                    }
                }
            }

        } else {
            throw new RuntimeException("this model[" + entityType + "] has no any field");
        }
        return primaryKeyField;
    }

    public static String getPrimaryKeyFieldName(Class<?> entityType) {
        Field idField = getPrimaryKeyField(entityType);
        return idField == null ? null : idField.getName();
    }

    public static boolean hasPrimaryKeyValue(Object entity) {
        if (entity == null) {
            return false;
        }
        Class entityType = entity.getClass();
        Field primaryKeyField = null;
        Field[] fields = entityType.getDeclaredFields();
        if (fields != null) {

            for (Field field : fields) {
                if (field.getAnnotation(Id.class) != null) {
                    primaryKeyField = field;
                    break;
                }
            }

            if (primaryKeyField == null) {
                for (Field field : fields) {
                    if ("id".equals(field.getName()) || "_id".equals(field.getName())) {
                        primaryKeyField = field;
                        break;
                    }
                }
            }

        } else {
            throw new RuntimeException("this model[" + entityType + "] has no any field");
        }

        if (primaryKeyField != null) {
            com.lidroid.xutils.db.table.Id id = new com.lidroid.xutils.db.table.Id(entityType, primaryKeyField);
            return id.getColumnValue(entity) == null;
        }
        return false;
    }
}
