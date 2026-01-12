package ch.hearc.jee2025.bechirjeespringproject.beer;

import ch.hearc.jee2025.bechirjeespringproject.brewery.Brewery;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

@Entity
public class Beer {

    protected Beer() {

    }

    public Beer(String name, double price, int stock) {
        this.name = name;
        this.price = price;
        this.stock = stock;
    }

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public Brewery getBrewery() { return brewery; }

    public void setBrewery(Brewery brewery) { this.brewery = brewery; }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private double price;

    private int stock;

    @ManyToOne
    @JoinColumn(name = "brewery_id", nullable = false)
    @JsonIgnoreProperties("beers")
    private Brewery brewery;
}
