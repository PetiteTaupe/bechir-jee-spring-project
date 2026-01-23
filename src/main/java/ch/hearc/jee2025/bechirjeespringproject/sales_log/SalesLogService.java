package ch.hearc.jee2025.bechirjeespringproject.sales_log;

import java.util.Optional;

public interface SalesLogService {

    SalesLog save(SalesLog salesLog);
    Optional<SalesLog> findById(Long id);
    Iterable<SalesLog> findAll();
}