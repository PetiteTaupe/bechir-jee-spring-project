package ch.hearc.jee2025.bechirjeespringproject.beer;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/beers")
public class BeerController {

    public BeerController(BeerService beerService) {
        this.beerService = beerService;
    }

    // CREATE/UPDATE
    @PostMapping
    public Beer create(@RequestBody Beer beer) {
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
        return beerService.save(beer);
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
}
