package lab2;

import lab1.Transaction;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO {

    public void insert(Connection con, int accountId, String referenceId, String type, double amount) throws SQLException {
        String sql = "INSERT INTO transactions (account_id, reference_id, transaction_type, amount) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            ps.setString(2, referenceId);
            ps.setString(3, type);
            ps.setDouble(4, amount);
            ps.executeUpdate();
        }
    }

    public List<Transaction> findByAccount(Connection con, int accountId) throws SQLException {
        String sql = "SELECT id, reference_id, transaction_type, amount, created_at " +
                "FROM transactions WHERE account_id = ? ORDER BY created_at DESC, id DESC";
        List<Transaction> list = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Transaction(rs.getInt("id"), rs.getString("reference_id"),
                            rs.getDouble("amount"), rs.getString("transaction_type"),
                            rs.getTimestamp("created_at").getTime()));
                }
            }
        }
        return list;
    }

    public List<String> dailySummary(Connection con) throws SQLException {
        String sql = "SELECT created_at::date AS day, transaction_type, COUNT(*) AS cnt, SUM(amount) AS total " +
                "FROM transactions GROUP BY day, transaction_type ORDER BY day DESC, transaction_type";
        List<String> lines = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lines.add(String.format("%s | %-13s | count=%d | total=%.2f",
                        rs.getDate("day"), rs.getString("transaction_type"),
                        rs.getInt("cnt"), rs.getDouble("total")));
            }
        }
        return lines;
    }

    public boolean delete(Connection con, int id) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("DELETE FROM transactions WHERE id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }
}