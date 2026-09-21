package com.healthfirst.pims.ui;

import com.healthfirst.pims.model.Sale;
import com.healthfirst.pims.model.SaleItem;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;

/**
 * Read-only receipt window shown after a successful checkout.
 * "Print" here just triggers the system print dialog on the text area,
 * which satisfies the brief's "print/save" requirement without needing
 * a PDF library.
 */
public class BillWindow extends JFrame {

    public BillWindow(Sale sale, int saleId) {
        setTitle("Bill - Sale #" + saleId);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(400, 450);
        setLocationRelativeTo(null);

        JTextArea billArea = new JTextArea();
        billArea.setEditable(false);
        billArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        billArea.setText(buildBillText(sale, saleId));

        JButton printButton = new JButton("Print");
        printButton.addActionListener(e -> {
            try {
                billArea.print();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Printing failed: " + ex.getMessage());
            }
        });

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttons.add(printButton);
        buttons.add(closeButton);

        setLayout(new BorderLayout());
        add(new JScrollPane(billArea), BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
    }

    private String buildBillText(Sale sale, int saleId) {
        StringBuilder sb = new StringBuilder();
        sb.append("======================================\n");
        sb.append("        HealthFirst Pharmacy\n");
        sb.append("======================================\n");
        sb.append("Bill No: ").append(saleId).append("\n");
        sb.append("Date:    ").append(new SimpleDateFormat("yyyy-MM-dd HH:mm").format(sale.getSaleDate())).append("\n");
        sb.append("--------------------------------------\n");

        for (SaleItem item : sale.getItems()) {
            sb.append(String.format("%-20s x%-3d R%8.2f%n",
                    item.getMedicineName(), item.getQuantitySold(), item.getLineTotal()));
        }

        sb.append("--------------------------------------\n");
        sb.append(String.format("TOTAL:                       R%8.2f%n", sale.getTotalAmount()));
        sb.append("======================================\n");
        sb.append("      Thank you for your purchase!\n");
        return sb.toString();
    }
}