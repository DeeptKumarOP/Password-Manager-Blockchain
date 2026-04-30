package backend.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DBConnection {
    private static final String PWD_DB_URL = "jdbc:sqlite:database.db";

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC"); 
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite JDBC driver not found.");
            System.exit(1);
        }
        return DriverManager.getConnection(PWD_DB_URL);
    }
    
    public static void initializeDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            
            String blockTable = "CREATE TABLE IF NOT EXISTS blocks (" +
                "block_index INTEGER PRIMARY KEY," +
                "username TEXT NOT NULL," +
                "salt TEXT NOT NULL," +
                "auth_salt TEXT NOT NULL," +
                "password_hash TEXT NOT NULL," +
                "public_key TEXT," +
                "encrypted_private_key TEXT," +
                "signature TEXT," +
                "previous_hash TEXT NOT NULL," +
                "current_hash TEXT NOT NULL," +
                "timestamp INTEGER NOT NULL," +
                "service_passwords TEXT" +
                ");";
                
            String logTable = "CREATE TABLE IF NOT EXISTS logs (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "username TEXT NOT NULL," +
                "action TEXT NOT NULL," +
                "timestamp INTEGER NOT NULL" +
                ");";
                
            stmt.execute(blockTable);
            stmt.execute(logTable);
        } catch (SQLException e) {
            System.err.println("Failed to initialize database: " + e.getMessage());
        }
    }
}
