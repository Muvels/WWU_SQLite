package net.sql;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

public class SQLiteNative {
    static {
    	try {
            loadNativeLibrary();
        } catch (IOException e) {
            // If you prefer to handle this differently, you can rethrow as a RuntimeException or log an error.
            throw new RuntimeException("Failed to load native library", e);
        }
    }

    /**
     * Detects the current platform, extracts the corresponding native library from the JAR,
     * and loads it into the JVM.
     *
     * @throws IOException if extraction or loading fails
     */
    private static void loadNativeLibrary() throws IOException {
        // Determine OS name in lowercase for easier matching.
        String libraryResourcePath = NativeLibraryLoader.getLibraryResourcePath();
        URL resourceUrl = SQLiteNative.class.getResource(libraryResourcePath);
        System.out.println("Resource URL: " + resourceUrl);

        // Extract the library from the JAR
        InputStream in = SQLiteNative.class.getResourceAsStream(libraryResourcePath);
        if (in == null) {
            throw new FileNotFoundException("Native library not found in JAR: " + libraryResourcePath);
        }

        // Create a temporary file to store the native library
        String filename = new File(libraryResourcePath).getName();
        String prefix = filename.substring(0, filename.lastIndexOf('.'));
        String suffix = filename.substring(filename.lastIndexOf('.'));
        File tempFile = File.createTempFile(prefix, suffix);
        tempFile.deleteOnExit();

        try (FileOutputStream out = new FileOutputStream(tempFile)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        } finally {
            in.close();
        }

        // Load the native library
        System.load(tempFile.getAbsolutePath());
    }

    /**
     * Opens the SQLite Database so that SQL queries can be executed on it.
     *
     * @param dbPath the file path of the SQLite database to open.
     * @return a native handle to the opened database, or 0 if the database could not be opened.
     *         The returned long value represents a pointer to the native sqlite3 structure.
     */
    public native long openDatabase(String dbPath);

    /**
     * Closes the SQLite Database.
     *
     * @param dbPtr a native handle (pointer) to the SQLite database to close.
     */
    public native void closeDatabase(long dbPtr);

    /**
     * Executes the specified SQL query on the given database.
     *
     * @param dbPtr a native handle to the SQLite database.
     * @param query the SQL query to execute.
     * @return the result code of the query execution (e.g., SQLITE_OK on success).
     */
    public native int execute(long dbPtr, String query);

    /**
     * Executes a SQL query on the given database and returns the result as a JSON string.
     * <p>
     * If the {@code options} parameter is 0 and the query returns a single column, the raw
     * string result is returned. Otherwise, the result is formatted as a JSON array of objects.
     * </p>
     *
     * @param dbPtr   a native handle to the SQLite database.
     * @param query   the SQL query to execute.
     * @param options query options that influence the result format 0 : 'No format' | 1: 'JSON format'.
     * @return a JSON formatted string representing the query result, or an error message if the query fails.
     */
    public native String queryDatabase(long dbPtr, String query, int options);

    /**
     * Executes an SQL query with the provided parameters and maps the result set to a list of objects
     * of the specified class type. The method dynamically matches database column names to Java class
     * properties based on a naming convention.
     *
     * @param dbPtr        A pointer to the SQLite database instance.
     * @param query        The SQL query string to be executed.
     * @param param        An optional parameter for the query (currently unused, can be null).
     * @param templateClass The Java class type to which the query results should be mapped.
     *                      The class must have setter methods following the naming convention:
     *                      {@code set<PropertyName>} for each matching database column.
     * @param <T>          The generic type representing the class type of the objects in the result list.
     * @return An {@code ArrayList<T>} containing instances of {@code templateClass}, populated with
     *         values from the database query result. Returns an empty list if no results are found
     *         or {@code null} if an error occurs.
     */
    public native <T> ArrayList<T> executeQueryWithMapping(long dbPtr, String query, Object param, Class<T> templateClass);


    /**
     * Prepares and starts executing the specified SQL query.
     * <p>
     * This method compiles the SQL statement and stores it globally so that subsequent calls to
     * {@link #fetchNextRow()} and the column accessor methods operate on its result set.
     * </p>
     * <p>
     * <strong>Note:</strong> If you intend to exit the result loop before processing all rows,
     * you must call {@link #cancelQuery()} to properly finalize the active query and release the allocated resources.
     * </p>
     *
     * @param dbPtr a native handle to the SQLite database.
     * @param query the SQL query to prepare.
     * @return 0 if the query was successfully prepared, or -1 if an error occurred.
     */
    public native int startQuery(long dbPtr, String query);

    /**
     * Advances to the next row in the currently active query result set.
     *
     * @return 1 if a new row is available, 0 if there are no more rows, or -1 if no query has been started.
     */
    public native int fetchNextRow();

    /**
     * Cancels the currently active query.
     * <p>
     * This method finalizes the active SQLite statement (if any), releasing the resources
     * associated with it. It should be used to abort a query that is no longer needed before
     * all rows have been processed.
     * </p>
     */
    public native void cancelQuery();

    /**
     * Retrieves the value of the specified column from the current row as a string.
     *
     * @param columnName the name of the column whose value is to be retrieved.
     * @return the string representation of the column value, or an empty string if the column is not found
     *         or if no query is active.
     */
    public native String getColumnString(String columnName);

    /**
     * Retrieves the value of the specified column from the current row as an integer.
     *
     * @param columnName the name of the column whose value is to be retrieved.
     * @return the integer value of the column, or 0 if the column is not found or if no query is active.
     */
    public native int getColumnInt(String columnName);

    /**
     * Retrieves the value of the specified column from the current row as a double.
     *
     * @param columnName the name of the column whose value is to be retrieved.
     * @return the double value of the column, or 0.0 if the column is not found or if no query is active.
     */
    public native double getColumnDouble(String columnName);

    /**
     * Retrieves the value of the specified column from the current row as a long.
     *
     * @param columnName the name of the column whose value is to be retrieved.
     * @return the long value of the column, or 0 if the column is not found or if no query is active.
     */
    public native long getColumnLong(String columnName);

    /**
     * Retrieves the value of the specified column from the current row as a byte array (BLOB).
     *
     * @param columnName the name of the column whose value is to be retrieved.
     * @return a byte array containing the blob data, or an empty array if the column is not found
     *         or if no query is active.
     */
    public native byte[] getColumnBlob(String columnName);

    /**
     * Retrieves the SQLite type of the specified column from the current row.
     * <p>
     * The returned value will be one of the following SQLite type codes:
     * <ul>
     *   <li>SQLITE_INTEGER</li>
     *   <li>SQLITE_FLOAT</li>
     *   <li>SQLITE_TEXT</li>
     *   <li>SQLITE_BLOB</li>
     *   <li>SQLITE_NULL</li>
     * </ul>
     * </p>
     *
     * @param columnName the name of the column whose type is to be retrieved.
     * @return an integer representing the SQLite type.
     */
    public native int getColumnType(String columnName);

}

