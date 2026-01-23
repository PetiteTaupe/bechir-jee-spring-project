package ch.hearc.jee2025.bechirjeespringproject.cart;

import ch.hearc.jee2025.bechirjeespringproject.beer.Beer;
import ch.hearc.jee2025.bechirjeespringproject.beer.BeerService;
import ch.hearc.jee2025.bechirjeespringproject.cart_item.CartItem;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Map;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CartControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private CartService cartService;
    private BeerService beerService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        cartService = mock(CartService.class);
        beerService = mock(BeerService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new CartController(cartService, beerService)).build();
    }

    @Test
    // Vérifie que GET /carts retourne 200 et une liste JSON.
    void getAll_returns200() throws Exception {
        Cart c1 = new Cart();
        c1.setId(10L);
        Cart c2 = new Cart();
        c2.setId(11L);

        when(cartService.findAll()).thenReturn(List.of(c1, c2));

        mockMvc.perform(get("/carts"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(10)));

        verify(cartService).findAll();
        verifyNoMoreInteractions(cartService);
    }

    @Test
    // Vérifie que GET /carts/{id} retourne 200 quand le panier existe.
    void getById_whenFound_returns200() throws Exception {
        Cart cart = new Cart();
        cart.setId(10L);
        when(cartService.findById(10L)).thenReturn(Optional.of(cart));

        mockMvc.perform(get("/carts/10"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(10)));

        verify(cartService).findById(10L);
        verifyNoMoreInteractions(cartService);
    }

    @Test
    // Vérifie que GET /carts/{id} retourne 404 quand le panier n'existe pas.
    void getById_whenMissing_returns404() throws Exception {
        when(cartService.findById(10L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/carts/10"))
                .andExpect(status().isNotFound());

        verify(cartService).findById(10L);
        verifyNoMoreInteractions(cartService);
    }

    @Test
    // Vérifie que GET /carts/{id}/total retourne 200 et le total calculé.
    void getTotal_returns200AndTotal() throws Exception {
        Beer beer = new Beer("Test", 2.5, 10);
        beer.setId(1L);

        Cart cart = new Cart();
        cart.setId(10L);
        cart.getItems().add(new CartItem(cart, beer, 3));

        when(cartService.findById(10L)).thenReturn(Optional.of(cart));

        mockMvc.perform(get("/carts/10/total"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.cartId", is(10)))
                .andExpect(jsonPath("$.total", is(7.5)));

        verify(cartService).findById(10L);
        verifyNoMoreInteractions(cartService);
    }

    @Test
    // Vérifie que POST /carts sauvegarde un panier.
    void create_savesCart() throws Exception {
        Cart cart = new Cart();
        when(cartService.save(any(Cart.class))).thenAnswer(inv -> inv.getArgument(0));

        mockMvc.perform(post("/carts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cart)))
                .andExpect(status().isOk());

        verify(cartService).save(any(Cart.class));
        verifyNoMoreInteractions(cartService);
    }

    @Test
    // Vérifie que PUT /carts/{id} retourne 404 si le panier n'existe pas.
    void update_whenMissing_returns404() throws Exception {
        when(cartService.findById(10L)).thenReturn(Optional.empty());

        mockMvc.perform(put("/carts/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isNotFound());

        verify(cartService).findById(10L);
        verifyNoMoreInteractions(cartService);
    }

    @Test
    // Vérifie que PUT /carts/{id} retourne 400 si un item n'a pas de beer.id.
    void update_missingBeerId_returns400() throws Exception {
        Cart existing = new Cart();
        existing.setId(10L);
        when(cartService.findById(10L)).thenReturn(Optional.of(existing));

        String payload = "{\"items\":[{\"quantity\":2,\"beer\":{}}]}";

        mockMvc.perform(put("/carts/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());

        verify(cartService).findById(10L);
        verifyNoMoreInteractions(cartService);
    }

    @Test
    // Vérifie que PUT /carts/{id} remplace les items, fixe item.cart, puis sauvegarde.
    void update_replacesItems_setsCartOnItems_andSaves() throws Exception {
        Cart existing = new Cart();
        existing.setId(10L);

        Beer oldBeer = new Beer("Old", 1.0, 1);
        oldBeer.setId(99L);
        existing.getItems().add(new CartItem(existing, oldBeer, 1));

        when(cartService.findById(10L)).thenReturn(Optional.of(existing));
        when(cartService.save(any(Cart.class))).thenAnswer(inv -> inv.getArgument(0));

        Beer managedBeer = new Beer("New", 2.0, 10);
        managedBeer.setId(1L);
        when(beerService.findById(1L)).thenReturn(Optional.of(managedBeer));

        String payload = "{\"items\":[{\"quantity\":2,\"beer\":{\"id\":1}}]}";

        mockMvc.perform(put("/carts/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk());

        ArgumentCaptor<Cart> captor = ArgumentCaptor.forClass(Cart.class);
        verify(cartService).findById(10L);
        verify(cartService).save(captor.capture());
        verify(beerService).findById(1L);

        Cart saved = captor.getValue();
        assertEquals(10L, saved.getId());
        assertEquals(1, saved.getItems().size());
        assertSame(saved, saved.getItems().getFirst().getCart());
        assertEquals(1L, saved.getItems().getFirst().getBeer().getId());
        assertEquals(2, saved.getItems().getFirst().getQuantity());

        verifyNoMoreInteractions(cartService, beerService);
    }

    @Test
    // Vérifie que DELETE /carts/{id} retourne 404 si le panier n'existe pas.
    void delete_whenNotFound_returns404() throws Exception {
        doThrow(new RuntimeException("Cart not found with id: 10")).when(cartService).deleteById(10L);

        mockMvc.perform(delete("/carts/10"))
                .andExpect(status().isNotFound());

        verify(cartService).deleteById(10L);
        verifyNoMoreInteractions(cartService);
    }

    @Test
    // Vérifie que DELETE /carts/{id} retourne 200 et un message de succès.
    void delete_whenOk_returns200AndMessage() throws Exception {
        mockMvc.perform(delete("/carts/10"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message", is("Cart deleted successfully")))
                .andExpect(jsonPath("$.id", is("10")));

        verify(cartService).deleteById(10L);
        verifyNoMoreInteractions(cartService);
    }

    @Test
    // Vérifie que POST /carts/{id}/checkout retourne 404 si le panier n'existe pas.
    void checkout_whenMissing_returns404() throws Exception {
        when(cartService.findById(10L)).thenReturn(Optional.empty());

        mockMvc.perform(post("/carts/10/checkout"))
                .andExpect(status().isNotFound());

        verify(cartService).findById(10L);
        verifyNoMoreInteractions(cartService, beerService);
    }

    @Test
    // Vérifie que POST /carts/{id}/checkout décrémente le stock, supprime le panier, et retourne 200 + total.
    void checkout_whenOk_decrementsStock_deletesCart_andReturnsTotal() throws Exception {
        Beer beer = new Beer("Test", 2.5, 10);
        beer.setId(1L);

        Cart cart = new Cart();
        cart.setId(10L);
        cart.getItems().add(new CartItem(cart, beer, 3));

        when(cartService.findById(10L)).thenReturn(Optional.of(cart));
        when(beerService.findById(1L)).thenReturn(Optional.of(beer));
        when(beerService.save(any(Beer.class))).thenAnswer(inv -> inv.getArgument(0));

        mockMvc.perform(post("/carts/10/checkout"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message", is("Checkout successful")))
                .andExpect(jsonPath("$.cartId", is(10)))
                .andExpect(jsonPath("$.total", is(7.5)));

        ArgumentCaptor<Beer> beerCaptor = ArgumentCaptor.forClass(Beer.class);
        verify(cartService).findById(10L);
        verify(beerService, atLeastOnce()).findById(1L);
        verify(beerService).save(beerCaptor.capture());
        verify(cartService).deleteById(10L);

        assertEquals(7, beerCaptor.getValue().getStock());
        verifyNoMoreInteractions(cartService, beerService);
    }

    @Test
    // Vérifie que POST /carts/{id}/checkout retourne 409 si stock insuffisant.
    void checkout_whenNotEnoughStock_returns409() throws Exception {
        Beer beer = new Beer("Test", 2.5, 2);
        beer.setId(1L);

        Cart cart = new Cart();
        cart.setId(10L);
        cart.getItems().add(new CartItem(cart, beer, 3));

        when(cartService.findById(10L)).thenReturn(Optional.of(cart));
        when(beerService.findById(1L)).thenReturn(Optional.of(beer));

        mockMvc.perform(post("/carts/10/checkout"))
                .andExpect(status().isConflict());

        verify(cartService).findById(10L);
        verify(beerService).findById(1L);
        verifyNoMoreInteractions(cartService, beerService);
    }
}
