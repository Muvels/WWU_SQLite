#include <jni.h>
#include <stdio.h>
#include <sqlite3.h>
#include <string.h>

// Global statement pointer to track active query results
sqlite3_stmt *global_stmt = NULL;

// ========== <Helper functions> ==========
int jsonCallback(void *data, int argc, char **argv, char **colNames) {
    strcat((char *)data, "{");
    for (int i = 0; i < argc; i++) {
        char buffer[512];
        snprintf(buffer, sizeof(buffer), "\"%s\":\"%s\"", colNames[i], argv[i] ? argv[i] : "NULL");
        strcat((char *)data, buffer);
        if (i < argc - 1) strcat((char *)data, ",");
    }
    strcat((char *)data, "},");
    return 0;
}
// ========== </Helper functions> ==========
// ========== <Database io functions> ==========
JNIEXPORT jlong JNICALL Java_net_sql_SQLiteNative_openDatabase(JNIEnv *env, jobject obj, jstring dbPath) {
    const char *nativeDbPath = (*env)->GetStringUTFChars(env, dbPath, 0);
    sqlite3 *db;
    if (sqlite3_open(nativeDbPath, &db) != SQLITE_OK) {
        return 0; // Error
    }
    (*env)->ReleaseStringUTFChars(env, dbPath, nativeDbPath);
    return (jlong)db;
}

JNIEXPORT void JNICALL Java_net_sql_SQLiteNative_closeDatabase(JNIEnv *env, jobject obj, jlong dbPtr) {
    sqlite3_close((sqlite3 *)dbPtr);
}
// ========== </Database io functions> ==========
// ========== <Data Definition Language function> ==========
JNIEXPORT jint JNICALL Java_net_sql_SQLiteNative_execute(JNIEnv *env, jobject obj, jlong dbPtr, jstring query) {
    const char *sql = (*env)->GetStringUTFChars(env, query, 0);
    char *errMsg = 0;
    int rc = sqlite3_exec((sqlite3 *)dbPtr, sql, 0, 0, &errMsg);
    if (errMsg) sqlite3_free(errMsg);
    (*env)->ReleaseStringUTFChars(env, query, sql);
    return rc;
}
// ========== </Data Definition Language function> ==========
// ========== <Data Manipulation Language function> ==========
// Function to query the database and return JSON
JNIEXPORT jstring JNICALL Java_net_sql_SQLiteNative_queryDatabase(JNIEnv *env, jobject obj, jlong dbPtr, jstring query, jint options) {
    const char *sql = (*env)->GetStringUTFChars(env, query, 0);
    sqlite3_stmt *stmt;
    char result[8192] = "[";  // Default JSON array output

    int rc = sqlite3_prepare_v2((sqlite3 *)dbPtr, sql, -1, &stmt, 0);
    if (rc != SQLITE_OK) {
        (*env)->ReleaseStringUTFChars(env, query, sql);
        return (*env)->NewStringUTF(env, "{\"error\":\"Query failed\"}");
    }

    int columnCount = sqlite3_column_count(stmt);

    // If options == 0, return raw string (only works for single-column results)
    if (options == 0 && columnCount == 1) {
        if (sqlite3_step(stmt) == SQLITE_ROW) {
            snprintf(result, sizeof(result), "%s", sqlite3_column_text(stmt, 0));
        }
    } else {
        while (sqlite3_step(stmt) == SQLITE_ROW) {
            strcat(result, "{");
            for (int i = 0; i < columnCount; i++) {
                char buffer[512];
                snprintf(buffer, sizeof(buffer), "\"%s\":\"%s\"",
                         sqlite3_column_name(stmt, i),
                         sqlite3_column_text(stmt, i) ? (const char *)sqlite3_column_text(stmt, i) : "NULL");
                strcat(result, buffer);
                if (i < columnCount - 1) strcat(result, ",");
            }
            strcat(result, "},");
        }

        // Remove last comma and close JSON array
        if (strlen(result) > 1) result[strlen(result) - 1] = ']';
        else strcat(result, "]");
    }

    sqlite3_finalize(stmt);
    (*env)->ReleaseStringUTFChars(env, query, sql);

    return (*env)->NewStringUTF(env, result);
}
// ========== </Data Manipulation Language function> ==========
// ========== <JAVA specific DML Modell functions> ==========
// Start a query and store the result
JNIEXPORT jint JNICALL Java_net_sql_SQLiteNative_startQuery(JNIEnv *env, jobject obj, jlong dbPtr, jstring query) {
    const char *sql = (*env)->GetStringUTFChars(env, query, 0);

    if (global_stmt != NULL) {
        sqlite3_finalize(global_stmt);  // Finalize previous statement if exists
        global_stmt = NULL;
    }

    int rc = sqlite3_prepare_v2((sqlite3 *)dbPtr, sql, -1, &global_stmt, 0);
    (*env)->ReleaseStringUTFChars(env, query, sql);

    return (rc == SQLITE_OK) ? 0 : -1;
}

// Move to the next row
JNIEXPORT jint JNICALL Java_net_sql_SQLiteNative_fetchNextRow(JNIEnv *env, jobject obj) {
    if (global_stmt == NULL) return -1;
    
    int rc = sqlite3_step(global_stmt);
    if (rc == SQLITE_ROW) return 1; // More rows available
    if (rc == SQLITE_DONE) {
        sqlite3_finalize(global_stmt);
        global_stmt = NULL;
    }
    
    return 0; // No more rows
}

