package com.lidroid.xutils.db.converter;

import android.database.Cursor;

/**
 * Author: wyouflf
 * Date: 13-11-4
 * Time: 下午10:51
 */
public class ShortColumnConverter implements ColumnConverter<Short, Object> {
    @Override
    public Short getFiledValue(Object entity, Cursor cursor, int index) {
        return cursor.getShort(index);
    }

    @Override
    public Object fieldValue2ColumnValue(Object entity, Short fieldValue) {
        return fieldValue;
    }

    @Override
    public String getColumnDbType() {
        return "INTEGER";
    }
}
