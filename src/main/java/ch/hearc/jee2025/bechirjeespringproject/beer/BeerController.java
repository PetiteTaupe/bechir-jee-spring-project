package ch.hearc.jee2025.bechirjeespringproject.beer;

import ch.hearc.jee2025.bechirjeespringproject.brewery.Brewery;
import ch.hearc.jee2025.bechirjeespringproject.brewery.BreweryService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

import static ch.hearc.jee2025.bechirjeespringproject.AdminUtils.checkAdminKey;

@RestController
@RequestMapping("/beers")
public class BeerController {

    public BeerController(BeerService beerService, BreweryService breweryService) {
        this.beerService = beerService;
        this.breweryService = breweryService;
    }

    // CREATE/UPDATE
    @PostMapping
    public Beer create(@RequestBody Beer beer, @RequestHeader(value = "X-ADMIN-KEY", required = false) String key) {
        checkAdminKey(key);
        validateBeerFields(beer);
        attachManagedBrewery(beer);
        return beerService.save(beer);
    }

    @PutMapping("/{id}")
    public Beer update(@PathVariable Long id, @RequestBody Beer beer, @RequestHeader(value = "X-ADMIN-KEY", required = false) String key) {
        checkAdminKey(key);
        if (beerService.findById(id).isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Beer not found with id: " + id
            );
        }
        beer.setId(id);
        validateBeerFields(beer);
        attachManagedBrewery(beer);
        return beerService.save(beer);
    }

    // READ
    @GetMapping
    public Iterable<Beer> getAll(@RequestParam(required = false) String country) {
        if (country != null && !country.isBlank()) {
            return beerService.findAllByCountry(country);
        }
        return beerService.findAll();
    }

    @GetMapping("/{id}")
    public Beer getById(@PathVariable Long id) {
        return beerService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Beer not found with id: " + id
                ));
    }

    // DELETE
    @DeleteMapping("/{id}")
    public Map<String, String> delete(@PathVariable Long id, @RequestHeader(value = "X-ADMIN-KEY", required = false) String key) {
        checkAdminKey(key);
        try {
            beerService.deleteById(id);
            return Map.of(
                    "message", "Beer deleted successfully",
                    "id", id.toString()
            );
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Beer cannot be deleted because it is used in one or more carts");
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    // utils
    private void attachManagedBrewery(Beer beer) {
        Brewery brewery = beer.getBrewery();
        if (brewery == null || brewery.getId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "brewery.id is required"
            );
        }

        Brewery managedBrewery = breweryService.findById(brewery.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Brewery not found with id: " + brewery.getId()
                ));
        beer.setBrewery(managedBrewery);
    }

    private void validateBeerFields(Beer beer) {
        if (beer.getName() == null || beer.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "name is required");
        }
        if (beer.getPrice() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "price must be greater than or equal to 0");
        }
        if (beer.getStock() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "stock must be greater than or equal to 0");
        }
    }

    private final BeerService beerService;
    private final BreweryService breweryService;
}
