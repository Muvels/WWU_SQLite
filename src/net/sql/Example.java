package net.sql;

public class Example {
    public static void main(String[] args) {
        // Thats why we have done the integration with the AutoCloseable thing.
        try (SQLiteDatabase db = new SQLiteDatabase("database")) {
            db.execute("CREATE TABLE IF NOT EXISTS users (id INTEGER PRIMARY KEY, name TEXT);");
            db.execute("INSERT INTO users (name) VALUES ('Alice');");
            db.execute("INSERT INTO users (name) VALUES ('Bob');");

            // Same here with AutoCloseable
            try (SQLiteStatement stmt = db.prepareStatement("SELECT * FROM users;")) {
                while (stmt.next()) {
                    int id = stmt.getInt("id");
                    String name = stmt.getString("name");
                    System.out.println("User: id=" + id + ", name=" + name + "Type of name: " +  stmt.getType("name"));
                }
            } // stmt.close() is called automatically here because we are using the nice AutoCloseable here.
        } catch (RuntimeException e) {
            e.printStackTrace();
        } // same here db.close() is called automatically.
    }
}
