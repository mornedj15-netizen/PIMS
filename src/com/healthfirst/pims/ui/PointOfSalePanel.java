package com.healthfirst.pims.ui;

import com.healthfirst.pims.dao.MedicineDAO;
import com.healthfirst.pims.dao.SaleDAO;
import com.healthfirst.pims.model.Medicine;
import com.healthfirst.pims.model.Sale;
import com.healthfirst.pims.model.SaleItem;
import com.healthfirst.pims.model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

/**
 * Cashier POS screen: pick a medicine, add it to the cart, adjust quantity,
 * and check out to record the sale via SaleDAO and pop up a bill window.
 */
public class PointOfSalePanel extends JPanel {

    private final MedicineDAO medicineDAO = new MedicineDAO();
    private final SaleDAO saleDAO = new SaleDAO();
    private final User currentUser;

    private final JComboBox<Medicine> medicineCombo = new JComboBox<>();
    private final JSpinner quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));

    private final DefaultTableModel cartModel = new DefaultTableModel(
            new String[]{"Medicine ID", "Name", "Qty", "Unit Price", "Line Total"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable cartTable = new JTable(cartModel);

    private final JLabel totalLabel = new JLabel("Total: R0.00");
    private final JLabel statusLabel = new JLabel(" ");

    public PointOfSalePanel(User currentUser) {
        this.currentUser = currentUser;
        setLayout(new BorderLayout(10, 10));

        add(buildAddItemPanel(), BorderLayout.NORTH);
        add(new JScrollPane(cartTable), BorderLayout.CENTER);
        add(buildActionsPanel(), BorderLayout.SOUTH);

        loadMedicinesIntoCombo();
    }

    private JPanel buildAddItemPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createTitledBorder("Add Item"));

        panel.add(new JLabel("Medicine:"));
        panel.add(medicineCombo);
        panel.add(new JLabel("Qty:"));
        panel.add(quantitySpinner);

        JButton addToCartButton = new JButton("Add to Cart");
        addToCartButton.addActionListener(e -> handleAddToCart());
        panel.add(addToCartButton);

        return panel;
    }

    private JPanel buildActionsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton removeButton = new JButton("Remove Selected");
        JButton clearCartButton = new JButton("Clear Cart");
        JButton checkoutButton = new JButton("Checkout");

        removeButton.addActionListener(e -> handleRemoveSelected());
        clearCartButton.addActionListener(e -> handleClearCart());
        checkoutButton.addActionListener(e -> handleCheckout());

        buttons.add(removeButton);
        buttons.add(clearCartButton);
        buttons.add(checkoutButton);

        totalLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        right.add(totalLabel);

        JPanel top = new JPanel(new BorderLayout());
        top.add(buttons, BorderLayout.WEST);
        top.add(right, BorderLayout.EAST);

        statusLabel.setForeground(Color.RED);
        panel.add(top, BorderLayout.NORTH);
        panel.add(statusLabel, BorderLayout.SOUTH);

        return panel;
    }

    private void loadMedicinesIntoCombo() {
        try {
            medicineCombo.removeAllItems();
            for (Medicine m : medicineDAO.getAll()) {
                medicineCombo.addItem(m);
            }
        } catch (SQLException e) {
            statusLabel.setText("Failed to load medicines: " + e.getMessage());
        }
    }

    private void handleAddToCart() {
        Medicine selected = (Medicine) medicineCombo.getSelectedItem();
        if (selected == null) {
            statusLabel.setText("No medicine selected.");
            return;
        }

        int quantity = (int) quantitySpinner.getValue();
        if (quantity > selected.getQuantityInStock()) {
            statusLabel.setText("Only " + selected.getQuantityInStock() + " in stock for " + selected.getName() + ".");
            return;
        }

        // If the medicine is already in the cart, increase its quantity instead of adding a duplicate row.
        for (int row = 0; row < cartModel.getRowCount(); row++) {
            int existingId = (int) cartModel.getValueAt(row, 0);
            if (existingId == selected.getMedicineId()) {
                int existingQty = (int) cartModel.getValueAt(row, 2);
                int newQty = existingQty + quantity;
                if (newQty > selected.getQuantityInStock()) {
                    statusLabel.setText("Cannot add more - would exceed available stock.");
                    return;
                }
                cartModel.setValueAt(newQty, row, 2);
                cartModel.setValueAt(selected.getPrice().multiply(BigDecimal.valueOf(newQty)), row, 4);
                recalculateTotal();
                statusLabel.setText(" ");
                return;
            }
        }

        BigDecimal lineTotal = selected.getPrice().multiply(BigDecimal.valueOf(quantity));
        cartModel.addRow(new Object[]{
                selected.getMedicineId(), selected.getName(), quantity, selected.getPrice(), lineTotal
        });
        recalculateTotal();
        statusLabel.setText(" ");
    }

    private void handleRemoveSelected() {
        int row = cartTable.getSelectedRow();
        if (row == -1) {
            statusLabel.setText("Select a cart row to remove first.");
            return;
        }
        cartModel.removeRow(row);
        recalculateTotal();
    }

    private void handleClearCart() {
        cartModel.setRowCount(0);
        recalculateTotal();
        statusLabel.setText(" ");
    }

    private void recalculateTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (int row = 0; row < cartModel.getRowCount(); row++) {
            total = total.add((BigDecimal) cartModel.getValueAt(row, 4));
        }
        totalLabel.setText("Total: R" + total.setScale(2, java.math.RoundingMode.HALF_UP));
    }

    private void handleCheckout() {
        if (cartModel.getRowCount() == 0) {
            statusLabel.setText("Cart is empty.");
            return;
        }

        Sale sale = new Sale();
        sale.setUserId(currentUser.getUserId());
        sale.setSaleDate(new Timestamp(System.currentTimeMillis()));

        BigDecimal total = BigDecimal.ZERO;
        for (int row = 0; row < cartModel.getRowCount(); row++) {
            int medicineId = (int) cartModel.getValueAt(row, 0);
            String name = String.valueOf(cartModel.getValueAt(row, 1));
            int qty = (int) cartModel.getValueAt(row, 2);
            BigDecimal unitPrice = (BigDecimal) cartModel.getValueAt(row, 3);

            sale.addItem(new SaleItem(medicineId, name, qty, unitPrice));
            total = total.add(unitPrice.multiply(BigDecimal.valueOf(qty)));
        }
        sale.setTotalAmount(total);

        try {
            int saleId = saleDAO.recordSale(sale);
            new BillWindow(sale, saleId).setVisible(true);

            handleClearCart();
            loadMedicinesIntoCombo(); // refresh stock levels shown in the dropdown

        } catch (SQLException e) {
            statusLabel.setText("Checkout failed: " + e.getMessage());
        }
    }
}