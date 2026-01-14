package ch.hearc.jee2025.bechirjeespringproject.cart_item;

import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CartItemServiceImpl implements CartItemService{

    public CartItemServiceImpl(CartItemRepository cartItemRepository) {
        this.cartItemRepository = cartItemRepository;
    }

    @Override
    public CartItem save(CartItem cartItem) {
        return cartItemRepository.save(cartItem);
    }

    @Override
    public Optional<CartItem> findById(Long id) {
        return cartItemRepository.findById(id);
    }

    @Override
    public Iterable<CartItem> findAll() {
        return cartItemRepository.findAll();
    }

    @Override
    public void deleteById(Long id) {
        CartItem cartItem = cartItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cart item not found with id: " + id));
        cartItemRepository.delete(cartItem);
    }

    private final CartItemRepository cartItemRepository;
}
