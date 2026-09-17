package com.healthfirst.pims.ui;

import com.healthfirst.pims.dao.SupplierDAO;
import com.healthfirst.pims.model.Supplier;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class ManageSuppliersPanel extends JPanel {

    private final SupplierDAO supplierDAO = new SupplierDAO();

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"ID", "Name", "Contact Person", "Phone", "Email", "Address"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(tableModel);

    private final JTextField nameField = new JTextField(15);
    private final JTextField contactField = new JTextField(15);
    private final JTextField phoneField = new JTextField(12);
    private final JTextField emailField = new JTextField(15);
    private final JTextField addressField = new JTextField(20);

    private final JLabel statusLabel = new JLabel(" ");
    private int selectedSupplierId = -1;

    public ManageSuppliersPanel() {
        setLayout(new BorderLayout(10, 10));

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) loadSelectedRowIntoForm();
        });
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(buildFormPanel(), BorderLayout.SOUTH);

        refreshTable();
    }

    private JPanel buildFormPanel() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Supplier Details"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        addField(form, gbc, row++, "Name:", nameField);
        addField(form, gbc, row++, "Contact Person:", contactField);
        addField(form, gbc, row++, "Phone:", phoneField);
        addField(form, gbc, row++, "Email:", emailField);
        addField(form, gbc, row++, "Address:", addressField);

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
            List<Supplier> suppliers = supplierDAO.getAll();
            for (Supplier s : suppliers) {
                tableModel.addRow(new Object[]{
                        s.getSupplierId(), s.getName(), s.getContactPerson(),
                        s.getPhone(), s.getEmail(), s.getAddress()
                });
            }
            statusLabel.setText(" ");
        } catch (SQLException e) {
            statusLabel.setText("Failed to load suppliers: " + e.getMessage());
        }
    }

    private void loadSelectedRowIntoForm() {
        int row = table.getSelectedRow();
        if (row == -1) return;

        selectedSupplierId = (int) tableModel.getValueAt(row, 0);
        nameField.setText(String.valueOf(tableModel.getValueAt(row, 1)));
        contactField.setText(String.valueOf(tableModel.getValueAt(row, 2)));
        phoneField.setText(String.valueOf(tableModel.getValueAt(row, 3)));
        emailField.setText(String.valueOf(tableModel.getValueAt(row, 4)));
        addressField.setText(String.valueOf(tableModel.getValueAt(row, 5)));
    }

    private void clearForm() {
        selectedSupplierId = -1;
        nameField.setText("");
        contactField.setText("");
        phoneField.setText("");
        emailField.setText("");
        addressField.setText("");
        table.clearSelection();
        statusLabel.setText(" ");
    }

    private void handleAdd() {
        Supplier s = readFormOrShowError();
        if (s == null) return;

        try {
            supplierDAO.add(s);
            refreshTable();
            clearForm();
        } catch (SQLException e) {
            statusLabel.setText("Failed to add supplier: " + e.getMessage());
        }
    }

    private void handleUpdate() {
        if (selectedSupplierId == -1) {
            statusLabel.setText("Select a row to update first.");
            return;
        }
        Supplier s = readFormOrShowError();
        if (s == null) return;
        s.setSupplierId(selectedSupplierId);

        try {
            supplierDAO.update(s);
            refreshTable();
            clearForm();
        } catch (SQLException e) {
            statusLabel.setText("Failed to update supplier: " + e.getMessage());
        }
    }

    private void handleDelete() {
        if (selectedSupplierId == -1) {
            statusLabel.setText("Select a row to delete first.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete this supplier? Medicines linked to it may fail to delete first.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            supplierDAO.delete(selectedSupplierId);
            refreshTable();
            clearForm();
        } catch (SQLException e) {
            statusLabel.setText("Failed to delete supplier: " + e.getMessage()
                    + " (medicines may still reference this supplier)");
        }
    }

    private Supplier readFormOrShowError() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            statusLabel.setText("Name is required.");
            return null;
        }

        Supplier s = new Supplier();
        s.setName(name);
        s.setContactPerson(contactField.getText().trim());
        s.setPhone(phoneField.getText().trim());
        s.setEmail(emailField.getText().trim());
        s.setAddress(addressField.getText().trim());
        return s;
    }
}