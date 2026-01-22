package ch.hearc.jee2025.bechirjeespringproject.brewery;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

import static ch.hearc.jee2025.bechirjeespringproject.AdminUtils.checkAdminKey;

@RestController
@RequestMapping("/breweries")
public class BreweryController {

    public BreweryController(BreweryService breweryService) {
        this.breweryService = breweryService;
    }

    // CREATE/UPDATE
    @PostMapping
    public Brewery create(@RequestBody Brewery brewery, @RequestHeader(value = "X-ADMIN-KEY", required = false) String key) {
        checkAdminKey(key);
        return breweryService.save(brewery);
    }

    @PutMapping("/{id}")
    public Brewery update(@PathVariable Long id, @RequestBody Brewery brewery, @RequestHeader(value = "X-ADMIN-KEY", required = false) String key) {
        checkAdminKey(key);
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
    public Map<String, String> delete(@PathVariable Long id, @RequestHeader(value = "X-ADMIN-KEY", required = false) String key) {
        checkAdminKey(key);
        try {
            breweryService.deleteById(id);
            return Map.of(
                    "message", "Brewery deleted successfully",
                    "id", id.toString()
            );
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Brewery cannot be deleted because it is linked to one or more beers");
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    private final BreweryService breweryService;
}
