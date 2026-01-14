package ch.hearc.jee2025.bechirjeespringproject.cart_item;

import ch.hearc.jee2025.bechirjeespringproject.beer.Beer;
import ch.hearc.jee2025.bechirjeespringproject.beer.BeerService;
import ch.hearc.jee2025.bechirjeespringproject.cart.Cart;
import ch.hearc.jee2025.bechirjeespringproject.cart.CartService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
    @RequestMapping("/cart_items")
public class CartItemController {

    public CartItemController(CartItemService cartItemService, CartService cartService, BeerService beerService) {
        this.cartItemService = cartItemService;
        this.cartService = cartService;
        this.beerService = beerService;
    }

    // CREATE/UPDATE
    @PostMapping
    public CartItem create(@RequestBody CartItem cartItem) {

        attachManagedEntities(cartItem);
        return cartItemService.save(cartItem);
    }

    @PutMapping("/{id}")
    public CartItem update(@PathVariable Long id, @RequestBody CartItem cartItem) {
        if (cartItemService.findById(id).isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Cart item not found with id: " + id
            );
        }
        cartItem.setId(id);
        attachManagedEntities(cartItem);
        return cartItemService.save(cartItem);
    }

    private void attachManagedEntities(CartItem item) {

        // Cart
        if (item.getCart() == null || item.getCart().getId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "cart.id is required"
            );
        }

        Cart managedCart = cartService.findById(item.getCart().getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Cart not found with id: " + item.getCart().getId()
                ));

        // Beer
        if (item.getBeer() == null || item.getBeer().getId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "beer.id is required"
            );
        }

        Beer managedBeer = beerService.findById(item.getBeer().getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Beer not found with id: " + item.getBeer().getId()
                ));

        // quantity
        if (item.getQuantity() <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "quantity must be greater than 0"
            );
        }

        item.setCart(managedCart);
        item.setBeer(managedBeer);
    }


    // READ
    @GetMapping
    public Iterable<CartItem> getAll() {
        return cartItemService.findAll();
    }

    @GetMapping("/{id}")
    public CartItem getById(@PathVariable Long id) {
        return cartItemService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Cart item not found with id: " + id
                ));
    }

    // DELETE
    @DeleteMapping("/{id}")
    public Map<String, String> delete(@PathVariable Long id) {
        try {
            cartItemService.deleteById(id);
            return Map.of(
                    "message", "Cart item deleted successfully",
                    "id", id.toString()
            );
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    private final CartItemService cartItemService;
    private final CartService cartService;
    private final BeerService beerService;
}
