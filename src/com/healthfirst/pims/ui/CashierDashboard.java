package com.healthfirst.pims.ui;

import com.healthfirst.pims.model.User;

import javax.swing.*;
import java.awt.*;

/**
 * Cashier landing screen: POS interface for making sales, plus a
 * stock-check tool (no add/edit rights - that's Admin-only).
 *
 * TODO: build the POS cart, checkout flow, and bill window.
 */
public class CashierDashboard extends JFrame {

    private final User currentUser;

    public CashierDashboard(User currentUser) {
        this.currentUser = currentUser;

        setTitle("HealthFirst PIMS - Cashier (" + currentUser.getFullName() + ")");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Point of Sale", new PointOfSalePanel(currentUser));
        tabs.addTab("Stock Check", buildPlaceholder("Read-only medicine lookup goes here"));

        add(tabs);
    }

    private JPanel buildPlaceholder(String message) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel(message, SwingConstants.CENTER), BorderLayout.CENTER);
        return panel;
    }
}