package ch.hearc.jee2025.bechirjeespringproject.cart;

import ch.hearc.jee2025.bechirjeespringproject.beer.Beer;
import ch.hearc.jee2025.bechirjeespringproject.beer.BeerService;
import ch.hearc.jee2025.bechirjeespringproject.cart_item.CartItem;
import ch.hearc.jee2025.bechirjeespringproject.sales_log.SalesLog;
import ch.hearc.jee2025.bechirjeespringproject.sales_log.SalesLogItem;
import ch.hearc.jee2025.bechirjeespringproject.sales_log.SalesLogService;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/carts")
public class CartController {

    public CartController(CartService cartService, BeerService beerService, SalesLogService salesLogService) {
        this.cartService = cartService;
        this.beerService = beerService;
        this.salesLogService = salesLogService;
    }

    // CREATE/UPDATE
    @PostMapping
    public Cart create(@RequestBody Cart cart) {
        if (cart.getItems() != null) {
            for (CartItem item : cart.getItems()) {
                attachManagedBeerAndValidateStock(item);
                item.setCart(cart);
            }
        }
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
                attachManagedBeerAndValidateStock(item);
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

        @PostMapping("/{id}/checkout")
        @Transactional
        public Map<String, Object> checkout(@PathVariable Long id) {
        Cart cart = cartService.findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Cart not found with id: " + id
            ));

        Map<Long, Integer> quantityByBeerId = new HashMap<>();
        Map<Long, Beer> managedBeerById = new HashMap<>();
        for (CartItem item : cart.getItems()) {
            attachManagedBeerAndValidateStock(item);
            Long beerId = item.getBeer().getId();
            quantityByBeerId.merge(beerId, item.getQuantity(), Integer::sum);
        }

        for (Map.Entry<Long, Integer> entry : quantityByBeerId.entrySet()) {
            Long beerId = entry.getKey();
            int requestedQuantity = entry.getValue();

            Beer managedBeer = beerService.findById(beerId)
                .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Beer not found with id: " + beerId
                ));

            if (requestedQuantity > managedBeer.getStock()) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Not enough stock for beer id: " + managedBeer.getId()
            );
            }

            managedBeer.setStock(managedBeer.getStock() - requestedQuantity);
            beerService.save(managedBeer);
            managedBeerById.put(beerId, managedBeer);
        }

        double total = cart.getTotalPrice();

        SalesLog salesLog = new SalesLog();
        salesLog.setCartId(id);
        salesLog.setTotal(total);
        salesLog.setCreatedAt(LocalDateTime.now());

        List<SalesLogItem> logItems = new ArrayList<>();
        for (Map.Entry<Long, Integer> entry : quantityByBeerId.entrySet()) {
            Long beerId = entry.getKey();
            int quantity = entry.getValue();
            Beer managedBeer = managedBeerById.get(beerId);
            SalesLogItem logItem = new SalesLogItem(
                    salesLog,
                    managedBeer.getId(),
                    managedBeer.getName(),
                    managedBeer.getPrice(),
                    quantity
            );
            logItems.add(logItem);
        }
        salesLog.setItems(logItems);
        salesLogService.save(salesLog);

        cartService.deleteById(id);

        return Map.of(
            "message", "Checkout successful",
            "cartId", id,
            "total", total
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

    private void attachManagedBeerAndValidateStock(CartItem item) {
        if (item.getBeer() == null || item.getBeer().getId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "CartItem must have a valid beer with id"
            );
        }

        if (item.getQuantity() <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "quantity must be greater than 0"
            );
        }

        Beer managedBeer = beerService.findById(item.getBeer().getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Beer not found with id: " + item.getBeer().getId()
                ));

        if (item.getQuantity() > managedBeer.getStock()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Not enough stock for beer id: " + managedBeer.getId()
            );
        }

        item.setBeer(managedBeer);
    }

    private final CartService cartService;
    private final BeerService beerService;
    private final SalesLogService salesLogService;
}
