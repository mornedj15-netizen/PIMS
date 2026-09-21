package com.healthfirst.pims.ui;

import com.healthfirst.pims.dao.MedicineDAO;
import com.healthfirst.pims.model.Medicine;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/**
 * Read-only medicine lookup for cashiers. No add/edit/delete controls -
 * that stays Admin-only per the brief.
 */
public class StockCheckPanel extends JPanel {

    private final MedicineDAO medicineDAO = new MedicineDAO();

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"Name", "Company", "Type", "Price", "In Stock", "Expiry"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(tableModel);
    private final JTextField searchField = new JTextField(20);
    private final JLabel statusLabel = new JLabel(" ");

    public StockCheckPanel() {
        setLayout(new BorderLayout(10, 10));

        add(buildSearchPanel(), BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        searchField.addCaretListener(e -> {
            String text = searchField.getText().trim();
            sorter.setRowFilter(text.isEmpty() ? null
                    : RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(text), 0));
        });

        refreshTable();
    }

    private JPanel buildSearchPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.add(new JLabel("Search by name:"));
        panel.add(searchField);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> refreshTable());
        panel.add(refreshButton);

        return panel;
    }

    private void refreshTable() {
        try {
            tableModel.setRowCount(0);
            List<Medicine> medicines = medicineDAO.getAll();
            for (Medicine m : medicines) {
                tableModel.addRow(new Object[]{
                        m.getName(), m.getCompany(), m.getMedicineType(),
                        m.getPrice(), m.getQuantityInStock(), m.getExpiryDate()
                });
            }
            statusLabel.setText(" ");
        } catch (SQLException e) {
            statusLabel.setText("Failed to load medicines: " + e.getMessage());
        }
    }
}