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

To build the Project, i use the jdk-21.0.6+7 all the distros of it can be found [here](https://github.com/adoptium/temurin21-binaries/releases/tag/jdk-21.0.6%2B7)

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

### Linux

Build the SQLite Library with the C source code as an amalgamation

Here is an example for ArchLinux with x64 architecture:
```bash
gcc -shared -fPIC -o libsqlite_native_musl_x64.so -I"./jdk-21.0.6+7/include" -I"./jdk-21.0.6+7/include/linux/" "./WWU_SQLite-main/c-src/sqlite_native.c" ./sqlite-amalgamation-3490100/libsqlite3.a -lpthread -ldl
```


#### Windows

Windows does not come with an c Compiler, so i used x64 Native Tools Command Prompt for VS 2022
For Windows, create a DLL from the C source. 
Build the SQLite Library with the C source code as an amalgamation

Since you need to build the SQLite Project on Windows, we provide the latest `.dll` in Releases

```bash
cl /LD /Fe:sqlite_native.dll /I "C:/Users/Marol/Downloads/sopra/jdk-21.0.6+7/include" /I "C:/Users/Marol/Downloads/sopra/jdk-21.0.6+7/include/win32" /I "C:/Users/Marol/Downloads/sqlite-amalgamation-3490000" "C:/Users/Marol/Downloads/sqlite-amalgamation-3490000/sqlite3.c" "C:/Users/Marol/Downloads/sopra/SQLite/sqlite_native.c" /link /out:"C:/Users/Marol/Downloads/sopra/SQLite/libsqlite_native.dll"
```

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
