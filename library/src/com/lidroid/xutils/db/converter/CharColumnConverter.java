package com.lidroid.xutils.db.converter;

import android.database.Cursor;

/**
 * Author: wyouflf
 * Date: 13-11-4
 * Time: 下午10:51
 */
public class CharColumnConverter implements ColumnConverter<Character, Object> {
    @Override
    public Character getFiledValue(Object entity, Cursor cursor, int index) {
        return (char) cursor.getInt(index);
    }

    @Override
    public Object fieldValue2ColumnValue(Object entity, Character fieldValue) {
        if (fieldValue == null) return null;
        return (int) fieldValue;
    }

    @Override
    public String getColumnDbType() {
        return "INTEGER";
    }
}
