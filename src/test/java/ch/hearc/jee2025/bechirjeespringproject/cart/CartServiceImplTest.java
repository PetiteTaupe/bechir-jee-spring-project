package ch.hearc.jee2025.bechirjeespringproject.cart;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    @Mock
    private CartRepository cartRepository;

    @InjectMocks
    private CartServiceImpl cartService;

    @Test
    // Vérifie que save() délègue à CartRepository.save().
    void save_delegatesToRepository() {
        Cart cart = new Cart();
        when(cartRepository.save(cart)).thenReturn(cart);

        Cart saved = cartService.save(cart);

        assertSame(cart, saved);
        verify(cartRepository).save(cart);
        verifyNoMoreInteractions(cartRepository);
    }

    @Test
    // Vérifie que findById() délègue à CartRepository.findById().
    void findById_delegatesToRepository() {
        Cart cart = new Cart();
        cart.setId(1L);
        when(cartRepository.findById(1L)).thenReturn(Optional.of(cart));

        Optional<Cart> result = cartService.findById(1L);

        assertTrue(result.isPresent());
        assertSame(cart, result.get());
        verify(cartRepository).findById(1L);
        verifyNoMoreInteractions(cartRepository);
    }

    @Test
    // Vérifie que findAll() délègue à CartRepository.findAll().
    void findAll_delegatesToRepository() {
        List<Cart> carts = List.of(new Cart(), new Cart());
        when(cartRepository.findAll()).thenReturn(carts);

        Iterable<Cart> result = cartService.findAll();

        assertSame(carts, result);
        verify(cartRepository).findAll();
        verifyNoMoreInteractions(cartRepository);
    }

    @Test
    // Vérifie que deleteById() supprime le panier quand il existe.
    void deleteById_whenCartExists_deletesIt() {
        Cart cart = new Cart();
        cart.setId(7L);
        when(cartRepository.findById(7L)).thenReturn(Optional.of(cart));

        cartService.deleteById(7L);

        verify(cartRepository).findById(7L);
        verify(cartRepository).delete(cart);
        verifyNoMoreInteractions(cartRepository);
    }

    @Test
    // Vérifie que deleteById() lève une exception quand le panier n'existe pas.
    void deleteById_whenCartMissing_throwsAndDoesNotDelete() {
        when(cartRepository.findById(7L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> cartService.deleteById(7L));
        assertTrue(ex.getMessage().contains("Cart not found"));

        verify(cartRepository).findById(7L);
        verify(cartRepository, never()).delete(any());
        verifyNoMoreInteractions(cartRepository);
    }
}
