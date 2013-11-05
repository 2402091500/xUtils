package com.lidroid.xutils.db.converter;

import android.database.Cursor;

/**
 * Author: wyouflf
 * Date: 13-11-4
 * Time: 下午10:51
 */
public class LongColumnConverter implements ColumnConverter<Long, Object> {
    @Override
    public Long getFiledValue(Object entity, Cursor cursor, int index) {
        return cursor.getLong(index);
    }

    @Override
    public Object fieldValue2ColumnValue(Object entity, Long fieldValue) {
        return fieldValue;
    }

    @Override
    public String getColumnDbType() {
        return "INTEGER";
    }
}
