package backend.db;

import backend.blockchain.Block;
import java.sql.*;
import java.util.ArrayList;

public class UserDAO {
    
    public void saveBlock(Block block) {
        String sql = "INSERT INTO blocks(block_index, username, salt, auth_salt, password_hash, public_key, encrypted_private_key, signature, previous_hash, current_hash, timestamp, service_passwords) VALUES(?,?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, block.index);
            pstmt.setString(2, block.username);
            pstmt.setString(3, block.salt);
            pstmt.setString(4, block.authSalt);
            pstmt.setString(5, block.passwordHash);
            pstmt.setString(6, block.publicKey);
            pstmt.setString(7, block.encryptedPrivateKey);
            pstmt.setString(8, block.signature);
            pstmt.setString(9, block.previousHash);
            pstmt.setString(10, block.currentHash);
            pstmt.setLong(11, block.timestamp);
            pstmt.setString(12, block.servicePasswords);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error saving block: " + e.getMessage());
        }
    }
    
    public void corruptBlock(int blockIndex) {
        String sql = "UPDATE blocks SET password_hash = 'tampered_fake_hash_123', signature = 'invalid_sig' WHERE block_index = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, blockIndex);
            int rows = pstmt.executeUpdate();
            if(rows > 0) {
                System.out.println("Simulated breach: Block " + blockIndex + " corrupted in DB.");
            }
        } catch (SQLException e) {
            System.err.println("Error corrupting block: " + e.getMessage());
        }
    }

    public ArrayList<Block> loadAllBlocks() {
        ArrayList<Block> blocks = new ArrayList<>();
        String sql = "SELECT * FROM blocks ORDER BY block_index ASC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
             
            while (rs.next()) {
                Block b = new Block(
                    rs.getInt("block_index"),
                    rs.getString("username"),
                    rs.getString("salt"),
                    rs.getString("auth_salt"),
                    rs.getString("password_hash"),
                    rs.getString("public_key"),
                    rs.getString("encrypted_private_key"),
                    rs.getString("signature"),
                    rs.getString("previous_hash"),
                    rs.getString("current_hash"),
                    rs.getLong("timestamp"),
                    rs.getString("service_passwords")
                );
                blocks.add(b);
            }
        } catch (SQLException e) {
            // ignore init err
        }
        return blocks;
    }
}
