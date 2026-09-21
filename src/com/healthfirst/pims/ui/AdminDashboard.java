package com.healthfirst.pims.ui;

import com.healthfirst.pims.model.User;

import javax.swing.*;
import java.awt.*;

/**
 * Admin landing screen. Holds tabs for Medicine Management, Supplier
 * Management, User Management, and Reports.
 *
 * TODO: build out each tab's contents (tables + forms backed by the DAOs).
 * This class currently just lays out the tab structure so you have a
 * working shell to run and commit as your starting point.
 */
public class AdminDashboard extends JFrame {

    private final User currentUser;

    public AdminDashboard(User currentUser) {
        this.currentUser = currentUser;

        setTitle("HealthFirst PIMS - Admin Dashboard (" + currentUser.getFullName() + ")");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Manage Medicines", new ManageMedicinesPanel());
        tabs.addTab("Manage Suppliers", new ManageSuppliersPanel());
        tabs.addTab("Manage Users", new ManageUsersPanel());
        tabs.addTab("Reports", new ReportsPanel());

        add(tabs);
    }

    private JPanel buildPlaceholder(String message) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel(message, SwingConstants.CENTER), BorderLayout.CENTER);
        return panel;
    }
}