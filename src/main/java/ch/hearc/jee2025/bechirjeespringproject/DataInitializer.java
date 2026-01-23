package ch.hearc.jee2025.bechirjeespringproject;

import ch.hearc.jee2025.bechirjeespringproject.beer.Beer;
import ch.hearc.jee2025.bechirjeespringproject.beer.BeerRepository;
import ch.hearc.jee2025.bechirjeespringproject.brewery.Brewery;
import ch.hearc.jee2025.bechirjeespringproject.brewery.BreweryRepository;
import ch.hearc.jee2025.bechirjeespringproject.cart.Cart;
import ch.hearc.jee2025.bechirjeespringproject.cart.CartRepository;
import ch.hearc.jee2025.bechirjeespringproject.cart_item.CartItem;
import ch.hearc.jee2025.bechirjeespringproject.cart_item.CartItemRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("h2")
public class DataInitializer implements ApplicationRunner {

    public DataInitializer(BreweryRepository breweryRepository, BeerRepository beerRepository, CartRepository cartRepository, CartItemRepository cartItemRepository) {
        this.breweryRepository = breweryRepository;
        this.beerRepository = beerRepository;
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        Brewery feldschloesschen = new Brewery("Feldschlösschen", "Switzerland");
        Brewery cardinal = new Brewery("Cardinal", "Switzerland");
        Brewery calanda = new Brewery("Calanda", "Switzerland");
        Brewery bfm = new Brewery("BFM", "Switzerland");
        Brewery boxer = new Brewery("Boxer", "Switzerland");
        Brewery heineken = new Brewery("Heineken", "Netherlands");
        Brewery duvel = new Brewery("Duvel", "Belgium");
        Brewery guinness = new Brewery("Guinness", "Ireland");

        if (breweryRepository.count() == 0 && beerRepository.count() == 0) {

            breweryRepository.save(feldschloesschen);
            breweryRepository.save(cardinal);
            breweryRepository.save(calanda);
            breweryRepository.save(bfm);
            breweryRepository.save(boxer);
            breweryRepository.save(heineken);
            breweryRepository.save(duvel);
            breweryRepository.save(guinness);

            Beer cardinalBlonde = new Beer("Cardinal Blonde", 2.9, 60);
            cardinalBlonde.setBrewery(cardinal);
            beerRepository.save(cardinalBlonde);

            Beer cardinalSpecial = new Beer("Cardinal Spéciale", 3.1, 45);
            cardinalSpecial.setBrewery(cardinal);
            beerRepository.save(cardinalSpecial);

            Beer calandaLager = new Beer("Calanda Lager", 3.0, 80);
            calandaLager.setBrewery(calanda);
            beerRepository.save(calandaLager);

            Beer calandaAmber = new Beer("Calanda Amber", 3.4, 50);
            calandaAmber.setBrewery(calanda);
            beerRepository.save(calandaAmber);

            Beer feldschloesschenOriginal = new Beer("Feldschlösschen Original", 3.2, 120);
            feldschloesschenOriginal.setBrewery(feldschloesschen);
            beerRepository.save(feldschloesschenOriginal);

            Beer feldschloesschenHopfenperle = new Beer("Feldschlösschen Hopfenperle", 3.6, 70);
            feldschloesschenHopfenperle.setBrewery(feldschloesschen);
            beerRepository.save(feldschloesschenHopfenperle);

            Beer bfmLaMeule = new Beer("BFM La Meule", 4.5, 40);
            bfmLaMeule.setBrewery(bfm);
            beerRepository.save(bfmLaMeule);

            Beer bfmSalamandre = new Beer("BFM Salamandre", 4.5, 40);
            bfmSalamandre.setBrewery(bfm);
            beerRepository.save(bfmSalamandre);

            Beer bfmAbbaye = new Beer("BFM Abbaye de Saint Bon-Chien", 6.2, 25);
            bfmAbbaye.setBrewery(bfm);
            beerRepository.save(bfmAbbaye);

            Beer boxerBlonde = new Beer("Boxer Blonde", 2.6, 90);
            boxerBlonde.setBrewery(boxer);
            beerRepository.save(boxerBlonde);

            Beer boxerIPA = new Beer("Boxer IPA", 3.9, 55);
            boxerIPA.setBrewery(boxer);
            beerRepository.save(boxerIPA);

            Beer heinekenOriginal = new Beer("Heineken", 2.8, 200);
            heinekenOriginal.setBrewery(heineken);
            beerRepository.save(heinekenOriginal);

            Beer duvelClassic = new Beer("Duvel", 4.9, 35);
            duvelClassic.setBrewery(duvel);
            beerRepository.save(duvelClassic);

            Beer guinnessDraught = new Beer("Guinness Draught", 4.2, 60);
            guinnessDraught.setBrewery(guinness);
            beerRepository.save(guinnessDraught);

            Cart cart1 = new Cart();
            CartItem item1 = new CartItem(cart1, cardinalBlonde, 3);
            CartItem item1b = new CartItem(cart1, feldschloesschenOriginal, 2);
            cart1.getItems().add(item1);
            cart1.getItems().add(item1b);

            cartRepository.save(cart1);
            cartItemRepository.save(item1);
            cartItemRepository.save(item1b);

            Cart cart2 = new Cart();
            CartItem item2a = new CartItem(cart2, calandaLager, 10);
            CartItem item2b = new CartItem(cart2, bfmLaMeule, 2);
            CartItem item2c = new CartItem(cart2, bfmSalamandre, 5);

            cart2.getItems().add(item2a);
            cart2.getItems().add(item2b);
            cart2.getItems().add(item2c);

            cartRepository.save(cart2);

            Cart cart3 = new Cart();
            CartItem item3a = new CartItem(cart3, boxerBlonde, 12);
            CartItem item3b = new CartItem(cart3, boxerIPA, 4);
            CartItem item3c = new CartItem(cart3, heinekenOriginal, 6);

            cart3.getItems().add(item3a);
            cart3.getItems().add(item3b);
            cart3.getItems().add(item3c);

            cartRepository.save(cart3);

            Cart cart4 = new Cart();
            CartItem item4a = new CartItem(cart4, duvelClassic, 3);
            CartItem item4b = new CartItem(cart4, guinnessDraught, 8);
            CartItem item4c = new CartItem(cart4, calandaAmber, 5);

            cart4.getItems().add(item4a);
            cart4.getItems().add(item4b);
            cart4.getItems().add(item4c);

            cartRepository.save(cart4);

            Cart cart5 = new Cart();
            CartItem item5a = new CartItem(cart5, bfmAbbaye, 2);
            CartItem item5b = new CartItem(cart5, feldschloesschenHopfenperle, 6);
            CartItem item5c = new CartItem(cart5, cardinalSpecial, 4);

            cart5.getItems().add(item5a);
            cart5.getItems().add(item5b);
            cart5.getItems().add(item5c);

            cartRepository.save(cart5);
        }
    }

    private final BreweryRepository breweryRepository;
    private final BeerRepository beerRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
}
