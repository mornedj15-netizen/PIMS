package com.healthfirst.pims.ui;

import com.healthfirst.pims.dao.UserDAO;
import com.healthfirst.pims.model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class ManageUsersPanel extends JPanel {

    private final UserDAO userDAO = new UserDAO();

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"ID", "Username", "Role", "Full Name"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(tableModel);

    private final JTextField usernameField = new JTextField(15);
    private final JPasswordField passwordField = new JPasswordField(15);
    private final JComboBox<String> roleCombo = new JComboBox<>(new String[]{"Cashier", "Admin"});
    private final JTextField fullNameField = new JTextField(15);

    private final JLabel statusLabel = new JLabel(" ");
    private int selectedUserId = -1;

    public ManageUsersPanel() {
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
        form.setBorder(BorderFactory.createTitledBorder("Cashier Account Details"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        addField(form, gbc, row++, "Username:", usernameField);
        addField(form, gbc, row++, "Password:", passwordField);
        addField(form, gbc, row++, "Role:", roleCombo);
        addField(form, gbc, row++, "Full Name:", fullNameField);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addButton = new JButton("Add");
        JButton deleteButton = new JButton("Delete Selected");
        JButton clearButton = new JButton("Clear Form");

        addButton.addActionListener(e -> handleAdd());
        deleteButton.addActionListener(e -> handleDelete());
        clearButton.addActionListener(e -> clearForm());

        buttons.add(addButton);
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

    private void addField(JPanel form, GridBagConstraints gbc, int row, String label, JComponent field) {
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
            List<User> users = userDAO.getAll();
            for (User u : users) {
                tableModel.addRow(new Object[]{u.getUserId(), u.getUsername(), u.getRole(), u.getFullName()});
            }
            statusLabel.setText(" ");
        } catch (SQLException e) {
            statusLabel.setText("Failed to load users: " + e.getMessage());
        }
    }

    private void loadSelectedRowIntoForm() {
        int row = table.getSelectedRow();
        if (row == -1) return;

        selectedUserId = (int) tableModel.getValueAt(row, 0);
        usernameField.setText(String.valueOf(tableModel.getValueAt(row, 1)));
        roleCombo.setSelectedItem(String.valueOf(tableModel.getValueAt(row, 2)));
        fullNameField.setText(String.valueOf(tableModel.getValueAt(row, 3)));
        passwordField.setText(""); // never show existing passwords back
    }

    private void clearForm() {
        selectedUserId = -1;
        usernameField.setText("");
        passwordField.setText("");
        roleCombo.setSelectedIndex(0);
        fullNameField.setText("");
        table.clearSelection();
        statusLabel.setText(" ");
    }

    private void handleAdd() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String role = (String) roleCombo.getSelectedItem();
        String fullName = fullNameField.getText().trim();

        if (username.isEmpty() || password.isEmpty() || fullName.isEmpty()) {
            statusLabel.setText("Username, password and full name are all required.");
            return;
        }

        User u = new User();
        u.setUsername(username);
        u.setPassword(password);
        u.setRole(role);
        u.setFullName(fullName);

        try {
            userDAO.add(u);
            refreshTable();
            clearForm();
        } catch (SQLException e) {
            statusLabel.setText("Failed to add user (username may already exist): " + e.getMessage());
        }
    }

    private void handleDelete() {
        if (selectedUserId == -1) {
            statusLabel.setText("Select a row to delete first.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete this user account? This cannot be undone.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            userDAO.delete(selectedUserId);
            refreshTable();
            clearForm();
        } catch (SQLException e) {
            statusLabel.setText("Failed to delete user: " + e.getMessage());
        }
    }
}