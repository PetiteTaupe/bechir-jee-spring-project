package ch.hearc.jee2025.bechirjeespringproject.beer;

import ch.hearc.jee2025.bechirjeespringproject.brewery.Brewery;
import ch.hearc.jee2025.bechirjeespringproject.brewery.BreweryService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/beers")
public class BeerController {

    public BeerController(BeerService beerService, BreweryService breweryService) {
        this.beerService = beerService;
        this.breweryService = breweryService;
    }

    // CREATE/UPDATE
    @PostMapping
    public Beer create(@RequestBody Beer beer) {
        attachManagedBrewery(beer);
        return beerService.save(beer);
    }

    @PutMapping("/{id}")
    public Beer update(@PathVariable Long id, @RequestBody Beer beer) {
        if (beerService.findById(id).isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Beer not found with id: " + id
            );
        }
        beer.setId(id);
        attachManagedBrewery(beer);
        return beerService.save(beer);
    }

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

    // READ
    @GetMapping
    public Iterable<Beer> getAll() {
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
    public Map<String, String> delete(@PathVariable Long id) {
        if (beerService.findById(id).isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Beer not found with id: " + id
            );
        }
        beerService.deleteById(id);
        return Map.of(
                "message", "Beer deleted successfully",
                "id", id.toString()
        );
    }

    private final BeerService beerService;
    private final BreweryService breweryService;
}
