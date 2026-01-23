package ch.hearc.jee2025.bechirjeespringproject.sales_log;

import jakarta.persistence.*;

@Entity
public class SalesLogItem {

    public SalesLogItem() {

    }

    public SalesLogItem(SalesLog salesLog, Long beerId, String beerName, double unitPrice, int quantity) {
        this.salesLog = salesLog;
        this.beerId = beerId;
        this.beerName = beerName;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        updateLineTotal();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public SalesLog getSalesLog() {
        return salesLog;
    }

    public void setSalesLog(SalesLog salesLog) {
        this.salesLog = salesLog;
    }

    public Long getBeerId() {
        return beerId;
    }

    public void setBeerId(Long beerId) {
        this.beerId = beerId;
    }

    public String getBeerName() {
        return beerName;
    }

    public void setBeerName(String beerName) {
        this.beerName = beerName;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
        updateLineTotal();
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
        updateLineTotal();
    }

    public double getLineTotal() {
        return lineTotal;
    }

    public void setLineTotal(double lineTotal) {
        this.lineTotal = lineTotal;
    }

    private void updateLineTotal() {
        this.lineTotal = unitPrice * quantity;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sales_log_id", nullable = false)
    private SalesLog salesLog;

    private Long beerId;

    private String beerName;

    private double unitPrice;

    private int quantity;

    private double lineTotal;
}