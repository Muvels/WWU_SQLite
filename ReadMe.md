# SQLite JNI Integration for Java

## Overview

This project provides a lightweight Java wrapper for the native SQLite interface using JNI (Java Native Interface). It enables Java applications to work directly with SQLite databases without relying on external libraries or the standard JDBC driver.

## Features

- **Direct SQLite Integration:** Connect to and operate on SQLite databases via native methods.
- **Platform-Aware Native Loading:** Automatically detects the operating system and loads the corresponding native library from within the JAR.
- **Query Execution & Management:** Execute SQL commands and manage query results through a simple API.
- **Resource Safety:** Implements `AutoCloseable` for seamless resource management using try-with-resources.

## Components

- **SQLiteDatabase:**  
  Provides a high-level API to open a database, execute non-query SQL statements, and prepare queries.  
  *Source: SQLiteDatabase.java*

- **SQLiteNative:**  
  Handles the native interactions, including loading the platform-specific library and declaring JNI methods for database operations such as opening, closing, executing queries, and fetching results.  
  *Source: SQLiteNative.java*

- **SQLiteStatement:**  
  Manages the lifecycle of an active SQL query. It provides methods to iterate over query results and retrieve data in various types.  
  *Source: SQLiteStatement.java*

- **Native Implementation (sqlite_native.c):**  
  The C source file implementing the JNI bridge to SQLite.
  *Source: sqlite_native.c*

## Compilation and Setup

I designed the Project, in a way that you dont need to compile the soruce by yourself, if you want to do it anyway here is how.

### Native Library Compilation

#### macOS

Compile the native library with:

```bash
gcc -shared -o libsqlite_native.dylib -I"./jdk-21.0.6+7/Contents/Home/include" -I"./jdk-21.0.6+7/Contents/Home/include/darwin/" "./SQLite/sqlite_native.c" -lsqlite3
```

> **Note:** macOS Gatekeeper may require you to sign the resulting dylib. For example:
>
> ```bash
> codesign --force --deep --sign - "/path/to/libsqlite_native.dylib"
> ```

#### Windows

For Windows, create a DLL from the C source. 

Since you need to build the SQLite Project on Windows, we provide the latest `.dll` in Releases

### Java Integration

The Java classes are designed to automatically extract and load the native library from the JAR (see `SQLiteNative.java`). Ensure that the native libraries are packaged in the `c-side` directory within your JAR file.

## Usage Example

Below is a sample usage demonstrating how to open a database, execute SQL statements, and process query results:

```java
import net.sql.SQLiteDatabase;
import net.sql.SQLiteStatement;

public class Main {
    public static void main(String[] args) {
        try (SQLiteDatabase db = new SQLiteDatabase("example.db")) {
            // Create a table if it doesn't exist
            db.execute("CREATE TABLE IF NOT EXISTS users (id INTEGER PRIMARY KEY, name TEXT);");

            // Insert data into the table
            db.execute("INSERT INTO users (name) VALUES ('Alice');");
            db.execute("INSERT INTO users (name) VALUES ('Bob');");

            // Query the database
            try (SQLiteStatement stmt = db.prepareStatement("SELECT id, name FROM users;")) {
                while (stmt.next()) {
                    int id = stmt.getInt("id");
                    String name = stmt.getString("name");
                    System.out.println("User: " + id + " - " + name);
                }
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }
}
```

## Directory Structure

```
.
├── src            // All Abstractions and apis for java.
├── resources     // `.dylib`, `.dll` and `.so` for the project.
└── c-src        // JNI C implementation.
```

## Acknowledgments

Developed by me for the WWU SoPra2025 project, this integration serves as a lightweight alternative to using JDBC for SQLite in Java.
