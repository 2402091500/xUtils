package com.lidroid.xutils.db.converter;

import android.database.Cursor;

/**
 * Author: wyouflf
 * Date: 13-11-4
 * Time: 下午10:51
 */
public class ByteColumnConverter implements ColumnConverter<Byte, Object> {
    @Override
    public Byte getFiledValue(Object entity, Cursor cursor, int index) {
        return (byte) cursor.getInt(index);
    }

    @Override
    public Object fieldValue2ColumnValue(Object entity, Byte fieldValue) {
        return fieldValue;
    }

    @Override
    public String getColumnDbType() {
        return "INTEGER";
    }
}
