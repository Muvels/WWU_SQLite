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
