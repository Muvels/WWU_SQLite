# SQLite JNI Integration for Java
Developed by me for the WWU SoPra2025 project, this integration serves as a lightweight alternative to using JDBC for SQLite in Java.
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

```bash
cl /LD /Fe:sqlite_native.dll /I "C:/Users/Marol/Downloads/sopra/jdk-21.0.6+7/include" /I "C:/Users/Marol/Downloads/sopra/jdk-21.0.6+7/include/win32" /I "C:/Users/Marol/Downloads/sqlite-amalgamation-3490000" "C:/Users/Marol/Downloads/sqlite-amalgamation-3490000/sqlite3.c" "C:/Users/Marol/Downloads/sopra/WWU_SQLite/c-src/sqlite_native.c"" /link /out:"C:/Users/Marol/Downloads/sopra/SQLite/libsqlite_native.dll"
```

Since you need to build the SQLite Project on Windows, we provide the latest `.dll` in Releases

```bash
cl /LD /Fe:sqlite_native.dll /I "C:/Users/Marol/Downloads/sopra/jdk-21.0.6+7/include" /I "C:/Users/Marol/Downloads/sopra/jdk-21.0.6+7/include/win32" /I "C:/Users/Marol/Downloads/sqlite-amalgamation-3490000" "C:/Users/Marol/Downloads/sqlite-amalgamation-3490000/sqlite3.c" "C:/Users/Marol/Downloads/sopra/SQLite/sqlite_native.c" /link /out:"C:/Users/Marol/Downloads/sopra/SQLite/libsqlite_native.dll"
```

### Java Integration

The Java classes are designed to automatically extract and load the native library from the JAR (see `SQLiteNative.java`). Ensure that the native libraries are packaged in the `c-side` directory within your JAR file.

## Usage Example

### Mapped Query Results with `executeQueryWithMapping`

I introduced a unique new feature to replace json formating in my integration: the **`executeQueryWithMapping`** function! This powerful addition automatically converts rows returned from an SQL query into fully populated Java objects. Imagine being able to query your SQLite database and instantly receive a list of your custom model objects—no manual parsing required!

#### How It Works

- **Automatic Object Mapping:**  
  Each row from your query is dynamically mapped to an instance of a specified Java class (e.g., `YourDataModel`), using setter methods that follow a simple naming convention (e.g., `setName`, `setAge`, etc.).

- **Type-Safe Conversion:**  
  My implementation supports multiple data types—integers, doubles, longs, byte arrays (BLOBs), and strings. It intelligently selects the appropriate setter based on the SQLite column type, ensuring that your data is converted safely and accurately.

- **Graceful Handling of Missing Setters:**  
  If your model class doesn't define a setter for a particular column, the mapping simply skips that column without throwing an error. This means you can design your classes with only the properties you need.

#### Example Usage

Here's a quick example of how you can use the new feature:

```java
ArrayList<YourDataModel> results = db.executeQueryWithMapping(
        "SELECT * FROM YourDataModel",
        null,
        YourDataModel.class
);
for (YourDataModel model : results) {
    System.out.println(model);
}

```

In this snippet, every row from the YourDataModel table is mapped into a YourDataModel object. The result is a clean, type-safe list of objects that you can work with immediately.

Benefits
  * Simplified Data Handling: No more tedious result parsing code—just query, map, and use!
  * Cleaner Code: Your data access layer becomes more expressive and easier to maintain.
  * Reduced Error Risk: Automatic type conversion minimizes the risk of runtime errors from manual parsing.

#### Below is a sample usage demonstrating how to open a database, execute SQL statements, and process query results:


```java
package net.sql;

import java.util.ArrayList;

class YourDataModel {
    private int id;
    private String name;
    private String description;
    private int age;
    private double salary;
    private long someLong;
    private byte[] data;

    public YourDataModel(int id, String name) {
    	this.name = name;
    	this.id = id;
    }
    
    // This is required, if you use a constructor with parameters, so my db_client can create a class
    public YourDataModel() {}

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public void setSalary(double salary) {
        this.salary = salary;
    }

    public void setSomeLong(long someLong) {
        this.someLong = someLong;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    @Override
    public String toString() {
        // For demonstration, we convert the blob (data) to a String
        String blobStr = (data != null) ? new String(data) : "null";
        return "YourDataModel{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", age=" + age +
                ", salary=" + salary +
                ", someLong=" + someLong +
                ", data=" + blobStr +
                '}';
    }
}

public class Example {
    public static void main(String[] args) {
        try (SQLiteDatabase db = new SQLiteDatabase("database")) {
            // Create and insert into a simple users table for testing
            db.execute("CREATE TABLE IF NOT EXISTS users (id INTEGER PRIMARY KEY, name TEXT);");
            db.execute("INSERT INTO users (name) VALUES ('Alice');");
            db.execute("INSERT INTO users (name) VALUES ('Bob');");

            // Create the YourDataModel table with various column types
            db.execute("CREATE TABLE IF NOT EXISTS YourDataModel (" +
                       "id INTEGER PRIMARY KEY, " +
                       "name TEXT, " +
                       "description TEXT, " +
                       "age INTEGER, " +
                       "salary REAL, " +
                       "someLong INTEGER, " +
                       "data BLOB" +
                       ");");
            // Insert two rows with values for all columns.
            // For the BLOB column, we use a hexadecimal literal (e.g., X'48656C6C6F' equals "Hello").
            db.execute("INSERT INTO YourDataModel (name, description, age, salary, someLong, data) VALUES " +
                       "('Alice', 'Software Developer', 48, 1234.56, 987654321, X'48656C6C6F');");
            db.execute("INSERT INTO YourDataModel (name, description, age, salary, someLong, data) VALUES " +
                       "('Bob', 'Data Analyst', 300, 6543.21, 123456789, X'776F726C64');");

            // Test the users table using a prepared statement
            try (SQLiteStatement stmt = db.prepareStatement("SELECT * FROM users;")) {
                while (stmt.next()) {
                    int id = stmt.getInt("id");
                    String name = stmt.getString("name");
                    System.out.println("User: id=" + id + ", name=" + name + " Type of name: " + stmt.getType("name"));
                }
            }
            
            System.out.println("Experimental Stuff now");
            
            ArrayList<YourDataModel> results = db.executeQueryWithMapping(
                    "SELECT * FROM YourDataModel",
                    null,
                    YourDataModel.class
                );
            
            for (YourDataModel model : results) {
                System.out.println(model);
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
