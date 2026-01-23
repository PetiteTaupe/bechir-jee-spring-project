package ch.hearc.jee2025.bechirjeespringproject.sales_log;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/sales_logs")
public class SalesLogController {

    public SalesLogController(SalesLogService salesLogService) {
        this.salesLogService = salesLogService;
    }

    @GetMapping("/top")
    public List<TopBeerSales> getTopBeers(@RequestParam(defaultValue = "10") int limit) {
        if (limit <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "limit must be greater than 0");
        }
        return salesLogService.getTopBeers(limit);
    }

    private final SalesLogService salesLogService;
}