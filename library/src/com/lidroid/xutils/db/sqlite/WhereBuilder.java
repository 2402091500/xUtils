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

import android.text.TextUtils;
import com.lidroid.xutils.db.table.ColumnUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: wyouflf
 * Date: 13-7-29
 * Time: 上午9:35
 */
public class WhereBuilder {

    private final List<String> whereItems;

    private WhereBuilder() {
        this.whereItems = new ArrayList<String>();
    }

    /**
     * create new instance
     *
     * @return
     */
    public static WhereBuilder b() {
        return new WhereBuilder();
    }

    /**
     * create new instance
     *
     * @param columnName
     * @param op         operator: "=","<","LIKE"...
     * @param value
     * @return
     */
    public static WhereBuilder b(String columnName, String op, Object value) {
        WhereBuilder result = new WhereBuilder();
        result.appendCondition(null, columnName, op, value);
        return result;
    }

    /**
     * add AND condition
     *
     * @param columnName
     * @param op         operator: "=","<","LIKE"...
     * @param value
     * @return
     */
    public WhereBuilder and(String columnName, String op, Object value) {
        appendCondition(whereItems.size() == 0 ? null : "AND", columnName, op, value);
        return this;
    }

    /**
     * add OR condition
     *
     * @param columnName
     * @param op         operator: "=","<","LIKE"...
     * @param value
     * @return
     */
    public WhereBuilder or(String columnName, String op, Object value) {
        appendCondition(whereItems.size() == 0 ? null : "OR", columnName, op, value);
        return this;
    }

    public WhereBuilder expr(String expr) {
        whereItems.add(" " + expr);
        return this;
    }

    public WhereBuilder expr(String columnName, String op, Object value) {
        appendCondition(null, columnName, op, value);
        return this;
    }

    public int getWhereItemSize() {
        return whereItems.size();
    }

    @Override
    public String toString() {
        if (whereItems.size() < 1) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (String item : whereItems) {
            sb.append(item);
        }
        return sb.toString();
    }

    private void appendCondition(String conj, String columnName, String op, Object value) {
        StringBuilder sqlSb = new StringBuilder();

        if (whereItems.size() > 0) {
            sqlSb.append(" ");
        }

        // append conj
        if (!TextUtils.isEmpty(conj)) {
            sqlSb.append(conj + " ");
        }

        // append columnName
        sqlSb.append(columnName);

        // convert op
        if ("!=".equals(op)) {
            op = "<>";
        } else if ("==".equals(op)) {
            op = "=";
        }

        // append op & value
        if (value == null) {
            if ("=".equals(op)) {
                sqlSb.append(" IS NULL");
            } else if ("<>".equals(op)) {
                sqlSb.append(" IS NOT NULL");
            } else {
                sqlSb.append(" " + op + " NULL");
            }
        } else {
            sqlSb.append(" " + op + " ");
            value = ColumnUtils.convert2DbColumnValueIfNeeded(value);
            if ("TEXT".equals(ColumnUtils.fieldType2DbType(value.getClass()))) {
                String valueStr = value.toString();
                if (valueStr.indexOf('\'') != -1) { // convert single quotations
                    valueStr = valueStr.replace("'", "''");
                }
                sqlSb.append("'" + valueStr + "'");
            } else {
                sqlSb.append(value);
            }
        }
        whereItems.add(sqlSb.toString());
    }
}
