package net.sql;

/**
 * A Java wrapper for an active SQLite query.
 *
 * This class encapsulates the lifecycle of a query:
 *  - Calling {@link #next()} to advance to the next row.
 *  - Using accessor methods to retrieve column values.
 *  - Automatically canceling (finalizing) the query when closed.
 */
public class SQLiteStatement implements AutoCloseable {
    private final SQLiteNative nativeSql;
    private boolean active = true;

    /**
     * Package-private constructor. Use {@link SQLiteDatabase#prepareStatement(String)} to obtain an instance.
     *
     * @param nativeSql the underlying native SQLite interface.
     */
    SQLiteStatement(SQLiteNative nativeSql) {
        this.nativeSql = nativeSql;
    }

    /**
     * Advances to the next row in the result set.
     *
     * @return {@code true} if a new row is available, {@code false} if there are no more rows.
     * @throws IllegalStateException if the statement is no longer active.
     */
    public boolean next() {
        if (!active) {
            throw new IllegalStateException("Query is no longer active.");
        }
        int result = nativeSql.fetchNextRow();
        if (result == 1) {
            return true;
        } else {
            // When result is 0 (or error), finalize the query.
            close();
            return false;
        }
    }

    /**
     * Returns the value of the specified column as an integer.
     *
     * @param columnName the column name.
     * @return the integer value.
     */
    public int getInt(String columnName) {
        return nativeSql.getColumnInt(columnName);
    }

    /**
     * Returns the value of the specified column as a String.
     *
     * @param columnName the column name.
     * @return the String value.
     */
    public String getString(String columnName) {
        return nativeSql.getColumnString(columnName);
    }

    /**
     * Returns the value of the specified column as a double.
     *
     * @param columnName the column name.
     * @return the double value.
     */
    public double getDouble(String columnName) {
        return nativeSql.getColumnDouble(columnName);
    }

    /**
     * Returns the value of the specified column as a long.
     *
     * @param columnName the column name.
     * @return the long value.
     */
    public long getLong(String columnName) {
        return nativeSql.getColumnLong(columnName);
    }

    /**
     * Returns the value of the specified column as a byte array (BLOB).
     *
     * @param columnName the column name.
     * @return a byte array containing the blob data, or an empty array if not found.
     */
    public byte[] getBlob(String columnName) {
        return nativeSql.getColumnBlob(columnName);
    }

    /**
     * Returns the SQLite type code for the specified column.
     * <p>
     * The returned type is one of:
     * <ul>
     *   <li>SQLITE_INTEGER</li>
     *   <li>SQLITE_FLOAT</li>
     *   <li>SQLITE_TEXT</li>
     *   <li>SQLITE_BLOB</li>
     *   <li>SQLITE_NULL</li>
     * </ul>
     * </p>
     *
     * @param columnName the column name.
     * @return the SQLite type code.
     */
    public int getType(String columnName) {
        return nativeSql.getColumnType(columnName);
    }

    /**
     * Cancels the query and finalizes any associated resources.
     */
    @Override
    public void close() {
        if (active) {
            nativeSql.cancelQuery();
            active = false;
        }
    }
}
