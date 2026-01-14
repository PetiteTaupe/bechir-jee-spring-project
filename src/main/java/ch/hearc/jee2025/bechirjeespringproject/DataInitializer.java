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

            Cart cart1 = new Cart();
            CartItem item1 = new CartItem(cart1, cardinalBlonde, 3);
            cart1.getItems().add(item1);

            cartRepository.save(cart1);
            cartItemRepository.save(item1);

            Cart cart2 = new Cart();
            CartItem item2a = new CartItem(cart2, calandaLager, 10);
            CartItem item2b = new CartItem(cart2, bfmLaMeule, 2);
            CartItem item2c = new CartItem(cart2, bfmSalamandre, 5);

            cart2.getItems().add(item2a);
            cart2.getItems().add(item2b);
            cart2.getItems().add(item2c);

            cartRepository.save(cart2);
        }
    }

    private final BreweryRepository breweryRepository;
    private final BeerRepository beerRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
}