// Get a column value as a string
JNIEXPORT jstring JNICALL Java_net_sql_SQLiteNative_getColumnString(JNIEnv *env, jobject obj, jstring columnName) {
    if (global_stmt == NULL) return (*env)->NewStringUTF(env, "");

    const char *nativeColName = (*env)->GetStringUTFChars(env, columnName, 0);
    int columnCount = sqlite3_column_count(global_stmt);
    
    for (int i = 0; i < columnCount; i++) {
        if (strcmp(nativeColName, sqlite3_column_name(global_stmt, i)) == 0) {
            const char *value = (const char *)sqlite3_column_text(global_stmt, i);
            (*env)->ReleaseStringUTFChars(env, columnName, nativeColName);
            return (*env)->NewStringUTF(env, value ? value : "NULL");
        }
    }

    (*env)->ReleaseStringUTFChars(env, columnName, nativeColName);
    return (*env)->NewStringUTF(env, "");
}

// Get a column value as an int
JNIEXPORT jint JNICALL Java_net_sql_SQLiteNative_getColumnInt(JNIEnv *env, jobject obj, jstring columnName) {
    if (global_stmt == NULL) return 0;

    const char *nativeColName = (*env)->GetStringUTFChars(env, columnName, 0);
    int columnCount = sqlite3_column_count(global_stmt);
    
    for (int i = 0; i < columnCount; i++) {
        if (strcmp(nativeColName, sqlite3_column_name(global_stmt, i)) == 0) {
            int value = sqlite3_column_int(global_stmt, i);
            (*env)->ReleaseStringUTFChars(env, columnName, nativeColName);
            return value;
        }
    }

    (*env)->ReleaseStringUTFChars(env, columnName, nativeColName);
    return 0;
}

// Get a column value as a double (REAL)
JNIEXPORT jdouble JNICALL Java_net_sql_SQLiteNative_getColumnDouble(JNIEnv *env, jobject obj, jstring columnName) {
    if (global_stmt == NULL) return 0.0;
    
    const char *nativeColName = (*env)->GetStringUTFChars(env, columnName, 0);
    int columnCount = sqlite3_column_count(global_stmt);
    
    for (int i = 0; i < columnCount; i++) {
        if (strcmp(nativeColName, sqlite3_column_name(global_stmt, i)) == 0) {
            double value = sqlite3_column_double(global_stmt, i);
            (*env)->ReleaseStringUTFChars(env, columnName, nativeColName);
            return value;
        }
    }
    
    (*env)->ReleaseStringUTFChars(env, columnName, nativeColName);
    return 0.0;
}

// Get a column value as a 64-bit integer (LONG)
JNIEXPORT jlong JNICALL Java_net_sql_SQLiteNative_getColumnLong(JNIEnv *env, jobject obj, jstring columnName) {
    if (global_stmt == NULL) return 0;
    
    const char *nativeColName = (*env)->GetStringUTFChars(env, columnName, 0);
    int columnCount = sqlite3_column_count(global_stmt);
    
    for (int i = 0; i < columnCount; i++) {
        if (strcmp(nativeColName, sqlite3_column_name(global_stmt, i)) == 0) {
            jlong value = (jlong)sqlite3_column_int64(global_stmt, i);
            (*env)->ReleaseStringUTFChars(env, columnName, nativeColName);
            return value;
        }
    }
    
    (*env)->ReleaseStringUTFChars(env, columnName, nativeColName);
    return 0;
}

// Get a column value as a BLOB (byte array)
JNIEXPORT jbyteArray JNICALL Java_net_sql_SQLiteNative_getColumnBlob(JNIEnv *env, jobject obj, jstring columnName) {
    if (global_stmt == NULL) return NULL;
    
    const char *nativeColName = (*env)->GetStringUTFChars(env, columnName, 0);
    int columnCount = sqlite3_column_count(global_stmt);
    
    for (int i = 0; i < columnCount; i++) {
        if (strcmp(nativeColName, sqlite3_column_name(global_stmt, i)) == 0) {
            int blobSize = sqlite3_column_bytes(global_stmt, i);
            const void *blobData = sqlite3_column_blob(global_stmt, i);
            
            jbyteArray byteArray = (*env)->NewByteArray(env, blobSize);
            if (byteArray != NULL && blobData != NULL) {
                (*env)->SetByteArrayRegion(env, byteArray, 0, blobSize, (const jbyte *)blobData);
            }
            (*env)->ReleaseStringUTFChars(env, columnName, nativeColName);
            return byteArray;
        }
    }
    
    (*env)->ReleaseStringUTFChars(env, columnName, nativeColName);
    // Return an empty byte array if the column was not found
    return (*env)->NewByteArray(env, 0);
}

// Get the column type (returns one of SQLITE_INTEGER, SQLITE_FLOAT, SQLITE_TEXT, SQLITE_BLOB, SQLITE_NULL)
JNIEXPORT jint JNICALL Java_net_sql_SQLiteNative_getColumnType(JNIEnv *env, jobject obj, jstring columnName) {
    if (global_stmt == NULL) return SQLITE_NULL;
    
    const char *nativeColName = (*env)->GetStringUTFChars(env, columnName, 0);
    int columnCount = sqlite3_column_count(global_stmt);
    
    for (int i = 0; i < columnCount; i++) {
        if (strcmp(nativeColName, sqlite3_column_name(global_stmt, i)) == 0) {
            int type = sqlite3_column_type(global_stmt, i);
            (*env)->ReleaseStringUTFChars(env, columnName, nativeColName);
            return type;
        }
    }
    
    (*env)->ReleaseStringUTFChars(env, columnName, nativeColName);
    return SQLITE_NULL;
}

JNIEXPORT void JNICALL Java_net_sql_SQLiteNative_cancelQuery(JNIEnv *env, jobject obj) {
    if (global_stmt != NULL) {
        sqlite3_finalize(global_stmt);
        global_stmt = NULL;
    }
}
// ========== </JAVA specific DML Modell functions> ==========


