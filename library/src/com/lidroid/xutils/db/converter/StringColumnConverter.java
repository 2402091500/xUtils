package com.lidroid.xutils.db.converter;

import android.database.Cursor;
import android.text.TextUtils;

/**
 * Author: wyouflf
 * Date: 13-11-4
 * Time: 下午10:51
 */
public class StringColumnConverter implements ColumnConverter<String, Object> {
    @Override
    public String getFiledValue(Cursor cursor, int index) {
        return cursor.getString(index);
    }

    @Override
    public String getFiledValue(String fieldStringValue) {
        return fieldStringValue;
    }

    @Override
    public Object fieldValue2ColumnValue(String fieldValue) {
        return fieldValue;
    }

    @Override
    public String getColumnDbType() {
        return "TEXT";
    }
}
