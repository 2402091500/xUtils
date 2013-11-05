package com.lidroid.xutils.db.converter;

import android.database.Cursor;

/**
 * Author: wyouflf
 * Date: 13-11-4
 * Time: 下午10:51
 */
public class BooleanColumnConverter implements ColumnConverter<Boolean, Object> {
    @Override
    public Boolean getFiledValue(Object entity, Cursor cursor, int index) {
        return cursor.getInt(index) == 1;
    }

    @Override
    public Object fieldValue2ColumnValue(Object entity, Boolean fieldValue) {
        if (fieldValue == null) return null;
        return fieldValue ? 1 : 0;
    }

    @Override
    public String getColumnDbType() {
        return "INTEGER";
    }
}
