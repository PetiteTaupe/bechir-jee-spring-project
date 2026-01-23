package ch.hearc.jee2025.bechirjeespringproject.sales_log;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
public class SalesLog {

    public SalesLog() {
        this.items = new ArrayList<>();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCartId() {
        return cartId;
    }

    public void setCartId(Long cartId) {
        this.cartId = cartId;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<SalesLogItem> getItems() {
        return items;
    }

    public void setItems(List<SalesLogItem> items) {
        this.items = items;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long cartId;

    private double total;

    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "salesLog", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("salesLog")
    private List<SalesLogItem> items = new ArrayList<>();
}