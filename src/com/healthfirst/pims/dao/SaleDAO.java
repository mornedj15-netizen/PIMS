package com.healthfirst.pims.dao;

import com.healthfirst.pims.db.DBConnection;
import com.healthfirst.pims.model.Sale;
import com.healthfirst.pims.model.SaleItem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Records completed sales. recordSale() wraps the sale header insert, the
 * line-item inserts, and the stock decrements in a single transaction, so
 * a failure partway through rolls everything back rather than leaving
 * stock out of sync with a half-recorded sale.
 */
public class SaleDAO {

    /** Persists the sale and returns the generated sale_id. */
    public int recordSale(Sale sale) throws SQLException {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            int saleId = insertSaleHeader(conn, sale);

            for (SaleItem item : sale.getItems()) {
                insertSaleItem(conn, saleId, item);
                decrementStock(conn, item.getMedicineId(), item.getQuantitySold());
            }

            conn.commit();
            return saleId;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    rollbackEx.printStackTrace();
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException resetEx) {
                    resetEx.printStackTrace();
                }
            }
        }
    }

    private int insertSaleHeader(Connection conn, Sale sale) throws SQLException {
        String sql = "INSERT INTO sales (total_amount, user_id) VALUES (?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setBigDecimal(1, sale.getTotalAmount());
            stmt.setInt(2, sale.getUserId());
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        throw new SQLException("Failed to obtain generated sale_id.");
    }

    private void insertSaleItem(Connection conn, int saleId, SaleItem item) throws SQLException {
        String sql = "INSERT INTO sale_items (sale_id, medicine_id, quantity_sold, price_at_sale) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, saleId);
            stmt.setInt(2, item.getMedicineId());
            stmt.setInt(3, item.getQuantitySold());
            stmt.setBigDecimal(4, item.getPriceAtSale());
            stmt.executeUpdate();
        }
    }

    private void decrementStock(Connection conn, int medicineId, int quantitySold) throws SQLException {
        String sql = "UPDATE medicines SET quantity_in_stock = quantity_in_stock - ? WHERE medicine_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, quantitySold);
            stmt.setInt(2, medicineId);
            stmt.executeUpdate();
        }
    }
}