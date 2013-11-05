package com.lidroid.xutils.db.converter;

import android.database.Cursor;

/**
 * Author: wyouflf
 * Date: 13-11-4
 * Time: 下午10:51
 */
public class StringColumnConverter implements ColumnConverter<String, Object> {
    @Override
    public String getFiledValue(Object entity, Cursor cursor, int index) {
        return cursor.getString(index);
    }

    @Override
    public Object fieldValue2ColumnValue(Object entity, String fieldValue) {
        return fieldValue;
    }

    @Override
    public String getColumnDbType() {
        return "TEXT";
    }
}
