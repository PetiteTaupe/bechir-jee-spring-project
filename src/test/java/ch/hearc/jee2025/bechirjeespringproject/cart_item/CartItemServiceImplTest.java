package ch.hearc.jee2025.bechirjeespringproject.cart_item;

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
class CartItemServiceImplTest {

    @Mock
    private CartItemRepository cartItemRepository;

    @InjectMocks
    private CartItemServiceImpl cartItemService;

    @Test
    // Vérifie que save() délègue à CartItemRepository.save().
    void save_delegatesToRepository() {
        CartItem item = mock(CartItem.class);
        when(cartItemRepository.save(item)).thenReturn(item);

        CartItem saved = cartItemService.save(item);

        assertSame(item, saved);
        verify(cartItemRepository).save(item);
        verifyNoMoreInteractions(cartItemRepository);
    }

    @Test
    // Vérifie que findById() délègue à CartItemRepository.findById().
    void findById_delegatesToRepository() {
        CartItem item = mock(CartItem.class);
        when(cartItemRepository.findById(1L)).thenReturn(Optional.of(item));

        Optional<CartItem> result = cartItemService.findById(1L);

        assertTrue(result.isPresent());
        assertSame(item, result.get());
        verify(cartItemRepository).findById(1L);
        verifyNoMoreInteractions(cartItemRepository);
    }

    @Test
    // Vérifie que findAll() délègue à CartItemRepository.findAll().
    void findAll_delegatesToRepository() {
        List<CartItem> items = List.of(mock(CartItem.class), mock(CartItem.class));
        when(cartItemRepository.findAll()).thenReturn(items);

        Iterable<CartItem> result = cartItemService.findAll();

        assertSame(items, result);
        verify(cartItemRepository).findAll();
        verifyNoMoreInteractions(cartItemRepository);
    }

    @Test
    // Vérifie que deleteById() supprime l'item quand il existe.
    void deleteById_whenItemExists_deletesIt() {
        CartItem item = mock(CartItem.class);
        when(cartItemRepository.findById(7L)).thenReturn(Optional.of(item));

        cartItemService.deleteById(7L);

        verify(cartItemRepository).findById(7L);
        verify(cartItemRepository).delete(item);
        verifyNoMoreInteractions(cartItemRepository);
    }

    @Test
    // Vérifie que deleteById() lève une exception quand l'item n'existe pas.
    void deleteById_whenItemMissing_throwsAndDoesNotDelete() {
        when(cartItemRepository.findById(7L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> cartItemService.deleteById(7L));
        assertTrue(ex.getMessage().contains("Cart item not found"));

        verify(cartItemRepository).findById(7L);
        verify(cartItemRepository, never()).delete(any());
        verifyNoMoreInteractions(cartItemRepository);
    }
}
