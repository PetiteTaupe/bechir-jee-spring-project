package ch.hearc.jee2025.bechirjeespringproject.cart_item;

import ch.hearc.jee2025.bechirjeespringproject.beer.Beer;
import ch.hearc.jee2025.bechirjeespringproject.cart.Cart;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

@Entity
public class CartItem {

    protected CartItem() {

    }

    public CartItem(Cart cart, Beer beer, int quantity) {
        this.cart = cart;
        this.beer = beer;
        this.quantity = quantity;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Cart getCart() {
        return cart;
    }

    public void setCart(Cart cart) {
        this.cart = cart;
    }

    public Beer getBeer() {
        return beer;
    }

    public void setBeer(Beer beer) {
        this.beer = beer;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cart_id", nullable = false)
    @JsonIgnoreProperties("items")
    private Cart cart;

    @ManyToOne
    @JoinColumn(name = "beer_id", nullable = false)
    private Beer beer;

    private int quantity;
}
