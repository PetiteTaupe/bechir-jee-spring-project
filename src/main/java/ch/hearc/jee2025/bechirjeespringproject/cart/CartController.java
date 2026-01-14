package ch.hearc.jee2025.bechirjeespringproject.cart;

import ch.hearc.jee2025.bechirjeespringproject.cart_item.CartItem;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/carts")
public class CartController {

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    // CREATE/UPDATE
    @PostMapping
    public Cart create(@RequestBody Cart cart) {
        return cartService.save(cart);
    }

    @PutMapping("/{id}")
    public Cart update(@PathVariable Long id, @RequestBody Cart cartUpdates) {
        Cart existingCart = cartService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Cart not found with id: " + id
                ));

        existingCart.getItems().clear();
        if (cartUpdates.getItems() != null) {
            for (CartItem item : cartUpdates.getItems()) {
                if (item.getBeer() == null || item.getBeer().getId() == null) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "CartItem must have a valid beer with id"
                    );
                }
                item.setCart(existingCart);
                existingCart.getItems().add(item);
            }
        }

        return cartService.save(existingCart);
    }


    // READ
    @GetMapping
    public Iterable<Cart> getAll() {
        return cartService.findAll();
    }

    @GetMapping("/{id}")
    public Cart getById(@PathVariable Long id) {
        return cartService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Cart not found with id: " + id
                ));
    }

    @GetMapping("/{id}/total")
    public Map<String, Object> getTotal(@PathVariable Long id) {
        Cart cart = cartService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Cart not found with id: " + id
                ));
        return Map.of(
                "cartId", cart.getId(),
                "total", cart.getTotalPrice()
        );
    }


    // DELETE
    @DeleteMapping("/{id}")
    public Map<String, String> delete(@PathVariable Long id) {
        try {
            cartService.deleteById(id);
            return Map.of(
                    "message", "Cart deleted successfully",
                    "id", id.toString()
            );
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }


    private final CartService cartService;
}
