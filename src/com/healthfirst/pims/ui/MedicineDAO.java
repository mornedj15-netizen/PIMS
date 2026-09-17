package com.healthfirst.pims.ui;

import com.healthfirst.pims.dao.MedicineDAO;
import com.healthfirst.pims.model.Medicine;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Admin tab: full CRUD over the medicines table, backed by MedicineDAO.
 * Select a row to load it into the form; Add/Update/Delete act on it.
 */
public class ManageMedicinesPanel extends JPanel {

    private final MedicineDAO medicineDAO = new MedicineDAO();

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"ID", "Name", "Company", "Type", "Price", "Qty", "Reorder Lvl", "Expiry", "Supplier ID"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false; // table is read-only; edits happen through the form
        }
    };
    private final JTable table = new JTable(tableModel);

    private final JTextField nameField = new JTextField(15);
    private final JTextField companyField = new JTextField(15);
    private final JTextField typeField = new JTextField(10);
    private final JTextField priceField = new JTextField(8);
    private final JTextField quantityField = new JTextField(6);
    private final JTextField reorderField = new JTextField(6);
    private final JTextField expiryField = new JTextField(10); // yyyy-MM-dd
    private final JTextField supplierIdField = new JTextField(6);

    private final JLabel statusLabel = new JLabel(" ");
    private int selectedMedicineId = -1;

    public ManageMedicinesPanel() {
        setLayout(new BorderLayout(10, 10));

        table.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) loadSelectedRowIntoForm();
        });
        add(new JScrollPane(table), BorderLayout.CENTER);

        add(buildFormPanel(), BorderLayout.SOUTH);

        refreshTable();
    }

    private JPanel buildFormPanel() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Medicine Details"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        addField(form, gbc, row++, "Name:", nameField);
        addField(form, gbc, row++, "Company:", companyField);
        addField(form, gbc, row++, "Type (Tablet/Capsule/Syrup/Injection/Cream):", typeField);
        addField(form, gbc, row++, "Price:", priceField);
        addField(form, gbc, row++, "Quantity in Stock:", quantityField);
        addField(form, gbc, row++, "Reorder Level:", reorderField);
        addField(form, gbc, row++, "Expiry Date (yyyy-MM-dd):", expiryField);
        addField(form, gbc, row++, "Supplier ID:", supplierIdField);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addButton = new JButton("Add");
        JButton updateButton = new JButton("Update Selected");
        JButton deleteButton = new JButton("Delete Selected");
        JButton clearButton = new JButton("Clear Form");

        addButton.addActionListener(e -> handleAdd());
        updateButton.addActionListener(e -> handleUpdate());
        deleteButton.addActionListener(e -> handleDelete());
        clearButton.addActionListener(e -> clearForm());

        buttons.add(addButton);
        buttons.add(updateButton);
        buttons.add(deleteButton);
        buttons.add(clearButton);

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        form.add(buttons, gbc);

        statusLabel.setForeground(Color.RED);
        gbc.gridy = row + 1;
        form.add(statusLabel, gbc);

        return form;
    }

    private void addField(JPanel form, GridBagConstraints gbc, int row, String label, JTextField field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        form.add(new JLabel(label), gbc);
        gbc.gridx = 1;
        form.add(field, gbc);
    }

    private void refreshTable() {
        try {
            tableModel.setRowCount(0);
            List<Medicine> medicines = medicineDAO.getAll();
            for (Medicine m : medicines) {
                tableModel.addRow(new Object[]{
                        m.getMedicineId(), m.getName(), m.getCompany(), m.getMedicineType(),
                        m.getPrice(), m.getQuantityInStock(), m.getReorderLevel(),
                        m.getExpiryDate(), m.getSupplierId()
                });
            }
            statusLabel.setText(" ");
        } catch (SQLException e) {
            statusLabel.setText("Failed to load medicines: " + e.getMessage());
        }
    }

    private void loadSelectedRowIntoForm() {
        int row = table.getSelectedRow();
        if (row == -1) return;

        selectedMedicineId = (int) tableModel.getValueAt(row, 0);
        nameField.setText(String.valueOf(tableModel.getValueAt(row, 1)));
        companyField.setText(String.valueOf(tableModel.getValueAt(row, 2)));
        typeField.setText(String.valueOf(tableModel.getValueAt(row, 3)));
        priceField.setText(String.valueOf(tableModel.getValueAt(row, 4)));
        quantityField.setText(String.valueOf(tableModel.getValueAt(row, 5)));
        reorderField.setText(String.valueOf(tableModel.getValueAt(row, 6)));
        expiryField.setText(String.valueOf(tableModel.getValueAt(row, 7)));
        supplierIdField.setText(String.valueOf(tableModel.getValueAt(row, 8)));
    }

    private void clearForm() {
        selectedMedicineId = -1;
        nameField.setText("");
        companyField.setText("");
        typeField.setText("");
        priceField.setText("");
        quantityField.setText("");
        reorderField.setText("");
        expiryField.setText("");
        supplierIdField.setText("");
        table.clearSelection();
        statusLabel.setText(" ");
    }

    private void handleAdd() {
        Medicine m = readFormOrShowError();
        if (m == null) return;

        try {
            medicineDAO.add(m);
            refreshTable();
            clearForm();
        } catch (SQLException e) {
            statusLabel.setText("Failed to add medicine: " + e.getMessage());
        }
    }

    private void handleUpdate() {
        if (selectedMedicineId == -1) {
            statusLabel.setText("Select a row to update first.");
            return;
        }
        Medicine m = readFormOrShowError();
        if (m == null) return;
        m.setMedicineId(selectedMedicineId);

        try {
            medicineDAO.update(m);
            refreshTable();
            clearForm();
        } catch (SQLException e) {
            statusLabel.setText("Failed to update medicine: " + e.getMessage());
        }
    }

    private void handleDelete() {
        if (selectedMedicineId == -1) {
            statusLabel.setText("Select a row to delete first.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete this medicine? This cannot be undone.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            medicineDAO.delete(selectedMedicineId);
            refreshTable();
            clearForm();
        } catch (SQLException e) {
            statusLabel.setText("Failed to delete medicine: " + e.getMessage());
        }
    }

    /** Validates and builds a Medicine from the form, or shows an error and returns null. */
    private Medicine readFormOrShowError() {
        try {
            String name = nameField.getText().trim();
            String company = companyField.getText().trim();
            String type = typeField.getText().trim();
            BigDecimal price = new BigDecimal(priceField.getText().trim());
            int quantity = Integer.parseInt(quantityField.getText().trim());
            int reorderLevel = Integer.parseInt(reorderField.getText().trim());
            LocalDate expiry = LocalDate.parse(expiryField.getText().trim());
            int supplierId = Integer.parseInt(supplierIdField.getText().trim());

            if (name.isEmpty()) {
                statusLabel.setText("Name is required.");
                return null;
            }

            Medicine m = new Medicine();
            m.setName(name);
            m.setCompany(company);
            m.setMedicineType(type);
            m.setPrice(price);
            m.setQuantityInStock(quantity);
            m.setReorderLevel(reorderLevel);
            m.setExpiryDate(expiry);
            m.setSupplierId(supplierId);
            return m;

        } catch (NumberFormatException e) {
            statusLabel.setText("Price, quantity, reorder level and supplier ID must be numbers.");
            return null;
        } catch (DateTimeParseException e) {
            statusLabel.setText("Expiry date must be in yyyy-MM-dd format, e.g. 2026-12-31.");
            return null;
        }
    }
}