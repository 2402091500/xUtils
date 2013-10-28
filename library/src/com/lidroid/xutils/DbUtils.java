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
import com.lidroid.xutils.db.sqlite.*;
import com.lidroid.xutils.db.table.*;
import com.lidroid.xutils.exception.DbException;
import com.lidroid.xutils.util.IOUtils;
import com.lidroid.xutils.util.LogUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

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

    private DbUtils(DaoConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("daoConfig may not be null");
        }

        if (config.getContext() == null) {
            throw new IllegalArgumentException("context mey not be null");
        }

        String sdCardPath = config.getSdCardPath();
        if (TextUtils.isEmpty(sdCardPath)) {
            this.database = new SQLiteDbHelper(config).getWritableDatabase();
        } else {
            this.database = createDbFileOnSDCard(config);
        }
        this.config = config;
    }


    private synchronized static DbUtils getInstance(DaoConfig daoConfig) {
        DbUtils dao = daoMap.get(daoConfig.getDbName());
        if (dao == null) {
            dao = new DbUtils(daoConfig);
            daoMap.put(daoConfig.getDbName(), dao);
        } else {
            dao.config = daoConfig;
        }
        return dao;
    }

    public static DbUtils create(Context context) {
        DaoConfig config = new DaoConfig(context);
        return getInstance(config);
    }

    public static DbUtils create(Context context, String dbName) {
        DaoConfig config = new DaoConfig(context);
        config.setDbName(dbName);
        return getInstance(config);
    }

    public static DbUtils create(Context context, String sdCardPath, String dbName) {
        DaoConfig config = new DaoConfig(context);
        config.setSdCardPath(sdCardPath);
        config.setDbName(dbName);
        return getInstance(config);
    }

    public static DbUtils create(Context context, String dbName, int dbVersion, DbUpgradeListener dbUpgradeListener) {
        DaoConfig config = new DaoConfig(context);
        config.setDbName(dbName);
        config.setDbVersion(dbVersion);
        config.setDbUpgradeListener(dbUpgradeListener);
        return getInstance(config);
    }

    public static DbUtils create(Context context, String sdCardPath, String dbName, int dbVersion, DbUpgradeListener dbUpgradeListener) {
        DaoConfig config = new DaoConfig(context);
        config.setSdCardPath(sdCardPath);
        config.setDbName(dbName);
        config.setDbVersion(dbVersion);
        config.setDbUpgradeListener(dbUpgradeListener);
        return getInstance(config);
    }

    public static DbUtils create(DaoConfig daoConfig) {
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

    public void saveOrUpdateAll(List<?> entities) throws DbException {
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

    public void replaceAll(List<?> entities) throws DbException {
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

    public void saveAll(List<?> entities) throws DbException {
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

    public void saveBindingIdAll(List<?> entities) throws DbException {
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
        if (!tableIsExist(entity.getClass())) return;
        try {
            beginTransaction();

            deleteWithoutTransaction(entity);

            setTransactionSuccessful();
        } finally {
            endTransaction();
        }
    }

    public void deleteAll(List<?> entities) throws DbException {
        if (entities == null || entities.size() < 1 || !tableIsExist(entities.get(0).getClass())) return;
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

    public void deleteAll(Class<?> entityType) throws DbException {
        if (!tableIsExist(entityType)) return;
        try {
            beginTransaction();

            SqlInfo sql = SqlInfoBuilder.buildDeleteSqlInfo(entityType, null);
            execNonQuery(sql);

            setTransactionSuccessful();
        } finally {
            endTransaction();
        }
    }

    public void deleteById(Class<?> entityType, Object idValue) throws DbException {
        if (!tableIsExist(entityType)) return;
        try {
            beginTransaction();

            execNonQuery(SqlInfoBuilder.buildDeleteSqlInfo(entityType, idValue));

            setTransactionSuccessful();
        } finally {
            endTransaction();
        }
    }

    public void delete(Class<?> entityType, WhereBuilder whereBuilder) throws DbException {
        if (!tableIsExist(entityType)) return;
        try {
            beginTransaction();

            SqlInfo sql = SqlInfoBuilder.buildDeleteSqlInfo(entityType, whereBuilder);
            execNonQuery(sql);

            setTransactionSuccessful();
        } finally {
            endTransaction();
        }
    }

    /**
     * @param entity
     * @param updateColumnNames if null, update all columns.
     * @throws DbException
     */
    public void update(Object entity, String... updateColumnNames) throws DbException {
        if (!tableIsExist(entity.getClass())) return;
        try {
            beginTransaction();

            updateWithoutTransaction(entity, updateColumnNames);

            setTransactionSuccessful();
        } finally {
            endTransaction();
        }
    }

    /**
     * @param entities
     * @param updateColumnNames if null, update all columns.
     * @throws DbException
     */
    public void updateAll(List<?> entities, String... updateColumnNames) throws DbException {
        if (entities == null || entities.size() < 1 || !tableIsExist(entities.get(0).getClass())) return;
        try {
            beginTransaction();

            for (Object entity : entities) {
                updateWithoutTransaction(entity, updateColumnNames);
            }

            setTransactionSuccessful();
        } finally {
            endTransaction();
        }
    }

    /**
     * @param entity
     * @param whereBuilder
     * @param updateColumnNames if null, update all columns.
     * @throws DbException
     */
    public void update(Object entity, WhereBuilder whereBuilder, String... updateColumnNames) throws DbException {
        if (!tableIsExist(entity.getClass())) return;
        try {
            beginTransaction();

            execNonQuery(SqlInfoBuilder.buildUpdateSqlInfo(this, entity, whereBuilder, updateColumnNames));

            setTransactionSuccessful();
        } finally {
            endTransaction();
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T findById(Class<T> entityType, Object idValue) throws DbException {
        if (!tableIsExist(entityType)) return null;

        Id id = Table.get(entityType).getId();
        Selector selector = Selector.from(entityType).where(id.getColumnName(), "=", idValue);

        String sql = selector.limit(1).toString();
        long seq = CursorUtils.FindCacheSequence.getSeq();
        findTempCache.setSeq(seq);
        Object obj = findTempCache.get(sql);
        if (obj != null) {
            return (T) obj;
        }

        Cursor cursor = execQuery(sql);
        try {
            if (cursor.moveToNext()) {
                T entity = (T) CursorUtils.getEntity(this, cursor, entityType, seq);
                findTempCache.put(sql, entity);
                return entity;
            }
        } finally {
            IOUtils.closeQuietly(cursor);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public <T> T findFirst(Selector selector) throws DbException {
        if (!tableIsExist(selector.getEntityType())) return null;

        String sql = selector.limit(1).toString();
        long seq = CursorUtils.FindCacheSequence.getSeq();
        findTempCache.setSeq(seq);
        Object obj = findTempCache.get(sql);
        if (obj != null) {
            return (T) obj;
        }

        Cursor cursor = execQuery(sql);
        try {
            if (cursor.moveToNext()) {
                T entity = (T) CursorUtils.getEntity(this, cursor, selector.getEntityType(), seq);
                findTempCache.put(sql, entity);
                return entity;
            }
        } finally {
            IOUtils.closeQuietly(cursor);
        }
        return null;
    }

    public <T> T findFirst(Object entity) throws DbException {
        if (!tableIsExist(entity.getClass())) return null;
        Selector selector = Selector.from(entity.getClass());
        List<KeyValue> entityKvList = SqlInfoBuilder.entity2KeyValueList(this, entity);
        if (entityKvList != null) {
            WhereBuilder wb = WhereBuilder.b();
            for (KeyValue keyValue : entityKvList) {
                Object value = keyValue.getValue();
                if (value != null) {
                    wb.and(keyValue.getKey(), "=", value);
                }
            }
            selector.where(wb);
        }
        return findFirst(selector);
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> findAll(Selector selector) throws DbException {
        if (!tableIsExist(selector.getEntityType())) return null;

        String sql = selector.toString();
        long seq = CursorUtils.FindCacheSequence.getSeq();
        findTempCache.setSeq(seq);
        Object obj = findTempCache.get(sql);
        if (obj != null) {
            return (List<T>) obj;
        }

        Cursor cursor = execQuery(sql);
        List<T> result = new ArrayList<T>();
        try {
            while (cursor.moveToNext()) {
                T entity = (T) CursorUtils.getEntity(this, cursor, selector.getEntityType(), seq);
                result.add(entity);
            }
            findTempCache.put(sql, result);
        } finally {
            IOUtils.closeQuietly(cursor);
        }
        return result;
    }

    public <T> List<T> findAll(Object entity) throws DbException {
        if (!tableIsExist(entity.getClass())) return null;
        Selector selector = Selector.from(entity.getClass());
        List<KeyValue> entityKvList = SqlInfoBuilder.entity2KeyValueList(this, entity);
        if (entityKvList != null) {
            WhereBuilder wb = WhereBuilder.b();
            for (KeyValue keyValue : entityKvList) {
                Object value = keyValue.getValue();
                if (value != null) {
                    wb.and(keyValue.getKey(), "=", value);
                }
            }
            selector.where(wb);
        }
        return findAll(selector);
    }

    public DbModel findDbModelFirst(SqlInfo sqlInfo) throws DbException {
        Cursor cursor = execQuery(sqlInfo);
        try {
            if (cursor.moveToNext()) {
                return CursorUtils.getDbModel(cursor);
            }
        } finally {
            IOUtils.closeQuietly(cursor);
        }
        return null;
    }

    public DbModel findDbModelFirst(DbModelSelector selector) throws DbException {
        if (!tableIsExist(selector.getEntityType())) return null;
        Cursor cursor = execQuery(selector.limit(1).toString());
        try {
            if (cursor.moveToNext()) {
                return CursorUtils.getDbModel(cursor);
            }
        } finally {
            IOUtils.closeQuietly(cursor);
        }
        return null;
    }

    public List<DbModel> findDbModelAll(SqlInfo sqlInfo) throws DbException {
        Cursor cursor = execQuery(sqlInfo);
        List<DbModel> dbModelList = new ArrayList<DbModel>();
        try {
            while (cursor.moveToNext()) {
                dbModelList.add(CursorUtils.getDbModel(cursor));
            }
        } finally {
            IOUtils.closeQuietly(cursor);
        }
        return dbModelList;
    }

    public List<DbModel> findDbModelAll(DbModelSelector selector) throws DbException {
        if (!tableIsExist(selector.getEntityType())) return null;
        Cursor cursor = execQuery(selector.toString());
        List<DbModel> dbModelList = new ArrayList<DbModel>();
        try {
            while (cursor.moveToNext()) {
                dbModelList.add(CursorUtils.getDbModel(cursor));
            }
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

    private class SQLiteDbHelper extends SQLiteOpenHelper {

        private DbUpgradeListener mDbUpgradeListener;

        public SQLiteDbHelper(DaoConfig config) {
            super(config.getContext(), config.getDbName(), null, config.getDbVersion());
            this.mDbUpgradeListener = config.getDbUpgradeListener();
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

    private SQLiteDatabase createDbFileOnSDCard(DaoConfig config) {
        SQLiteDatabase result = null;

        File dbFile = new File(config.getSdCardPath(), config.getDbName());
        boolean dbFileExists = dbFile.exists();
        result = SQLiteDatabase.openOrCreateDatabase(dbFile, null);

        if (result != null) {
            int oldVersion = result.getVersion();
            int newVersion = config.getDbVersion();
            if (oldVersion != newVersion) {
                if (dbFileExists && config.getDbUpgradeListener() != null) {
                    config.getDbUpgradeListener().onUpgrade(result, oldVersion, newVersion);
                }
                result.setVersion(newVersion);
            }
        }

        return result;
    }

    //***************************** private operations with out transaction *****************************
    private void saveOrUpdateWithoutTransaction(Object entity) throws DbException {
        Id id = TableUtils.getId(entity.getClass());
        if (id.isAutoIncrement()) {
            if (TableUtils.getIdValue(entity) != null) {
                updateWithoutTransaction(entity);
            } else {
                saveBindingIdWithoutTransaction(entity);
            }
        } else {
            replaceWithoutTransaction(entity);
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
        Table table = Table.get(entity.getClass());
        Id idColumn = table.getId();
        if (idColumn.isAutoIncrement()) {
            List<KeyValue> entityKvList = SqlInfoBuilder.entity2KeyValueList(this, entity);
            if (entityKvList != null && entityKvList.size() > 0) {
                ContentValues cv = new ContentValues();
                DbUtils.fillContentValues(cv, entityKvList);
                Long id = database.insert(table.getTableName(), null, cv);
                if (id == -1) {
                    return false;
                }
                idColumn.setValue2Entity(entity, id.toString());
                return true;
            }
        } else {
            execNonQuery(SqlInfoBuilder.buildInsertSqlInfo(this, entity));
            return true;
        }
        return false;
    }

    private void deleteWithoutTransaction(Object entity) throws DbException {
        execNonQuery(SqlInfoBuilder.buildDeleteSqlInfo(entity));
    }

    private void updateWithoutTransaction(Object entity, String... updateColumnNames) throws DbException {
        execNonQuery(SqlInfoBuilder.buildUpdateSqlInfo(this, entity, updateColumnNames));
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
                        String tableName = cursor.getString(0);
                        execNonQuery("DROP TABLE " + tableName);
                        Table.remove(tableName);
                    } catch (Throwable e) {
                        LogUtils.e(e.getMessage(), e);
                    }
                }
            }
        } finally {
            IOUtils.closeQuietly(cursor);
        }
    }

    public void dropTable(Class<?> entityType) throws DbException {
        if (!tableIsExist(entityType)) return;
        Table table = Table.get(entityType);
        execNonQuery("DROP TABLE " + table.getTableName());
        Table.remove(entityType);
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
            if (sqlInfo.getBindArgs() != null) {
                database.execSQL(sqlInfo.getSql(), sqlInfo.getBindArgsAsArray());
            } else {
                database.execSQL(sqlInfo.getSql());
            }
        } catch (Throwable e) {
            throw new DbException(e);
        }
    }

    public void execNonQuery(String sql) throws DbException {
        debugSql(sql);
        try {
            database.execSQL(sql);
        } catch (Throwable e) {
            throw new DbException(e);
        }
    }

    public Cursor execQuery(SqlInfo sqlInfo) throws DbException {
        debugSql(sqlInfo.getSql());
        try {
            return database.rawQuery(sqlInfo.getSql(), sqlInfo.getBindArgsAsStrArray());
        } catch (Throwable e) {
            throw new DbException(e);
        }
    }

    public Cursor execQuery(String sql) throws DbException {
        debugSql(sql);
        try {
            return database.rawQuery(sql, null);
        } catch (Throwable e) {
            throw new DbException(e);
        }
    }

    /////////////////////// temp cache ////////////////////////////////////////////////////////////////
    private final FindTempCache findTempCache = new FindTempCache();

    private class FindTempCache {
        private FindTempCache() {
        }

        /**
         * key: sql;
         * value: find result
         */
        private final ConcurrentHashMap<String, Object> cache = new ConcurrentHashMap<String, Object>();

        private long seq = 0;

        public void put(String sql, Object result) {
            cache.put(sql, result);
        }

        public Object get(String sql) {
            return cache.get(sql);
        }

        public void setSeq(long seq) {
            if (this.seq != seq) {
                cache.clear();
                this.seq = seq;
            }
        }
    }

}
