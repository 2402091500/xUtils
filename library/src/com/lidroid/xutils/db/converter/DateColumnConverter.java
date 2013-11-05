package com.lidroid.xutils.db.converter;

import android.database.Cursor;

import java.util.Date;

/**
 * Author: wyouflf
 * Date: 13-11-4
 * Time: 下午10:51
 */
public class DateColumnConverter implements ColumnConverter<Date, Object> {
    @Override
    public Date getFiledValue(Object entity, Cursor cursor, int index) {
        return new Date(cursor.getLong(index));
    }

    @Override
    public Object fieldValue2ColumnValue(Object entity, Date fieldValue) {
        if (fieldValue == null) return null;
        return fieldValue.getTime();
    }

    @Override
    public String getColumnDbType() {
        return "INTEGER";
    }
}
