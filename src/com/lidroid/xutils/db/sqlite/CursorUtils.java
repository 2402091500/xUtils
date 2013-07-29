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

import android.database.Cursor;
import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.db.table.Column;
import com.lidroid.xutils.db.table.DbModel;
import com.lidroid.xutils.db.table.Foreign;
import com.lidroid.xutils.db.table.Table;
import com.lidroid.xutils.util.LogUtils;

public class CursorUtils {

    @SuppressWarnings("unchecked")
    public static <T> T getEntity(DbUtils db, Cursor cursor, Class<T> entityType) {
        try {
            if (cursor != null) {
                int columnCount = cursor.getColumnCount();
                Table table = Table.get(entityType);
                T entity = entityType.newInstance();
                for (int i = 0; i < columnCount; i++) {
                    String columnName = cursor.getColumnName(i);
                    Column column = table.columnMap.get(columnName);
                    if (column != null) {
                        if (column instanceof Foreign) {
                            ((Foreign) column).db = db;
                        }
                        column.setValue2Entity(entity, cursor.getString(i));
                    } else if (columnName.equals(table.getId().getColumnName())) {
                        table.getId().setValue2Entity(entity, cursor.getString(i));
                    }
                }
                return entity;
            }
        } catch (Exception e) {
            LogUtils.e(e.getMessage(), e);
        }

        return null;
    }

    public static DbModel getDbModel(Cursor cursor) {
        DbModel result = null;
        if (cursor != null) {
            result = new DbModel();
            int columnCount = cursor.getColumnCount();
            for (int i = 0; i < columnCount; i++) {
                result.add(cursor.getColumnName(i), cursor.getString(i));
            }
        }
        return result;
    }
}
