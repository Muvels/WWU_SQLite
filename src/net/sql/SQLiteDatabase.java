package net.sql;

import java.util.ArrayList;

/**
 * A Java wrapper for the native SQLite interface.
 */
public class SQLiteDatabase implements AutoCloseable {
    private final SQLiteNative nativeSql;
    private final long dbPtr;

    /**
     * Opens the specified database.
     *
     * @param dbPath the path to the SQLite database file.
     * @throws RuntimeException if the database cannot be opened.
     */
    public SQLiteDatabase(String dbPath) {
        nativeSql = new SQLiteNative();
        dbPtr = nativeSql.openDatabase(dbPath);
        if (dbPtr == 0) {
            throw new RuntimeException("Failed to open database: " + dbPath);
        }
    }

    /**
     * Executes a SQL statement that does not return a result set (e.g., DDL or INSERT).
     *
     * @param sql the SQL statement to execute.
     * @throws RuntimeException if execution fails.
     */
    public void execute(String sql) {
        int result = nativeSql.execute(dbPtr, sql);
        if (result != 0) {
            throw new RuntimeException("Failed to execute SQL: " + sql);
        }
    }

    /**
     * Prepares a SQL query and returns a {@link SQLiteStatement} to iterate its results.
     *
     * @param sql the SQL query to prepare.
     * @return a {@link SQLiteStatement} that you can use to fetch rows.
     * @throws RuntimeException if the query cannot be started.
     */
    public SQLiteStatement prepareStatement(String sql) {
        int result = nativeSql.startQuery(dbPtr, sql);
        if (result != 0) {
            throw new RuntimeException("Failed to start query: " + sql);
        }
        return new SQLiteStatement(nativeSql);
    }

    /**
     * Executes a SQL query and maps the result set to a list of objects of the specified type.
     * This method uses the native mapping functionality which converts each row into an instance
     * of the provided template class, using setters for the appropriate types (int, double, long,
     * byte[] and String).
     *
     * @param sql           the SQL query to execute.
     * @param param         an optional parameter for the query (can be null).
     * @param templateClass the class of the object to map each row to.
     * @param <T>           the type of the object.
     * @return an {@link ArrayList} of objects of type T containing the mapped results.
     * @throws RuntimeException if the query mapping fails.
     */
    public <T> ArrayList<T> executeQueryWithMapping(String sql, Object param, Class<T> templateClass) {
        ArrayList<T> results = nativeSql.executeQueryWithMapping(dbPtr, sql, param, templateClass);
        if (results == null) {
            throw new RuntimeException("Failed to execute query with mapping: " + sql);
        }
        return results;
    }

    /**
     * Closes the database and releases associated resources.
     */
    @Override
    public void close() {
        nativeSql.closeDatabase(dbPtr);
    }
}
