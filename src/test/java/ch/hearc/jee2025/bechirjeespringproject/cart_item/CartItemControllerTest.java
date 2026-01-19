package ch.hearc.jee2025.bechirjeespringproject.cart_item;

import ch.hearc.jee2025.bechirjeespringproject.beer.Beer;
import ch.hearc.jee2025.bechirjeespringproject.beer.BeerService;
import ch.hearc.jee2025.bechirjeespringproject.cart.Cart;
import ch.hearc.jee2025.bechirjeespringproject.cart.CartService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CartItemControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private CartItemService cartItemService;
    private CartService cartService;
    private BeerService beerService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        cartItemService = mock(CartItemService.class);
        cartService = mock(CartService.class);
        beerService = mock(BeerService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new CartItemController(cartItemService, cartService, beerService)).build();
    }

    @Test
    // Vérifie que GET /cart_items retourne 200 et une liste JSON.
    void getAll_returns200() throws Exception {
        Cart cart = new Cart();
        cart.setId(1L);
        Beer beer = new Beer("A", 1.0, 10);
        beer.setId(2L);

        CartItem i1 = new CartItem(cart, beer, 1);
        i1.setId(10L);
        CartItem i2 = new CartItem(cart, beer, 2);
        i2.setId(11L);

        when(cartItemService.findAll()).thenReturn(List.of(i1, i2));

        mockMvc.perform(get("/cart_items"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(10)));

        verify(cartItemService).findAll();
        verifyNoMoreInteractions(cartItemService, cartService, beerService);
    }

    @Test
    // Vérifie que GET /cart_items/{id} retourne 200 quand l'item existe.
    void getById_whenFound_returns200() throws Exception {
        Cart cart = new Cart();
        cart.setId(1L);
        Beer beer = new Beer("A", 1.0, 10);
        beer.setId(2L);

        CartItem item = new CartItem(cart, beer, 1);
        item.setId(10L);

        when(cartItemService.findById(10L)).thenReturn(Optional.of(item));

        mockMvc.perform(get("/cart_items/10"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(10)))
                .andExpect(jsonPath("$.quantity", is(1)));

        verify(cartItemService).findById(10L);
        verifyNoMoreInteractions(cartItemService, cartService, beerService);
    }

    @Test
    // Vérifie que GET /cart_items/{id} retourne 404 quand l'item n'existe pas.
    void getById_whenMissing_returns404() throws Exception {
        when(cartItemService.findById(10L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/cart_items/10"))
                .andExpect(status().isNotFound());

        verify(cartItemService).findById(10L);
        verifyNoMoreInteractions(cartItemService, cartService, beerService);
    }

    @Test
    // Vérifie que POST /cart_items retourne 400 si cart.id est manquant.
    void create_missingCartId_returns400() throws Exception {
        String payload = "{\"quantity\":1,\"cart\":{},\"beer\":{\"id\":2}}";

        mockMvc.perform(post("/cart_items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(cartItemService, cartService, beerService);
    }

    @Test
    // Vérifie que POST /cart_items retourne 404 si cart.id n'existe pas.
    void create_cartNotFound_returns404() throws Exception {
        when(cartService.findById(1L)).thenReturn(Optional.empty());

        String payload = "{\"quantity\":1,\"cart\":{\"id\":1},\"beer\":{\"id\":2}}";

        mockMvc.perform(post("/cart_items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isNotFound());

        verify(cartService).findById(1L);
        verifyNoMoreInteractions(cartService);
        verifyNoInteractions(cartItemService, beerService);
    }

    @Test
    // Vérifie que POST /cart_items retourne 400 si beer.id est manquant.
    void create_missingBeerId_returns400() throws Exception {
        Cart cart = new Cart();
        cart.setId(1L);
        when(cartService.findById(1L)).thenReturn(Optional.of(cart));

        String payload = "{\"quantity\":1,\"cart\":{\"id\":1},\"beer\":{}}";

        mockMvc.perform(post("/cart_items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());

        verify(cartService).findById(1L);
        verifyNoMoreInteractions(cartService);
        verifyNoInteractions(cartItemService, beerService);
    }

    @Test
    // Vérifie que POST /cart_items retourne 404 si beer.id n'existe pas.
    void create_beerNotFound_returns404() throws Exception {
        Cart cart = new Cart();
        cart.setId(1L);
        when(cartService.findById(1L)).thenReturn(Optional.of(cart));
        when(beerService.findById(2L)).thenReturn(Optional.empty());

        String payload = "{\"quantity\":1,\"cart\":{\"id\":1},\"beer\":{\"id\":2}}";

        mockMvc.perform(post("/cart_items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isNotFound());

        verify(cartService).findById(1L);
        verify(beerService).findById(2L);
        verifyNoMoreInteractions(cartService, beerService);
        verifyNoInteractions(cartItemService);
    }

    @Test
    // Vérifie que POST /cart_items retourne 400 si quantity <= 0.
    void create_invalidQuantity_returns400() throws Exception {
        Cart cart = new Cart();
        cart.setId(1L);
        Beer beer = new Beer("A", 1.0, 10);
        beer.setId(2L);
        when(cartService.findById(1L)).thenReturn(Optional.of(cart));
        when(beerService.findById(2L)).thenReturn(Optional.of(beer));

        String payload = "{\"quantity\":0,\"cart\":{\"id\":1},\"beer\":{\"id\":2}}";

        mockMvc.perform(post("/cart_items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());

        verify(cartService).findById(1L);
        verify(beerService).findById(2L);
        verifyNoMoreInteractions(cartService, beerService);
        verifyNoInteractions(cartItemService);
    }

    @Test
    // Vérifie que POST /cart_items attache cart+beer managés et sauvegarde.
    void create_withValidPayload_attachesManagedEntities_andSaves() throws Exception {
        Cart managedCart = new Cart();
        managedCart.setId(1L);
        Beer managedBeer = new Beer("A", 1.0, 10);
        managedBeer.setId(2L);

        when(cartService.findById(1L)).thenReturn(Optional.of(managedCart));
        when(beerService.findById(2L)).thenReturn(Optional.of(managedBeer));
        when(cartItemService.save(any(CartItem.class))).thenAnswer(inv -> inv.getArgument(0));

        String payload = "{\"quantity\":2,\"cart\":{\"id\":1},\"beer\":{\"id\":2}}";

        mockMvc.perform(post("/cart_items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk());

        ArgumentCaptor<CartItem> captor = ArgumentCaptor.forClass(CartItem.class);
        verify(cartService).findById(1L);
        verify(beerService).findById(2L);
        verify(cartItemService).save(captor.capture());

        CartItem saved = captor.getValue();
        assertSame(managedCart, saved.getCart());
        assertSame(managedBeer, saved.getBeer());
        assertEquals(2, saved.getQuantity());

        verifyNoMoreInteractions(cartItemService, cartService, beerService);
    }

    @Test
    // Vérifie que PUT /cart_items/{id} retourne 404 si l'item n'existe pas.
    void update_whenMissing_returns404() throws Exception {
        when(cartItemService.findById(10L)).thenReturn(Optional.empty());

        mockMvc.perform(put("/cart_items/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isNotFound());

        verify(cartItemService).findById(10L);
        verifyNoMoreInteractions(cartItemService, cartService, beerService);
    }

    @Test
    // Vérifie que PUT /cart_items/{id} force l'id, attache cart+beer managés et sauvegarde.
    void update_whenFound_setsId_attachesManagedEntities_andSaves() throws Exception {
        Cart managedCart = new Cart();
        managedCart.setId(1L);
        Beer managedBeer = new Beer("A", 1.0, 10);
        managedBeer.setId(2L);

        CartItem existing = new CartItem(managedCart, managedBeer, 1);
        existing.setId(10L);

        when(cartItemService.findById(10L)).thenReturn(Optional.of(existing));
        when(cartService.findById(1L)).thenReturn(Optional.of(managedCart));
        when(beerService.findById(2L)).thenReturn(Optional.of(managedBeer));
        when(cartItemService.save(any(CartItem.class))).thenAnswer(inv -> inv.getArgument(0));

        String payload = "{\"quantity\":3,\"cart\":{\"id\":1},\"beer\":{\"id\":2}}";

        mockMvc.perform(put("/cart_items/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk());

        ArgumentCaptor<CartItem> captor = ArgumentCaptor.forClass(CartItem.class);
        verify(cartItemService).findById(10L);
        verify(cartService).findById(1L);
        verify(beerService).findById(2L);
        verify(cartItemService).save(captor.capture());

        CartItem saved = captor.getValue();
        assertEquals(10L, saved.getId());
        assertSame(managedCart, saved.getCart());
        assertSame(managedBeer, saved.getBeer());
        assertEquals(3, saved.getQuantity());

        verifyNoMoreInteractions(cartItemService, cartService, beerService);
    }

    @Test
    // Vérifie que DELETE /cart_items/{id} retourne 404 si l'item n'existe pas.
    void delete_whenNotFound_returns404() throws Exception {
        doThrow(new RuntimeException("Cart item not found with id: 10")).when(cartItemService).deleteById(10L);

        mockMvc.perform(delete("/cart_items/10"))
                .andExpect(status().isNotFound());

        verify(cartItemService).deleteById(10L);
        verifyNoMoreInteractions(cartItemService, cartService, beerService);
    }

    @Test
    // Vérifie que DELETE /cart_items/{id} retourne 200 et un message de succès.
    void delete_whenOk_returns200AndMessage() throws Exception {
        mockMvc.perform(delete("/cart_items/10"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message", is("Cart item deleted successfully")))
                .andExpect(jsonPath("$.id", is("10")));

        verify(cartItemService).deleteById(10L);
        verifyNoMoreInteractions(cartItemService, cartService, beerService);
    }
}
