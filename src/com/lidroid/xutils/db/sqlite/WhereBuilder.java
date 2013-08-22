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

import com.lidroid.xutils.db.table.ColumnUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: wyouflf
 * Date: 13-7-29
 * Time: 上午9:35
 */
public class WhereBuilder {

    private List<String> whereItems;

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
    public WhereBuilder append(String columnName, String op, Object value) {
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
    public WhereBuilder appendOR(String columnName, String op, Object value) {
        appendCondition(whereItems.size() == 0 ? null : "OR", columnName, op, value);
        return this;
    }

    @Override
    public String toString() {
        if (whereItems == null || whereItems.size() < 1) {
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
        if (conj != null && conj.length() > 0) {
            sqlSb.append(" " + conj + " ");
        }
        sqlSb.append(columnName).append(" " + op + " ");
        value = ColumnUtils.convert2DbColumnValueIfNeeded(value);
        if (value == null) {
            sqlSb.append("NULL");
        } else if ("TEXT".equals(ColumnUtils.fieldType2DbType(value.getClass()))) {
            String valueStr = value.toString();
            if (valueStr.contains("'")) { // 单引号转义
                valueStr = valueStr.replace("'", "''");
            }
            sqlSb.append("'" + valueStr + "'");
        } else {
            sqlSb.append(value);
        }
        whereItems.add(sqlSb.toString());
    }
}
