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

import java.util.LinkedList;

public class SqlInfo {

    private String sql;
    private LinkedList<Object> bindingArgs;

    public SqlInfo() {
    }

    public SqlInfo(String sql) {
        this.sql = sql;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public SqlInfo append2Sql(String str) {
        sql += str;
        return this;
    }

    public LinkedList<Object> getBindingArgs() {
        return bindingArgs;
    }

    public Object[] getBindingArgsAsArray() {
        if (bindingArgs != null) {
            return bindingArgs.toArray();
        }
        return null;
    }

    public String[] getBindingArgsAsStringArray() {
        if (bindingArgs != null) {
            String[] strings = new String[bindingArgs.size()];
            for (int i = 0; i < bindingArgs.size(); i++) {
                strings[i] = bindingArgs.get(i).toString();
            }
            return strings;
        }
        return null;
    }

    public void addValue(Object value) {
        if (bindingArgs == null) {
            bindingArgs = new LinkedList<Object>();
        }

        bindingArgs.add(ColumnUtils.convertIfNeeded(value));
    }

}
