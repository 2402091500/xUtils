package com.lidroid.xutils.db.converter;

import android.database.Cursor;

/**
 * Author: wyouflf
 * Date: 13-11-4
 * Time: 下午10:51
 */
public class IntegerColumnConverter implements ColumnConverter<Integer, Object> {
    @Override
    public Integer getFiledValue(Object entity, Cursor cursor, int index) {
        return cursor.getInt(index);
    }

    @Override
    public Object fieldValue2ColumnValue(Object entity, Integer fieldValue) {
        return fieldValue;
    }

    @Override
    public String getColumnDbType() {
        return "INTEGER";
    }
}
