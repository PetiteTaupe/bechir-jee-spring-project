package ch.hearc.jee2025.bechirjeespringproject.beer;

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
class BeerServiceImplTest {

    @Mock
    private BeerRepository beerRepository;

    @InjectMocks
    private BeerServiceImpl beerService;

    @Test
    // Vérifie que save() délègue à BeerRepository.save().
    void save_delegatesToRepository() {
        Beer beer = new Beer("Test", 2.5, 10);
        when(beerRepository.save(beer)).thenReturn(beer);

        Beer saved = beerService.save(beer);

        assertSame(beer, saved);
        verify(beerRepository).save(beer);
        verifyNoMoreInteractions(beerRepository);
    }

    @Test
    // Vérifie que findById() délègue à BeerRepository.findById().
    void findById_delegatesToRepository() {
        Beer beer = new Beer("Test", 2.5, 10);
        beer.setId(1L);
        when(beerRepository.findById(1L)).thenReturn(Optional.of(beer));

        Optional<Beer> result = beerService.findById(1L);

        assertTrue(result.isPresent());
        assertSame(beer, result.get());
        verify(beerRepository).findById(1L);
        verifyNoMoreInteractions(beerRepository);
    }

    @Test
    // Vérifie que findAll() délègue à BeerRepository.findAll().
    void findAll_delegatesToRepository() {
        List<Beer> beers = List.of(new Beer("A", 1.0, 1), new Beer("B", 2.0, 2));
        when(beerRepository.findAll()).thenReturn(beers);

        Iterable<Beer> result = beerService.findAll();

        assertSame(beers, result);
        verify(beerRepository).findAll();
        verifyNoMoreInteractions(beerRepository);
    }

    @Test
    // Vérifie que findAllByCountry() délègue à BeerRepository.findByBrewery_CountryIgnoreCase().
    void findAllByCountry_delegatesToRepository() {
        List<Beer> beers = List.of(new Beer("A", 1.0, 1));
        when(beerRepository.findByBrewery_CountryIgnoreCase("CH")).thenReturn(beers);

        Iterable<Beer> result = beerService.findAllByCountry("CH");

        assertSame(beers, result);
        verify(beerRepository).findByBrewery_CountryIgnoreCase("CH");
        verifyNoMoreInteractions(beerRepository);
    }

    @Test
    // Vérifie que deleteById() supprime la bière quand elle existe.
    void deleteById_whenBeerExists_deletesIt() {
        Beer beer = new Beer("Test", 2.5, 10);
        beer.setId(7L);
        when(beerRepository.findById(7L)).thenReturn(Optional.of(beer));

        beerService.deleteById(7L);

        verify(beerRepository).findById(7L);
        verify(beerRepository).delete(beer);
        verifyNoMoreInteractions(beerRepository);
    }

    @Test
    // Vérifie que deleteById() lève une exception quand la bière n'existe pas.
    void deleteById_whenBeerMissing_throwsAndDoesNotDelete() {
        when(beerRepository.findById(7L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> beerService.deleteById(7L));
        assertTrue(ex.getMessage().contains("Beer not found"));

        verify(beerRepository).findById(7L);
        verify(beerRepository, never()).delete(any());
        verifyNoMoreInteractions(beerRepository);
    }
}
