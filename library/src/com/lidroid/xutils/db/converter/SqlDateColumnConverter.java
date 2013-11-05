package com.lidroid.xutils.db.converter;

import android.database.Cursor;

/**
 * Author: wyouflf
 * Date: 13-11-4
 * Time: 下午10:51
 */
public class SqlDateColumnConverter implements ColumnConverter<java.sql.Date, Object> {
    @Override
    public java.sql.Date getFiledValue(Object entity, Cursor cursor, int index) {
        return new java.sql.Date(cursor.getLong(index));
    }

    @Override
    public Object fieldValue2ColumnValue(Object entity, java.sql.Date fieldValue) {
        if (fieldValue == null) return null;
        return fieldValue.getTime();
    }

    @Override
    public String getColumnDbType() {
        return "INTEGER";
    }
}
