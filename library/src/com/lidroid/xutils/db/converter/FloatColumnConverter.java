package com.lidroid.xutils.db.converter;

import android.database.Cursor;

/**
 * Author: wyouflf
 * Date: 13-11-4
 * Time: 下午10:51
 */
public class FloatColumnConverter implements ColumnConverter<Float, Object> {
    @Override
    public Float getFiledValue(Object entity, Cursor cursor, int index) {
        return cursor.getFloat(index);
    }

    @Override
    public Object fieldValue2ColumnValue(Object entity, Float fieldValue) {
        return fieldValue;
    }

    @Override
    public String getColumnDbType() {
        return "REAL";
    }
}
