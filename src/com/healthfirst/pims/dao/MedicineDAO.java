package com.healthfirst.pims.dao;

import com.healthfirst.pims.db.DBConnection;
import com.healthfirst.pims.model.Medicine;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * CRUD operations for the `medicines` table, plus the queries backing the
 * Low Stock and Expiry reports.
 */
public class MedicineDAO {

    public List<Medicine> getAll() throws SQLException {
        String sql = "SELECT * FROM medicines ORDER BY name";
        List<Medicine> medicines = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                medicines.add(mapRow(rs));
            }
        }
        return medicines;
    }

    public void add(Medicine m) throws SQLException {
        String sql = "INSERT INTO medicines (name, company, medicine_type, price, quantity_in_stock, "
                + "reorder_level, expiry_date, supplier_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            bindParams(stmt, m);
            stmt.executeUpdate();
        }
    }

    public void update(Medicine m) throws SQLException {
        String sql = "UPDATE medicines SET name=?, company=?, medicine_type=?, price=?, quantity_in_stock=?, "
                + "reorder_level=?, expiry_date=?, supplier_id=? WHERE medicine_id=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            bindParams(stmt, m);
            stmt.setInt(9, m.getMedicineId());
            stmt.executeUpdate();
        }
    }

    public void delete(int medicineId) throws SQLException {
        String sql = "DELETE FROM medicines WHERE medicine_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, medicineId);
            stmt.executeUpdate();
        }
    }

    /** Medicines at or below their reorder level - used by the Low Stock report. */
    public List<Medicine> getLowStock() throws SQLException {
        String sql = "SELECT * FROM medicines WHERE quantity_in_stock <= reorder_level ORDER BY quantity_in_stock";
        List<Medicine> medicines = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                medicines.add(mapRow(rs));
            }
        }
        return medicines;
    }

    /** Medicines expiring within the next month - used by the Expiry report. */
    public List<Medicine> getExpiringWithinDays(int days) throws SQLException {
        String sql = "SELECT * FROM medicines WHERE expiry_date <= DATE_ADD(CURDATE(), INTERVAL ? DAY) "
                + "ORDER BY expiry_date";
        List<Medicine> medicines = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, days);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    medicines.add(mapRow(rs));
                }
            }
        }
        return medicines;
    }

    private void bindParams(PreparedStatement stmt, Medicine m) throws SQLException {
        stmt.setString(1, m.getName());
        stmt.setString(2, m.getCompany());
        stmt.setString(3, m.getMedicineType());
        stmt.setBigDecimal(4, m.getPrice());
        stmt.setInt(5, m.getQuantityInStock());
        stmt.setInt(6, m.getReorderLevel());
        stmt.setDate(7, java.sql.Date.valueOf(m.getExpiryDate()));
        stmt.setInt(8, m.getSupplierId());
    }

    private Medicine mapRow(ResultSet rs) throws SQLException {
        LocalDate expiry = rs.getDate("expiry_date") != null
                ? rs.getDate("expiry_date").toLocalDate()
                : null;

        return new Medicine(
                rs.getInt("medicine_id"),
                rs.getString("name"),
                rs.getString("company"),
                rs.getString("medicine_type"),
                rs.getBigDecimal("price"),
                rs.getInt("quantity_in_stock"),
                rs.getInt("reorder_level"),
                expiry,
                rs.getInt("supplier_id")
        );
    }
}