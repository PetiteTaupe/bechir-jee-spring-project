package ch.hearc.jee2025.bechirjeespringproject.sales_log;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface SalesLogItemRepository extends CrudRepository<SalesLogItem, Long> {

    @Query("select i.beerId as beerId, i.beerName as beerName, sum(i.quantity) as totalQuantity " +
            "from SalesLogItem i group by i.beerId, i.beerName order by sum(i.quantity) desc")
    Page<TopBeerSalesView> findTopBeerSales(Pageable pageable);
}