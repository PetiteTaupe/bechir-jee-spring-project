package ch.hearc.jee2025.bechirjeespringproject.beer;

import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class BeerServiceImpl implements BeerService {

    public BeerServiceImpl(BeerRepository beerRepository) {
        this.beerRepository = beerRepository;
    }

    @Override
    public Beer save(Beer beer) {
        return beerRepository.save(beer);
    }

    @Override
    public Optional<Beer> findById(Long id) {
        return beerRepository.findById(id);
    }

    @Override
    public Iterable<Beer> findAll() {
        return beerRepository.findAll();
    }

    @Override
    public void deleteById(Long id) {
        Beer beer = beerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Beer not found with id: " + id));
        beerRepository.delete(beer);
    }


    private final BeerRepository beerRepository;
}
