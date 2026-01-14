package ch.hearc.jee2025.bechirjeespringproject.cart_item;

import java.util.Optional;

public interface CartItemService {

    CartItem save(CartItem cartItem);
    Optional<CartItem> findById(Long id);
    Iterable<CartItem> findAll();
    void deleteById(Long id);
}
