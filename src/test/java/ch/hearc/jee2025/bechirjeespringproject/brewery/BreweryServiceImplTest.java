package ch.hearc.jee2025.bechirjeespringproject.brewery;

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
class BreweryServiceImplTest {

    @Mock
    private BreweryRepository breweryRepository;

    @InjectMocks
    private BreweryServiceImpl breweryService;

    @Test
    // Vérifie que save() délègue à BreweryRepository.save().
    void save_delegatesToRepository() {
        Brewery brewery = new Brewery("B", "CH");
        when(breweryRepository.save(brewery)).thenReturn(brewery);

        Brewery saved = breweryService.save(brewery);

        assertSame(brewery, saved);
        verify(breweryRepository).save(brewery);
        verifyNoMoreInteractions(breweryRepository);
    }

    @Test
    // Vérifie que findById() délègue à BreweryRepository.findById().
    void findById_delegatesToRepository() {
        Brewery brewery = new Brewery("B", "CH");
        brewery.setId(1L);
        when(breweryRepository.findById(1L)).thenReturn(Optional.of(brewery));

        Optional<Brewery> result = breweryService.findById(1L);

        assertTrue(result.isPresent());
        assertSame(brewery, result.get());
        verify(breweryRepository).findById(1L);
        verifyNoMoreInteractions(breweryRepository);
    }

    @Test
    // Vérifie que findAll() délègue à BreweryRepository.findAll().
    void findAll_delegatesToRepository() {
        List<Brewery> breweries = List.of(new Brewery("A", "CH"), new Brewery("B", "FR"));
        when(breweryRepository.findAll()).thenReturn(breweries);

        Iterable<Brewery> result = breweryService.findAll();

        assertSame(breweries, result);
        verify(breweryRepository).findAll();
        verifyNoMoreInteractions(breweryRepository);
    }

    @Test
    // Vérifie que deleteById() supprime la brasserie quand elle existe.
    void deleteById_whenBreweryExists_deletesIt() {
        Brewery brewery = new Brewery("B", "CH");
        brewery.setId(7L);
        when(breweryRepository.findById(7L)).thenReturn(Optional.of(brewery));

        breweryService.deleteById(7L);

        verify(breweryRepository).findById(7L);
        verify(breweryRepository).delete(brewery);
        verifyNoMoreInteractions(breweryRepository);
    }

    @Test
    // Vérifie que deleteById() lève une exception quand la brasserie n'existe pas.
    void deleteById_whenBreweryMissing_throwsAndDoesNotDelete() {
        when(breweryRepository.findById(7L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> breweryService.deleteById(7L));
        assertTrue(ex.getMessage().contains("Brewery not found"));

        verify(breweryRepository).findById(7L);
        verify(breweryRepository, never()).delete(any());
        verifyNoMoreInteractions(breweryRepository);
    }
}
