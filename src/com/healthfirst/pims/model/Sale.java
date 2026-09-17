package com.healthfirst.pims.model;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class Sale {

    private int saleId;
    private Timestamp saleDate;
    private BigDecimal totalAmount;
    private int userId;
    private List<SaleItem> items = new ArrayList<>();

    public Sale() {
    }

    public Sale(int saleId, Timestamp saleDate, BigDecimal totalAmount, int userId) {
        this.saleId = saleId;
        this.saleDate = saleDate;
        this.totalAmount = totalAmount;
        this.userId = userId;
    }

    public int getSaleId() { return saleId; }
    public void setSaleId(int saleId) { this.saleId = saleId; }

    public Timestamp getSaleDate() { return saleDate; }
    public void setSaleDate(Timestamp saleDate) { this.saleDate = saleDate; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public List<SaleItem> getItems() { return items; }
    public void addItem(SaleItem item) { this.items.add(item); }
}