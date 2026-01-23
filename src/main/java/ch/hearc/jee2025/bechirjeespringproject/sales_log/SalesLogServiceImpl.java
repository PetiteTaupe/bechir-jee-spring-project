package ch.hearc.jee2025.bechirjeespringproject.sales_log;

import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SalesLogServiceImpl implements SalesLogService {

    public SalesLogServiceImpl(SalesLogRepository salesLogRepository) {
        this.salesLogRepository = salesLogRepository;
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

    private final SalesLogRepository salesLogRepository;
}