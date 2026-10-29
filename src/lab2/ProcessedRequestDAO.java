package lab2;

import java.sql.*;

public class ProcessedRequestDAO {

    public boolean exists(Connection con, String referenceId) throws SQLException {
        String sql = "SELECT 1 FROM processed_requests WHERE reference_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, referenceId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public void insert(Connection con, String referenceId) throws SQLException {
        String sql = "INSERT INTO processed_requests (reference_id) VALUES (?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, referenceId);
            ps.executeUpdate();
        }
    }

    public boolean delete(Connection con, String referenceId) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("DELETE FROM processed_requests WHERE reference_id = ?")) {
            ps.setString(1, referenceId);
            return ps.executeUpdate() > 0;
        }
    }
}