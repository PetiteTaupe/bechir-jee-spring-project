package ch.hearc.jee2025.bechirjeespringproject.beer;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface BeerRepository extends CrudRepository<Beer,Long> {

	List<Beer> findByBrewery_CountryIgnoreCase(String country);

}
