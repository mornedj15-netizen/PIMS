package com.healthfirst.pims.dao;

import com.healthfirst.pims.db.DBConnection;
import com.healthfirst.pims.model.Sale;
import com.healthfirst.pims.model.SaleItem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.ArrayList;
import java.util.List;

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

    /** All sales, most recent first - for the Sales report. */
    public List<Object[]> getSalesReport() throws SQLException {
        String sql = "SELECT s.sale_id, s.sale_date, s.total_amount, u.full_name "
                + "FROM sales s JOIN users u ON s.user_id = u.user_id "
                + "ORDER BY s.sale_date DESC";
        List<Object[]> rows = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                rows.add(new Object[] {
                        rs.getInt("sale_id"), rs.getTimestamp("sale_date"),
                        rs.getBigDecimal("total_amount"), rs.getString("full_name")
                });
            }
        }
        return rows;
    }

    /**
     * Total quantity sold and revenue per medicine, highest revenue first - for the
     * Item-Wise report.
     */
    public List<Object[]> getItemWiseReport() throws SQLException {
        String sql = "SELECT m.name, SUM(si.quantity_sold) AS total_qty, "
                + "SUM(si.quantity_sold * si.price_at_sale) AS total_revenue "
                + "FROM sale_items si JOIN medicines m ON si.medicine_id = m.medicine_id "
                + "GROUP BY m.medicine_id, m.name "
                + "ORDER BY total_revenue DESC";
        List<Object[]> rows = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                rows.add(new Object[] {
                        rs.getString("name"), rs.getInt("total_qty"), rs.getBigDecimal("total_revenue")
                });
            }
        }
        return rows;
    }
}