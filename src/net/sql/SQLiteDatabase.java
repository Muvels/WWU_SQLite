package net.sql;

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
     * Closes the database and releases associated resources.
     */
    @Override
    public void close() {
        nativeSql.closeDatabase(dbPtr);
    }
}
