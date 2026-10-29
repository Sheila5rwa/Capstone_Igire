package lab2;

import lab1.Account;
import lab1.SavingsAccount;
import lab1.WalletAccount;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AccountDAO {

    private static final String SELECT =
            "SELECT a.id, a.account_type, a.balance, a.status, c.full_name " +
                    "FROM accounts a JOIN customers c ON c.id = a.customer_id ";

    public int create(Connection con, int customerId, String type) throws SQLException {
        String sql = "INSERT INTO accounts (customer_id, account_type) VALUES (?, ?) RETURNING id";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            ps.setString(2, type);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public Account findById(Connection con, int id) throws SQLException {
        return one(con, SELECT + "WHERE a.id = ?", id);
    }

    public Account findByIdForUpdate(Connection con, int id) throws SQLException {
        return one(con, SELECT + "WHERE a.id = ? FOR UPDATE OF a", id);
    }

    public List<Account> findByCustomer(Connection con, int customerId) throws SQLException {
        List<Account> list = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(SELECT + "WHERE a.customer_id = ? ORDER BY a.id")) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public boolean updateBalance(Connection con, int id, double balance) throws SQLException {
        String sql = "UPDATE accounts SET balance = ? WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDouble(1, balance);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateStatus(Connection con, int id, String status) throws SQLException {
        String sql = "UPDATE accounts SET status = ? WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteInactive(Connection con, int id) throws SQLException {
        String sql = "DELETE FROM accounts WHERE id = ? AND status = 'INACTIVE'";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private Account one(Connection con, String sql, int id) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    private Account map(ResultSet rs) throws SQLException {
        String number = String.valueOf(rs.getInt("id"));
        String owner = rs.getString("full_name");
        double balance = rs.getDouble("balance");
        String status = rs.getString("status");
        if ("SAVINGS".equals(rs.getString("account_type"))) {
            return new SavingsAccount(number, owner, balance, "RWF", status);
        }
        return new WalletAccount(number, owner, balance, "RWF", status);
    }
}