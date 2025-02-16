# WWU CSQLLite java integration

We wrote this in "SoPra2025" to include sqllite into our java application wihout using external librarys or the defacto standard jdbc

This implementation uses JNI - Java native Interface

Compiled with files from JDK Version: 21.0.6+7

### Macos compiliation
```
gcc -shared -o libsqlite_native.dylib -I"./jdk-21.0.6+7/Contents/Home/include" -I "./jdk-21.0.6+7/Contents/Home/include/darwin/" "./SQLite/sqlite_native.c" -lsqlite3
```

Macos Gatekeeper needs you sign to the dylib file.

Here is an example of how to sign the file just for you:
```
codesign --force --deep --sign - "/Users/matteomarolt/Developer/Software Praktikum 2025/libsqlite_native.dylib"
```

### Windows compilation
```
We need to add the creation of a dll here.
```

