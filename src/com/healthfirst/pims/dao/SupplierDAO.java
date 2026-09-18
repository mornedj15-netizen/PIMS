package com.healthfirst.pims.dao;

import com.healthfirst.pims.db.DBConnection;
import com.healthfirst.pims.model.Supplier;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SupplierDAO {

    public List<Supplier> getAll() throws SQLException {
        String sql = "SELECT * FROM suppliers ORDER BY name";
        List<Supplier> suppliers = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                suppliers.add(mapRow(rs));
            }
        }
        return suppliers;
    }

    public void add(Supplier s) throws SQLException {
        String sql = "INSERT INTO suppliers (name, contact_person, phone, email, address) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            bindParams(stmt, s);
            stmt.executeUpdate();
        }
    }

    public void update(Supplier s) throws SQLException {
        String sql = "UPDATE suppliers SET name=?, contact_person=?, phone=?, email=?, address=? WHERE supplier_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            bindParams(stmt, s);
            stmt.setInt(6, s.getSupplierId());
            stmt.executeUpdate();
        }
    }

    public void delete(int supplierId) throws SQLException {
        String sql = "DELETE FROM suppliers WHERE supplier_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, supplierId);
            stmt.executeUpdate();
        }
    }

    private void bindParams(PreparedStatement stmt, Supplier s) throws SQLException {
        stmt.setString(1, s.getName());
        stmt.setString(2, s.getContactPerson());
        stmt.setString(3, s.getPhone());
        stmt.setString(4, s.getEmail());
        stmt.setString(5, s.getAddress());
    }

    private Supplier mapRow(ResultSet rs) throws SQLException {
        return new Supplier(
                rs.getInt("supplier_id"),
                rs.getString("name"),
                rs.getString("contact_person"),
                rs.getString("phone"),
                rs.getString("email"),
                rs.getString("address")
        );
    }
}