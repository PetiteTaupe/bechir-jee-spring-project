package ch.hearc.jee2025.bechirjeespringproject.cart;

import java.util.Optional;

public interface CartService {

    Cart save(Cart cart);
    Optional<Cart> findById(Long id);
    Iterable<Cart> findAll();
    void deleteById(Long id);
}
