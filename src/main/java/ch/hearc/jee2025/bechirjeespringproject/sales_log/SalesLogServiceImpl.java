package ch.hearc.jee2025.bechirjeespringproject.sales_log;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.List;

@Service
public class SalesLogServiceImpl implements SalesLogService {

    public SalesLogServiceImpl(SalesLogRepository salesLogRepository, SalesLogItemRepository salesLogItemRepository) {
        this.salesLogRepository = salesLogRepository;
        this.salesLogItemRepository = salesLogItemRepository;
    }

    @Override
    public SalesLog save(SalesLog salesLog) {
        return salesLogRepository.save(salesLog);
    }

    @Override
    public Optional<SalesLog> findById(Long id) {
        return salesLogRepository.findById(id);
    }

    @Override
    public Iterable<SalesLog> findAll() {
        return salesLogRepository.findAll();
    }

    @Override
    public List<TopBeerSales> getTopBeers(int limit) {
        int pageSize = Math.max(limit, 1);
        return salesLogItemRepository.findTopBeerSales(PageRequest.of(0, pageSize))
                .map(view -> new TopBeerSales(
                        view.getBeerId(),
                        view.getBeerName(),
                        view.getTotalQuantity() == null ? 0 : view.getTotalQuantity()
                ))
                .getContent();
    }

    private final SalesLogRepository salesLogRepository;
    private final SalesLogItemRepository salesLogItemRepository;
}