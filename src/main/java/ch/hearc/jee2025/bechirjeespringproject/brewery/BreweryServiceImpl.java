package ch.hearc.jee2025.bechirjeespringproject.brewery;

import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class BreweryServiceImpl implements BreweryService{

    public BreweryServiceImpl(BreweryRepository beerRepository) {
        this.breweryRepository = beerRepository;
    }

    @Override
    public Brewery save(Brewery brewery) {
        return breweryRepository.save(brewery);
    }

    @Override
    public Optional<Brewery> findById(Long id) {
        return breweryRepository.findById(id);
    }

    @Override
    public Iterable<Brewery> findAll() {
        return breweryRepository.findAll();
    }

    @Override
    public void deleteById(Long id) {
        Brewery brewery = breweryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Brewery not found with id: " + id));
        breweryRepository.delete(brewery);
    }

    private final BreweryRepository breweryRepository;
}
