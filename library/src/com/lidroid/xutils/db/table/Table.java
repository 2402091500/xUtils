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

import android.text.TextUtils;

import java.util.HashMap;
import java.util.Map;


public class Table {

    private String tableName;

    private Id id;

    /**
     * key: columnName
     */
    public final HashMap<String, Column> columnMap;

    /**
     * key: className
     */
    private static final HashMap<String, Table> tableMap = new HashMap<String, Table>();

    private Table(Class<?> entityType) {
        this.tableName = TableUtils.getTableName(entityType);
        this.id = TableUtils.getId(entityType);
        this.columnMap = TableUtils.getColumnMap(entityType);
    }

    public static synchronized Table get(Class<?> entityType) {

        Table table = tableMap.get(entityType.getCanonicalName());
        if (table == null) {
            table = new Table(entityType);
            tableMap.put(entityType.getCanonicalName(), table);
        }

        return table;
    }

    public static synchronized void remove(Class<?> entityType) {
        tableMap.remove(entityType.getCanonicalName());
    }

    public static synchronized void remove(String tableName) {
        if (tableMap.size() > 0) {
            String key = null;
            for (Map.Entry<String, Table> entry : tableMap.entrySet()) {
                Table table = entry.getValue();
                if (table != null && table.getTableName().equals(tableName)) {
                    key = entry.getKey();
                    break;
                }
            }
            if (TextUtils.isEmpty(key)) {
                tableMap.remove(key);
            }
        }
    }

    public String getTableName() {
        return tableName;
    }

    public Id getId() {
        return id;
    }

    private boolean checkDatabase;

    public boolean isCheckDatabase() {
        return checkDatabase;
    }

    public void setCheckDatabase(boolean checkDatabase) {
        this.checkDatabase = checkDatabase;
    }

}
