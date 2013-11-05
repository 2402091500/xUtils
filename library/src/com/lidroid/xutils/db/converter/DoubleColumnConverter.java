package com.lidroid.xutils.db.converter;

import android.database.Cursor;

/**
 * Author: wyouflf
 * Date: 13-11-4
 * Time: 下午10:51
 */
public class DoubleColumnConverter implements ColumnConverter<Double, Object> {
    @Override
    public Double getFiledValue(Object entity, Cursor cursor, int index) {
        return cursor.getDouble(index);
    }

    @Override
    public Object fieldValue2ColumnValue(Object entity, Double fieldValue) {
        return fieldValue;
    }

    @Override
    public String getColumnDbType() {
        return "REAL";
    }
}
