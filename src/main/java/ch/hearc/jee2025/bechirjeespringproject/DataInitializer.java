package ch.hearc.jee2025.bechirjeespringproject;

import ch.hearc.jee2025.bechirjeespringproject.beer.Beer;
import ch.hearc.jee2025.bechirjeespringproject.beer.BeerRepository;
import ch.hearc.jee2025.bechirjeespringproject.brewery.Brewery;
import ch.hearc.jee2025.bechirjeespringproject.brewery.BreweryRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("h2")
public class DataInitializer implements ApplicationRunner {

    public DataInitializer(BreweryRepository breweryRepository, BeerRepository beerRepository) {
        this.breweryRepository = breweryRepository;
        this.beerRepository = beerRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        Brewery feldschloesschen = new Brewery("Feldschlösschen", "Switzerland");
        Brewery cardinal = new Brewery("Cardinal", "Switzerland");
        Brewery calanda = new Brewery("Calanda", "Switzerland");
        Brewery bfm = new Brewery("BFM", "Switzerland");

        if (breweryRepository.count() == 0 && beerRepository.count() == 0) {

            breweryRepository.save(feldschloesschen);
            breweryRepository.save(cardinal);
            breweryRepository.save(calanda);
            breweryRepository.save(bfm);

            Beer cardinalBlonde = new Beer("Cardinal Blonde", 2.9, 60);
            cardinalBlonde.setBrewery(cardinal);
            beerRepository.save(cardinalBlonde);

            Beer calandaLager = new Beer("Calanda Lager", 3.0, 80);
            calandaLager.setBrewery(calanda);
            beerRepository.save(calandaLager);

            Beer feldschloesschenOriginal = new Beer("Feldschlösschen Original", 3.2, 120);
            feldschloesschenOriginal.setBrewery(feldschloesschen);
            beerRepository.save(feldschloesschenOriginal);

            Beer bfmLaMeule = new Beer("BFM La Meule", 4.5, 40);
            bfmLaMeule.setBrewery(bfm);
            beerRepository.save(bfmLaMeule);

            Beer bfmSalamandre = new Beer("BFM Salamandre", 4.5, 40);
            bfmSalamandre.setBrewery(bfm);
            beerRepository.save(bfmSalamandre);
        }
    }

    private final BreweryRepository breweryRepository;
    private final BeerRepository beerRepository;
}
