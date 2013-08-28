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
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import com.lidroid.xutils.db.sqlite.CursorUtils;
import com.lidroid.xutils.db.sqlite.DbModelSelector;
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.db.sqlite.SqlInfo;
import com.lidroid.xutils.db.sqlite.SqlInfoBuilder;
import com.lidroid.xutils.db.sqlite.WhereBuilder;
import com.lidroid.xutils.db.table.DbModel;
import com.lidroid.xutils.db.table.Id;
import com.lidroid.xutils.db.table.KeyValue;
import com.lidroid.xutils.db.table.Table;
import com.lidroid.xutils.db.table.TableUtils;
import com.lidroid.xutils.exception.DbException;
import com.lidroid.xutils.util.IOUtils;
import com.lidroid.xutils.util.LogUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DbUtils {

    //*************************************** create instance ****************************************************

    /**
     * key: dbName
     */
    private static HashMap<String, DbUtils> daoMap = new HashMap<String, DbUtils>();

    private SQLiteDatabase database;
    private DaoConfig config;
    private boolean debug = false;
    private boolean allowTransaction = false;

    private DbUtils(DaoConfig config) throws DbException {
        if (config == null) {
            throw new RuntimeException("daoConfig is null");
        }

        if (config.getContext() == null) {
            throw new RuntimeException("android context is null");
        }

        String sdCardPath = config.getSdCardPath();
        if (TextUtils.isEmpty(sdCardPath)) {
            this.database = new SQLiteDbHelper(
                    config.getContext().getApplicationContext(),
                    config.getDbName(),
                    config.getDbVersion(),
                    config.getDbUpgradeListener()).getWritableDatabase();
        } else {
            this.database = createDbFileOnSDCard(sdCardPath, config.getDbName());
        }
        this.config = config;
    }


    private synchronized static DbUtils getInstance(DaoConfig daoConfig) throws DbException {
        DbUtils dao = daoMap.get(daoConfig.getDbName());
        if (dao == null) {
            dao = new DbUtils(daoConfig);
            daoMap.put(daoConfig.getDbName(), dao);
        } else {
            dao.config = daoConfig;
        }
        return dao;
    }

    public static DbUtils create(Context context) throws DbException {
        DaoConfig config = new DaoConfig(context);
        return getInstance(config);
    }

    public static DbUtils create(Context context, String dbName) throws DbException {
        DaoConfig config = new DaoConfig(context);
        config.setDbName(dbName);
        return getInstance(config);
    }

    public static DbUtils create(Context context, String sdCardPath, String dbName) throws DbException {
        DaoConfig config = new DaoConfig(context);
        config.setSdCardPath(sdCardPath);
        config.setDbName(dbName);
        return getInstance(config);
    }

    public static DbUtils create(Context context, String dbName, int dbVersion, DbUpgradeListener dbUpgradeListener) throws DbException {
        DaoConfig config = new DaoConfig(context);
        config.setDbName(dbName);
        config.setDbVersion(dbVersion);
        config.setDbUpgradeListener(dbUpgradeListener);
        return getInstance(config);
    }

    public static DbUtils create(DaoConfig daoConfig) throws DbException {
        return getInstance(daoConfig);
    }

    public DbUtils configDebug(boolean debug) {
        this.debug = debug;
        return this;
    }

    public DbUtils configAllowTransaction(boolean allowTransaction) {
        this.allowTransaction = allowTransaction;
        return this;
    }

    public SQLiteDatabase getDatabase() {
        return database;
    }

    //*********************************************** operations ********************************************************

    public void saveOrUpdate(Object entity) throws DbException {
        try {
            beginTransaction();

            saveOrUpdateWithoutTransaction(entity);

            setTransactionSuccessful();
        } finally {
            endTransaction();
        }
    }

    public void saveOrUpdate(List<Object> entities) throws DbException {
        try {
            beginTransaction();

            for (Object entity : entities) {
                saveOrUpdateWithoutTransaction(entity);
            }

            setTransactionSuccessful();
        } finally {
            endTransaction();
        }
    }

    public void replace(Object entity) throws DbException {
        try {
            beginTransaction();

            replaceWithoutTransaction(entity);

            setTransactionSuccessful();
        } finally {
            endTransaction();
        }
    }

    public void replace(List<Object> entities) throws DbException {
        try {
            beginTransaction();

            for (Object entity : entities) {
                replaceWithoutTransaction(entity);
            }

            setTransactionSuccessful();
        } finally {
            endTransaction();
        }
    }

    public void save(Object entity) throws DbException {
        try {
            beginTransaction();

            saveWithoutTransaction(entity);

            setTransactionSuccessful();
        } finally {
            endTransaction();
        }
    }

    public void save(List<Object> entities) throws DbException {
        try {
            beginTransaction();

            for (Object entity : entities) {
                saveWithoutTransaction(entity);
            }

            setTransactionSuccessful();
        } finally {
            endTransaction();
        }
    }

    public boolean saveBindingId(Object entity) throws DbException {
        boolean result = false;
        try {
            beginTransaction();

            result = saveBindingIdWithoutTransaction(entity);

            setTransactionSuccessful();
        } finally {
            endTransaction();
        }
        return result;
    }

    public void saveBindingId(List<Object> entities) throws DbException {
        try {
            beginTransaction();

            for (Object entity : entities) {
                if (!saveBindingIdWithoutTransaction(entity)) {
                    throw new DbException("saveBindingId error, transaction will not commit!");
                }
            }

            setTransactionSuccessful();
        } finally {
            endTransaction();
        }
    }


    public void delete(Object entity) throws DbException {
        try {
            beginTransaction();

            deleteWithoutTransaction(entity);

            setTransactionSuccessful();
        } finally {
            endTransaction();
        }
    }

    public void delete(List<Object> entities) throws DbException {
        try {
            beginTransaction();

            for (Object entity : entities) {
                deleteWithoutTransaction(entity);
            }

            setTransactionSuccessful();
        } finally {
            endTransaction();
        }
    }

    public void deleteById(Class<?> entityType, Object idValue) throws DbException {
        try {
            beginTransaction();

            execNonQuery(SqlInfoBuilder.buildDeleteSqlInfo(entityType, idValue));

            setTransactionSuccessful();
        } finally {
            endTransaction();
        }
    }

    public void delete(Class<?> entityType, WhereBuilder whereBuilder) throws DbException {
        try {
            beginTransaction();

            SqlInfo sql = SqlInfoBuilder.buildDeleteSqlInfo(entityType, whereBuilder);
            execNonQuery(sql);

            setTransactionSuccessful();
        } finally {
            endTransaction();
        }
    }

    public void update(Object entity) throws DbException {
        try {
            beginTransaction();

            updateWithoutTransaction(entity);

            setTransactionSuccessful();
        } finally {
            endTransaction();
        }
    }

    public void update(List<Object> entities) throws DbException {
        try {
            beginTransaction();

            for (Object entity : entities) {
                updateWithoutTransaction(entity);
            }

            setTransactionSuccessful();
        } finally {
            endTransaction();
        }
    }

    public void update(Object entity, WhereBuilder whereBuilder) throws DbException {
        try {
            beginTransaction();

            execNonQuery(SqlInfoBuilder.buildUpdateSqlInfo(this, entity, whereBuilder));

            setTransactionSuccessful();
        } finally {
            endTransaction();
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T findById(Class<T> entityType, Object idValue) throws DbException {
        Id id = Table.get(entityType).getId();
        Selector selector = Selector.from(entityType).where(WhereBuilder.b(id.getColumnName(), "=", idValue));
        Cursor cursor = execQuery(selector.limit(1).toString());
        try {
            if (cursor.moveToNext()) {
                return (T) CursorUtils.getEntity(this, cursor, selector.getEntityType());
            }
        } catch (Exception e) {
            throw new DbException(e);
        } finally {
            IOUtils.closeQuietly(cursor);
        }
        return null;
    }

    public <T> T findFirst(Object entity) throws DbException {
        Selector selector = Selector.from(entity.getClass());
        List<KeyValue> entityKvList = SqlInfoBuilder.entity2KeyValueList(this, entity);
        if (entityKvList != null) {
            WhereBuilder wb = WhereBuilder.b();
            for (KeyValue keyValue : entityKvList) {
                wb.append(keyValue.getKey(), "=", keyValue.getValue());
            }
            selector.where(wb);
        }
        return findFirst(selector);
    }

    public <T> List<T> findAll(Object entity) throws DbException {
        Selector selector = Selector.from(entity.getClass());
        List<KeyValue> entityKvList = SqlInfoBuilder.entity2KeyValueList(this, entity);
        if (entityKvList != null) {
            WhereBuilder wb = WhereBuilder.b();
            for (KeyValue keyValue : entityKvList) {
                wb.append(keyValue.getKey(), "=", keyValue.getValue());
            }
            selector.where(wb);
        }
        return findAll(selector);
    }

    @SuppressWarnings("unchecked")
    public <T> T findFirst(Selector selector) throws DbException {
        Cursor cursor = execQuery(selector.limit(1).toString());
        try {
            if (cursor.moveToNext()) {
                return (T) CursorUtils.getEntity(this, cursor, selector.getEntityType());
            }
        } catch (Exception e) {
            throw new DbException(e);
        } finally {
            IOUtils.closeQuietly(cursor);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> findAll(Selector selector) throws DbException {
        Cursor cursor = execQuery(selector.toString());
        List<T> result = new ArrayList<T>();
        try {
            while (cursor.moveToNext()) {
                result.add((T) CursorUtils.getEntity(this, cursor, selector.getEntityType()));
            }
        } catch (Exception e) {
            throw new DbException(e);
        } finally {
            IOUtils.closeQuietly(cursor);
        }
        return result;
    }

    public DbModel findDbModelFirst(String sql) throws DbException {
        Cursor cursor = execQuery(sql);
        try {
            if (cursor.moveToNext()) {
                return CursorUtils.getDbModel(cursor);
            }
        } catch (Exception e) {
            throw new DbException(e);
        } finally {
            IOUtils.closeQuietly(cursor);
        }
        return null;
    }

    public DbModel findDbModelFirst(DbModelSelector selector) throws DbException {
        Cursor cursor = execQuery(selector.limit(1).toString());
        try {
            if (cursor.moveToNext()) {
                return CursorUtils.getDbModel(cursor);
            }
        } catch (Exception e) {
            throw new DbException(e);
        } finally {
            IOUtils.closeQuietly(cursor);
        }
        return null;
    }

    public List<DbModel> findDbModelAll(String sql) throws DbException {
        Cursor cursor = execQuery(sql);
        List<DbModel> dbModelList = new ArrayList<DbModel>();
        try {
            while (cursor.moveToNext()) {
                dbModelList.add(CursorUtils.getDbModel(cursor));
            }
        } catch (Exception e) {
            throw new DbException(e);
        } finally {
            IOUtils.closeQuietly(cursor);
        }
        return dbModelList;
    }

    public List<DbModel> findDbModelAll(DbModelSelector selector) throws DbException {
        Cursor cursor = execQuery(selector.toString());
        List<DbModel> dbModelList = new ArrayList<DbModel>();
        try {
            while (cursor.moveToNext()) {
                dbModelList.add(CursorUtils.getDbModel(cursor));
            }
        } catch (Exception e) {
            throw new DbException(e);
        } finally {
            IOUtils.closeQuietly(cursor);
        }
        return dbModelList;
    }

    //******************************************** config ******************************************************

    public static class DaoConfig {
        private Context context;
        private String dbName = "xUtils.db"; // default db name
        private int dbVersion = 1;
        private DbUpgradeListener dbUpgradeListener;

        private String sdCardPath;

        public DaoConfig(Context context) {
            this.context = context;
        }

        public Context getContext() {
            return context;
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

        public DbUpgradeListener getDbUpgradeListener() {
            return dbUpgradeListener;
        }

        public void setDbUpgradeListener(DbUpgradeListener dbUpgradeListener) {
            this.dbUpgradeListener = dbUpgradeListener;
        }

        public String getSdCardPath() {
            return sdCardPath;
        }

        public void setSdCardPath(String sdCardPath) {
            this.sdCardPath = sdCardPath;
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
                try {
                    dropDb();
                } catch (DbException e) {
                    LogUtils.e(e.getMessage(), e);
                }
            }
        }
    }

    private SQLiteDatabase createDbFileOnSDCard(String sdCardPath, String dbName) throws DbException {
        File dbFile = new File(sdCardPath, dbName);
        if (!dbFile.exists()) {
            try {
                if (dbFile.createNewFile()) {
                    return SQLiteDatabase.openOrCreateDatabase(dbFile, null);
                }
            } catch (IOException e) {
                throw new DbException("create db error", e);
            }
        } else {
            return SQLiteDatabase.openOrCreateDatabase(dbFile, null);
        }

        return null;
    }

    //***************************** private operations with out transaction *****************************
    private void saveOrUpdateWithoutTransaction(Object entity) throws DbException {
        if (TableUtils.getIdValue(entity) != null) {
            update(entity);
        } else {
            saveBindingId(entity);
        }
    }

    private void replaceWithoutTransaction(Object entity) throws DbException {
        createTableIfNotExist(entity.getClass());
        execNonQuery(SqlInfoBuilder.buildReplaceSqlInfo(this, entity));
    }

    private void saveWithoutTransaction(Object entity) throws DbException {
        createTableIfNotExist(entity.getClass());
        execNonQuery(SqlInfoBuilder.buildInsertSqlInfo(this, entity));
    }

    private boolean saveBindingIdWithoutTransaction(Object entity) throws DbException {
        createTableIfNotExist(entity.getClass());
        List<KeyValue> entityKvList = SqlInfoBuilder.entity2KeyValueList(this, entity);
        if (entityKvList != null && entityKvList.size() > 0) {
            Table table = Table.get(entity.getClass());
            ContentValues cv = new ContentValues();
            DbUtils.fillContentValues(cv, entityKvList);
            Long id = database.insert(table.getTableName(), null, cv);
            if (id == -1) {
                return false;
            }
            table.getId().setValue2Entity(entity, id.toString());
            return true;
        }
        return false;
    }

    private void deleteWithoutTransaction(Object entity) throws DbException {
        execNonQuery(SqlInfoBuilder.buildDeleteSqlInfo(entity));
    }

    private void updateWithoutTransaction(Object entity) throws DbException {
        execNonQuery(SqlInfoBuilder.buildUpdateSqlInfo(this, entity));
    }

    //************************************************ tools ***********************************

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
        if (!tableIsExist(entityType)) {
            SqlInfo sqlInfo = SqlInfoBuilder.buildCreateTableSqlInfo(entityType);
            execNonQuery(sqlInfo);
        }
    }

    public boolean tableIsExist(Class<?> entityType) throws DbException {
        Table table = Table.get(entityType);
        if (table.isCheckDatabase()) {
            return true;
        }

        Cursor cursor = null;
        try {
            cursor = execQuery("SELECT COUNT(*) AS c FROM sqlite_master WHERE type ='table' AND name ='" + table.getTableName() + "'");
            if (cursor != null && cursor.moveToNext()) {
                int count = cursor.getInt(0);
                if (count > 0) {
                    table.setCheckDatabase(true);
                    return true;
                }
            }

        } catch (Exception e) {
            throw new DbException(e);
        } finally {
            IOUtils.closeQuietly(cursor);
        }

        return false;
    }

    public void dropDb() throws DbException {
        Cursor cursor = null;
        try {
            cursor = execQuery("SELECT name FROM sqlite_master WHERE type ='table'");
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    try {
                        execNonQuery("DROP TABLE " + cursor.getString(0));
                    } catch (Exception e) {
                        LogUtils.e(e.getMessage(), e);
                    }
                }
            }
        } catch (Exception e) {
            throw new DbException(e);
        } finally {
            IOUtils.closeQuietly(cursor);
        }
    }

    public void dropTable(Class<?> entityType) throws DbException {
        try {
            Table table = Table.get(entityType);
            execNonQuery("DROP TABLE " + table.getTableName());
        } catch (Exception e) {
            throw new DbException(e);
        }
    }

    ///////////////////////////////////// exec sql /////////////////////////////////////////////////////
    private void debugSql(String sql) {
        if (config != null && debug) {
            LogUtils.d(sql);
        }
    }

    private void beginTransaction() {
        if (allowTransaction) {
            database.beginTransaction();
        }
    }

    private void setTransactionSuccessful() {
        if (allowTransaction) {
            database.setTransactionSuccessful();
        }
    }

    private void endTransaction() {
        if (allowTransaction) {
            database.endTransaction();
        }
    }


    public void execNonQuery(SqlInfo sqlInfo) throws DbException {
        debugSql(sqlInfo.getSql());
        try {
            if (sqlInfo.getBindingArgs() != null) {
                database.execSQL(sqlInfo.getSql(), sqlInfo.getBindingArgsAsArray());
            } else {
                database.execSQL(sqlInfo.getSql());
            }
        } catch (Exception e) {
            throw new DbException(e);
        }
    }

    public void execNonQuery(String sql) throws DbException {
        debugSql(sql);
        try {
            database.execSQL(sql);
        } catch (Exception e) {
            throw new DbException(e);
        }
    }

    public Cursor execQuery(SqlInfo sqlInfo) throws DbException {
        debugSql(sqlInfo.getSql());
        try {
            return database.rawQuery(sqlInfo.getSql(), sqlInfo.getBindingArgsAsStringArray());
        } catch (Exception e) {
            throw new DbException(e);
        }
    }

    public Cursor execQuery(String sql) throws DbException {
        debugSql(sql);
        try {
            return database.rawQuery(sql, null);
        } catch (Exception e) {
            throw new DbException(e);
        }
    }

}
