package backend.db;

import backend.models.LogEntry;
import java.sql.*;
import java.util.ArrayList;

public class LogDAO {
    public void saveLog(LogEntry log) {
        String sql = "INSERT INTO logs(username, action, timestamp) VALUES(?,?,?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, log.username);
            pstmt.setString(2, log.action);
            pstmt.setLong(3, log.timestamp);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error saving log: " + e.getMessage());
        }
    }

    public ArrayList<LogEntry> getAllLogs() {
        ArrayList<LogEntry> logs = new ArrayList<>();
        String sql = "SELECT * FROM logs ORDER BY timestamp DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
             
            while (rs.next()) {
                logs.add(new LogEntry(
                    rs.getString("username"),
                    rs.getString("action"),
                    rs.getLong("timestamp")
                ));
            }
        } catch (SQLException e) {
            
        }
        return logs;
    }
}
