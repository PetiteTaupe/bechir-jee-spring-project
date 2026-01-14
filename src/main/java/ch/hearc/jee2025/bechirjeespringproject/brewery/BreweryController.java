package ch.hearc.jee2025.bechirjeespringproject.brewery;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/breweries")
public class BreweryController {

    public BreweryController(BreweryService breweryService) {
        this.breweryService = breweryService;
    }

    // CREATE/UPDATE
    @PostMapping
    public Brewery create(@RequestBody Brewery brewery) {
        return breweryService.save(brewery);
    }

    @PutMapping("/{id}")
    public Brewery update(@PathVariable Long id, @RequestBody Brewery brewery) {
        if (breweryService.findById(id).isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Brewery not found with id: " + id
            );
        }
        brewery.setId(id);
        return breweryService.save(brewery);
    }

    // READ
    @GetMapping
    public Iterable<Brewery> getAll() {
        return breweryService.findAll();
    }

    @GetMapping("/{id}")
    public Brewery getById(@PathVariable Long id) {
        return breweryService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Brewery not found with id: " + id
                ));
    }

    // DELETE
    @DeleteMapping("/{id}")
    public Map<String, String> delete(@PathVariable Long id) {
        try {
            breweryService.deleteById(id);
            return Map.of(
                    "message", "Brewery deleted successfully",
                    "id", id.toString()
            );
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    private final BreweryService breweryService;
}
