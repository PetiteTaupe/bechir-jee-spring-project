package ch.hearc.jee2025.bechirjeespringproject.brewery;

import java.util.Optional;

public interface BreweryService {

    Brewery save(Brewery brewery);
    Optional<Brewery> findById(Long id);
    Iterable<Brewery> findAll();
    void deleteById(Long id);
}
