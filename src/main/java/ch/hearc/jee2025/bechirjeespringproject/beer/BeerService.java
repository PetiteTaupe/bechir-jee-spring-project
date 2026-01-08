package ch.hearc.jee2025.bechirjeespringproject.beer;

import java.util.Optional;

public interface BeerService {

    Beer save(Beer beer);
    Optional<Beer> findById(Long id);
    Iterable<Beer> findAll();
    void deleteById(Long id);
}
