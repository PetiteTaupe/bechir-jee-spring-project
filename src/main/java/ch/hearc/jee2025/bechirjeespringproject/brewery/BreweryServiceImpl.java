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
        if (!breweryRepository.existsById(id)) {
            throw new RuntimeException("Beer not found with id: " + id);
        }
        breweryRepository.deleteById(id);
    }

    private final BreweryRepository breweryRepository;
}
