package com.healthfirst.pims.ui;

import com.healthfirst.pims.dao.MedicineDAO;
import com.healthfirst.pims.dao.SaleDAO;
import com.healthfirst.pims.model.Medicine;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class ReportsPanel extends JPanel {

    private final SaleDAO saleDAO = new SaleDAO();
    private final MedicineDAO medicineDAO = new MedicineDAO();

    public ReportsPanel() {
        setLayout(new BorderLayout());

        JTabbedPane reportTabs = new JTabbedPane();
        reportTabs.addTab("Sales Report", buildSalesReportTab());
        reportTabs.addTab("Item-Wise Report", buildItemWiseReportTab());
        reportTabs.addTab("Low Stock Report", buildLowStockReportTab());
        reportTabs.addTab("Expiry Report (30 days)", buildExpiryReportTab());

        add(reportTabs, BorderLayout.CENTER);
    }

    private JPanel buildSalesReportTab() {
        DefaultTableModel model = new DefaultTableModel(
                new String[]{"Sale ID", "Date", "Total", "Cashier"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(model);

        return wrapWithRefresh(table, () -> {
            model.setRowCount(0);
            try {
                for (Object[] row : saleDAO.getSalesReport()) {
                    model.addRow(row);
                }
                return null;
            } catch (SQLException e) {
                return "Failed to load sales report: " + e.getMessage();
            }
        });
    }

    private JPanel buildItemWiseReportTab() {
        DefaultTableModel model = new DefaultTableModel(
                new String[]{"Medicine", "Total Quantity Sold", "Total Revenue"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(model);

        return wrapWithRefresh(table, () -> {
            model.setRowCount(0);
            try {
                for (Object[] row : saleDAO.getItemWiseReport()) {
                    model.addRow(row);
                }
                return null;
            } catch (SQLException e) {
                return "Failed to load item-wise report: " + e.getMessage();
            }
        });
    }

    private JPanel buildLowStockReportTab() {
        DefaultTableModel model = new DefaultTableModel(
                new String[]{"Name", "In Stock", "Reorder Level", "Supplier ID"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(model);

        return wrapWithRefresh(table, () -> {
            model.setRowCount(0);
            try {
                List<Medicine> medicines = medicineDAO.getLowStock();
                for (Medicine m : medicines) {
                    model.addRow(new Object[]{
                            m.getName(), m.getQuantityInStock(), m.getReorderLevel(), m.getSupplierId()
                    });
                }
                return null;
            } catch (SQLException e) {
                return "Failed to load low stock report: " + e.getMessage();
            }
        });
    }

    private JPanel buildExpiryReportTab() {
        DefaultTableModel model = new DefaultTableModel(
                new String[]{"Name", "Expiry Date", "In Stock"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(model);

        return wrapWithRefresh(table, () -> {
            model.setRowCount(0);
            try {
                List<Medicine> medicines = medicineDAO.getExpiringWithinDays(30);
                for (Medicine m : medicines) {
                    model.addRow(new Object[]{m.getName(), m.getExpiryDate(), m.getQuantityInStock()});
                }
                return null;
            } catch (SQLException e) {
                return "Failed to load expiry report: " + e.getMessage();
            }
        });
    }

    /** Wraps a report table with a Refresh button + status label, and loads it once immediately. */
    private JPanel wrapWithRefresh(JTable table, java.util.function.Supplier<String> loadAction) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        JLabel statusLabel = new JLabel(" ");
        statusLabel.setForeground(Color.RED);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> {
            String error = loadAction.get();
            statusLabel.setText(error != null ? error : " ");
        });

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(refreshButton);

        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(statusLabel, BorderLayout.SOUTH);

        String initialError = loadAction.get();
        statusLabel.setText(initialError != null ? initialError : " ");

        return panel;
    }
}