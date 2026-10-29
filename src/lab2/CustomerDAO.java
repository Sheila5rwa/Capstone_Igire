package lab2;

import lab1.Customer;
import java.sql.*;

public class CustomerDAO {

    public int create(Connection con, Customer c, String pinHash) throws SQLException {
        String sql = "INSERT INTO customers (full_name, email, phone_number, pin_hash) " +
                "VALUES (?, ?, ?, ?) RETURNING id";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, c.getCustomerName());
            ps.setString(2, c.getEmail());
            ps.setString(3, c.getPhoneNumber());
            ps.setString(4, pinHash);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public Customer findById(Connection con, int id) throws SQLException {
        String sql = "SELECT id, full_name, email, phone_number FROM customers WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return new Customer(rs.getInt("id"), rs.getString("full_name"),
                        rs.getString("email"), rs.getString("phone_number"), "");
            }
        }
    }

    public String getPinHash(Connection con, int id) throws SQLException {
        String sql = "SELECT pin_hash FROM customers WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString("pin_hash") : null;
            }
        }
    }

    public boolean update(Connection con, int id, String name, String email, String phone) throws SQLException {
        String sql = "UPDATE customers SET full_name = ?, email = ?, phone_number = ? WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, phone);
            ps.setInt(4, id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updatePin(Connection con, int id, String newHash) throws SQLException {
        String sql = "UPDATE customers SET pin_hash = ? WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, newHash);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(Connection con, int id) throws SQLException {
        String sql = "DELETE FROM customers WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }
}