/*
 * Copyright (c) 2013. wyouflf (wyouflf@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lidroid.xutils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.lidroid.xutils.db.sqlite.CursorUtils;
import com.lidroid.xutils.db.sqlite.SqlBuilder;
import com.lidroid.xutils.db.sqlite.SqlInfo;
import com.lidroid.xutils.db.sqlite.WhereBuilder;
import com.lidroid.xutils.db.table.DbModel;
import com.lidroid.xutils.db.table.KeyValue;
import com.lidroid.xutils.db.table.Table;
import com.lidroid.xutils.exception.DbException;
import com.lidroid.xutils.util.LogUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DbUtils {

    //*************************************** create instance ****************************************************

    /**
     * key: dbName
     */
    private static HashMap<String, DbUtils> daoMap = new HashMap<String, DbUtils>();

    private SQLiteDatabase db;
    private DaoConfig config;

    private DbUtils(DaoConfig config) {
        if (config == null) {
            throw new RuntimeException("daoConfig is null");
        }

        if (config.getContext() == null) {
            throw new RuntimeException("android context is null");
        }

        this.db = new SQLiteDbHelper(config.getContext().getApplicationContext(), config.getDbName(), config.getDbVersion(), config.getDbUpgradeListener()).getWritableDatabase();
        this.config = config;
    }


    private synchronized static DbUtils getInstance(DaoConfig daoConfig) {
        DbUtils dao = daoMap.get(daoConfig.getDbName());
        if (dao == null) {
            dao = new DbUtils(daoConfig);
            daoMap.put(daoConfig.getDbName(), dao);
        }
        return dao;
    }

    public static DbUtils create(Context context) {
        DaoConfig config = new DaoConfig();
        config.setContext(context);

        return getInstance(config);

    }

    public static DbUtils create(Context context, boolean isDebug) {
        DaoConfig config = new DaoConfig();
        config.setContext(context);
        config.setDebug(isDebug);
        return getInstance(config);

    }

    public static DbUtils create(Context context, String dbName) {
        DaoConfig config = new DaoConfig();
        config.setContext(context);
        config.setDbName(dbName);

        return getInstance(config);
    }

    public static DbUtils create(Context context, String dbName, boolean isDebug) {
        DaoConfig config = new DaoConfig();
        config.setContext(context);
        config.setDbName(dbName);
        config.setDebug(isDebug);
        return getInstance(config);
    }

    public static DbUtils create(Context context, String dbName, boolean isDebug, int dbVersion, DbUpgradeListener dbUpgradeListener) {
        DaoConfig config = new DaoConfig();
        config.setContext(context);
        config.setDbName(dbName);
        config.setDebug(isDebug);
        config.setDbVersion(dbVersion);
        config.setDbUpgradeListener(dbUpgradeListener);
        return getInstance(config);
    }

    public static DbUtils create(DaoConfig daoConfig) {
        return getInstance(daoConfig);
    }


    //*********************************************** operations ********************************************************

    public void save(Object entity) throws DbException {
        createTableIfNotExist(entity.getClass());
        execNonQuery(SqlBuilder.buildInsertSqlInfo(entity));
    }

    public boolean saveBindingId(Object entity) throws DbException {
        createTableIfNotExist(entity.getClass());
        List<KeyValue> entityKvList = SqlBuilder.entity2KeyValueList(entity);
        if (entityKvList != null && entityKvList.size() > 0) {
            Table table = Table.get(entity.getClass());
            ContentValues cv = new ContentValues();
            DbUtils.fillContentValues(cv, entityKvList);
            Long id = db.insert(table.getTableName(), null, cv);
            if (id == -1) {
                return false;
            }
            table.getId().setValue(entity, id.toString());
            return true;
        }
        return false;
    }


    public void delete(Object entity) throws DbException {
        createTableIfNotExist(entity.getClass());
        execNonQuery(SqlBuilder.buildDeleteSqlInfo(entity));
    }

    public void deleteById(Class<?> entityType, Object id) throws DbException {
        createTableIfNotExist(entityType);
        execNonQuery(SqlBuilder.buildDeleteSqlInfo(entityType, id));
    }

    public void deleteByWhere(Class<?> entityType, WhereBuilder whereBuilder) throws DbException {
        createTableIfNotExist(entityType);
        SqlInfo sql = SqlBuilder.buildDeleteSql(entityType, whereBuilder);
        execNonQuery(sql);
    }

    public void dropDb() {
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type ='table'", null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    db.execSQL("DROP TABLE " + cursor.getString(0));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
    }


    public void update(Object entity) throws DbException {
        createTableIfNotExist(entity.getClass());
        execNonQuery(SqlBuilder.buildUpdateSqlInfo(entity));
    }

    public void update(Object entity, WhereBuilder whereBuilder) throws DbException {
        createTableIfNotExist(entity.getClass());
        execNonQuery(SqlBuilder.buildUpdateSqlInfo(entity, whereBuilder));
    }


    public <T> T findById(Class<T> entityType, Object id) throws DbException {
        createTableIfNotExist(entityType);
        SqlInfo sqlInfo = SqlBuilder.buildSelectSqlInfo(entityType, id);
        if (sqlInfo != null) {
            Cursor cursor = execQuery(sqlInfo);
            try {
                if (cursor.moveToNext()) {
                    return CursorUtils.getEntity(cursor, entityType);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (cursor != null) {
                    cursor.close();
                    cursor = null;
                }
            }
        }
        return null;
    }

    public <T> List<T> findAll(Class<T> entityType) throws DbException {
        createTableIfNotExist(entityType);
        return findAllBySql(entityType, SqlBuilder.buildSelectSql(entityType));
    }

    public <T> List<T> findAll(Class<T> entityType, String orderByColumnName, boolean desc) throws DbException {
        createTableIfNotExist(entityType);
        return findAllBySql(entityType,
                SqlBuilder.buildSelectSql(entityType).append2Sql(" ORDER BY " + orderByColumnName + (desc ? " DESC" : " ASC")));
    }

    public <T> List<T> findAllByWhere(Class<T> entityType, WhereBuilder whereBuilder) throws DbException {
        createTableIfNotExist(entityType);
        return findAllBySql(entityType, SqlBuilder.buildSelectSqlByWhere(entityType, whereBuilder));
    }

    public <T> List<T> findAllByWhere(Class<T> entityType, WhereBuilder whereBuilder, String orderByColumnName, boolean desc) throws DbException {
        createTableIfNotExist(entityType);
        return findAllBySql(entityType,
                SqlBuilder.buildSelectSqlByWhere(entityType, whereBuilder).append2Sql(" ORDER BY " + orderByColumnName + (desc ? " DESC" : " ASC")));
    }

    private <T> List<T> findAllBySql(Class<T> entityType, SqlInfo sqlInfo) throws DbException {
        createTableIfNotExist(entityType);
        Cursor cursor = execQuery(sqlInfo);
        try {
            List<T> list = new ArrayList<T>();
            while (cursor.moveToNext()) {
                T t = CursorUtils.getEntity(cursor, entityType);
                list.add(t);
            }
            return list;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return null;
    }

    public DbModel findDbModelBySQL(String sql) {
        debugSql(sql);
        Cursor cursor = db.rawQuery(sql, null);
        try {
            if (cursor.moveToNext()) {
                return CursorUtils.getDbModel(cursor);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return null;
    }

    public List<DbModel> findDbModelListBySQL(String sql) {
        debugSql(sql);
        Cursor cursor = db.rawQuery(sql, null);
        List<DbModel> dbModelList = new ArrayList<DbModel>();
        try {
            while (cursor.moveToNext()) {
                dbModelList.add(CursorUtils.getDbModel(cursor));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return dbModelList;
    }

    //******************************************** config ******************************************************

    public static class DaoConfig {
        private Context context = null;
        private String dbName = "xUtils.db"; // default db name
        private int dbVersion = 1;
        private boolean debug = true;
        private DbUpgradeListener dbUpgradeListener;

        public Context getContext() {
            return context;
        }

        public void setContext(Context context) {
            this.context = context;
        }

        public String getDbName() {
            return dbName;
        }

        public void setDbName(String dbName) {
            this.dbName = dbName;
        }

        public int getDbVersion() {
            return dbVersion;
        }

        public void setDbVersion(int dbVersion) {
            this.dbVersion = dbVersion;
        }

        public boolean isDebug() {
            return debug;
        }

        public void setDebug(boolean debug) {
            this.debug = debug;
        }

        public DbUpgradeListener getDbUpgradeListener() {
            return dbUpgradeListener;
        }

        public void setDbUpgradeListener(DbUpgradeListener dbUpgradeListener) {
            this.dbUpgradeListener = dbUpgradeListener;
        }

    }

    public interface DbUpgradeListener {
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion);
    }

    class SQLiteDbHelper extends SQLiteOpenHelper {

        private DbUpgradeListener mDbUpgradeListener;

        public SQLiteDbHelper(Context context, String name, int version, DbUpgradeListener dbUpgradeListener) {
            super(context, name, null, version);
            this.mDbUpgradeListener = dbUpgradeListener;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (mDbUpgradeListener != null) {
                mDbUpgradeListener.onUpgrade(db, oldVersion, newVersion);
            } else {
                dropDb();
            }
        }
    }

    //************************************************ private tools ***********************************

    private static void fillContentValues(ContentValues contentValues, List<KeyValue> list) {
        if (list != null && contentValues != null) {
            for (KeyValue kv : list) {
                contentValues.put(kv.getKey(), kv.getValue().toString());
            }
        } else {
            LogUtils.w("List<KeyValue> is empty or ContentValues is empty!");
        }

    }

    private void createTableIfNotExist(Class<?> entityType) throws DbException {
        if (!tableIsExist(Table.get(entityType))) {
            SqlInfo sqlInfo = SqlBuilder.buildCreateTableSql(entityType);
            execNonQuery(sqlInfo);
        }
    }

    private boolean tableIsExist(Table table) {
        if (table.isCheckDatabase()) {
            return true;
        }

        Cursor cursor = null;
        try {
            String sql = "SELECT COUNT(*) AS c FROM sqlite_master WHERE type ='table' AND name ='" + table.getTableName() + "' ";
            debugSql(sql);
            cursor = db.rawQuery(sql, null);
            if (cursor != null && cursor.moveToNext()) {
                int count = cursor.getInt(0);
                if (count > 0) {
                    table.setCheckDatabase(true);
                    return true;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }

        return false;
    }

    private void debugSql(String sql) {
        if (config != null && config.isDebug()) {
            LogUtils.d(sql);
        }
    }

    private void execNonQuery(SqlInfo sqlInfo) {
        debugSql(sqlInfo.getSql());
        if (sqlInfo.getBindingArgs() != null) {
            db.execSQL(sqlInfo.getSql(), sqlInfo.getBindingArgsAsArray());
        } else {
            db.execSQL(sqlInfo.getSql());
        }
    }

    private Cursor execQuery(SqlInfo sqlInfo) {
        debugSql(sqlInfo.getSql());
        return db.rawQuery(sqlInfo.getSql(), sqlInfo.getBindingArgsAsStringArray());
    }

}
